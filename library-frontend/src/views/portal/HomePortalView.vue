<template>
  <!-- 整个门户页面外壳 -->
  <div class="portal-page">
    <!-- 主体内容区域 -->
    <div class="portal-body">
      <!-- 搜索栏：输入框 + 检索按钮 -->
      <div class="portal-search-row">
        <!-- 关键词输入框，回车也能触发搜索 -->
        <el-input
          v-model="keyword"
          placeholder="请输入书名、作者或出版社关键词"
          size="large"
          clearable
          class="portal-search-input"
          @keyup.enter="handleSearch"
        />
        <!-- 点击检索按钮发起搜索 -->
        <el-button type="primary" size="large" @click="handleSearch">检索</el-button>
      </div>

      <!-- 图书馆公告 -->
      <el-card shadow="never" class="portal-notice-card">
        <template #header>
          <div class="notice-header">
            <span class="section-title">📢 图书馆公告</span>
            <el-button type="primary" link @click="openAllNotices">查看全部 →</el-button>
          </div>
        </template>
        <div v-loading="loading" class="notice-list">
          <el-empty v-if="!loading && latestNotices.length === 0" description="暂无公告" :image-size="64" />
          <div
            v-for="item in displayNotices"
            :key="item.id"
            class="notice-item"
            @click="openDetail(item)"
          >
            <div class="notice-item-main">
              <el-tag v-if="isRecent(item.createDate)" type="danger" size="small" class="new-tag">新</el-tag>
              <span class="notice-topic">{{ item.topic }}</span>
              <span class="notice-content-brief">{{ item.content }}</span>
            </div>
            <span class="notice-date">{{ formatShortDate(item.createDate) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 三个榜单卡片的网格容器 -->
      <div class="portal-sections">
        <!-- 热读推荐榜单 -->
        <el-card shadow="never" class="portal-section">
          <template #header><span class="section-title">🔥 热读推荐</span></template>
          <!-- 热读排行榜表格，按借阅次数排序 -->
          <el-table :data="hotBooks" v-loading="loading" stripe empty-text="暂无数据" size="small">
            <!-- 排名列 -->
            <el-table-column type="index" label="排名" width="52" />
            <!-- 书名列，溢出显示省略号 -->
            <el-table-column prop="bookName" label="书名" min-width="80" show-overflow-tooltip />
            <!-- 借阅次数列，用绿色标签展示 -->
            <el-table-column prop="borrowCount" label="次数" width="64" align="center">
              <template #default="{ row }">
                <el-tag type="success" size="small">{{ row.borrowCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 借书明星榜单 -->
        <el-card shadow="never" class="portal-section">
          <template #header><span class="section-title">⭐ 借书明星</span></template>
          <!-- 借书最多的学生排行 -->
          <el-table :data="borrowStars" v-loading="loading" stripe empty-text="暂无数据" size="small">
            <!-- 排名列 -->
            <el-table-column type="index" label="排名" width="52" />
            <!-- 学生姓名列 -->
            <el-table-column prop="studentName" label="姓名" min-width="72" show-overflow-tooltip />
            <!-- 借阅次数列，用蓝色标签展示 -->
            <el-table-column prop="borrowCount" label="次数" width="64" align="center">
              <template #default="{ row }">
                <el-tag type="primary" size="small">{{ row.borrowCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 新书上架榜单 -->
        <el-card shadow="never" class="portal-section">
          <template #header><span class="section-title">📗 新书上架</span></template>
          <!-- 最近上架的新书列表 -->
          <el-table :data="newBooks" v-loading="loading" stripe empty-text="暂无数据" size="small">
            <!-- 序号列 -->
            <el-table-column type="index" label="序号" width="52" />
            <!-- 书名列 -->
            <el-table-column prop="bookName" label="书名" min-width="80" show-overflow-tooltip />
            <!-- 出版日期列 -->
            <el-table-column prop="pubDate" label="日期" width="88" show-overflow-tooltip />
          </el-table>
        </el-card>
      </div>
    </div>

    <el-dialog v-model="allVisible" title="📢 全部公告" width="720px">
      <el-table :data="allNotices" v-loading="allLoading" stripe empty-text="暂无公告" max-height="420">
        <el-table-column prop="topic" label="标题" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">{{ row.topic }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="author" label="发布人" width="100" show-overflow-tooltip />
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createDate) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="detailNotice?.topic || '公告详情'" width="560px">
      <el-descriptions v-if="detailNotice" :column="1" border>
        <el-descriptions-item label="发布人">{{ detailNotice.author }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDateTime(detailNotice.createDate) }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="notice-detail-content">{{ detailNotice.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 公共门户首页：无需登录即可浏览热读推荐、借书明星、新书上架三个榜单。
 * 搜索时如果未登录，会先跳转到登录页并记住 keyword，登录后自动回来搜索。
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getPortalHome } from '@/api/modules/portal'
import { listNotices } from '@/api/modules/notices'

// 路由实例，用于页面跳转
const router = useRouter()
// 认证状态仓库，判断登录态
const authStore = useAuthStore()
// 计算属性：当前是否已登录
const isLoggedIn = computed(() => authStore.isLoggedIn)

// 搜索关键词
const keyword = ref('')
// 表格加载状态，三个榜单共用
const loading = ref(false)
// 热读推荐数据
const hotBooks = ref([])
// 借书明星数据
const borrowStars = ref([])
// 新书上架数据
const newBooks = ref([])
// 最新公告（接口返回最多 3 条）
const latestNotices = ref([])
// 首页展示的公告（固定 3 条）
const displayNotices = computed(() => latestNotices.value.slice(0, 3))
// 全部公告弹窗
const allVisible = ref(false)
const allLoading = ref(false)
const allNotices = ref([])
// 公告详情弹窗
const detailVisible = ref(false)
const detailNotice = ref(null)

function formatDateTime(value) {
  if (!value) return '—'
  const text = String(value).replace('T', ' ')
  return text.length >= 16 ? text.slice(0, 16) : text
}

function formatShortDate(value) {
  if (!value) return '—'
  const text = String(value).replace('T', ' ')
  return text.length >= 10 ? text.slice(5, 10) : text
}

function isRecent(value) {
  if (!value) return false
  const date = new Date(value)
  const diff = Date.now() - date.getTime()
  return diff >= 0 && diff <= 7 * 24 * 60 * 60 * 1000
}

function openDetail(item) {
  detailNotice.value = item
  detailVisible.value = true
}

async function openAllNotices() {
  allVisible.value = true
  allLoading.value = true
  try {
    const res = await listNotices({ page: 1, size: 100 })
    allNotices.value = res.data?.list || []
  } finally {
    allLoading.value = false
  }
}

// 请求门户首页三个榜单数据
async function loadPortalData() {
  loading.value = true
  try {
    const res = await getPortalHome()
    // 接口返回的三个数组，兜底空数组防止表格报错
    hotBooks.value = res.data?.hotBooks || []
    borrowStars.value = res.data?.borrowStars || []
    newBooks.value = res.data?.newBooks || []
    latestNotices.value = res.data?.latestNotices || []
  } finally {
    loading.value = false
  }
}

// 未登录时把 keyword 带到登录页，登录成功后会自动 redirect 回来
function handleSearch() {
  // 去除首尾空格后校验
  const kw = keyword.value.trim()
  if (!kw) {
    ElMessage.warning('请输入检索关键词')
    return
  }
  // 已登录直接跳搜索结果页
  if (isLoggedIn.value) {
    router.push({ path: '/books/search', query: { keyword: kw } })
    return
  }
  // 未登录则跳登录页，把搜索意图带过去，登录后自动回来
  router.push({
    path: '/login',
    query: { redirect: '/books/search', keyword: kw },
  })
}

// 页面挂载后立即拉取榜单数据
onMounted(loadPortalData)
</script>

<style scoped>
/* 门户页面容器，不设最小高度由内容撑开 */
.portal-page {
  min-height: auto;
  background: transparent;
}

/* 主体不限制最大宽度 */
.portal-body {
  width: 100%;
  max-width: none;
}

/* 搜索栏布局：输入框和按钮水平排列 */
.portal-search-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

/* 搜索框占满剩余宽度 */
.portal-search-input {
  flex: 1;
}

.portal-notice-card {
  border-radius: 12px;
  margin-bottom: 28px;
}

.notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-list {
  min-height: 48px;
}

.notice-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover .notice-topic {
  color: var(--el-color-primary);
}

.notice-item-main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.new-tag {
  flex-shrink: 0;
}

.notice-topic {
  font-weight: 600;
  flex-shrink: 0;
}

.notice-content-brief {
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-date {
  color: var(--color-text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}

.notice-detail-content {
  white-space: pre-wrap;
  line-height: 1.7;
}

/* 三个榜单卡片三列等宽网格 */
.portal-sections {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  align-items: start;
}

/* 单个榜单卡片圆角 */
.portal-section {
  border-radius: 12px;
  min-width: 0;
}

/* 缩小卡片内部内边距 */
.portal-section :deep(.el-card__body) {
  padding: 12px;
}

/* 表格撑满卡片宽度 */
.portal-section :deep(.el-table) {
  width: 100%;
}

/* 窄屏下三列变单列，防止挤压 */
@media (max-width: 1100px) {
  .portal-sections {
    grid-template-columns: 1fr;
  }
}

/* 榜单标题样式 */
.section-title {
  font-size: 16px;
  font-weight: 600;
}
</style>
