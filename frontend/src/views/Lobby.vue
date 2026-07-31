<template>
  <div class="lobby-grid">
    <!-- 左侧 -->
    <aside class="lobby-left">
      <div class="card" style="padding:var(--spacing-6);text-align:center">
        <div class="profile-avatar">{{ userStore.userInitial }}</div>
        <h3 style="margin-top:var(--spacing-3);margin-bottom:2px">{{ userStore.username || '用户' }}</h3>
        <p class="text-sm text-muted">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</p>
        <div class="profile-stats">
          <div><span class="ps-num">{{ interviewCount }}</span><span class="ps-lbl">面试</span></div>
          <div class="ps-d"></div>
          <div><span class="ps-num">{{ avgScore }}</span><span class="ps-lbl">均分</span></div>
        </div>
      </div>
      <div class="card" style="padding:var(--spacing-3)">
        <nav class="side-nav">
          <a class="sn-link active" @click="$router.push('/lobby')">✦ 面试大厅</a>
          <a class="sn-link" @click="$router.push('/resume')">📄 我的简历</a>
          <a class="sn-link" @click="$router.push('/history')">📋 练习记录</a>
          <a v-if="userStore.isAdmin" class="sn-link" @click="$router.push('/admin')">⚙️ 管理</a>
        </nav>
      </div>
      <div class="card" style="padding:var(--spacing-4)">
        <p class="eyebrow" style="margin-bottom:var(--spacing-2)">Tip</p>
        <p class="text-sm" style="color:var(--color-text-body);line-height:1.6">回答问题时先给出核心结论，再用具体案例展开说明。STAR 法则是最常用的结构化表达方式。</p>
      </div>
      <button class="btn btn--primary btn--full btn--lg" @click="showCreateModal = true">
        <span style="font-size:18px;margin-right:4px">✦</span>
        开始面试
      </button>
    </aside>

    <!-- 中间 -->
    <main class="lobby-center">
      <div style="margin-bottom:var(--spacing-6)">
        <h2>推荐岗位</h2>
        <p class="text-muted text-sm" style="margin-top:2px">选择岗位开始一场模拟面试</p>
      </div>

      <div class="card" style="padding:var(--spacing-4);margin-bottom:var(--spacing-5)">
        <div style="position:relative;margin-bottom:var(--spacing-3)">
          <svg style="position:absolute;left:12px;top:50%;transform:translateY(-50%);width:16px;height:16px;color:var(--color-text-placeholder)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input type="text" class="form-input" style="padding-left:36px" v-model="searchQuery" placeholder="搜索岗位...">
        </div>
        <div style="display:flex;flex-wrap:wrap;gap:6px;align-items:center">
          <span class="text-sm text-muted" style="margin-right:4px">方向</span>
          <button v-for="d in directions" :key="d" class="chip" :class="{ 'chip--active': filterDirection === d }" @click="filterDirection = d">{{ d }}</button>
          <span class="text-sm text-muted" style="margin:0 4px 0 12px">难度</span>
          <button v-for="l in levels" :key="l" class="chip" :class="{ 'chip--active': filterLevel === l }" @click="filterLevel = l">{{ l }}</button>
        </div>
      </div>

      <div class="job-grid">
        <div v-for="job in filteredJobs" :key="job.id" class="card card--interactive" style="padding:var(--spacing-5);cursor:pointer" @click="startInterview(job)">
          <div class="job-top">
            <span class="text-xs text-muted" style="text-transform:uppercase;letter-spacing:0.04em">AI 面试</span>
            <span class="tag" :class="'tag--' + (job.level === '初级' || job.level === 'EASY' ? 'easy' : job.level === '高级' || job.level === 'HARD' ? 'hard' : 'medium')">{{ levelText(job.level) }}</span>
          </div>
          <h4 style="margin:8px 0 4px">{{ job.name }}</h4>
          <p class="text-sm text-muted" style="line-height:1.5;margin-bottom:12px">{{ job.description }}</p>
          <div class="job-bottom">
            <span class="text-xs text-muted">{{ job.category }}</span>
            <span class="job-arrow">→</span>
          </div>
        </div>
        <div v-if="filteredJobs.length === 0" class="empty-state" style="grid-column:1/-1">
          <p>没有匹配的岗位</p>
        </div>
      </div>
    </main>

    <!-- 右侧 -->
    <aside class="lobby-right">
      <div class="card" style="padding:var(--spacing-5)">
        <p class="eyebrow" style="margin-bottom:var(--spacing-4)">Hot Topics</p>
        <div style="display:flex;flex-direction:column;gap:12px">
          <div v-for="(item, index) in hotQuestions" :key="index" class="hot-item">
            <span class="hot-rank" :class="{ 'hot-rank--top': index < 3 }">{{ index + 1 }}</span>
            <div>
              <div class="hot-name">{{ item.name }}</div>
              <div class="hot-count">{{ item.count }}</div>
            </div>
          </div>
        </div>
      </div>
      <div class="card" style="padding:var(--spacing-5)">
        <p class="eyebrow" style="margin-bottom:var(--spacing-3)">Platform</p>
        <div class="pstats">
          <div class="ps"><span class="ps-v">{{ interviewCount }}</span><span class="ps-l">面试</span></div>
          <div class="ps"><span class="ps-v">{{ avgScore }}</span><span class="ps-l">均分</span></div>
          <div class="ps"><span class="ps-v">{{ jobs.length }}</span><span class="ps-l">岗位</span></div>
        </div>
      </div>
    </aside>

    <!-- Modal -->
    <teleport to="body">
      <transition name="fade">
        <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
          <div class="card" style="width:100%;max-width:420px;padding:0;overflow:hidden">
            <div style="display:flex;align-items:center;justify-content:space-between;padding:var(--spacing-5) var(--spacing-6)">
              <h4>创建面试</h4>
              <button class="btn btn--ghost btn--icon" @click="showCreateModal = false" style="font-size:18px">✕</button>
            </div>
            <div style="padding:0 var(--spacing-6) var(--spacing-5)">
              <div class="form-group"><label class="form-label">岗位方向</label>
                <select class="form-select" v-model="createForm.jobName"><option value="">请选择</option><option v-for="job in jobs" :key="job.id" :value="job.name">{{ job.name }}</option></select>
              </div>
              <div class="form-row">
                <div class="form-group"><label class="form-label">难度</label><select class="form-select" v-model="createForm.difficulty"><option>初级</option><option>中级</option><option>高级</option></select></div>
                <div class="form-group"><label class="form-label">模式</label><select class="form-select" v-model="createForm.mode"><option value="TEXT">文字</option><option value="VOICE">语音</option><option value="VIDEO">视频</option></select></div>
              </div>
            </div>
            <div style="display:flex;gap:10px;justify-content:flex-end;padding:var(--spacing-4) var(--spacing-6);border-top:1px solid var(--color-divider)">
              <button class="btn btn--ghost" @click="showCreateModal = false">取消</button>
              <button class="btn btn--primary" @click="createInterview">开始</button>
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
import { useUserStore } from '@/store'
import { levelText } from '@/utils/constants'
import api from '@/api'

