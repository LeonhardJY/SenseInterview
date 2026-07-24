<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册账号</h2>
      <p>创建您的账号，开始面试训练之旅</p>
      <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="register-btn" @click="handleRegister" :loading="loading">
          注 册
        </el-button>
      </el-form>
      <p class="login-prompt">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await registerFormRef.value.validate()
  if (!valid) return

  loading.value = true
  try {
    await api.post('/auth/register', registerForm.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #F5F5F5;
}

.register-box {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 48px 40px;
  border: 1px solid #E8E8E8;
  border-radius: 2px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.register-box h2 {
  font-size: 22px;
  font-weight: normal;
  color: #1A1A1A;
  margin-bottom: 8px;
  text-align: center;
}

.register-box > p {
  font-size: 14px;
  color: #767676;
  margin-bottom: 36px;
  text-align: center;
}

.register-btn {
  width: 100%;
  height: 44px;
  background: #C74634;
  border-color: #C74634;
}

.register-btn:hover {
  background: #A83A2B;
  border-color: #A83A2B;
}

.login-prompt {
  margin-top: 28px;
  text-align: center;
  font-size: 13px;
  color: #767676;
}

.login-prompt a {
  color: #C74634;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}
</style>