<template>
  <div class="register-container">
    <!-- 左侧品牌展示区域 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="logo-area">
          <img src="@/assets/logo.svg" alt="CodeGenie Logo" class="brand-logo" />
          <h1 class="brand-title">CodeGenie</h1>
        </div>
        <div class="brand-description">
          <h2>开启您的编程之旅</h2>
          <p>加入我们，体验AI驱动的智能编程</p>
          <div class="benefits">
            <div class="benefit-item">
              <div class="benefit-icon">✨</div>
              <div class="benefit-text">
                <h4>智能代码生成</h4>
                <p>AI助手帮您快速生成高质量代码</p>
              </div>
            </div>
            <div class="benefit-item">
              <div class="benefit-icon">⚡</div>
              <div class="benefit-text">
                <h4>提升开发效率</h4>
                <p>减少重复工作，专注核心业务逻辑</p>
              </div>
            </div>
            <div class="benefit-item">
              <div class="benefit-icon">🎯</div>
              <div class="benefit-text">
                <h4>精准代码建议</h4>
                <p>基于上下文的智能代码补全</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单区域 -->
    <div class="form-section">
      <div class="register-card">
        <div class="register-header">
          <h2>创建账户</h2>
          <p>填写以下信息开始您的编程之旅</p>
        </div>

        <a-form
          :model="registerForm"
          :rules="registerRules"
          @finish="handleRegister"
          @finishFailed="handleRegisterFailed"
          layout="vertical"
          class="register-form"
        >
          <a-form-item  name="email">
            <a-input
              v-model:value="registerForm.email"
              placeholder="邮箱"
              size="large"
              :prefix="h(MailOutlined)"
            />
          </a-form-item>

          <a-form-item  name="userName">
            <a-input
              v-model:value="registerForm.userName"
              placeholder="用户名"
              size="large"
              :prefix="h(UserOutlined)"
            />
          </a-form-item>

          <a-form-item  name="userPassword">
            <a-input-password
              v-model:value="registerForm.userPassword"
              placeholder="密码"
              size="large"
              :prefix="h(LockOutlined)"
            />
          </a-form-item>

          <a-form-item  name="checkPassword">
            <a-input-password
              v-model:value="registerForm.checkPassword"
              placeholder="确认密码"
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
              注册
            </a-button>
          </a-form-item>
        </a-form>

        <div class="register-footer">
          <span>已有账户？</span>
          <a-button type="link" @click="goToLogin">立即登录</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const userStore = useUserStore()

// 注册表单数据
const registerForm = ref<API.UserRegisterRequest>({
  email: '',
  userName: '',
  userPassword: '',
  checkPassword: ''
})

// 邮箱验证规则
const validateEmail = (_rule: unknown, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请输入邮箱地址'))
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    return Promise.reject(new Error('请输入有效的邮箱地址'))
  }
  return Promise.resolve()
}

// 确认密码验证规则
const validateConfirmPassword = (_rule: unknown, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请确认密码'))
  }
  if (value !== registerForm.value.userPassword) {
    return Promise.reject(new Error('两次输入的密码不一致'))
  }
  return Promise.resolve()
}

// 表单验证规则
const registerRules: Record<string, Rule[]> = {
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度应在2-20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  checkPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 处理注册
const handleRegister = async (values: API.UserRegisterRequest) => {
  const result = await userStore.register(values)
  if (result.success) {
    // 跳转到登录页面
    router.push('/login')
  }
}

// 处理注册失败
const handleRegisterFailed = (errorInfo: unknown) => {
  console.log('注册表单验证失败:', errorInfo)
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
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
  text-align: left;
  color: white;
  z-index: 1;
  position: relative;
  max-width: 500px;
}

.logo-area {
  text-align: center;
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
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px 0;
  opacity: 0.95;
  text-align: center;
}

.brand-description > p {
  font-size: 18px;
  margin: 0 0 40px 0;
  opacity: 0.8;
  text-align: center;
}

.benefits {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 40px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.benefit-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  flex-shrink: 0;
}

.benefit-text h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  opacity: 0.95;
}

.benefit-text p {
  font-size: 14px;
  margin: 0;
  opacity: 0.8;
  line-height: 1.5;
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

.register-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h2 {
  color: #1f2937;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.register-header p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

.register-form {
  margin-bottom: 24px;
}

.register-footer {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.register-footer .ant-btn-link {
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
  .register-container {
    flex-direction: column;
  }

  .brand-section {
    min-height: 50vh;
    padding: 20px;
  }

  .brand-content {
    text-align: center;
  }

  .brand-title {
    font-size: 36px;
  }

  .brand-description h2 {
    font-size: 24px;
  }

  .benefits {
    gap: 16px;
  }

  .benefit-item {
    flex-direction: column;
    text-align: center;
    align-items: center;
  }

  .form-section {
    padding: 20px;
  }

  .register-card {
    padding: 20px;
  }
}
</style>
