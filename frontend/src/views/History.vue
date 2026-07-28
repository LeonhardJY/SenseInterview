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
          <p class="page-subtitle">共 {{ filteredList.length }} 条记录</p>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <select v-model="filterStatus" class="filter-select">
        <option value="">全部状态</option>
        <option value="FINISHED">已完成</option>
        <option value="RUNNING">进行中</option>
        <option value="CREATED">待开始</option>
      </select>
      <select v-model="filterMode" class="filter-select">
        <option value="">全部模式</option>
        <option value="TEXT">文字面试</option>
        <option value="VOICE">语音面试</option>
        <option value="VIDEO">视频面试</option>
      </select>
      <select v-model="sortBy" class="filter-select">
        <option value="time">按时间排序</option>
        <option value="score">按分数排序</option>
      </select>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-value">{{ historyList.length }}</span>
        <span class="stat-label">总面试数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ completedCount }}</span>
        <span class="stat-label">已完成</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ avgScore }}</span>
        <span class="stat-label">平均分</span>
      </div>
    </div>

    <!-- 面试列表 -->
    <div class="history-list">
      <div v-for="item in filteredList" :key="item.id" class="history-card" @click="viewDetail(item)">
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
            <span class="level-tag" :class="levelClass(item.difficulty)">{{ levelText(item.difficulty) }}</span>
            <span class="meta-dot"></span>
            <span>{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
        <div class="card-right">
          <div v-if="item.score !== undefined && item.score !== null" class="score-badge" :class="scoreLevel(item.score)">
            {{ item.score }}
          </div>
          <div v-else class="score-badge no-score">-</div>
        </div>
        <div class="card-actions" @click.stop>
          <button v-if="item.status === 'FINISHED' || item.status === 'COMPLETED'" class="btn btn-primary btn-sm" @click="viewReport(item)">
            查看报告
          </button>
          <button v-else-if="item.status === 'RUNNING' || item.status === 'IN_PROGRESS'" class="btn btn-outline btn-sm" @click="continueInterview(item)">
            继续面试
          </button>
          <button v-else class="btn btn-outline btn-sm" @click="continueInterview(item)">
            开始面试
          </button>
          <button class="btn btn-ghost btn-sm" @click="confirmDelete(item)">
            删除
          </button>
        </div>
      </div>

      <div v-if="filteredList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
        <p>暂无练习记录</p>
        <button class="btn btn-primary" @click="$router.push('/lobby')">开始第一场面试</button>
      </div>
    </div>

    <!-- 面试详情弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>面试详情</h3>
              <button class="modal-close" @click="showDetailModal = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body" v-if="selectedItem">
              <div class="detail-grid">
                <div class="detail-item">
                  <span class="detail-label">岗位</span>
                  <span class="detail-value">{{ selectedItem.jobName }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">模式</span>
                  <span class="detail-value">{{ modeText(selectedItem.mode) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">难度</span>
                  <span class="detail-value">{{ levelText(selectedItem.difficulty) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">状态</span>
                  <span class="detail-value">{{ statusText(selectedItem.status) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">创建时间</span>
                  <span class="detail-value">{{ formatDate(selectedItem.createTime) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">开始时间</span>
                  <span class="detail-value">{{ formatDate(selectedItem.startTime) }}</span>
                </div>
                <div class="detail-item">
                  <span class="detail-label">结束时间</span>
                  <span class="detail-value">{{ formatDate(selectedItem.endTime) }}</span>
                </div>
                <div class="detail-item" v-if="selectedItem.score !== undefined">
                  <span class="detail-label">得分</span>
                  <span class="detail-value score">{{ selectedItem.score || '-' }}</span>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="showDetailModal = false">关闭</button>
              <button v-if="selectedItem?.status === 'FINISHED' || selectedItem?.status === 'COMPLETED'" class="btn btn-primary" @click="viewReport(selectedItem)">
                查看报告
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store'
import api from '@/api'
import {
  statusText, statusClass,
  levelText, levelClass,
  modeText, formatDate, scoreLevel
} from '@/utils/constants'

const router = useRouter()
const userStore = useUserStore()
const historyList = ref([])
const filterStatus = ref('')
const filterMode = ref('')
const sortBy = ref('time')
const showDetailModal = ref(false)
const selectedItem = ref(null)

// 统计数据
const completedCount = computed(() => {
  return historyList.value.filter(item => item.status === 'FINISHED' || item.status === 'COMPLETED').length
})

const avgScore = computed(() => {
  const scored = historyList.value.filter(item => item.score != null)
  if (scored.length === 0) return '-'
  const sum = scored.reduce((acc, item) => acc + item.score, 0)
  return Math.round(sum / scored.length)
})

// 筛选和排序
const filteredList = computed(() => {
  let list = [...historyList.value]

  // 筛选状态
  if (filterStatus.value) {
    list = list.filter(item => item.status === filterStatus.value)
  }

  // 筛选模式
  if (filterMode.value) {
    list = list.filter(item => item.mode === filterMode.value)
  }

  // 排序
  if (sortBy.value === 'score') {
    list.sort((a, b) => (b.score || 0) - (a.score || 0))
  } else {
    list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  }

  return list
})

onMounted(() => loadHistory())

const loadHistory = async () => {
  try {
    const res = await api.get('/interview/list', {
      params: { userId: userStore.userId || undefined }
    })
    historyList.value = res.data || []

    // 加载每个面试的报告分数
    await loadScores()
  } catch (e) { console.error(e) }
}

const loadScores = async () => {
  await Promise.allSettled(historyList.value.map(async (item) => {
    // 后端用 FINISHED，前端也兼容 COMPLETED
    if (item.status !== 'FINISHED' && item.status !== 'COMPLETED') return
    try {
      const res = await api.get(`/report/${item.id}`)
      if (res.data) {
        item.score = res.data.totalScore
      }
    } catch (e) {
      console.debug('报告未生成:', item.id)
    }
  }))
}

const viewDetail = (item) => {
  selectedItem.value = item
  showDetailModal.value = true
}

const viewReport = (item) => {
  showDetailModal.value = false
  router.push(`/report/${item.id}`)
}

const continueInterview = (item) => {
  router.push(`/interview/${item.id}`)
}

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

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-back svg {
  width: 18px;
  height: 18px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0;
}

.page-subtitle {
  font-size: 13px;
  color: var(--gray-500);
  margin: 4px 0 0 0;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.filter-select {
  height: 36px;
  padding: 0 12px;
  font-size: 13px;
  border: 1px solid var(--gray-200);
  border-radius: var(--border-radius-sm);
  background: white;
  cursor: pointer;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.stat-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: var(--space-4);
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: var(--gray-900);
}

.stat-label {
  font-size: 12px;
  color: var(--gray-500);
}

/* 面试列表 */
.history-list { display: flex; flex-direction: column; gap: var(--space-3); }

.history-card {
  background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius);
  padding: var(--space-5); display: flex; align-items: center; gap: var(--space-5);
  transition: var(--transition); cursor: pointer;
}

.history-card:hover { border-color: var(--gray-300); box-shadow: var(--shadow); }

.card-left { flex-shrink: 0; }

.status-badge {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 4px 12px; font-size: 12px; font-weight: 500; border-radius: 9999px;
}

.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }

.status-badge.finished { color: var(--success); background: var(--success-bg); }
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

/* 分数 */
.card-right {
  flex-shrink: 0;
}

.score-badge {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.score-badge.excellent {
  background: var(--success-bg);
  color: var(--success);
}

.score-badge.good {
  background: var(--primary-bg);
  color: var(--primary);
}

.score-badge.average {
  background: var(--warning-bg);
  color: var(--warning);
}

.score-badge.poor {
  background: var(--danger-bg);
  color: var(--danger);
}

.score-badge.no-score {
  background: var(--gray-100);
  color: var(--gray-400);
}

.card-actions { display: flex; gap: var(--space-2); flex-shrink: 0; }

/* 按钮样式使用全局 design-system.css 中的 .btn 体系 */

.empty-state {
  text-align: center;
  padding: var(--space-10);
  color: var(--gray-400);
}

.empty-state svg {
  width: 48px;
  height: 48px;
  margin-bottom: var(--space-4);
}

.empty-state p {
  margin-bottom: var(--space-4);
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: var(--border-radius-lg);
  width: 100%;
  max-width: 480px;
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5);
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--gray-400);
}

.modal-close:hover {
  color: var(--gray-600);
}

.modal-close svg {
  width: 18px;
  height: 18px;
}

.modal-body {
  padding: var(--space-5);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--gray-500);
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--gray-900);
}

.detail-value.score {
  font-size: 18px;
  color: var(--primary);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-color);
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
