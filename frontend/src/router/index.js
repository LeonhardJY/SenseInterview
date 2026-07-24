import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
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
        path: 'interview/:taskId',
        name: 'Interview',
        component: () => import('@/views/Interview.vue'),
        meta: { title: '面试房间' }
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
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router