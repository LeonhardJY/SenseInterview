<template>
  <div style="max-width:800px;margin:0 auto">
    <div style="display:flex;align-items:center;gap:var(--spacing-4);margin-bottom:var(--spacing-6)">
      <button class="btn btn--ghost btn--sm" @click="$router.push('/lobby')" style="font-size:16px">←</button>
      <div>
        <h2>面试报告</h2>
        <p class="text-muted text-sm" v-if="report.jobName">{{ report.jobName }} · {{ modeText(report.mode) }} · {{ report.difficulty }}</p>
      </div>
    </div>

    <div v-if="report" style="display:flex;flex-direction:column;gap:var(--spacing-4)">
      <!-- 信息条 -->
      <div class="card" style="padding:var(--spacing-4) var(--spacing-5);display:flex;gap:var(--spacing-6)">
        <div><span class="text-xs text-muted">用时</span><div style="font-size:14px;font-weight:600;color:var(--color-text-primary)">{{ report.duration || '-' }}</div></div>
        <div><span class="text-xs text-muted">轮次</span><div style="font-size:14px;font-weight:600;color:var(--color-text-primary)">{{ report.totalRounds || 0 }} 轮</div></div>
      </div>

      <!-- 评分 -->
      <div class="card" style="padding:var(--spacing-5)">
        <p class="eyebrow" style="margin-bottom:var(--spacing-4)">Scores</p>
        <div style="display:flex;gap:var(--spacing-6);align-items:center">
          <div style="width:90px;height:90px;border-radius:50%;background:linear-gradient(135deg,var(--color-accent),#E8A87C);display:flex;flex-direction:column;align-items:center;justify-content:center;color:white;flex-shrink:0">
            <span style="font-family:var(--font-display);font-size:28px;font-weight:700;line-height:1">{{ report.totalScore || '-' }}</span>
            <span style="font-size:10px;opacity:0.85">综合</span>
          </div>
          <div style="flex:1;display:flex;flex-direction:column;gap:8px">
            <div v-if="report.professionalScore != null" class="sr"><span class="srl">专业能力</span><div class="srb"><div class="srf" style="width:{{ report.professionalScore }}%;background:var(--color-accent)"></div></div><span class="srv">{{ report.professionalScore }}</span></div>
            <div v-if="report.communicationScore != null" class="sr"><span class="srl">表达能力</span><div class="srb"><div class="srf" style="width:{{ report.communicationScore }}%"></div></div><span class="srv">{{ report.communicationScore }}</span></div>
            <div v-if="report.logicScore != null" class="sr"><span class="srl">逻辑能力</span><div class="srb"><div class="srf" style="width:{{ report.logicScore }}%"></div></div><span class="srv">{{ report.logicScore }}</span></div>
            <div v-if="report.emotionScore != null" class="sr"><span class="srl">情绪控制</span><div class="srb"><div class="srf" style="width:{{ report.emotionScore }}%;background:var(--color-accent)"></div></div><span class="srv">{{ report.emotionScore }}</span></div>
            <div v-if="report.confidenceScore != null" class="sr"><span class="srl">自信程度</span><div class="srb"><div class="srf" style="width:{{ report.confidenceScore }}%"></div></div><span class="srv">{{ report.confidenceScore }}</span></div>
          </div>
        </div>
      </div>

      <!-- 情绪分析 -->
      <div class="card" style="padding:var(--spacing-5)" v-if="report.emotionSummary">
        <p class="eyebrow" style="margin-bottom:var(--spacing-4)">Emotion</p>
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:var(--spacing-4)">
          <div style="display:flex;align-items:center;gap:var(--spacing-3)">
            <span style="font-size:32px">{{ emotionIcon(report.emotionSummary.dominantEmotion) }}</span>
            <div>
              <div style="font-family:var(--font-display);font-size:20px;font-weight:600;color:var(--color-text-primary)">{{ report.emotionSummary.emotionLabel }}</div>
              <div class="text-xs text-muted">分析 {{ report.emotionSummary.totalFrames }} 帧 · {{ report.emotionSummary.emotionChanges }} 次波动</div>
            </div>
          </div>
        </div>
        <div v-if="report.emotionSummary.distribution?.length" style="display:flex;flex-direction:column;gap:6px">
          <div v-for="d in report.emotionSummary.distribution" :key="d.emotion" style="display:flex;align-items:center;gap:10px">
            <span class="text-xs" style="width:36px;color:var(--color-text-secondary)">{{ d.label }}</span>
            <div style="flex:1;height:8px;background:var(--color-surface-subtle);border-radius:4px;overflow:hidden"><div style="height:100%;border-radius:4px;transition:width 0.5s" :style="{ width: d.percentage + '%', background: emotionColor(d.emotion) }"></div></div>
            <span class="text-xs" style="width:40px;text-align:right;color:var(--color-text-secondary)">{{ d.percentage }}%</span>
          </div>
        </div>
      </div>

      <!-- 总结 -->
      <div class="card" style="padding:var(--spacing-5)" v-if="report.summary">
        <p class="eyebrow" style="margin-bottom:var(--spacing-3)">Summary</p>
        <p style="font-size:14px;color:var(--color-text-body);line-height:1.8">{{ report.summary }}</p>
      </div>

      <div class="card" style="padding:var(--spacing-5)" v-if="report.suggestion">
        <p class="eyebrow" style="margin-bottom:var(--spacing-3)">Suggestion</p>
        <p style="font-size:14px;color:var(--color-text-body);line-height:1.8">{{ report.suggestion }}</p>
      </div>

      <!-- 问答 -->
      <div class="card" style="padding:var(--spacing-5)" v-if="report.records?.length">
        <p class="eyebrow" style="margin-bottom:var(--spacing-4)">Transcript</p>
        <div style="display:flex;flex-direction:column;gap:var(--spacing-3)">
          <div v-for="(qa, i) in report.records" :key="i" style="padding:var(--spacing-4);background:var(--color-surface-subtle);border-radius:var(--radius-sm)">
            <div class="text-xs" style="color:var(--color-accent);font-weight:600;margin-bottom:var(--spacing-2)">第 {{ qa.round }} 轮</div>
            <div style="margin-bottom:var(--spacing-2)"><span class="text-xs" style="color:var(--color-accent);font-weight:500">问题</span><p style="font-size:13px;color:var(--color-text-body);margin:2px 0 0">{{ qa.question }}</p></div>
            <div v-if="qa.answer"><span class="text-xs" style="color:var(--color-success);font-weight:500">回答</span><p style="font-size:13px;color:var(--color-text-body);margin:2px 0 0">{{ qa.answer }}</p></div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-if="!report.totalScore && !report.emotionSummary"><p>报告生成中</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { modeText } from '@/utils/constants'

const route = useRoute(); const taskId = route.params.taskId
const report = ref({})

const emotionIcon = (e) => { const m = { happy:'😊',sad:'😔',angry:'😠',surprise:'😮',fear:'😨',disgust:'😣',neutral:'😐' }; return m[e] || '❓' }
const emotionColor = (e) => { const m = { happy:'#34d399',sad:'#60a5fa',angry:'#f87171',surprise:'#fbbf24',fear:'#f87171',disgust:'#a78bfa',neutral:'#94a3b8' }; return m[e] || '#94a3b8' }

onMounted(async () => {
  try { const res = await api.get(`/report/comprehensive/${taskId}`); report.value = res.data || {} } catch {}
})
</script>

<style scoped>
.sr { display:flex; align-items:center; gap:8px; }
.srl { font-size:12px; color:var(--color-text-secondary); width:56px; flex-shrink:0; }
.srb { flex:1; height:6px; background:var(--color-surface-subtle); border-radius:999px; overflow:hidden; }
.srf { height:100%; border-radius:999px; transition:width 0.5s; }
.srv { font-size:12px; font-weight:600; color:var(--color-text-primary); width:24px; text-align:right; }
</style>
