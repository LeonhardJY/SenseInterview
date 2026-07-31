<template>
  <div class="jobs-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Jobs</p>
        <h2 class="page-title">岗位管理</h2>
        <p class="page-subtitle text-muted text-sm">共 {{ jobs.length }} 个岗位</p>
      </div>
      <button class="btn btn--primary" @click="showAddDialog">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        新增岗位
      </button>
    </header>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="card stat-card">
        <div class="stat-icon total"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ jobs.length }}</span><span class="stat-label">总岗位数</span></div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon dev"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 18l6-6-6-6M8 6l-6 6 6 6"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ categoryCount }}</span><span class="stat-label">分类数</span></div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon active"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ levelCountText }}</span><span class="stat-label">难度分布</span></div>
      </div>
    </div>

    <!-- 岗位列表 -->
    <div class="card table-card">
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
            <td><span class="tag" :class="levelClass(job.level)">{{ levelText(job.level) }}</span></td>
            <td class="cell-time">{{ formatDate(job.createTime) }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn--sm btn--ghost" @click="editJob(job)">编辑</button>
                <button class="btn btn--sm btn--danger" @click="deleteJob(job)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="jobs.length === 0" class="empty-state">
        <p>暂无岗位数据</p>
        <button class="btn btn--primary" @click="showAddDialog">新增第一个岗位</button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
          <div class="card modal-content">
            <div class="modal-header">
              <h3>{{ isEdit ? '编辑岗位' : '新增岗位' }}</h3>
              <button class="btn btn--ghost btn--icon" @click="showDialog = false" style="font-size:18px">✕</button>
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
              <div class="form-row form-row--2">
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
              <button class="btn btn--ghost" @click="showDialog = false">取消</button>
              <button class="btn btn--primary" @click="saveJob" :disabled="saving || !form.name || !form.category || !form.level">
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
.jobs-page {
  max-width: 1200px;
}

/* ── 页头标题区 ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-5);
}

.page-title {
  font-size: 28px;
}

.page-subtitle {
  margin: var(--spacing-1) 0 0 0;
}

/* ── 统计卡片（外层复用 .card） ── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-4);
  margin-bottom: var(--spacing-5);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-4);
  padding: var(--spacing-5);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-icon.total { background: var(--color-accent-light); color: var(--color-accent); }
.stat-icon.dev { background: var(--color-success-light); color: var(--color-success); }
.stat-icon.active { background: var(--color-surface-subtle); color: var(--color-accent); }

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

/* ── 表格（外层复用 .card，去掉内边距让表格铺满） ── */
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
  white-space: nowrap;
}

.data-table tbody tr:hover td {
  background: var(--color-surface-subtle);
}

.cell-id {
  color: var(--color-text-placeholder);
  font-size: 12px;
}

.job-name {
  font-weight: 500;
  color: var(--color-text-primary);
}

.cell-desc {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text-secondary);
}

.cell-time {
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.category-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  background: var(--color-surface-subtle);
  color: var(--color-text-secondary);
  border-radius: 999px;
}

/* 难度标签：复用 .tag 基础胶囊样式，这里只补颜色 */
.level-easy { background: var(--color-success-light); color: var(--color-success); }
.level-medium { background: #FEF3C7; color: #B45309; }
.level-hard { background: var(--color-danger-light); color: var(--color-danger); }

.action-btns {
  display: flex;
  gap: var(--spacing-2);
}

/* 按钮内 svg 尺寸 */
.btn svg {
  width: 16px;
  height: 16px;
}

/* 危险按钮（design-system 未提供，对齐色板自建） */
.btn--danger {
  background: var(--color-danger-light);
  border-color: transparent;
  color: var(--color-danger);
}

.btn--danger:hover {
  background: var(--color-danger);
  border-color: var(--color-danger);
  color: #fff;
}

/* ── 弹窗（内容复用 .card，header/body/footer 自带内边距） ── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 25, 23, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: var(--spacing-4);
}

.modal-content {
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 0;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-5) var(--spacing-6);
  border-bottom: 1px solid var(--color-divider);
  position: sticky;
  top: 0;
  z-index: 1;
  background: var(--color-surface);
}

.modal-body {
  padding: var(--spacing-6);
}

.form-row--2 {
  grid-template-columns: repeat(2, 1fr);
}

@media (max-width: 640px) {
  .form-row--2 { grid-template-columns: 1fr; gap: 0; }
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: var(--spacing-4) var(--spacing-6);
  border-top: 1px solid var(--color-divider);
  position: sticky;
  bottom: 0;
  z-index: 1;
  background: var(--color-surface);
}

.required {
  color: var(--color-danger);
}

/* ── 动画 ── */
.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--transition-base);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
