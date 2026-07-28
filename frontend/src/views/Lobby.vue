<template>
  <div class="lobby">
    <!-- 左侧面板 -->
    <aside class="panel-left">
      <div class="card" style="padding:22px 18px;text-align:center">
        <div class="pa-avatar">{{ userStore.userInitial }}</div>
        <div class="pa-name">{{ userStore.username || '用户' }}</div>
        <div class="pa-role">{{ userStore.isAdmin ? '管理员' : '普通用户' }}</div>
        <div class="pa-stats">
          <div><span class="pa-num">{{ interviewCount }}</span><span class="pa-lbl">面试</span></div>
          <div class="pa-dot"></div>
          <div><span class="pa-num">{{ avgScore }}</span><span class="pa-lbl">均分</span></div>
        </div>
      </div>

      <div class="card" style="padding:10px">
        <nav class="nav">
          <a class="nav-link active" @click="$router.push('/lobby')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:17px;height:17px"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><path d="M9 22V12h6v10"/></svg>
            面试大厅
          </a>
          <a class="nav-link" @click="$router.push('/resume')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:17px;height:17px"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6"/></svg>
            我的简历
          </a>
          <a class="nav-link" @click="$router.push('/history')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:17px;height:17px"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            练习记录
          </a>
          <a v-if="userStore.isAdmin" class="nav-link" @click="$router.push('/admin')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:17px;height:17px"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
            管理
          </a>
        </nav>
      </div>

      <div class="card" style="padding:16px 18px">
        <h4 style="font-size:12px;font-weight:600;color:var(--gray-400);text-transform:uppercase;letter-spacing:0.04em;margin-bottom:10px">今日提示</h4>
        <p style="font-size:12px;color:var(--gray-600);line-height:1.7">回答问题时先给出核心结论，再用具体案例展开说明。 STAR 法则是最常用的结构化表达方式。</p>
      </div>

      <button class="btn btn-primary" style="width:100%;justify-content:center;padding:10px;background:var(--accent);color:white" @click="showCreateModal = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px"><path d="M12 5v14M5 12h14"/></svg>
        开始面试
      </button>
    </aside>

    <!-- 中间内容 -->
    <main class="panel-center">
      <div style="margin-bottom:24px">
        <h1 style="font-size:22px;font-weight:700;color:var(--gray-900)">欢迎回来, {{ userStore.username || '用户' }}</h1>
        <p style="font-size:13px;color:var(--gray-400);margin-top:2px">选择岗位开始一场模拟面试</p>
      </div>

      <!-- 搜索 + 筛选 分两行 -->
      <div class="card" style="padding:16px 20px;margin-bottom:20px">
        <div style="position:relative;margin-bottom:14px">
          <svg style="position:absolute;left:12px;top:50%;transform:translateY(-50%);width:16px;height:16px;color:var(--gray-400)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
          <input type="text" class="input" style="padding-left:36px" v-model="searchQuery" placeholder="搜索岗位...">
        </div>
        <div style="margin-bottom:8px">
          <span style="font-size:12px;color:var(--gray-400);font-weight:500;margin-right:8px">方向</span>
          <span v-for="d in directions" :key="d" class="chip" :class="{ active: filterDirection === d }" @click="filterDirection = d">{{ d }}</span>
        </div>
        <div>
          <span style="font-size:12px;color:var(--gray-400);font-weight:500;margin-right:8px">难度</span>
          <span v-for="l in levels" :key="l" class="chip" :class="{ active: filterLevel === l }" @click="filterLevel = l">{{ l }}</span>
        </div>
      </div>

      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:14px">
        <h3 style="font-size:16px;font-weight:600;color:var(--gray-800)">推荐岗位</h3>
        <span style="font-size:12px;color:var(--gray-400)">{{ filteredJobs.length }} 个</span>
      </div>

      <div class="job-grid">
        <div v-for="job in filteredJobs" :key="job.id" class="card job-card" @click="startInterview(job)">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
            <span style="font-size:11px;color:var(--gray-400);font-weight:500;text-transform:uppercase;letter-spacing:0.04em">AI 面试</span>
            <span class="tag" style="background:var(--accent-light);color:var(--accent)">{{ levelText(job.level) }}</span>
          </div>
          <h4 style="font-size:15px;font-weight:600;color:var(--gray-800);margin-bottom:4px">{{ job.name }}</h4>
          <p style="font-size:12px;color:var(--gray-400);line-height:1.5;margin-bottom:14px">{{ job.description }}</p>
          <div style="display:flex;align-items:center;justify-content:space-between;padding-top:12px;border-top:1px solid var(--card-border)">
            <span style="font-size:12px;color:var(--gray-400)">{{ job.category }}</span>
            <svg style="width:14px;height:14px;color:var(--gray-300);transition:var(--transition)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
          </div>
        </div>
        <div v-if="filteredJobs.length === 0" class="empty-state" style="grid-column:1/-1;padding:40px"><p>没有匹配的岗位</p></div>
      </div>
    </main>

    <!-- 右侧面板 -->
    <aside class="panel-right">
      <div class="card" style="padding:18px">
        <h4 style="font-size:14px;font-weight:600;color:var(--gray-800);margin-bottom:14px;padding-bottom:10px;border-bottom:1px solid var(--card-border)">热门题库</h4>
        <div style="display:flex;flex-direction:column;gap:10px">
          <div v-for="(item, index) in hotQuestions" :key="index" style="display:flex;align-items:center;gap:12px">
            <span class="hr" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <div style="flex:1;min-width:0">
              <div style="font-size:13px;font-weight:500;color:var(--gray-700);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">{{ item.name }}</div>
              <div style="font-size:11px;color:var(--gray-400);margin-top:1px">{{ item.count }} 次练习</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="padding:18px">
        <h4 style="font-size:14px;font-weight:600;color:var(--gray-800);margin-bottom:12px;padding-bottom:10px;border-bottom:1px solid var(--card-border)">面试提示</h4>
        <ul style="list-style:none;display:flex;flex-direction:column;gap:10px">
          <li style="font-size:12px;color:var(--gray-500);line-height:1.6;padding-left:16px;position:relative">
            <span style="position:absolute;left:0;top:6px;width:6px;height:6px;border-radius:50%;background:var(--accent-light)"></span>
            回答问题时先给出结论，再展开说明
          </li>
          <li style="font-size:12px;color:var(--gray-500);line-height:1.6;padding-left:16px;position:relative">
            <span style="position:absolute;left:0;top:6px;width:6px;height:6px;border-radius:50%;background:var(--accent-light)"></span>
            使用 STAR 法则组织项目经历描述
          </li>
          <li style="font-size:12px;color:var(--gray-500);line-height:1.6;padding-left:16px;position:relative">
            <span style="position:absolute;left:0;top:6px;width:6px;height:6px;border-radius:50%;background:var(--accent-light)"></span>
            遇到不会的问题，诚实地说明相关经验
          </li>
          <li style="font-size:12px;color:var(--gray-500);line-height:1.6;padding-left:16px;position:relative">
            <span style="position:absolute;left:0;top:6px;width:6px;height:6px;border-radius:50%;background:var(--accent-light)"></span>
            视频面试时注意眼神交流和坐姿
          </li>
        </ul>
      </div>
    </aside>

    <!-- 创建弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
          <div class="card" style="width:100%;max-width:420px;padding:0;overflow:hidden">
            <div style="display:flex;align-items:center;justify-content:space-between;padding:20px 24px">
              <h3 style="font-size:16px;font-weight:600;color:var(--gray-900)">创建面试</h3>
              <button class="modal-close" @click="showCreateModal = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px"><path d="M18 6L6 18M6 6l12 12"/></svg>
              </button>
            </div>
            <div style="padding:0 24px 20px">
              <div style="margin-bottom:16px">
                <label style="font-size:13px;font-weight:500;color:var(--gray-700);margin-bottom:6px;display:block">岗位方向</label>
                <select class="input" v-model="createForm.jobName">
                  <option value="">请选择</option><option v-for="job in jobs" :key="job.id" :value="job.name">{{ job.name }}</option>
                </select>
              </div>
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
                <div><label style="font-size:13px;font-weight:500;color:var(--gray-700);margin-bottom:6px;display:block">难度</label><select class="input" v-model="createForm.difficulty"><option>初级</option><option>中级</option><option>高级</option></select></div>
                <div><label style="font-size:13px;font-weight:500;color:var(--gray-700);margin-bottom:6px;display:block">模式</label><select class="input" v-model="createForm.mode"><option value="TEXT">文字</option><option value="VOICE">语音</option><option value="VIDEO">视频</option></select></div>
              </div>
            </div>
            <div style="display:flex;gap:12px;justify-content:flex-end;padding:16px 24px;border-top:1px solid var(--card-border)">
              <button class="btn btn-ghost" @click="showCreateModal = false">取消</button>
              <button class="btn btn-primary" @click="createInterview" style="background:var(--accent);color:white">开始面试</button>
            </div>
          </div>
        </div>
      </transition>
    </teleport>

    <transition name="toast"><div v-if="toast" class="toast">{{ toastMsg }}</div></transition>
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
const toast = ref(false); const toastMsg = ref('')
const interviewCount = ref(0); const avgScore = ref('-')
const jobs = ref([]); const hotQuestions = ref([])
const createForm = ref({ jobName: '', difficulty: '中级', mode: 'TEXT' })

