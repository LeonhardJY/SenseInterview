/**
 * 音频录制工具
 *
 * 基于 MediaRecorder API 录制用户语音，
 * 输出可直接上传后端的 Blob。
 *
 * 和 speechRecognition.js 的关系：
 * - speechRecognition 用 Web Speech API 实时识别（浏览器内置，依赖 Chrome）
 * - audioRecorder 录制原始音频上传到后端 Whisper（精度更高，跨浏览器）
 * 两者可并存，根据面试模式切换。
 */

export class AudioRecorder {
  constructor() {
    this.mediaRecorder = null
    this.audioChunks = []
    this.audioBlob = null
    this.isRecording = false
    this.stream = null
    this.mimeType = this.getSupportedMimeType()
  }

  /**
   * 获取浏览器支持的音频格式（优先选高质量的）
   */
  getSupportedMimeType() {
    const types = [
      'audio/webm;codecs=opus',
      'audio/webm',
      'audio/ogg;codecs=opus',
      'audio/mp4'
    ]
    for (const type of types) {
      if (MediaRecorder.isTypeSupported(type)) {
        return type
      }
    }
    return 'audio/webm'
  }

  /**
   * 检查浏览器是否支持录音
   */
  static isSupported() {
    return !!(
      navigator.mediaDevices &&
      navigator.mediaDevices.getUserMedia &&
      MediaRecorder
    )
  }

  /**
   * 开始录制
   * @returns {Promise<boolean>} 是否成功启动
   */
  async start() {
    if (this.isRecording) {
      console.warn('[AudioRecorder] 正在录制中')
      return false
    }

    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      this.audioChunks = []
      this.audioBlob = null

      this.mediaRecorder = new MediaRecorder(this.stream, {
        mimeType: this.mimeType
      })

      this.mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data)
        }
      }

      this.mediaRecorder.onstop = () => {
        this.audioBlob = new Blob(this.audioChunks, { type: this.mimeType })
        this.cleanup()
      }

      this.mediaRecorder.start()
      this.isRecording = true
      return true
    } catch (error) {
      console.error('[AudioRecorder] 启动录音失败:', error)
      if (error.name === 'NotAllowedError') {
        throw new Error('请允许使用麦克风权限')
      }
      throw new Error('启动录音失败: ' + error.message)
    }
  }

  /**
   * 停止录制
   * @returns {Promise<Blob|null>} 录制的音频数据
   */
  stop() {
    return new Promise((resolve) => {
      if (!this.mediaRecorder || !this.isRecording) {
        resolve(null)
        return
      }

      this.mediaRecorder.onstop = () => {
        this.audioBlob = new Blob(this.audioChunks, { type: this.mimeType })
        this.cleanup()
        resolve(this.audioBlob)
      }

      this.mediaRecorder.stop()
      this.isRecording = false
    })
  }

  /**
   * 获取录制时长（秒）
   */
  getDuration() {
    if (!this.mediaRecorder) return 0
    // MediaRecorder 没有直接提供时长，可根据 chunks 估算
    return 0
  }

  /**
   * 清理资源
   */
  cleanup() {
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop())
      this.stream = null
    }
    this.mediaRecorder = null
  }

  /**
   * 销毁
   */
  destroy() {
    if (this.isRecording) {
      this.mediaRecorder.stop()
    }
    this.cleanup()
    this.audioChunks = []
    this.audioBlob = null
    this.isRecording = false
  }
}

/**
 * 创建录音实例的工厂函数
 */
export function createAudioRecorder() {
  return new AudioRecorder()
}

/**
 * 检查浏览器是否支持录音
 */
export function isAudioRecordingSupported() {
  return AudioRecorder.isSupported()
}
