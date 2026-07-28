<template>
  <div class="interviews-page">
    <div class="page-header">
      <h2 class="page-title">面试记录</h2>
      <div class="page-stats">
        共 {{ interviews.length }} 条记录
      </div>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <select v-model="filterStatus" class="filter-select">
        <option value="">全部状态</option>
        <option value="COMPLETED">已完成</option>
        <option value="IN_PROGRESS">进行中</option>
        <option value="CREATED">待开始</option>
      </select>
      <select v-model="filterMode" class="filter-select">
        <option value="">全部模式</option>
        <option value="TEXT">文字面试</option>
        <option value="VOICE">语音面试</option>
        <option value="VIDEO">视频面试</option>
      </select>
    </div>

    <!-- 面试列表 -->
    <div class="table-card">
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
              <span class="status-tag" :class="item.status?.toLowerCase()">
                {{ statusText(item.status) }}
              </span>
            </td>
            <td>{{ formatDate(item.startTime) }}</td>
            <td>{{ formatDate(item.endTime) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn-sm btn-primary" @click="viewReport(item)" v-if="item.status === 'COMPLETED'">
                  查看报告
                </button>
                <button class="btn btn-sm btn-danger" @click="deleteInterview(item)">
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
    if (filterStatus.value && item.status !== filterStatus.value) return false
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
  const map = { CREATED: '待开始', IN_PROGRESS: '进行中', COMPLETED: '已完成' }
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
  align-items: center;
  margin-bottom: var(--space-5);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--gray-900);
}

.page-stats {
  font-size: 13px;
  color: var(--gray-500);
}

.filter-bar {
  display: flex;
  align-items: center;
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

.table-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
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
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
}

.data-table th {
  background: var(--gray-50);
  font-weight: 600;
  color: var(--gray-700);
}

.data-table tr:hover {
  background: var(--gray-50);
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
}

.status-tag.created {
  background: var(--gray-100);
  color: var(--gray-600);
}

.status-tag.in_progress {
  background: var(--primary-bg);
  color: var(--primary);
}

.status-tag.completed {
  background: var(--success-bg);
  color: var(--success);
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-size: 12px;
  border: none;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: var(--transition);
}

.btn-sm {
  padding: 4px 8px;
  font-size: 11px;
}

.btn-primary {
  background: var(--primary-bg);
  color: var(--primary);
}

.btn-primary:hover {
  background: var(--primary);
  color: white;
}

.btn-danger {
  background: var(--danger-bg);
  color: var(--danger);
}

.btn-danger:hover {
  background: var(--danger);
  color: white;
}

.empty-state {
  text-align: center;
  padding: var(--space-8);
  color: var(--gray-400);
}
</style>
