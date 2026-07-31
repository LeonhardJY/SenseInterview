<template>
  <div class="questions-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Question Bank</p>
        <h2 class="page-title">题库管理</h2>
      </div>
      <button class="btn btn--primary" @click="showAddModal = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        添加题目
      </button>
    </header>

    <!-- 筛选 -->
    <div class="filter-bar">
      <select v-model="filterCategory" class="form-select filter-select">
        <option value="">全部分类</option>
        <option value="后端开发">后端开发</option>
        <option value="前端开发">前端开发</option>
        <option value="移动开发">移动开发</option>
        <option value="数据">数据</option>
        <option value="测试">测试</option>
        <option value="运维">运维</option>
        <option value="产品">产品</option>
        <option value="设计">设计</option>
      </select>
      <select v-model="filterLevel" class="form-select filter-select">
        <option value="">全部难度</option>
        <option value="初级">初级</option>
        <option value="中级">中级</option>
        <option value="高级">高级</option>
      </select>
      <span class="filter-count text-muted text-sm">共 {{ filteredQuestions.length }} 题</span>
    </div>

    <!-- 题目列表 -->
    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>题目</th>
            <th>分类</th>
            <th>难度</th>
            <th>参考答案</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="q in filteredQuestions" :key="q.id">
            <td>{{ q.id }}</td>
            <td class="title-cell">{{ q.title }}</td>
            <td>{{ q.category || '-' }}</td>
            <td>
              <span class="tag" :class="{ 'tag--easy': q.level === '初级', 'tag--medium': q.level === '中级', 'tag--hard': q.level === '高级' }">{{ q.level || '-' }}</span>
            </td>
            <td class="answer-cell">{{ q.answer ? (q.answer.substring(0, 50) + '...') : '-' }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn--sm btn--primary" @click="editQuestion(q)">编辑</button>
                <button class="btn btn--sm btn--danger" @click="deleteQuestion(q)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredQuestions.length === 0" class="empty-state">
        <p>暂无题目数据</p>
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
          <div class="card modal-content">
            <div class="modal-header">
              <h3>{{ editingId ? '编辑题目' : '添加题目' }}</h3>
              <button class="btn btn--ghost btn--icon" @click="closeModal" style="font-size:18px">✕</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">题目标题 *</label>
                <textarea v-model="form.title" class="form-textarea" rows="3" placeholder="请输入题目标题"></textarea>
              </div>
              <div class="form-row form-row--2">
                <div class="form-group">
                  <label class="form-label">分类</label>
                  <select v-model="form.category" class="form-select">
                    <option value="后端开发">后端开发</option>
                    <option value="前端开发">前端开发</option>
                    <option value="移动开发">移动开发</option>
                    <option value="数据">数据</option>
                    <option value="测试">测试</option>
                    <option value="运维">运维</option>
                    <option value="产品">产品</option>
                    <option value="设计">设计</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">难度</label>
                  <select v-model="form.level" class="form-select">
                    <option value="初级">初级</option>
                    <option value="中级">中级</option>
                    <option value="高级">高级</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">参考答案</label>
                <textarea v-model="form.answer" class="form-textarea" rows="5" placeholder="请输入参考答案"></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn--ghost" @click="closeModal">取消</button>
              <button class="btn btn--primary" @click="saveQuestion">保存</button>
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

const questions = ref([])
const filterCategory = ref('')
const filterLevel = ref('')
const showAddModal = ref(false)
const editingId = ref(null)
const form = ref({
  title: '',
  category: '后端开发',
  level: '初级',
  answer: ''
})

const filteredQuestions = computed(() => {
  return questions.value.filter(q => {
    if (filterCategory.value && q.category !== filterCategory.value) return false
    if (filterLevel.value && q.level !== filterLevel.value) return false
    return true
  })
})

onMounted(() => {
  loadQuestions()
})

const loadQuestions = async () => {
  try {
    const res = await api.get('/question/list')
    questions.value = res.data || []
  } catch (e) { console.error(e) }
}

const editQuestion = (q) => {
  editingId.value = q.id
  form.value = {
    title: q.title,
    category: q.category || '后端开发',
    level: q.level || '初级',
    answer: q.answer || ''
  }
  showAddModal.value = true
}

const closeModal = () => {
  showAddModal.value = false
  editingId.value = null
  form.value = { title: '', category: '后端开发', level: '初级', answer: '' }
}

const saveQuestion = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入题目标题')
    return
  }

  try {
    if (editingId.value) {
      await api.put('/question/update', {
        id: editingId.value,
        ...form.value
      })
      ElMessage.success('更新成功')
    } else {
      await api.post('/question/add', form.value)
      ElMessage.success('添加成功')
    }
    closeModal()
    loadQuestions()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const deleteQuestion = async (q) => {
  try {
    await ElMessageBox.confirm(`确定要删除题目吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })

    await api.delete(`/question/${q.id}`)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.questions-page {
  max-width: 1200px;
}

/* ── 页头标题区 ── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-5);
}

.page-title {
  font-size: 28px;
}

/* ── 筛选 ── */
.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-4);
}

.filter-select {
  width: auto;
  min-width: 140px;
}

.filter-count {
  margin-left: auto;
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
}

.data-table tbody tr:hover {
  background: var(--color-surface-subtle);
}

.title-cell {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.answer-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text-secondary);
}

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
  max-width: 560px;
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
