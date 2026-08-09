import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/lobby',
    children: [
      {
        path: 'lobby',
        name: 'Lobby',
        component: () => import('@/views/Lobby.vue'),
        meta: { title: '面试大厅' }
      },
      {
        path: 'report/:taskId',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
        meta: { title: '面试报告' }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/History.vue'),
        meta: { title: '历史记录' }
      },
      {
        path: 'resume',
        name: 'Resume',
        component: () => import('@/views/Resume.vue'),
        meta: { title: '简历管理' }
      },
      {
        path: 'question-bank',
        name: 'QuestionBank',
        component: () => import('@/views/QuestionBank.vue'),
        meta: { title: '面试题库' }
      },
      {
        path: 'agent-interview',
        name: 'AgentInterview',
        component: () => import('@/views/AgentInterview.vue'),
        meta: { title: 'Agent 面试' }
      },
      {
        path: 'admin',
        name: 'Admin',
        component: () => import('@/views/admin/Index.vue'),
        redirect: '/admin/dashboard',
        meta: { title: '管理后台', requiresAdmin: true },
        children: [
          {
            path: 'dashboard',
            name: 'AdminDashboard',
            component: () => import('@/views/admin/Dashboard.vue'),
            meta: { title: '数据看板' }
          },
          {
            path: 'users',
            name: 'AdminUsers',
            component: () => import('@/views/admin/Users.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'questions',
            name: 'AdminQuestions',
            component: () => import('@/views/admin/Questions.vue'),
            meta: { title: '题库管理' }
          },
          {
            path: 'interviews',
            name: 'AdminInterviews',
            component: () => import('@/views/admin/Interviews.vue'),
            meta: { title: '面试记录' }
          },
          {
            path: 'jobs',
            name: 'AdminJobs',
            component: () => import('@/views/admin/Jobs.vue'),
            meta: { title: '岗位管理' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole')

  // 未登录跳转登录页
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 管理员页面权限检查
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (userRole !== 'ADMIN') {
      next('/lobby')
      return
    }
  }

  next()
})

export default router