const router = useRouter(); const userStore = useUserStore()
const showCreateModal = ref(false); const searchQuery = ref('')
const filterDirection = ref('全部'); const filterLevel = ref('全部')
const interviewCount = ref(0); const avgScore = ref('-')
const jobs = ref([]); const hotQuestions = ref([])
const createForm = ref({ jobName: '', difficulty: '中级', mode: 'TEXT' })

const directions = ['全部','前端开发','后端开发','移动开发','数据','测试','运维','产品','设计']
const levels = ['全部','初级','中级','高级']

const filteredJobs = computed(() => jobs.value.filter(job => {
  if (filterDirection.value !== '全部' && job.category !== filterDirection.value) return false
  if (filterLevel.value !== '全部' && levelText(job.level) !== filterLevel.value) return false
  if (searchQuery.value && !job.name.includes(searchQuery.value) && !job.category.includes(searchQuery.value)) return false
  return true
}))

onMounted(() => loadData())

const loadData = async () => {
  try {
    const [jobRes, hotRes, ivRes] = await Promise.all([
      api.get('/job/list'), api.get('/hot/list'),
      api.get('/interview/list', { params: { userId: userStore.userId } })
    ])
    jobs.value = jobRes.data || []; hotQuestions.value = hotRes.data || []
    const list = ivRes.data || []
    interviewCount.value = list.filter(i => i.status === 'FINISHED' || i.status === 'COMPLETED').length
    const scores = await Promise.all(list.filter(i => i.status === 'FINISHED' || i.status === 'COMPLETED').map(async i => {
      try { const r = await api.get(`/report/${i.id}`); return r.data?.totalScore } catch { return null }
    }))
    const valid = scores.filter(s => s != null)
    if (valid.length > 0) avgScore.value = Math.round(valid.reduce((a, b) => a + b, 0) / valid.length)
  } catch (e) { console.warn(e) }
}

