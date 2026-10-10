<template>
  <div class="page-container">
    <!-- 权限控制：只渲染管理员视图 -->
    <template v-if="isAdmin">
      <h2 class="page-title">📚 编册入库</h2>

      <!-- 搜索栏：支持回车触发、清空触发 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="模糊查询：ISBN、书名、作者、出版社、分类号"
          size="large"
          clearable
          class="search-input-flex"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" size="large" :icon="Search" @click="handleSearch">查询</el-button>
        <!-- 新增时把当前搜索关键字带过去，方便预填 ISBN -->
        <el-button type="success" size="large" :icon="Plus" @click="goAdd">新增</el-button>
      </div>

      <el-card shadow="never" class="card-wrapper">
        <!-- 书目表格：后端分页，v-loading 控制加载态 -->
        <el-table :data="bookList" v-loading="loading" stripe empty-text="暂无书目数据">
          <el-table-column prop="ISBN" label="ISBN" width="140" show-overflow-tooltip />
          <el-table-column prop="bookName" label="书名" min-width="140" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="110" show-overflow-tooltip />
          <el-table-column prop="publisher" label="出版社" width="120" show-overflow-tooltip />
          <el-table-column prop="pubDate" label="出版日期" width="110" />
          <el-table-column prop="introduction" label="介绍" min-width="160" show-overflow-tooltip />
          <el-table-column prop="category" label="分类号" width="90" show-overflow-tooltip />
          <!-- 状态列：1=上架(绿) 其他=下架(灰)，后端 bookStatus 字段 -->
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">上架</el-tag>
              <el-tag v-else class="status-off-shelf" size="small" effect="plain">下架</el-tag>
            </template>
          </el-table-column>
          <!-- 编辑/副本按钮：跳转到独立的编辑表单和副本管理页 -->
          <el-table-column label="编辑副本" width="160" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="goEdit(row)">编辑</el-button>
              <el-button type="success" link size="small" @click="goCopies(row)">副本</el-button>
            </template>
          </el-table-column>
          <!-- 下架操作：仅上架状态的书才能下架，已下架的显示占位符 -->
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 1"
                type="danger"
                link
                size="small"
                @click="handleOffShelf(row)"
              >
                下架
              </el-button>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件：翻页或切换每页条数时触发 handlePageChange -->
        <Pagination
          ref="paginationRef"
          :total="total"
          :default-page-size="pageSize"
          @change="handlePageChange"
        />
      </el-card>
    </template>

    <!-- 非管理员看到的无权限提示 -->
    <template v-else>
      <el-result icon="warning" title="无权限访问" sub-title="编册入库仅限管理员操作。">
        <template #extra>
          <el-button type="primary" @click="$router.push('/home')">返回首页</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<script setup>
/**
 * 管理员编册入库页面
 * 功能：带分页的书目列表，支持模糊搜索、新增书目、编辑元信息、管理副本、下架
 * 接口：GET /api/books/catalog（分页+关键字）、PUT /api/books/{isbn}/off-shelf
 * 权限：仅管理员可见，非管理员跳转无权限页
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { listCatalog, offShelfBook } from '@/api/modules/books'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
const authStore = useAuthStore()
// 从 Pinia store 读角色，控制整个页面的渲染分支
const isAdmin = computed(() => authStore.role === 'admin')

// ---- 搜索与分页状态 ----
const keyword = ref('')          // 搜索关键字，支持 ISBN/书名/作者/出版社/分类号
const loading = ref(false)       // 表格加载态
const bookList = ref([])         // 当前页的书目数据
const total = ref(0)             // 匹配的总记录数，传给分页组件
const currentPage = ref(1)       // 当前页码
const pageSize = ref(10)         // 每页条数
const paginationRef = ref(null)  // 分页组件引用，用于重置页码

/**
 * 加载书目列表：调 GET /api/books/catalog，后端返回 { list, total }
 * 空关键字时传 undefined，后端会忽略该参数返回全部
 */
async function loadBooks() {
  loading.value = true
  try {
    const res = await listCatalog({
      keyword: keyword.value.trim() || undefined,
      page: currentPage.value,
      size: pageSize.value,
    })
    bookList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

// 搜索：重置到第一页再加载，同时通知分页组件也重置
function handleSearch() {
  currentPage.value = 1
  paginationRef.value?.reset()
  loadBooks()
}

// 翻页/切条数：Pagination 组件 emit 的 { page, size }
function handlePageChange({ page, size }) {
  currentPage.value = page
  pageSize.value = size
  loadBooks()
}

// 新增书目：跳转到 /books/add，顺便把搜索关键字带过去做预填
function goAdd() {
  router.push({ path: '/books/add', query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {} })
}

// 编辑书目元信息：ISBN 作为路由参数传给编辑表单
function goEdit(row) {
  router.push({ path: `/books/${row.ISBN}/edit` })
}

// 管理副本：跳转到副本列表页，可增删改单个副本
function goCopies(row) {
  router.push({ path: `/books/${row.ISBN}/copies` })
}

/**
 * 下架操作：软删除整本书
 * 后端会把 bookStatus 置 0，同时注销所有可借副本（status→2）
 * 有在借副本时后端会拒绝，所以这里只做二次确认
 */
async function handleOffShelf(row) {
  try {
    await ElMessageBox.confirm(
      `确认下架《${row.bookName}》？下架后该书将无法被检索，可借副本将一并注销。`,
      '图书下架',
      { type: 'warning' },
    )
  } catch {
    return // 用户点取消，什么都不做
  }
  try {
    await offShelfBook(row.ISBN)
    ElMessage.success('下架成功')
    loadBooks() // 刷新列表，下架的书会显示灰色"下架"标签
  } catch {
    /* 请求失败时 request 拦截器已经弹了错误提示，这里不用重复处理 */
  }
}

// 页面挂载后立即加载数据，非管理员跳过（模板里已经渲染了无权限页）
onMounted(() => {
  if (isAdmin.value) {
    loadBooks()
  }
})
</script>

<style scoped>
/* 搜索框自适应宽度，撑满剩余空间但不超过 520px */
.search-input-flex {
  flex: 1;
  max-width: 520px;
}

/* 已下架书的占位符样式 */
.text-muted {
  color: var(--color-text-secondary);
  font-size: 12px;
}

/* 下架标签的灰色样式，区别于默认的蓝色 */
.status-off-shelf {
  color: var(--color-text-secondary);
  border-color: var(--color-border-light);
  background-color: var(--color-muted-bg);
}
</style>
