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
          <!-- 面试官出题中... -->
          <div v-if="isFirstLoading" class="loading-first">
            <div class="loading-spinner"></div>
            <p>面试官正在出题中<span class="loading-dots"><span>.</span><span>.</span><span>.</span></span></p>
          </div>

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
          <div class="input-toolbar">
            <button
              class="btn btn-icon voice-btn"
              :class="{ active: listeningMode === 'browser', disabled: !speechSupported }"
              @click="toggleVoice"
              :title="listeningMode === 'browser' ? '停止录音' : '开始语音输入'"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/>
                <path d="M19 10v2a7 7 0 01-14 0v-2"/>
                <line x1="12" y1="19" x2="12" y2="23"/>
                <line x1="8" y1="23" x2="16" y2="23"/>
              </svg>
              {{ listeningMode === 'browser' ? '停止' : '语音' }}
            </button>
            <span v-if="listeningMode === 'browser'" class="voice-status">
              <span class="voice-dot"></span>
              正在识别...
            </span>
          </div>
          <textarea
            v-model="inputMessage"
            class="input-textarea"
            :placeholder="listeningMode === 'browser' ? '正在识别语音...' : '请输入您的回答... (Ctrl+Enter 发送)'"
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
              <span class="info-value">
                {{ modeText(interviewInfo.mode) }}
                <span v-if="interviewInfo.mode === 'VOICE'" class="mode-hint">语音输入已就绪</span>
                <span v-if="interviewInfo.mode === 'VIDEO'" class="mode-hint">摄像头+语音</span>
              </span>
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

  <!-- 摄像头浮动窗（VIDEO 模式） -->
  <div v-if="interviewInfo.mode === 'VIDEO'" class="camera-float">
    <CameraFeed
      ref="cameraRef"
      :mirrored="true"
      @frame="onVideoFrame"
      @emotion="onEmotionResult"
      @error="onCameraError"
    />
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

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
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

// WebSocket
const ws = ref(null)
const wsConnected = ref(false)

// 首次加载
const isFirstLoading = ref(true)

// 语音识别相关
const speechSupported = ref(isSpeechRecognitionSupported())
const listeningMode = ref('none') // 'none' | 'browser'
const speechRecognition = ref(null)

// 语音输入使用浏览器原生 API

// 摄像头（VIDEO 模式）
const cameraRef = ref(null)
const cameraError = ref(null)

// 当前面试模式
const interviewMode = computed(() => interviewInfo.value.mode || 'TEXT')
const isVoiceMode = computed(() => interviewMode.value === 'VOICE' || interviewMode.value === 'VIDEO')
const isVideoMode = computed(() => interviewMode.value === 'VIDEO')

onMounted(async () => {
  // 1. 建立 WebSocket 连接
  initWebSocket()

  // 2. 加载面试信息
  await loadInterviewInfo()
  await loadFirstQuestion()
  initSpeechRecognition()

  // 3. 兜底：第一个 AI 消息有内容了 → 收起加载动画
  //（避免 onDone 回调链路问题导致 spinner 卡死）
  const stopLoadingWatch = watch(messages, (msgs) => {
    const aiMsg = msgs.find(m => m.type === 'ai')
    if (aiMsg?.content && isFirstLoading.value) {
      isFirstLoading.value = false
    }
  }, { deep: true })

  // 4. 根据面试模式自动启用对应功能
  setTimeout(() => {
    if (isVoiceMode.value && speechSupported.value) {
      startBrowserVoice()
    }
    if (isVideoMode.value && cameraRef.value) {
      cameraRef.value.startCamera()
    }
  }, 500)
})

onUnmounted(() => {
  // 断开 WebSocket
  if (ws.value) {
    ws.value.disconnect()
  }
  // 停止语音识别
  if (speechRecognition.value) {
    speechRecognition.value.stop()
  }
  // 清理录音器
  // 清理录音资源（浏览器语音识别已停止，无需额外清理）
})

// ========== 摄像头（VIDEO 模式） ==========

/**
 * 摄像头帧回调
 */
const onVideoFrame = (videoElement) => {
  // 留空，帧捕获由 CameraFeed 内部处理
}

/**
 * 情绪分析结果回调 — 保存到后端
 */
const onEmotionResult = async (emotionData) => {
  if (!emotionData?.emotion || !taskId) return
  try {
    await api.post('/evaluation/save', {
      taskId: Number(taskId),
      analysisType: 'EMOTION',
      resultJson: JSON.stringify(emotionData)
    })
  } catch (e) {
    // 情绪记录非关键路径，静默处理
    console.debug('[面试] 保存情绪记录失败:', e)
  }
}

/**
 * 摄像头错误回调
 */
