<template>
  <div class="page-container">
    <!-- 页头：标题 + 返回按钮 -->
    <div class="page-header">
      <h2 class="page-title">📋 副本管理</h2>
      <el-button @click="goBack">返回列表</el-button>
    </div>

    <!-- 图书基本信息卡片，加载中显示骨架 -->
    <el-card shadow="never" class="card-wrapper book-info-card" v-loading="loading">
      <div class="book-info-row">
        <div class="book-info-main">
          <h3 class="info-title">图书基本信息</h3>
          <!-- 有书目数据时才渲染描述列表 -->
          <el-descriptions v-if="bookInfo" :column="3" border size="default">
            <el-descriptions-item label="ISBN">{{ bookInfo.ISBN }}</el-descriptions-item>
            <el-descriptions-item label="书名">{{ bookInfo.bookName }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ bookInfo.author }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ bookInfo.publisher }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ bookInfo.pubDate }}</el-descriptions-item>
            <el-descriptions-item label="分类号">{{ bookInfo.category }}</el-descriptions-item>
            <!-- 上架/下架状态标签 -->
            <el-descriptions-item label="状态">
              <el-tag :type="bookInfo.status === 1 ? 'success' : 'info'" size="small">
                {{ bookInfo.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="介绍" :span="2">
              {{ bookInfo.introduction || '暂无简介' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <!-- 右侧操作按钮组 -->
        <div class="book-info-actions">
          <!-- 新增副本按钮，下架状态禁用 -->
          <el-button type="success" :icon="Plus" :disabled="bookInfo?.status !== 1" @click="openAddDialog">
            新增
          </el-button>
          <!-- 跳转编辑书目页 -->
          <el-button type="primary" link @click="goEditBook">编辑书目</el-button>
        </div>
      </div>
    </el-card>

    <!-- 副本列表表格卡片 -->
    <el-card shadow="never" class="card-wrapper">
      <el-table :data="copies" stripe empty-text="暂无副本">
        <el-table-column prop="barCode" label="条码号" min-width="160" />
        <el-table-column prop="ISBN" label="ISBN" width="140" />
        <el-table-column prop="place" label="位置" min-width="140" />
        <!-- 副本状态列：用 StatusBadge 组件渲染 -->
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <StatusBadge
              :status="row.status"
              :status-map="{ 0: '借出', 1: '可借', 2: '注销' }"
            />
          </template>
        </el-table-column>
        <!-- 编辑按钮列：已注销的副本禁用 -->
        <el-table-column label="编辑" width="80" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :disabled="row.status === 2"
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
        <!-- 注销按钮列：只有"可借"状态才能注销 -->
        <el-table-column label="注销" width="80" align="center">
          <template #default="{ row }">
            <el-button
              type="danger"
              link
              size="small"
              :disabled="row.status !== 1"
              @click="handleDiscard(row)"
            >
              注销
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增副本弹窗 -->
    <el-dialog v-model="addDialogVisible" title="新增副本" width="520px" destroy-on-close>
      <!-- 新增表单：条码号 + 馆藏位置 -->
      <el-form ref="addFormRef" :model="addForm" :rules="copyRules" label-width="90px">
        <el-form-item label="条码号" prop="barCode">
          <el-input v-model="addForm.barCode" placeholder="请输入副本条码" />
        </el-form-item>
        <el-form-item label="馆藏位置" prop="place">
          <el-input v-model="addForm.place" placeholder="如 A区-01架-01层" />
        </el-form-item>
      </el-form>
      <!-- 弹窗底部：取消 / 确认 -->
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addSubmitting" @click="handleAddCopy">确认</el-button>
      </template>
    </el-dialog>

    <!-- 编辑副本弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑副本" width="520px" destroy-on-close>
      <!-- 编辑表单：条码号只读，可修改位置 -->
      <el-form ref="editFormRef" :model="editForm" :rules="copyRules" label-width="90px">
        <el-form-item label="条码号">
          <el-input :model-value="editForm.barCode" disabled />
        </el-form-item>
        <el-form-item label="馆藏位置" prop="place">
          <el-input v-model="editForm.place" placeholder="如 A区-01架-01层" />
        </el-form-item>
      </el-form>
      <!-- 弹窗底部：取消 / 保存 -->
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditCopy">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 副本管理页：展示某本书的所有副本，支持新增副本、编辑馆藏位置、注销副本。
 * 只有"可借"状态的副本才能注销；已注销的不能编辑。
 */
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import StatusBadge from '@/components/StatusBadge.vue'
import { getBookDetail, addBookCopies, updateCopy, discardCopy } from '@/api/modules/books'

const route = useRoute() // 路由对象，取 URL 参数用
const router = useRouter() // 路由实例，页面跳转用
const isbn = route.params.isbn // 从路由拿到当前 ISBN

const loading = ref(false) // 页面加载态
const bookInfo = ref(null) // 图书基本信息
const copies = ref([]) // 副本列表数据

const addDialogVisible = ref(false) // 控制新增弹窗显隐
const editDialogVisible = ref(false) // 控制编辑弹窗显隐
const addSubmitting = ref(false) // 新增提交中，防止重复点击
const editSubmitting = ref(false) // 编辑提交中，防止重复点击
const addFormRef = ref(null) // 新增表单实例，用于手动校验
const editFormRef = ref(null) // 编辑表单实例，用于手动校验

const addForm = reactive({ barCode: '', place: '' }) // 新增表单数据
const editForm = reactive({ barCode: '', place: '' }) // 编辑表单数据

// 副本表单校验规则：条码必填，位置必填且需符合 "X区-XX架-XX层" 格式
const copyRules = {
  barCode: [{ required: true, message: '请输入条码号', trigger: 'blur' }],
  place: [
    { required: true, message: '请输入馆藏位置', trigger: 'blur' },
    { pattern: /^[A-Z]区-\d{2}架-\d{2}层$/, message: '格式：X区-XX架-XX层', trigger: 'blur' },
  ],
}

// 页面初始化：拉取图书详情 + 副本列表
async function loadData() {
  loading.value = true
  try {
    const res = await getBookDetail(isbn)
    bookInfo.value = res.data
    copies.value = res.data?.copies || []
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗，清空表单
function openAddDialog() {
  addForm.barCode = ''
  addForm.place = ''
  addDialogVisible.value = true
}

// 打开编辑弹窗，用当前行数据回填
function openEditDialog(row) {
  editForm.barCode = row.barCode
  editForm.place = row.place
  editDialogVisible.value = true
}

// 提交新增副本：校验 -> 调接口 -> 刷新列表
async function handleAddCopy() {
  let valid = false
  try {
    valid = await addFormRef.value.validate()
  } catch {
    /* 校验失败不处理 */
  }
  if (!valid) return

  addSubmitting.value = true
  try {
    await addBookCopies(isbn, [{ barCode: addForm.barCode, place: addForm.place }])
    ElMessage.success('新增副本成功')
    addDialogVisible.value = false
    await loadData()
  } finally {
    addSubmitting.value = false
  }
}

// 提交编辑副本：校验 -> 调接口 -> 刷新列表
async function handleEditCopy() {
  let valid = false
  try {
    valid = await editFormRef.value.validate()
  } catch {
    /* 校验失败不处理 */
  }
  if (!valid) return

  editSubmitting.value = true
  try {
    await updateCopy(editForm.barCode, { place: editForm.place })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    await loadData()
  } finally {
    editSubmitting.value = false
  }
}

// 注销副本：只有 status===1（可借）才能注销，借出中的不行
async function handleDiscard(row) {
  // 二次确认弹窗
  try {
    await ElMessageBox.confirm(`确认注销条码 ${row.barCode} 的副本？注销后检索时将不再显示。`, '副本注销', {
      type: 'warning',
    })
  } catch {
    return // 用户点了取消
  }
  try {
    await discardCopy(row.barCode)
    ElMessage.success('注销成功')
    await loadData()
  } catch {
    /* request 拦截器已提示错误 */
  }
}

// 返回图书目录列表页
function goBack() {
  router.push('/books/catalog')
}

// 跳转到编辑书目页
function goEditBook() {
  router.push(`/books/${isbn}/edit`)
}

onMounted(loadData) // 组件挂载后加载数据
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.page-header .page-title {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}

.book-info-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.book-info-main {
  flex: 1;
  min-width: 0;
}

.info-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}

.book-info-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}
</style>
