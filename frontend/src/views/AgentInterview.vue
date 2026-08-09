<template>
  <div class="agent-page">
    <!-- ===== 顶部栏 ===== -->
    <header class="agent-top">
      <button class="back-btn" @click="handleBack" aria-label="返回">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
      </button>
      <div class="agent-brand">
        <div class="agent-logo seal-mini">问</div>
        <div>
          <h3>审问 · Agent 面试官</h3>
          <p class="agent-sub">从大厂真题出题 · 逐题追问 · 生成评分</p>
        </div>
      </div>
      <div class="agent-actions">
        <span class="mode-chip" v-if="interviewStarted">{{ modeText(mode) }}</span>
        <span class="status-pill" :class="ended ? 'status--done' : 'status--live'">
          <span class="dot"></span>{{ ended ? '已结束' : '进行中' }}
        </span>
        <span class="duration" v-if="!ended">{{ durationText }}</span>
        <button class="btn-end" @click="endInterview" :disabled="ended">{{ ended ? '已结束' : '结束' }}</button>
      </div>
    </header>

    <div class="agent-body">
      <!-- ===== 聊天区 ===== -->
      <main class="chat-col">
        <!-- 欢迎态 -->
        <section v-if="messages.length === 0" class="welcome">
          <div class="welcome-card">
            <!-- 印章签名元素 -->
            <div class="seal-orbit">
              <span class="seal-ring"></span>
              <span class="seal-ring seal-ring--2"></span>
              <div class="seal" title="审问之 — 出自《礼记·中庸》">问</div>
            </div>

            <h2 class="welcome-title">以问为始，<em>见贤思齐</em></h2>

            <p class="welcome-quote">「博学之，审问之，慎思之，明辨之，笃行之。」</p>
            <p class="welcome-attr">——《礼记 · 中庸》</p>
            <p class="welcome-desc">我会从大厂真题库为你出题，逐题追问；面试结束，生成一份评分报告。</p>

            <!-- 模式选择 -->
            <div class="select-group">
              <span class="select-label">面试模式</span>
              <div class="mode-options">
                <button
                  v-for="m in modes"
                  :key="m.key"
                  class="mode-opt"
                  :class="{ 'mode-opt--active': mode === m.key }"
                  @click="selectMode(m.key)"
                >
                  <svg class="mode-opt-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" v-html="m.icon"></svg>
                  {{ m.label }}
                </button>
              </div>
            </div>

            <!-- 方向选择 -->
            <div class="select-group">
              <span class="select-label">出题方向</span>
              <div class="direction-chips">
                <button v-for="(s, i) in starters" :key="i" class="direction-chip" @click="quickStart(s.label)">
                  <span class="direction-index">{{ String(i + 1).padStart(2, '0') }}</span>
                  <span>{{ s.label }}</span>
                </button>
              </div>
            </div>

            <!-- 公司真题入口 -->
            <div class="company-line">
              <span class="company-label">真题来源</span>
              <div class="company-tags">
                <button v-for="c in companies" :key="c.name" class="company-tag" :style="tagStyle(c)" @click="startCompanyInterview(c.name)">
                  <span class="company-glyph">{{ c.glyph }}</span>{{ c.name }}
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- 消息列表 -->
        <section v-else ref="chatScroll" class="chat-scroll">
          <div v-for="(msg, index) in messages" :key="index" class="msg" :class="'msg--' + msg.type">
            <div class="msg-avatar" :class="'msg-avatar--' + msg.type">
              <span v-if="msg.type === 'ai'" class="avatar-seal">问</span>
              <span v-else>我</span>
            </div>
            <div class="msg-content">
              <div v-if="msg.type === 'ai' && detectSource(msg.content)" class="source-badge">
                <span class="source-dot"></span>{{ detectSource(msg.content) }} 真题
              </div>
              <div class="msg-bubble" :class="'msg-bubble--' + msg.type">
                <div v-if="msg.loading" class="typing">
                  <span></span><span></span><span></span>
                  <span class="typing-label">面试官沉吟中</span>
                </div>
                <div v-else class="md-body" v-html="renderMessage(msg.content)"></div>
              </div>
              <div v-if="msg.tools && msg.tools.length" class="tool-row">
                <span v-for="(t, ti) in msg.tools" :key="ti" class="tool-tag">☗ {{ t }}</span>
              </div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>
        </section>

        <!-- 输入区 -->
        <footer class="input-bar">
          <div class="input-wrap">
            <button
              v-if="speechSupported && interviewStarted"
              class="mic-btn"
              :class="{ 'mic-btn--on': listening }"
              @click="toggleVoice"
              :title="listening ? '停止录音' : '语音输入'"
            >
              <svg v-if="!listening" viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v4"/></svg>
              <span v-else class="mic-wave"><i></i><i></i><i></i></span>
            </button>

            <textarea
              v-model="inputMessage"
              class="input-area"
              :placeholder="inputPlaceholder"
              @keydown.ctrl.enter="sendMessage"
              @keydown.enter.exact.prevent="sendMessage"
              :disabled="ended"
              rows="1"
            ></textarea>

            <button
              v-if="interviewStarted"
              class="cam-toggle-btn"
              :class="{ 'cam-toggle-btn--on': cameraVisible }"
              @click="toggleCamera"
              :title="cameraVisible ? '关闭摄像头' : '开启摄像头'"
            >
              <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M23 7l-7 5 7 5V7z"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
            </button>

            <button class="send-btn" :class="{ 'send-btn--active': canSend }" :disabled="!canSend" @click="sendMessage" aria-label="发送">
              <svg v-if="!sending" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/></svg>
              <span v-else class="send-spinner"></span>
            </button>
          </div>
          <p class="input-hint">{{ inputHint }}</p>
        </footer>
      </main>

      <!-- ===== 侧边栏 ===== -->
      <aside class="side-col">
        <div class="card side-card">
          <div class="side-head">
            <p class="eyebrow">面试聚焦</p>
            <span class="side-head-hint">点击切换</span>
          </div>
          <div class="focus-tags">
            <button v-for="t in focusTopics" :key="t" class="focus-tag" :class="{ 'focus-tag--active': activeFocus === t }" @click="focusOn(t)">{{ t }}</button>
          </div>
          <button class="focus-redirect" @click="focusOn('')">恢复自由出题</button>
        </div>

        <div class="card side-card">
          <p class="eyebrow">Session</p>
          <div class="stat-list">
            <div class="stat-item"><span class="stat-label">消息</span><span class="stat-value">{{ realMessageCount }}</span></div>
            <div class="stat-item"><span class="stat-label">已答轮次</span><span class="stat-value">{{ roundNum }}</span></div>
            <div class="stat-item"><span class="stat-label">工具调用</span><span class="stat-value">{{ toolCallCount }}</span></div>
            <div class="stat-item"><span class="stat-label">时长</span><span class="stat-value">{{ durationText }}</span></div>
            <div class="stat-item"><span class="stat-label">会话</span><span class="stat-value mono">{{ shortSessionId }}</span></div>
          </div>
        </div>

        <div class="card side-card">
          <p class="eyebrow">能力</p>
          <div class="caps">
            <span class="cap"><span class="cap-dot cap--kb"></span>真题库检索</span>
            <span class="cap"><span class="cap-dot cap--bank"></span>题库查询</span>
            <span class="cap"><span class="cap-dot cap--ai"></span>智能出题</span>
            <span class="cap"><span class="cap-dot cap--chat"></span>追问点评</span>
            <span class="cap"><span class="cap-dot cap--score"></span>评分报告</span>
          </div>
        </div>

        <!-- 人文箴言 -->
        <div class="card side-card side-card--proverb">
          <p class="eyebrow">箴言</p>
          <p class="proverb-text">问答之间，见真章。<br>答得出，是积累；<br>答不出，是方向。</p>
          <p class="proverb-attr">——审问录</p>
        </div>

        <button class="end-btn" :disabled="ended" @click="endInterview">
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
          {{ ended ? '已结束' : '结束并生成报告' }}
        </button>
      </aside>
    </div>

    <!-- 摄像头浮窗（视频模式） -->
    <div v-if="cameraVisible" class="cam-float">
      <CameraFeed ref="cameraRef" :mirrored="true" @emotion="onEmotionResult" @error="onCameraError" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import api, { agentChat, agentEnd } from '@/api'
