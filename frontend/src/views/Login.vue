<template>
  <div class="login-page">
    <div class="login-bg" />
    <span class="floating-brand" aria-hidden="true">SenseInterview</span>

    <div class="login-grid section">
      <div class="login-left">
        <div class="brand">
          <span class="brand__mark">✦</span>
          <span class="brand__name">SenseInterview</span>
        </div>
        <h1 class="login-title">AI 模拟面试平台</h1>
        <p class="login-desc">融合语音交互、面部表情分析与大语言模型，实现面试表现的多维度评估与实时反馈。</p>
        <div class="login-tech">
          <span class="tech-tag">Spring Boot</span>
          <span class="tech-tag">Vue 3</span>
          <span class="tech-tag">DeepSeek</span>
          <span class="tech-tag">DeepFace</span>
          <span class="tech-tag">WebSocket</span>
          <span class="tech-tag">Redis</span>
        </div>
        <div class="login-features">
          <div class="lf-item">
            <span class="lf-dot" />
            <span>DeepSeek 大模型驱动，SSE 流式输出，首 token 延迟约 2.1 秒</span>
          </div>
          <div class="lf-item">
            <span class="lf-dot" />
            <span>DeepFace 情绪识别，实时分析面试者面部表情</span>
          </div>
          <div class="lf-item">
            <span class="lf-dot" />
            <span>TEXT / VOICE / VIDEO 三种面试模式切换</span>
          </div>
          <div class="lf-item">
            <span class="lf-dot" />
            <span>五维评分报告：专业能力、表达、逻辑、情绪、自信</span>
          </div>
        </div>
      </div>

      <div class="login-right">
        <div class="card" style="padding:var(--spacing-8)">
          <transition name="fade" mode="out-in">
            <div v-if="mode === 'login'" key="login">
              <div style="margin-bottom:var(--spacing-6)">
                <h3>欢迎回来</h3>
                <p class="text-muted text-sm" style="margin-top:4px">登录以继续面试训练</p>
              </div>
              <div v-if="errorMsg" class="form-alert error">{{ errorMsg }}</div>
              <div v-if="successMsg" class="form-alert success">{{ successMsg }}</div>
              <form @submit.prevent="handleLogin">
                <div class="form-group"><label class="form-label">账号</label><input type="text" class="form-input" v-model="loginForm.username" placeholder="用户名"></div>
                <div class="form-group"><label class="form-label">密码</label><input :type="showPw ? 'text' : 'password'" class="form-input" v-model="loginForm.password" placeholder="密码"></div>
                <button type="submit" class="btn btn--primary btn--full" :disabled="loading" style="margin-top:4px">{{ loading ? '登录中...' : '登录' }}</button>
              </form>
              <p class="text-sm text-center" style="margin-top:20px;color:var(--color-text-secondary)">还没有账号？<a @click.prevent="mode='register'" style="cursor:pointer">注册</a></p>
            </div>
            <div v-else key="register">
              <div style="margin-bottom:var(--spacing-6)">
                <h3>创建账号</h3>
                <p class="text-muted text-sm" style="margin-top:4px">注册后即可开始面试训练</p>
              </div>
              <div v-if="errorMsg" class="form-alert error">{{ errorMsg }}</div>
              <div v-if="successMsg" class="form-alert success">{{ successMsg }}</div>
              <form @submit.prevent="handleRegister">
                <div class="form-group"><label class="form-label">用户名</label><input type="text" class="form-input" v-model="registerForm.username" placeholder="用户名"></div>
                <div class="form-group"><label class="form-label">邮箱</label><input type="email" class="form-input" v-model="registerForm.email" placeholder="邮箱"></div>
                <div class="form-group"><label class="form-label">密码</label><input type="password" class="form-input" v-model="registerForm.password" placeholder="至少6位"></div>
                <div class="form-group"><label class="form-label">确认密码</label><input type="password" class="form-input" v-model="registerForm.confirmPassword" placeholder="再次输入"></div>
                <button type="submit" class="btn btn--primary btn--full" :disabled="loading" style="margin-top:4px">{{ loading ? '注册中...' : '注册' }}</button>
              </form>
              <p class="text-sm text-center" style="margin-top:20px;color:var(--color-text-secondary)">已有账号？<a @click.prevent="mode='login'" style="cursor:pointer">返回登录</a></p>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import api from '@/api'

const router = useRouter(); const userStore = useUserStore()
const mode = ref('login'); const loading = ref(false); const showPw = ref(false)
const errorMsg = ref(''); const successMsg = ref('')
const loginForm = ref({ username:'', password:'' })
const registerForm = ref({ username:'', email:'', password:'', confirmPassword:'' })

