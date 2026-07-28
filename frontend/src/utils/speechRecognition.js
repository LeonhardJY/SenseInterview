/**
 * 语音识别工具类
 * 基于 Web Speech API 实现语音转文字
 * 注意：目前仅 Chrome 浏览器支持较好
 */

export class SpeechRecognition {
  constructor() {
    this.recognition = null
    this.isSupported = false
    this.isListening = false
    this.transcript = ''

    this.init()
  }

  /**
   * 初始化语音识别
   */
  init() {
    // 检查浏览器是否支持
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition

    if (!SpeechRecognition) {
      console.warn('当前浏览器不支持语音识别')
      this.isSupported = false
      return
    }

    this.isSupported = true
    this.recognition = new SpeechRecognition()

    // 配置参数
    this.recognition.lang = 'zh-CN'           // 设置中文
    this.recognition.continuous = true         // 持续识别
    this.recognition.interimResults = true     // 返回中间结果
    this.recognition.maxAlternatives = 1       // 最大备选结果数

    // 绑定事件回调
    this.bindEvents()
  }

  /**
   * 绑定事件回调
   */
  bindEvents() {
    if (!this.recognition) return

    // 识别结果回调
    this.recognition.onresult = (event) => {
      let interimTranscript = ''
      let finalTranscript = ''

      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript
        if (event.results[i].isFinal) {
          finalTranscript += transcript
        } else {
          interimTranscript += transcript
        }
      }

      // 更新识别结果
      this.transcript = finalTranscript || interimTranscript

      // 触发结果回调
      if (this.onResult) {
        this.onResult({
          transcript: this.transcript,
          isFinal: !!finalTranscript
        })
      }
    }

    // 开始识别
    this.recognition.onstart = () => {
      this.isListening = true
      if (this.onStart) this.onStart()
    }

    // 结束识别
    this.recognition.onend = () => {
      this.isListening = false
      if (this.onEnd) this.onEnd()
    }

    // 错误处理
    this.recognition.onerror = (event) => {
      console.error('语音识别错误:', event.error)
      this.isListening = false

      if (this.onError) {
        this.onError(event.error)
      }
    }
  }

  /**
   * 开始识别
   * @param {Object} callbacks - 回调函数
   * @param {Function} callbacks.onResult - 识别结果回调
   * @param {Function} callbacks.onStart - 开始回调
   * @param {Function} callbacks.onEnd - 结束回调
   * @param {Function} callbacks.onError - 错误回调
   */
  start(callbacks = {}) {
    if (!this.isSupported) {
      console.error('浏览器不支持语音识别')
      return false
    }

    if (this.isListening) {
      console.warn('正在识别中...')
      return false
    }

    // 设置回调
    this.onResult = callbacks.onResult
    this.onStart = callbacks.onStart
    this.onEnd = callbacks.onEnd
    this.onError = callbacks.onError

    // 清空之前的识别结果
    this.transcript = ''

    try {
      this.recognition.start()
      return true
    } catch (error) {
      console.error('启动语音识别失败:', error)
      return false
    }
  }

  /**
   * 停止识别
   */
  stop() {
    if (this.recognition && this.isListening) {
      this.recognition.stop()
    }
  }

  /**
   * 中断识别
   */
  abort() {
    if (this.recognition && this.isListening) {
      this.recognition.abort()
    }
  }

  /**
   * 获取当前识别状态
   * @returns {Object} 状态信息
   */
  getStatus() {
    return {
      isSupported: this.isSupported,
      isListening: this.isListening,
      transcript: this.transcript
    }
  }
}

/**
 * 创建语音识别实例的工厂函数
 * @returns {SpeechRecognition} 语音识别实例
 */
export function createSpeechRecognition() {
  return new SpeechRecognition()
}

/**
 * 检查浏览器是否支持语音识别
 * @returns {boolean} 是否支持
 */
export function isSpeechRecognitionSupported() {
  return !!(window.SpeechRecognition || window.webkitSpeechRecognition)
}
