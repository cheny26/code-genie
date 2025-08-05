<template>
  <div class="about-container">
    <a-card>
      <div class="about-content">
        <h1>关于 CodeGenie</h1>
        <p>CodeGenie 是一个智能代码生成平台，致力于提高开发效率。</p>

        <a-divider />

        <h2>主要特性</h2>
        <ul>
          <li>🚀 智能代码生成</li>
          <li>🎯 AI 驱动开发</li>
          <li>⚡ 高效开发体验</li>
          <li>🔧 现代化工具链</li>
        </ul>

        <a-divider />

        <h2>技术栈</h2>
        <a-row :gutter="[16, 16]">
          <a-col :span="12">
            <h3>前端</h3>
            <ul>
              <li>Vue 3</li>
              <li>TypeScript</li>
              <li>Ant Design Vue</li>
              <li>Vite</li>
            </ul>
          </a-col>
          <a-col :span="12">
            <h3>状态管理</h3>
            <ul>
              <li>Pinia</li>
              <li>Vue Router</li>
            </ul>
          </a-col>
        </a-row>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
// 关于页面组件
</script>

<style scoped>
.about-container {
  padding: 24px;
  max-width: 80%;
  margin: 0 auto;
  /* background: #f7f8fc; */
}

.about-content {
  padding: 24px;
}

.about-content h1 {
  color: #1890ff;
  margin-bottom: 16px;
}

.about-content h2 {
  color: #262626;
  margin-top: 24px;
  margin-bottom: 16px;
}

.about-content h3 {
  color: #595959;
  margin-bottom: 12px;
}

.about-content ul {
  margin-left: 20px;
}

.about-content li {
  margin-bottom: 8px;
  line-height: 1.6;
}

.about-content p {
  font-size: 16px;
  line-height: 1.6;
  color: #595959;
}
</style>
