<template>
  <div class="login-page">
    <!-- 左侧介绍 -->
    <div class="intro-panel">
      <div class="intro-sticky">
        <div class="brand">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:26px;height:26px"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
          <span>SenseInterview</span>
        </div>

        <h1 class="intro-title">AI 模拟面试平台</h1>
        <p class="intro-desc">融合语音交互、面部表情分析与大语言模型，实现面试表现的多维度评估与实时反馈。</p>

        <div class="tech-section">
          <h4>技术栈</h4>
          <div class="tech-tags">
            <span class="tech-tag">Spring Boot</span>
            <span class="tech-tag">Vue 3</span>
            <span class="tech-tag">DeepSeek</span>
            <span class="tech-tag">DeepFace</span>
            <span class="tech-tag">WebSocket</span>
            <span class="tech-tag">Redis</span>
            <span class="tech-tag">LangChain4j</span>
            <span class="tech-tag">MySQL</span>
          </div>
        </div>

        <div class="feature-section">
          <h4>核心能力</h4>
          <div class="feature-list">
            <div class="feature-item">
              <div class="fi-dot"></div>
              <div class="fi-text">多轮对话上下文保持，SSE 流式输出</div>
            </div>
            <div class="feature-item">
              <div class="fi-dot"></div>
              <div class="fi-text">DeepFace 面部情绪实时识别</div>
            </div>
            <div class="feature-item">
              <div class="fi-dot"></div>
              <div class="fi-text">TEXT / VOICE / VIDEO 三种面试模式</div>
            </div>
            <div class="feature-item">
              <div class="fi-dot"></div>
              <div class="fi-text">五维评分报告 + 情绪趋势分析</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单 -->
    <div class="form-panel">
      <div class="form-card card">
        <transition name="fade" mode="out-in">
          <div v-if="mode === 'login'" key="login">
            <div class="form-head">
              <h2>欢迎回来</h2>
              <p>登录以继续面试训练</p>
            </div>
            <div v-if="errorMsg" class="alert error">{{ errorMsg }}</div>
            <div v-if="successMsg" class="alert success">{{ successMsg }}</div>
            <form @submit.prevent="handleLogin">
              <div class="field"><label>账号</label><input type="text" class="input" v-model="loginForm.username" placeholder="用户名"></div>
              <div class="field"><label>密码</label><input :type="showPw ? 'text' : 'password'" class="input" v-model="loginForm.password" placeholder="密码"></div>
              <button type="submit" class="btn btn-primary" style="width:100%" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
            </form>
            <p class="switch-text">还没有账号？<a @click.prevent="mode='register'">注册</a></p>
          </div>
          <div v-else key="register">
            <div class="form-head">
              <h2>创建账号</h2>
              <p>注册后即可开始面试训练</p>
            </div>
            <div v-if="errorMsg" class="alert error">{{ errorMsg }}</div>
            <div v-if="successMsg" class="alert success">{{ successMsg }}</div>
            <form @submit.prevent="handleRegister">
              <div class="field"><label>用户名</label><input type="text" class="input" v-model="registerForm.username" placeholder="用户名"></div>
              <div class="field"><label>邮箱</label><input type="email" class="input" v-model="registerForm.email" placeholder="邮箱"></div>
              <div class="field"><label>密码</label><input type="password" class="input" v-model="registerForm.password" placeholder="至少6位"></div>
              <div class="field"><label>确认密码</label><input type="password" class="input" v-model="registerForm.confirmPassword" placeholder="再次输入"></div>
              <button type="submit" class="btn btn-primary" style="width:100%" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
            </form>
            <p class="switch-text">已有账号？<a @click.prevent="mode='login'">返回登录</a></p>
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
  min-height:100vh; display:flex; position:relative; overflow:hidden;
  background:var(--bg);
}
/* 右侧粒子渐变动画 */
.login-page::before {
  content:''; position:absolute; right:0; top:0; width:55%; height:100%;
  background:
    radial-gradient(circle at 70% 30%, rgba(217,119,6,0.06) 0%, transparent 40%),
    radial-gradient(circle at 50% 70%, rgba(99,102,241,0.04) 0%, transparent 40%),
    radial-gradient(circle at 80% 50%, rgba(244,114,182,0.03) 0%, transparent 35%);
  animation:ambientGlow 8s ease-in-out infinite alternate;
  pointer-events:none;
}
@keyframes ambientGlow {
  0% { opacity:0.6; transform:scale(1) }
  50% { opacity:1; transform:scale(1.05) }
  100% { opacity:0.6; transform:scale(1) }
}
/* 分隔线 */
.login-page::after {
  content:''; position:absolute; left:45%; top:8%; bottom:8%;
  width:1px; background:linear-gradient(180deg,transparent,var(--gray-300),transparent);
}

