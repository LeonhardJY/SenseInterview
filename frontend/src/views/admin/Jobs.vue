<template>
  <div class="jobs-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">岗位管理</h2>
        <p class="page-subtitle">共 {{ jobs.length }} 个岗位</p>
      </div>
      <button class="btn btn-primary" @click="showAddDialog">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        新增岗位
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ jobs.length }}</span><span class="stat-label">总岗位数</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon dev"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 18l6-6-6-6M8 6l-6 6 6 6"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ categoryCount }}</span><span class="stat-label">分类数</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ levelCountText }}</span><span class="stat-label">难度分布</span></div>
      </div>
    </div>

    <!-- 岗位列表 -->
    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>岗位名称</th>
            <th>描述</th>
            <th>分类</th>
            <th>难度</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="job in sortedJobs" :key="job.id">
            <td class="cell-id">{{ job.id }}</td>
            <td>
              <span class="job-name">{{ job.name }}</span>
            </td>
            <td class="cell-desc">{{ job.description || '-' }}</td>
            <td><span class="category-tag">{{ job.category || '-' }}</span></td>
            <td><span class="level-tag" :class="levelClass(job.level)">{{ levelText(job.level) }}</span></td>
            <td class="cell-time">{{ formatDate(job.createTime) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn-ghost btn-sm" @click="editJob(job)">编辑</button>
                <button class="btn btn-danger btn-sm" @click="deleteJob(job)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="jobs.length === 0" class="empty-state">
        <p>暂无岗位数据</p>
        <button class="btn btn-primary" @click="showAddDialog">新增第一个岗位</button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ isEdit ? '编辑岗位' : '新增岗位' }}</h3>
              <button class="modal-close" @click="showDialog = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">岗位名称 <span class="required">*</span></label>
                <input type="text" class="form-input" v-model="form.name" placeholder="例如：Java后端开发">
              </div>
              <div class="form-group">
                <label class="form-label">描述</label>
                <textarea class="form-textarea" v-model="form.description" rows="3" placeholder="岗位描述和要求"></textarea>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">分类 <span class="required">*</span></label>
                  <select class="form-select" v-model="form.category">
                    <option value="">请选择分类</option>
                    <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">难度 <span class="required">*</span></label>
                  <select class="form-select" v-model="form.level">
                    <option value="">请选择难度</option>
                    <option value="初级">初级</option>
                    <option value="中级">中级</option>
                    <option value="高级">高级</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="showDialog = false">取消</button>
              <button class="btn btn-primary" @click="saveJob" :disabled="saving || !form.name || !form.category || !form.level">
                {{ saving ? '保存中...' : (isEdit ? '保存修改' : '新增岗位') }}
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
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const jobs = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const categories = ['后端开发', '前端开发', '移动开发', '数据', '测试', '运维', '产品', '设计']

const defaultForm = () => ({
  id: null,
  name: '',
  description: '',
  category: '',
  level: ''
})

const form = ref(defaultForm())

// 按创建时间倒序排列
const sortedJobs = computed(() => {
  return [...jobs.value].sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
})

// 统计：分类数量
const categoryCount = computed(() => {
  const cats = new Set(jobs.value.map(j => j.category).filter(Boolean))
  return cats.size
})

// 统计：难度分布文字
const levelCountText = computed(() => {
  const levels = jobs.value.map(j => j.level).filter(Boolean)
  const primary = levels.filter(l => l === '初级' || l === 'EASY').length
  const medium = levels.filter(l => l === '中级' || l === 'MEDIUM').length
  const hard = levels.filter(l => l === '高级' || l === 'HARD').length
  return `${primary}初/${medium}中/${hard}高`
})

onMounted(() => loadJobs())

const loadJobs = async () => {
  try {
    const res = await api.get('/job/list')
    jobs.value = res.data || []
  } catch (e) { console.error('加载岗位列表失败:', e) }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = defaultForm()
  showDialog.value = true
}

const editJob = (item) => {
  isEdit.value = true
  form.value = {
    id: item.id,
    name: item.name,
    description: item.description || '',
    category: item.category,
    level: item.level
  }
  showDialog.value = true
}

