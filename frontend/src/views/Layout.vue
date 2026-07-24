<template>
  <div class="layout">
    <el-container>
      <el-aside width="200px" class="aside">
        <div class="logo">
          <div class="logo-mark">AI</div>
          <span>AI面试平台</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="menu"
          router
        >
          <el-menu-item index="/lobby">
            <el-icon><HomeFilled /></el-icon>
            <span>面试大厅</span>
          </el-menu-item>
          <el-menu-item index="/history">
            <el-icon><Clock /></el-icon>
            <span>历史记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRoute.meta.title }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown>
              <span class="el-dropdown-link">
                <el-avatar :size="32" src="" />
                <span class="username">{{ userInfo?.username || '用户' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #1A1A1A;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid #333;
}

.logo-mark {
  width: 32px;
  height: 32px;
  background: #C74634;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-weight: bold;
  font-size: 12px;
}

.logo span {
  font-size: 16px;
  font-weight: 500;
}

.menu {
  border-right: none;
  background: transparent;
}

.menu .el-menu-item {
  color: #C4C4C4;
}

.menu .el-menu-item:hover {
  background: #333;
  color: #fff;
}

.menu .el-menu-item.is-active {
  background: #C74634;
  color: #fff;
}

.header {
  background: #fff;
  border-bottom: 1px solid #E8E8E8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main {
  background: #F5F5F5;
  padding: 20px;
}
</style>