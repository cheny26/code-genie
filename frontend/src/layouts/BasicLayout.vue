<template>
  <a-layout class="basic-layout" >
    <!-- 顶部导航栏 -->
    <a-layout-header class="header" >
      <GlobalHeader />
    </a-layout-header>

    <!-- 中间内容区域 -->
    <a-layout-content class="content" >
      <div class="content-wrapper" >
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
  position: relative;
  z-index: 100;
  width: 100%;
  padding: 0;
  height: 64px;
  background: transparent;
}


.content {
  flex: 1;
}


.content-wrapper {
  margin: 0 auto;
  min-height: calc(100vh - 200px);
  background:#f7f8fc;
}

.footer {
  border-top: 1px solid #e8e8e8;
  padding: 0;
}

</style>
