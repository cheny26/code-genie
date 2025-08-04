<template>
  <div class="home-container">
    <!-- 网站标题和介绍 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          代码精灵，让代码生成更简单
        </h1>
        <p class="hero-subtitle">副标题</p>

        <!-- 用户提示词输入框 -->
        <div class="prompt-input-section">
          <div class="input-wrapper">
            <a-textarea
              v-model:value="userPrompt"
              placeholder="使用 NoCode 创建一个高效的小工具，游戏玩法是......"
              size="large"
              class="main-input"
              @keyup.enter="createApp"
              :auto-size="{ minRows: 1, maxRows: 10 }"
            />

            <div>
              <a-button
                type="primary"
                size="large"
                shape="circle"
                :loading="creating"
                @click="createApp"
                :disabled="!userPrompt.trim() "
              >
              <ArrowUpOutlined />
              </a-button>
            </div>
          </div>

          <div class="quick-prompts">
            <a-button
              v-for="prompt in quickPrompts"
              :key="prompt"
              size="small"
              type="text"
              @click="userPrompt = prompt"
              class="quick-prompt-btn"
            >
              {{ prompt }}
            </a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的应用 -->
    <div class="section" v-if="userStore.isLoggedIn">
      <div class="section-header">
        <h2>我的作品</h2>
        <div class="section-actions">
          <a-input-search
            v-model:value="myAppSearchText"
            placeholder="搜索我的应用"
            style="width: 200px"
            @search="loadMyApps"
            @change="onMyAppSearchChange"
          />
        </div>
      </div>

      <div class="app-grid">
        <AppCard
          v-for="app in myApps"
          :key="app.id"
          :app="app"
          :show-actions="true"
          :current-user-avatar="userStore.userAvatar"
          :current-user-name="userStore.userName"
          @click="goToChat"
          @edit="editApp"
          @delete="deleteMyApp"
          @view-chat="goToViewChat"
          @view-work="goToViewWork"
        />

        <!-- 空状态 -->
        <div v-if="myApps.length === 0 && !myAppLoading" class="empty-state">
          <a-empty description="还没有创建任何应用，快来创建第一个吧！" />
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="myAppTotal > 0">
        <a-pagination
          v-model:current="myAppPage"
          v-model:page-size="myAppPageSize"
          :total="myAppTotal"
          :show-size-changer="false"
          :page-size-options="['20']"
          show-quick-jumper
          @change="loadMyApps"
        />
      </div>
    </div>

    <!-- 精选案例 -->
    <div class="section">
      <div class="section-header">
        <h2>精选案例</h2>
        <div class="section-actions">
          <a-input-search
            v-model:value="featuredSearchText"
            placeholder="搜索精选应用"
            style="width: 200px"
            @search="loadFeaturedApps"
            @change="onFeaturedSearchChange"
          />
        </div>
      </div>

      <div class="app-grid">
        <AppCard
          v-for="app in featuredApps"
          :key="app.id"
          :app="app"
          @click="goToChat"
          @view-chat="goToViewChat"
          @view-work="goToViewWork"
        />

        <!-- 空状态 -->
        <div v-if="featuredApps.length === 0 && !featuredLoading" class="empty-state">
          <a-empty description="暂无精选应用" />
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="featuredTotal > 0">
        <a-pagination
          v-model:current="featuredPage"
          v-model:page-size="featuredPageSize"
          :total="featuredTotal"
          :show-size-changer="false"
          :page-size-options="['20']"
          show-quick-jumper
          @change="loadFeaturedApps"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage, deleteApp } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'
// 使用全局类型声明
type AppVO = API.AppVO

const router = useRouter()
const userStore = useUserStore()

// 用户输入
const userPrompt = ref('')
const creating = ref(false)

// 快速提示词
const quickPrompts = ref([
  '个人博客生成器',
  '企业网站',
  '电商运营后台',
  '聊天话题社区'
])

// 我的应用
const myApps = ref<AppVO[]>([])
const myAppLoading = ref(false)
const myAppPage = ref(1)
const myAppPageSize = ref(20)
const myAppTotal = ref(0)
const myAppSearchText = ref('')

