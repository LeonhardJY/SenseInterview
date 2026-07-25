<template>
  <div class="interview-page">
    <!-- 顶部栏 -->
    <div class="interview-header">
      <div class="header-left">
        <button class="btn btn-ghost btn-back" @click="$router.push('/lobby')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回
        </button>
        <div class="header-info">
          <h1>AI面试房间</h1>
          <span class="status-badge" :class="statusClass">
            <span class="status-dot"></span>
            {{ statusText }}
          </span>
        </div>
      </div>
      <button class="btn btn-danger" @click="endInterview">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="9" x2="15" y2="15"/><line x1="15" y1="9" x2="9" y2="15"/></svg>
        结束面试
      </button>
    </div>

    <div class="interview-content">
      <!-- 聊天区域 -->
      <div class="chat-area">
        <div class="chat-messages" ref="chatContainer">
          <div v-for="(msg, index) in messages" :key="index" class="message" :class="msg.type">
            <div class="message-avatar" :class="msg.type">
              {{ msg.type === 'ai' ? 'AI' : '我' }}
            </div>
            <div class="message-body">
              <div class="message-bubble" :class="msg.type">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <textarea
            v-model="inputMessage"
            class="input-textarea"
            placeholder="请输入您的回答... (Ctrl+Enter 发送)"
            @keydown.ctrl.enter="sendMessage"
          ></textarea>
          <button class="btn btn-primary send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || sending">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            发送
          </button>
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="side-panel">
        <div class="panel-card">
          <h4 class="panel-title">面试进度</h4>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <p class="progress-text">第 {{ currentRound }} / {{ totalRounds }} 轮</p>
        </div>

        <div class="panel-card">
          <h4 class="panel-title">当前问题</h4>
          <p class="current-question">{{ currentQuestion }}</p>
        </div>

        <div class="panel-card">
          <h4 class="panel-title">面试信息</h4>
          <div class="info-list">
            <div class="info-row">
              <span class="info-label">岗位</span>
              <span class="info-value">{{ interviewInfo.jobName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">模式</span>
              <span class="info-value">{{ interviewInfo.mode === 'TEXT' ? '文字面试' : interviewInfo.mode }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">难度</span>
              <span class="info-value">{{ interviewInfo.difficulty }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const taskId = route.params.taskId

const chatContainer = ref(null)
const inputMessage = ref('')
const sending = ref(false)
const messages = ref([])
const currentRound = ref(1)
const totalRounds = ref(5)
const currentQuestion = ref('')
const interviewInfo = ref({})
const statusClass = ref('running')
const statusText = ref('进行中')
const progress = ref(20)

onMounted(() => {
  loadInterviewInfo()
  loadFirstQuestion()
})

const loadInterviewInfo = async () => {
  try {
    const res = await api.get(`/interview/${taskId}`)
    interviewInfo.value = res.data
  } catch (error) { console.error(error) }
}

const loadFirstQuestion = async () => {
  try {
    const res = await api.post('/ai/generate-question', null, {
      params: { jobName: interviewInfo.value.jobName || 'Java开发', difficulty: interviewInfo.value.difficulty || 'MEDIUM' }
    })
    currentQuestion.value = res.data
    addMessage('ai', currentQuestion.value)
  } catch (error) { console.error(error) }
}

const addMessage = (type, content) => {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  messages.value.push({ type, content, time })
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return

  const userMessage = inputMessage.value.trim()
  addMessage('user', userMessage)
  inputMessage.value = ''
  sending.value = true

  try {
    await api.post('/interview/answer', {
      taskId: parseInt(taskId),
      questionId: currentRound.value,
      answer: userMessage
    })

    if (currentRound.value >= totalRounds.value) {
      addMessage('ai', '面试已完成，正在生成评价报告...')
      await api.post(`/interview/end/${taskId}`)
      statusClass.value = 'finished'
      statusText.value = '已结束'
      setTimeout(() => { router.push(`/report/${taskId}`) }, 1500)
      return
    }

    const res = await api.post('/ai/generate-follow-up', null, {
      params: { question: currentQuestion.value, answer: userMessage }
    })

    currentRound.value++
    progress.value = Math.min((currentRound.value / totalRounds.value) * 100, 100)
    currentQuestion.value = res.data
    addMessage('ai', currentQuestion.value)
  } catch (error) { console.error(error) }
  finally { sending.value = false }
}

const endInterview = async () => {
  try {
    await api.post(`/interview/end/${taskId}`)
    statusClass.value = 'finished'
    statusText.value = '已结束'
    router.push(`/report/${taskId}`)
  } catch (error) { console.error(error) }
}
</script>

<style scoped>
.interview-page { height: calc(100vh - 80px); display: flex; flex-direction: column; }

.interview-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--space-4) 0; margin-bottom: var(--space-4);
}

.header-left { display: flex; align-items: center; gap: var(--space-4); }
.header-info { display: flex; align-items: center; gap: var(--space-3); }
.header-info h1 { font-size: 18px; font-weight: 600; color: var(--gray-900); margin: 0; }

.status-badge { display: inline-flex; align-items: center; gap: 6px; padding: 4px 12px; font-size: 12px; font-weight: 500; border-radius: 9999px; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.status-badge.running { color: var(--primary); background: var(--primary-bg); }
.status-badge.finished { color: var(--success); background: var(--success-bg); }

.interview-content { flex: 1; display: flex; gap: var(--space-4); overflow: hidden; }

/* 聊天区域 */
.chat-area { flex: 1; display: flex; flex-direction: column; background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius); overflow: hidden; }

.chat-messages { flex: 1; overflow-y: auto; padding: var(--space-5); }

.message { display: flex; gap: var(--space-3); margin-bottom: var(--space-4); }
.message.user { flex-direction: row-reverse; }

.message-avatar {
  width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-size: 12px; font-weight: 600; flex-shrink: 0;
}
.message-avatar.ai { background: var(--primary-bg); color: var(--primary); }
.message-avatar.user { background: var(--gray-100); color: var(--gray-600); }

.message-body { max-width: 70%; }
.message.user .message-body { text-align: right; }

.message-bubble {
  padding: 12px 16px; border-radius: 12px; font-size: 14px; line-height: 1.6;
}
.message-bubble.ai { background: var(--gray-50); color: var(--gray-800); border-bottom-left-radius: 4px; }
.message-bubble.user { background: var(--primary); color: white; border-bottom-right-radius: 4px; }

.message-time { font-size: 11px; color: var(--gray-400); margin-top: 4px; }

.chat-input { padding: var(--space-4); border-top: 1px solid var(--border-color); display: flex; gap: var(--space-3); align-items: flex-end; }

.input-textarea {
  flex: 1; height: 80px; padding: 12px; font-size: 14px; font-family: var(--font-sans);
  border: 1px solid var(--gray-200); border-radius: var(--border-radius-sm); resize: none;
  background: var(--gray-50); transition: var(--transition);
}

.input-textarea:focus { outline: none; border-color: var(--primary); background: white; }

.send-btn { height: 80px; padding: 0 20px; display: flex; align-items: center; gap: 6px; }

/* 右侧面板 */
.side-panel { width: 280px; display: flex; flex-direction: column; gap: var(--space-4); }

.panel-card { background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius); padding: var(--space-5); }

.panel-title {
  font-size: 13px; font-weight: 600; color: var(--gray-500); text-transform: uppercase;
  letter-spacing: 0.5px; margin-bottom: var(--space-3);
}

.progress-bar { height: 6px; background: var(--gray-100); border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: var(--primary); border-radius: 3px; transition: width 0.3s ease; }
.progress-text { font-size: 13px; color: var(--gray-500); margin-top: var(--space-2); text-align: center; }

.current-question { font-size: 14px; color: var(--gray-700); line-height: 1.6; }

.info-list { display: flex; flex-direction: column; gap: var(--space-3); }
.info-row { display: flex; justify-content: space-between; font-size: 13px; }
.info-label { color: var(--gray-500); }
.info-value { color: var(--gray-800); font-weight: 500; }
</style>