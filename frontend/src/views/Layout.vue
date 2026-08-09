<template>
  <div class="shell">
    <header class="header">
      <div class="header__inner">
        <button class="header__brand" @click="$router.push('/lobby')">
          <span class="header__mark">✦</span>
          <span class="header__name">SenseInterview</span>
        </button>
        <nav class="header__nav">
          <button :class="['header__link', { 'header__link--active': $route.path === '/lobby' }]" @click="$router.push('/lobby')">面试大厅</button>
          <button :class="['header__link', { 'header__link--active': $route.path === '/agent-interview' }]" @click="$router.push('/agent-interview')" style="color:var(--color-accent)">✦ Agent</button>
          <button :class="['header__link', { 'header__link--active': $route.path === '/history' }]" @click="$router.push('/history')">练习记录</button>
          <button :class="['header__link', { 'header__link--active': $route.path === '/resume' }]" @click="$router.push('/resume')">简历</button>
          <button v-if="userStore.isAdmin" :class="['header__link', { 'header__link--active': $route.path.startsWith('/admin') }]" @click="$router.push('/admin')">管理</button>
        </nav>
        <div class="header__right">
          <div class="user-trigger" @click="showMenu = !showMenu">
            <div class="user-avatar">{{ userStore.userInitial }}</div>
            <span class="user-name">{{ userStore.username }}</span>
          </div>
          <transition name="fade">
            <div v-if="showMenu" class="drop-wrap">
              <div class="drop-mask" @click="showMenu = false"></div>
              <div class="drop-panel card">
                <div class="drop-head">
                  <div class="drop-avatar">{{ userStore.userInitial }}</div>
                  <div>
                    <div class="drop-name">{{ userStore.username }}</div>
                    <div class="drop-role">{{ userStore.isAdmin ? '管理员' : '用户' }}</div>
                  </div>
                </div>
                <div class="drop-divider"></div>
                <div class="drop-item" @click="navigate('/resume')">个人中心</div>
                <div class="drop-item" @click="navigate('/history')">练习记录</div>
                <div v-if="userStore.isAdmin" class="drop-item" @click="navigate('/admin')">管理后台</div>
                <div class="drop-divider"></div>
                <div class="drop-item text-sm" style="color:var(--color-text-secondary)" @click="handleLogout">退出登录</div>
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
.shell { min-height: 100vh; }

.header {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  height: var(--header-height);
  background: rgba(244, 241, 234, 0.92);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid var(--color-divider);
}
.header__inner {
  max-width: var(--max-width); margin: 0 auto; height: 100%;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 var(--spacing-6);
}
.header__brand { display: flex; align-items: center; gap: 10px; background: none; border: none; cursor: pointer; padding: 0; color: var(--color-text-primary); }
.header__mark { font-size: 20px; color: var(--color-accent); line-height: 1; transition: transform var(--transition-fast); }
.header__brand:hover .header__mark { transform: rotate(90deg) scale(1.2); }
.header__name { font-family: var(--font-display); font-size: 24px; font-weight: 700; letter-spacing: -0.03em; }
.header__brand:hover .header__name { color: var(--color-accent); }

.header__nav { display: flex; gap: var(--spacing-1); }
.header__link {
  position: relative; padding: 8px 20px; font-family: var(--font-body); font-size: 14px;
  font-weight: 500; color: var(--color-text-secondary); background: none; border: none;
  border-radius: 999px; cursor: pointer; transition: all var(--transition-fast);
}
.header__link:hover { color: var(--color-text-primary); background: var(--color-surface-subtle); }
.header__link--active { color: var(--color-text-primary); font-weight: 600; background: var(--color-surface); box-shadow: var(--shadow-sm); }

.header__right { position: relative; display: flex; align-items: center; gap: var(--spacing-3); }
.user-trigger { display: flex; align-items: center; gap: 8px; padding: 4px 12px 4px 4px; border-radius: 999px; cursor: pointer; transition: all var(--transition-fast); }
.user-trigger:hover { background: var(--color-surface-subtle); }
.user-avatar { width: 30px; height: 30px; border-radius: 50%; background: var(--color-accent); color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
.user-name { font-size: 14px; font-weight: 500; color: var(--color-text-primary); }

.drop-mask { position: fixed; inset: 0; z-index: 198; }
.drop-panel { position: absolute; top: calc(100% + 8px); right: 0; width: 220px; padding: 0; overflow: hidden; z-index: 199; }
.drop-head { display: flex; align-items: center; gap: 12px; padding: 16px; }
.drop-avatar { width: 38px; height: 38px; border-radius: 50%; background: var(--color-accent); color: white; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; }
.drop-name { font-family: var(--font-display); font-size: 16px; font-weight: 600; color: var(--color-text-primary); }
.drop-role { font-size: 12px; color: var(--color-text-secondary); }
.drop-divider { height: 1px; background: var(--color-divider); margin: 0 8px; }
.drop-item { padding: 10px 16px; font-size: 13px; color: var(--color-text-body); cursor: pointer; transition: all var(--transition-fast); }
.drop-item:hover { background: var(--color-accent-light); color: var(--color-accent); }

.main { padding-top: var(--header-height); min-height: 100vh; padding: calc(var(--header-height) + var(--spacing-6)) var(--spacing-6) var(--spacing-6); max-width: var(--max-width); margin: 0 auto; }

@media (max-width: 768px) {
  .header__nav { display: none; }
  .header__inner { padding: 0 var(--spacing-4); }
  .main { padding: calc(var(--header-height) + var(--spacing-4)) var(--spacing-4) var(--spacing-4); }
}
</style>