import { renderMarkdown } from '@/utils/markdown'
import { modeText } from '@/utils/constants'
import CameraFeed from '@/components/CameraFeed.vue'
import { createSpeechRecognition, isSpeechRecognitionSupported } from '@/utils/speechRecognition'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ===== 基础状态 =====
const chatScroll = ref(null)
const inputMessage = ref('')
const sending = ref(false)
const messages = ref([])
const ended = ref(false)
const activeFocus = ref('')
const selectedJob = ref('Java 后端开发')

// ===== 面试模式 & 评分 =====
const modes = [
  { key: 'TEXT', label: '文字面试', icon: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M3 9h18M7 13h6M7 16h4"/>' },
  { key: 'VOICE', label: '语音面试', icon: '<path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v4"/>' },
  { key: 'VIDEO', label: '视频面试', icon: '<path d="M23 7l-7 5 7 5V7z"/><rect x="1" y="5" width="15" height="14" rx="2"/>' }
]
const mode = ref('TEXT')
const taskId = ref(null)
const interviewStarted = ref(false)
const currentQuestion = ref('')
const roundNum = ref(0)
const qaList = ref([])

// ===== 会话 =====
const sessionId = ref(`agent-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`)
const shortSessionId = computed(() => sessionId.value.slice(-8).toUpperCase())
const canSend = computed(() => inputMessage.value.trim() && !sending.value && !ended.value)
const realMessageCount = computed(() => messages.value.filter(m => !m.loading).length)
const toolCallCount = computed(() => messages.value.filter(m => m.type === 'ai' && m.tools?.length).length)

// ===== 语音 & 视频 =====
const speechSupported = ref(isSpeechRecognitionSupported())
const listening = ref(false)
let speechRecognition = null
const cameraRef = ref(null)
const cameraVisible = ref(false)

const inputPlaceholder = computed(() => {
  if (ended.value) return '面试已结束'
  if (listening.value) return '正在聆听…'
  return mode.value === 'VOICE' ? '开口作答，或在此输入…' : '在此输入你的回答…'
})
const inputHint = computed(() => {
  if (mode.value === 'VOICE') return '语音模式：说完自动发送 · 结束面试将生成评分报告'
  if (mode.value === 'VIDEO') return '摄像头记录表情 · 面试结束生成综合报告'
  return 'Enter 发送 · 面试结束自动生成评分报告'
})

// ===== 面试聚焦模块 =====
const focusTopics = ['JVM', '并发', '集合', 'MySQL', 'Redis', '分布式', '系统设计', '网络']
const starters = [
  { label: 'Java 后端' },
  { label: '前端开发' },
  { label: '分布式/微服务' },
  { label: '系统设计' }
]
const companies = [
  { name: '阿里', glyph: '阿', color: '#C1440E' },
  { name: '腾讯', glyph: '腾', color: '#1769AA' },
  { name: '字节', glyph: '字', color: '#2E5AAC' },
  { name: '美团', glyph: '美', color: '#B8860B' },
  { name: '华为', glyph: '华', color: '#B32424' }
]
const tagStyle = (c) => ({ '--tag-color': c.color, '--tag-glyph': `'${c.glyph}'` })

// ===== 时长计时 =====
const startTime = Date.now()
const durationText = ref('00:00')
let timer = null

// ===== 来源识别 =====
const SOURCE_NAMES = ['阿里', '腾讯', '字节', '美团', '华为', '讯飞', '京东', '百度']
function detectSource(content) {
  if (!content) return null
  for (const name of SOURCE_NAMES) {
    if (content.includes(name)) return name
  }
  return null
}

const getTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatScroll.value) chatScroll.value.scrollTop = chatScroll.value.scrollHeight
}

