<template>
  <!-- 图书检索页面主容器 -->
  <div class="page-container">
    <h2 class="page-title">🔍 图书检索</h2>

    <!-- 搜索输入框 + 搜索按钮 -->
    <div class="search-bar">
      <!-- 绑定关键词，回车触发搜索 -->
      <el-input
        v-model="keyword"
        placeholder="请输入书名、作者、ISBN 或关键词..."
        :prefix-icon="Search"
        size="large"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      />
      <!-- 点击搜索按钮发起查询，loading 期间禁用 -->
      <el-button type="primary" size="large" :icon="Search" @click="handleSearch" :loading="searchLoading">
        搜索
      </el-button>
    </div>

    <!-- 搜索结果表格卡片，点击行可打开详情抽屉 -->
    <el-card shadow="never" class="card-wrapper">
      <!-- 搜索结果列表，loading 期间显示骨架，点击行弹出详情 -->
      <el-table
        :data="bookList"
        v-loading="searchLoading"
        stripe
        highlight-current-row
        @row-click="showDetail"
        max-height="500"
        empty-text="请输入关键词搜索图书"
      >
        <!-- 各列展示书名/作者/ISBN/出版社/分类 -->
        <el-table-column prop="bookName" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" show-overflow-tooltip />
        <el-table-column prop="ISBN" label="ISBN" width="150" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <!-- 操作列：查看详情按钮，阻止冒泡避免重复触发行点击 -->
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="showDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 右侧抽屉：展示选中图书的详细信息 -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentBook?.bookName || '图书详情'"
      size="600px"
      direction="rtl"
    >
      <!-- 有选中图书时才渲染详情内容 -->
      <template v-if="currentBook">
        <!-- 基本信息：ISBN/书名/作者/出版社/出版日期/分类，两列布局 -->
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="ISBN">{{ currentBook.ISBN }}</el-descriptions-item>
          <el-descriptions-item label="书名">{{ currentBook.bookName }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ currentBook.author }}</el-descriptions-item>
          <el-descriptions-item label="出版社">{{ currentBook.publisher }}</el-descriptions-item>
          <el-descriptions-item label="出版日期">{{ currentBook.pubDate }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentBook.category }}</el-descriptions-item>
        </el-descriptions>

        <!-- 图书简介，没有简介时显示占位文字 -->
        <el-descriptions :column="1" border size="default" class="mt-16">
          <el-descriptions-item label="简介">{{ currentBook.introduction || '暂无简介' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 副本列表：展示每本实体书的条码、位置和借阅状态 -->
        <h3 class="section-title mt-16">📋 副本列表（共 {{ copies.length }} 册）</h3>
        <!-- 副本表格，排除已注销的副本，最多显示350高度 -->
        <el-table :data="copies" stripe size="default" max-height="350">
          <el-table-column prop="barCode" label="条码" width="160" />
          <el-table-column prop="place" label="馆藏地点" min-width="120" />
          <!-- 状态列用 StatusBadge 组件渲染不同颜色标签 -->
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <StatusBadge
                :status="row.status"
                :status-map="{ 0: '借出', 1: '可借', 2: '注销' }"
              />
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * 图书检索页：支持书名/作者/ISBN 关键词搜索。
 * 点击行或"详情"按钮打开右侧抽屉，展示书目信息 + 可用副本列表（已注销的不显示）。
 * 支持从门户页带 keyword 跳过来自动搜索。
 */
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { searchBooks, getBookDetail } from '@/api/modules/books'
import StatusBadge from '@/components/StatusBadge.vue'
import logger from '@/utils/logger'

const route = useRoute() // 路由对象，用于读取 URL 查询参数
const COMPONENT = 'BookSearchView > ' // 日志前缀，方便定位来源
const keyword = ref('') // 搜索关键词，双向绑定到输入框
const searchLoading = ref(false) // 搜索按钮 loading 状态，防重复点击
const bookList = ref([]) // 搜索结果列表，绑定到表格
const drawerVisible = ref(false) // 控制详情抽屉的显示/隐藏
const currentBook = ref(null) // 当前选中的图书对象，展示在抽屉里
const copies = ref([]) // 当前图书的副本列表，过滤掉已注销的

// 搜索按钮点击或回车触发，调接口拿图书列表
async function handleSearch() {
  logger.operation(`${COMPONENT}handleSearch`, '▶ 进入', { keyword: keyword.value })

  // 关键词为空时提示用户，不发请求
  if (!keyword.value || keyword.value.trim() === '') {
    logger.validate(`${COMPONENT}handleSearch`, '❌ 关键词为空')
    ElMessage.warning('请输入搜索关键词')
    return
  }

  searchLoading.value = true // 开启 loading 防连点
  try {
    const res = await searchBooks(keyword.value.trim()) // 调用搜索接口
    bookList.value = res.data || [] // 回填结果到列表

    logger.operation(`${COMPONENT}handleSearch`, '✅ 成功', {
      keyword: keyword.value,
      hitCount: bookList.value.length,
    })

    // 没搜到结果给个提示
    if (bookList.value.length === 0) {
      ElMessage.info('未找到匹配的图书')
    }
  } catch (e) {
    logger.error(`${COMPONENT}handleSearch`, '❌ 搜索失败', { keyword: keyword.value })
  } finally {
    searchLoading.value = false // 无论成功失败都关掉 loading
  }
}

// 点击表格行或详情按钮，拉取图书详情并打开抽屉
async function showDetail(book) {
  logger.operation(`${COMPONENT}showDetail`, '▶ 进入', {
    ISBN: book.ISBN,
    bookName: book.bookName || '未知',
  })

  try {
    const res = await getBookDetail(book.ISBN) // 按 ISBN 拉详情
    currentBook.value = res.data // 赋值当前图书
    // 过滤掉已注销(status=2)的副本，只展示可借和借出中的
    copies.value = (res.data?.copies || []).filter(c => c.status !== 2)
    drawerVisible.value = true // 打开抽屉

    // 统计各状态副本数量，用于日志记录
    const statusCount = {}
    copies.value.forEach(c => {
      const key = { 0: '借出', 1: '可借', 2: '注销' }[c.status] || `未知(${c.status})`
      statusCount[key] = (statusCount[key] || 0) + 1
    })

    logger.operation(`${COMPONENT}showDetail`, '✅ 成功', {
      ISBN: book.ISBN,
      bookName: res.data?.bookName,
      copiesCount: copies.value.length,
      statusSummary: statusCount,
    })
  } catch (e) {
    logger.error(`${COMPONENT}showDetail`, '❌ 获取详情失败', { ISBN: book.ISBN })
  }
}

// 页面挂载后检查 URL 参数，支持从门户页带关键词跳转自动搜索
onMounted(() => {
  const kw = route.query.keyword // 读取 URL 里的 keyword 参数
  if (kw) {
    keyword.value = String(kw) // 回填到输入框
    handleSearch() // 自动触发一次搜索
  }
})
</script>

<style scoped>
/* 搜索框弹性宽度，最大不超过 600px */
.search-input {
  flex: 1;
  max-width: 600px;
}

/* 抽屉内小标题样式 */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}
</style>
