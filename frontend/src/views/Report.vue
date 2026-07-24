<template>
  <div class="report">
    <div class="report-header">
      <div class="ph-left">
        <button class="btn-back" @click="$router.push('/lobby')">← 返回</button>
        <h1>面试报告</h1>
      </div>
    </div>

    <div class="report-content">
      <div class="score-card">
        <div class="total-score">
          <div class="score-circle">
            <span class="score-number">{{ report.totalScore || 0 }}</span>
            <span class="score-label">综合评分</span>
          </div>
        </div>

        <div class="score-details">
          <div class="score-item">
            <span class="label">专业能力</span>
            <el-progress :percentage="report.professionalScore || 0" :stroke-width="8" />
          </div>
          <div class="score-item">
            <span class="label">表达能力</span>
            <el-progress :percentage="report.communicationScore || 0" :stroke-width="8" />
          </div>
          <div class="score-item">
            <span class="label">逻辑能力</span>
            <el-progress :percentage="report.logicScore || 0" :stroke-width="8" />
          </div>
        </div>
      </div>

      <div class="detail-card">
        <h3>面试总结</h3>
        <p>{{ report.summary || '暂无总结' }}</p>
      </div>

      <div class="detail-card">
        <h3>改进建议</h3>
        <p>{{ report.suggestion || '暂无建议' }}</p>
      </div>

      <div class="detail-card">
        <h3>面试记录</h3>
        <div class="record-list">
          <div
            v-for="(record, index) in records"
            :key="index"
            class="record-item"
          >
            <div class="record-round">第 {{ index + 1 }} 轮</div>
            <div class="record-content">
              <div class="question">
                <strong>问题：</strong>{{ record.question }}
              </div>
              <div class="answer">
                <strong>回答：</strong>{{ record.answer }}
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
  } catch (error) {
    console.error(error)
  }
}

const loadRecords = async () => {
  try {
    const res = await api.get(`/evaluation/task/${taskId}`)
    records.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const goBack = () => {
  router.push('/lobby')
}
</script>

<style scoped>
.report {
  padding: 20px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.ph-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.report-header h1 {
  font-size: 24px;
  color: #1A1A1A;
  margin: 0;
}

.btn-back {
  background: none;
  border: 1px solid #C4C4C4;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-back:hover {
  border-color: #C74634;
  color: #C74634;
}

.report-content {
  max-width: 800px;
  margin: 0 auto;
}

.score-card {
  background: #fff;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 20px;
  display: flex;
  gap: 40px;
}

.total-score {
  flex-shrink: 0;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #C74634 0%, #E85D4A 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.score-number {
  font-size: 36px;
  font-weight: bold;
}

.score-label {
  font-size: 12px;
  opacity: 0.9;
}

.score-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.score-item .label {
  width: 80px;
  font-size: 14px;
  color: #666;
}

.score-item .el-progress {
  flex: 1;
}

.detail-card {
  background: #fff;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
}

.detail-card h3 {
  font-size: 16px;
  color: #1A1A1A;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #E8E8E8;
}

.detail-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-item {
  background: #F9F9F9;
  border-radius: 8px;
  padding: 16px;
}

.record-round {
  font-size: 12px;
  color: #C74634;
  font-weight: 500;
  margin-bottom: 8px;
}

.record-content {
  font-size: 14px;
  line-height: 1.6;
}

.question, .answer {
  margin-bottom: 8px;
}

.question strong, .answer strong {
  color: #333;
}
</style>