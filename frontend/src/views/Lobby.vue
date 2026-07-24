<template>
  <div class="lobby">
    <div class="page-shell">
      <!-- Left Column -->
      <aside class="col-left">
        <div class="panel profile-card">
          <div class="profile-top">
            <div class="profile-avatar">{{ userInitial }}</div>
            <div>
              <div class="profile-name">{{ username }}</div>
              <div class="profile-sub">{{ role === 'ADMIN' ? '管理员' : '普通用户' }} · {{ interviewCount }} 场面试</div>
            </div>
          </div>
        </div>
        <div class="panel quick-links">
          <div class="quick-list">
            <div class="quick-item" @click="$router.push('/history')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
              我的面试
              <span class="qi-badge">{{ interviewCount }}</span>
            </div>
            <div class="quick-item" @click="$router.push('/resume')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M9 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-4"/><path d="M9 15l10-10 4 4-10 10H9v-4z"/></svg>
              我的简历
            </div>
            <div class="quick-item" @click="$router.push('/question-bank')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 17.3l-6.16 3.24 1.18-6.88L2 8.9l6.92-1L12 1.5l3.08 6.4 6.92 1-5.02 4.76 1.18 6.88z"/></svg>
              面试题库
            </div>
          </div>
        </div>
        <button class="btn-create-room" @click="showCreateModal = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          创建面试房间
        </button>
      </aside>

      <!-- Center Column -->
      <main class="col-center">
        <div class="panel toolbar">
          <div class="search-row">
            <div class="search-input-wrap">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35"/></svg>
              <input type="text" class="search-input" v-model="searchQuery" placeholder="按岗位方向或关键词搜索，如「前端」「Java」">
            </div>
            <button class="btn-quick-match" @click="quickMatch">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 2L4 14h7l-1 8 9-12h-7l1-8z"/></svg>
              快速匹配
            </button>
          </div>
          <div class="filter-groups">
            <div class="filter-group">
              <span class="fg-label">方向</span>
              <button v-for="d in directions" :key="d" class="chip" :class="{ active: filterDirection === d }" @click="filterDirection = d">{{ d }}</button>
            </div>
            <div class="filter-group">
              <span class="fg-label">难度</span>
              <button v-for="l in levels" :key="l" class="chip" :class="{ active: filterLevel === l }" @click="filterLevel = l">{{ l }}</button>
            </div>
          </div>
        </div>

        <div class="list-header">
          <div class="lh-left">
            <h2>岗位列表</h2>
            <span class="lh-count">共 {{ filteredJobs.length }} 个岗位</span>
          </div>
        </div>

        <div class="room-container">
          <div v-for="job in filteredJobs" :key="job.id" class="room-card">
            <div class="rc-top">
              <span class="mode-tag">AI 对练</span>
              <span class="tag-pill">
                <span class="diff-dot" :class="diffDot[job.level]"></span>
                {{ levelText(job.level) }}
              </span>
            </div>
            <div class="rc-title">{{ job.name }}</div>
            <div class="rc-tags">
              <span class="tag-pill">{{ job.category }}</span>
            </div>
            <div class="rc-bottom">
              <span class="rc-desc">{{ job.description }}</span>
              <button class="btn-join" @click="startInterview(job)">开始面试</button>
            </div>
          </div>
        </div>
      </main>

      <!-- Right Column -->
      <aside class="col-right">
        <div class="panel">
          <div class="panel-header"><h3>本周练习统计</h3></div>
          <div class="stat-numbers">
            <div class="stat-box"><div class="sv">{{ interviewCount }}</div><div class="sl">总场次</div></div>
            <div class="stat-box"><div class="sv">{{ avgScore }}</div><div class="sl">平均得分</div></div>
          </div>
        </div>
        <div class="panel">
          <div class="panel-header"><h3>热门题库推荐</h3></div>
          <div class="recommend-list">
            <div class="recommend-item" v-for="(item, index) in hotQuestions" :key="index">
              <div class="rec-rank">{{ index + 1 }}</div>
              <div class="rec-body">
                <div class="rn">{{ item.name }}</div>
                <div class="rc-meta">{{ item.count }} 人练习过</div>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- Create Room Modal -->
    <div class="modal-overlay" :class="{ open: showCreateModal }" @click.self="showCreateModal = false">
      <div class="modal-box">
        <h3>创建面试房间</h3>
        <p class="modal-sub">设置面试信息后即可开始AI模拟面试</p>
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="岗位方向">
            <el-select v-model="createForm.jobName" placeholder="请选择岗位" style="width:100%">
              <el-option v-for="job in jobs" :key="job.id" :label="job.name" :value="job.name" />
            </el-select>
          </el-form-item>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item label="难度">
                <el-select v-model="createForm.difficulty" style="width:100%">
                  <el-option label="简单" value="EASY" />
                  <el-option label="中等" value="MEDIUM" />
                  <el-option label="困难" value="HARD" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="模式">
                <el-select v-model="createForm.mode" style="width:100%">
                  <el-option label="文字面试" value="TEXT" />
                  <el-option label="语音面试" value="VOICE" />
                  <el-option label="视频面试" value="VIDEO" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showCreateModal = false">取消</button>
          <button class="btn-primary-modal" @click="createInterview">创建并进入房间</button>
        </div>
      </div>
    </div>

    <div v-if="toast" class="toast show">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6L9 17l-5-5"/></svg>
      <span>{{ toastMsg }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const showCreateModal = ref(false)
const searchQuery = ref('')
const filterDirection = ref('全部')
const filterLevel = ref('全部')
const interviewCount = ref(0)
const avgScore = ref(82)
const toast = ref(false)
const toastMsg = ref('')
const username = ref(localStorage.getItem('username') || '用户')
const role = ref(localStorage.getItem('userRole') || 'USER')
const userInitial = ref((localStorage.getItem('username') || 'U').charAt(0).toUpperCase())

const directions = ['全部', '前端开发', 'Java后端', '算法', '产品经理', '测试开发', '大数据']
const levels = ['全部', '初级', '中级', '高级']
const diffDot = { EASY: 'd1', MEDIUM: 'd2', HARD: 'd3' }
const levelText = (l) => ({ EASY: '初级', MEDIUM: '中级', HARD: '高级' }[l] || l)

const jobs = ref([])
const hotQuestions = ref([])

const createForm = ref({ jobName: '', difficulty: 'MEDIUM', mode: 'TEXT' })

const levelMap = { '初级': 'EASY', '中级': 'MEDIUM', '高级': 'HARD' }

const filteredJobs = computed(() => {
  return jobs.value.filter(job => {
    if (filterDirection.value !== '全部' && job.category !== filterDirection.value) return false
    if (filterLevel.value !== '全部' && job.level !== levelMap[filterLevel.value]) return false
    if (searchQuery.value && !job.name.includes(searchQuery.value) && !job.category.includes(searchQuery.value)) return false
    return true
  })
})

onMounted(() => {
  loadInterviewCount()
  loadJobs()
  loadHotQuestions()
})

const loadInterviewCount = async () => {
  try { const res = await api.get('/interview/list'); interviewCount.value = res.data?.length || 0 } catch (e) {}
}

const loadJobs = async () => {
  try { const res = await api.get('/job/list'); jobs.value = res.data || [] } catch (e) {}
}

const loadHotQuestions = async () => {
  try { const res = await api.get('/hot/list'); hotQuestions.value = res.data || [] } catch (e) {}
}

const showToast = (msg) => { toastMsg.value = msg; toast.value = true; setTimeout(() => { toast.value = false }, 2600) }

const startInterview = (job) => { createForm.value.jobName = job.name; showCreateModal.value = true }

const createInterview = async () => {
  if (!createForm.value.jobName) { showToast('请选择岗位方向'); return }
  try {
    const res = await api.post('/interview/create', { userId: 1, jobName: createForm.value.jobName, mode: createForm.value.mode, difficulty: createForm.value.difficulty })
    showCreateModal.value = false; router.push(`/interview/${res.data.id}`)
  } catch (e) { showToast('创建失败，请重试') }
}

const quickMatch = () => {
  const picked = jobs.value[Math.floor(Math.random() * jobs.value.length)]
  showToast('正在匹配岗位…')
  setTimeout(() => { createForm.value.jobName = picked.name; showCreateModal.value = true }, 500)
}
</script>

<style scoped>
.lobby { max-width: 1400px; margin: 0 auto; }
* { box-sizing: border-box; margin: 0; padding: 0; }
.page-shell { display: grid; grid-template-columns: 248px minmax(0,1fr) 296px; gap: 20px; align-items: start; }
.panel { background: #fff; border: 1px solid #E8E8E8; border-radius: 4px; }
.panel-header { padding: 14px 16px 10px; display: flex; align-items: center; justify-content: space-between; }
.panel-header h3 { font-size: 14px; font-weight: normal; color: #1A1A1A; }

/* Left */
.col-left { display: flex; flex-direction: column; gap: 16px; position: sticky; top: 76px; }
.profile-card { padding: 20px 16px; }
.profile-top { display: flex; align-items: center; gap: 12px; }
.profile-avatar { width: 46px; height: 46px; border-radius: 50%; background: #C74634; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 17px; }
.profile-name { font-size: 15px; color: #1A1A1A; }
.profile-sub { font-family: sans-serif; font-size: 12px; color: #767676; margin-top: 2px; }
.quick-list { padding: 6px; }
.quick-item { display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: 4px; font-size: 13px; color: #404040; cursor: pointer; transition: background 0.2s; }
.quick-item:hover { background: #F5F5F5; }
.quick-item svg { width: 16px; height: 16px; color: #767676; flex-shrink: 0; }
.qi-badge { margin-left: auto; font-family: sans-serif; font-size: 10px; color: #C74634; background: #FBEEEC; padding: 1px 6px; border-radius: 999px; }
.btn-create-room { width: 100%; padding: 13px 14px; background: #C74634; color: #fff; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: background 0.2s; }
.btn-create-room:hover { background: #A83A2B; }
.btn-create-room svg { width: 16px; height: 16px; }

/* Center */
.col-center { display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.toolbar { padding: 16px; display: flex; flex-direction: column; gap: 14px; }
.search-row { display: flex; gap: 10px; }
.search-input-wrap { position: relative; flex: 1; }
.search-input-wrap svg { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 16px; height: 16px; color: #9E9E9E; }
.search-input { width: 100%; height: 42px; padding: 0 14px 0 36px; border: 1px solid #C4C4C4; border-radius: 4px; font-size: 13px; color: #1A1A1A; outline: none; transition: border-color 0.2s, box-shadow 0.2s; }
.search-input::placeholder { color: #9E9E9E; }
.search-input:focus { border-color: #C74634; box-shadow: 0 0 0 3px rgba(199,70,52,0.12); }
.btn-quick-match { flex-shrink: 0; height: 42px; padding: 0 18px; background: #fff; color: #C74634; border: 1px solid #C74634; border-radius: 4px; font-size: 13px; cursor: pointer; display: flex; align-items: center; gap: 7px; white-space: nowrap; transition: all 0.2s; }
.btn-quick-match:hover { background: #C74634; color: #fff; }
.btn-quick-match svg { width: 15px; height: 15px; }
.filter-groups { display: flex; flex-wrap: wrap; gap: 20px 28px; }
.filter-group { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.fg-label { font-family: sans-serif; font-size: 11px; color: #767676; }
.chip { font-family: sans-serif; font-size: 12px; color: #5C5C5C; background: #fff; border: 1px solid #C4C4C4; padding: 5px 12px; border-radius: 999px; cursor: pointer; white-space: nowrap; transition: all 0.2s; }
.chip:hover { border-color: #767676; }
.chip.active { background: #1A1A1A; border-color: #1A1A1A; color: #fff; }
.list-header { display: flex; align-items: center; justify-content: space-between; }
.lh-left { display: flex; align-items: baseline; gap: 8px; }
.lh-left h2 { font-size: 16px; font-weight: normal; color: #1A1A1A; }
.lh-count { font-family: sans-serif; font-size: 12px; color: #767676; }
.room-container { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.room-card { background: #fff; border: 1px solid #E8E8E8; border-radius: 4px; padding: 16px; display: flex; flex-direction: column; gap: 12px; transition: all 0.2s; }
.room-card:hover { border-color: #C74634; box-shadow: 0 4px 14px rgba(0,0,0,0.06); transform: translateY(-1px); }
.rc-top { display: flex; align-items: center; justify-content: space-between; }
.mode-tag { font-family: sans-serif; font-size: 11px; color: #5C5C5C; border: 1px solid #C4C4C4; padding: 3px 9px; border-radius: 4px; }
.tag-pill { font-family: sans-serif; font-size: 11px; color: #5C5C5C; background: #F5F5F5; padding: 3px 9px; border-radius: 4px; display: flex; align-items: center; gap: 5px; }
.diff-dot { width: 6px; height: 6px; border-radius: 50%; }
.diff-dot.d1 { background: #2E7D32; }
.diff-dot.d2 { background: #E65100; }
.diff-dot.d3 { background: #C62828; }
.rc-title { font-size: 15px; color: #1A1A1A; }
.rc-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.rc-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: auto; padding-top: 10px; border-top: 1px solid #F5F5F5; }
.rc-desc { font-family: sans-serif; font-size: 12px; color: #767676; }
.btn-join { font-family: sans-serif; font-size: 12px; padding: 7px 16px; border-radius: 4px; border: none; cursor: pointer; background: #C74634; color: #fff; transition: background 0.2s; }
.btn-join:hover { background: #A83A2B; }

/* Right */
.col-right { display: flex; flex-direction: column; gap: 16px; }
.stat-numbers { display: flex; padding: 4px 16px 16px; gap: 10px; }
.stat-box { flex: 1; text-align: center; padding: 10px 4px; background: #F5F5F5; border-radius: 4px; }
.sv { font-size: 20px; color: #1A1A1A; font-family: sans-serif; font-weight: 600; }
.sl { font-size: 11px; color: #767676; margin-top: 3px; font-family: sans-serif; }
.recommend-list { padding: 4px 6px 10px; }
.recommend-item { display: flex; align-items: center; gap: 10px; padding: 10px; border-radius: 4px; cursor: pointer; transition: background 0.2s; }
.recommend-item:hover { background: #F5F5F5; }
.rec-rank { flex-shrink: 0; width: 22px; height: 22px; border-radius: 4px; background: #1A1A1A; color: #fff; font-family: sans-serif; font-size: 11px; display: flex; align-items: center; justify-content: center; }
.recommend-item:first-child .rec-rank { background: #C74634; }
.rec-body { flex: 1; min-width: 0; }
.rn { font-size: 13px; color: #2D2D2D; }
.rc-meta { font-family: sans-serif; font-size: 11px; color: #767676; margin-top: 1px; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(26,26,26,0.5); display: none; align-items: center; justify-content: center; z-index: 100; padding: 20px; }
.modal-overlay.open { display: flex; }
.modal-box { background: #fff; width: 100%; max-width: 440px; border-radius: 6px; padding: 28px; box-shadow: 0 20px 60px rgba(0,0,0,0.25); }
.modal-box h3 { font-size: 18px; color: #1A1A1A; margin-bottom: 4px; font-weight: normal; }
.modal-sub { font-family: sans-serif; font-size: 13px; color: #9E9E9E; margin-bottom: 22px; }
.modal-actions { display: flex; gap: 10px; margin-top: 20px; }
.btn-secondary { flex: 1; height: 42px; background: #fff; color: #404040; border: 1px solid #C4C4C4; border-radius: 4px; font-size: 14px; cursor: pointer; }
.btn-secondary:hover { background: #F5F5F5; }
.btn-primary-modal { flex: 1.4; height: 42px; background: #C74634; color: #fff; border: none; border-radius: 4px; font-size: 14px; cursor: pointer; }
.btn-primary-modal:hover { background: #A83A2B; }

/* Toast */
.toast { position: fixed; bottom: 24px; right: 24px; background: #1A1A1A; color: #fff; padding: 13px 18px; border-radius: 4px; font-family: sans-serif; font-size: 13px; display: flex; align-items: center; gap: 10px; box-shadow: 0 10px 30px rgba(0,0,0,0.25); transform: translateY(20px); opacity: 0; pointer-events: none; transition: transform 0.25s, opacity 0.25s; z-index: 200; border-left: 3px solid #C74634; }
.toast.show { transform: translateY(0); opacity: 1; }
.toast svg { width: 16px; height: 16px; color: #6FCF73; flex-shrink: 0; }

@media (max-width: 1180px) { .page-shell { grid-template-columns: 1fr; max-width: 720px; } }
@media (max-width: 720px) { .room-container { grid-template-columns: 1fr; } }
</style>