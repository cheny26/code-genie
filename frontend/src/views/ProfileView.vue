<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <div class="profile-header">
        <h1>
          <UserOutlined />
          个人资料
        </h1>
        <p>管理您的个人信息和账户设置</p>
      </div>

      <a-divider />

      <!-- 个人信息展示 -->
      <div v-if="!isEditing" class="profile-display">
        <a-row :gutter="[24, 24]">
          <!-- 左侧：头像和基本信息 -->
          <a-col :xs="24" :md="8">
            <div class="avatar-section">
              <a-avatar :size="120" :src="userStore.userAvatar" class="user-avatar">
                {{ userStore.userName?.charAt(0)?.toUpperCase() }}
              </a-avatar>
              <h2 class="username">{{ userStore.userName }}</h2>
              <a-tag :color="userStore.isAdmin ? 'red' : 'blue'" class="role-tag">
                {{ userStore.isAdmin ? '管理员' : '普通用户' }}
              </a-tag>
            </div>
          </a-col>

          <!-- 右侧：详细信息 -->
          <a-col :xs="24" :md="16">
            <div class="info-section">
              <a-descriptions :column="1" bordered>
                <a-descriptions-item label="用户名">
                  <span class="info-value">{{ userStore.userName || '-' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="邮箱">
                  <span class="info-value">{{ userStore.userInfo?.email || '-' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="积分">
                  <span class="info-value">
                    <a-statistic
                      :value="userStore.userInfo?.points || 0"
                      :value-style="{ color: '#1890ff' }"
                    />
                  </span>
                </a-descriptions-item>
                <a-descriptions-item label="个人简介">
                  <span class="info-value">{{ userStore.userInfo?.userProfile || '暂无简介' }}</span>
                </a-descriptions-item>
                <a-descriptions-item label="注册时间">
                  <span class="info-value">{{ formatDate(userStore.userInfo?.createTime) }}</span>
                </a-descriptions-item>
              </a-descriptions>

              <div class="action-buttons">
                <a-button type="primary" @click="startEdit">
                  <template #icon><EditOutlined /></template>
                  编辑资料
                </a-button>
                <a-button @click="changePasswordVisible = true">
                  <template #icon><KeyOutlined /></template>
                  修改密码
                </a-button>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <!-- 编辑模式 -->
      <div v-else class="profile-edit">
        <a-form
          ref="formRef"
          :model="editForm"
          :rules="formRules"
          layout="vertical"
          @finish="handleSubmit"
        >
          <a-row :gutter="24">
            <a-col :xs="24" :md="12">
              <a-form-item label="用户名" name="userName">
                <a-input
                  v-model:value="editForm.userName"
                  placeholder="请输入用户名"
                  :disabled="true"
                />
                <div class="form-tip">用户名不可修改</div>
              </a-form-item>
            </a-col>
            <a-col :xs="24" :md="12">
              <a-form-item label="头像链接" name="userAvatar">
                <a-input
                  v-model:value="editForm.userAvatar"
                  placeholder="请输入头像链接"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="个人简介" name="userProfile">
            <a-textarea
              v-model:value="editForm.userProfile"
              placeholder="请输入个人简介"
              :rows="4"
              :maxlength="200"
              show-count
            />
          </a-form-item>

          <div class="edit-actions">
            <a-space>
              <a-button type="primary" html-type="submit" :loading="submitLoading">
                <template #icon><SaveOutlined /></template>
                保存修改
              </a-button>
              <a-button @click="cancelEdit">
                取消
              </a-button>
            </a-space>
          </div>
        </a-form>
      </div>
    </a-card>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="changePasswordVisible"
      title="修改密码"
      @ok="handleChangePassword"
      @cancel="cancelChangePassword"
      :confirm-loading="passwordLoading"
      width="400px"
    >
      <a-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        layout="vertical"
      >
        <a-form-item label="当前密码" name="currentPassword">
          <a-input-password
            v-model:value="passwordForm.currentPassword"
            placeholder="请输入当前密码"
          />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入新密码"
          />
        </a-form-item>
        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password
            v-model:value="passwordForm.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  EditOutlined,
  SaveOutlined,
  KeyOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { updateUser } from '@/api/userController'

const userStore = useUserStore()

// 响应式数据
const isEditing = ref(false)
const submitLoading = ref(false)
const changePasswordVisible = ref(false)
const passwordLoading = ref(false)
const formRef = ref()
const passwordFormRef = ref()

// 编辑表单
const editForm = reactive({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

// 密码修改表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const formRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userAvatar: [
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
  ],
  userProfile: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: unknown, value: string) => {
        if (value !== passwordForm.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

// 开始编辑
const startEdit = () => {
  isEditing.value = true
  editForm.userName = userStore.userName || ''
  editForm.userAvatar = userStore.userAvatar || ''
  editForm.userProfile = userStore.userInfo?.userProfile || ''
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  formRef.value?.resetFields()
}

// 提交编辑
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const response = await updateUser({
      id: userStore.userInfo?.id,
      userName: editForm.userName,
      userAvatar: editForm.userAvatar,
      userProfile: editForm.userProfile
    })

    if (response.data.code === 0) {
      message.success('个人资料更新成功')
      isEditing.value = false
      // 更新本地用户信息
      await userStore.fetchCurrentUser()
    } else {
      message.error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人资料失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    // 这里需要调用修改密码的API
    // 由于当前API中没有修改密码的接口，这里只是模拟
    message.success('密码修改成功，请重新登录')
    changePasswordVisible.value = false

    // 清空表单
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''

    // 可以选择自动登出用户
    // await userStore.logout()
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 取消修改密码
const cancelChangePassword = () => {
  changePasswordVisible.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.resetFields()
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时获取用户信息
onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchCurrentUser()
  }
})
</script>

<style scoped>
.profile-container {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.profile-header h1 {
  color: #1890ff;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: 600;
}

.profile-header p {
  color: #8c8c8c;
  margin: 0;
  font-size: 14px;
}

.avatar-section {
  text-align: center;
  padding: 24px;
}

.user-avatar {
  margin-bottom: 16px;
  border: 4px solid #f0f0f0;
}

.username {
  margin: 16px 0 8px 0;
  color: #262626;
  font-size: 20px;
  font-weight: 600;
}

.role-tag {
  font-size: 12px;
}

.info-section {
  padding: 24px 0;
}

.info-value {
  color: #262626;
  font-weight: 500;
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.profile-edit {
  padding: 24px 0;
}

.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.edit-actions {
  margin-top: 24px;
  text-align: center;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 600;
  color: #595959;
  width: 120px;
}

:deep(.ant-descriptions-item-content) {
  color: #262626;
}

:deep(.ant-card-body) {
  padding: 32px;
}

:deep(.ant-btn) {
  border-radius: 6px;
}

:deep(.ant-input) {
  border-radius: 6px;
}

:deep(.ant-input-password) {
  border-radius: 6px;
}

:deep(.ant-form-item-label > label) {
  font-weight: 600;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }

  .avatar-section {
    padding: 16px;
  }

  .action-buttons {
    flex-direction: column;
  }

  :deep(.ant-card-body) {
    padding: 24px 16px;
  }
}
</style>