const startInterview = (job) => { createForm.value.jobName = job.name; showCreateModal.value = true }
const createInterview = async () => {
  if (!createForm.value.jobName) return
  try {
    const res = await api.post('/interview/create', { userId: userStore.userId, jobName: createForm.value.jobName, mode: createForm.value.mode, difficulty: createForm.value.difficulty })
    showCreateModal.value = false; router.push(`/interview/${res.data.id}`)
  } catch {}
}
</script>

<style scoped>
.lobby-grid { display:grid; grid-template-columns: 240px 1fr 240px; gap: var(--spacing-5); align-items: flex-start; }

/* 左侧 */
.lobby-left { position:sticky; top: calc(var(--header-height) + var(--spacing-6)); display:flex; flex-direction:column; gap:var(--spacing-4); }
.profile-avatar { width:56px; height:56px; border-radius:50%; margin:0 auto; background:var(--color-accent); color:white; display:flex; align-items:center; justify-content:center; font-size:20px; font-weight:600; }
.profile-stats { display:flex; align-items:center; justify-content:center; gap:var(--spacing-5); padding-top:var(--spacing-4); margin-top:var(--spacing-4); border-top:1px solid var(--color-divider); }
.ps-num { display:block; font-family:var(--font-display); font-size:22px; font-weight:700; color:var(--color-accent); line-height:1; }
.ps-lbl { font-size:11px; color:var(--color-text-secondary); }
.ps-d { width:1px; height:24px; background:var(--color-divider); }

.side-nav { display:flex; flex-direction:column; gap:2px; }
.sn-link { display:block; padding:8px 12px; font-size:14px; color:var(--color-text-body); border-radius:var(--radius-sm); cursor:pointer; transition:all var(--transition-fast); text-decoration:none; }
.sn-link:hover { background:var(--color-surface-subtle); }
.sn-link.active { background:var(--color-accent-light); color:var(--color-accent); font-weight:500; }

/* 中间 */
.lobby-center { min-width:0; }
.chip {
  display:inline-flex; align-items:center; padding:5px 12px; font-size:13px; font-weight:500;
  color:var(--color-text-body); background:transparent; border:1px solid transparent;
  border-radius:999px; cursor:pointer; transition:all var(--transition-fast); line-height:1.3;
}
.chip:hover { border-color:var(--color-border); background:var(--color-surface); }
.chip--active { background:var(--color-accent); border-color:var(--color-accent); color:var(--color-accent-text); }

.job-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:var(--spacing-4); }
.job-top { display:flex; align-items:center; justify-content:space-between; }
.job-bottom { display:flex; align-items:center; justify-content:space-between; padding-top:var(--spacing-3); border-top:1px solid var(--color-divider); }
.job-arrow { color:var(--color-text-placeholder); transition:all var(--transition-fast); font-family:var(--font-display); font-size:18px; }
.card--interactive:hover .job-arrow { color:var(--color-accent); transform:translateX(4px); }

/* 右侧 */
.lobby-right { display:flex; flex-direction:column; gap:var(--spacing-4); position:sticky; top: calc(var(--header-height) + var(--spacing-6)); }
.hot-item { display:flex; align-items:center; gap:12px; }
.hot-rank { width:24px; height:24px; border-radius:6px; background:var(--color-surface-subtle); color:var(--color-text-secondary); display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600; flex-shrink:0; }
.hot-rank--top { background:var(--color-accent); color:white; }
.hot-name { font-size:13px; font-weight:500; color:var(--color-text-primary); line-height:1.3; }
.hot-count { font-size:11px; color:var(--color-text-secondary); margin-top:1px; }

.pstats { display:grid; grid-template-columns:repeat(3,1fr); gap:var(--spacing-2); }
.ps { text-align:center; }
.ps .ps-v { display:block; font-family:var(--font-display); font-size:22px; font-weight:700; color:var(--color-accent); }
.ps .ps-l { font-size:11px; color:var(--color-text-secondary); }

.modal-overlay { position:fixed; inset:0; background:rgba(28,25,23,0.3); display:flex; align-items:center; justify-content:center; z-index:200; padding:var(--spacing-4); }

@media (max-width:1024px) {
  .lobby-grid { grid-template-columns:1fr; }
  .lobby-left, .lobby-right { position:static; }
  .lobby-left { display:grid; grid-template-columns:1fr 1fr; }
}
@media (max-width:768px) {
  .lobby-left { grid-template-columns:1fr; }
  .job-grid { grid-template-columns:1fr; }
}
</style>
