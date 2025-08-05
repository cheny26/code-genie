<template>
  <div class="app-chat-container">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" @click="goBack" class="back-btn">
          <ArrowLeftOutlined />
        </a-button>
        <h1 class="app-title">{{ appInfo?.appName || '应用对话' }}</h1>
        <a-button shape="circle" :icon="h(EditOutlined)" style="border:none" @click="openEditModal"/>
      </div>
      <div class="header-right">
        <a-button
          type="default"
          @click="showAppDetails"
          style="margin-right: 8px;"
        >
          <InfoCircleOutlined />
          应用详情
        </a-button>
        <a-button
          type="primary"
          :loading="deploying"
          @click="deployApp"
          :disabled="!canDeploy"
        >
          <CloudUploadOutlined />
          部署应用
        </a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="chat-content" ref="chatContent">
      <!-- 左侧对话区域 -->
      <div class="chat-panel" :style="{ width: leftPanelWidth + 'px' }">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer" @scroll="checkIfUserAtBottom">
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message-item"
            :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }"
          >
            <div class="message-avatar">
              <a-avatar v-if="message.role === 'user'" :src="userStore.userAvatar" size="small">
                {{ userStore.userName?.charAt(0)?.toUpperCase() }}
              </a-avatar>
              <a-avatar v-else size="small" style="background-color: #1890ff;">
                <RobotOutlined />
              </a-avatar>
            </div>
            <div class="message-content">
              <div class="message-text">
                <MarkdownRenderer
                  v-if="message.role === 'assistant'"
                  :content="message.content"
                />
                <span v-else>{{ message.content }}</span>
              </div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>

          <!-- AI 正在输入 -->
          <div v-if="isGenerating" class="message-item ai-message">
            <div class="message-avatar">
              <a-avatar size="small" style="background-color: #1890ff;">
                <RobotOutlined />
              </a-avatar>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-container">
          <a-tooltip
            v-if="!isOwner"
            title="无法在别人的作品下对话哦~"
            placement="top"
          >
            <a-input-search
              v-model:value="userInput"
              placeholder="请输入您的需求..."
              :loading="isGenerating"
              @search="sendMessage"
              @keydown.enter="sendMessage"
              size="large"
              class="message-input"
              :disabled="!isOwner"
            >
              <template #enterButton>
                <a-button type="primary" :disabled="isGenerating || !isOwner">
                  <SendOutlined />
                </a-button>
              </template>
            </a-input-search>
          </a-tooltip>
          <a-input-search
            v-else
            v-model:value="userInput"
            placeholder="请输入您的需求..."
            :loading="isGenerating"
            @search="sendMessage"
            @keydown.enter="sendMessage"
            size="large"
            class="message-input"
          >
            <template #enterButton>
              <a-button type="primary" :disabled="isGenerating">
                <SendOutlined />
              </a-button>
            </template>
          </a-input-search>
        </div>
      </div>

      <!-- 拖拽分隔条 -->
      <div
        class="resize-handle"
        @mousedown="startResize"
        @touchstart="startResize"
      >
        <div class="resize-line"></div>
      </div>

      <!-- 右侧网页展示区域 -->
      <div class="preview-panel" :style="{ width: rightPanelWidth + 'px' }">
        <div class="preview-header">
          <h3>网页预览</h3>
          <div class="preview-actions">
            <a-button
              v-if="previewUrl"
              type="link"
              @click="openInNewTab"
              size="small"
            >
              新窗口打开
            </a-button>
          </div>
        </div>
        <div class="preview-content">
          <iframe
            v-if="previewUrl&&!isGenerating"
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
            @load="onIframeLoad"
            @error="onIframeError"
          ></iframe>
          <!-- 生成中动画 -->
          <div v-else class="generating-container">
            <div class="generating-content">
              <div class="generating-animation">
                <div class="code-blocks">
                  <div class="code-block" v-for="i in 6" :key="i"></div>
                </div>
                <div class="generating-icon">
                  <CodeOutlined />
                </div>
              </div>
              <h3>正在生成网站...</h3>
              <p>AI正在为您创建精美的网站，请稍候</p>
              <div class="progress-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 名称修改 -->
    <div>
    <a-modal v-model:open="open" title="编辑应用名称" @ok="handleOk">
      <a-input
        v-model:value="editingAppName"
        placeholder="请输入应用名称"
        :maxlength="50"
        show-count
      />
    </a-modal>
  </div>

    <!-- 应用详情悬浮窗 -->
    <AppDetailsModal
      :visible="appDetailsVisible"
      :app="appInfo"
      :show-actions="canManageApp"
      @close="appDetailsVisible = false"
      @edit="editApp"
      @delete="deleteAppConfirm"
    />

    <!-- 部署成功弹窗 -->
    <a-modal
      v-model:open="deploySuccessVisible"
      :width="480"
      :footer="null"
      :centered="true"
      class="deploy-success-modal"
    >
            <!-- 成功图标 -->
        <a-result
          status="success"
          title="网站部署成功"
          sub-title="你的网站已成功部署，可以通过以下链接访问："
        >
         <template #extra>
          <a-input-group compact>
            <a-input v-model:value="deploySuccessUrl" style="width: calc(100% - 100px);margin-bottom: 20px" />
            <a-button @click="copyDeployUrl">
              <template #icon><CopyOutlined /></template>
            </a-button>
          </a-input-group>
          <a-button @click="visitWebsite" type="primary" >
            访问网站
          </a-button>
          <a-button @click="closeDeploySuccess">
            关闭
          </a-button>
       </template>
     </a-result>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { h } from 'vue';
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CloudUploadOutlined,
  CopyOutlined ,
  RobotOutlined,
  SendOutlined,
  CodeOutlined,
  EditOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { getAppVoById, deployApp as deployAppApi, deleteApp, deleteAppByAdmin, updateApp } from '@/api/appController'
