import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    // 超时错误给出友好提示
    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default api

// ========== Agent 面试 ==========

// Agent 对话涉及 DeepSeek 生成 + 工具调用，耗时较长，单独放宽超时
const LONG_TIMEOUT = 120000

/** Agent 面试官对话 */
export const agentChat = (sessionId, message) =>
  api.post('/ai/agent-chat', { sessionId, message }, { timeout: LONG_TIMEOUT })

/** 结束 Agent 面试会话 */
export const agentEnd = (sessionId) => api.post('/ai/agent-end', { sessionId })

/**
 * Agent 面试官流式对话（SSE）：POST + fetch ReadableStream 逐 token 回调。
 * 相比阻塞式 agentChat，首 token 即到即渲染，显著降低"等全文"的感知延迟。
 * @param {string} sessionId 会话 ID
 * @param {string} message   用户消息
 * @param {{onToken?:Function,onComplete?:Function,onError?:Function}} handlers 回调
 */
export const agentChatStream = async (sessionId, message, { onToken, onComplete, onError } = {}) => {
  const token = localStorage.getItem('token')
  try {
    const resp = await fetch('/api/ai/agent-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ sessionId, message })
    })
    if (!resp.ok || !resp.body) throw new Error(`SSE 请求失败: ${resp.status}`)

    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    for (;;) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      // SSE 帧以空行(\n\n)分隔；一帧内可能有多行 data:（按规范用 \n 连接）
      const frames = buffer.split('\n\n')
      buffer = frames.pop() // 末段可能不完整，留到下次拼接
      for (const frame of frames) {
        const dataLines = frame.split('\n')
          .filter(l => l.startsWith('data:'))
          .map(l => l.slice(5).replace(/^ /, ''))
        if (dataLines.length) onToken && onToken(dataLines.join('\n'))
      }
    }
    onComplete && onComplete()
  } catch (e) {
    onError && onError(e)
  }
}