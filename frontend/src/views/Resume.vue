<template>
  <div class="resume-page">
    <div class="page-header">
      <div class="page-header-left">
        <button class="btn btn-ghost btn-back" @click="$router.push('/lobby')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回
        </button>
        <div>
          <h1 class="page-title">我的简历</h1>
          <p class="page-subtitle">共 {{ resumeList.length }} 份简历</p>
        </div>
      </div>
      <button class="btn btn-primary" @click="showCreateDialog">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        新建简历
      </button>
    </div>

    <div class="resume-grid">
      <div v-for="item in resumeList" :key="item.id" class="resume-card" :class="{ default: item.status === 2 }">
        <div class="card-header">
          <div class="card-title-row">
            <h3 class="card-title">{{ item.title || '未命名简历' }}</h3>
            <span v-if="item.status === 2" class="default-badge">默认</span>
          </div>
          <div class="card-actions">
            <button class="btn btn-ghost btn-sm" @click="previewResume(item)">预览</button>
            <button class="btn btn-ghost btn-sm" @click="editResume(item)">编辑</button>
            <button class="btn btn-ghost btn-sm" @click="setDefault(item)" v-if="item.status !== 2">设为默认</button>
            <button class="btn btn-danger btn-sm" @click="deleteResume(item.id)">删除</button>
          </div>
        </div>
        <div class="card-body">
          <div class="info-grid">
            <div class="info-item" v-if="item.name">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ item.name }}</span>
            </div>
            <div class="info-item" v-if="item.jobPosition">
              <span class="info-label">岗位</span>
              <span class="info-value">{{ item.jobPosition }}</span>
            </div>
            <div class="info-item" v-if="item.education">
              <span class="info-label">学历</span>
              <span class="info-value">{{ item.education }}</span>
            </div>
            <div class="info-item" v-if="item.workYears !== null">
              <span class="info-label">经验</span>
              <span class="info-value">{{ item.workYears }}年</span>
            </div>
          </div>
          <div class="skills-row" v-if="item.skills">
            <span v-for="skill in item.skills.split(',')" :key="skill" class="skill-tag">{{ skill.trim() }}</span>
          </div>
        </div>
        <div class="card-footer">
          <span class="card-time">{{ formatDate(item.createTime) }}</span>
          <button class="btn btn-primary btn-sm" @click="startInterview(item)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/></svg>
            开始面试
          </button>
        </div>
      </div>

      <div v-if="resumeList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/></svg>
        <p>暂无简历</p>
        <button class="btn btn-primary" @click="showCreateDialog">创建第一份简历</button>
      </div>
    </div>

    <!-- 弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
          <div class="modal-content modal-lg">
            <div class="modal-header">
              <h3>{{ isEdit ? '编辑简历' : '新建简历' }}</h3>
              <button class="modal-close" @click="showDialog = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">简历标题</label>
                <input type="text" class="form-input" v-model="form.title" placeholder="如：Java高级开发简历">
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">姓名</label>
                  <input type="text" class="form-input" v-model="form.name" placeholder="请输入姓名">
                </div>
                <div class="form-group">
                  <label class="form-label">手机号</label>
                  <input type="text" class="form-input" v-model="form.phone" placeholder="请输入手机号">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">邮箱</label>
                  <input type="text" class="form-input" v-model="form.email" placeholder="请输入邮箱">
                </div>
                <div class="form-group">
                  <label class="form-label">求职岗位</label>
                  <input type="text" class="form-input" v-model="form.jobPosition" placeholder="如：Java开发工程师">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">学历</label>
                  <select class="form-select" v-model="form.education">
                    <option value="">请选择</option>
                    <option value="大专">大专</option>
                    <option value="本科">本科</option>
                    <option value="硕士">硕士</option>
                    <option value="博士">博士</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">工作年限</label>
                  <input type="number" class="form-input" v-model="form.workYears" min="0" max="50">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">毕业院校</label>
                  <input type="text" class="form-input" v-model="form.school" placeholder="请输入毕业院校">
                </div>
                <div class="form-group">
                  <label class="form-label">专业</label>
                  <input type="text" class="form-input" v-model="form.major" placeholder="请输入专业">
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">技能标签</label>
                <input type="text" class="form-input" v-model="form.skills" placeholder="多个技能用逗号分隔，如：Java,Spring,MySQL">
              </div>
              <div class="form-group">
                <label class="form-label">工作经历</label>
                <textarea class="form-textarea" v-model="form.experience" rows="3" placeholder="请描述工作经历"></textarea>
              </div>
              <div class="form-group">
                <label class="form-label">自我介绍</label>
                <textarea class="form-textarea" v-model="form.selfIntroduction" rows="2" placeholder="请简要介绍自己"></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="showDialog = false">取消</button>
              <button class="btn btn-primary" @click="saveResume" :disabled="saving">
                {{ isEdit ? '保存修改' : '创建简历' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 预览弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showPreviewDialog" class="modal-overlay" @click.self="showPreviewDialog = false">
          <div class="modal-content modal-lg">
            <div class="modal-header">
              <h3>简历预览</h3>
              <button class="modal-close" @click="showPreviewDialog = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body" v-if="previewItem">
              <div class="preview-section">
                <div class="preview-header">
                  <h2 class="preview-name">{{ previewItem.name || '未填写姓名' }}</h2>
                  <div class="preview-contact">
                    <span v-if="previewItem.phone">{{ previewItem.phone }}</span>
                    <span v-if="previewItem.email">{{ previewItem.email }}</span>
                  </div>
                </div>

                <div class="preview-block" v-if="previewItem.jobPosition">
                  <h4 class="block-title">求职意向</h4>
                  <p>{{ previewItem.jobPosition }}</p>
                </div>

                <div class="preview-block">
                  <h4 class="block-title">基本信息</h4>
                  <div class="info-row">
                    <span v-if="previewItem.education">学历：{{ previewItem.education }}</span>
                    <span v-if="previewItem.school">院校：{{ previewItem.school }}</span>
                    <span v-if="previewItem.major">专业：{{ previewItem.major }}</span>
                    <span v-if="previewItem.workYears !== null">工作年限：{{ previewItem.workYears }}年</span>
                  </div>
                </div>

                <div class="preview-block" v-if="previewItem.skills">
                  <h4 class="block-title">专业技能</h4>
                  <div class="skills-row">
                    <span v-for="skill in previewItem.skills.split(',')" :key="skill" class="skill-tag">{{ skill.trim() }}</span>
                  </div>
                </div>

                <div class="preview-block" v-if="previewItem.experience">
                  <h4 class="block-title">工作经历</h4>
                  <p class="preview-text">{{ previewItem.experience }}</p>
                </div>

                <div class="preview-block" v-if="previewItem.selfIntroduction">
                  <h4 class="block-title">自我介绍</h4>
                  <p class="preview-text">{{ previewItem.selfIntroduction }}</p>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="showPreviewDialog = false">关闭</button>
              <button class="btn btn-primary" @click="startInterview(previewItem)">使用此简历开始面试</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const resumeList = ref([])
const showDialog = ref(false)
const showPreviewDialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const previewItem = ref(null)

const defaultForm = () => ({
  id: null, userId: userStore.userId,
  title: '', name: '', phone: '', email: '', education: '', school: '',
  major: '', workYears: 0, jobPosition: '', skills: '', experience: '', selfIntroduction: ''
})

const form = ref(defaultForm())

onMounted(() => loadResumeList())

const loadResumeList = async () => {
  try {
    const res = await api.get(`/resume/list/${userStore.userId}`)
    resumeList.value = res.data || []
  } catch (e) { console.error(e) }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const showCreateDialog = () => {
  isEdit.value = false
  form.value = defaultForm()
  showDialog.value = true
}

const editResume = (item) => {
  isEdit.value = true
  form.value = { ...item }
  showDialog.value = true
}

const previewResume = (item) => {
  previewItem.value = item
  showPreviewDialog.value = true
}

const saveResume = async () => {
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put('/resume/update', form.value)
      ElMessage.success('简历更新成功')
    } else {
      await api.post('/resume/create', form.value)
      ElMessage.success('简历创建成功')
    }
    showDialog.value = false
    loadResumeList()
  } catch (e) { console.error(e) } finally { saving.value = false }
}

const setDefault = async (item) => {
  try {
    await api.put(`/resume/set-default/${item.id}`)
    ElMessage.success('已设为默认简历')
    loadResumeList()
  } catch (e) { ElMessage.error('设置失败') }
}

const deleteResume = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这份简历吗？', '确认删除', { type: 'warning' })
    await api.delete(`/resume/${id}`)
    ElMessage.success('删除成功')
    loadResumeList()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const startInterview = (item) => {
  // 跳转到面试大厅，并传递简历信息
  router.push({
    path: '/lobby',
    query: {
      resumeId: item.id,
      jobName: item.jobPosition,
      fromResume: true
    }
  })
}
</script>

<style scoped>
.resume-page { max-width: 1200px; margin: 0 auto; }

.resume-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: var(--space-5);
}

