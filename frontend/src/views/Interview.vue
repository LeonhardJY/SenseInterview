<template>
  <div class="interview">
    <div class="interview-top card" style="padding:var(--spacing-4) var(--spacing-5);margin-bottom:var(--spacing-4)">
      <div style="display:flex;align-items:center;gap:var(--spacing-4)">
        <button class="btn btn--ghost btn--sm" @click="$router.push('/lobby')" style="font-size:16px">←</button>
        <div>
          <h4 style="margin:0">AI 面试房间</h4>
          <p class="text-xs text-muted" style="margin-top:2px">{{ interviewInfo.jobName || '加载中...' }} · {{ modeText(interviewInfo.mode) }} · {{ interviewInfo.difficulty }}</p>
        </div>
        <span class="status" :class="'status--' + (statusClass || 'running')" style="margin-left:auto">{{ statusText }}</span>
      </div>
      <button class="btn btn--ghost btn--sm" style="color:var(--color-danger)" @click="endInterview">结束</button>
    </div>

    <div class="interview-body">
      <div class="chat-col">
        <div class="card" style="flex:1;display:flex;flex-direction:column;padding:0;overflow:hidden">
          <div class="chat-scroll" ref="chatContainer">
            <div v-if="isFirstLoading" class="loading-msg">
              <div class="spinner"></div>
              <p class="text-sm text-muted">面试官正在出题中...</p>
            </div>
            <div v-for="(msg, index) in messages" :key="index" class="msg" :class="'msg--' + msg.type">
              <div class="msg-avatar" :class="'msg-avatar--' + msg.type">{{ msg.type === 'ai' ? 'AI' : '我' }}</div>
              <div>
                <div class="msg-bubble" :class="'msg-bubble--' + msg.type">{{ msg.content }}</div>
                <div class="msg-time">{{ msg.time }}</div>
              </div>
            </div>
          </div>
          <div style="padding:var(--spacing-4);border-top:1px solid var(--color-divider)">
            <div style="display:flex;gap:8px;margin-bottom:8px">
              <button class="btn btn--sm" :class="{ 'btn--primary': listeningMode === 'browser', 'btn--ghost': listeningMode !== 'browser' }" @click="toggleVoice">
                {{ listeningMode === 'browser' ? '🎤 停止' : '🎤 语音' }}
              </button>
              <span v-if="listeningMode === 'browser'" class="text-xs" style="color:var(--color-accent);align-self:center">正在识别...</span>
            </div>
            <div style="display:flex;gap:10px">
              <textarea v-model="inputMessage" class="form-textarea" style="flex:1;min-height:60px;height:60px" :placeholder="listeningMode === 'browser' ? '正在识别...' : '输入你的回答... (Ctrl+Enter)'" @keydown.ctrl.enter="sendMessage"></textarea>
              <button class="btn btn--primary" style="height:60px;width:60px;padding:0;justify-content:center;font-size:18px" @click="sendMessage" :disabled="!inputMessage.trim() || sending">→</button>
            </div>
          </div>
        </div>
      </div>

      <div class="side-col">
        <div class="card" style="padding:var(--spacing-4)">
          <p class="eyebrow" style="margin-bottom:var(--spacing-3)">Progress</p>
          <div style="height:4px;background:var(--color-border);border-radius:2px;overflow:hidden;margin-bottom:var(--spacing-2)">
            <div style="height:100%;background:var(--color-accent);border-radius:2px;transition:width 0.3s" :style="{ width: progress + '%' }"></div>
          </div>
          <p class="text-sm text-muted text-center">{{ currentRound }} / {{ totalRounds }} 轮</p>
        </div>
        <div class="card" style="padding:var(--spacing-4)">
          <p class="eyebrow" style="margin-bottom:var(--spacing-2)">Question</p>
          <p class="text-sm" style="color:var(--color-text-body);line-height:1.6">{{ currentQuestion || '等待出题...' }}</p>
        </div>
        <div class="card" style="padding:var(--spacing-4)">
          <p class="eyebrow" style="margin-bottom:var(--spacing-3)">Info</p>
          <div style="display:flex;flex-direction:column;gap:8px">
            <div style="display:flex;justify-content:space-between;font-size:13px"><span class="text-muted">岗位</span><span style="color:var(--color-text-primary);font-weight:500">{{ interviewInfo.jobName }}</span></div>
            <div style="display:flex;justify-content:space-between;font-size:13px"><span class="text-muted">模式</span><span style="color:var(--color-text-primary)">{{ modeText(interviewInfo.mode) }}</span></div>
            <div style="display:flex;justify-content:space-between;font-size:13px"><span class="text-muted">难度</span><span style="color:var(--color-text-primary)">{{ interviewInfo.difficulty }}</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 摄像头 -->
    <div v-if="interviewInfo.mode === 'VIDEO'" class="cam-float">
      <CameraFeed ref="cameraRef" :mirrored="true" @emotion="onEmotionResult" @error="onCameraError" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import api from '@/api'
