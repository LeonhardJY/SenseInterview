<template>
  <div class="layout">
    <header class="topnav">
      <div class="topnav-inner">
        <div class="brand-logo" @click="$router.push('/lobby')">
          <div class="brand-logo-mark">AI</div>
          <div class="brand-logo-text">
            AI模拟面试
            <small>MULTIMODAL INTERVIEW</small>
          </div>
        </div>
        <div class="nav-right">
          <div class="user-menu">
            <button class="user-avatar-btn" @click="showUserMenu = !showUserMenu">
              <div class="avatar-circle">{{ userInitial }}</div>
            </button>
          </div>
        </div>
      </div>
      <div v-if="showUserMenu" class="dropdown-backdrop" @click="showUserMenu = false"></div>
      <div v-if="showUserMenu" class="dropdown-panel" @click.stop>
        <div class="dp-user">
          <div class="dp-avatar">{{ userInitial }}</div>
          <div>
            <div class="dp-username">{{ username }}</div>
            <div class="dp-role">{{ role === 'ADMIN' ? '管理员' : '普通用户' }}</div>
          </div>
        </div>
        <div class="dp-divider"></div>
        <div class="dp-item" @click="goTo('/resume'); showUserMenu=false">个人中心</div>
        <div class="dp-item" @click="goTo('/history'); showUserMenu=false">练习记录</div>
        <div class="dp-divider"></div>
        <div class="dp-item dp-danger" @click="handleLogout">退出登录</div>
      </div>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const showUserMenu = ref(false)
const username = ref(localStorage.getItem('username') || '用户')
const role = ref(localStorage.getItem('userRole') || 'USER')
const userInitial = ref((localStorage.getItem('username') || 'U').charAt(0).toUpperCase())

const goTo = (path) => router.push(path)

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #F5F5F5; }
* { box-sizing: border-box; margin: 0; padding: 0; }

.topnav { background: #1A1A1A; color: #fff; position: sticky; top: 0; z-index: 40; }
.topnav-inner { max-width: 1400px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.brand-logo { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.brand-logo-mark { width: 32px; height: 32px; background: #C74634; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; color: #fff; }
.brand-logo-text { font-size: 14px; letter-spacing: 0.02em; white-space: nowrap; color: #fff; }
.brand-logo-text small { display: block; font-family: sans-serif; font-size: 9px; letter-spacing: 0.1em; color: #9E9E9E; text-transform: uppercase; margin-top: 1px; }
.nav-right { display: flex; align-items: center; }
.user-avatar-btn { background: transparent; border: none; cursor: pointer; padding: 4px; border-radius: 999px; }
.user-avatar-btn:hover { background: rgba(255,255,255,0.08); }
.avatar-circle { width: 32px; height: 32px; border-radius: 50%; background: #C74634; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; }

.dropdown-backdrop { position: fixed; inset: 0; z-index: 49; }
.dropdown-panel { position: absolute; top: 52px; right: 24px; width: 200px; background: #fff; border: 1px solid #E8E8E8; border-radius: 6px; box-shadow: 0 8px 24px rgba(0,0,0,0.12); padding: 6px; z-index: 50; }
.dp-user { display: flex; align-items: center; gap: 10px; padding: 10px 12px; }
.dp-avatar { width: 32px; height: 32px; border-radius: 50%; background: #C74634; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; flex-shrink: 0; }
.dp-username { font-size: 14px; color: #1A1A1A; font-weight: 500; }
.dp-role { font-size: 11px; color: #9E9E9E; font-family: sans-serif; }
.dp-item { padding: 10px 12px; font-size: 13px; color: #333; border-radius: 4px; cursor: pointer; font-family: sans-serif; }
.dp-item:hover { background: #F5F5F5; }
.dp-danger { color: #D32F2F; }
.dp-danger:hover { background: #FEF2F2; }
.dp-divider { height: 1px; background: #E8E8E8; margin: 4px 0; }

.content { max-width: 1400px; margin: 0 auto; padding: 24px; }
</style>