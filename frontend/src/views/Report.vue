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
          <p class="page-subtitle" v-if="report.jobName">{{ report.jobName }} · {{ modeText(report.mode) }} · {{ report.difficulty }}</p>
        </div>
      </div>
    </div>

    <div class="report-content" v-if="report">
      <!-- 基础信息条 -->
      <div class="info-bar">
        <div class="info-item">
          <span class="info-label">用时</span>
          <span class="info-value">{{ report.duration || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">轮次</span>
          <span class="info-value">{{ report.totalRounds || 0 }} 轮</span>
        </div>
      </div>

      <!-- 评分区域：文本维度 + 情绪维度 -->
      <div class="score-section">
        <h3 class="section-title">评分总览</h3>
        <div class="score-grid">
          <div class="score-card-main">
            <div class="score-circle">
              <span class="score-num">{{ report.totalScore || '-' }}</span>
              <span class="score-lbl">综合</span>
            </div>
          </div>
          <div class="score-list">
            <div class="score-row" v-if="report.professionalScore != null">
              <span class="score-label">专业能力</span>
              <div class="score-bar"><div class="bar-fill professional" :style="{ width: report.professionalScore + '%' }"></div></div>
              <span class="score-val">{{ report.professionalScore }}</span>
            </div>
            <div class="score-row" v-if="report.communicationScore != null">
              <span class="score-label">表达能力</span>
              <div class="score-bar"><div class="bar-fill communication" :style="{ width: report.communicationScore + '%' }"></div></div>
              <span class="score-val">{{ report.communicationScore }}</span>
            </div>
            <div class="score-row" v-if="report.logicScore != null">
              <span class="score-label">逻辑能力</span>
              <div class="score-bar"><div class="bar-fill logic" :style="{ width: report.logicScore + '%' }"></div></div>
              <span class="score-val">{{ report.logicScore }}</span>
            </div>
            <div class="score-row" v-if="report.emotionScore != null">
              <span class="score-label">情绪控制</span>
              <div class="score-bar"><div class="bar-fill emotion" :style="{ width: report.emotionScore + '%' }"></div></div>
              <span class="score-val">{{ report.emotionScore }}</span>
            </div>
            <div class="score-row" v-if="report.confidenceScore != null">
              <span class="score-label">自信程度</span>
              <div class="score-bar"><div class="bar-fill confidence" :style="{ width: report.confidenceScore + '%' }"></div></div>
              <span class="score-val">{{ report.confidenceScore }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 情绪分析 -->
      <div class="block" v-if="report.emotionSummary">
        <h3 class="section-title">情绪分析</h3>
        <div class="emotion-header">
          <div class="emotion-main">
            <span class="emotion-icon-lg">{{ emotionIcon(report.emotionSummary.dominantEmotion) }}</span>
            <div>
              <div class="emotion-dominant">{{ report.emotionSummary.emotionLabel }}</div>
              <div class="emotion-sub">主要情绪 · 分析 {{ report.emotionSummary.totalFrames }} 帧</div>
            </div>
          </div>
          <div class="emotion-changes">
            <span class="change-count">{{ report.emotionSummary.emotionChanges }}</span>
            <span class="change-label">次情绪波动</span>
          </div>
        </div>
        <div class="emotion-dist" v-if="report.emotionSummary.distribution?.length">
          <div v-for="d in report.emotionSummary.distribution" :key="d.emotion" class="dist-row">
            <span class="dist-label">{{ d.label }}</span>
            <div class="dist-bar"><div class="dist-fill" :class="'emotion-' + d.emotion" :style="{ width: d.percentage + '%' }"></div></div>
            <span class="dist-pct">{{ d.percentage }}%</span>
          </div>
        </div>
      </div>

      <!-- 总结 -->
      <div class="block" v-if="report.summary">
        <h3 class="section-title">面试总结</h3>
        <p class="card-text">{{ report.summary }}</p>
      </div>

      <!-- 改进建议 -->
      <div class="block" v-if="report.suggestion">
        <h3 class="section-title">改进建议</h3>
        <p class="card-text">{{ report.suggestion }}</p>
      </div>

      <!-- 问答记录 -->
      <div class="block" v-if="report.records?.length">
        <h3 class="section-title">问答记录</h3>
        <div class="qa-list">
          <div v-for="(qa, i) in report.records" :key="i" class="qa-item">
            <div class="qa-round">第 {{ qa.round }} 轮</div>
            <div class="qa-block">
              <span class="qa-label q">问题</span>
              <p class="qa-text">{{ qa.question }}</p>
            </div>
            <div class="qa-block" v-if="qa.answer">
              <span class="qa-label a">回答</span>
              <p class="qa-text">{{ qa.answer }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!report.totalScore && !report.emotionSummary">
        <p>报告生成中，请稍后再查看</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'
import { modeText } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const taskId = route.params.taskId

const report = ref({})

const emotionIcon = (emotion) => {
  const map = { happy: '😊', sad: '😔', angry: '😠', surprise: '😮', fear: '😨', disgust: '😣', neutral: '😐' }
  return map[emotion] || '❓'
}

onMounted(async () => {
  try {
    const res = await api.get(`/report/comprehensive/${taskId}`)
    report.value = res.data || {}
  } catch (e) {
    console.error('加载报告失败:', e)
  }
})
</script>

<style scoped>
.report-page { max-width: 800px; margin: 0 auto; }

/* ── 头部 ── */
.page-header { margin-bottom:24px }
.page-header-left { display:flex; align-items:center; gap:16px }

/* ── 信息条 ── */
.info-bar {
  display:flex; gap:24px; padding:16px 20px; margin-bottom:16px;
  background:var(--glass); backdrop-filter:blur(16px);
  border:1px solid var(--glass-border); border-radius:var(--radius)
}
.info-item { display:flex; align-items:center; gap:6px }
.info-label { font-size:12px; color:var(--gray-400) }
.info-value { font-size:14px; font-weight:600; color:var(--gray-800) }

/* ── 通用区块 ── */
.block {
  padding:24px; margin-bottom:16px;
  background:var(--glass); backdrop-filter:blur(16px);
  border:1px solid var(--glass-border); border-radius:var(--radius)
}
.block-title {
  font-size:15px; font-weight:600; color:var(--gray-900);
  margin-bottom:16px; padding-bottom:12px;
  border-bottom:1px solid rgba(0,0,0,0.04)
}
.block-text { font-size:14px; color:var(--gray-600); line-height:1.8 }

/* ── 评分区域 ── */
.score-grid { display:flex; gap:24px; align-items:center }
.score-ring {
  width:100px; height:100px; border-radius:50%; flex-shrink:0;
  background:linear-gradient(135deg, var(--primary), var(--primary-soft));
  display:flex; flex-direction:column; align-items:center; justify-content:center; color:white;
  box-shadow:0 4px 16px rgba(67,56,202,0.3)
}
.score-num { font-size:28px; font-weight:700; line-height:1 }
.score-ring-label { font-size:11px; opacity:0.9 }

.score-list { flex:1; display:flex; flex-direction:column; gap:12px }
.score-row { display:flex; align-items:center; gap:12px }
.score-row-label { font-size:12px; color:var(--gray-500); width:60px; flex-shrink:0 }
.score-track { flex:1; height:8px; background:var(--gray-100); border-radius:100px; overflow:hidden }
.score-fill { height:100%; border-radius:100px; transition:width 0.6s ease }
.score-fill.professional { background:linear-gradient(90deg,var(--primary),var(--primary-soft)) }
.score-fill.communication { background:linear-gradient(90deg,#06b6d4,#22d3ee) }
.score-fill.logic { background:linear-gradient(90deg,#8b5cf6,#a78bfa) }
.score-fill.emotion { background:linear-gradient(90deg,#f59e0b,#fbbf24) }
.score-fill.confidence { background:linear-gradient(90deg,#10b981,#34d399) }
.score-row-val { font-size:13px; font-weight:600; color:var(--gray-700); width:28px; text-align:right }

/* ── 情绪分析 ── */
.emotion-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px }
.emotion-main { display:flex; align-items:center; gap:12px }
.emotion-icon-lg { font-size:32px }
.emotion-dominant { font-size:18px; font-weight:600; color:var(--gray-900) }
.emotion-sub { font-size:12px; color:var(--gray-400) }
.emotion-changes { text-align:center }
.change-count { display:block; font-size:24px; font-weight:700; color:var(--primary) }
.change-label { font-size:11px; color:var(--gray-400) }

.emotion-dist { display:flex; flex-direction:column; gap:8px }
.dist-row { display:flex; align-items:center; gap:12px }
.dist-label { font-size:12px; color:var(--gray-500); width:36px }
.dist-track { flex:1; height:10px; background:var(--gray-100); border-radius:100px; overflow:hidden }
.dist-fill { height:100%; border-radius:100px }
.dist-pct { font-size:12px; color:var(--gray-400); width:44px; text-align:right }

/* ── 问答记录 ── */
.qa-list { display:flex; flex-direction:column; gap:10px }
.qa-item {
  background:rgba(255,255,255,0.5); border-radius:var(--radius-sm);
  padding:16px; border:1px solid rgba(255,255,255,0.3)
}
.qa-round { font-size:12px; font-weight:600; color:var(--primary); margin-bottom:8px }
.qa-block { margin-bottom:8px }
.qa-block:last-child { margin-bottom:0 }
.qa-label { font-size:11px; font-weight:600; padding:1px 10px; border-radius:100px; display:inline-block; margin-bottom:4px }
.qa-label.q { color:var(--primary); background:var(--primary-light) }
.qa-label.a { color:#059669; background:var(--emerald-light) }
.qa-text { font-size:14px; color:var(--gray-700); line-height:1.6; margin:0 }
</style>
