<template>
  <div class="app-manage-container">
    <div class="page-header">
      <h1>应用管理</h1>
      <p>管理系统中的所有应用</p>
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
            <a-form-item label="生成类型">
              <a-select
                v-model:value="searchForm.codeGenType"
                placeholder="请选择生成类型"
                allow-clear
                style="width: 100%"
              >
                <a-select-option
                  v-for="option in CODE_GEN_TYPES"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
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

    <!-- 应用列表 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="apps"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <!-- 应用封面 -->
        <template #cover="{ record }">
          <div class="app-cover-cell">
            <img v-if="record.cover" :src="record.cover" :alt="record.appName" />
            <div v-else class="default-cover">
              <CodeOutlined />
            </div>
          </div>
        </template>

        <!-- 应用名称 -->
        <template #appName="{ record }">
          <div class="app-name-cell">
            <a @click="viewApp(record.id)" class="app-name-link">
              {{ record.appName }}
            </a>
            <div class="app-prompt" v-if="record.initPrompt">
              {{ record.initPrompt.substring(0, 50) }}{{ record.initPrompt.length > 50 ? '...' : '' }}
            </div>
          </div>
        </template>

        <!-- 创建者 -->
        <template #creator="{ record }">
          <div class="creator-cell">
            <a-avatar :src="record.user?.userAvatar" size="small">
              {{ record.user?.userName?.charAt(0)?.toUpperCase() }}
            </a-avatar>
            <span class="creator-name">{{ record.user?.userName }}</span>
          </div>
        </template>

        <!-- 生成类型 -->
        <template #codeGenType="{ record }">
          <a-tag color="blue">
            {{ getCodeGenTypeLabel(record.codeGenType) }}
          </a-tag>
        </template>

        <!-- 优先级 -->
        <template #priority="{ record }">
          <a-tag :color="getPriorityColor(record.priority)">
            {{ getPriorityText(record.priority) }}
          </a-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ record }">
          <a-tag v-if="record.deployKey" color="green">已部署</a-tag>
          <a-tag v-else color="orange">未部署</a-tag>
        </template>

        <!-- 创建时间 -->
        <template #createTime="{ record }">
          {{ formatDate(record.createTime) }}
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="editApp(record.id)">
              <EditOutlined />
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="setFeatured(record)"
              :disabled="record.priority === 99"
            >
              <StarOutlined />
              {{ record.priority === 99 ? '已精选' : '精选' }}
            </a-button>
            <a-popconfirm
              title="确定要删除这个应用吗？"
              @confirm="deleteApp(record.id)"
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
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  EditOutlined,
  DeleteOutlined,
  StarOutlined,
  CodeOutlined,
  SearchOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import { listAppVoByPageAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPES, getCodeGenTypeLabel } from '@/constants/codeGenType'

type AppVO = API.AppVO
const router = useRouter()

// 数据
const apps = ref<AppVO[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  appName: '',
  codeGenType: undefined as string | undefined
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

// 表格列定义
const columns = [
  {
    title: '封面',
    dataIndex: 'cover',
    key: 'cover',
    width: 80,
    slots: { customRender: 'cover' }
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    width: 200,
    slots: { customRender: 'appName' }
  },
  {
    title: '创建者',
    dataIndex: 'user',
    key: 'creator',
    width: 120,
    slots: { customRender: 'creator' }
  },
  {
    title: '类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 120,
    slots: { customRender: 'codeGenType' }
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 100,
    slots: { customRender: 'priority' }
  },
  {
    title: '状态',
    dataIndex: 'deployKey',
    key: 'status',
    width: 100,
    slots: { customRender: 'status' }
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

// 加载应用列表
const loadApps = async () => {
  loading.value = true
  try {
    const response = await listAppVoByPageAdmin({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      appName: searchForm.appName || undefined,
      codeGenType: searchForm.codeGenType,
      sortField: 'createTime',
      sortOrder: 'desc'
    })

    if (response.data.code === 0 && response.data.data) {
      apps.value = response.data.data.records || []
      pagination.total = Number(response.data.data.totalRow) || 0
    }
  } catch (error) {
    console.error('加载应用列表失败:', error)
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.current = 1
  loadApps()
}

// 重置搜索
const resetSearch = () => {
  searchForm.appName = ''
  searchForm.codeGenType = undefined
  pagination.current = 1
  loadApps()
}

// 表格变化处理
const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadApps()
}

// 查看应用
const viewApp = (appId: number) => {
  router.push(`/app/chat/${appId}`)
}

// 编辑应用
const editApp = (appId: number) => {
  router.push(`/app/edit/${appId}`)
}

// 设置精选
const setFeatured = async (app: AppVO) => {
  try {
    const response = await updateAppByAdmin({
      id: app.id,
      priority: 99
    })

    if (response.data.code === 0) {
      message.success('设置精选成功')
      loadApps()
    } else {
      message.error(response.data.message || '设置失败')
    }
  } catch (error) {
    console.error('设置精选失败:', error)
    message.error('设置失败')
  }
}

// 删除应用
const deleteApp = async (appId: number) => {
  try {
    const response = await deleteAppByAdmin({ id: appId })
    if (response.data.code === 0) {
      message.success('删除成功')
      loadApps()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    message.error('删除失败')
  }
}

// 获取优先级颜色
const getPriorityColor = (priority?: number) => {
  if (priority === 99) return 'gold'
  if (priority && priority > 50) return 'blue'
  return 'default'
}

// 获取优先级文本
const getPriorityText = (priority?: number) => {
  if (priority === 99) return '精选'
  if (priority && priority > 50) return '高'
  return '普通'
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 页面加载
onMounted(() => {
  loadApps()
})
</script>

<style scoped>
.app-manage-container {
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

.app-cover-cell {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.app-cover-cell img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  color: #bfbfbf;
  font-size: 16px;
}

.app-name-cell {
  max-width: 180px;
}

.app-name-link {
  font-weight: 500;
  color: #1890ff;
  text-decoration: none;
}

.app-name-link:hover {
  text-decoration: underline;
}

.app-prompt {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
  line-height: 1.4;
}

.creator-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.creator-name {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-manage-container {
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