const directions = ['全部', '前端开发', '后端开发', '移动开发', '数据', '测试', '运维', '产品', '设计']
const levels = ['全部', '初级', '中级', '高级']

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

const showT = (m) => { toastMsg.value = m; toast.value = true; setTimeout(() => { toast.value = false }, 2600) }
const startInterview = (job) => { createForm.value.jobName = job.name; showCreateModal.value = true }
const createInterview = async () => {
  if (!createForm.value.jobName) { showT('请选择岗位'); return }
  try {
    const res = await api.post('/interview/create', { userId: userStore.userId, jobName: createForm.value.jobName, mode: createForm.value.mode, difficulty: createForm.value.difficulty })
    showCreateModal.value = false; router.push(`/interview/${res.data.id}`)
  } catch { showT('创建失败') }
}
</script>

<style scoped>
.lobby { display:flex; gap:28px; max-width:1160px; margin:0 auto; align-items:flex-start }

/* ── 左侧面板 ── */
.panel-left { width:200px; position:sticky; top:80px; display:flex; flex-direction:column; gap:12px; flex-shrink:0 }

.pa-avatar {
  width:52px; height:52px; border-radius:50%; margin:0 auto 10px;
  background:linear-gradient(135deg,var(--accent),#92400e); color:white;
  display:flex; align-items:center; justify-content:center; font-size:18px; font-weight:700
}
.pa-name { font-size:15px; font-weight:600; color:var(--gray-800) }
.pa-role { font-size:11px; color:var(--gray-400); margin-bottom:14px }
.pa-stats { display:flex; align-items:center; justify-content:center; gap:16px; padding-top:14px; border-top:1px solid var(--card-border) }
.pa-num { display:block; font-size:18px; font-weight:700; color:var(--accent) }
.pa-lbl { font-size:11px; color:var(--gray-400) }
.pa-dot { width:3px; height:3px; border-radius:50%; background:var(--gray-200) }

.nav { display:flex; flex-direction:column; gap:2px }
.nav-link {
  display:flex; align-items:center; gap:10px; padding:8px 12px; font-size:13px; font-weight:500;
  color:var(--gray-500); border-radius:10px; cursor:pointer; transition:var(--transition); text-decoration:none
}
.nav-link:hover { background:var(--gray-50); color:var(--gray-700) }
.nav-link.active { background:var(--accent-light); color:var(--accent); font-weight:600 }

/* ── 中间 ── */
.panel-center { flex:1; min-width:0 }

.chip {
  display:inline-flex; padding:3px 12px; font-size:12px; font-weight:500;
  color:var(--gray-500); border-radius:var(--radius-pill);
  cursor:pointer; transition:var(--transition); margin:2px
}
.chip:hover { background:var(--gray-100); color:var(--gray-700) }
.chip.active { background:var(--primary); color:white }

.job-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(230px,1fr)); gap:12px }
.job-card { padding:16px; cursor:pointer; transition:var(--transition) }
.job-card:hover { transform:translateY(-2px); box-shadow:var(--shadow-md) }
.job-card:hover svg { color:var(--accent); transform:translateX(4px) }

