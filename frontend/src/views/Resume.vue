<template>
  <div class="resume-page">
    <!-- 页头 hero 色块 -->
    <div class="resume-hero">
      <div class="resume-hero-inner">
        <button class="btn btn--ghost hero-back" @click="$router.push('/lobby')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回大厅
        </button>
        <p class="hero-eyebrow">MY RESUMES</p>
        <h1 class="hero-title">我的简历</h1>
        <p class="hero-sub">管理你的简历，选择一份开始模拟面试</p>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-num">{{ resumeList.length }}</span>
            <span class="hero-stat-lbl">份简历</span>
          </div>
          <div class="hero-stat-div"></div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ defaultCount }}</span>
            <span class="hero-stat-lbl">默认</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 简历列表 -->
    <div class="resume-body">
      <div class="resume-toolbar">
        <p class="text-muted text-sm">共 {{ resumeList.length }} 份简历，设为默认的简历将用于面试</p>
        <button class="btn btn--primary" @click="showCreateDialog">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          新建简历
        </button>
      </div>

      <div class="resume-grid">
        <div v-for="item in resumeList" :key="item.id" class="card resume-card" :class="{ 'resume-card--default': item.status === 2 }">
          <div class="resume-card-top" v-if="item.status === 2">
            <span class="default-badge">★ 默认简历</span>
          </div>
          <div class="resume-card-header">
            <div class="resume-card-title-row">
              <h3 class="resume-card-title">{{ item.title || '未命名简历' }}</h3>
            </div>
            <div class="resume-card-actions">
              <button class="btn btn--ghost btn--sm" @click="previewResume(item)">预览</button>
              <button class="btn btn--ghost btn--sm" @click="editResume(item)">编辑</button>
              <button class="btn btn--ghost btn--sm" @click="setDefault(item)" v-if="item.status !== 2">设为默认</button>
              <button class="btn btn--danger btn--sm" @click="deleteResume(item.id)">删除</button>
            </div>
          </div>
          <div class="resume-card-body">
            <div class="info-grid">
              <div class="info-item" v-if="item.name">
                <span class="info-label">姓名</span>
                <span class="info-value">{{ item.name }}</span>
              </div>
              <div class="info-item" v-if="item.jobPosition">
                <span class="info-label">求职岗位</span>
                <span class="info-value">{{ item.jobPosition }}</span>
              </div>
              <div class="info-item" v-if="item.education">
                <span class="info-label">学历</span>
                <span class="info-value">{{ item.education }}</span>
              </div>
              <div class="info-item" v-if="item.workYears !== null">
                <span class="info-label">工作年限</span>
                <span class="info-value">{{ item.workYears }}年</span>
              </div>
            </div>
            <div class="skills-row" v-if="item.skills">
              <span v-for="skill in item.skills.split(',')" :key="skill" class="tag skill-tag">{{ skill.trim() }}</span>
            </div>
          </div>
          <div class="resume-card-footer">
            <span class="card-time text-xs text-muted">创建于 {{ formatDate(item.createTime) }}</span>
            <button class="btn btn--primary btn--sm" @click="startInterview(item)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 1a3 3 0 00-3 3v8a3 3 0 006 0V4a3 3 0 00-3-3z"/></svg>
              开始面试
            </button>
          </div>
        </div>

        <div v-if="resumeList.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/></svg>
          <p>还没有简历，创建第一份开始面试吧</p>
          <button class="btn btn--primary" @click="showCreateDialog">创建第一份简历</button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑弹窗 -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
          <div class="card modal-content">
            <div class="modal-header">
              <h4>{{ isEdit ? '编辑简历' : '新建简历' }}</h4>
              <button class="btn btn--ghost btn--icon" @click="showDialog = false" style="font-size:18px">✕</button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">简历标题</label>
                <input type="text" class="form-input" v-model="form.title" placeholder="如：Java高级开发简历">
              </div>
              <div class="form-row form-row--2">
                <div class="form-group">
                  <label class="form-label">姓名</label>
                  <input type="text" class="form-input" v-model="form.name" placeholder="请输入姓名">
                </div>
                <div class="form-group">
                  <label class="form-label">手机号</label>
                  <input type="text" class="form-input" v-model="form.phone" placeholder="请输入手机号">
                </div>
              </div>
              <div class="form-row form-row--2">
                <div class="form-group">
                  <label class="form-label">邮箱</label>
                  <input type="text" class="form-input" v-model="form.email" placeholder="请输入邮箱">
                </div>
                <div class="form-group">
                  <label class="form-label">求职岗位</label>
                  <input type="text" class="form-input" v-model="form.jobPosition" placeholder="如：Java开发工程师">
                </div>
              </div>
              <div class="form-row form-row--2">
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
              <div class="form-row form-row--2">
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
              <button class="btn btn--ghost" @click="showDialog = false">取消</button>
              <button class="btn btn--primary" @click="saveResume" :disabled="saving">
                {{ isEdit ? '保存修改' : '创建简历' }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- 预览弹窗 -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showPreviewDialog" class="modal-overlay" @click.self="showPreviewDialog = false">
          <div class="card modal-content modal-lg">
            <div class="modal-header">
              <h4>简历预览</h4>
              <button class="btn btn--ghost btn--icon" @click="showPreviewDialog = false" style="font-size:18px">✕</button>
            </div>
            <div class="modal-body" v-if="previewItem">
              <div class="preview-sheet">
                <div class="preview-header">
                  <h2 class="preview-name">{{ previewItem.name || '未填写姓名' }}</h2>
                  <p class="preview-job">{{ previewItem.jobPosition || '求职意向未填写' }}</p>
                  <div class="preview-contact">
                    <span v-if="previewItem.phone">☎ {{ previewItem.phone }}</span>
                    <span v-if="previewItem.email">✉ {{ previewItem.email }}</span>
                  </div>
                </div>

                <div class="preview-block" v-if="previewItem.education || previewItem.school || previewItem.major">
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
                    <span v-for="skill in previewItem.skills.split(',')" :key="skill" class="tag skill-tag">{{ skill.trim() }}</span>
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
              <button class="btn btn--ghost" @click="showPreviewDialog = false">关闭</button>
              <button class="btn btn--primary" @click="startInterview(previewItem)">使用此简历开始面试</button>
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

const defaultCount = computed(() => resumeList.value.filter(i => i.status === 2).length)

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
.resume-page { max-width: 960px; margin: 0 auto; padding: 0 var(--spacing-4); }

/* ── 页头 hero 色块 ── */
.resume-hero {
  background: linear-gradient(135deg, var(--color-accent), #E8A87C);
  border-radius: var(--radius-lg);
  padding: var(--spacing-8) var(--spacing-8) var(--spacing-7);
  color: var(--color-accent-text);
  margin: var(--spacing-6) 0 var(--spacing-6);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
}
.resume-hero::after {
  content: '';
  position: absolute;
  right: -40px; top: -40px;
  width: 180px; height: 180px;
  border-radius: 50%;
  background: rgba(255,255,255,0.12);
}
.resume-hero::before {
  content: '';
  position: absolute;
  right: 40px; bottom: -60px;
  width: 120px; height: 120px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
}
.resume-hero-inner { position: relative; z-index: 1; }

.hero-back {
  color: rgba(255,255,255,0.9); padding: 6px 12px; margin-bottom: var(--spacing-5); font-size: 13px;
}
.hero-back:hover { background: rgba(255,255,255,0.15); color: #fff; }
.hero-back svg { width: 16px; height: 16px; }

.hero-eyebrow {
  font-family: var(--font-display); font-size: 14px; font-style: italic;
  letter-spacing: 0.14em; opacity: 0.85; margin-bottom: var(--spacing-2);
}
.hero-title {
  font-size: 40px; color: #fff; letter-spacing: -0.01em;
}
.hero-sub { font-size: 15px; opacity: 0.9; margin-top: var(--spacing-2); }

.hero-stats {
  display: flex; align-items: center; gap: var(--spacing-5);
  margin-top: var(--spacing-6);
  padding-top: var(--spacing-5);
  border-top: 1px solid rgba(255,255,255,0.25);
}
.hero-stat-num { display: block; font-family: var(--font-display); font-size: 28px; font-weight: 700; color: #fff; line-height: 1; }
.hero-stat-lbl { font-size: 12px; opacity: 0.85; }
.hero-stat-div { width: 1px; height: 28px; background: rgba(255,255,255,0.3); }

/* ── 工具栏 ── */
.resume-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: var(--spacing-4);
}

/* ── 简历卡片网格 ── */
.resume-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: var(--spacing-4); }

.resume-card { padding: 0; overflow: hidden; position: relative; }
.resume-card--default { border-color: var(--color-accent); }
.resume-card--default::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-accent), #E8A87C);
}