import CameraFeed from '@/components/CameraFeed.vue'
import { createSpeechRecognition, isSpeechRecognitionSupported } from '@/utils/speechRecognition'
import { createInterviewSocket } from '@/utils/websocket'
import { fetchStream } from '@/utils/sse'
import { modeText } from '@/utils/constants'

const route = useRoute(); const router = useRouter(); const userStore = useUserStore()
const taskId = route.params.taskId

const chatContainer = ref(null); const inputMessage = ref(''); const sending = ref(false)
const messages = ref([]); const currentRound = ref(1); const totalRounds = ref(5)
const currentQuestion = ref(''); const interviewInfo = ref({}); const qaList = ref([])
const statusClass = ref('running'); const statusText = ref('进行中'); const progress = ref(20)
const isFirstLoading = ref(true); const isGenerating = ref(false)
const speechSupported = ref(isSpeechRecognitionSupported()); const listeningMode = ref('none')
const speechRecognition = ref(null); const ws = ref(null); const cameraRef = ref(null)

const isVoiceMode = computed(() => interviewInfo.value.mode === 'VOICE' || interviewInfo.value.mode === 'VIDEO')

const scrollToBottom = async () => { await nextTick(); if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight }
const addMessage = (type, content) => {
  const now = new Date(); const time = `${now.getHours().toString().padStart(2,'0')}:${now.getMinutes().toString().padStart(2,'0')}`
  messages.value.push({ type, content, time }); scrollToBottom()
}

onMounted(async () => {
  initWebSocket(); await loadInterviewInfo(); await loadFirstQuestion(); initSpeechRecognition()
  setTimeout(() => {
    if (interviewInfo.value.mode === 'VIDEO' && cameraRef.value) cameraRef.value.startCamera()
  }, 800)
})

const stopLoadingWatch = watch(messages, (msgs) => {
  const ai = msgs.find(m => m.type === 'ai')
  if (ai?.content && isFirstLoading.value) isFirstLoading.value = false
}, { deep: true })

onUnmounted(() => { if (ws.value) ws.value.disconnect(); if (speechRecognition.value) speechRecognition.value.stop() })

function initWebSocket() {
  if (!userStore.userId) return
  ws.value = createInterviewSocket(taskId, userStore.userId)
  ws.value.on('CONNECTED', () => { console.log('[WS] 连接成功') }).connect()
}

function initSpeechRecognition() {
  if (!speechSupported.value) return
  speechRecognition.value = createSpeechRecognition()
  speechRecognition.value.start({
    onResult: (r) => { inputMessage.value = r.transcript },
    onStart: () => { listeningMode.value = 'browser' },
    onEnd: () => { listeningMode.value = 'none'; if (isVoiceMode.value && inputMessage.value.trim()) sendMessage() },
    onError: () => { listeningMode.value = 'none' }
  })
}

function toggleVoice() {
  if (listeningMode.value === 'browser') {
    if (speechRecognition.value) speechRecognition.value.stop()
    listeningMode.value = 'none'
  } else if (speechSupported.value) {
    const s = speechRecognition.value.start({
      onResult: (r) => { inputMessage.value = r.transcript },
      onStart: () => { listeningMode.value = 'browser' },
      onEnd: () => { listeningMode.value = 'none'; if (isVoiceMode.value && inputMessage.value.trim()) sendMessage() },
      onError: () => { listeningMode.value = 'none' }
    })
    if (!s) ElMessage.error('启动语音识别失败')
  } else ElMessage.warning('当前浏览器不支持语音输入')
}

async function loadInterviewInfo() {
  try { const res = await api.get(`/interview/${taskId}`); interviewInfo.value = res.data } catch {}
}