import AppDetailsModal from '@/components/AppDetailsModal.vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
type AppVO = API.AppVO

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 应用信息
const appInfo = ref<AppVO>()
const appId = computed(() => route.params.id)
const isViewMode = computed(() => route.query.view === '1')
const isOwner = computed(() => appInfo.value?.userId === userStore.userInfo?.id)

// 消息相关
interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const messages = ref<Message[]>([])
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

// 预览相关
const previewUrl = ref('')
const canDeploy = ref(false)
const deploying = ref(false)
const deployedUrl = ref('')

// 拖拽调整面板大小相关
const chatContent = ref<HTMLElement>()
const leftPanelWidth = ref(400) // 默认左侧面板宽度
const rightPanelWidth = ref(600) // 默认右侧面板宽度
const isResizing = ref(false)
const minPanelWidth = 300 // 最小面板宽度
let animationFrameId: number | null = null
let containerRect: DOMRect | null = null
// 名称修改
const open = ref(false)
const editingAppName = ref('')

// 部署成功弹窗
const deploySuccessVisible = ref(false)
const deploySuccessUrl = ref('')

const handleOk = async () => {
  if (!appInfo.value?.id || !editingAppName.value.trim()) {
    message.warning('请输入有效的应用名称')
    return
  }

  try {
    const response = await updateApp({
      id: appInfo.value.id,
      appName: editingAppName.value.trim()
    })

    if (response.data.code === 0) {
      // 更新本地应用信息
      if (appInfo.value) {
        appInfo.value.appName = editingAppName.value.trim()
      }
      message.success('应用名称修改成功')
      open.value = false
    } else {
      message.error(response.data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改应用名称失败:', error)
    message.error('修改失败，请重试')
  }
}
const closeDeploySuccess = () => {
  deploySuccessVisible.value = false
}

// 打开编辑模态框时初始化名称
const openEditModal = () => {
  editingAppName.value = appInfo.value?.appName || ''
  open.value = true
}

// 应用详情相关
const appDetailsVisible = ref(false)
const canManageApp = computed(() =>
  isOwner.value || userStore.isAdmin
)

// 加载应用信息
const loadAppInfo = async () => {
  try {
    console.log(appId.value)
    const response = await getAppVoById({ id: appId.value })
    if (response.data.code === 0 && response.data.data) {
      appInfo.value = response.data.data

      // 如果有初始提示词且不是查看模式，自动发送
      if (appInfo.value.initPrompt && !isViewMode.value) {
        await sendInitialMessage(appInfo.value.initPrompt)
      }
    } else {
      message.error('应用不存在')
      router.push('/')
    }
  } catch (error) {
    console.error('加载应用信息失败:', error)
    message.error('加载失败')
    router.push('/')
  }
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: prompt,
    timestamp: new Date()
  })

  await generateResponse(prompt)
}

// 发送消息
const sendMessage = async () => {
  const content = userInput.value.trim()
  if (!content || isGenerating.value) return

  // 检查权限
  if (!isOwner.value) {
    message.warning('无法在别人的作品下对话哦~')
    return
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content,
    timestamp: new Date()
  })

  userInput.value = ''
  await generateResponse(content)
}

