<template>
  <a-modal
    :open="visible"
    title="应用详情"
    :footer="null"
    width="500px"
    @cancel="$emit('close')"
  >
    <div class="app-details-content" v-if="app">
      <!-- 应用基础信息 -->
      <div class="app-basic-info">
        <h3>应用基础信息</h3>
        <div class="info-item">
          <span class="label">应用名称：</span>
          <span>{{ app.appName }}</span>
        </div>
        <div class="info-item" v-if="app.initPrompt">
          <span class="label">初始提示：</span>
          <span class="prompt-text">{{ app.initPrompt }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建者：</span>
          <div class="creator-info">
            <a-avatar
              :src="app.user?.userAvatar"
              size="small"
              style="margin-right: 8px;"
            >
              {{ app.user?.userName?.charAt(0)?.toUpperCase() }}
            </a-avatar>
            <span>{{ app.user?.userName || '未知用户' }}</span>
          </div>
        </div>
        <div class="info-item">
          <span class="label">创建时间：</span>
          <span>{{ formatCreateTime(app.createTime) }}</span>
        </div>
        <div class="info-item" v-if="app.codeGenType">
          <span class="label">生成类型：</span>
          <a-tag color="blue">{{ getCodeGenTypeLabel(app.codeGenType) }}</a-tag>
        </div>
        <div class="info-item" v-if="app.deployKey">
          <span class="label">部署状态：</span>
          <a-tag color="green">已部署</a-tag>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div class="app-actions" v-if="showActions">
        <h3>操作</h3>
        <a-space>
          <slot name="actions" :app="app">
            <a-button type="primary" @click="$emit('edit', app.id)">
              <EditOutlined />
              修改
            </a-button>
            <a-popconfirm
              title="确定要删除这个应用吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="$emit('delete', app.id)"
            >
              <a-button danger>
                <DeleteOutlined />
                删除
              </a-button>
            </a-popconfirm>
          </slot>
        </a-space>
      </div>

      <!-- 部署信息 -->
      <div class="deploy-info" v-if="app.deployKey">
        <h3>部署信息</h3>
        <div class="info-item">
          <span class="label">访问地址：</span>
          <a :href="deployUrl" target="_blank" rel="noopener noreferrer">
            {{ deployUrl }}
          </a>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
type AppVO=API.AppVO
interface Props {
  visible: boolean
  app: AppVO | null
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false
})

defineEmits<{
  close: []
  edit: [id: number]
  delete: [id: number]
}>()

const deployUrl = computed(() => {
  if (!props.app?.deployKey) return ''
  return `http://localhost/${props.app.deployKey}`
})

const formatCreateTime = (time: string | undefined) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.app-details-content {
  max-height: 80vh;
  overflow-y: auto;
}

.app-basic-info,
.app-actions,
.deploy-info {
  margin-bottom: 10px;
}

.app-basic-info h3,
.app-actions h3,
.deploy-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  line-height: 1.5;
}

.info-item .label {
  min-width: 80px;
  color: #8c8c8c;
  font-weight: 500;
  flex-shrink: 0;
}

.creator-info {
  display: flex;
  align-items: center;
}

.prompt-text {
  flex: 1;
  word-break: break-word;
  line-height: 1.6;
}

.deploy-info a {
  color: #1890ff;
  text-decoration: none;
  word-break: break-all;
}

.deploy-info a:hover {
  text-decoration: underline;
}
</style>
