<template>
  <div class="page-container">
    <template v-if="isAdmin">
      <h2 class="page-title">📢 公告管理</h2>

      <el-card shadow="never" class="card-wrapper">
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索标题、内容或发布人"
            size="large"
            clearable
            class="search-input-flex"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
          <el-button type="primary" size="large" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button size="large" :icon="RefreshRight" @click="loadNotices">刷新</el-button>
          <el-button type="success" size="large" :icon="Plus" @click="openCreate">发布公告</el-button>
        </div>

        <el-table :data="noticeList" v-loading="loading" stripe empty-text="暂无公告">
          <el-table-column prop="topic" label="标题" min-width="140" show-overflow-tooltip />
          <el-table-column prop="content" label="内容摘要" min-width="220" show-overflow-tooltip />
          <el-table-column prop="author" label="发布人" width="120" show-overflow-tooltip />
          <el-table-column label="发布时间" width="170">
            <template #default="{ row }">{{ formatDateTime(row.createDate) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          ref="paginationRef"
          :total="total"
          :default-page-size="pageSize"
          @change="handlePageChange"
        />
      </el-card>

      <el-dialog
        v-model="dialogVisible"
        :title="editingId ? '编辑公告' : '发布公告'"
        width="560px"
        :close-on-click-modal="false"
      >
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="标题" prop="topic">
            <el-input v-model="form.topic" maxlength="50" show-word-limit placeholder="请输入公告标题" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="5"
              maxlength="255"
              show-word-limit
              placeholder="请输入公告内容"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
        </template>
      </el-dialog>
    </template>

    <template v-else>
      <el-result icon="warning" title="无权限访问" sub-title="公告管理仅限管理员操作。">
        <template #extra>
          <el-button type="primary" @click="$router.push('/home')">返回首页</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Search, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { listNotices, createNotice, updateNotice, deleteNotice } from '@/api/modules/notices'
import Pagination from '@/components/Pagination.vue'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.role === 'admin')

const keyword = ref('')
const loading = ref(false)
const noticeList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const paginationRef = ref(null)

const dialogVisible = ref(false)
const editingId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const form = ref({ topic: '', content: '' })

const rules = {
  topic: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
}

function formatDateTime(value) {
  if (!value) return '—'
  const text = String(value).replace('T', ' ')
  return text.length >= 16 ? text.slice(0, 16) : text
}

async function loadNotices() {
  loading.value = true
  try {
    const res = await listNotices({
      keyword: keyword.value.trim() || undefined,
      page: currentPage.value,
      size: pageSize.value,
    })
    noticeList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  paginationRef.value?.reset()
  loadNotices()
}

function handlePageChange({ page, size }) {
  currentPage.value = page
  pageSize.value = size
  loadNotices()
}

function resetForm() {
  form.value = { topic: '', content: '' }
  editingId.value = null
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = { topic: row.topic, content: row.content }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (editingId.value) {
      await updateNotice(editingId.value, form.value)
      ElMessage.success('公告已更新')
    } else {
      await createNotice(form.value)
      ElMessage.success('公告已发布')
    }
    dialogVisible.value = false
    loadNotices()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.topic}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  await deleteNotice(row.id)
  ElMessage.success('公告已删除')
  loadNotices()
}

onMounted(() => {
  if (isAdmin.value) loadNotices()
})
</script>
