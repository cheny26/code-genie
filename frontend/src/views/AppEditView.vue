<template>
  <div class="app-edit-container">
    <div class="page-header">
      <a-button type="text" @click="goBack" class="back-btn">
        <ArrowLeftOutlined />
        返回
      </a-button>
      <h1>{{ isEdit ? '编辑应用' : '应用详情' }}</h1>
    </div>

    <div class="edit-content" v-if="app">
      <a-form
        :model="form"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
        ref="formRef"
      >
        <!-- 应用封面 -->
        <a-form-item label="应用封面" name="cover">
          <div class="cover-upload">
            <div class="cover-preview">
              <img v-if="form.cover" :src="form.cover" alt="应用封面" />
              <div v-else class="default-cover">
                <CodeOutlined />
                <span>暂无封面</span>
              </div>
            </div>
            <div class="cover-actions" v-if="canEdit">
              <a-input
                v-model:value="form.cover"
                placeholder="请输入封面图片URL"
                style="margin-bottom: 8px;"
              />
              <a-button type="dashed" size="small">
                <UploadOutlined />
                上传封面
              </a-button>
            </div>
          </div>
        </a-form-item>

        <!-- 应用名称 -->
        <a-form-item label="应用名称" name="appName">
          <a-input
            v-model:value="form.appName"
            placeholder="请输入应用名称"
            :disabled="!canEdit"
            :max-length="50"
            show-count
          />
        </a-form-item>

        <!-- 应用描述 -->
        <a-form-item label="应用描述" name="appDescription">
          <a-textarea
            v-model:value="form.appDescription"
            placeholder="请输入应用描述"
            :disabled="!canEdit"
            :rows="4"
            :max-length="200"
            show-count
          />
        </a-form-item>

        <!-- 初始提示词 -->
        <a-form-item label="初始提示词">
          <a-textarea
            :value="app.initPrompt"
            placeholder="暂无初始提示词"
            :rows="3"
            disabled
          />
        </a-form-item>

        <!-- 应用类型 -->
        <a-form-item label="应用类型">
          <a-input :value="getCodeGenTypeLabel(app.codeGenType || '')" disabled />
        </a-form-item>

        <!-- 优先级（仅管理员可见） -->
        <a-form-item label="优先级" name="priority" v-if="isAdmin">
          <a-select v-model:value="form.priority" :disabled="!canEdit">
            <a-select-option :value="0">普通</a-select-option>
            <a-select-option :value="50">高</a-select-option>
            <a-select-option :value="99">精选</a-select-option>
          </a-select>
          <div class="form-help">精选应用将在首页优先展示</div>
        </a-form-item>

        <!-- 创建信息 -->
        <a-form-item label="创建信息">
          <div class="create-info">
            <div class="info-item">
              <span class="label">创建者：</span>
              <a-avatar :src="app.user?.userAvatar" size="small">
                {{ app.user?.userName?.charAt(0)?.toUpperCase() }}
              </a-avatar>
              <span class="value">{{ app.user?.userName }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ formatDate(app.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间：</span>
              <span class="value">{{ formatDate(app.updateTime) }}</span>
            </div>
          </div>
        </a-form-item>

        <!-- 部署信息 -->
        <a-form-item label="部署信息" v-if="app.deployKey">
          <div class="deploy-info">
            <a-tag color="green">已部署</a-tag>
            <a-button type="link" @click="openDeployUrl" v-if="deployUrl">
              <LinkOutlined />
              访问应用
            </a-button>
          </div>
        </a-form-item>

        <!-- 操作按钮 -->
        <a-form-item v-if="canEdit">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">
              保存修改
            </a-button>
            <a-button @click="resetForm">
              重置
            </a-button>
            <a-button type="link" @click="viewApp">
              <EyeOutlined />
              预览应用
            </a-button>
          </a-space>
        </a-form-item>

        <!-- 只读模式的操作按钮 -->
        <a-form-item v-else>
          <a-space>
            <a-button type="primary" @click="viewApp">
              <EyeOutlined />
              查看应用
            </a-button>
            <a-button type="link" @click="openDeployUrl" v-if="deployUrl">
              <LinkOutlined />
              访问部署版本
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-else-if="loading">
      <a-spin size="large" />
    </div>

    <!-- 错误状态 -->
    <div class="error-container" v-else>
      <a-result
        status="404"
        title="应用不存在"
        sub-title="抱歉，您访问的应用不存在或已被删除"
      >
        <template #extra>
          <a-button type="primary" @click="goBack">
            返回
          </a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/userStore'
import {
  ArrowLeftOutlined,
  CodeOutlined,
  UploadOutlined,
  EyeOutlined,
  LinkOutlined
} from '@ant-design/icons-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import type { AppVO, AppUpdateRequest, AppAdminUpdateRequest } from '@/api/typings'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const app = ref<AppVO | null>(null)
const loading = ref(true)
const submitting = ref(false)
const formRef = ref()

// 表单数据
const form = reactive({
  appName: '',
  appDescription: '',
  cover: '',
  priority: 0
})

// 表单验证规则
const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度在1-50个字符', trigger: 'blur' }
  ]
}

