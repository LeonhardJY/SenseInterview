<template>
  <div class="report-page">
    <div class="page-header">
      <div class="page-header-left">
        <button class="btn btn-ghost btn-back" @click="$router.push('/lobby')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          返回
        </button>
        <div>
          <h1 class="page-title">面试报告</h1>
          <p class="page-subtitle">面试表现分析与评价</p>
        </div>
      </div>
    </div>

    <div class="report-content">
      <!-- 评分卡片 -->
      <div class="score-card">
        <div class="score-circle">
          <div class="score-inner">
            <span class="score-number">{{ report.totalScore || 0 }}</span>
            <span class="score-label">综合评分</span>
          </div>
        </div>
        <div class="score-details">
          <div class="score-item">
            <div class="score-item-header">
              <span class="score-item-label">专业能力</span>
              <span class="score-item-value">{{ report.professionalScore || 0 }}</span>
            </div>
            <div class="score-bar"><div class="score-bar-fill" :style="{ width: (report.professionalScore || 0) + '%' }"></div></div>
          </div>
          <div class="score-item">
            <div class="score-item-header">
              <span class="score-item-label">表达能力</span>
              <span class="score-item-value">{{ report.communicationScore || 0 }}</span>
            </div>
            <div class="score-bar"><div class="score-bar-fill" :style="{ width: (report.communicationScore || 0) + '%' }"></div></div>
          </div>
          <div class="score-item">
            <div class="score-item-header">
              <span class="score-item-label">逻辑能力</span>
              <span class="score-item-value">{{ report.logicScore || 0 }}</span>
            </div>
            <div class="score-bar"><div class="score-bar-fill" :style="{ width: (report.logicScore || 0) + '%' }"></div></div>
          </div>
        </div>
      </div>

      <!-- 总结 -->
      <div class="detail-card">
        <h3 class="detail-title">面试总结</h3>
        <p class="detail-text">{{ report.summary || '暂无总结' }}</p>
      </div>

      <!-- 建议 -->
      <div class="detail-card">
        <h3 class="detail-title">改进建议</h3>
        <p class="detail-text">{{ report.suggestion || '暂无建议' }}</p>
      </div>

      <!-- 面试记录 -->
      <div class="detail-card" v-if="records.length > 0">
        <h3 class="detail-title">面试记录</h3>
        <div class="record-list">
          <div v-for="(record, index) in records" :key="index" class="record-item">
            <div class="record-round">第 {{ index + 1 }} 轮</div>
            <div class="record-content">
              <div class="record-question">
                <span class="record-label">问题</span>
                <p>{{ record.question }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const taskId = route.params.taskId

const report = ref({})
const records = ref([])

onMounted(() => {
  loadReport()
  loadRecords()
})

const loadReport = async () => {
  try {
    const res = await api.get(`/report/${taskId}`)
    report.value = res.data || {}
  } catch (e) { console.error(e) }
}

const loadRecords = async () => {
  try {
    const res = await api.get(`/interview/records/${taskId}`)
    records.value = res.data || []
  } catch (e) { console.error(e) }
}
</script>

<style scoped>
.report-page { max-width: 800px; margin: 0 auto; }

.score-card {
  background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius);
  padding: var(--space-8); margin-bottom: var(--space-5);
  display: flex; gap: var(--space-8); align-items: center;
}

.score-circle {
  width: 140px; height: 140px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 14px rgba(79, 70, 229, 0.3);
}

.score-inner { text-align: center; color: white; }
.score-number { display: block; font-size: 36px; font-weight: 700; line-height: 1; }
.score-label { font-size: 13px; opacity: 0.9; }

.score-details { flex: 1; display: flex; flex-direction: column; gap: var(--space-5); }

.score-item-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.score-item-label { font-size: 13px; color: var(--gray-600); }
.score-item-value { font-size: 14px; font-weight: 600; color: var(--gray-900); }

.score-bar { height: 8px; background: var(--gray-100); border-radius: 4px; overflow: hidden; }
.score-bar-fill { height: 100%; background: var(--primary); border-radius: 4px; transition: width 0.5s ease; }

.detail-card {
  background: white; border: 1px solid var(--border-color); border-radius: var(--border-radius);
  padding: var(--space-6); margin-bottom: var(--space-5);
}

.detail-title {
  font-size: 16px; font-weight: 600; color: var(--gray-900);
  margin-bottom: var(--space-4); padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-color);
}

.detail-text { font-size: 14px; color: var(--gray-600); line-height: 1.8; }

.record-list { display: flex; flex-direction: column; gap: var(--space-3); }

.record-item {
  background: var(--gray-50); border-radius: var(--border-radius-sm); padding: var(--space-4);
}

.record-round { font-size: 12px; font-weight: 600; color: var(--primary); margin-bottom: var(--space-2); }

.record-label { font-size: 12px; color: var(--gray-500); margin-bottom: 4px; display: block; }
.record-content p { font-size: 14px; color: var(--gray-700); line-height: 1.6; }
</style>