const renderMessage = (text) => renderMarkdown(text)

const addMessage = (type, content, tools = []) => {
  messages.value.push({ type, content, tools, time: getTime(), loading: false })
  scrollToBottom()
}
const addLoading = () => {
  messages.value.push({ type: 'ai', content: '', tools: [], time: getTime(), loading: true })
  scrollToBottom()
}
const removeLoading = () => {
  const idx = messages.value.findIndex(m => m.loading)
  if (idx !== -1) messages.value.splice(idx, 1)
}

// ===== 模式选择 =====
const selectMode = (m) => {
  mode.value = m
  if (m === 'VIDEO') {
    cameraVisible.value = true
    setTimeout(() => cameraRef.value?.startCamera(), 300)
  } else {
    cameraVisible.value = false
    setTimeout(() => cameraRef.value?.stopCamera(), 0)
  }
}

const toggleCamera = () => {
  cameraVisible.value = !cameraVisible.value
  if (cameraVisible.value) {
    setTimeout(() => cameraRef.value?.startCamera(), 300)
    if (mode.value !== 'VIDEO') mode.value = 'VIDEO'
  }
}

const onCameraError = () => { cameraVisible.value = false }

// ===== 创建任务 & 提交答案（评分支撑） =====
const startInterview = async () => {
  const res = await api.post('/interview/create', {
    userId: userStore.userId,
    jobName: selectedJob.value,
    mode: mode.value,
    difficulty: '中级'
  })
  taskId.value = res.data.id
  interviewStarted.value = true
  if (mode.value === 'VIDEO') {
    cameraVisible.value = true
    setTimeout(() => cameraRef.value?.startCamera(), 300)
  }
  return taskId.value
}

const submitAnswer = async (answerText) => {
  if (!taskId.value) return
  roundNum.value++
  qaList.value.push({ question: currentQuestion.value, answer: answerText })
  try {
    await api.post('/interview/answer', {
      taskId: taskId.value,
      roundNum: roundNum.value,
      question: currentQuestion.value,
      answerText
    })
  } catch (e) { console.warn('保存回答失败', e) }
}

