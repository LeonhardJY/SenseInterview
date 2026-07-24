<template>
  <div class="history-page">
    <div class="page-header">
      <div class="ph-left">
        <button class="btn-back" @click="$router.push('/lobby')">← 返回</button>
        <h1>练习记录</h1>
        <span class="ph-count">共 {{ historyList.length }} 条记录</span>
      </div>
    </div>

    <div class="history-list">
      <div v-for="item in historyList" :key="item.id" class="h-card">
        <div class="h-left">
          <div class="h-status" :class="statusClass(item.status)">{{ statusText(item.status) }}</div>
          <div class="h-info">
            <div class="h-title">{{ item.jobName }}</div>
            <div class="h-meta">
              <span>{{ modeText(item.mode) }}</span>
              <span class="dot">·</span>
              <span class="level-tag" :class="'level-' + (item.difficulty || 'MEDIUM').toLowerCase()">{{ levelText(item.difficulty) }}</span>
              <span class="dot">·</span>
              <span>{{ item.createTime }}</span>
            </div>
          </div>
        </div>
        <div class="h-actions">
          <button v-if="item.status === 'FINISHED'" class="btn-ghost" @click="viewReport(item)">查看报告</button>
          <button v-else class="btn-primary-sm" @click="continueInterview(item)">继续面试</button>
          <button class="btn-delete" @click="confirmDelete(item)">删除</button>
        </div>
      </div>
      <div v-if="historyList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon"><path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
        <p>暂无练习记录</p>
        <button class="btn-primary" @click="$router.push('/lobby')">开始第一场面试</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const router = useRouter()
const historyList = ref([])

onMounted(() => loadHistory())

const loadHistory = async () => {
  try {
    const res = await api.get('/interview/list')
    historyList.value = res.data || []
  } catch (e) { console.error(e) }
}

const statusText = (s) => ({ CREATED: '未开始', RUNNING: '进行中', FINISHED: '已结束' }[s] || s)
const statusClass = (s) => ({ CREATED: 'waiting', RUNNING: 'live', FINISHED: 'ended' }[s] || '')
const levelText = (l) => ({ EASY: '初级', MEDIUM: '中级', HARD: '高级' }[l] || l)
const modeText = (m) => ({ TEXT: '文字面试', VOICE: '语音面试', VIDEO: '视频面试' }[m] || m)

const viewReport = (item) => router.push(`/report/${item.id}`)
const continueInterview = (item) => router.push(`/interview/${item.id}`)

const confirmDelete = (item) => {
  ElMessageBox.confirm(
    `确定要删除「${item.jobName}」的${modeText(item.mode)}记录吗？删除后不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await api.delete(`/interview/${item.id}`)
    ElMessage.success('删除成功')
    loadHistory()
  }).catch(() => {})
}
</script>

<style scoped>
.history-page { max-width: 900px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.ph-left { display: flex; align-items: center; gap: 12px; }
.ph-left h1 { font-size: 22px; font-weight: normal; color: #1A1A1A; margin: 0; }
.ph-count { font-family: sans-serif; font-size: 13px; color: #9E9E9E; }
.btn-back { background: none; border: 1px solid #C4C4C4; border-radius: 4px; padding: 6px 12px; font-size: 13px; color: #666; cursor: pointer; transition: all 0.2s; }
.btn-back:hover { border-color: #C74634; color: #C74634; }
.history-list { display: flex; flex-direction: column; gap: 10px; }
.h-card { background: #fff; border: 1px solid #E8E8E8; border-radius: 6px; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center; transition: all 0.2s; }
.h-card:hover { border-color: #C74634; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.h-left { display: flex; align-items: center; gap: 16px; }
.h-status { font-family: sans-serif; font-size: 11px; padding: 3px 10px; border-radius: 999px; white-space: nowrap; }
.h-status.waiting { color: #5C5C5C; background: #F5F5F5; border: 1px solid #E8E8E8; }
.h-status.live { color: #A83A2B; background: #FBEEEC; border: 1px solid #F0C9C2; }
.h-status.ended { color: #767676; background: #F5F5F5; border: 1px solid #E8E8E8; }
.h-title { font-size: 15px; color: #1A1A1A; margin-bottom: 4px; }
.h-meta { font-family: sans-serif; font-size: 12px; color: #9E9E9E; display: flex; align-items: center; gap: 6px; }
.dot { color: #C4C4C4; }
.level-tag { padding: 1px 8px; border-radius: 4px; font-size: 11px; font-weight: 500; }
.level-easy { color: #2E7D32; background: #E8F5E9; }
.level-medium { color: #E65100; background: #FFF3E0; }
.level-hard { color: #C62828; background: #FFEBEE; }
.h-actions { flex-shrink: 0; display: flex; gap: 8px; }
.btn-ghost { padding: 7px 16px; background: transparent; color: #C74634; border: 1px solid #C74634; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s; }
.btn-ghost:hover { background: #C74634; color: #fff; }
.btn-primary-sm { padding: 7px 16px; background: #C74634; color: #fff; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: background 0.2s; }
.btn-primary-sm:hover { background: #A83A2B; }
.btn-delete { padding: 7px 16px; background: transparent; color: #9E9E9E; border: 1px solid #E8E8E8; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s; }
.btn-delete:hover { color: #D32F2F; border-color: #D32F2F; }
.btn-primary { padding: 10px 20px; background: #C74634; color: #fff; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; transition: background 0.2s; }
.btn-primary:hover { background: #A83A2B; }
.empty-state { text-align: center; padding: 60px 20px; color: #9E9E9E; }
.empty-icon { width: 48px; height: 48px; margin-bottom: 16px; }
.empty-state p { margin-bottom: 16px; font-size: 14px; }
</style>