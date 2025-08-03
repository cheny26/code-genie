<template>
  <div class="app-chat-container">
    <!-- 顶部栏 -->
    <div class="chat-header">
      <div class="header-left">
        <a-button type="text" @click="goBack" class="back-btn">
          <ArrowLeftOutlined />
        </a-button>
        <h1 class="app-title">{{ appInfo?.appName || '应用对话' }}</h1>
      </div>
      <div class="header-right">
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
    <div class="chat-content">
      <!-- 左侧对话区域 -->
      <div class="chat-panel">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
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
              <div class="message-text" v-html="formatMessage(message.content)"></div>
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
          <a-input-search
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

      <!-- 右侧网页展示区域 -->
      <div class="preview-panel">
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
            v-if="previewUrl"
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
          ></iframe>
          <div v-else class="preview-placeholder">
            <div class="placeholder-content">
              <CodeOutlined style="font-size: 48px; color: #bfbfbf;" />
              <p>网站生成完成后将在此处展示</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  CloudUploadOutlined,
  RobotOutlined,
  SendOutlined,
  CodeOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { getAppVoById, deployApp as deployAppApi } from '@/api/appController'
import type { AppVO } from '@/api/typings'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 应用信息
const appInfo = ref<AppVO>()
const appId = computed(() => route.params.id)

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

// 加载应用信息
const loadAppInfo = async () => {
  try {
    console.log(appId.value)
    const response = await getAppVoById({ id: appId.value })
    if (response.data.code === 0 && response.data.data) {
      appInfo.value = response.data.data

      // 如果有初始提示词，自动发送
      if (appInfo.value.initPrompt) {
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

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content,
    timestamp: new Date()
  })

  userInput.value = ''
  await generateResponse(content)
}

// 生成AI回复
const generateResponse = async (userMessage: string) => {
  isGenerating.value = true

  try {
    // 创建AI消息
    const aiMessage: Message = {
      role: 'assistant',
      content: '',
      timestamp: new Date()
    }
    messages.value.push(aiMessage)

    await nextTick()
    scrollToBottom()

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
          } catch (parseError) {
            // 如果不是JSON格式，直接使用原始数据
            content = data
          }
          
          // 更新AI消息内容
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.role === 'assistant') {
            lastMessage.content += content
            nextTick(() => scrollToBottom())
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
      
      // 如果没有收到任何内容，显示错误消息
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage && lastMessage.role === 'assistant' && !lastMessage.content) {
        lastMessage.content = '抱歉，生成过程中出现了错误，请重试。'
      }
    }

    eventSource.addEventListener('close', () => {
      eventSource.close()
      isGenerating.value = false
      
      // 检查是否生成完成，更新预览
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage && lastMessage.role === 'assistant') {
        if (lastMessage.content.includes('网站生成完成') || 
            lastMessage.content.includes('代码生成完成') ||
            lastMessage.content.includes('生成完成')) {
          canDeploy.value = true
          updatePreviewUrl()
        }
      }
    })

    // 设置超时
    setTimeout(() => {
      if (eventSource.readyState !== EventSource.CLOSED) {
        eventSource.close()
        isGenerating.value = false
      }
    }, 300000) // 5分钟超时

  } catch (error) {
    console.error('生成回复失败:', error)
    message.error('生成失败，请重试')
    // 移除失败的AI消息
    messages.value.pop()
    isGenerating.value = false
  }
}

// 更新预览URL
const updatePreviewUrl = () => {
  if (appInfo.value) {
    const codeGenType = appInfo.value.codeGenType || 'website'
    const staticBaseUrl = import.meta.env.VITE_STATIC_BASE_URL || 'http://localhost:8080'
    previewUrl.value = `${staticBaseUrl}/static/${codeGenType}_${appInfo.value.id}/`
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
      message.success('部署成功！')
      
      // 显示部署成功的消息
      const deployMessage: Message = {
        role: 'assistant',
        content: `🎉 应用部署成功！\n\n部署地址：${response.data.data}\n\n您可以通过上述链接访问您的应用。`,
        timestamp: new Date()
      }
      messages.value.push(deployMessage)
      
      await nextTick()
      scrollToBottom()
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

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化消息内容
const formatMessage = (content: string) => {
  // 简单的markdown转换
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
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

// 页面加载
onMounted(() => {
  loadAppInfo()
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
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-right: 1px solid #e8e8e8;
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

.user-message .message-content {
  text-align: right;
}

.message-text {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-text {
  background: #1890ff;
  color: white;
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

.preview-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-content {
    flex-direction: column;
  }

  .chat-panel {
    height: 60%;
  }

  .preview-panel {
    height: 40%;
    border-right: none;
    border-top: 1px solid #e8e8e8;
  }

  .message-content {
    max-width: 85%;
  }
}
</style>
