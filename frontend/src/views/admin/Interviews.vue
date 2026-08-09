<template>
  <div class="interviews-page fade-in">
    <div class="page-header">
      <div>
        <p class="eyebrow">Admin · 面试</p>
        <h2>面试记录</h2>
      </div>
      <p class="page-stats text-sm text-muted">共 {{ interviews.length }} 条记录</p>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <select v-model="filterStatus" class="filter-select form-select">
        <option value="">全部状态</option>
        <option value="DONE">已完成</option>
        <option value="IN_PROGRESS">进行中</option>
        <option value="CREATED">待开始</option>
      </select>
      <select v-model="filterMode" class="filter-select form-select">
        <option value="">全部模式</option>
        <option value="TEXT">文字面试</option>
        <option value="VOICE">语音面试</option>
        <option value="VIDEO">视频面试</option>
      </select>
    </div>

    <!-- 面试列表 -->
    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户ID</th>
            <th>岗位</th>
            <th>模式</th>
            <th>难度</th>
            <th>状态</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredInterviews" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.userId }}</td>
            <td>{{ item.jobName || '-' }}</td>
            <td>{{ modeText(item.mode) }}</td>
            <td>{{ item.difficulty || '-' }}</td>
            <td>
              <span class="status status-tag" :class="item.status?.toLowerCase()">
                {{ statusText(item.status) }}
              </span>
            </td>
            <td>{{ formatDate(item.startTime) }}</td>
            <td>{{ formatDate(item.endTime) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn--sm btn--primary" @click="viewReport(item)" v-if="item.status === 'COMPLETED' || item.status === 'FINISHED'">
                  查看报告
                </button>
                <button class="btn btn--sm btn--danger" @click="deleteInterview(item)">
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredInterviews.length === 0" class="empty-state">
        <p>暂无面试记录</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const router = useRouter()
const interviews = ref([])
const filterStatus = ref('')
const filterMode = ref('')

const filteredInterviews = computed(() => {
  return interviews.value.filter(item => {
    if (filterStatus.value === 'DONE' && item.status !== 'COMPLETED' && item.status !== 'FINISHED') return false
    if (filterStatus.value && filterStatus.value !== 'DONE' && item.status !== filterStatus.value) return false
    if (filterMode.value && item.mode !== filterMode.value) return false
    return true
  })
})

onMounted(() => {
  loadInterviews()
})

const loadInterviews = async () => {
  try {
    const res = await api.get('/interview/list')
    interviews.value = res.data || []
  } catch (e) { console.error(e) }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const modeText = (mode) => {
  const map = { TEXT: '文字', VOICE: '语音', VIDEO: '视频' }
  return map[mode] || mode || '-'
}

const statusText = (status) => {
  const map = { CREATED: '待开始', IN_PROGRESS: '进行中', COMPLETED: '已完成', FINISHED: '已完成' }
  return map[status] || status || '-'
}

const viewReport = (item) => {
  router.push(`/report/${item.id}`)
}

const deleteInterview = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这条面试记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    await api.delete(`/interview/${item.id}`)
    ElMessage.success('删除成功')
    loadInterviews()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.interviews-page {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--spacing-5);
}

.page-header .eyebrow {
  margin-bottom: 2px;
}

.page-header h2 {
  font-size: 24px;
  margin: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
}

.filter-bar .filter-select {
  width: 170px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--color-divider);
  font-size: 13px;
}

.data-table th {
  background: var(--color-surface-subtle);
  font-weight: 600;
  color: var(--color-text-body);
}

.data-table tbody tr:last-child td {
  border-bottom: none;
}

.data-table tbody tr:hover {
  background: var(--color-surface-hover);
}

.status-tag.created {
  background: #FEF3C7;
  color: #B45309;
}

.status-tag.in_progress {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.status-tag.completed,
.status-tag.finished {
  background: var(--color-success-light);
  color: var(--color-success);
}

.action-btns {
  display: flex;
  gap: var(--spacing-2);
}

.btn--danger {
  background: var(--color-danger-light);
  border-color: transparent;
  color: var(--color-danger);
}

.btn--danger:hover {
  background: var(--color-danger);
  color: white;
}
</style>