// ===== 语音 =====
const toggleVoice = () => {
  if (listening.value) { speechRecognition?.stop(); listening.value = false }
  else startVoice()
}

const startVoice = () => {
  if (!speechSupported.value) { ElMessage.warning('当前浏览器不支持语音输入'); return }
  if (!speechRecognition) speechRecognition = createSpeechRecognition()
  const ok = speechRecognition.start({
    onResult: (r) => { inputMessage.value = r.transcript },
    onStart: () => { listening.value = true },
    onEnd: () => {
      listening.value = false
      if (mode.value === 'VOICE' && inputMessage.value.trim()) sendMessage()
    },
    onError: () => { listening.value = false }
  })
  if (!ok) ElMessage.error('启动语音识别失败')
}

// ===== 发送消息 =====
const quickStart = (topic) => {
  selectedJob.value = topic.includes('Java') || topic.includes('分布式') ? 'Java 后端开发' : topic
  inputMessage.value = `我想面试${topic}方向，请根据大厂真题库给我出一道高频面试题`
  sendMessage()
}

const startCompanyInterview = (company) => {
  selectedJob.value = 'Java 后端开发'
  inputMessage.value = `请给我出一道${company}公司的真题，从知识库中检索相关的高频面试题`
  sendMessage()
}

const focusOn = (topic) => {
  activeFocus.value = topic
  if (!topic) { ElMessage.success('已恢复自由出题'); return }
  inputMessage.value = `接下来请重点围绕【${topic}】方向出题和追问，贴合真实面试节奏`
  sendMessage()
}

const sendMessage = async () => {
  if (!canSend.value) return

  const userMsg = inputMessage.value.trim()
  addMessage('user', userMsg)
  inputMessage.value = ''
  sending.value = true
  addLoading()

  try {
    if (!interviewStarted.value) {
      await startInterview()
    } else {
      await submitAnswer(userMsg)
    }

    const res = await agentChat(sessionId.value, userMsg)
    removeLoading()
    const aiMsg = res.data
    currentQuestion.value = aiMsg
    addMessage('ai', aiMsg, ['知识库检索'])

    if (mode.value === 'VOICE' && !ended.value) {
      setTimeout(() => startVoice(), 400)
    }
  } catch (e) {
    removeLoading()
    addMessage('ai', '抱歉，面试官暂时无法回应。请确认后端服务已启动后重试。')
    console.error('[Agent Chat]', e)
  } finally {
    sending.value = false
  }
}

// ===== 情绪保存（视频模式） =====
const onEmotionResult = async (d) => {
  if (!taskId.value) return
  try {
    await api.post('/evaluation/save', { taskId: taskId.value, analysisType: 'EMOTION', resultJson: JSON.stringify(d) })
  } catch (e) { console.warn('保存情绪失败', e) }
}

// ===== 结束面试 → 生成评分报告 =====
const endInterview = async () => {
  if (ended.value) return

  if (listening.value) speechRecognition?.stop()
  listening.value = false
  if (cameraVisible.value) cameraRef.value?.stopCamera()

  try { await agentEnd(sessionId.value) } catch {}

  if (taskId.value && qaList.value.length > 0) {
    ended.value = true
    ElMessage.success('面试已结束，正在生成评分报告…')
    try {
      await api.post('/interview/generate-report', { taskId: taskId.value, qaList: qaList.value })
      await api.post(`/interview/end/${taskId.value}`)
      setTimeout(() => router.push(`/report/${taskId.value}`), 1500)
    } catch (e) {
      console.error('生成报告失败', e)
      ElMessage.error('报告生成失败，请稍后重试')
      setTimeout(() => router.push(`/report/${taskId.value}`), 800)
    }
  } else {
    ended.value = true
    ElMessage.success('面试已结束')
    setTimeout(() => router.push('/lobby'), 600)
  }
}

const handleBack = () => {
  if (messages.value.length > 0 && !ended.value) {
    if (!window.confirm('确定离开？当前会话将结束并生成报告。')) return
    endInterview()
  }
  router.push('/lobby')
}

watch(realMessageCount, () => scrollToBottom())

