<template>
  <div class="app-card" @click="emit('click', app.id)">
    <div class="app-cover" @mouseenter="showHoverButtons = true" @mouseleave="showHoverButtons = false">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="default-cover">
        <CodeOutlined />
      </div>

             <!-- Hover时显示的按钮 -->
       <div v-show="showHoverButtons" class="hover-buttons" @click.stop>
         <a-button
           type="primary"
           @click="emit('viewChat', app.id)"
         >
           查看对话
         </a-button>
         <a-button
           v-if="app.deployKey"
           type="default"
           @click="emit('viewWork', app.deployKey)"
         >
           查看作品
         </a-button>


       </div>
    </div>
    <div class="app-info">
      <div class="app-bottom">
        <div class="app-left">
          <a-avatar
            :size="32"
            :src="avatarSrc"
            class="user-avatar"
          >
            {{ avatarText }}
          </a-avatar>
          <div class="app-details">
            <h3 class="app-name">{{ app.appName }}</h3>
            <p class="app-creator">{{ creatorText }}</p>
          </div>
                </div>

        <!-- 管理操作下拉菜单 -->
        <div v-if="showActions" class="app-actions">
          <a-dropdown placement="bottom" >
            <a-button type="text" size="small" @click.stop>
              <MoreOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="edit" @click="emit('edit', app.id)">
                  <EditOutlined />
                  编辑
                </a-menu-item>
                <a-menu-item key="delete" @click="emit('delete', app.id)" class="delete-item">
                  <DeleteOutlined />
                  删除
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>

      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { CodeOutlined, MoreOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'

// 使用全局类型声明
type AppVO = API.AppVO

interface Props {
  app: AppVO
  showActions?: boolean
  currentUserAvatar?: string
  currentUserName?: string
}

// 控制hover按钮显示
const showHoverButtons = ref(false)

const props = withDefaults(defineProps<Props>(), {
  showActions: false
})

const emit = defineEmits(['click', 'edit', 'delete', 'viewChat', 'viewWork'])

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

// 计算头像源
const avatarSrc = computed(() => {
  if (props.showActions) {
    // 我的应用模式，使用当前用户头像
    return props.currentUserAvatar
  } else {
    // 精选应用模式，使用应用创建者头像
    return props.app.user?.userAvatar
  }
})

// 计算头像文字
const avatarText = computed(() => {
  if (props.showActions) {
    // 我的应用模式，使用当前用户名
    return props.currentUserName?.charAt(0) || 'U'
  } else {
    // 精选应用模式，使用应用创建者名字
    return props.app.user?.userName?.charAt(0) || 'U'
  }
})

// 计算创建者文字
const creatorText = computed(() => {
  if (props.showActions) {
    // 我的应用模式，只显示创建时间
    return formatDate(props.app.createTime)
  } else {
    // 精选应用模式，显示创建者和创建时间
    const userName = props.app.user?.userName || '未知'
    const createTime = formatDate(props.app.createTime)
    return `${userName} ${createTime}`
  }
})
</script>

<style scoped>
.app-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.app-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.app-cover {
  position: relative;
  height: 160px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  font-size: 48px;
  color: #bfbfbf;
}

.app-info {
  padding: 16px;
}

.app-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.user-avatar {
  flex-shrink: 0;
}

.app-details {
  flex: 1;
  min-width: 0;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #262626;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-creator {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.hover-buttons {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}
</style>