.resume-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  overflow: hidden;
  transition: var(--transition);
}

.resume-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.resume-card .card-header {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.resume-card .card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gray-900);
}

.card-actions {
  display: flex;
  gap: var(--space-2);
}

.resume-card .card-body {
  padding: var(--space-5);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 12px;
  color: var(--gray-500);
}

.info-value {
  font-size: 14px;
  color: var(--gray-800);
}

.skills-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.skill-tag {
  font-size: 12px;
  color: var(--gray-600);
  background: var(--gray-100);
  padding: 3px 10px;
  border-radius: 9999px;
}

.resume-card .card-footer {
  padding: var(--space-3) var(--space-5);
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--gray-50);
}

.card-time {
  font-size: 12px;
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
  padding: var(--space-4);
}

.modal-content {
  background: white;
  border-radius: var(--border-radius-lg);
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
}

.modal-lg {
  max-width: 640px;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  background: white;
  z-index: 1;
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

.modal-close:hover {
  background: var(--gray-100);
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

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

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
  position: sticky;
  bottom: 0;
  background: white;
}

.modal-enter-active, .modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-from, .modal-leave-to {
  opacity: 0;
}

/* 默认简历样式 */
.resume-card.default {
  border-color: var(--primary);
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.default-badge {
  font-size: 11px;
  padding: 2px 8px;
  background: var(--primary-bg);
  color: var(--primary);
  border-radius: 4px;
  font-weight: 500;
}

.btn svg {
  width: 14px;
  height: 14px;
}

/* 预览弹窗样式 */
.preview-section {
  padding: var(--space-4);
}

.preview-header {
  text-align: center;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 2px solid var(--primary);
}

.preview-name {
  font-size: 24px;
  font-weight: 600;
  color: var(--gray-900);
  margin: 0 0 var(--space-2) 0;
}

.preview-contact {
  display: flex;
  justify-content: center;
  gap: var(--space-4);
  font-size: 14px;
  color: var(--gray-600);
}

.preview-block {
  margin-bottom: var(--space-5);
}

.block-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--primary);
  margin: 0 0 var(--space-3) 0;
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border-color);
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: 14px;
  color: var(--gray-700);
}

.preview-text {
  font-size: 14px;
  color: var(--gray-700);
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>