onMounted(() => {
  scrollToBottom()
  timer = setInterval(() => {
    const s = Math.floor((Date.now() - startTime) / 1000)
    durationText.value = `${Math.floor(s / 60).toString().padStart(2, '0')}:${(s % 60).toString().padStart(2, '0')}`
  }, 1000)
  // 从大厅带过来的岗位，自动开始一场该方向的面试
  const job = route.query.job
  if (job && typeof job === 'string' && job.trim()) {
    selectedJob.value = job
    setTimeout(() => {
      inputMessage.value = `我想面试${job}方向，请根据大厂真题库给我出一道高频面试题`
      sendMessage()
    }, 600)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (speechRecognition?.isListening) speechRecognition.stop()
  cameraRef.value?.stopCamera()
})
</script>

<style scoped>
.agent-page { max-width: 1160px; margin: 0 auto; display: flex; flex-direction: column; height: calc(100vh - var(--header-height) - var(--spacing-8)); }

/* ===== 顶部栏 ===== */
.agent-top {
  display: flex; align-items: center; gap: var(--spacing-4);
  padding: var(--spacing-3) var(--spacing-4);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-4);
}
.back-btn {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; border: none; border-radius: 50%;
  background: var(--color-surface-subtle); color: var(--color-text-secondary);
  cursor: pointer; transition: all var(--transition-fast);
}
.back-btn:hover { background: var(--color-accent-light); color: var(--color-accent); transform: translateX(-2px); }
.agent-brand { display: flex; align-items: center; gap: 12px; }
.agent-logo {
  width: 40px; height: 40px; border-radius: 10px;
  background: #B03A2E; color: #F4F1EA;
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-display); font-size: 20px; font-weight: 700;
  box-shadow: inset 0 0 0 2px rgba(244,241,234,.35), var(--shadow-md);
}
.agent-brand h3 { margin: 0; font-family: var(--font-display); font-size: 18px; font-weight: 700; letter-spacing: -0.02em; color: var(--color-text-primary); }
.agent-sub { margin: 1px 0 0; font-size: 11px; color: var(--color-text-secondary); }
.agent-actions { margin-left: auto; display: flex; align-items: center; gap: var(--spacing-3); }
.mode-chip { font-size: 12px; color: var(--color-accent); background: var(--color-accent-light); padding: 4px 10px; border-radius: 999px; font-weight: 500; }
.status-pill {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 5px 12px; border-radius: 999px; font-size: 12px; font-weight: 500;
}
.status-pill .dot { width: 7px; height: 7px; border-radius: 50%; }
.status--live { background: var(--color-success-light); color: var(--color-success); }
.status--live .dot { background: var(--color-success); animation: pulse 1.6s ease-in-out infinite; }
.status--done { background: var(--color-surface-subtle); color: var(--color-text-secondary); }
.status--done .dot { background: var(--color-text-placeholder); }
@keyframes pulse { 0%,100% { opacity:1 } 50% { opacity:.4 } }
.duration { font-family: ui-monospace, "SF Mono", Consolas, monospace; font-size: 13px; color: var(--color-text-secondary); }
.btn-end {
  padding: 7px 16px; border: 1px solid var(--color-border); border-radius: 999px;
  background: var(--color-surface); color: var(--color-danger); font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all var(--transition-fast);
}
.btn-end:hover:not(:disabled) { background: var(--color-danger-light); border-color: transparent; }
.btn-end:disabled { opacity: .5; cursor: not-allowed; }

/* ===== 主体布局 ===== */
.agent-body { display: flex; gap: var(--spacing-4); flex: 1; min-height: 0; }
.chat-col { flex: 1; display: flex; flex-direction: column; min-width: 0; background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }

/* ===== 欢迎态 ===== */
.welcome { flex: 1; display: flex; align-items: center; justify-content: center; padding: var(--spacing-6); overflow-y: auto; }
.welcome-card { max-width: 560px; text-align: center; }

/* 印章签名元素 */
.seal-orbit { position: relative; width: 108px; height: 108px; margin: 0 auto var(--spacing-4); display: flex; align-items: center; justify-content: center; }
.seal-ring { position: absolute; inset: 0; border-radius: 50%; border: 1px solid rgba(176,58,46,.18); animation: breathe 3.4s ease-in-out infinite; }
.seal-ring--2 { inset: 14px; border-color: rgba(176,58,46,.12); animation-delay: 1.7s; }
@keyframes breathe { 0%,100% { transform: scale(1); opacity: .8 } 50% { transform: scale(1.06); opacity: .4 } }
.seal {
  width: 62px; height: 62px; border-radius: 9px;
  background: #B03A2E; color: #F6F1E8;
  display: flex; align-items: center; justify-content: center;
  font-family: var(--font-display); font-size: 30px; font-weight: 700; line-height: 1;
  box-shadow: inset 0 0 0 2px rgba(246,241,232,.4), inset 0 0 0 6px rgba(246,241,232,.08), 0 6px 18px rgba(176,58,46,.28);
  transform: rotate(-3deg); cursor: default;
  transition: transform var(--transition-base);
}
.seal-orbit:hover .seal { transform: rotate(0deg) scale(1.04); }

