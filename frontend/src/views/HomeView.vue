<template>
  <div class="home-container">
    <!-- 网站标题和介绍 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          一句话 <span class="brand-highlight">🤖</span> 呈所想
        </h1>
        <p class="hero-subtitle">与 AI 对话轻松创建应用和网站</p>

        <!-- 用户提示词输入框 -->
        <div class="prompt-input-section">
          <a-input-search
            v-model:value="userPrompt"
            placeholder="使用 NoCode 创建一个高效的小工具，帮我计算......"
            size="large"
            enter-button="创建应用"
            :loading="creating"
            @search="createApp"
            class="prompt-input"
          />
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
        <div
          v-for="app in myApps"
          :key="app.id"
          class="app-card"
          @click="goToChat(app.id!)"
        >
          <div class="app-cover">
            <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
            <div v-else class="default-cover">
              <CodeOutlined />
            </div>
          </div>
          <div class="app-info">
            <h3 class="app-name">{{ app.appName }}</h3>
            <p class="app-creator">创建于 {{ formatDate(app.createTime) }}</p>
            <div class="app-actions">
              <a-button size="small" type="text" @click.stop="editApp(app.id!)">
                编辑
              </a-button>
              <a-button size="small" type="text" danger @click.stop="deleteMyApp(app.id!)">
                删除
              </a-button>
            </div>
          </div>
        </div>

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
        <div
          v-for="app in featuredApps"
          :key="app.id"
          class="app-card featured"
          @click="goToChat(app.id!)"
        >
          <div class="app-cover">
            <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
            <div v-else class="default-cover">
              <CodeOutlined />
            </div>
            <div class="featured-badge">精选</div>
          </div>
          <div class="app-info">
            <h3 class="app-name">{{ app.appName }}</h3>
            <p class="app-creator">{{ app.user?.userName || 'NoCode 官方' }}</p>
            <div class="app-meta">
              <a-tag color="blue">{{ app.codeGenType || '工具' }}</a-tag>
            </div>
          </div>
        </div>

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
import { CodeOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import { addApp, listMyAppVoByPage, listFeaturedAppVoByPage, deleteApp } from '@/api/appController'
import type { AppVO } from '@/api/typings'

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

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.hero-section {
  text-align: center;
  padding: 80px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: -24px -24px 40px -24px;
  color: white;
}

.hero-title {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
  line-height: 1.2;
}

.brand-highlight {
  color: #ffd700;
}

.hero-subtitle {
  font-size: 20px;
  margin-bottom: 40px;
  opacity: 0.9;
}

.prompt-input-section {
  max-width: 600px;
  margin: 0 auto;
}

.prompt-input {
  margin-bottom: 16px;
}

.prompt-input :deep(.ant-input) {
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 16px;
}

.prompt-input :deep(.ant-btn) {
  border-radius: 8px;
  height: auto;
  padding: 12px 24px;
  font-size: 16px;
}

.quick-prompts {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.quick-prompt-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
}

.quick-prompt-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.section {
  margin-bottom: 60px;
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
  gap: 24px;
  margin-bottom: 24px;
}

.app-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.app-card.featured {
  border: 2px solid #1890ff;
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

.featured-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #1890ff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.app-info {
  padding: 16px;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #262626;
}

.app-creator {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0 0 12px 0;
}

.app-actions {
  display: flex;
  gap: 8px;
}

.app-meta {
  margin-top: 8px;
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

