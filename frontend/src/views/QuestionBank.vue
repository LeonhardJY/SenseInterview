<template>
  <div class="qb-page">
    <div class="page-header">
      <div class="page-header-left">
        <button class="btn btn-ghost btn-back" @click="$router.push('/lobby')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回
        </button>
        <div>
          <h1 class="page-title">面试题库</h1>
          <p class="page-subtitle">共 {{ questions.length }} 道题目</p>
        </div>
      </div>
      <div class="header-actions">
        <select class="form-select-sm" v-model="filterCategory" @change="loadQuestions">
          <option value="">全部分类</option>
          <option value="Java">Java</option>
          <option value="前端">前端</option>
          <option value="Python">Python</option>
          <option value="数据库">数据库</option>
          <option value="算法">算法</option>
          <option value="产品">产品</option>
          <option value="测试">测试</option>
        </select>
        <select class="form-select-sm" v-model="filterLevel" @change="loadQuestions">
          <option value="">全部难度</option>
          <option value="EASY">简单</option>
          <option value="MEDIUM">中等</option>
          <option value="HARD">困难</option>
        </select>
        <button class="btn btn-primary" @click="showAddDialog">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          添加题目
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon total"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ questions.length }}</span><span class="stat-label">总题数</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon easy"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ easyCount }}</span><span class="stat-label">简单</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon medium"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ mediumCount }}</span><span class="stat-label">中等</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon hard"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/><path d="M12 9v4M12 17h.01"/></svg></div>
        <div class="stat-info"><span class="stat-num">{{ hardCount }}</span><span class="stat-label">困难</span></div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <div v-for="item in questions" :key="item.id" class="question-card">
        <div class="question-header">
          <div class="question-meta">
            <span class="question-id">#{{ item.id }}</span>
            <span class="level-tag" :class="levelClass(item.level)">{{ levelText(item.level) }}</span>
            <span class="category-tag">{{ item.category }}</span>
          </div>
          <div class="question-actions">
            <button class="btn btn-ghost btn-sm" @click="editQuestion(item)">编辑</button>
            <button class="btn btn-danger btn-sm" @click="deleteQuestion(item.id)">删除</button>
          </div>
        </div>
        <p class="question-text">{{ item.title }}</p>
        <div v-if="expandedId === item.id" class="question-answer">
          <div class="answer-label">参考答案</div>
          <p>{{ item.answer || '暂无参考答案' }}</p>
        </div>
        <button class="expand-btn" @click="toggleExpand(item.id)">
          {{ expandedId === item.id ? '收起答案' : '查看答案' }}
          <svg :class="{ expanded: expandedId === item.id }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
        </button>
      </div>

      <div v-if="questions.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
        <p>暂无题目</p>
        <button class="btn btn-primary" @click="showAddDialog">添加第一道题目</button>
      </div>
    </div>

    <!-- 弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>{{ isEdit ? '编辑题目' : '添加题目' }}</h3>
              <button class="modal-close" @click="showDialog = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">题目内容</label>
                <textarea class="form-textarea" v-model="form.title" rows="3" placeholder="请输入题目内容"></textarea>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">分类</label>
                  <select class="form-select" v-model="form.category">
                    <option value="">请选择分类</option>
                    <option value="Java">Java</option>
                    <option value="前端">前端</option>
                    <option value="Python">Python</option>
                    <option value="数据库">数据库</option>
                    <option value="算法">算法</option>
                    <option value="产品">产品</option>
                    <option value="测试">测试</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">难度</label>
                  <select class="form-select" v-model="form.level">
                    <option value="">请选择难度</option>
                    <option value="EASY">简单</option>
                    <option value="MEDIUM">中等</option>
                    <option value="HARD">困难</option>
                  </select>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">参考答案</label>
                <textarea class="form-textarea" v-model="form.answer" rows="4" placeholder="请输入参考答案（可选）"></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="showDialog = false">取消</button>
              <button class="btn btn-primary" @click="saveQuestion" :disabled="saving">
                {{ isEdit ? '保存修改' : '添加题目' }}
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

const questions = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const expandedId = ref(null)
const filterCategory = ref('')
const filterLevel = ref('')

const form = ref({ id: null, title: '', category: '', level: '', answer: '' })

const easyCount = computed(() => questions.value.filter(q => q.level === 'EASY').length)
const mediumCount = computed(() => questions.value.filter(q => q.level === 'MEDIUM').length)
const hardCount = computed(() => questions.value.filter(q => q.level === 'HARD').length)

onMounted(() => loadQuestions())

const loadQuestions = async () => {
  try {
    let url = '/question/list'
    if (filterCategory.value) url = `/question/category/${filterCategory.value}`
    else if (filterLevel.value) url = `/question/level/${filterLevel.value}`
    const res = await api.get(url)
    questions.value = res.data || []
  } catch (e) { console.error(e) }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = { id: null, title: '', category: '', level: '', answer: '' }
  showDialog.value = true
}

const editQuestion = (item) => { isEdit.value = true; form.value = { ...item }; showDialog.value = true }