.welcome-title { font-family: var(--font-display); font-size: 30px; font-weight: 700; letter-spacing: 0.02em; color: var(--color-text-primary); margin: 0 0 var(--spacing-2); }
.welcome-title em { font-style: italic; color: var(--color-accent); }
.welcome-quote { font-family: var(--font-display); font-size: 16px; letter-spacing: 0.08em; color: var(--color-text-body); margin: 0 0 2px; line-height: 1.8; }
.welcome-attr { font-size: 11px; color: var(--color-text-placeholder); letter-spacing: 0.1em; margin: 0 0 var(--spacing-3); }
.welcome-desc { font-size: 13px; line-height: 1.7; color: var(--color-text-body); margin: 0 auto var(--spacing-4); max-width: 420px; }

/* 选择分组 */
.select-group { margin-bottom: var(--spacing-4); }
.select-label { display: block; font-size: 11px; letter-spacing: .1em; color: var(--color-text-placeholder); margin-bottom: 8px; }
.mode-options { display: flex; gap: 8px; justify-content: center; }
.mode-opt {
  flex: 1; max-width: 140px; display: flex; align-items: center; justify-content: center; gap: 7px;
  padding: 10px 0; font-size: 13px; font-weight: 500;
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-surface); color: var(--color-text-body); cursor: pointer;
  transition: all var(--transition-fast);
}
.mode-opt:hover { border-color: var(--color-accent); transform: translateY(-1px); }
.mode-opt--active { background: var(--color-accent); border-color: var(--color-accent); color: white; box-shadow: var(--shadow-md); }
.mode-opt-icon { width: 16px; height: 16px; opacity: .85; }

.direction-chips { display: grid; grid-template-columns: 1fr 1fr; gap: 9px; }
.direction-chip {
  display: flex; align-items: center; gap: 10px; padding: 11px 14px;
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-surface); font-size: 13px; font-weight: 500; color: var(--color-text-body);
  cursor: pointer; transition: all var(--transition-fast); text-align: left;
}
.direction-chip:hover { border-color: var(--color-accent); background: var(--color-accent-light); color: var(--color-accent); transform: translateY(-1px); box-shadow: var(--shadow-md); }
.direction-index { font-family: var(--font-display); font-size: 11px; color: var(--color-text-placeholder); letter-spacing: .06em; transition: color var(--transition-fast); }
.direction-chip:hover .direction-index { color: var(--color-accent); }

.company-line { margin-top: var(--spacing-4); padding-top: var(--spacing-4); border-top: 1px solid var(--color-divider); }
.company-label { display: block; font-size: 11px; letter-spacing: .1em; color: var(--color-text-placeholder); margin-bottom: 10px; }
.company-tags { display: flex; gap: 10px; justify-content: center; flex-wrap: wrap; }
.company-tag {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px 6px 8px; border-radius: 999px; font-size: 13px; font-weight: 500;
  border: 1px solid var(--color-border); background: var(--color-surface); color: var(--color-text-body);
  cursor: pointer; transition: all var(--transition-fast);
}
.company-glyph {
  width: 22px; height: 22px; border-radius: 50%; flex-shrink: 0;
  background: var(--tag-color); color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600;
}
.company-tag:hover { border-color: var(--tag-color); color: var(--tag-color); transform: translateY(-1px); box-shadow: var(--shadow-md); }