const onCameraError = (err) => {
  cameraError.value = err
  console.error('[面试] 摄像头错误:', err)
}

// ========== WebSocket ==========

/**
 * 初始化 WebSocket 连接
 */
const initWebSocket = () => {
  if (!userStore.userId) {
    console.warn('[面试] userId 为空，跳过 WebSocket')
    return
  }

  ws.value = createInterviewSocket(taskId, userStore.userId)

  ws.value
    .on('CONNECTED', () => {
      wsConnected.value = true
      console.log('[面试] WebSocket 连接成功')
    })
    .on('EVALUATION_UPDATE', (data) => {
      // 收到实时评估更新
      console.log('[面试] 收到评估更新:', data)
    })
    .on('PROGRESS_UPDATE', (data) => {
      // 收到进度更新
      if (data?.currentRound) {
        currentRound.value = data.currentRound
      }
      if (data?.progress !== undefined) {
        progress.value = data.progress
      }
    })
    .on('QUESTION_PUSH', (data) => {
      // AI 主动推送题目（用于流式/异步场景）
      if (data?.question) {
        currentQuestion.value = data.question
        addMessage('ai', data.question)
      }
    })
    .on('close', (event) => {
      wsConnected.value = false
      if (event.code !== 1000) {
        console.warn('[面试] WebSocket 异常断开，代码:', event.code)
      }
    })

  ws.value.connect()
}

/**
 * 初始化语音识别（浏览器内置 Web Speech API）
 */
const initSpeechRecognition = () => {
  if (!speechSupported.value) return
  speechRecognition.value = createSpeechRecognition()
}


/**
 * 切换语音输入（浏览器原生 Web Speech API）
 */
const toggleVoice = () => {
  if (listeningMode.value === 'browser') {
    stopBrowserVoice()
  } else if (speechSupported.value) {
    startBrowserVoice()
  } else {
    ElMessage.warning('当前浏览器不支持语音输入，请使用 Chrome 或 Edge')
  }
}

/**
 * 启动浏览器原生语音识别
 */
const startBrowserVoice = () => {
  if (!speechRecognition.value) return

  const success = speechRecognition.value.start({
    onResult: (result) => {
      inputMessage.value = result.transcript
    },
    onStart: () => {
      listeningMode.value = 'browser'
    },
    onEnd: () => {
      listeningMode.value = 'none'
      // VOICE/VIDEO 模式：语音结束时自动发送
      if (isVoiceMode.value && inputMessage.value.trim()) {
        sendMessage()
      }
    },
    onError: (error) => {
      listeningMode.value = 'none'
      if (error === 'not-allowed') {
        ElMessage.error('请允许浏览器使用麦克风，或使用文字输入')
      } else {
        // 浏览器识别失败，给提示但不自动降级（Whisper 需配置 API Key）
        console.warn('[语音] 浏览器识别失败:', error)
        ElMessage.warning('浏览器语音识别异常，请点"语音"按钮重试，或使用文字输入')
      }
    }
  })

  if (!success) {
    ElMessage.error('启动语音识别失败')
  }
}

/**
 * 停止浏览器语音识别
 */
const stopBrowserVoice = () => {
  if (speechRecognition.value) {
    speechRecognition.value.stop()
  }
  listeningMode.value = 'none'
}

const loadInterviewInfo = async () => {
  try {
    const res = await api.get(`/interview/${taskId}`)
    interviewInfo.value = res.data
  } catch (error) {
    console.error('加载面试信息失败:', error)
    ElMessage.error('加载面试信息失败')
  }
}

