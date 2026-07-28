<template>
  <div class="questions-page">
    <div class="page-header">
      <h2 class="page-title">题库管理</h2>
      <button class="btn btn-primary" @click="showAddModal = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        添加题目
      </button>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <select v-model="filterCategory" class="filter-select">
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
      <select v-model="filterLevel" class="filter-select">
        <option value="">全部难度</option>
        <option value="初级">初级</option>
        <option value="中级">中级</option>
        <option value="高级">高级</option>
      </select>
      <span class="filter-count">共 {{ filteredQuestions.length }} 题</span>
    </div>

    <!-- 题目列表 -->
    <div class="table-card">
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
              <span class="level-tag" :class="q.level">{{ q.level || '-' }}</span>
            </td>
            <td class="answer-cell">{{ q.answer ? (q.answer.substring(0, 50) + '...') : '-' }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn-sm btn-primary" @click="editQuestion(q)">编辑</button>
                <button class="btn btn-sm btn-danger" @click="deleteQuestion(q)">删除</button>
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
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ editingId ? '编辑题目' : '添加题目' }}</h3>
              <button class="modal-close" @click="closeModal">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">题目标题 *</label>
                <textarea v-model="form.title" class="form-textarea" rows="3" placeholder="请输入题目标题"></textarea>
              </div>
              <div class="form-row">
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
              <button class="btn btn-outline" @click="closeModal">取消</button>
              <button class="btn btn-primary" @click="saveQuestion">保存</button>
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

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 13px;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: var(--transition);
}

.btn svg {
  width: 16px;
  height: 16px;
}

.btn-primary {
  background: var(--primary);
  color: white;
}

.btn-primary:hover {
  background: var(--primary-dark);
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

.filter-count {
  font-size: 13px;
  color: var(--gray-500);
  margin-left: auto;
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
  color: var(--gray-500);
}

.level-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
}

.level-tag.初级 {
  background: var(--success-bg);
  color: var(--success);
}

.level-tag.中级 {
  background: var(--warning-bg);
  color: var(--warning);
}

.level-tag.高级 {
  background: var(--danger-bg);
  color: var(--danger);
}

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 11px;
}

.btn-outline {
  background: white;
  color: var(--gray-700);
  border: 1px solid var(--gray-200);
}

.btn-outline:hover {
  background: var(--gray-50);
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
  max-width: 560px;
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

.form-group {
  margin-bottom: var(--space-4);
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--gray-700);
  margin-bottom: 6px;
}

.form-textarea {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  border: 1px solid var(--gray-200);
  border-radius: var(--border-radius-sm);
  resize: vertical;
}

.form-textarea:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-bg);
}

.form-select {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  font-size: 13px;
  border: 1px solid var(--gray-200);
  border-radius: var(--border-radius-sm);
  background: white;
  cursor: pointer;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
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