/* ===== 消息 ===== */
.chat-scroll { flex: 1; overflow-y: auto; padding: var(--spacing-6); display: flex; flex-direction: column; gap: var(--spacing-5); }
.msg { display: flex; gap: 10px; animation: msgIn .3s ease; }
@keyframes msgIn { from { opacity: 0; transform: translateY(8px) } to { opacity: 1; transform: translateY(0) } }
.msg--user { flex-direction: row-reverse; }
.msg-avatar {
  width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 600;
}
.msg-avatar--ai { background: #B03A2E; color: #F6F1E8; box-shadow: var(--shadow-sm); }
.avatar-seal { font-family: var(--font-display); font-size: 15px; font-weight: 700; }
.msg-avatar--user { background: var(--color-surface-subtle); color: var(--color-text-secondary); }
.msg-content { display: flex; flex-direction: column; max-width: min(620px, 78%); }
.msg--user .msg-content { align-items: flex-end; }
.msg-bubble {
  padding: 12px 16px; border-radius: var(--radius-md); font-size: 14px; line-height: 1.75;
}
.msg-bubble--ai {
  background: var(--color-surface-subtle); color: var(--color-text-body);
  border-bottom-left-radius: 4px; border: 1px solid var(--color-border);
}
.msg-bubble--user {
  background: linear-gradient(135deg, var(--color-accent), #CC6A4B); color: white;
  border-bottom-right-radius: 4px; box-shadow: var(--shadow-sm);
}

.source-badge {
  align-self: flex-start; display: inline-flex; align-items: center; gap: 6px;
  padding: 3px 10px; margin-bottom: 5px; font-size: 11px; font-weight: 500; letter-spacing: .02em;
  color: var(--color-accent); background: var(--color-accent-light);
  border: 1px solid color-mix(in srgb, var(--color-accent) 20%, transparent);
  border-radius: 999px; animation: msgIn .3s ease;
}
.source-dot { width: 5px; height: 5px; border-radius: 50%; background: var(--color-accent); }

.md-body :deep(p) { margin: 0 0 8px; }
.md-body :deep(p:last-child) { margin-bottom: 0; }
.md-body :deep(h1), .md-body :deep(h2), .md-body :deep(h3), .md-body :deep(h4) {
  font-family: var(--font-display); color: var(--color-text-primary); margin: 14px 0 8px; line-height: 1.4;
}
.md-body :deep(h1) { font-size: 20px; }
.md-body :deep(h2) { font-size: 17px; }
.md-body :deep(h3) { font-size: 15px; }
.md-body :deep(h4) { font-size: 14px; }
.md-body :deep(strong) { color: var(--color-text-primary); font-weight: 600; }
.md-body :deep(code) {
  background: rgba(217, 119, 87, 0.12); color: #A0482E; padding: 1px 6px; border-radius: 4px;
  font-size: 12px; font-family: ui-monospace, "SF Mono", Consolas, monospace;
}
.msg-bubble--user :deep(code) { background: rgba(255,255,255,.2); color: white; }
.md-body :deep(ul), .md-body :deep(ol) { margin: 6px 0 10px; padding-left: 20px; }
.md-body :deep(li) { margin-bottom: 4px; }
.md-body :deep(blockquote) {
  border-left: 3px solid var(--color-accent); padding: 4px 12px; margin: 8px 0;
  background: var(--color-accent-light); border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-text-body);
}
.md-body :deep(hr) { border: none; border-top: 1px dashed var(--color-border); margin: 12px 0; }
.md-body :deep(table) {
  width: 100%; border-collapse: collapse; margin: 10px 0; font-size: 13px;
  border: 1px solid var(--color-border); border-radius: var(--radius-sm); overflow: hidden;
}
.md-body :deep(th) { background: var(--color-surface-hover); font-weight: 600; text-align: left; }
.md-body :deep(th), .md-body :deep(td) { padding: 7px 10px; border-bottom: 1px solid var(--color-border); }
.md-body :deep(tr:last-child td) { border-bottom: none; }
.md-body :deep(a) { color: var(--color-accent); }

.typing { display: flex; align-items: center; gap: 5px; padding: 2px 0; }
.typing span { width: 7px; height: 7px; border-radius: 50%; background: var(--color-text-placeholder); animation: bounce 1.2s ease-in-out infinite; }
.typing span:nth-child(2) { animation-delay: .2s; }
.typing span:nth-child(3) { animation-delay: .4s; }
.typing-label { font-size: 12px; color: var(--color-text-placeholder); margin-left: 6px; }
@keyframes bounce { 0%,60%,100% { transform: translateY(0) } 30% { transform: translateY(-5px) } }

.tool-row { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 6px; }
.tool-tag {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 2px 9px; font-size: 11px; color: var(--color-accent);
  background: var(--color-accent-light); border-radius: 999px; letter-spacing: .02em;
}
.msg--user .tool-row { display: none; }
.msg-time { font-size: 10px; color: var(--color-text-placeholder); margin-top: 4px; }

/* ===== 输入区 ===== */
.input-bar { padding: var(--spacing-4); border-top: 1px solid var(--color-divider); background: var(--color-surface); }
.input-wrap {
  display: flex; align-items: flex-end; gap: 8px;
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  background: var(--color-surface); padding: 8px 8px 8px 12px;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}
.input-wrap:focus-within { border-color: var(--color-accent); box-shadow: 0 0 0 3px rgba(217, 119, 87, 0.12); }
.input-area {
  flex: 1; border: none; outline: none; resize: none;
  font-family: var(--font-body); font-size: 14px; line-height: 1.6;
  background: transparent; color: var(--color-text-primary);
  max-height: 120px; padding: 2px 0;
}
.input-area::placeholder { color: var(--color-text-placeholder); }
.input-area:disabled { opacity: .5; }

.mic-btn, .cam-toggle-btn {
  width: 36px; height: 36px; border: none; border-radius: var(--radius-sm);
  background: var(--color-surface-subtle); color: var(--color-text-secondary);
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  transition: all var(--transition-fast); flex-shrink: 0;
}
.mic-btn:hover, .cam-toggle-btn:hover { color: var(--color-accent); }
.mic-btn--on { background: var(--color-danger-light); color: var(--color-danger); }
.cam-toggle-btn--on { background: var(--color-accent); color: white; }
.mic-wave { display: flex; align-items: flex-end; gap: 2px; height: 16px; }
.mic-wave i { width: 3px; background: var(--color-danger); border-radius: 2px; animation: wave 0.8s ease-in-out infinite; }
.mic-wave i:nth-child(1) { height: 8px; }
.mic-wave i:nth-child(2) { height: 15px; animation-delay: .15s; }
.mic-wave i:nth-child(3) { height: 10px; animation-delay: .3s; }
@keyframes wave { 0%,100% { transform: scaleY(0.6) } 50% { transform: scaleY(1.1) } }

.send-btn {
  width: 38px; height: 38px; border: none; border-radius: var(--radius-sm);
  background: var(--color-surface-subtle); color: var(--color-text-placeholder);
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  transition: all var(--transition-fast); flex-shrink: 0;
}
.send-btn--active { background: var(--color-accent); color: white; box-shadow: var(--shadow-md); }
.send-btn--active:hover { background: var(--color-accent-hover); transform: translateY(-1px); }
.send-spinner { width: 16px; height: 16px; border: 2px solid rgba(255,255,255,.3); border-top-color: white; border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg) } }
.input-hint { margin: 8px 2px 0; font-size: 11px; color: var(--color-text-placeholder); }

