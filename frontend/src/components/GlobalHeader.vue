<template>
  <div class="global-header" :class="{ 'scrolled': isScrolled }">
    <div class="header-left">
      <!-- Logo 和网站标题 -->
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
        <span class="site-title">CodeGenie</span>
      </div>

      <!-- 导航菜单 -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        class="nav-menu"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </div>

    <div class="header-right">
      <!-- 未登录状态 -->
      <div v-if="!userStore.isLoggedIn" class="auth-buttons">
        <a-button type="primary" @click="goToLogin">登录</a-button>
        <a-button  @click="goToRegister">注册</a-button>
      </div>

      <!-- 已登录状态 -->
      <div v-else class="user-info">
        <a-dropdown>
          <a-avatar :src="userStore.userAvatar" :size="32">
              {{ userStore.userName?.charAt(0)?.toUpperCase() }}
            </a-avatar>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="profile">
                <UserOutlined />
                个人资料
              </a-menu-item>
              <a-menu-item key="settings">
                <SettingOutlined />
                设置
              </a-menu-item>
              <a-menu-item key="logout">
                <LogoutOutlined />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {  UserOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 滚动状态
const isScrolled = ref(false)

// 节流函数
const throttle = <T extends (...args: unknown[]) => void>(func: T, delay: number) => {
  let timeoutId: number | null = null
  let lastExecTime = 0
  return function (this: unknown, ...args: Parameters<T>) {
    const currentTime = Date.now()

    if (currentTime - lastExecTime > delay) {
      func.apply(this, args)
      lastExecTime = currentTime
    } else {
      if (timeoutId) {
        clearTimeout(timeoutId)
      }
      timeoutId = setTimeout(() => {
        func.apply(this, args)
        lastExecTime = Date.now()
      }, delay - (currentTime - lastExecTime))
    }
  }
}

// 滚动监听函数
const handleScroll = throttle(() => {
  isScrolled.value = window.scrollY > 10
}, 16) // 约60fps

// 从路由配置中获取菜单配置
const menuConfig = computed(() => {
  return router.getRoutes()
    .filter(route => {
      // 只显示标记为 showInMenu 的路由
      if (!route.meta?.showInMenu) return false

      // 检查权限要求
      if (route.meta?.requiresAuth && !userStore.isLoggedIn) return false
      if (route.meta?.requiresAdmin && !userStore.isAdmin) return false

      return true
    })
    .map(route => ({
      key: route.path,
      label: route.meta?.title || route.name,
      path: route.path,
    }))
})

// 当前选中的菜单项
const selectedKeys = ref<string[]>([route.path])

// 菜单项配置
const menuItems = computed<MenuProps['items']>(() => {
  return menuConfig.value.map(item => ({
    key: item.key,
    label: item.label
  }))
})

// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  const menuItem = menuConfig.value.find(item => item.key === key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}

// 处理用户下拉菜单点击
const handleUserMenuClick = ({ key }: { key: string }) => {
  switch (key) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      // TODO: 实现设置页面
      console.log('设置功能待实现')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  const result = await userStore.logout()
  if (result.success) {
    router.push('/')
  }
}

// 组件挂载时初始化用户信息
onMounted(() => {
  userStore.initUserInfo()
  // 如果本地有用户信息，尝试获取最新的用户信息
  if (userStore.isLoggedIn) {
    userStore.fetchCurrentUser()
  }

  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)
  // 初始检查滚动位置
  handleScroll()
})

// 组件卸载时移除滚动监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化，更新选中的菜单项
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})
</script>

<style scoped>
.global-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #f7f8fc;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 1px solid transparent;
}

.global-header.scrolled {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(20px);
  /* border-bottom: 1px solid rgba(0, 0, 0, 0.1); */
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.logo-section {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo {
  height: 32px;
  width: 32px;
  margin-right: 12px;
}

.site-title {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 滚动时Logo和标题的样式调整 */
.global-header.scrolled .logo {
  opacity: 0.9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.global-header.scrolled .site-title {
  color: #1890ff;
  text-shadow: 0 1px 2px rgba(24, 144, 255, 0.1);
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  line-height: 64px;
  font-weight: 500;
  background: transparent;
}

/* 滚动时菜单样式 */
.global-header.scrolled .nav-menu {
  background: transparent;
}

/* 菜单项样式优化 */
.nav-menu :deep(.ant-menu-item) {
  margin: 0 4px;
  border-bottom: none !important;
}

.nav-menu :deep(.ant-menu-item:hover) {
  color: #1890ff !important;
  /* border-bottom: none !important; */
}

.nav-menu :deep(.ant-menu-item-selected) {
  border-bottom: none !important;
}

.nav-menu :deep(.ant-menu-item::after) {
  color: #1890ff;
  display: none !important;
}

.nav-menu :deep(.ant-menu-item-selected::after) {
  display: none !important;
}

.header-right {
  display: flex;
  align-items: center;
}

.auth-buttons {
  display: flex;
  gap: 12px;
}

/* 滚动时按钮样式调整 */
.global-header.scrolled .auth-buttons .ant-btn {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.global-header.scrolled .auth-buttons .ant-btn:not(.ant-btn-primary) {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(24, 144, 255, 0.3);
  color: #1890ff;
}

.global-header.scrolled .auth-buttons .ant-btn-primary {
  /* background: rgba(24, 144, 255, 0.9); */
  border-color: rgba(24, 144, 255, 0.9);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
}

.user-button {
  display: flex;
  align-items: center;
  gap: 8px;
  height: auto;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.username {
  font-weight: 500;
  color: #333;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 滚动时用户信息样式调整 */
.global-header.scrolled .user-button {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(24, 144, 255, 0.2);
}

.global-header.scrolled .user-button:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(24, 144, 255, 0.4);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.global-header.scrolled .username {
  color: rgba(0, 0, 0, 0.85);
}
</style>