/* ── 左侧介绍 ── */
.intro-panel {
  flex:0 0 45%; display:flex; align-items:center; justify-content:flex-end;
  padding:40px 60px 40px 0;
}
.intro-sticky {
  max-width:420px;
  animation:floatIn 0.6s ease both;
}

.brand { display:flex; align-items:center; gap:12px; font-size:20px; font-weight:700; color:var(--primary); margin-bottom:40px }
.brand svg { width:30px; height:30px; color:var(--accent) }

.intro-title { font-size:34px; font-weight:700; color:var(--gray-900); letter-spacing:-0.03em; line-height:1.25; margin-bottom:14px }
.intro-desc { font-size:15px; color:var(--gray-400); line-height:1.8; margin-bottom:36px }

.tech-section { margin-bottom:32px }
.tech-section h4 { font-size:13px; font-weight:600; color:var(--gray-600); margin-bottom:12px }
.tech-tags { display:flex; flex-wrap:wrap; gap:8px }
.tech-tag {
  padding:4px 14px; font-size:13px; font-weight:500;
  background:var(--accent-light); color:var(--accent);
  border-radius:var(--radius-pill)
}

.feature-section h4 { font-size:13px; font-weight:600; color:var(--gray-600); margin-bottom:12px }
.feature-list { display:flex; flex-direction:column; gap:12px }
.feature-item { display:flex; align-items:flex-start; gap:10px }
.fi-dot { width:6px; height:6px; border-radius:50%; background:var(--accent); margin-top:7px; flex-shrink:0 }
.fi-text { font-size:14px; color:var(--gray-600); line-height:1.6 }

/* ── 右侧表单 ── */
.form-panel {
  flex:1; display:flex; align-items:center; justify-content:center;
  padding:40px 60px;
  position:relative; z-index:1;
}
.form-card { width:100%; max-width:420px; padding:36px }

.form-head { margin-bottom:28px }
.form-head h2 { font-size:22px; font-weight:700; color:var(--gray-900); margin-bottom:6px }
.form-head p { font-size:14px; color:var(--gray-400) }

.field { margin-bottom:18px }
.field label { display:block; font-size:13px; font-weight:500; color:var(--gray-700); margin-bottom:6px }

.alert { padding:10px 14px; border-radius:12px; font-size:13px; margin-bottom:16px }
.alert.error { background:var(--rose-light); color:var(--rose) }
.alert.success { background:rgba(34,197,94,0.1); color:#16a34a }

.switch-text { text-align:center; margin-top:20px; font-size:13px; color:var(--gray-400) }
.switch-text a { color:var(--accent); font-weight:500; cursor:pointer; text-decoration:none }

.fade-enter-active,.fade-leave-active { transition:opacity 0.2s ease }
.fade-enter-from,.fade-leave-to { opacity:0 }

@keyframes floatIn { from { opacity:0; transform:translateY(16px) } to { opacity:1; transform:translateY(0) } }

@media (max-width:768px) {
  .login-page { flex-direction:column }
  .intro-panel { flex:none; padding:32px 24px 0; justify-content:center }
  .intro-sticky { max-width:100%; position:static }
  .intro-title { font-size:22px }
  .form-panel { padding:24px; justify-content:center }
  .form-card { max-width:100% }
}
</style>