/* ===== 侧边栏 ===== */
.side-col { width: 260px; display: flex; flex-direction: column; gap: var(--spacing-3); flex-shrink: 0; }
.side-card { padding: var(--spacing-4); }
.side-card .eyebrow { margin-bottom: var(--spacing-3); }
.side-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-3); }
.side-head .eyebrow { margin-bottom: 0; }
.side-head-hint { font-size: 10px; color: var(--color-text-placeholder); }

.focus-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
.focus-tag {
  padding: 5px 11px; font-size: 12px; border-radius: 999px;
  border: 1px solid var(--color-border); background: var(--color-surface);
  color: var(--color-text-body); cursor: pointer; transition: all var(--transition-fast);
}
.focus-tag:hover { border-color: var(--color-accent); color: var(--color-accent); }
.focus-tag--active { background: var(--color-accent); border-color: var(--color-accent); color: white; }
.focus-redirect {
  width: 100%; padding: 7px; font-size: 12px; border: none; border-radius: var(--radius-sm);
  background: var(--color-surface-subtle); color: var(--color-text-secondary); cursor: pointer;
  transition: all var(--transition-fast);
}
.focus-redirect:hover { background: var(--color-accent-light); color: var(--color-accent); }

.stat-list { display: flex; flex-direction: column; gap: 10px; }
.stat-item { display: flex; align-items: center; justify-content: space-between; font-size: 13px; }
.stat-label { color: var(--color-text-secondary); }
.stat-value { color: var(--color-text-primary); font-weight: 600; }
.stat-value.mono { font-family: ui-monospace, "SF Mono", Consolas, monospace; font-size: 11px; }

.caps { display: flex; flex-direction: column; gap: 9px; }
.cap { display: flex; align-items: center; gap: 8px; font-size: 13px; color: var(--color-text-body); }
.cap-dot { width: 8px; height: 8px; border-radius: 50%; }
.cap--kb { background: #D97757; }
.cap--bank { background: #6BA08A; }
.cap--ai { background: #E0A96D; }
.cap--chat { background: #7B8EC0; }
.cap--score { background: #9A7BB8; }

/* 人文箴言 */
.side-card--proverb { background: linear-gradient(160deg, #FBF7F0, #F5EDE2); border-color: #EADDC8; }
.proverb-text { font-family: var(--font-display); font-size: 14px; line-height: 2; letter-spacing: .06em; color: var(--color-text-body); margin: 0; }
.proverb-attr { font-size: 11px; color: var(--color-text-placeholder); letter-spacing: .1em; margin: 8px 0 0; text-align: right; }

.end-btn {
  display: flex; align-items: center; justify-content: center; gap: 7px;
  padding: 11px; border: 1px solid var(--color-danger-light); border-radius: var(--radius-md);
  background: var(--color-surface); color: var(--color-danger); font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all var(--transition-fast);
}
.end-btn:hover:not(:disabled) { background: var(--color-danger-light); }
.end-btn:disabled { opacity: .5; cursor: not-allowed; }

/* 摄像头浮窗 */
.cam-float { position: fixed; bottom: 24px; right: 24px; z-index: 50; }

@media (max-width: 900px) {
  .agent-body { flex-direction: column; }
  .side-col { width: 100%; flex-direction: row; flex-wrap: wrap; }
  .side-card { flex: 1; min-width: 200px; }
  .end-btn { width: 100%; }
  .agent-sub { display: none; }
  .duration { display: none; }
  .mode-chip { display: none; }
}
</style>
