<template>
  <div class="page" style="max-width:900px">
    <div style="margin-bottom:24px">
      <h1 class="page-title">练习记录</h1>
      <p style="font-size:13px;color:var(--gray-400);margin-top:2px">共 {{ filteredList.length }} 条记录</p>
    </div>

    <!-- 统计色块 -->
    <div class="stats">
      <div class="card" style="padding:18px 16px;text-align:center;border-left:3px solid var(--accent)">
        <div style="font-size:26px;font-weight:700;color:var(--accent)">{{ historyList.length }}</div>
        <div style="font-size:12px;color:var(--gray-400);margin-top:2px">总面试数</div>
      </div>
      <div class="card" style="padding:18px 16px;text-align:center;border-left:3px solid #22c55e">
        <div style="font-size:26px;font-weight:700;color:#22c55e">{{ completedCount }}</div>
        <div style="font-size:12px;color:var(--gray-400);margin-top:2px">已完成</div>
      </div>
      <div class="card" style="padding:18px 16px;text-align:center;border-left:3px solid var(--primary)">
        <div style="font-size:26px;font-weight:700;color:var(--primary)">{{ avgScore }}</div>
        <div style="font-size:12px;color:var(--gray-400);margin-top:2px">平均分</div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="card" style="padding:12px 16px;margin-bottom:20px;display:flex;gap:10px;flex-wrap:wrap">
      <select v-model="filterStatus" class="input" style="width:130px"><option value="">全部状态</option><option value="FINISHED">已完成</option><option value="RUNNING">进行中</option><option value="CREATED">待开始</option></select>
      <select v-model="filterMode" class="input" style="width:130px"><option value="">全部模式</option><option value="TEXT">文字</option><option value="VOICE">语音</option><option value="VIDEO">视频</option></select>
      <select v-model="sortBy" class="input" style="width:130px"><option value="time">按时间</option><option value="score">按分数</option></select>
    </div>

    <!-- 列表 -->
    <div class="list">
      <div v-for="item in filteredList" :key="item.id" class="card list-item" @click="viewDetail(item)">
        <div class="li-status">
          <span class="status" :class="'status-' + (item.status === 'FINISHED' || item.status === 'COMPLETED' ? 'finished' : item.status === 'RUNNING' || item.status === 'IN_PROGRESS' ? 'running' : 'created')">{{ statusText(item.status) }}</span>
        </div>
        <div class="li-body">
          <div class="li-title">{{ item.jobName }}</div>
          <div class="li-meta">
            <span>{{ modeText(item.mode) }}</span>
            <span class="li-dot"></span>
            <span style="font-weight:500">{{ levelText(item.difficulty) }}</span>
            <span class="li-dot"></span>
            <span>{{ formatDate(item.createTime) }}</span>
          </div>
        </div>
        <div class="li-score">
          <div v-if="item.score != null" class="sc" :class="scoreLevel(item.score)">{{ item.score }}</div>
          <div v-else class="sc no">-</div>
        </div>
        <div class="li-actions" @click.stop>
          <button v-if="item.status === 'FINISHED' || item.status === 'COMPLETED'" class="btn btn-sm btn-primary" @click="viewReport(item)" style="background:var(--accent);color:white">报告</button>
          <button v-else-if="item.status === 'RUNNING' || item.status === 'IN_PROGRESS'" class="btn btn-sm btn-outline" @click="continueInterview(item)">继续</button>
          <button v-else class="btn btn-sm btn-outline" @click="continueInterview(item)">开始</button>
          <button class="btn btn-sm btn-ghost" @click="confirmDelete(item)">删除</button>
        </div>
      </div>
      <div v-if="filteredList.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
        <p>暂无练习记录</p>
        <button class="btn btn-primary" @click="$router.push('/lobby')" style="background:var(--accent);color:white">开始面试</button>
      </div>
    </div>

    <!-- 弹窗 -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
          <div class="card" style="width:100%;max-width:440px;padding:0;overflow:hidden">
            <div style="display:flex;align-items:center;justify-content:space-between;padding:18px 22px;border-bottom:1px solid var(--card-border)">
              <h3 style="font-size:15px;font-weight:600;color:var(--gray-900)">面试详情</h3>
              <button class="modal-close" @click="showDetailModal = false"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px"><path d="M18 6L6 18M6 6l12 12"/></svg></button>
            </div>
            <div style="padding:18px 22px" v-if="selectedItem">
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px">
                <div v-for="d in detailFields" :key="d.key" style="display:flex;flex-direction:column;gap:2px">
                  <span style="font-size:11px;color:var(--gray-400)">{{ d.label }}</span>
                  <span style="font-size:13px;font-weight:500;color:var(--gray-800)">{{ d.value }}</span>
                </div>
              </div>
            </div>
            <div style="display:flex;gap:10px;justify-content:flex-end;padding:14px 22px;border-top:1px solid var(--card-border)">
              <button class="btn btn-ghost btn-sm" @click="showDetailModal = false">关闭</button>
              <button v-if="selectedItem?.status === 'FINISHED' || selectedItem?.status === 'COMPLETED'" class="btn btn-primary btn-sm" @click="viewReport(selectedItem)" style="background:var(--accent);color:white">查看报告</button>
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
import { statusText, statusClass, levelText, modeText, formatDate, scoreLevel } from '@/utils/constants'

