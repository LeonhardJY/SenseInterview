<template>
  <div class="history-page">
    <div class="page-header">
      <div class="page-header-left">
        <button class="btn btn-ghost btn-back" @click="$router.push('/lobby')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回
        </button>
        <div>
          <h1 class="page-title">练习记录</h1>
          <p class="page-subtitle">共 {{ historyList.length }} 条记录</p>
        </div>
      </div>
    </div>

    <div class="history-list">
      <div v-for="item in historyList" :key="item.id" class="history-card">
        <div class="card-left">
          <div class="status-badge" :class="statusClass(item.status)">
            <span class="status-dot"></span>
            {{ statusText(item.status) }}
          </div>
        </div>
        <div class="card-center">
          <h3 class="card-title">{{ item.jobName }}</h3>
          <div class="card-meta">
            <span>{{ modeText(item.mode) }}</span>
            <span class="meta-dot"></span>
            <span class="level-tag" :class="'level-' + (item.difficulty || 'MEDIUM').toLowerCase()">{{ levelText(item.difficulty) }}</span>
            <span class="meta-dot"></span>
            <span>{{ item.createTime }}</span>
          </div>
        </div>
        <div class="card-actions">
          <button v-if="item.status === 'FINISHED'" class="btn btn-primary btn-sm" @click="viewReport(item)">查看报告</button>
          <button v-else class="btn btn-outline btn-sm" @click="continueInterview(item)">继续面试</button>
          <button class="btn btn-ghost btn-sm" @click="confirmDelete(item)">删除</button>
        </div>
      </div>

      <div v-if="historyList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
        <p>暂无练习记录</p>
        <button class="btn btn-primary" @click="$router.push('/lobby')">开始第一场面试</button>
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
const statusClass = (s) => ({ CREATED: 'created', RUNNING: 'running', FINISHED: 'finished' }[s] || '')
const levelText = (l) => ({ EASY: '初级', MEDIUM: '中级', HARD: '高级' }[l] || l)
const modeText = (m) => ({ TEXT: '文字面试', VOICE: '语音面试', VIDEO: '视频面试' }[m] || m)

const viewReport = (item) => router.push(`/report/${item.id}`)
const continueInterview = (item) => router.push(`/interview/${item.id}`)

const confirmDelete = (item) => {
  ElMessageBox.confirm(
    `确定要删除「${item.jobName}」的${modeText(item.mode)}记录吗？`,
    '确认删除',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await api.delete(`/interview/${item.id}`)
    ElMessage.success('删除成功')
    loadHistory()
  }).catch(() => {})
}
</script>

<style scoped>
.history-page { max-width: 900px; margin: 0 auto; }

.history-list { display: flex; flex-direction: column; gap: var(--space-3); }

.history-card {
  background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius);
  padding: var(--space-5); display: flex; align-items: center; gap: var(--space-5);
  transition: var(--transition);
}

.history-card:hover { border-color: var(--gray-300); box-shadow: var(--shadow); }

.card-left { flex-shrink: 0; }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px; font-size: 12px; font-weight: 500; border-radius: 9999px;
}

.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

.status-badge.finished { color: var(--gray-500); background: var(--gray-100); }
.status-badge.running { color: var(--primary); background: var(--primary-bg); }
.status-badge.created { color: var(--warning); background: var(--warning-bg); }

.card-center { flex: 1; min-width: 0; }

.card-title { font-size: 15px; font-weight: 600; color: var(--gray-900); margin-bottom: 4px; }

.card-meta {
  display: flex; align-items: center; gap: var(--space-2);
  font-size: 13px; color: var(--gray-500);
}

.meta-dot { width: 3px; height: 3px; border-radius: 50%; background: var(--gray-300); }

.level-tag { font-size: 12px; font-weight: 500; padding: 1px 8px; border-radius: 4px; }
.level-easy { color: var(--level-easy); background: var(--level-easy-bg); }
.level-medium { color: var(--level-medium); background: var(--level-medium-bg); }
.level-hard { color: var(--level-hard); background: var(--level-hard-bg); }

.card-actions { display: flex; gap: var(--space-2); flex-shrink: 0; }
</style>