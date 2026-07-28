<template>
  <div class="shell">
    <header class="topbar">
      <div class="topbar-inner">
        <div class="brand" @click="$router.push('/lobby')">
          <div class="brand-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <div>
            <div class="brand-name">SenseInterview</div>
            <div class="brand-sub">AI Mock Interview</div>
          </div>
        </div>
        <div class="topbar-right">
          <div class="user-trigger" @click="showMenu = !showMenu">
            <div class="us-avatar">{{ userStore.userInitial }}</div>
            <span class="us-name">{{ userStore.username }}</span>
            <svg class="us-chev" :class="{ open: showMenu }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
          </div>
          <transition name="drop">
            <div v-if="showMenu" class="drop-wrap">
              <div class="drop-mask" @click="showMenu = false"></div>
              <div class="drop-panel card">
                <div style="display:flex;align-items:center;gap:12px;padding:14px;border-bottom:1px solid var(--card-border)">
                  <div class="drop-avatar">{{ userStore.userInitial }}</div>
                  <div><div style="font-size:13px;font-weight:600;color:var(--gray-800)">{{ userStore.username }}</div><div style="font-size:11px;color:var(--gray-400)">{{ userStore.isAdmin ? '管理员' : '用户' }}</div></div>
                </div>
                <div style="padding:4px">
                  <div class="drop-item" @click="navigate('/resume')">个人中心</div>
                  <div class="drop-item" @click="navigate('/history')">练习记录</div>
                  <div v-if="userStore.isAdmin" class="drop-item" @click="navigate('/admin')">管理后台</div>
                  <div style="height:1px;background:var(--card-border);margin:4px 0"></div>
                  <div class="drop-item" style="color:var(--gray-500)" @click="handleLogout">退出登录</div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'

const router = useRouter(); const userStore = useUserStore()
const showMenu = ref(false)
const navigate = (p) => { showMenu.value = false; router.push(p) }
const handleLogout = () => { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.shell { min-height:100vh; background:var(--bg) }
.topbar {
  position:sticky; top:0; z-index:100;
  background:white; border-bottom:1px solid var(--card-border)
}
.topbar-inner {
  display:flex; align-items:center; justify-content:space-between;
  height:56px; padding:0 28px; max-width:1100px; margin:0 auto
}
.brand { display:flex; align-items:center; gap:10px; cursor:pointer }
.brand-icon { width:28px; height:28px; color:var(--accent) }
.brand-icon svg { width:100%; height:100% }
.brand-name { font-size:15px; font-weight:700; color:var(--primary); letter-spacing:-0.02em; line-height:1.2 }
.brand-sub { font-size:10px; color:var(--gray-400); letter-spacing:0.05em; text-transform:uppercase }

.topbar-right { position:relative }
.user-trigger {
  display:flex; align-items:center; gap:8px;
  padding:4px 10px 4px 4px; border-radius:var(--radius-pill);
  cursor:pointer; transition:var(--transition)
}
.user-trigger:hover { background:var(--gray-50) }
.us-avatar { width:28px; height:28px; border-radius:50%; background:var(--accent); color:white; display:flex; align-items:center; justify-content:center; font-size:11px; font-weight:600 }
.us-name { font-size:13px; font-weight:500; color:var(--gray-700) }
.us-chev { width:12px; height:12px; color:var(--gray-400); transition:transform 0.2s }
.us-chev.open { transform:rotate(180deg) }

.drop-mask { position:fixed; inset:0; z-index:198 }
.drop-panel { position:absolute; top:calc(100% + 8px); right:0; width:200px; z-index:199; padding:4px }
.drop-avatar { width:36px; height:36px; border-radius:50%; background:var(--accent); color:white; display:flex; align-items:center; justify-content:center; font-size:13px; font-weight:600 }
.drop-item { padding:9px 14px; font-size:13px; color:var(--gray-600); border-radius:10px; cursor:pointer; transition:var(--transition) }
.drop-item:hover { background:var(--accent-light); color:var(--accent) }

.main { padding:24px 28px; min-height:calc(100vh - 56px) }

.drop-enter-active,.drop-leave-active { transition:all 0.2s ease }
.drop-enter-from,.drop-leave-to { opacity:0; transform:translateY(-8px) }

@media (max-width:768px) {
  .main { padding:16px }
  .topbar-inner { padding:0 16px }
}
</style>
