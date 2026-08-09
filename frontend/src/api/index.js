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