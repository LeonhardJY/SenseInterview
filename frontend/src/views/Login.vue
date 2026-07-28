<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <span class="logo-text">Interview AI</span>
        </div>

        <h1 class="brand-title">AI智能模拟面试平台</h1>
        <p class="brand-desc">融合语音、视频与文本的智能面试训练系统，助你在真实场景中从容应对。</p>

        <ul class="features">
          <li>
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
            </div>
            <div class="feature-text">
              <strong>多模态实时交互</strong>
              <span>支持语音对话、表情分析与代码白板</span>
            </div>
          </li>
          <li>
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg>
            </div>
            <div class="feature-text">
              <strong>AI智能评估反馈</strong>
              <span>从表达逻辑、技术深度到肢体语言</span>
            </div>
          </li>
          <li>
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>
            </div>
            <div class="feature-text">
              <strong>行业题库精准匹配</strong>
              <span>覆盖互联网、金融、咨询等主流岗位</span>
            </div>
          </li>
        </ul>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="form-container">
        <!-- ===== 登录表单 ===== -->
        <transition name="fade" mode="out-in">
          <div v-if="mode === 'login'" key="login">
            <div class="form-header">
              <h2>欢迎回来</h2>
              <p>请登录以继续您的面试训练</p>
            </div>

            <div v-if="errorMsg" class="alert alert-error">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg>
              {{ errorMsg }}
            </div>

            <form @submit.prevent="handleLogin">
              <div class="form-group">
                <label class="form-label">账号</label>
                <input type="text" class="form-input" v-model="loginForm.username" placeholder="请输入用户名" autocomplete="username">
              </div>
              <div class="form-group">
                <label class="form-label">密码</label>
                <div class="password-wrapper">
                  <input :type="showPassword ? 'text' : 'password'" class="form-input" v-model="loginForm.password" placeholder="请输入密码" autocomplete="current-password">
                  <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                    <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                  </button>
                </div>
              </div>
              <button type="submit" class="login-btn" :disabled="loading">
                {{ loading ? '登录中...' : '登 录' }}
              </button>
            </form>

            <div class="divider"><span>其他登录方式</span></div>
            <div class="alt-login">
              <button type="button" class="alt-btn" @click="handlePhoneLogin">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
                手机号快捷登录
              </button>
              <button type="button" class="alt-btn" @click="handleGithubLogin">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"/></svg>
                使用 GitHub 登录
              </button>
            </div>
            <p class="switch-link">
              还没有账号？<a @click.prevent="mode = 'register'">立即注册</a>
            </p>
          </div>

          <!-- ===== 注册表单 ===== -->
          <div v-else key="register">
            <div class="form-header">
              <h2>创建账号</h2>
              <p>注册后即可开始面试训练</p>
            </div>

            <div v-if="errorMsg" class="alert alert-error">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4M12 16h.01"/></svg>
              {{ errorMsg }}
            </div>

            <div v-if="successMsg" class="alert alert-success">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><path d="M22 4L12 14.01l-3-3"/></svg>
              {{ successMsg }}
            </div>

            <form @submit.prevent="handleRegister">
              <div class="form-group">
                <label class="form-label">用户名</label>
                <input type="text" class="form-input" v-model="registerForm.username" placeholder="请输入用户名" autocomplete="username">
              </div>
              <div class="form-group">
                <label class="form-label">邮箱</label>
                <input type="email" class="form-input" v-model="registerForm.email" placeholder="请输入邮箱" autocomplete="email">
              </div>
              <div class="form-group">
                <label class="form-label">密码</label>
                <input type="password" class="form-input" v-model="registerForm.password" placeholder="请输入密码（至少6位）" autocomplete="new-password">
              </div>
              <div class="form-group">
                <label class="form-label">确认密码</label>
                <input type="password" class="form-input" v-model="registerForm.confirmPassword" placeholder="请再次输入密码" autocomplete="new-password">
              </div>
              <button type="submit" class="login-btn" :disabled="loading">
                {{ loading ? '注册中...' : '注 册' }}
              </button>
            </form>
            <p class="switch-link">
              已有账号？<a @click.prevent="mode = 'login'">返回登录</a>
            </p>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { ElMessage } from 'element-plus'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()

const mode = ref('login')
const loading = ref(false)
const showPassword = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', email: '', password: '', confirmPassword: '' })

const handleLogin = async () => {
  errorMsg.value = ''
  if (!loginForm.value.username) { errorMsg.value = '请输入用户名'; return }
  if (!loginForm.value.password) { errorMsg.value = '请输入密码'; return }

  loading.value = true
  try {
    const res = await api.post('/auth/login', loginForm.value)
    userStore.setToken(res.data.token)
    userStore.setUser(res.data)   // setUser 会同步存到 localStorage
    router.push('/')
  } catch (error) {
    errorMsg.value = error.message || '登录失败，请检查用户名和密码'
  } finally { loading.value = false }
}

