<template>
  <div style="max-width:900px;margin:0 auto">
    <div style="margin-bottom:var(--spacing-6)">
      <h2>练习记录</h2>
      <p class="text-muted text-sm" style="margin-top:2px">共 {{ filteredList.length }} 条记录</p>
    </div>

    <div class="stats-row">
      <div class="card" style="padding:var(--spacing-5);text-align:center;border-left:3px solid var(--color-accent)">
        <span class="stat-num" style="color:var(--color-accent)">{{ historyList.length }}</span>
        <span class="stat-lbl">总面试数</span>
      </div>
      <div class="card" style="padding:var(--spacing-5);text-align:center;border-left:3px solid var(--color-success)">
        <span class="stat-num" style="color:var(--color-success)">{{ completedCount }}</span>
        <span class="stat-lbl">已完成</span>
      </div>
      <div class="card" style="padding:var(--spacing-5);text-align:center;border-left:3px solid var(--color-accent)">
        <span class="stat-num" style="color:var(--color-text-primary)">{{ avgScore }}</span>
        <span class="stat-lbl">平均分</span>
      </div>
    </div>

    <div class="card" style="padding:var(--spacing-4);margin-bottom:var(--spacing-5);display:flex;gap:10px;flex-wrap:wrap">
      <select v-model="filterStatus" class="form-select" style="width:130px"><option value="">全部状态</option><option value="FINISHED">已完成</option><option value="RUNNING">进行中</option><option value="CREATED">待开始</option></select>
      <select v-model="filterMode" class="form-select" style="width:130px"><option value="">全部模式</option><option value="TEXT">文字</option><option value="VOICE">语音</option><option value="VIDEO">视频</option></select>
      <select v-model="sortBy" class="form-select" style="width:130px"><option value="time">按时间</option><option value="score">按分数</option></select>
    </div>

    <div style="display:flex;flex-direction:column;gap:var(--spacing-3)">
      <div v-for="item in filteredList" :key="item.id" class="card card--interactive" style="padding:var(--spacing-4);cursor:pointer;display:flex;align-items:center;gap:var(--spacing-4)" @click="viewDetail(item)">
        <span class="status" :class="'status--' + (item.status === 'FINISHED' || item.status === 'COMPLETED' ? 'finished' : item.status === 'RUNNING' || item.status === 'IN_PROGRESS' ? 'running' : 'created')" style="flex-shrink:0">{{ statusText(item.status) }}</span>
        <div style="flex:1;min-width:0">
          <div style="font-size:15px;font-weight:600;color:var(--color-text-primary);margin-bottom:2px">{{ item.jobName }}</div>
          <div class="text-xs text-muted">{{ modeText(item.mode) }} · {{ levelText(item.difficulty) }} · {{ formatDate(item.createTime) }}</div>
        </div>
        <div v-if="item.score != null" class="score-dot" :class="'sd-' + scoreLevel(item.score)">{{ item.score }}</div>
        <div v-else class="score-dot sd-none">-</div>
        <div style="display:flex;gap:6px;flex-shrink:0" @click.stop>
          <button v-if="item.status === 'FINISHED' || item.status === 'COMPLETED'" class="btn btn--primary" @click="viewReport(item)">查看报告</button>
          <button class="btn btn--ghost" @click="confirmDelete(item)">删除</button>
        </div>
      </div>
      <div v-if="filteredList.length === 0" class="empty-state">
        <p>暂无练习记录</p>
        <button class="btn btn--primary" style="margin-top:var(--spacing-4)" @click="$router.push('/lobby')">开始第一场面试</button>
      </div>
    </div>

    <teleport to="body">
      <transition name="fade">
        <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
          <div class="card" style="width:100%;max-width:420px;padding:0;overflow:hidden">
            <div style="display:flex;align-items:center;justify-content:space-between;padding:var(--spacing-5) var(--spacing-6)">
              <h4>面试详情</h4>
              <button class="btn btn--ghost btn--icon" @click="showDetailModal = false">✕</button>
            </div>
            <div style="padding:0 var(--spacing-6) var(--spacing-5)" v-if="selectedItem">
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:var(--spacing-3)">
                <div v-for="d in detailFields" :key="d.key"><span class="text-xs text-muted">{{ d.label }}</span><div style="font-size:13px;font-weight:500;color:var(--color-text-primary)">{{ d.value }}</div></div>
              </div>
            </div>
            <div style="display:flex;gap:10px;justify-content:flex-end;padding:var(--spacing-4) var(--spacing-6);border-top:1px solid var(--color-divider)">
              <button class="btn btn--ghost" @click="showDetailModal = false">关闭</button>
              <button v-if="selectedItem?.status === 'FINISHED' || selectedItem?.status === 'COMPLETED'" class="btn btn--primary" @click="viewReport(selectedItem)">查看报告</button>
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
import { statusText, levelText, modeText, formatDate, scoreLevel } from '@/utils/constants'

