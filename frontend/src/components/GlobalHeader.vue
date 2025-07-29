<template>
  <div class="global-header">
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
        <a-button @click="goToLogin">登录</a-button>
        <a-button type="primary" @click="goToRegister">注册</a-button>
      </div>

      <!-- 已登录状态 -->
      <div v-else class="user-info">
        <a-dropdown>
          <a-button type="text" class="user-button">
            <a-avatar :src="userStore.userAvatar" :size="32">
              {{ userStore.userName?.charAt(0)?.toUpperCase() }}
            </a-avatar>
            <span class="username">{{ userStore.userName }}</span>
          </a-button>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {  UserOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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
  background: #fff;
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
}

.nav-menu {
  flex: 1;
  border-bottom: none;
  line-height: 64px;
}

.header-right {
  display: flex;
  align-items: center;
}

.auth-buttons {
  display: flex;
  gap: 12px;
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
}

.username {
  font-weight: 500;
  color: #333;
}
</style>