// 滚动相关状态
const isUserAtBottom = ref(true)
const shouldAutoScroll = ref(true)

// 检查用户是否在消息底部
const checkIfUserAtBottom = () => {
  if (messagesContainer.value) {
    const container = messagesContainer.value
    const threshold = 50 // 50px的容差
    isUserAtBottom.value = container.scrollTop + container.clientHeight >= container.scrollHeight - threshold
  }
}

// 智能滚动到底部（只在用户在底部时才滚动）
const smartScrollToBottom = () => {
  if (shouldAutoScroll.value && isUserAtBottom.value && messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 生成AI回复
const generateResponse = async (userMessage: string) => {
  isGenerating.value = true
  let aiMessage: Message | null = null
  let hasError = false

  try {
    await nextTick()
    scrollToBottom() // 开始生成时滚动到底部

    // 使用SSE流式响应
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const url = `${baseUrl}/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(userMessage)}`

    const eventSource = new EventSource(url, {
      withCredentials: true
    })

    eventSource.onmessage = (event) => {
      try {
        const data = event.data
        if (data && data !== '[DONE]') {
          // 解析JSON格式的流式数据
          let content = ''
          try {
            const parsed = JSON.parse(data)
            content = parsed.d || ''
          } catch {
            // 如果不是JSON格式，直接使用原始数据
            content = data
          }

          // 如果还没有创建AI消息，创建一个
          if (!aiMessage && content) {
            aiMessage = {
              role: 'assistant',
              content: '',
              timestamp: new Date()
            } as Message
            messages.value.push(aiMessage)
            isGenerating.value = false // 开始接收内容后隐藏typing indicator
          }

          // 更新AI消息内容
          if (aiMessage && content) {
            aiMessage.content += content
            // 强制触发响应式更新
            messages.value = [...messages.value]
            nextTick(() => smartScrollToBottom())
          }
        }
      } catch (error) {
        console.error('解析SSE数据失败:', error)
      }
    }

    eventSource.onerror = (error) => {
      console.error('SSE连接错误:', error)
      eventSource.close()
      isGenerating.value = false
      hasError = true

      // 如果还没有AI消息，创建一个错误消息
      if (!aiMessage) {
        aiMessage = {
          role: 'assistant',
          content: '抱歉，生成过程中出现了错误，请重试。',
          timestamp: new Date()
        } as Message
        messages.value.push(aiMessage)
      } else if (!aiMessage.content) {
        aiMessage.content = '抱歉，生成过程中出现了错误，请重试。'
      }
      messages.value = [...messages.value]
    }

    // 监听 'done' 事件，表示代码生成完成
    eventSource.addEventListener('done', () => {
      console.log('代码生成完成，更新预览')
      eventSource.close()
      isGenerating.value = false
      canDeploy.value = true
      updatePreviewUrl()
    })

    eventSource.addEventListener('close', () => {
      eventSource.close()
      isGenerating.value = false
    })

    // 设置超时
    setTimeout(() => {
      if (eventSource.readyState !== EventSource.CLOSED) {
        eventSource.close()
        isGenerating.value = false
        if (!hasError) {
          if (!aiMessage) {
            aiMessage = {
              role: 'assistant',
              content: '请求超时，请重试。',
              timestamp: new Date()
            } as Message
            messages.value.push(aiMessage)
          } else if (!aiMessage.content) {
            aiMessage.content = '请求超时，请重试。'
          }
          messages.value = [...messages.value]
        }
      }
    }, 300000) // 5分钟超时

  } catch (error) {
    console.error('生成回复失败:', error)
    message.error('生成失败，请重试')
    isGenerating.value = false

    // 如果还没有AI消息，创建一个错误消息
    if (!aiMessage) {
      const errorMessage: Message = {
        role: 'assistant',
        content: '生成失败，请重试。',
        timestamp: new Date()
      }
      messages.value.push(errorMessage)
    } else if (aiMessage && (aiMessage as Message).content === '') {
      (aiMessage as Message).content = '生成失败，请重试。'
    }
    messages.value = [...messages.value]
  }
}

// 更新预览URL
const updatePreviewUrl = () => {
  if (appInfo.value) {
    const codeGenType = appInfo.value.codeGenType || 'website'
    const staticBaseUrl = import.meta.env.VITE_STATIC_BASE_URL || 'http://localhost:8080'
    const newPreviewUrl = `${staticBaseUrl}/static/${codeGenType}_${appInfo.value.id}/`

    console.log('更新预览URL:', {
      appId: appInfo.value.id,
      codeGenType,
      staticBaseUrl,
      newPreviewUrl
    })

    // 添加时间戳参数强制刷新iframe
    previewUrl.value = `${newPreviewUrl}?t=${Date.now()}`

    // 显示预览更新消息
    message.success('预览已更新')
  } else {
    console.error('应用信息不存在，无法更新预览URL')
  }
}

// 部署应用
const deployApp = async () => {
  if (!appInfo.value) return

  deploying.value = true
  try {
    const response = await deployAppApi({ appId: appInfo.value.id })
    if (response.data.code === 0 && response.data.data) {
      deployedUrl.value = response.data.data
      deploySuccessUrl.value = response.data.data

      // 显示美化的部署成功弹窗
      deploySuccessVisible.value = true
    } else {
      message.error(response.data.message || '部署失败')
    }
  } catch (error) {
    console.error('部署失败:', error)
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 复制部署链接
const copyDeployUrl = async () => {
  try {
    await navigator.clipboard.writeText(deploySuccessUrl.value)
    message.success('链接已复制到剪贴板')
  } catch {
    // 如果复制失败，显示链接
    message.info(`部署地址：${deploySuccessUrl.value}`)
  }
}

// 访问网站
const visitWebsite = () => {
  window.open(deploySuccessUrl.value, '_blank')
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// iframe加载成功
const onIframeLoad = () => {
  console.log('预览页面加载成功')
}

// iframe加载失败
const onIframeError = () => {
  console.error('预览页面加载失败')
  message.error('预览页面加载失败，请检查生成的代码是否正确')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}



// 格式化时间
const formatTime = (date: Date) => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 返回上一页
const goBack = () => {
  router.push('/')
}

// 显示应用详情
const showAppDetails = () => {
  appDetailsVisible.value = true
}


// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用确认
const deleteAppConfirm = async () => {
  if (!appInfo.value?.id) return

  try {
    let response
    if (userStore.isAdmin) {
      // 管理员删除
      response = await deleteAppByAdmin({ id: appInfo.value.id })
    } else {
      // 用户删除自己的应用
      response = await deleteApp({ id: appInfo.value.id })
    }

    if (response.data.code === 0) {
      message.success('删除成功')
      appDetailsVisible.value = false
      // 返回首页
      router.push('/')
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    message.error('删除失败，请重试')
  }
}

// 开始拖拽调整大小
const startResize = (e: MouseEvent | TouchEvent) => {
  isResizing.value = true

  // 缓存容器尺寸，避免重复计算
  if (chatContent.value) {
    containerRect = chatContent.value.getBoundingClientRect()
  }

  document.addEventListener('mousemove', handleResize, { passive: false })
  document.addEventListener('mouseup', stopResize)
  document.addEventListener('touchmove', handleResize, { passive: false })
  document.addEventListener('touchend', stopResize)
  e.preventDefault()
}

// 处理拖拽调整
const handleResize = (e: MouseEvent | TouchEvent) => {
  if (!isResizing.value || !containerRect) return

  // 取消之前的动画帧
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }

  // 使用 requestAnimationFrame 优化渲染性能
  animationFrameId = requestAnimationFrame(() => {
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX
    const newLeftWidth = clientX - containerRect!.left
    const containerWidth = containerRect!.width
    const newRightWidth = containerWidth - newLeftWidth - 8 // 减去分隔条宽度

    // 限制最小宽度
    if (newLeftWidth >= minPanelWidth && newRightWidth >= minPanelWidth) {
      leftPanelWidth.value = newLeftWidth
      rightPanelWidth.value = newRightWidth
    }
  })
}

// 停止拖拽调整
const stopResize = () => {
  isResizing.value = false

  // 清理动画帧
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }

  // 清理缓存的容器尺寸
  containerRect = null

  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  document.removeEventListener('touchmove', handleResize)
  document.removeEventListener('touchend', stopResize)
}

// 初始化面板大小
const initializePanelSizes = () => {
  if (chatContent.value) {
    const containerWidth = chatContent.value.clientWidth
    leftPanelWidth.value = Math.floor(containerWidth * 0.4) // 40%
    rightPanelWidth.value = Math.floor(containerWidth * 0.6) // 60%
  }
}

// 页面加载
onMounted(() => {
  loadAppInfo()
  nextTick(() => {
    initializePanelSizes()
    // 监听窗口大小变化
    window.addEventListener('resize', initializePanelSizes)
    // 初始化滚动状态
    checkIfUserAtBottom()
  })
})
</script>

<style scoped>
.app-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.chat-header {
  background: white;
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  padding: 4px 8px;
}

.app-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.chat-content {
  flex: 1;
  display: flex;
  height: calc(100vh - 73px);
  position: relative;
}

/* 拖拽时的性能优化 */
.chat-content.resizing {
  pointer-events: none;
}

.chat-content.resizing .chat-panel,
.chat-content.resizing .preview-panel {
  will-change: width;
  transform: translateZ(0); /* 启用硬件加速 */
}

.chat-content.resizing iframe {
  pointer-events: none;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  background: #F7F8FC;
  border-right: 1px solid #e8e8e8;
  min-width: 300px;
}

.messages-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.message-item.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  min-width: 100px;
}

.message-text {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-x: auto;
}

.user-message .message-text {
  background: #1890ff;
  color: white;
}

/* AI 消息的 Markdown 样式优化 */
.ai-message .message-text {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
}

.ai-message .message-text .markdown-renderer {
  padding: 16px;
}

/* 用户消息中的文本保持简单样式 */
.user-message .message-text {
  background: #1890ff;
  color: white;
  border: none;
  box-shadow: none;
  padding: 12px 16px;
}

.user-message .message-text span {
  color: inherit;
}

.message-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.user-message .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #f5f5f5;
  border-radius: 12px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #bfbfbf;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-container {
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  background: white;
}

.message-input {
  width: 100%;
}

.resize-handle {
  width: 8px;
  background: #f0f0f0;
  cursor: col-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: background-color 0.2s ease;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.resize-handle:hover {
  background: #d9d9d9;
}

.resize-line {
  width: 2px;
  height: 40px;
  background: #bfbfbf;
  border-radius: 1px;
}

.resize-handle:hover .resize-line {
  background: #8c8c8c;
}

.preview-panel {
  display: flex;
  flex-direction: column;
  background: white;
  min-width: 300px;
}

.preview-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-content {
  flex: 1;
  position: relative;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.placeholder-content {
  text-align: center;
  color: #8c8c8c;
}

.placeholder-content p {
  margin: 16px 0 0 0;
  font-size: 14px;
}

/* 生成中动画样式 */
.generating-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  overflow: hidden;
}

.generating-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
}

.generating-content {
  text-align: center;
  color: #595959;
  z-index: 1;
}

.generating-animation {
  position: relative;
  margin-bottom: 24px;
}

.code-blocks {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 16px;
  justify-items: center;
}

.code-block {
  width: 40px;
  height: 6px;
  background: #1890ff;
  border-radius: 3px;
  animation: codeGenerate 1.5s ease-in-out infinite;
}

.code-block:nth-child(1) { animation-delay: 0s; }
.code-block:nth-child(2) { animation-delay: 0.2s; }
.code-block:nth-child(3) { animation-delay: 0.4s; }
.code-block:nth-child(4) { animation-delay: 0.6s; }
.code-block:nth-child(5) { animation-delay: 0.8s; }
.code-block:nth-child(6) { animation-delay: 1s; }

.generating-icon {
  font-size: 32px;
  color: #1890ff;
  animation: pulse 2s ease-in-out infinite;
}

.generating-content h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.generating-content p {
  margin: 0 0 20px 0;
  font-size: 14px;
  color: #8c8c8c;
}

.progress-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.progress-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1890ff;
  animation: dotPulse 1.4s ease-in-out infinite;
}

.progress-dots span:nth-child(1) { animation-delay: 0s; }
.progress-dots span:nth-child(2) { animation-delay: 0.2s; }
.progress-dots span:nth-child(3) { animation-delay: 0.4s; }

/* 动画关键帧 */
@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

@keyframes codeGenerate {
  0%, 100% {
    transform: scaleX(1);
    opacity: 0.3;
  }
  50% {
    transform: scaleX(1.5);
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
}

@keyframes dotPulse {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-content {
    flex-direction: column;
  }

  .chat-panel {
    height: 60% !important;
    width: 100% !important;
  }

  .resize-handle {
    display: none;
  }

  .preview-panel {
    height: 40% !important;
    width: 100% !important;
    border-right: none;
    border-top: 1px solid #e8e8e8;
  }

  .message-content {
    max-width: 85%;
  }
}

/* 应用详情悬浮窗样式 */
.app-details-content {
  padding: 8px 0;
}

.app-basic-info {
  margin-bottom: 24px;
}

.app-basic-info h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-item .label {
  font-weight: 500;
  color: #595959;
  min-width: 80px;
}

.creator-info {
  display: flex;
  align-items: center;
}

.app-actions h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

</style>