const saveJob = async () => {
  if (!form.value.name || !form.value.category || !form.value.level) {
    ElMessage.warning('请填写完整信息')
    return
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await api.put('/job/update', form.value)
      ElMessage.success('岗位更新成功')
    } else {
      await api.post('/job/create', form.value)
      ElMessage.success('岗位新增成功')
    }
    showDialog.value = false
    loadJobs()
  } catch (e) {
    console.error('保存岗位失败:', e)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const deleteJob = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除岗位「${item.name}」吗？`,
      '确认删除',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
    )
    await api.delete(`/job/${item.id}`)
    ElMessage.success('删除成功')
    loadJobs()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 工具函数（与设计系统复用一致的命名）
const levelText = (l) => {
  const map = { EASY: '初级', MEDIUM: '中级', HARD: '高级', '初级': '初级', '中级': '中级', '高级': '高级' }
  return map[l] || l || '-'
}

const levelClass = (l) => {
  const map = { EASY: 'level-easy', MEDIUM: 'level-medium', HARD: 'level-hard', '初级': 'level-easy', '中级': 'level-medium', '高级': 'level-hard' }
  return map[l] || ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.jobs-page { max-width: 1200px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-5);
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
  padding: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--border-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg { width: 24px; height: 24px; }
.stat-icon.total { background: var(--primary-bg); color: var(--primary); }
.stat-icon.dev { background: var(--success-bg); color: var(--success); }
.stat-icon.active { background: var(--info-bg); color: var(--info); }

.stat-num { display: block; font-size: 24px; font-weight: 600; color: var(--gray-900); }
.stat-label { font-size: 13px; color: var(--gray-500); }
.stat-info { display: flex; flex-direction: column; }

/* 表格 */
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
  white-space: nowrap;
}

.data-table tr:hover td {
  background: var(--gray-50);
}

.cell-id {
  font-family: var(--font-mono);
  color: var(--gray-400);
  font-size: 12px;
}

.job-name {
  font-weight: 500;
  color: var(--gray-900);
}

.cell-desc {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--gray-500);
}

.cell-time {
  color: var(--gray-500);
  white-space: nowrap;
}

.category-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  background: var(--gray-100);
  color: var(--gray-600);
  border-radius: 4px;
}

.level-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
}

.level-easy { color: var(--level-easy); background: var(--level-easy-bg); }
.level-medium { color: var(--level-medium); background: var(--level-medium-bg); }
.level-hard { color: var(--level-hard); background: var(--level-hard-bg); }

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

.btn-sm { padding: 4px 8px; font-size: 11px; }

.btn-primary {
  background: var(--primary);
  color: white;
}

.btn-primary:hover { background: var(--primary-dark); }

.btn-ghost {
  background: transparent;
  color: var(--gray-500);
}

.btn-ghost:hover {
  background: var(--gray-100);
  color: var(--gray-700);
}

.btn-danger {
  background: var(--danger-bg);
  color: var(--danger);
}

.btn-danger:hover {
  background: var(--danger);
  color: white;
}

.btn-outline {
  background: white;
  color: var(--gray-700);
  border: 1px solid var(--gray-200);
}

.btn-outline:hover {
  background: var(--gray-50);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-10);
  color: var(--gray-400);
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
  padding: var(--space-4);
}

.modal-content {
  background: white;
  border-radius: var(--border-radius-lg);
  width: 100%;
  max-width: 520px;
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
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-900);
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gray-400);
  transition: var(--transition);
}

.modal-close:hover { background: var(--gray-100); color: var(--gray-600); }

.modal-close svg { width: 18px; height: 18px; }

.modal-body { padding: var(--space-5); }

.form-group { margin-bottom: var(--space-4); }

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--gray-700);
  margin-bottom: 6px;
}

.required { color: var(--danger); }

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid var(--gray-300);
  border-radius: var(--border-radius-sm);
  background: white;
  color: var(--gray-900);
  transition: var(--transition);
  font-family: var(--font-sans);
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-bg);
}

.form-textarea { resize: vertical; min-height: 80px; }

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.modal-footer {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-color);
  justify-content: flex-end;
}

.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
