<template>
  <div class="camera-feed" :class="{ minimized, active: isActive }">
    <video ref="videoRef" autoplay playsinline muted :class="{ mirrored }" />

    <div v-if="!isActive && !error && !loading" class="cam-overlay">
      <div class="cam-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M23 7l-7 5 7 5V7z"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg></div>
      <p class="cam-hint">摄像头已关闭</p>
      <button class="btn btn-sm btn-primary" @click="startCamera">开启</button>
    </div>

    <div v-if="loading" class="cam-overlay"><div class="cam-spinner"></div><p class="cam-hint">启动摄像头...</p></div>

    <div v-if="error" class="cam-overlay error">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="cam-icon"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      <p class="cam-hint">{{ error }}</p>
      <button class="btn btn-sm btn-outline" @click="startCamera">重试</button>
    </div>

    <div v-if="isActive && emotionDisplay" class="emotion-badge" :class="'emotion-' + emotionDisplay.emotion">
      <span class="emotion-icon">{{ emotionIcon }}</span>
      <span class="emotion-label">{{ emotionText }}</span>
      <span class="emotion-conf">{{ (emotionDisplay.confidence * 100).toFixed(0) }}%</span>
    </div>

    <div v-if="isActive && emotionServiceOffline" class="emotion-badge emotion-offline">
      <span class="emotion-label">表情分析未连接</span>
    </div>

    <div v-if="isActive" class="cam-topbar">
      <button class="cam-btn" @click="toggleMinimize">
        <svg viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M2 2h8v1H2zM2 5h8v1H2zM2 8h8v1H2z" v-if="!minimized"/><path d="M2 2h8v8H2z" v-else/></svg>
      </button>
      <button class="cam-btn cam-btn-danger" @click="stopCamera">
        <svg viewBox="0 0 12 12" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="2" y1="2" x2="10" y2="10"/><line x1="10" y1="2" x2="2" y2="10"/></svg>
      </button>
    </div>

    <div v-if="minimized && isActive" class="min-label" @click="toggleMinimize">
      <span class="min-dot"></span> 摄像头
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, computed } from 'vue'
import api from '@/api'

const props = defineProps({
  mirrored: { type: Boolean, default: true },
  enableEmotion: { type: Boolean, default: true },
  emotionInterval: { type: Number, default: 4000 }
})

const emit = defineEmits(['frame', 'error', 'emotion'])

const videoRef = ref(null)
const isActive = ref(false)
const loading = ref(false)
const minimized = ref(false)
const error = ref(null)
let mediaStream = null
let animFrame = null
let emotionTimer = null
let canvas = null

const lastEmotion = ref(null)
const emotionServiceOffline = ref(false)
let failCount = 0

const emotionDisplay = computed(() => {
  if (!lastEmotion.value) return null
  const e = lastEmotion.value.emotion
  return (e && e !== 'unknown' && e !== 'none') ? lastEmotion.value : null
})

const emotionIcon = computed(() => {
  const map = { happy: '😊', sad: '😔', angry: '😠', surprise: '😮', fear: '😨', disgust: '😣', neutral: '😐' }
  return map[lastEmotion.value?.emotion] || '❓'
})

const emotionText = computed(() => {
  const map = { happy: '自信', sad: '低落', angry: '紧张', surprise: '惊讶', fear: '紧张', disgust: '不适', neutral: '平静' }
  return map[lastEmotion.value?.emotion] || '分析中'
})

function startCamera() {
  error.value = null
  loading.value = true
  navigator.mediaDevices.getUserMedia({ video: { width: 320, height: 240, facingMode: 'user' } })
    .then(stream => {
      mediaStream = stream
      if (videoRef.value) videoRef.value.srcObject = stream
      isActive.value = true
      loading.value = false
      startFrameCapture()
      if (props.enableEmotion) startEmotionAnalysis()
    })
    .catch(e => {
      loading.value = false
      if (e.name === 'NotAllowedError') error.value = '请允许摄像头权限'
      else if (e.name === 'NotFoundError') error.value = '未检测到摄像头'
      else error.value = e.message
      emit('error', error.value)
    })
}

function stopCamera() {
  if (mediaStream) { mediaStream.getTracks().forEach(t => t.stop()); mediaStream = null }
  if (videoRef.value) videoRef.value.srcObject = null
  isActive.value = false
  minimized.value = false
  stopFrameCapture()
  stopEmotionAnalysis()
}

