<template>
  <div class="dashboard">
    <h2 class="page-title">数据看板</h2>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon users">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4-4v2"/><circle cx="9" cy="7" r="4"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ userStats.totalUsers || 0 }}</span>
          <span class="stat-label">总用户数</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon interviews">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ interviewStats.totalInterviews || 0 }}</span>
          <span class="stat-label">总面试数</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon questions">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ questionStats.totalQuestions || 0 }}</span>
          <span class="stat-label">题库数量</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ completionRate }}%</span>
          <span class="stat-label">完成率</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 面试趋势图 -->
      <div class="chart-card">
        <h3 class="card-title">近7天面试趋势</h3>
        <div ref="trendChart" class="chart-container"></div>
      </div>

      <!-- 岗位热度图 -->
      <div class="chart-card">
        <h3 class="card-title">岗位热度排行</h3>
        <div ref="jobChart" class="chart-container"></div>
      </div>

      <!-- 分数分布图 -->
      <div class="chart-card">
        <h3 class="card-title">分数分布</h3>
        <div ref="scoreChart" class="chart-container"></div>
      </div>

      <!-- 详细统计 -->
      <div class="chart-card">
        <h3 class="card-title">详细统计</h3>
        <div class="detail-list">
          <div class="detail-item">
            <span class="item-label">管理员</span>
            <span class="item-value">{{ userStats.adminUsers || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="item-label">普通用户</span>
            <span class="item-value">{{ (userStats.totalUsers || 0) - (userStats.adminUsers || 0) }}</span>
          </div>
          <div class="detail-item">
            <span class="item-label">禁用用户</span>
            <span class="item-value">{{ userStats.disabledUsers || 0 }}</span>
          </div>
          <div class="detail-item divider"></div>
          <div class="detail-item">
            <span class="item-label">已完成面试</span>
            <span class="item-value">{{ interviewStats.completedInterviews || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="item-label">进行中面试</span>
            <span class="item-value">{{ interviewStats.inProgressInterviews || 0 }}</span>
          </div>
          <div class="detail-item divider"></div>
          <div class="detail-item">
            <span class="item-label">后端题目</span>
            <span class="item-value">{{ questionStats.javaQuestions || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="item-label">前端题目</span>
            <span class="item-value">{{ questionStats.frontendQuestions || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="item-label">其他题目</span>
            <span class="item-value">{{ questionStats.otherQuestions || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'

const userStats = ref({})
const interviewStats = ref({})
const questionStats = ref({})
const trendData = ref([])
const jobStats = ref([])
const scoreDistribution = ref([])

// 图表 DOM 引用
const trendChart = ref(null)
const jobChart = ref(null)
const scoreChart = ref(null)

// 图表实例
let trendChartInstance = null
let jobChartInstance = null
let scoreChartInstance = null

const completionRate = computed(() => {
  const total = interviewStats.value.totalInterviews || 0
  const completed = interviewStats.value.completedInterviews || 0
  return total > 0 ? Math.round((completed / total) * 100) : 0
})

onMounted(async () => {
  await loadAllData()
  await nextTick()
  initCharts()
})

onUnmounted(() => {
  // 销毁图表实例
  trendChartInstance?.dispose()
  jobChartInstance?.dispose()
  scoreChartInstance?.dispose()
})

const loadAllData = async () => {
  await Promise.all([
    loadUserStats(),
    loadInterviewStats(),
    loadQuestionStats(),
    loadTrendData(),
    loadJobStats(),
    loadScoreDistribution()
  ])
}

const loadUserStats = async () => {
  try {
    const res = await api.get('/user/stats')
    userStats.value = res.data || {}
  } catch (e) { console.error(e) }
}

const loadInterviewStats = async () => {
  try {
    const res = await api.get('/interview/interview/stats')
    interviewStats.value = res.data || {}
  } catch (e) { console.error(e) }
}

const loadQuestionStats = async () => {
  try {
    const res = await api.get('/question/stats')
    questionStats.value = res.data || {}
  } catch (e) { console.error(e) }
}

const loadTrendData = async () => {
  try {
    const res = await api.get('/interview/interview/trend')
    trendData.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadJobStats = async () => {
  try {
    const res = await api.get('/interview/interview/job-stats')
    jobStats.value = res.data || []
  } catch (e) { console.error(e) }
}

const loadScoreDistribution = async () => {
  try {
    const res = await api.get('/interview/interview/score-distribution')
    scoreDistribution.value = res.data || []
  } catch (e) { console.error(e) }
}

const initCharts = () => {
  initTrendChart()
  initJobChart()
  initScoreChart()
}

const initTrendChart = () => {
  if (!trendChart.value || trendData.value.length === 0) return

  trendChartInstance = echarts.init(trendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendData.value.map(item => {
        const date = new Date(item.date)
        return `${date.getMonth() + 1}/${date.getDate()}`
      }),
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#6B7280' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6B7280' },
      splitLine: { lineStyle: { color: '#F3F4F6' } }
    },
    series: [{
      name: '面试数',
      type: 'bar',
      barWidth: '60%',
      data: trendData.value.map(item => item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#818CF8' },
          { offset: 1, color: '#6366F1' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  }
  trendChartInstance.setOption(option)
}

const initJobChart = () => {
  if (!jobChart.value || jobStats.value.length === 0) return

  jobChartInstance = echarts.init(jobChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#6B7280' },
      splitLine: { lineStyle: { color: '#F3F4F6' } }
    },
    yAxis: {
      type: 'category',
      data: jobStats.value.map(item => item.jobName).reverse(),
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: {
        color: '#6B7280',
        width: 80,
        overflow: 'truncate'
      }
    },
    series: [{
      name: '面试次数',
      type: 'bar',
      data: jobStats.value.map(item => item.count).reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#34D399' },
          { offset: 1, color: '#10B981' }
        ]),
        borderRadius: [0, 4, 4, 0]
      }
    }]
  }
  jobChartInstance.setOption(option)
}

const initScoreChart = () => {
  if (!scoreChart.value || scoreDistribution.value.length === 0) return

  scoreChartInstance = echarts.init(scoreChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#6B7280' }
    },
    series: [{
      name: '分数分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: scoreDistribution.value.map(item => ({
        value: item.count,
        name: item.range
      })),
      color: ['#EF4444', '#F59E0B', '#3B82F6', '#10B981', '#8B5CF6']
    }]
  }
  scoreChartInstance.setOption(option)
}

// 窗口大小改变时重绘图表
window.addEventListener('resize', () => {
  trendChartInstance?.resize()
  jobChartInstance?.resize()
  scoreChartInstance?.resize()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: var(--space-6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-icon.users {
  background: var(--primary-bg);
  color: var(--primary);
}

.stat-icon.interviews {
  background: var(--success-bg);
  color: var(--success);
}

.stat-icon.questions {
  background: var(--warning-bg);
  color: var(--warning);
}

.stat-icon.active {
  background: var(--info-bg);
  color: var(--info);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--gray-900);
}

.stat-label {
  font-size: 13px;
  color: var(--gray-500);
}

/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.chart-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  padding: var(--space-5);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--gray-900);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-color);
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 详细统计列表 */
.detail-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-2) 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-2) 0;
}

.detail-item.divider {
  height: 1px;
  background: var(--border-color);
  padding: 0;
}

.item-label {
  font-size: 13px;
  color: var(--gray-600);
}

.item-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--gray-900);
}
</style>