const saveQuestion = async () => {
  saving.value = true
  try {
    if (isEdit.value) { await api.put('/question/update', form.value); ElMessage.success('题目更新成功') }
    else { await api.post('/question/add', form.value); ElMessage.success('题目添加成功') }
    showDialog.value = false; loadQuestions()
  } catch (e) { console.error(e) } finally { saving.value = false }
}

const deleteQuestion = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这道题目吗？', '确认删除', { type: 'warning' })
    await api.delete(`/question/${id}`); ElMessage.success('删除成功'); loadQuestions()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const toggleExpand = (id) => { expandedId.value = expandedId.value === id ? null : id }
const levelText = (l) => ({ EASY: '简单', MEDIUM: '中等', HARD: '困难' }[l] || l)
const levelClass = (l) => ({ EASY: 'easy', MEDIUM: 'medium', HARD: 'hard' }[l] || '')
</script>

<style scoped>
.qb-page { max-width: 1000px; margin: 0 auto; }

.header-actions { display: flex; gap: var(--space-3); align-items: center; }

.form-select-sm {
  height: 36px; padding: 0 28px 0 12px; font-size: 13px;
  border: 1px solid var(--gray-200); border-radius: var(--border-radius-sm);
  background: white; color: var(--gray-700); cursor: pointer;
}

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-4); margin-bottom: var(--space-6); }

.stat-card {
  background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius);
  padding: var(--space-5); display: flex; align-items: center; gap: var(--space-4);
}

.stat-icon { width: 48px; height: 48px; border-radius: var(--border-radius); display: flex; align-items: center; justify-content: center; }
.stat-icon svg { width: 24px; height: 24px; }
.stat-icon.total { background: var(--primary-bg); color: var(--primary); }
.stat-icon.easy { background: var(--level-easy-bg); color: var(--level-easy); }
.stat-icon.medium { background: var(--level-medium-bg); color: var(--level-medium); }
.stat-icon.hard { background: var(--level-hard-bg); color: var(--level-hard); }
.stat-num { display: block; font-size: 24px; font-weight: 600; color: var(--gray-900); }
.stat-label { font-size: 13px; color: var(--gray-500); }

/* 题目列表 */
.question-list { display: flex; flex-direction: column; gap: var(--space-3); }

.question-card {
  background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius);
  padding: var(--space-5); transition: var(--transition);
}

.question-card:hover { border-color: var(--gray-300); }

.question-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-3); }
.question-meta { display: flex; align-items: center; gap: var(--space-2); }
.question-id { font-size: 12px; color: var(--gray-400); font-family: var(--font-mono); }
.category-tag { font-size: 12px; color: var(--gray-500); background: var(--gray-100); padding: 2px 10px; border-radius: 4px; }
.level-tag { font-size: 12px; font-weight: 500; padding: 2px 10px; border-radius: 4px; }
.level-easy { color: var(--level-easy); background: var(--level-easy-bg); }
.level-medium { color: var(--level-medium); background: var(--level-medium-bg); }
.level-hard { color: var(--level-hard); background: var(--level-hard-bg); }
.question-actions { display: flex; gap: var(--space-2); }
.question-text { font-size: 15px; color: var(--gray-800); line-height: 1.6; margin-bottom: var(--space-3); }

.question-answer { background: var(--gray-50); border-radius: var(--border-radius-sm); padding: var(--space-4); margin-bottom: var(--space-3); }
.answer-label { font-size: 12px; font-weight: 600; color: var(--gray-500); margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
.question-answer p { font-size: 14px; color: var(--gray-600); line-height: 1.6; }

.expand-btn { display: flex; align-items: center; gap: 4px; background: none; border: none; font-size: 13px; color: var(--primary); cursor: pointer; padding: 0; }
.expand-btn svg { width: 14px; height: 14px; transition: transform 0.2s; }
.expand-btn svg.expanded { transform: rotate(180deg); }

/* 弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: var(--space-4); }
.modal-content { background: white; border-radius: var(--border-radius-lg); width: 100%; max-width: 560px; box-shadow: var(--shadow-lg); }
.modal-header { display: flex; align-items: center; justify-content: space-between; padding: var(--space-5); border-bottom: 1px solid var(--border-color); }
.modal-header h3 { font-size: 18px; font-weight: 600; color: var(--gray-900); }
.modal-close { width: 32px; height: 32px; border: none; background: none; border-radius: var(--border-radius-sm); cursor: pointer; display: flex; align-items: center; justify-content: center; color: var(--gray-400); transition: var(--transition); }
.modal-close:hover { background: var(--gray-100); color: var(--gray-600); }
.modal-close svg { width: 18px; height: 18px; }
.modal-body { padding: var(--space-5); }
.form-group { margin-bottom: var(--space-4); }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--gray-700); margin-bottom: 6px; }
.form-input, .form-select, .form-textarea { width: 100%; padding: 10px 12px; font-size: 14px; border: 1px solid var(--gray-300); border-radius: var(--border-radius-sm); background: white; color: var(--gray-900); transition: var(--transition); font-family: var(--font-sans); }
.form-input:focus, .form-select:focus, .form-textarea:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-bg); }
.form-textarea { resize: vertical; min-height: 80px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); }
.modal-footer { display: flex; gap: var(--space-3); padding: var(--space-4) var(--space-5); border-top: 1px solid var(--border-color); justify-content: flex-end; }
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>