/* ── 右侧面板 ── */
.panel-right { width:220px; position:sticky; top:80px; display:flex; flex-direction:column; gap:12px; flex-shrink:0 }

.hr {
  width:24px; height:24px; border-radius:7px;
  background:var(--gray-100); color:var(--gray-500);
  display:flex; align-items:center; justify-content:center;
  font-size:11px; font-weight:600; flex-shrink:0
}
.hr.top { background:var(--accent); color:white }

/* ── 弹窗 ── */
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.15); display:flex; align-items:center; justify-content:center; z-index:1000; padding:16px }
.modal-close { width:28px; height:28px; border:none; background:none; border-radius:8px; cursor:pointer; display:flex; align-items:center; justify-content:center; color:var(--gray-400) }
.modal-close:hover { background:var(--gray-100); color:var(--gray-600) }

.toast { position:fixed; bottom:24px; left:50%; transform:translateX(-50%); background:var(--gray-900); color:white; padding:10px 20px; border-radius:var(--radius-pill); font-size:13px; font-weight:500; z-index:2000 }

.modal-enter-active,.modal-leave-active { transition:all 0.2s ease }
.modal-enter-from,.modal-leave-to { opacity:0 }
.modal-enter-from .card,.modal-leave-to .card { transform:scale(0.95) }

@media (max-width:768px) {
  .lobby { flex-direction:column }
  .panel-left,.panel-right { width:100%; position:static }
}
</style>