const handleRegister = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  if (!registerForm.value.username) { errorMsg.value = '请输入用户名'; return }
  if (!registerForm.value.email) { errorMsg.value = '请输入邮箱'; return }
  if (!registerForm.value.password || registerForm.value.password.length < 6) { errorMsg.value = '密码至少6位'; return }
  if (registerForm.value.password !== registerForm.value.confirmPassword) { errorMsg.value = '两次密码不一致'; return }

  loading.value = true
  try {
    await api.post('/auth/register', {
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password
    })
    successMsg.value = '注册成功！即将跳转到登录页面...'
    setTimeout(() => {
      mode.value = 'login'
      loginForm.value.username = registerForm.value.username
      registerForm.value = { username: '', email: '', password: '', confirmPassword: '' }
      successMsg.value = ''
    }, 1500)
  } catch (error) {
    errorMsg.value = error.message || '注册失败，请重试'
  } finally { loading.value = false }
}

const handlePhoneLogin = () => { ElMessage.info('手机号快捷登录功能即将上线') }
const handleGithubLogin = () => { ElMessage.info('GitHub OAuth 登录功能即将上线') }
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; }

.brand-section {
  flex: 0 0 45%; background: linear-gradient(135deg, var(--gray-900) 0%, #1a1a2e 100%);
  color: white; display: flex; align-items: center; padding: 60px; position: relative; overflow: hidden;
}
.brand-section::before { content: ''; position: absolute; top: -50%; right: -20%; width: 600px; height: 600px; border-radius: 50%; border: 1px solid rgba(79, 70, 229, 0.2); }
.brand-section::after { content: ''; position: absolute; bottom: -30%; left: -10%; width: 400px; height: 400px; border-radius: 50%; border: 1px solid rgba(79, 70, 229, 0.15); }
.brand-content { position: relative; z-index: 1; }
.brand-logo { display: flex; align-items: center; gap: 12px; margin-bottom: 48px; }
.logo-icon { width: 44px; height: 44px; background: var(--primary); border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.logo-icon svg { width: 24px; height: 24px; color: white; }
.logo-text { font-size: 18px; font-weight: 600; letter-spacing: 0.5px; }
.brand-title { font-size: 36px; font-weight: 600; line-height: 1.3; margin-bottom: 16px; }
.brand-desc { font-size: 16px; color: var(--gray-400); line-height: 1.7; margin-bottom: 48px; }
.features { list-style: none; display: flex; flex-direction: column; gap: 24px; }
.features li { display: flex; align-items: flex-start; gap: 16px; }
.feature-icon { width: 40px; height: 40px; border-radius: 10px; background: rgba(79, 70, 229, 0.15); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.feature-icon svg { width: 20px; height: 20px; color: var(--primary-light); }
.feature-text strong { display: block; font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.feature-text span { font-size: 14px; color: var(--gray-400); }

.form-section { flex: 1; display: flex; align-items: center; justify-content: center; padding: 60px; background: var(--gray-50); }
.form-container { width: 100%; max-width: 400px; }
.form-header { margin-bottom: 32px; }
.form-header h2 { font-size: 24px; font-weight: 600; color: var(--gray-900); margin-bottom: 8px; }
.form-header p { font-size: 14px; color: var(--gray-500); }

.alert { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-radius: var(--border-radius); margin-bottom: 20px; font-size: 13px; }
.alert-error { background: var(--danger-bg); color: var(--danger); border: 1px solid #fecaca; }
.alert-success { background: var(--success-bg); color: var(--success); border: 1px solid #a7f3d0; }
.alert svg { width: 18px; height: 18px; flex-shrink: 0; }

.form-group { margin-bottom: 20px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--gray-700); margin-bottom: 6px; }
.form-input { width: 100%; height: 44px; padding: 0 14px; font-size: 14px; border: 1px solid var(--gray-300); border-radius: var(--border-radius); background: white; transition: var(--transition); }
.form-input:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-bg); }
.form-input::placeholder { color: var(--gray-400); }

.password-wrapper { position: relative; }
.password-wrapper .form-input { padding-right: 44px; }
.toggle-password { position: absolute; right: 0; top: 0; width: 44px; height: 44px; border: none; background: none; cursor: pointer; display: flex; align-items: center; justify-content: center; color: var(--gray-400); transition: var(--transition); }
.toggle-password:hover { color: var(--gray-600); }
.toggle-password svg { width: 18px; height: 18px; }

.login-btn { width: 100%; height: 44px; background: var(--primary); color: white; border: none; border-radius: var(--border-radius); font-size: 15px; font-weight: 500; cursor: pointer; transition: var(--transition); }
.login-btn:hover { background: var(--primary-dark); }
.login-btn:disabled { background: var(--gray-300); cursor: not-allowed; }

.divider { display: flex; align-items: center; gap: 16px; margin: 24px 0; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: var(--gray-200); }
.divider span { font-size: 12px; color: var(--gray-400); }

.alt-login { display: flex; flex-direction: column; gap: 10px; }
.alt-btn { display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; height: 42px; background: white; border: 1px solid var(--gray-200); border-radius: var(--border-radius); font-size: 13px; color: var(--gray-600); cursor: pointer; transition: var(--transition); }
.alt-btn:hover { border-color: var(--gray-300); background: var(--gray-50); }
.alt-btn svg { width: 18px; height: 18px; }

.switch-link { margin-top: 24px; text-align: center; font-size: 13px; color: var(--gray-500); }
.switch-link a { color: var(--primary); text-decoration: none; font-weight: 500; cursor: pointer; }
.switch-link a:hover { text-decoration: underline; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 768px) {
  .login-page { flex-direction: column; }
  .brand-section { flex: none; padding: 40px 24px; }
  .brand-title { font-size: 28px; }
  .form-section { padding: 40px 24px; }
}
</style>