async function loadFirstQuestion() {
  isFirstLoading.value = true
  try {
    const mi = messages.value.length; addMessage('ai', '')
    await fetchStream('/api/ai/generate-question-stream', { jobName: interviewInfo.value.jobName || 'Java开发', difficulty: interviewInfo.value.difficulty || '中级' }, {
      onDelta: (c) => { messages.value[mi].content += c; scrollToBottom() },
      onDone: (f) => { messages.value[mi].content = f; currentQuestion.value = f; isFirstLoading.value = false },
      onError: () => { messages.value[mi].content = '(加载失败)'; isFirstLoading.value = false }
    })
  } catch { isFirstLoading.value = false }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return
  const userMsg = inputMessage.value.trim()
  addMessage('user', userMsg); inputMessage.value = ''; sending.value = true
  qaList.value.push({ question: currentQuestion.value, answer: userMsg })
  try {
    await api.post('/interview/answer', { taskId: Number(taskId), roundNum: currentRound.value, question: currentQuestion.value, answerText: userMsg })
    if (currentRound.value >= totalRounds.value) { addMessage('ai', '面试已完成，正在生成报告...'); await generateReport(); return }
    const mi = messages.value.length; addMessage('ai', '')
    await fetchStream('/api/ai/generate-follow-up-stream', { history: qaList.value.map(q => ({ question: q.question, answer: q.answer })), currentQuestion: currentQuestion.value, currentAnswer: userMsg }, {
      onDelta: (c) => { messages.value[mi].content += c; scrollToBottom() },
      onDone: (f) => { messages.value[mi].content = f; currentQuestion.value = f; currentRound.value++; progress.value = Math.min((currentRound.value / totalRounds.value) * 100, 100) },
      onError: (e) => { messages.value[mi].content = '(回答失败)'; ElMessage.error(e) }
    })
  } catch { ElMessage.error('发送失败') } finally { sending.value = false }
}

const generateReport = async () => {
  if (isGenerating.value) return; isGenerating.value = true
  try {
    await api.post('/interview/generate-report', { taskId: Number(taskId), qaList: qaList.value })
    await api.post(`/interview/end/${taskId}`)
    statusClass.value = 'finished'; statusText.value = '已结束'
    addMessage('ai', '报告生成完成！正在跳转...')
    setTimeout(() => router.push(`/report/${taskId}`), 1000)
  } catch { addMessage('ai', '报告生成失败') } finally { isGenerating.value = false }
}

const endInterview = async () => {
  if (qaList.value.length > 0) await generateReport()
  else { await api.post(`/interview/end/${taskId}`); router.push(`/report/${taskId}`) }
}

const onEmotionResult = async (d) => {
  try { await api.post('/evaluation/save', { taskId: Number(taskId), analysisType: 'EMOTION', resultJson: JSON.stringify(d) }) } catch {}
}
const onCameraError = () => {}
</script>

<style scoped>
.interview { display:flex; flex-direction:column; max-width:1100px; margin:0 auto; }

.interview-top { display:flex; align-items:center; justify-content:space-between; }
.interview-body { display:flex; gap:var(--spacing-4); flex:1; min-height:0; }
.chat-col { flex:1; display:flex; flex-direction:column; }
.chat-scroll { flex:1; overflow-y:auto; padding:var(--spacing-5); max-height:65vh; }

.loading-msg { text-align:center; padding:var(--spacing-12) 0; }
.spinner { width:24px; height:24px; border:2px solid var(--color-border); border-top-color:var(--color-accent); border-radius:50%; margin:0 auto var(--spacing-3); animation:spin 0.8s linear infinite; }
@keyframes spin { to { transform:rotate(360deg) } }

.msg { display:flex; gap:10px; margin-bottom:var(--spacing-4); }
.msg--user { flex-direction:row-reverse; }
.msg-avatar { width:32px; height:32px; border-radius:50%; flex-shrink:0; display:flex; align-items:center; justify-content:center; font-size:11px; font-weight:600; }
.msg-avatar--ai { background:var(--color-accent-light); color:var(--color-accent); }
.msg-avatar--user { background:var(--color-surface-subtle); color:var(--color-text-secondary); }
.msg-bubble { padding:10px 14px; border-radius:var(--radius-md); font-size:14px; line-height:1.7; max-width:480px; }
.msg-bubble--ai { background:var(--color-surface-subtle); color:var(--color-text-body); border-bottom-left-radius:4px; }
.msg-bubble--user { background:var(--color-accent); color:white; border-bottom-right-radius:4px; }
.msg-time { font-size:10px; color:var(--color-text-placeholder); margin-top:4px; }
.msg--user .msg-time { text-align:right; }

.side-col { width:240px; display:flex; flex-direction:column; gap:var(--spacing-4); flex-shrink:0; }

.cam-float { position:fixed; bottom:24px; right:24px; z-index:50; }
</style>