const router = useRouter(); const userStore = useUserStore()
const historyList = ref([]); const filterStatus = ref('')
const filterMode = ref(''); const sortBy = ref('time')
const showDetailModal = ref(false); const selectedItem = ref(null)

const completedCount = computed(() => historyList.value.filter(i => i.status === 'FINISHED' || i.status === 'COMPLETED').length)
const avgScore = computed(() => {
  const s = historyList.value.filter(i => i.score != null)
  return s.length ? Math.round(s.reduce((a, b) => a + b.score, 0) / s.length) : '-'
})
const filteredList = computed(() => {
  let l = [...historyList.value]
  if (filterStatus.value) l = l.filter(i => i.status === filterStatus.value)
  if (filterMode.value) l = l.filter(i => i.mode === filterMode.value)
  if (sortBy.value === 'score') l.sort((a, b) => (b.score || 0) - (a.score || 0))
  else l.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  return l
})

const detailFields = computed(() => {
  const i = selectedItem.value; if (!i) return []
  return [
    { key: 'job', label: '岗位', value: i.jobName },
    { key: 'mode', label: '模式', value: modeText(i.mode) },
    { key: 'difficulty', label: '难度', value: levelText(i.difficulty) },
    { key: 'status', label: '状态', value: statusText(i.status) },
    { key: 'created', label: '创建时间', value: formatDate(i.createTime) },
    { key: 'score', label: '得分', value: i.score != null ? i.score : '-' },
  ]
})

onMounted(() => loadHistory())
const loadHistory = async () => {
  try {
    const res = await api.get('/interview/list', { params: { userId: userStore.userId || undefined } })
    historyList.value = res.data || []
    await Promise.allSettled(historyList.value.map(async (item) => {
      if (item.status !== 'FINISHED' && item.status !== 'COMPLETED') return
      try { const r = await api.get(`/report/${item.id}`); if (r.data) item.score = r.data.totalScore } catch {}
    }))
  } catch (e) { console.error(e) }
}

const viewDetail = (item) => { selectedItem.value = item; showDetailModal.value = true }
const viewReport = (item) => { showDetailModal.value = false; router.push(`/report/${item.id}`) }
const continueInterview = (item) => { router.push(`/interview/${item.id}`) }
const confirmDelete = (item) => {
  ElMessageBox.confirm(`确定删除「${item.jobName}」的记录？`, '确认', { type: 'warning' }).then(async () => {
    await api.delete(`/interview/${item.id}`); ElMessage.success('已删除'); loadHistory()
  }).catch(() => {})
}
</script>

<style scoped>
.stats { display:grid; grid-template-columns:repeat(3,1fr); gap:12px; margin-bottom:20px }

.list { display:flex; flex-direction:column; gap:10px }
.list-item {
  display:flex; align-items:center; gap:16px; padding:16px 20px;
  cursor:pointer; transition:var(--transition)
}
.list-item:hover { transform:translateY(-1px); box-shadow:var(--shadow-md) }

.li-status { flex-shrink:0 }
.li-body { flex:1; min-width:0 }
.li-title { font-size:14px; font-weight:600; color:var(--gray-800); margin-bottom:3px }
.li-meta { display:flex; align-items:center; gap:6px; font-size:12px; color:var(--gray-400) }
.li-dot { width:3px; height:3px; border-radius:50%; background:var(--gray-300) }

.li-score { flex-shrink:0 }
.sc {
  width:40px; height:40px; border-radius:50%;
  display:flex; align-items:center; justify-content:center;
  font-size:13px; font-weight:700
}
.sc.excellent { background:rgba(34,197,94,0.12); color:#22c55e }
.sc.good { background:var(--accent-light); color:var(--accent) }
.sc.average { background:rgba(234,179,8,0.12); color:#ca8a04 }
.sc.poor { background:rgba(239,68,68,0.12); color:#ef4444 }
.sc.no { background:var(--gray-100); color:var(--gray-300) }

.li-actions { display:flex; gap:6px; flex-shrink:0 }

/* ── 弹窗 ── */
.modal-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.15); display:flex; align-items:center; justify-content:center; z-index:1000; padding:16px }
.modal-close { width:28px; height:28px; border:none; background:none; border-radius:8px; cursor:pointer; display:flex; align-items:center; justify-content:center; color:var(--gray-400) }
.modal-close:hover { background:var(--gray-100); color:var(--gray-600) }

.modal-enter-active,.modal-leave-active { transition:all 0.2s ease }
.modal-enter-from,.modal-leave-to { opacity:0 }
.modal-enter-from .card,.modal-leave-to .card { transform:scale(0.95) }

@media (max-width:768px) {
  .stats { grid-template-columns:1fr }
  .list-item { flex-wrap:wrap }
  .li-actions { width:100%; justify-content:flex-end }
}
</style>
