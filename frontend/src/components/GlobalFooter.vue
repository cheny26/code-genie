<template>
  <div class="global-footer">
    <div class="footer-content">
      <p class="copyright">© 2024 chen. All rights reserved.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// 这里可以添加任何需要的逻辑
</script>

<style scoped>
.global-footer {
  background: #fff;
  border-top: 1px solid #e8e8e8;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  text-align: center;
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}
</style>
