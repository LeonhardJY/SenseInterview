/**
 * WebSocket 连接管理
 *
 * 封装了与后端面试房间的 WebSocket 通信：
 * - 自动连接/重连
 * - 心跳保活
 * - 统一的消息分发
 * - 连接状态管理
 */

/**
 * 开发环境通过 Vite proxy 转发，生产环境通过网关转发
 * 用相对路径，自动适配当前页面的 host:port
 */
const WS_BASE_URL = `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws/interview`
const RECONNECT_DELAY = 3000    // 重连间隔
const PING_INTERVAL = 15000      // 心跳间隔
const MAX_RECONNECT_ATTEMPTS = 5 // 最大重连次数

export class InterviewWebSocket {
  constructor(taskId, userId) {
    this.taskId = taskId
    this.userId = userId
    this.ws = null
    this.isConnected = false
    this.reconnectAttempts = 0
    this.pingTimer = null
    this.reconnectTimer = null
    this.destroyed = false

    // 消息回调注册表
    this.handlers = {}
  }

  /**
   * 建立连接
   */
  connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) return

    this.destroyed = false
    const url = `${WS_BASE_URL}/${this.taskId}?userId=${this.userId}`

    try {
      this.ws = new WebSocket(url)
    } catch (e) {
      console.error('[WebSocket] 创建连接失败:', e)
      this.scheduleReconnect()
      return
    }

    this.ws.onopen = () => {
      console.log('[WebSocket] 已连接, taskId:', this.taskId)
      this.isConnected = true
      this.reconnectAttempts = 0
      this.startPing()

      if (this.handlers['open']) {
        this.handlers['open']()
      }
    }

    this.ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        this.dispatch(msg)
      } catch (e) {
        console.warn('[WebSocket] 消息解析失败:', e)
      }
    }

    this.ws.onclose = (event) => {
      console.log('[WebSocket] 连接关闭, code:', event.code)
      this.isConnected = false
      this.stopPing()

      if (this.handlers['close']) {
        this.handlers['close'](event)
      }

      // 非正常关闭时自动重连
      if (!this.destroyed && event.code !== 1000) {
        this.scheduleReconnect()
      }
    }

    this.ws.onerror = (error) => {
      console.error('[WebSocket] 连接错误:', error)
      if (this.handlers['error']) {
        this.handlers['error'](error)
      }
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    this.destroyed = true
    this.stopPing()
    clearTimeout(this.reconnectTimer)

    if (this.ws) {
      this.ws.close(1000, '用户离开')
      this.ws = null
    }
    this.isConnected = false
  }

  /**
   * 注册消息处理器
   * @param {string} type - 消息类型
   * @param {Function} handler - 处理函数 (data) => void
   */
  on(type, handler) {
    this.handlers[type] = handler
    return this
  }

  /**
   * 发送消息
   */
  send(type, data = {}) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('[WebSocket] 未连接，无法发送消息')
      return
    }
    this.ws.send(JSON.stringify({ type, ...data }))
  }

  // ========== 内部方法 ==========

  /** 分发消息到注册的处理器 */
  dispatch(msg) {
    const handler = this.handlers[msg.type]
    if (handler) {
      handler(msg.data, msg)
    } else {
      console.debug('[WebSocket] 未处理的消息类型:', msg.type, msg)
    }
  }

  /** 心跳保活 */
  startPing() {
    this.stopPing()
    this.pingTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'PING' }))
      }
    }, PING_INTERVAL)
  }

  stopPing() {
    if (this.pingTimer) {
      clearInterval(this.pingTimer)
      this.pingTimer = null
    }
  }

  /** 自动重连 */
  scheduleReconnect() {
    if (this.destroyed) return
    if (this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.error('[WebSocket] 重连次数已达上限')
      return
    }

    this.reconnectAttempts++
    const delay = RECONNECT_DELAY * Math.min(this.reconnectAttempts, 3)
    console.log(`[WebSocket] 将在 ${delay}ms 后重连 (第${this.reconnectAttempts}次)`)

    this.reconnectTimer = setTimeout(() => {
      if (!this.destroyed) {
        this.connect()
      }
    }, delay)
  }
}

/**
 * 创建面试 WebSocket 连接的快捷方法
 */
export function createInterviewSocket(taskId, userId) {
  return new InterviewWebSocket(taskId, userId)
}