const loadFirstQuestion = async () => {
  isFirstLoading.value = true
  try {
    const msgIdx = messages.value.length
    addMessage('ai', '') // 占位，流式填充

    await fetchStream('/api/ai/generate-question-stream', {
      jobName: interviewInfo.value.jobName || 'Java开发',
      difficulty: interviewInfo.value.difficulty || '中级'
    }, {
      onDelta: (chunk) => {
        messages.value[msgIdx].content += chunk
        scrollToBottom()
      },
      onDone: (fullText) => {
        messages.value[msgIdx].content = fullText
        currentQuestion.value = fullText
        isFirstLoading.value = false
      },
      onError: (err) => {
        messages.value[msgIdx].content = '(加载题目失败，请重试)'
        ElMessage.error('加载面试题目失败: ' + err)
        isFirstLoading.value = false
      }
    })
  } catch (error) {
    console.error('加载面试题目失败:', error)
    ElMessage.error('加载面试题目失败，请重试')
    isFirstLoading.value = false
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
  if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight
}

// 保存问答记录
const qaList = ref([])

// 报告生成中标记（防止重复生成）
const isGenerating = ref(false)

const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return

  const userMessage = inputMessage.value.trim()
  addMessage('user', userMessage)
  inputMessage.value = ''
  sending.value = true

  try {
    // 保存问答记录
    qaList.value.push({
      question: currentQuestion.value,
      answer: userMessage
    })

    await api.post('/interview/answer', {
      taskId: Number(taskId),
      roundNum: currentRound.value,
      question: currentQuestion.value,
      answerText: userMessage
    })

    if (currentRound.value >= totalRounds.value) {
      addMessage('ai', '面试已完成，正在生成评价报告...')
      await generateReport()
      return
    }

    // 多轮追问：走 SSE 流式接口，逐字显示
    const aiMessageIndex = messages.value.length
    addMessage('ai', '') // 先占位，流式填充

    await fetchStream('/api/ai/generate-follow-up-stream', {
      history: qaList.value.map(qa => ({
        question: qa.question,
        answer: qa.answer
      })),
      currentQuestion: currentQuestion.value,
      currentAnswer: userMessage
    }, {
      onDelta: (chunk) => {
        // 逐字追加到当前 AI 消息
        messages.value[aiMessageIndex].content += chunk
        scrollToBottom()
      },
      onDone: (fullText) => {
        messages.value[aiMessageIndex].content = fullText
        currentQuestion.value = fullText
        currentRound.value++
        progress.value = Math.min((currentRound.value / totalRounds.value) * 100, 100)
      },
      onError: (errorMsg) => {
        messages.value[aiMessageIndex].content = '(AI响应失败，请重试)'
        ElMessage.error('AI响应失败: ' + errorMsg)
      }
    })
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

const generateReport = async () => {
  if (isGenerating.value) return
  isGenerating.value = true

  try {
    await api.post('/interview/generate-report', {
      taskId: Number(taskId),
      qaList: qaList.value
    })

    await api.post(`/interview/end/${taskId}`)
    statusClass.value = 'finished'
    statusText.value = '已结束'

    addMessage('ai', '报告生成完成！正在跳转...')
    setTimeout(() => { router.push(`/report/${taskId}`) }, 1000)
  } catch (error) {
    console.error('生成报告失败:', error)
    addMessage('ai', '报告生成失败，请稍后重试')
  } finally {
    isGenerating.value = false
  }
}

const endInterview = async () => {
  try {
    if (qaList.value.length > 0) {
      await generateReport()
    } else {
      await api.post(`/interview/end/${taskId}`)
      statusClass.value = 'finished'
      statusText.value = '已结束'
      router.push(`/report/${taskId}`)
    }
  } catch (error) {
    console.error('结束面试失败:', error)
    ElMessage.error('结束面试失败')
  }
}
</script>

<style scoped>
/* ── 页面容器 ── */
.interview-page { height:calc(100vh-80px); display:flex; flex-direction:column; }

/* ── 顶部栏 ── */
.interview-header {
  display:flex; justify-content:space-between; align-items:center;
  padding:12px 16px; margin-bottom:16px;
  background:var(--card); border:1px solid var(--card-border);
  border-radius:var(--radius); box-shadow:var(--shadow)
}
.header-left { display:flex; align-items:center; gap:16px }
.header-info { display:flex; align-items:center; gap:10px }
.header-info h1 { font-size:16px; font-weight:600; color:var(--gray-800); margin:0 }

.tag.status {
  display:inline-flex; align-items:center; gap:5px;
  padding:3px 10px; font-size:11px; font-weight:500; border-radius:var(--radius-pill)
}
.tag.status::before { content:''; width:5px; height:5px; border-radius:50%; background:currentColor }

/* ── 主内容区 ── */
.interview-content { flex:1; display:flex; gap:16px; overflow:hidden; }

/* ── 聊天区域 ── */
.chat-area {
  flex:1; display:flex; flex-direction:column;
  background:var(--card); border:1px solid var(--card-border);
  border-radius:var(--radius); box-shadow:var(--shadow); overflow:hidden
}
.chat-messages { flex:1; overflow-y:auto; padding:20px }

/* 加载中 */
.loading-first {
  display:flex; flex-direction:column; align-items:center; justify-content:center;
  padding:60px 24px; gap:16px
}
.loading-first p { font-size:13px; color:var(--gray-400) }
.loading-spinner {
  width:32px; height:32px; border:3px solid var(--gray-100);
  border-top-color:var(--accent); border-radius:50%; animation:spin 0.8s linear infinite
}
@keyframes spin { to { transform:rotate(360deg) } }
.loading-dots span { animation:dotPulse 1.4s infinite }
.loading-dots span:nth-child(2) { animation-delay:0.2s }
.loading-dots span:nth-child(3) { animation-delay:0.4s }
@keyframes dotPulse { 0%,60%,100% { opacity:0 } 30% { opacity:1 } }

/* 消息 */
.message { display:flex; gap:12px; margin-bottom:16px }
.message.user { flex-direction:row-reverse }
.message-avatar {
  width:34px; height:34px; border-radius:50%; flex-shrink:0;
  display:flex; align-items:center; justify-content:center;
  font-size:11px; font-weight:600
}
.message-avatar.ai { background:var(--accent-light); color:var(--accent) }
.message-avatar.user { background:var(--gray-100); color:var(--gray-500) }
.message-body { max-width:70% }
.message.user .message-body { text-align:right }
.message-bubble {
  padding:12px 16px; border-radius:14px; font-size:13px; line-height:1.7
}
.message-bubble.ai { background:var(--gray-50); color:var(--gray-700); border-bottom-left-radius:4px }
.message-bubble.user { background:var(--accent); color:white; border-bottom-right-radius:4px }
.message-time { font-size:10px; color:var(--gray-400); margin-top:4px }

/* ── 输入区 ── */
.chat-input {
  padding:14px; border-top:1px solid var(--card-border);
  display:flex; gap:10px; align-items:flex-end
}
.input-toolbar { display:flex; align-items:center; gap:8px; margin-bottom:8px }

.voice-btn {
  display:inline-flex; align-items:center; gap:6px;
  padding:5px 12px; font-size:11px; font-weight:500;
  background:white; color:var(--gray-600);
  border:1px solid var(--card-border); border-radius:var(--radius-pill);
  cursor:pointer; transition:var(--transition)
}
.voice-btn:hover { background:var(--gray-50); border-color:var(--gray-200) }
.voice-btn.active { background:var(--rose-light); color:var(--rose); border-color:var(--rose) }
.voice-btn svg { width:14px; height:14px }

.voice-status { display:inline-flex; align-items:center; gap:6px; font-size:11px; color:var(--rose) }
.voice-dot { width:6px; height:6px; border-radius:50%; background:var(--rose); animation:pulse 1s infinite }
@keyframes pulse { 0%,100% { opacity:1 } 50% { opacity:0.5 } }

.input-textarea {
  flex:1; height:72px; padding:10px 14px; resize:none;
  font-size:13px; font-family:var(--font); line-height:1.6;
  background:var(--gray-50); border:1px solid var(--card-border);
  border-radius:var(--radius-sm); outline:none; transition:var(--transition)
}
.input-textarea:focus { border-color:var(--accent); background:white; box-shadow:0 0 0 3px var(--accent-light) }

.send-btn {
  height:72px; padding:0 18px;
  background:var(--accent); color:white;
  border:none; border-radius:var(--radius-sm);
  font-size:13px; font-weight:500; cursor:pointer; transition:var(--transition)
}
.send-btn:hover { background:#b45309 }
.send-btn:disabled { opacity:0.4; cursor:not-allowed }

/* ── 右侧面板 ── */
.side-panel { width:280px; display:flex; flex-direction:column; gap:12px }
.panel-card {
  background:var(--card); border:1px solid var(--card-border);
  border-radius:var(--radius); box-shadow:var(--shadow); padding:18px
}
.panel-title {
  font-size:11px; font-weight:600; color:var(--gray-400);
  text-transform:uppercase; letter-spacing:0.05em; margin-bottom:12px
}
.progress-bar { height:6px; background:var(--gray-100); border-radius:100px; overflow:hidden }
.progress-fill { height:100%; background:var(--accent); border-radius:100px; transition:width 0.3s ease }
.progress-text { font-size:12px; color:var(--gray-400); margin-top:8px; text-align:center }
.current-question { font-size:13px; color:var(--gray-700); line-height:1.7 }
.info-list { display:flex; flex-direction:column; gap:10px }
.info-row { display:flex; justify-content:space-between; font-size:12px }
.info-label { color:var(--gray-400) }
.info-value { color:var(--gray-700); font-weight:500; text-align:right }
.mode-hint { display:block; font-size:10px; color:var(--accent); margin-top:2px }

/* ── 浮动摄像头 ── */
.camera-float {
  position:fixed; bottom:24px; right:24px; z-index:999;
  animation:camSlideIn 0.3s ease;
  filter:drop-shadow(0 4px 16px rgba(0,0,0,0.2))
}
@keyframes camSlideIn { from { opacity:0; transform:translateY(16px) scale(0.95) } to { opacity:1; transform:translateY(0) scale(1) } }

@media (max-width:768px) {
  .side-panel { display:none }
  .interview-header { flex-wrap:wrap; gap:8px }
  .header-info h1 { font-size:14px }
}
</style>