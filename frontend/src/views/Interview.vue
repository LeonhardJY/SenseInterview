<template>
  <div class="interview-room">
    <div class="room-header">
      <div class="room-info">
        <h2>AI面试房间</h2>
        <el-tag :type="statusType">{{ statusText }}</el-tag>
      </div>
      <div class="room-actions">
        <el-button type="danger" @click="endInterview">结束面试</el-button>
      </div>
    </div>

    <div class="room-content">
      <div class="chat-area">
        <div class="chat-messages" ref="chatContainer">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message', msg.type]"
          >
            <div class="message-avatar">
              <el-avatar v-if="msg.type === 'ai'" :size="36">AI</el-avatar>
              <el-avatar v-else :size="36">我</el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的回答..."
            @keyup.enter.ctrl="sendMessage"
          />
          <el-button
            type="primary"
            @click="sendMessage"
            :loading="sending"
            :disabled="!inputMessage.trim()"
          >
            发送回答
          </el-button>
        </div>
      </div>

      <div class="side-panel">
        <div class="panel-card">
          <h4>面试进度</h4>
          <el-progress :percentage="progress" :stroke-width="10" />
          <p class="progress-text">第 {{ currentRound }} / {{ totalRounds }} 轮</p>
        </div>

        <div class="panel-card">
          <h4>当前问题</h4>
          <p class="current-question">{{ currentQuestion }}</p>
        </div>

        <div class="panel-card">
          <h4>面试信息</h4>
          <div class="info-item">
            <span>岗位：</span>
            <span>{{ interviewInfo.jobName }}</span>
          </div>
          <div class="info-item">
            <span>模式：</span>
            <span>{{ interviewInfo.mode }}</span>
          </div>
          <div class="info-item">
            <span>难度：</span>
            <span>{{ interviewInfo.difficulty }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const statusType = ref('warning')
const statusText = ref('进行中')

const progress = ref(0)

onMounted(() => {
  loadInterviewInfo()
  loadFirstQuestion()
})

const loadInterviewInfo = async () => {
  try {
    const res = await api.get(`/interview/${taskId}`)
    interviewInfo.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadFirstQuestion = async () => {
  try {
    const res = await api.post('/ai/generate-question', null, {
      params: {
        jobName: interviewInfo.value.jobName || 'Java开发',
        difficulty: interviewInfo.value.difficulty || 'MEDIUM'
      }
    })
    currentQuestion.value = res.data
    addMessage('ai', currentQuestion.value)
  } catch (error) {
    console.error(error)
  }
}

const addMessage = (type, content) => {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  messages.value.push({ type, content, time })
  scrollToBottom()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
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

    const res = await api.post('/ai/generate-follow-up', null, {
      params: {
        question: currentQuestion.value,
        answer: userMessage
      }
    })

    currentRound.value++
    progress.value = Math.min((currentRound.value / totalRounds.value) * 100, 100)
    currentQuestion.value = res.data
    addMessage('ai', currentQuestion.value)
  } catch (error) {
    console.error(error)
  } finally {
    sending.value = false
  }
}

const endInterview = async () => {
  try {
    await api.post(`/interview/end/${taskId}`)
    statusType.value = 'success'
    statusText.value = '已结束'
    router.push(`/report/${taskId}`)
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.interview-room {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.room-info h2 {
  font-size: 20px;
  color: #1A1A1A;
}

.room-content {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #E8E8E8;
  border-radius: 8px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: #F5F5F5;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
}

.message.user .message-text {
  background: #C74634;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #E8E8E8;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input .el-input {
  flex: 1;
}

.side-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-card {
  background: #fff;
  border: 1px solid #E8E8E8;
  border-radius: 8px;
  padding: 16px;
}

.panel-card h4 {
  font-size: 14px;
  color: #1A1A1A;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #E8E8E8;
}

.progress-text {
  font-size: 13px;
  color: #767676;
  margin-top: 8px;
  text-align: center;
}

.current-question {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
}

.info-item span:first-child {
  color: #767676;
}
</style>