// 精选应用
const featuredApps = ref<AppVO[]>([])
const featuredLoading = ref(false)
const featuredPage = ref(1)
const featuredPageSize = ref(20)
const featuredTotal = ref(0)
const featuredSearchText = ref('')

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!userStore.isLoggedIn) {
    message.warning('请先登录')
    router.push('/login')
    return
  }

  creating.value = true
  try {
    const response = await addApp({
      initPrompt: userPrompt.value.trim()
    })

    if (response.data.code === 0 && response.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面
      router.push(`/app/chat/${response.data.data}`)
    } else {
      message.error(response.data.message || '创建失败')
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!userStore.isLoggedIn) return

  myAppLoading.value = true
  try {
    const response = await listMyAppVoByPage({
      pageNum: myAppPage.value,
      pageSize: myAppPageSize.value,
      appName: myAppSearchText.value.trim() || undefined,
      sortField: 'createTime',
      sortOrder: 'desc'
    })

    if (response.data.code === 0 && response.data.data) {
      myApps.value = response.data.data.records || []
      myAppTotal.value = Number(response.data.data.totalRow) || 0
    }
  } catch (error) {
    console.error('加载我的应用失败:', error)
    message.error('加载失败')
  } finally {
    myAppLoading.value = false
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  featuredLoading.value = true
  try {
    const response = await listFeaturedAppVoByPage({
      pageNum: featuredPage.value,
      pageSize: featuredPageSize.value,
      appName: featuredSearchText.value.trim() || undefined,
      sortField: 'priority',
      sortOrder: 'desc'
    })

    if (response.data.code === 0 && response.data.data) {
      featuredApps.value = response.data.data.records || []
      featuredTotal.value = Number(response.data.data.totalRow) || 0
    }
  } catch (error) {
    console.error('加载精选应用失败:', error)
    message.error('加载失败')
  } finally {
    featuredLoading.value = false
  }
}

// 搜索变化处理
const onMyAppSearchChange = () => {
  myAppPage.value = 1
  loadMyApps()
}

const onFeaturedSearchChange = () => {
  featuredPage.value = 1
  loadFeaturedApps()
}

// 跳转到对话页面
const goToChat = (appId: number) => {
  console.log(appId)
  router.push(`/app/chat/${appId}`)
}

// 查看对话（带view参数）
const goToViewChat = (appId: number) => {
  router.push(`/app/chat/${appId}?view=1`)
}

// 查看作品
const goToViewWork = (deployKey: string) => {
  window.open(`http://localhost/${deployKey}`, '_blank')
}

// 编辑应用
const editApp = (appId: number) => {
  router.push(`/app/edit/${appId}`)
}

// 删除我的应用
const deleteMyApp = async (appId: number) => {
  try {
    const response = await deleteApp({ id: appId })
    if (response.data.code === 0) {
      message.success('删除成功')
      loadMyApps()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    message.error('删除失败')
  }
}



// 页面加载
onMounted(() => {
  loadFeaturedApps()
  if (userStore.isLoggedIn) {
    loadMyApps()
  }
})
</script>

<style scoped>
.home-container {
  margin: 0 auto;
  padding: 24px 24px;
  background-color: #f7f8fc;
}

.hero-section {
  text-align: center;
  padding: 120px 0 80px 0;
  color: rgb(7, 7, 7);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
  line-height: 1.2;
}



.hero-subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.prompt-input-section {
  max-width: 800px;
  margin: 0 auto;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 16px;
  padding: 8px;
  gap: 8px;
}



.main-input {
  flex: 1;
  border: none;
  box-shadow: none ;
  padding: 12px 16px;
  font-size: 16px;
}


.quick-prompts {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.quick-prompt-btn {
  background: rgb(225, 221, 221);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: rgb(63, 62, 62);
  border-radius: 20px;
}

.section {
  padding: 0 24px;
  margin-bottom: 10px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  margin-bottom: 24px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>

