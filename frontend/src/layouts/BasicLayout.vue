<template>
  <a-layout class="basic-layout" :class="{ 'home-layout': isHomePage }">
    <!-- 顶部导航栏 -->
    <a-layout-header class="header" :class="{ 'home-header': isHomePage }">
      <GlobalHeader />
    </a-layout-header>

    <!-- 中间内容区域 -->
    <a-layout-content class="content" :class="{ 'home-content': isHomePage }">
      <div class="content-wrapper" :class="{ 'home-content-wrapper': isHomePage }">
        <RouterView />
      </div>
    </a-layout-content>

    <!-- 底部版权信息 -->
    <a-layout-footer class="footer" :class="{ 'home-footer': isHomePage }">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()
const isHomePage = computed(() => route.path === '/')
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.home-layout {
  background: transparent;
}

.header {
  position: relative;
  z-index: 100;
  width: 100%;
  background: transparent;
  padding: 0;
  height: 64px;
}

.home-header {
  background: transparent;
}

.content {
  flex: 1;
}

.home-content {
  padding: 0;
  /* 只需要header高度，首页通常有自己的间距设计 */
  background: transparent;
}

.content-wrapper {
  margin: 0 auto;
  min-height: calc(100vh - 200px);
  background: #f7f8fc;
}

.home-content-wrapper {
  margin: 0;
  min-height: calc(100vh - 64px - 80px); /* 减去header和footer的高度 */
  width: 100%;
}

.footer {
  border-top: 1px solid #e8e8e8;
  padding: 0;
}

.home-footer {
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}
</style>
