<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <a-layout-header class="header">
      <GlobalHeader />
    </a-layout-header>
    
    <!-- 中间内容区域 -->
    <a-layout-content class="content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </a-layout-content>
    
    <!-- 底部版权信息 -->
    <a-layout-footer class="footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.content {
  flex: 1;
  padding: 24px;
  background: #f0f2f5;
}

.content-wrapper {
  margin: 0 auto;
  background: #fff;
  padding: 24px;
  /* border-radius: 8px; */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 200px);
}

.footer {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 0;
}
</style>