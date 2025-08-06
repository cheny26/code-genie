<template>
  <div class="chat-manage-container">
    <div class="page-header">
      <h1>对话管理</h1>
      <p>管理系统中的所有对话记录</p>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="应用名称">
              <a-input
                v-model:value="searchForm.appName"
                placeholder="请输入应用名称"
                allow-clear
                @press-enter="handleSearch"
              />
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="消息类型">
              <a-select
                v-model:value="searchForm.messageType"
                placeholder="请选择消息类型"
                allow-clear
                style="width: 100%"
              >
                <a-select-option value="user">用户消息</a-select-option>
                <a-select-option value="assistant">AI回复</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item label="用户名称">
              <a-input
                v-model:value="searchForm.userName"
                placeholder="请输入用户名称"
                allow-clear
                @press-enter="handleSearch"
              />
            </a-form-item>
          </a-col>

          <a-col :span="6">
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleSearch">
                  <SearchOutlined />
                  搜索
                </a-button>
                <a-button @click="resetSearch">
                  <ReloadOutlined />
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
      </a-row>
    </div>

    <!-- 对话列表 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="chatHistories"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <!-- 应用信息 -->
        <template #appInfo="{ record }">
          <div class="app-info-cell">
            <a @click="viewApp(record.appId)" class="app-name-link">
              {{ record.appName }}
            </a>
          </div>
        </template>

        <!-- 用户信息 -->
        <template #userInfo="{ record }">
          <div class="user-info-cell">
            <span class="user-name">{{ record.userName }}</span>
          </div>
        </template>

        <!-- 消息类型 -->
        <template #messageType="{ record }">
          <a-tag :color="record.messageType === 'user' ? 'blue' : 'green'">
            {{ record.messageType === 'user' ? '用户消息' : 'AI回复' }}
          </a-tag>
        </template>

        <!-- 消息内容 -->
        <template #message="{ record }">
          <div class="message-cell">
            <div class="message-preview">
              {{ record.message?.substring(0, 100) }}{{ (record.message?.length || 0) > 100 ? '...' : '' }}
            </div>
          </div>
        </template>

        <!-- 创建时间 -->
        <template #createTime="{ record }">
          {{ formatDate(record.createTime) }}
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="viewChatDetail(record)">
              <EyeOutlined />
              查看
            </a-button>
            <a-button type="link" size="small" @click="viewApp(record.appId)">
              <MessageOutlined />
              进入对话
            </a-button>
            <a-popconfirm
              title="确定要删除这条对话记录吗？"
              @confirm="deleteChatHistoryRecord(record.id)"
              ok-text="确定"
              cancel-text="取消"
            >
              <a-button type="link" size="small" danger>
                <DeleteOutlined />
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </div>

    <!-- 对话详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="对话详情"
      :width="800"
      :footer="null"
    >
      <div v-if="selectedChatHistory" class="chat-detail-content">
        <div class="detail-info">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="应用名称">
              {{ selectedChatHistory.appName }}
            </a-descriptions-item>
            <a-descriptions-item label="用户名称">
              {{ selectedChatHistory.userName }}
            </a-descriptions-item>
            <a-descriptions-item label="消息类型">
              <a-tag :color="selectedChatHistory.messageType === 'user' ? 'blue' : 'green'">
                {{ selectedChatHistory.messageType === 'user' ? '用户消息' : 'AI回复' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ formatDate(selectedChatHistory.createTime) }}
            </a-descriptions-item>
          </a-descriptions>
        </div>

        <div class="message-content">
          <h4>消息内容：</h4>
          <div class="message-text">
            <MarkdownRenderer
              v-if="selectedChatHistory.messageType === 'assistant'"
              :content="selectedChatHistory.message || ''"
            />
            <pre v-else class="user-message-text">{{ selectedChatHistory.message }}</pre>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  MessageOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { listChatHistoryVoByPage, deleteChatHistory } from '@/api/chatHistoryController'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

type ChatHistoryVO = API.ChatHistoryVO
const router = useRouter()

// 数据
const chatHistories = ref<ChatHistoryVO[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  appName: '',
  messageType: undefined as string | undefined,
  userName: ''
})

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 详情弹窗
const detailModalVisible = ref(false)
const selectedChatHistory = ref<ChatHistoryVO>()

// 表格列定义
const columns = [
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    width: 150,
    slots: { customRender: 'appInfo' }
  },
  {
    title: '用户名称',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
    slots: { customRender: 'userInfo' }
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 100,
    slots: { customRender: 'messageType' }
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    width: 300,
    slots: { customRender: 'message' }
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150,
    slots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    slots: { customRender: 'action' }
  }
]

// 加载对话历史列表
const loadChatHistories = async () => {
  loading.value = true
  try {
    const response = await listChatHistoryVoByPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      appName: searchForm.appName || undefined,
      messageType: searchForm.messageType,
      userName: searchForm.userName || undefined,
      sortField: 'createTime',
      sortOrder: 'desc'
    })

    if (response.data.code === 0 && response.data.data) {
      chatHistories.value = response.data.data.records || []
      pagination.total = Number(response.data.data.totalRow) || 0
    }
  } catch (error) {
    console.error('加载对话历史列表失败:', error)
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  loadChatHistories()
}

// 重置搜索
const resetSearch = () => {
  searchForm.appName = ''
  searchForm.messageType = undefined
  searchForm.userName = ''
  pagination.current = 1
  loadChatHistories()
}

// 表格变化处理
const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadChatHistories()
}

// 查看应用对话
const viewApp = (appId?: number) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 查看对话详情
const viewChatDetail = (chatHistory: ChatHistoryVO) => {
  selectedChatHistory.value = chatHistory
  detailModalVisible.value = true
}

// 删除对话记录
const deleteChatHistoryRecord = async (chatId?: number) => {
  if (!chatId) return

  try {
    const response = await deleteChatHistory({ id: chatId })
    if (response.data.code === 0) {
      message.success('删除成功')
      loadChatHistories()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除对话记录失败:', error)
    message.error('删除失败')
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 页面加载
onMounted(() => {
  loadChatHistories()
})
</script>

<style scoped>
.chat-manage-container {
  padding: 24px;
  background: white;
  border-radius: 8px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #262626;
}

.page-header p {
  color: #8c8c8c;
  margin: 0;
}

.search-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.table-section {
  background: white;
}

.app-info-cell {
  max-width: 140px;
}

.app-name-link {
  font-weight: 500;
  color: #1890ff;
  text-decoration: none;
}

.app-name-link:hover {
  text-decoration: underline;
}

.user-info-cell {
  display: flex;
  align-items: center;
}

.user-name {
  font-size: 14px;
}

.message-cell {
  max-width: 280px;
}

.message-preview {
  font-size: 14px;
  line-height: 1.4;
  color: #595959;
  word-break: break-word;
}

.chat-detail-content {
  padding: 16px 0;
}

.detail-info {
  margin-bottom: 24px;
}

.message-content h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.message-text {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.user-message-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  color: #262626;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-manage-container {
    padding: 16px;
  }

  .search-section {
    padding: 12px;
  }

  .search-section .ant-row {
    flex-direction: column;
    gap: 12px;
  }

  .search-section .ant-col {
    width: 100% !important;
  }
}
</style>
