<template>
  <div class="history">
    <div class="history-header">
      <h1>历史记录</h1>
      <p>查看您的面试历史</p>
    </div>

    <div class="history-list">
      <div
        v-for="item in historyList"
        :key="item.id"
        class="history-item"
      >
        <div class="item-info">
          <h3>{{ item.jobName }}</h3>
          <p class="item-time">{{ item.createTime }}</p>
        </div>
        <div class="item-meta">
          <el-tag size="small">{{ item.mode }}</el-tag>
          <el-tag size="small" type="info">{{ item.difficulty }}</el-tag>
        </div>
        <div class="item-score">
          <span class="score">{{ item.totalScore || '--' }}</span>
          <span class="score-label">分</span>
        </div>
        <div class="item-actions">
          <el-button type="primary" link @click="viewReport(item)">
            查看报告
          </el-button>
          <el-button type="info" link @click="continueInterview(item)" v-if="item.status === 'CREATED' || item.status === 'RUNNING'">
            继续面试
          </el-button>
        </div>
      </div>

      <el-empty v-if="historyList.length === 0" description="暂无面试记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const historyList = ref([])

onMounted(() => {
  loadHistory()
})

const loadHistory = async () => {
  try {
    const res = await api.get('/interview/list')
    historyList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const viewReport = (item) => {
  router.push(`/report/${item.id}`)
}

const continueInterview = (item) => {
  router.push(`/interview/${item.id}`)
}
</script>

<style scoped>
.history {
  padding: 20px;
}

.history-header {
  margin-bottom: 24px;
}

.history-header h1 {
  font-size: 24px;
  color: #1A1A1A;
  margin-bottom: 8px;
}

.history-header p {
  color: #767676;
  font-size: 14px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  background: #fff;
  border: 1px solid #E8E8E8;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
}

.history-item:hover {
  border-color: #C74634;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.item-info {
  flex: 1;
}

.item-info h3 {
  font-size: 16px;
  color: #1A1A1A;
  margin-bottom: 4px;
}

.item-time {
  font-size: 13px;
  color: #999;
}

.item-meta {
  display: flex;
  gap: 8px;
}

.item-score {
  text-align: center;
  min-width: 60px;
}

.item-score .score {
  font-size: 24px;
  font-weight: bold;
  color: #C74634;
}

.item-score .score-label {
  font-size: 12px;
  color: #999;
  margin-left: 2px;
}

.item-actions {
  display: flex;
  gap: 8px;
}
</style>