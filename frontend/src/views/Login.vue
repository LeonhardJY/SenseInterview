<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand-logo">
        <div class="logo-mark">AI</div>
        <span class="logo-text">AI INTERVIEW</span>
      </div>
      <h1>AI智能模拟面试平台</h1>
      <p>融合语音、视频与文本的智能面试训练系统</p>
      <ul class="features">
        <li>
          <span class="feature-icon">◈</span>
          <span>多模态实时交互</span>
        </li>
        <li>
          <span class="feature-icon">◈</span>
          <span>AI智能评估反馈</span>
        </li>
        <li>
          <span class="feature-icon">◈</span>
          <span>行业题库精准匹配</span>
        </li>
      </ul>
    </div>
    <div class="login-right">
      <div class="form-container">
        <h2>登录账号</h2>
        <p>欢迎回来，请登录以继续您的面试训练</p>
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top">
          <el-form-item label="邮箱 / 手机号" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入邮箱或手机号" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item>
            <div class="form-row">
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              <el-link type="primary">忘记密码？</el-link>
            </div>
          </el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
            登 录
          </el-button>
        </el-form>
        <el-divider>其他登录方式</el-divider>
        <div class="alt-login">
          <el-button class="alt-btn" @click="handlePhoneLogin">
            <el-icon><Iphone /></el-icon>
            手机号快捷登录
          </el-button>
          <el-button class="alt-btn" @click="handleGithubLogin">
            <el-icon><Link /></el-icon>
            使用 GitHub 登录
          </el-button>
        </div>
        <p class="register-prompt">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入邮箱或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    const res = await api.post('/auth/login', loginForm.value)
    userStore.setToken(res.data.token)
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePhoneLogin = () => {
  ElMessage.info('手机号快捷登录功能即将上线')
}

const handleGithubLogin = () => {
  ElMessage.info('GitHub OAuth 登录功能即将上线')
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 0 0 44%;
  max-width: 560px;
  background: #1A1A1A;
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 64px 56px;
  position: relative;
}

.login-left::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #C74634;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 48px;
}

.logo-mark {
  width: 40px;
  height: 40px;
  background: #C74634;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-weight: bold;
  font-size: 14px;
}

.logo-text {
  font-size: 13px;
  letter-spacing: 0.08em;
  color: #9E9E9E;
  text-transform: uppercase;
}

.login-left h1 {
  font-size: 32px;
  font-weight: normal;
  line-height: 1.35;
  margin-bottom: 16px;
}

.login-left p {
  font-size: 16px;
  color: #9E9E9E;
  margin-bottom: 48px;
  line-height: 1.7;
}

.features {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.features li {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
  color: #C4C4C4;
}

.feature-icon {
  color: #E85D4A;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: #F5F5F5;
}

.form-container {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 48px 40px;
  border: 1px solid #E8E8E8;
  border-radius: 2px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.form-container h2 {
  font-size: 22px;
  font-weight: normal;
  color: #1A1A1A;
  margin-bottom: 8px;
}

.form-container > p {
  font-size: 14px;
  color: #767676;
  margin-bottom: 36px;
}

.form-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 44px;
  background: #C74634;
  border-color: #C74634;
}

.login-btn:hover {
  background: #A83A2B;
  border-color: #A83A2B;
}

.alt-login {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alt-btn {
  width: 100%;
}

.register-prompt {
  margin-top: 28px;
  text-align: center;
  font-size: 13px;
  color: #767676;
}

.register-prompt a {
  color: #C74634;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .login-left {
    flex: none;
    max-width: none;
    padding: 40px 28px 36px;
  }

  .login-left h1 {
    font-size: 26px;
  }

  .login-right {
    padding: 32px 20px 48px;
  }

  .form-container {
    padding: 36px 28px;
    box-shadow: none;
    border: none;
    background: transparent;
  }
}
</style>