const handleLogin = async () => {
  errorMsg.value = ''
  if (!loginForm.value.username) { errorMsg.value = '请输入用户名'; return }
  if (!loginForm.value.password) { errorMsg.value = '请输入密码'; return }
  loading.value = true
  try {
    const res = await api.post('/auth/login', loginForm.value)
    userStore.setToken(res.data.token); userStore.setUser(res.data); router.push('/')
  } catch (e) { errorMsg.value = e.message || '登录失败' }
  finally { loading.value = false }
}

const handleRegister = async () => {
  errorMsg.value = ''; successMsg.value = ''
  if (!registerForm.value.username) { errorMsg.value = '请输入用户名'; return }
  if (!registerForm.value.email) { errorMsg.value = '请输入邮箱'; return }
  if (!registerForm.value.password || registerForm.value.password.length < 6) { errorMsg.value = '密码至少6位'; return }
  if (registerForm.value.password !== registerForm.value.confirmPassword) { errorMsg.value = '两次密码不一致'; return }
  loading.value = true
  try {
    await api.post('/auth/register', { username: registerForm.value.username, email: registerForm.value.email, password: registerForm.value.password })
    successMsg.value = '注册成功！即将跳转...'
    setTimeout(() => { mode.value = 'login'; loginForm.value.username = registerForm.value.username; registerForm.value = { username:'', email:'', password:'', confirmPassword:'' }; successMsg.value = '' }, 1500)
  } catch (e) { errorMsg.value = e.message || '注册失败' }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center;
  position: relative; overflow: hidden;
}
.login-bg {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse 50% 40% at 15% 30%, rgba(217,119,87,0.06) 0%, transparent 55%),
              radial-gradient(ellipse 40% 35% at 85% 80%, rgba(217,119,87,0.04) 0%, transparent 50%);
  pointer-events: none;
}
.floating-brand {
  position: fixed; top: 45%; left: 50%; z-index: 0;
  font-family: "Instrument Serif", Georgia, serif;
  font-size: clamp(120px, 20vw, 300px); font-weight: 400;
  color: var(--color-text-primary); opacity: 0.035;
  pointer-events: none; user-select: none;
  line-height: 1; white-space: nowrap;
  animation: floatSlow 12s ease-in-out infinite;
}
@keyframes floatSlow {
  0%, 100% { transform: translate(-50%,-50%) rotate(-1deg); }
  50% { transform: translate(-50%,-50%) rotate(-1deg) translateY(-16px); }
}

.login-grid {
  display: grid; grid-template-columns: 1fr 420px; gap: 60px;
  align-items: center; width: 100%; position: relative; z-index: 1;
  padding-top: var(--spacing-10); padding-bottom: var(--spacing-10);
}
.login-left { position: relative; }

.brand { display: flex; align-items: center; gap: 10px; margin-bottom: var(--spacing-8); }
.brand__mark { font-size: 22px; color: var(--color-accent); }
.brand__name { font-family: var(--font-display); font-size: 26px; font-weight: 700; color: var(--color-text-primary); }

.login-title { font-size: 48px; font-weight: 600; margin-bottom: var(--spacing-4); }
.login-desc { font-size: 15px; color: var(--color-text-secondary); line-height: 1.7; max-width: 420px; margin-bottom: var(--spacing-8); }

.tech-tag {
  display: inline-flex; padding: 4px 14px; margin: 0 6px 6px 0;
  font-size: 13px; font-weight: 500;
  background: var(--color-accent-light); color: var(--color-accent);
  border-radius: 999px;
}
.login-features { display: flex; flex-direction: column; gap: 12px; margin-top: var(--spacing-6); }
.lf-item { display: flex; align-items: flex-start; gap: 10px; font-size: 14px; color: var(--color-text-body); line-height: 1.5; }
.lf-dot { width: 5px; height: 5px; border-radius: 50%; background: var(--color-accent); margin-top: 8px; flex-shrink: 0; }

.form-alert { padding: 10px 14px; border-radius: var(--radius-sm); font-size: 13px; margin-bottom: var(--spacing-4); }
.form-alert.error { background: var(--color-danger-light); color: var(--color-danger); }
.form-alert.success { background: var(--color-success-light); color: var(--color-success); }

@media (max-width: 1024px) {
  .login-grid { grid-template-columns: 1fr; gap: var(--spacing-8); }
  .login-title { font-size: 36px; }
  .login-desc { max-width: none; }
}
@media (max-width: 768px) {
  .login-page { align-items: flex-start; }
  .login-grid { padding-top: var(--spacing-8); }
  .login-title { font-size: 28px; }
  .login-features { display: none; }
}
</style>
