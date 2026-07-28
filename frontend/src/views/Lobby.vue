<template>
  <div class="lobby">
    <div class="page-shell">
      <!-- 左侧栏 -->
      <aside class="sidebar">
        <div class="sidebar-card profile-card">
          <div class="profile-avatar">{{ userStore.userInitial }}</div>
          <div class="profile-name">{{ userStore.username }}</div>
          <div class="profile-role">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</div>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-value">{{ interviewCount }}</span>
              <span class="stat-label">面试</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ avgScore }}</span>
              <span class="stat-label">均分</span>
            </div>
          </div>
        </div>

        <div class="sidebar-card">
          <nav class="sidebar-nav">
            <a class="nav-item" :class="{ active: currentPath === '/lobby' }" @click="$router.push('/lobby')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><path d="M9 22V12h6v10"/></svg>
              面试大厅
            </a>
            <a class="nav-item" :class="{ active: currentPath === '/resume' }" @click="$router.push('/resume')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6"/></svg>
              我的简历
            </a>
            <a class="nav-item" :class="{ active: currentPath === '/question-bank' }" @click="$router.push('/question-bank')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
              面试题库
            </a>
            <a class="nav-item" :class="{ active: currentPath === '/history' }" @click="$router.push('/history')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              练习记录
            </a>
          </nav>
        </div>

        <button class="create-btn" @click="showCreateModal = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          开始面试
        </button>
      </aside>

      <!-- 中间内容 -->
      <main class="content">
        <!-- 搜索和筛选 -->
        <div class="search-card">
          <div class="search-row">
            <div class="search-input-wrap">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
              <input type="text" class="search-input" v-model="searchQuery" placeholder="搜索岗位方向...">
            </div>
            <button class="quick-match-btn" @click="quickMatch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
              快速匹配
            </button>
          </div>

          <div class="filter-row">
            <div class="filter-group">
              <span class="filter-label">方向</span>
              <button v-for="d in directions" :key="d" class="filter-chip" :class="{ active: filterDirection === d }" @click="filterDirection = d">{{ d }}</button>
            </div>
            <div class="filter-group">
              <span class="filter-label">难度</span>
              <button v-for="l in levels" :key="l" class="filter-chip" :class="{ active: filterLevel === l }" @click="filterLevel = l">{{ l }}</button>
            </div>
          </div>
        </div>

        <!-- 岗位列表 -->
        <div class="list-header">
          <h2 class="list-title">岗位列表</h2>
          <span class="list-count">{{ filteredJobs.length }} 个岗位</span>
        </div>

        <div class="job-grid">
          <div v-for="job in filteredJobs" :key="job.id" class="job-card" @click="startInterview(job)">
            <div class="job-card-header">
              <span class="job-mode">AI 对练</span>
              <span class="level-tag" :class="'level-' + (job.level || 'MEDIUM').toLowerCase()">{{ levelText(job.level) }}</span>
            </div>
            <h3 class="job-name">{{ job.name }}</h3>
            <p class="job-desc">{{ job.description }}</p>
            <div class="job-footer">
              <span class="job-category">{{ job.category }}</span>
              <span class="job-arrow">→</span>
            </div>
          </div>
        </div>

        <div v-if="filteredJobs.length === 0" class="empty-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <p>没有找到匹配的岗位</p>
        </div>
      </main>

      <!-- 右侧栏 -->
      <aside class="sidebar-right">
        <div class="sidebar-card">
          <h3 class="card-title">热门题库</h3>
          <div class="hot-list">
            <div v-for="(item, index) in hotQuestions" :key="index" class="hot-item">
              <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <div class="hot-info">
                <span class="hot-name">{{ item.name }}</span>
                <span class="hot-count">{{ item.count }} 人练习</span>
              </div>
            </div>
          </div>
        </div>

        <div class="sidebar-card">
          <h3 class="card-title">平台数据</h3>
          <div class="platform-stats">
            <div class="platform-stat">
              <span class="platform-value">1,284</span>
              <span class="platform-label">在线用户</span>
            </div>
            <div class="platform-stat">
              <span class="platform-value">5,672</span>
              <span class="platform-label">面试总数</span>
            </div>
            <div class="platform-stat">
              <span class="platform-value">82.5</span>
              <span class="platform-label">平均分</span>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 创建面试弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
          <div class="modal-content">
            <div class="modal-header">
              <h3>创建面试</h3>
              <button class="modal-close" @click="showCreateModal = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div class="modal-body">
              <div class="form-group">
                <label class="form-label">岗位方向</label>
                <select class="form-select" v-model="createForm.jobName">
                  <option value="">请选择岗位</option>
                  <option v-for="job in jobs" :key="job.id" :value="job.name">{{ job.name }}</option>
                </select>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label class="form-label">难度</label>
                  <select class="form-select" v-model="createForm.difficulty">
                    <option value="初级">初级</option>
                    <option value="中级">中级</option>
                    <option value="高级">高级</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">模式</label>
                  <select class="form-select" v-model="createForm.mode">
                    <option value="TEXT">文字面试</option>
                    <option value="VOICE">语音面试</option>
                    <option value="VIDEO">视频面试</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-outline" @click="showCreateModal = false">取消</button>
              <button class="btn btn-primary" @click="createInterview">开始面试</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <!-- Toast -->
    <transition name="toast">
      <div v-if="toast" class="toast">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg>
        {{ toastMsg }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { levelText } from '@/utils/constants'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前路由路径，用于导航高亮
const currentPath = computed(() => route.path)
const showCreateModal = ref(false)
const searchQuery = ref('')
const filterDirection = ref('全部')
const filterLevel = ref('全部')
const interviewCount = ref(0)
const avgScore = ref(82)
const toast = ref(false)
const toastMsg = ref('')

const directions = ['全部', '前端开发', '后端开发', '移动开发', '数据', '测试', '运维', '产品', '设计']
const levels = ['全部', '初级', '中级', '高级']
// levelText 从 '@/utils/constants' 导入

const jobs = ref([])
const hotQuestions = ref([])

const createForm = ref({ jobName: '', difficulty: '中级', mode: 'TEXT' })

const filteredJobs = computed(() => {
  return jobs.value.filter(job => {
    if (filterDirection.value !== '全部' && job.category !== filterDirection.value) return false
    if (filterLevel.value !== '全部' && levelText(job.level) !== filterLevel.value) return false
    if (searchQuery.value && !job.name.includes(searchQuery.value) && !job.category.includes(searchQuery.value)) return false
    return true
  })
})

onMounted(() => {
  loadInterviewCount()
  loadJobs()
  loadHotQuestions()

  // 检查是否从简历页面跳转过来
  if (route.query.fromResume === 'true') {
    const jobName = route.query.jobName
    if (jobName) {
      createForm.value.jobName = jobName
      showCreateModal.value = true
      ElMessage.info(`已自动填充岗位：${jobName}`)
    }
  }
})

const loadInterviewCount = async () => {
  try {
    const res = await api.get('/interview/list', { params: { userId: userStore.userId } })
    interviewCount.value = res.data?.length || 0
  } catch (e) { console.warn('加载面试次数失败:', e) }
}

const loadJobs = async () => {
  try { const res = await api.get('/job/list'); jobs.value = res.data || [] } catch (e) { console.warn('加载岗位列表失败:', e) }
}

const loadHotQuestions = async () => {
  try { const res = await api.get('/hot/list'); hotQuestions.value = res.data || [] } catch (e) { console.warn('加载热门题库失败:', e) }
}

const showToast = (msg) => {
  toastMsg.value = msg
  toast.value = true
  setTimeout(() => { toast.value = false }, 2600)
}

const startInterview = (job) => {
  createForm.value.jobName = job.name
  showCreateModal.value = true
}

const createInterview = async () => {
  if (!createForm.value.jobName) {
    showToast('请选择岗位方向')
    return
  }
  try {
    const res = await api.post('/interview/create', {
      userId: userStore.userId,
      jobName: createForm.value.jobName,
      mode: createForm.value.mode,
      difficulty: createForm.value.difficulty
    })
    showCreateModal.value = false
    router.push(`/interview/${res.data.id}`)
  } catch (e) {
    showToast('创建失败，请重试')
  }
}

const quickMatch = () => {
  const picked = jobs.value[Math.floor(Math.random() * jobs.value.length)]
  createForm.value.jobName = picked.name
  showCreateModal.value = true
}
</script>

<style scoped>
.lobby {
  max-width: 1400px;
  margin: 0 auto;
}

.page-shell {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: var(--space-5);
  align-items: start;
}

/* 侧边栏 */
.sidebar {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.sidebar-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: var(--space-4);
}

.profile-card {
  text-align: center;
  padding: var(--space-6) var(--space-4);
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  margin: 0 auto var(--space-3);
}

.profile-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: 4px;
}