const router = useRouter(); const userStore = useUserStore()
const historyList = ref([]); const filterStatus = ref(''); const filterMode = ref(''); const sortBy = ref('time')
const showDetailModal = ref(false); const selectedItem = ref(null)

const completedCount = computed(() => historyList.value.filter(i => i.status === 'FINISHED' || i.status === 'COMPLETED').length)
const avgScore = computed(() => { const s = historyList.value.filter(i => i.score != null); return s.length ? Math.round(s.reduce((a,b)=>a+b.score,0)/s.length) : '-' })
const filteredList = computed(() => {
  let l = [...historyList.value]
  if (filterStatus.value) l = l.filter(i => i.status === filterStatus.value)
  if (filterMode.value) l = l.filter(i => i.mode === filterMode.value)
  if (sortBy.value === 'score') l.sort((a,b) => (b.score||0) - (a.score||0))
  else l.sort((a,b) => new Date(b.createTime) - new Date(a.createTime))
  return l
})
const detailFields = computed(() => {
  const i = selectedItem.value; if (!i) return []
  return [
    { key:'job', label:'岗位', value:i.jobName }, { key:'mode', label:'模式', value:modeText(i.mode) },
    { key:'difficulty', label:'难度', value:levelText(i.difficulty) }, { key:'status', label:'状态', value:statusText(i.status) },
    { key:'created', label:'时间', value:formatDate(i.createTime) }, { key:'score', label:'得分', value:i.score != null ? i.score : '-' }
  ]
})

onMounted(() => loadHistory())
const loadHistory = async () => {
  try {
    const res = await api.get('/interview/list', { params: { userId: userStore.userId || undefined } })
    historyList.value = res.data || []
    await Promise.allSettled(historyList.value.map(async i => {
      if (i.status !== 'FINISHED' && i.status !== 'COMPLETED') return
      try { const r = await api.get(`/report/${i.id}`); if (r.data) i.score = r.data.totalScore } catch {}
    }))
  } catch {}
}
const viewDetail = (item) => { selectedItem.value = item; showDetailModal.value = true }
const viewReport = (item) => { showDetailModal.value = false; router.push(`/report/${item.id}`) }
const confirmDelete = (item) => {
  ElMessageBox.confirm(`确定删除「${item.jobName}」的记录？`,'确认删除',{ confirmButtonText:'确定删除', cancelButtonText:'取消', type:'warning' })
    .then(async () => { await api.delete(`/interview/${item.id}`); ElMessage.success('已删除'); loadHistory() }).catch(() => {})
}
</script>

<style scoped>
.stats-row { display:grid; grid-template-columns:repeat(3,1fr); gap:var(--spacing-4); margin-bottom:var(--spacing-5); }
.stat-num { display:block; font-family:var(--font-display); font-size:26px; font-weight:700; line-height:1; }
.stat-lbl { font-size:12px; color:var(--color-text-secondary); margin-top:4px; }

.score-dot { width:36px; height:36px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:13px; font-weight:700; flex-shrink:0; }
.sd-excellent { background:var(--color-success-light); color:var(--color-success); }
.sd-good { background:var(--color-accent-light); color:var(--color-accent); }
.sd-average { background:#FEF3C7; color:#B45309; }
.sd-poor { background:var(--color-danger-light); color:var(--color-danger); }
.sd-none { background:var(--color-surface-subtle); color:var(--color-text-placeholder); }

.modal-overlay { position:fixed; inset:0; background:rgba(28,25,23,0.3); display:flex; align-items:center; justify-content:center; z-index:200; padding:var(--spacing-4); }

@media (max-width:768px) { .stats-row { grid-template-columns:1fr; } }
</style>
