<template>
  <div class="login-container">
    <!-- 左侧品牌展示区域 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="logo-area">
          <img src="@/assets/logo.svg" alt="CodeGenie Logo" class="brand-logo" />
          <h1 class="brand-title">CodeGenie</h1>
        </div>
        <div class="brand-description">
          <h2>智能代码生成平台</h2>
          <p>让AI助力您的编程之旅</p>
          <div class="features">
            <div class="feature-item">
              <div class="feature-icon">🚀</div>
              <span>高效开发</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🤖</div>
              <span>AI驱动</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">💡</div>
              <span>智能提示</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="form-section">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户继续使用</p>
        </div>

        <a-form
          :model="loginForm"
          :rules="loginRules"
          @finish="handleLogin"
          @finishFailed="handleLoginFailed"
          layout="vertical"
          class="login-form"
        >
          <a-form-item name="userName">
            <a-input
              v-model:value="loginForm.userName"
              placeholder="用户名"
              size="large"
              :prefix="h(UserOutlined)"
            />
          </a-form-item>

          <a-form-item  name="userPassword">
            <a-input-password
              v-model:value="loginForm.userPassword"
              placeholder="密码"
              size="large"
              :prefix="h(LockOutlined)"
            />
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <div class="login-footer">
          <span>还没有账户？</span>
          <a-button type="link" @click="goToRegister">立即注册</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const userStore = useUserStore()

// 登录表单数据
const loginForm = ref<API.UserLoginRequest>({
  userName: '',
  userPassword: ''
})

// 表单验证规则
const loginRules: Record<string, Rule[]> = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度应在2-20个字符之间', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async (values: API.UserLoginRequest) => {
  const result = await userStore.login(values)
  if (result.success) {
    // 跳转到首页
    router.push('/')
  }
}

// 处理登录失败
const handleLoginFailed = (errorInfo: unknown) => {
  console.log('登录表单验证失败:', errorInfo)
}

// 跳转到注册页面
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

/* 左侧品牌展示区域 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="white" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="white" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="white" opacity="0.1"/><circle cx="10" cy="60" r="0.5" fill="white" opacity="0.1"/><circle cx="90" cy="40" r="0.5" fill="white" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  pointer-events: none;
}

.brand-content {
  text-align: center;
  color: white;
  z-index: 1;
  position: relative;
}

.logo-area {
  margin-bottom: 40px;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  filter: brightness(0) invert(1);
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -1px;
}

.brand-description h2 {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 16px 0;
  opacity: 0.95;
}

.brand-description p {
  font-size: 18px;
  margin: 0 0 40px 0;
  opacity: 0.8;
}

.features {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.feature-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.feature-item span {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.9;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  color: #1f2937;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.login-header p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.login-footer {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.login-footer .ant-btn-link {
  padding: 0;
  height: auto;
  font-size: 14px;
  font-weight: 500;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

:deep(.ant-input-affix-wrapper:focus-within) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.ant-btn-primary) {
  border-radius: 8px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.2s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .brand-section {
    min-height: 40vh;
    padding: 20px;
  }

  .brand-title {
    font-size: 36px;
  }

  .brand-description h2 {
    font-size: 24px;
  }

  .features {
    gap: 20px;
  }

  .form-section {
    padding: 20px;
  }

  .login-card {
    padding: 20px;
  }
}
</style>
