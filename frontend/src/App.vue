<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { healthCheck } from './api/healthController';

const route = useRoute()

// 检查当前路由是否需要隐藏布局
const hideLayout = computed(() => route.meta?.hideLayout)

healthCheck().then(res => {
  console.log(res)
})
</script>

<template>
    <a-config-provider :locale="zhCN">
      <BasicLayout v-if="!hideLayout" />
      <RouterView v-else />
    </a-config-provider>
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  height: 100%;
}
</style>
