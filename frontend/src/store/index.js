import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户状态管理
 *
 * 为什么用 Store 而不是直接读 localStorage？
 * 1. 集中管理 — 所有页面从同一个地方拿数据，改一处全生效
 * 2. 响应式 — localStorage 不是响应式的，改了不会自动刷新页面
 * 3. 类型安全 — 写错属性名会报错，不会静默返回 undefined
 */
export const useUserStore = defineStore('user', () => {
  // ========== 状态（从 localStorage 初始化） ==========
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userRole = ref(localStorage.getItem('userRole') || 'USER')
  const userId = ref(parseInt(localStorage.getItem('userId') || '0'))
  const userInfo = ref(null)

  // ========== 计算属性 ==========
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userRole.value === 'ADMIN')
  const userInitial = computed(() => (username.value || 'U').charAt(0).toUpperCase())

  // ========== 操作方法 ==========

  /** 设置 token */
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   * @param {Object} info - 后端返回的用户信息，兼容 { id, username, role } 和 { userId, username, role }
   */
  function setUser(info) {
    if (!info) return
    userInfo.value = info
    username.value = info.username || info.nickname || ''
    userRole.value = info.role || 'USER'
    // 兼容 userId（登录接口）和 id（用户信息接口）
    userId.value = info.userId || info.id || 0

    // 同步到 localStorage（刷新页面后还能保持登录）
    localStorage.setItem('username', username.value)
    localStorage.setItem('userRole', userRole.value)
    localStorage.setItem('userId', String(userId.value))
  }

  /** 退出登录 — 清除所有用户状态 */
  function logout() {
    token.value = ''
    username.value = ''
    userRole.value = 'USER'
    userId.value = 0
    userInfo.value = null

    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userRole')
    localStorage.removeItem('userId')
  }

  return {
    token, username, userRole, userId, userInfo,
    isLoggedIn, isAdmin, userInitial,
    setToken, setUser, logout
  }
})