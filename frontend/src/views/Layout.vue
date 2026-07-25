<template>
  <div class="layout">
    <header class="header">
      <div class="header-left">
        <div class="logo" @click="$router.push('/lobby')">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">AI面试平台</span>
            <span class="logo-sub">Interview System</span>
          </div>
        </div>
      </div>
      <div class="header-right">
        <div class="user-dropdown" @click="showMenu = !showMenu">
          <div class="user-avatar">{{ userInitial }}</div>
          <div class="user-info">
            <span class="user-name">{{ username }}</span>
            <span class="user-role">{{ role === 'ADMIN' ? '管理员' : '用户' }}</span>
          </div>
          <svg class="dropdown-arrow" :class="{ open: showMenu }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 9l6 6 6-6"/>
          </svg>
        </div>
        <div v-if="showMenu" class="dropdown-mask" @click="showMenu = false"></div>
        <transition name="dropdown">
          <div v-if="showMenu" class="dropdown-menu">
            <div class="dropdown-header">
              <div class="dropdown-avatar">{{ userInitial }}</div>
              <div>
                <div class="dropdown-name">{{ username }}</div>
                <div class="dropdown-role">{{ role === 'ADMIN' ? '管理员' : '普通用户' }}</div>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" @click="navigate('/resume')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/></svg>
              个人中心
            </a>
            <a class="dropdown-item" @click="navigate('/history')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              练习记录
            </a>
            <div class="dropdown-divider"></div>
            <a class="dropdown-item danger" @click="handleLogout">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg>
              退出登录
            </a>
          </div>
        </transition>
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

const router = useRouter()
const showMenu = ref(false)
const username = ref(localStorage.getItem('username') || '用户')
const role = ref(localStorage.getItem('userRole') || 'USER')
const userInitial = ref((localStorage.getItem('username') || 'U').charAt(0).toUpperCase())

const navigate = (path) => {
  showMenu.value = false
  router.push(path)
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('userRole')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: var(--bg-page);
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  background: white;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: var(--transition);
}

.logo:hover {
  opacity: 0.8;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-icon svg {
  width: 20px;
  height: 20px;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--gray-900);
  line-height: 1.2;
}

.logo-sub {
  font-size: 11px;
  color: var(--gray-400);
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  position: relative;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: var(--transition);
}

.user-dropdown:hover {
  background: var(--gray-50);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--gray-800);
  line-height: 1.2;
}

.user-role {
  font-size: 11px;
  color: var(--gray-400);
}

.dropdown-arrow {
  width: 16px;
  height: 16px;
  color: var(--gray-400);
  transition: transform 0.2s;
}

.dropdown-arrow.open {
  transform: rotate(180deg);
}

.dropdown-mask {
  position: fixed;
  inset: 0;
  z-index: 99;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 220px;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  padding: 8px;
  z-index: 100;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
}

.dropdown-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
}

.dropdown-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--gray-800);
}

.dropdown-role {
  font-size: 12px;
  color: var(--gray-400);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--gray-600);
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: var(--transition);
  text-decoration: none;
}

.dropdown-item:hover {
  background: var(--gray-50);
  color: var(--gray-800);
}

.dropdown-item.danger {
  color: var(--danger);
}

.dropdown-item.danger:hover {
  background: var(--danger-bg);
}

.dropdown-item svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.main {
  min-height: calc(100vh - 64px);
  padding: var(--space-6);
  max-width: 1400px;
  margin: 0 auto;
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 768px) {
  .header {
    padding: 0 var(--space-4);
  }

  .logo-text {
    display: none;
  }

  .user-info {
    display: none;
  }

  .main {
    padding: var(--space-4);
  }
}
</style>