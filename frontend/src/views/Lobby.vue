<template>
  <div class="lobby">
    <div class="lobby-header">
      <h1>面试大厅</h1>
      <p>选择岗位开始模拟面试</p>
    </div>

    <div class="job-cards">
      <div
        v-for="job in jobs"
        :key="job.id"
        class="job-card"
        @click="selectJob(job)"
      >
        <div class="job-icon">
          <el-icon :size="32"><Monitor /></el-icon>
        </div>
        <h3>{{ job.name }}</h3>
        <p>{{ job.description }}</p>
        <div class="job-tags">
          <el-tag size="small" type="info">{{ job.level }}</el-tag>
          <el-tag size="small">{{ job.category }}</el-tag>
        </div>
      </div>
    </div>

    <!-- 面试配置弹窗 -->
    <el-dialog v-model="showDialog" title="面试配置" width="400px">
      <el-form :model="interviewConfig" label-width="80px">
        <el-form-item label="面试模式">
          <el-radio-group v-model="interviewConfig.mode">
            <el-radio-button value="TEXT">文字面试</el-radio-button>
            <el-radio-button value="VOICE">语音面试</el-radio-button>
            <el-radio-button value="VIDEO">视频面试</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="难度">
          <el-radio-group v-model="interviewConfig.difficulty">
            <el-radio-button value="EASY">简单</el-radio-button>
            <el-radio-button value="MEDIUM">中等</el-radio-button>
            <el-radio-button value="HARD">困难</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="startInterview" :loading="loading">
          开始面试
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const showDialog = ref(false)
const loading = ref(false)
const selectedJob = ref(null)

const interviewConfig = ref({
  mode: 'TEXT',
  difficulty: 'MEDIUM'
})

const jobs = ref([
  { id: 1, name: 'Java开发工程师', description: '负责Java后端开发', level: '中等', category: '后端' },
  { id: 2, name: '前端开发工程师', description: '负责Web前端开发', level: '中等', category: '前端' },
  { id: 3, name: 'Python开发工程师', description: '负责Python后端开发', level: '中等', category: '后端' },
  { id: 4, name: '产品经理', description: '负责产品规划与设计', level: '中等', category: '产品' },
  { id: 5, name: '数据分析师', description: '负责数据分析与挖掘', level: '中等', category: '数据' },
  { id: 6, name: '测试工程师', description: '负责软件测试', level: '中等', category: '测试' }
])

const selectJob = (job) => {
  selectedJob.value = job
  showDialog.value = true
}

const startInterview = async () => {
  loading.value = true
  try {
    const res = await api.post('/interview/create', {
      jobName: selectedJob.value.name,
      mode: interviewConfig.value.mode,
      difficulty: interviewConfig.value.difficulty
    })
    router.push(`/interview/${res.data.id}`)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
    showDialog.value = false
  }
}
</script>

<style scoped>
.lobby {
  padding: 20px;
}

.lobby-header {
  margin-bottom: 30px;
}

.lobby-header h1 {
  font-size: 24px;
  color: #1A1A1A;
  margin-bottom: 8px;
}

.lobby-header p {
  color: #767676;
  font-size: 14px;
}

.job-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.job-card {
  background: #fff;
  border: 1px solid #E8E8E8;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.job-card:hover {
  border-color: #C74634;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.job-icon {
  width: 56px;
  height: 56px;
  background: #FFF5F4;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C74634;
  margin-bottom: 16px;
}

.job-card h3 {
  font-size: 18px;
  color: #1A1A1A;
  margin-bottom: 8px;
}

.job-card p {
  font-size: 14px;
  color: #767676;
  margin-bottom: 16px;
}

.job-tags {
  display: flex;
  gap: 8px;
}
</style>