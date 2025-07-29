import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: {
        title: '首页',
        showInMenu: true,
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: {
        hideLayout: true // 登录页面不显示布局
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: {
        hideLayout: true // 注册页面不显示布局
      }
    },
    {
      path: '/user-manage',
      name: 'userManage',
      component: () => import('../views/UserManageView.vue'),
      meta: {
        title: '用户管理',
        showInMenu: true,
        requiresAuth: true, // 需要登录才能访问
        requiresAdmin: true, // 需要管理员权限
        order: 10
      }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        title: '个人资料',
        showInMenu: false,
        requiresAuth: true, // 需要登录才能访问
      }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/AboutView.vue'),
      meta: {
        title: '关于',
        showInMenu: true,
      }
    },
  ],
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 初始化用户信息（如果还没有初始化）
  if (!userStore.userInfo) {
    userStore.initUserInfo()
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/')
    return
  }

  // 如果已登录用户访问登录或注册页面，重定向到首页
  if ((to.name === 'login' || to.name === 'register') && userStore.isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router