.resume-card-top { padding: var(--spacing-3) var(--spacing-5) 0; }
.default-badge {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 12px; font-weight: 500; color: var(--color-accent);
  background: var(--color-accent-light); border-radius: 999px; padding: 3px 10px;
}

.resume-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--spacing-4) var(--spacing-5);
  border-bottom: 1px solid var(--color-divider);
}
.resume-card-title { font-size: 17px; font-weight: 600; }
.resume-card-actions { display: flex; gap: 2px; flex-shrink: 0; }

.resume-card-body { padding: var(--spacing-5); }

.info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: var(--spacing-4); margin-bottom: var(--spacing-4); }
.info-item { display: flex; flex-direction: column; gap: 2px; }
.info-label { font-size: 12px; color: var(--color-text-placeholder); }
.info-value { font-size: 14px; color: var(--color-text-primary); font-weight: 500; }

.skills-row { display: flex; flex-wrap: wrap; gap: 6px; }
.skill-tag { background: var(--color-surface-subtle); color: var(--color-text-body); }

.resume-card-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--spacing-4) var(--spacing-5);
  border-top: 1px solid var(--color-divider);
}
.resume-card-footer .btn svg { width: 14px; height: 14px; }

/* ── 危险按钮（design-system 未提供，对齐色板自建） ── */
.btn--danger {
  background: var(--color-danger-light); border-color: transparent;
  color: var(--color-danger);
}
.btn--danger:hover { background: var(--color-danger); border-color: var(--color-danger); color: #fff; }

/* ── 弹窗 ── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(28, 25, 23, 0.3);
  display: flex; align-items: center; justify-content: center;
  z-index: 200; padding: var(--spacing-4);
}
.modal-content { width: 100%; max-width: 520px; max-height: 90vh; overflow-y: auto; padding: 0; }
.modal-lg { max-width: 680px; }

.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--spacing-5) var(--spacing-6);
  border-bottom: 1px solid var(--color-divider);
  position: sticky; top: 0; z-index: 1;
  background: var(--color-surface);
}
.modal-header h4 { font-size: 17px; }

.modal-body { padding: var(--spacing-6); }
.form-row--2 { grid-template-columns: repeat(2, 1fr); }
@media (max-width: 640px) { .form-row--2 { grid-template-columns: 1fr; gap: 0; } }

.modal-footer {
  display: flex; gap: 10px; justify-content: flex-end;
  padding: var(--spacing-4) var(--spacing-6);
  border-top: 1px solid var(--color-divider);
  position: sticky; bottom: 0; z-index: 1;
  background: var(--color-surface);
}

/* ── 预览 ── */
.preview-sheet { padding: var(--spacing-2); }
.preview-header {
  text-align: center;
  padding: var(--spacing-6) var(--spacing-5);
  margin-bottom: var(--spacing-5);
  border-radius: var(--radius-md);
  background: var(--color-accent-light);
  border-bottom: 3px solid var(--color-accent);
}
.preview-name { font-size: 26px; font-weight: 600; color: var(--color-text-primary); }
.preview-job {
  font-size: 14px; color: var(--color-accent);
  font-weight: 500; margin-top: var(--spacing-1);
}
.preview-contact { display: flex; justify-content: center; gap: var(--spacing-4); font-size: 13px; color: var(--color-text-secondary); margin-top: var(--spacing-3); }

.preview-block { margin-bottom: var(--spacing-5); }
.preview-block .block-title {
  font-size: 14px; font-weight: 600; color: var(--color-accent);
  margin-bottom: var(--spacing-3); padding-bottom: var(--spacing-2);
  border-bottom: 2px solid var(--color-divider);
  letter-spacing: 0.04em;
}
.info-row { display: flex; flex-wrap: wrap; gap: var(--spacing-4); font-size: 13px; color: var(--color-text-body); }
.preview-text { font-size: 13px; color: var(--color-text-body); line-height: 1.8; white-space: pre-wrap; }
</style>