// 计算属性
const appId = computed(() => route.params.id)
const isAdmin = computed(() => userStore.userInfo?.userRole === 'admin')
const isOwner = computed(() => app.value?.userId === userStore.userInfo?.id)
const canEdit = computed(() => isAdmin.value || isOwner.value)
const isEdit = computed(() => canEdit.value)

const deployUrl = computed(() => {
  if (!app.value?.deployKey) return ''
  return `http://localhost:8080/static/${app.value.codeGenType}_${app.value.id}/`
})

// 加载应用详情
const loadApp = async () => {
  loading.value = true
  try {
    const response = await getAppVoById({ id: appId.value })
    if (response.data.code === 0 && response.data.data) {
      app.value = response.data.data
      // 初始化表单数据
      form.appName = response.data.data.appName || ''
      form.appDescription = response.data.data.appDescription || ''
      form.cover = response.data.data.cover || ''
      form.priority = response.data.data.priority || 0
    } else {
      message.error(response.data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载应用详情失败:', error)
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!app.value) return
  
  submitting.value = true
  try {
    let response
    
    if (isAdmin.value) {
      // 管理员更新
      const updateData: AppAdminUpdateRequest = {
        id: app.value.id,
        appName: form.appName,
        appDescription: form.appDescription,
        cover: form.cover,
        priority: form.priority
      }
      response = await updateAppByAdmin(updateData)
    } else {
      // 普通用户更新
      const updateData: AppUpdateRequest = {
        id: app.value.id,
        appName: form.appName,
        appDescription: form.appDescription
      }
      response = await updateApp(updateData)
    }
    
    if (response.data.code === 0) {
      message.success('保存成功')
      // 重新加载数据
      await loadApp()
    } else {
      message.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (!app.value) return
  form.appName = app.value.appName || ''
  form.appDescription = app.value.appDescription || ''
  form.cover = app.value.cover || ''
  form.priority = app.value.priority || 0
}

// 查看应用
const viewApp = () => {
  router.push(`/app/chat/${appId.value}`)
}

// 打开部署URL
const openDeployUrl = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 页面加载
onMounted(() => {
  loadApp()
})
</script>

<style scoped>
.app-edit-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
  background: white;
  border-radius: 8px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.back-btn {
  margin-right: 16px;
  padding: 4px 8px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #262626;
}

.edit-content {
  max-width: 600px;
}

.cover-upload {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.cover-preview {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #fafafa;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #bfbfbf;
  font-size: 12px;
}

.default-cover .anticon {
  font-size: 24px;
}

.cover-actions {
  flex: 1;
}

.form-help {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.create-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-weight: 500;
  color: #595959;
  min-width: 80px;
}

.info-item .value {
  color: #262626;
}

.deploy-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.error-container {
  text-align: center;
  padding: 40px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-edit-container {
    padding: 16px;
  }
  
  .cover-upload {
    flex-direction: column;
  }
  
  .cover-preview {
    width: 100px;
    height: 100px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-item .label {
    min-width: auto;
  }
}
</style>