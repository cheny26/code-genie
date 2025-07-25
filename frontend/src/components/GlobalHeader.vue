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
      <!-- 登录按钮（暂时替代用户头像和昵称） -->
      <a-button type="primary" @click="handleLogin">
        登录
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

// 菜单配置
const menuConfig = [
  {
    key: '/',
    label: '首页',
    path: '/'
  },
  {
    key: '/about',
    label: '关于',
    path: '/about'
  }
]

// 当前选中的菜单项
const selectedKeys = ref<string[]>([route.path])

// 菜单项配置
const menuItems = computed<MenuProps['items']>(() => {
  return menuConfig.map(item => ({
    key: item.key,
    label: item.label
  }))
})

// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  const menuItem = menuConfig.find(item => item.key === key)
  if (menuItem) {
    router.push(menuItem.path)
  }
}

// 登录按钮点击处理
const handleLogin = () => {
  console.log('登录功能待实现')
  // 这里可以添加登录逻辑
}

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
</style>