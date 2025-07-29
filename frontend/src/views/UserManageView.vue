<template>
  <div class="user-manage-container">
    <!-- 搜索和操作区域 -->
    <div class="search-section">
      <a-card>
        <a-row :gutter="16" align="middle">
          <a-col :span="6">
            <a-input
              v-model:value="searchForm.userName"
              placeholder="搜索用户名"
              allow-clear
              @press-enter="handleSearch"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="searchForm.userRole"
              placeholder="选择用户角色"
              allow-clear
              style="width: 100%"
            >
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button type="primary" @click="handleSearch" :loading="loading">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
              <a-button @click="handleReset">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-col>
          <a-col :span="6" style="text-align: right">
            <a-button type="primary" @click="showAddModal">
              <template #icon><PlusOutlined /></template>
              添加用户
            </a-button>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 用户列表 -->
    <div class="table-section">
      <a-card>
        <a-table
          :columns="columns"
          :data-source="userList"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
          row-key="id"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'userAvatar'">
              <a-avatar :src="record.userAvatar" :size="40">
                <template #icon><UserOutlined /></template>
              </a-avatar>
            </template>
            <template v-else-if="column.key === 'userRole'">
              <a-tag :color="record.userRole === 'admin' ? 'red' : 'blue'">
                {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'createTime'">
              {{ formatDate(record.createTime) }}
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a-button type="link" size="small" @click="handleEdit(record)">
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除这个用户吗？"
                  @confirm="handleDelete(record.id)"
                  ok-text="确定"
                  cancel-text="取消"
                >
                  <a-button type="link" size="small" danger>
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </a-card>
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="userForm"
        :rules="formRules"
        layout="vertical"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="用户名" name="userName">
              <a-input v-model:value="userForm.userName" placeholder="请输入用户名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="用户角色" name="userRole">
              <a-select v-model:value="userForm.userRole" placeholder="请选择用户角色">
                <a-select-option value="user">普通用户</a-select-option>
                <a-select-option value="admin">管理员</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="头像链接" name="userAvatar">
          <a-input v-model:value="userForm.userAvatar" placeholder="请输入头像链接" />
        </a-form-item>
        <a-form-item label="用户简介" name="userProfile">
          <a-textarea
            v-model:value="userForm.userProfile"
            placeholder="请输入用户简介"
            :rows="3"
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
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import {
  listUserVoByPage,
  addUser,
  updateUser,
  deleteUser
} from '@/api/userController'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const modalVisible = ref(false)
const isEdit = ref(false)
const userList = ref<API.UserVO[]>([])
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  userName: '',
  userRole: undefined as string | undefined
})

// 用户表单
const userForm = reactive({
  id: undefined as number | undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user'
})

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条记录`
})

// 表格列配置
const columns = [
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    width: 80,
    align: 'center'
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 150
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 200
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 100,
    align: 'center'
  },
  {
    title: '积分',
    dataIndex: 'points',
    key: 'points',
    width: 100,
    align: 'center'
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    align: 'center'
  }
]

// 表单验证规则
const formRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ]
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params: API.UserQueryRequest = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      userName: searchForm.userName || undefined,
      userRole: searchForm.userRole || undefined
    }

    const response = await listUserVoByPage(params)
    if (response.data.code === 0 && response.data.data) {
      userList.value = response.data.data.records || []
      pagination.total = response.data.data.totalRow || 0
    } else {
      message.error(response.data.message || '获取用户列表失败')
    }
  } catch (error) {
    message.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

// 重置搜索
const handleReset = () => {
  searchForm.userName = ''
  searchForm.userRole = undefined
  pagination.current = 1
  fetchUserList()
}

// 表格变化处理
const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchUserList()
}

// 显示添加用户弹窗
const showAddModal = () => {
  isEdit.value = false
  modalVisible.value = true
  resetForm()
}

// 编辑用户
const handleEdit = (record: API.UserVO) => {
  isEdit.value = true
  modalVisible.value = true
  userForm.id = record.id
  userForm.userName = record.userName || ''
  userForm.userAvatar = record.userAvatar || ''
  userForm.userProfile = record.userProfile || ''
  userForm.userRole = record.userRole || 'user'
}

// 删除用户
const handleDelete = async (id: number) => {
  try {
    const response = await deleteUser({ id })
    if (response.data.code === 0) {
      message.success('删除用户成功')
      fetchUserList()
    } else {
      message.error(response.data.message || '删除用户失败')
    }
  } catch (error) {
    message.error('删除用户失败')
    console.error('删除用户失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      // 编辑用户
      const response = await updateUser({
        id: userForm.id,
        userName: userForm.userName,
        userAvatar: userForm.userAvatar,
        userProfile: userForm.userProfile,
        userRole: userForm.userRole
      })
      if (response.data.code === 0) {
        message.success('更新用户成功')
        modalVisible.value = false
        fetchUserList()
      } else {
        message.error(response.data.message || '更新用户失败')
      }
    } else {
      // 添加用户
      const response = await addUser({
        userName: userForm.userName,
        userAvatar: userForm.userAvatar,
        userProfile: userForm.userProfile,
        userRole: userForm.userRole
      })
      if (response.data.code === 0) {
        message.success('添加用户成功')
        modalVisible.value = false
        fetchUserList()
      } else {
        message.error(response.data.message || '添加用户失败')
      }
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 取消弹窗
const handleCancel = () => {
  modalVisible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  userForm.id = undefined
  userForm.userName = ''
  userForm.userAvatar = ''
  userForm.userProfile = ''
  userForm.userRole = 'user'
  formRef.value?.resetFields()
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-manage-container {
  /* padding: 24px;
  background-color: #8f2121; */
  min-height: calc(100vh - 200px);
}


.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.page-header p {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.search-section {
  margin-bottom: 16px;
}

.table-section {
  background: white;
  border-radius: 8px;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f5f5f5;
}

:deep(.ant-card) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.ant-btn) {
  border-radius: 6px;
}

:deep(.ant-input) {
  border-radius: 6px;
}

:deep(.ant-select .ant-select-selector) {
  border-radius: 6px;
}

:deep(.ant-modal .ant-modal-content) {
  border-radius: 8px;
}
</style>