.profile-role {
  font-size: 13px;
  color: var(--gray-500);
  margin-bottom: var(--space-4);
}

.profile-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-color);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: var(--gray-900);
}

.stat-label {
  font-size: 12px;
  color: var(--gray-500);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--border-color);
}

/* 侧边栏导航 */
.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  font-size: 14px;
  color: var(--gray-600);
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
}

.nav-item:hover {
  background: var(--gray-50);
  color: var(--gray-900);
}

.nav-item.active {
  background: var(--primary-bg);
  color: var(--primary);
}

.nav-item svg {
  width: 18px;
  height: 18px;
}

/* 创建按钮 */
.create-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: var(--transition);
}

.create-btn:hover {
  background: var(--primary-dark);
}

.create-btn svg {
  width: 18px;
  height: 18px;
}

/* 中间内容 */
.content {
  min-width: 0;
}

/* 搜索卡片 */
.search-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: var(--space-5);
  margin-bottom: var(--space-5);
}

.search-row {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.search-input-wrap {
  flex: 1;
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--gray-400);
}

.search-input {
  width: 100%;
  height: 42px;
  padding: 0 16px 0 40px;
  font-size: 14px;
  border: 1px solid var(--gray-200);
  border-radius: var(--border-radius);
  background: var(--gray-50);
  transition: var(--transition);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 3px var(--primary-bg);
}