function startFrameCapture() {
  const fn = () => {
    if (!isActive.value || !videoRef.value) return
    if (videoRef.value.readyState >= 2) emit('frame', videoRef.value)
    animFrame = requestAnimationFrame(fn)
  }
  fn()
}

function stopFrameCapture() {
  if (animFrame) { cancelAnimationFrame(animFrame); animFrame = null }
}

function startEmotionAnalysis() {
  canvas = document.createElement('canvas')
  canvas.width = 160; canvas.height = 120
  failCount = 0

  const analyze = async () => {
    if (!isActive.value || !videoRef.value || !canvas) return
    try {
      const ctx = canvas.getContext('2d')
      ctx.drawImage(videoRef.value, 0, 0, 160, 120)
      const base64 = canvas.toDataURL('image/jpeg', 0.8).split(',')[1]
      const res = await api.post('/ai/analyze-emotion', { image: base64 })

      if (res.data?.error) {
        // DeepFace 未启动 / 调用失败
        failCount++
        if (failCount >= 2) emotionServiceOffline.value = true
      } else if (res.data?.emotion && !['unknown', 'none'].includes(res.data.emotion)) {
        // 成功识别到情绪
        lastEmotion.value = res.data
        emotionServiceOffline.value = false
        failCount = 0
        emit('emotion', res.data)
      }
    } catch (e) {
      failCount++
      if (failCount >= 2) emotionServiceOffline.value = true
    }
    if (isActive.value) emotionTimer = setTimeout(analyze, props.emotionInterval)
  }
  setTimeout(analyze, 1500)
}

function stopEmotionAnalysis() {
  if (emotionTimer) { clearTimeout(emotionTimer); emotionTimer = null }
  canvas = null
}

function toggleMinimize() { minimized.value = !minimized.value }

onUnmounted(stopCamera)

defineExpose({ startCamera, stopCamera })
</script>

<style scoped>
.camera-feed {
  position: relative; width: 240px; border-radius: 12px; overflow: hidden;
  background: #000; box-shadow: 0 4px 20px rgba(0,0,0,0.3);
  transition: all 0.3s ease; border: 2px solid transparent;
}
.camera-feed.active { border-color: var(--primary); }
.camera-feed.minimized { width: 120px; height: 90px; border-radius: 8px; cursor: pointer; }
video { width: 100%; display: block; aspect-ratio: 4/3; object-fit: cover; }
video.mirrored { transform: scaleX(-1); }

.cam-overlay {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 8px;
  background: var(--gray-100); color: var(--gray-500); padding: 16px; text-align: center;
}
.cam-overlay.error { color: var(--danger); background: var(--danger-bg); }
.cam-icon { width: 40px; height: 40px; opacity: 0.5; }
.cam-hint { font-size: 12px; margin: 0; line-height: 1.4; }
.cam-spinner { width: 28px; height: 28px; border: 3px solid var(--gray-200); border-top-color: var(--primary); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.emotion-badge {
  position: absolute; bottom: 8px; left: 8px; right: 8px;
  display: flex; align-items: center; gap: 6px;
  padding: 6px 10px; background: rgba(0,0,0,0.7); backdrop-filter: blur(4px);
  border-radius: 8px; font-size: 13px; font-weight: 600; color: white;
}
.emotion-badge.emotion-offline { font-size: 11px; font-weight: 400; color: var(--gray-400); }
.emotion-icon { font-size: 18px; }
.emotion-label { flex: 1; }
.emotion-conf { font-size: 11px; opacity: 0.8; }
.emotion-happy { color: #4ade80; }
.emotion-neutral { color: #94a3b8; }
.emotion-surprise { color: #fbbf24; }
.emotion-sad { color: #60a5fa; }
.emotion-angry, .emotion-fear { color: #f87171; }
.emotion-disgust { color: #a78bfa; }

.cam-topbar { position: absolute; top: 4px; right: 4px; display: flex; gap: 4px; opacity: 0; transition: opacity 0.2s; }
.camera-feed:hover .cam-topbar { opacity: 1; }
.cam-btn {
  width: 24px; height: 24px; border: none; border-radius: 4px;
  background: rgba(0,0,0,0.5); color: white; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.cam-btn:hover { background: rgba(0,0,0,0.8); }
.cam-btn-danger:hover { background: var(--danger); }
.cam-btn svg { width: 12px; height: 12px; }

.min-label {
  position: absolute; inset: 0; display: flex; align-items: center;
  justify-content: center; gap: 6px; font-size: 12px; color: white;
  background: rgba(0,0,0,0.4);
}
.min-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--success); animation: pulse 1.5s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>