.search-input::placeholder {
  color: var(--gray-400);
}

.quick-match-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  height: 42px;
  background: white;
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: var(--border-radius);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  white-space: nowrap;
}

.quick-match-btn:hover {
  background: var(--primary);
  color: white;
}

.quick-match-btn svg {
  width: 16px;
  height: 16px;
}

/* 筛选 */
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.filter-label {
  font-size: 13px;
  color: var(--gray-500);
  margin-right: 4px;
}

.filter-chip {
  padding: 5px 14px;
  font-size: 13px;
  color: var(--gray-600);
  background: white;
  border: 1px solid var(--gray-200);
  border-radius: 9999px;
  cursor: pointer;
  transition: var(--transition);
}

.filter-chip:hover {
  border-color: var(--gray-300);
}

.filter-chip.active {
  background: var(--gray-900);
  border-color: var(--gray-900);
  color: white;
}

/* 列表头部 */
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.list-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
}

.list-count {
  font-size: 13px;
  color: var(--gray-500);
}

/* 岗位网格 */
.job-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.job-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: var(--space-5);
  cursor: pointer;
  transition: var(--transition);
}

.job-card:hover {
  border-color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.job-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.job-mode {
  font-size: 12px;
  color: var(--gray-500);
  padding: 2px 8px;
  background: var(--gray-100);
  border-radius: 4px;
}

.level-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 4px;
}

.level-easy { color: var(--level-easy); background: var(--level-easy-bg); }
.level-medium { color: var(--level-medium); background: var(--level-medium-bg); }
.level-hard { color: var(--level-hard); background: var(--level-hard-bg); }

.job-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: 6px;
}

.job-desc {
  font-size: 13px;
  color: var(--gray-500);
  margin-bottom: var(--space-3);
}

.job-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-color);
}

.job-category {
  font-size: 12px;
  color: var(--gray-500);
}

.job-arrow {
  font-size: 14px;
  color: var(--gray-400);
  transition: var(--transition);
}

.job-card:hover .job-arrow {
  color: var(--primary);
  transform: translateX(4px);
}

/* 右侧栏 */
.sidebar-right {
  position: sticky;
  top: 80px;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: var(--space-3);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-color);
}

/* 热门题库 */
.hot-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.hot-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 8px;
  border-radius: var(--border-radius-sm);
  transition: var(--transition);
}

.hot-item:hover {
  background: var(--gray-50);
}

.hot-rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: var(--gray-100);
  color: var(--gray-600);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.hot-rank.top {
  background: var(--primary);
  color: white;
}

.hot-info {
  flex: 1;
  min-width: 0;
}

.hot-name {
  display: block;
  font-size: 13px;
  color: var(--gray-800);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-count {
  font-size: 12px;
  color: var(--gray-400);
}

/* 平台数据 */
.platform-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-2);
}

.platform-stat {
  text-align: center;
  padding: var(--space-3);
  background: var(--gray-50);
  border-radius: var(--border-radius-sm);
}

.platform-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: var(--gray-900);
}

.platform-label {
  font-size: 11px;
  color: var(--gray-500);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-12) var(--space-6);
  color: var(--gray-400);
}

.empty-state svg {
  width: 48px;
  height: 48px;
  margin-bottom: var(--space-3);
  opacity: 0.5;
}

.empty-state p {
  font-size: 14px;
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

.form-select {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  font-size: 14px;
  border: 1px solid var(--gray-300);
  border-radius: var(--border-radius-sm);
  background: white;
  color: var(--gray-900);
  cursor: pointer;
  transition: var(--transition);
}

.form-select:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px var(--primary-bg);
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
}

/* Toast */
.toast {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--gray-900);
  color: white;
  padding: 12px 20px;
  border-radius: var(--border-radius);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: var(--shadow-lg);
  z-index: 2000;
}

.toast svg {
  width: 18px;
  height: 18px;
  color: var(--success);
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

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px);
}

/* 响应式 */
@media (max-width: 1200px) {
  .page-shell {
    grid-template-columns: 1fr;
  }

  .sidebar,
  .sidebar-right {
    position: static;
  }

  .sidebar {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .sidebar-card {
    flex: 1;
    min-width: 200px;
  }

  .create-btn {
    width: auto;
    padding: 12px 24px;
  }
}

@media (max-width: 768px) {
  .job-grid {
    grid-template-columns: 1fr;
  }

  .search-row {
    flex-direction: column;
  }

  .quick-match-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>