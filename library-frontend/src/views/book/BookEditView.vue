<template>
  <div class="page-container">
    <!-- 权限拦截：非管理员显示无权限提示 -->
    <template v-if="isAdmin">
      <h2 class="page-title">✏️ 图书采编</h2>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- 新书入库 / 副本新增 -->
      <el-tab-pane label="新书入库" name="acquire">
        <el-card shadow="never" class="card-wrapper">
          <el-form ref="acquireFormRef" :model="acquireForm" :rules="acquireRules" label-width="90px">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="ISBN" prop="ISBN">
                  <el-input v-model="acquireForm.ISBN" placeholder="请输入ISBN" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="书名" prop="bookName">
                  <el-input v-model="acquireForm.bookName" placeholder="请输入书名" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="作者" prop="author">
                  <el-input v-model="acquireForm.author" placeholder="请输入作者" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="出版社" prop="publisher">
                  <el-input v-model="acquireForm.publisher" placeholder="请输入出版社" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="出版日期" prop="pubDate">
                  <el-date-picker
                    v-model="acquireForm.pubDate"
                    type="date"
                    placeholder="选择日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="分类" prop="category">
                  <el-input v-model="acquireForm.category" placeholder="请输入分类" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="简介" prop="introduction">
              <el-input v-model="acquireForm.introduction" type="textarea" :rows="2" placeholder="请输入简介" />
            </el-form-item>
          </el-form>

          <!-- 副本列表 -->
          <h3 class="section-title">📋 副本列表</h3>
          <div class="action-bar">
            <el-button type="success" :icon="Plus" @click="addCopyRow">添加副本</el-button>
          </div>
          <el-table :data="acquireForm.copies" stripe size="default">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="条码" min-width="180">
              <template #default="{ row }">
                <el-input v-model="row.barCode" placeholder="请输入副本条码" size="default" />
              </template>
            </el-table-column>
            <el-table-column label="馆藏地点" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.place" placeholder="请输入馆藏地点" size="default" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ $index }">
                <el-button type="danger" link @click="removeCopyRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="action-bar mt-16">
            <el-button type="primary" size="large" :loading="acquireLoading" @click="handleAcquire">
              确认入库
            </el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 图书下架 -->
      <el-tab-pane label="图书下架" name="offShelf">
        <el-card shadow="never" class="card-wrapper">
          <div class="search-bar">
            <el-input v-model="isbnOffShelf" placeholder="请输入要下架的图书 ISBN" size="large" style="width: 300px;" />
            <el-button type="primary" size="large" :icon="Search" @click="queryBookForOffShelf">查询</el-button>
          </div>

          <el-descriptions v-if="bookOffShelfInfo" :column="2" border class="mt-16">
            <el-descriptions-item label="ISBN">{{ bookOffShelfInfo.ISBN }}</el-descriptions-item>
            <el-descriptions-item label="书名">{{ bookOffShelfInfo.bookName }}</el-descriptions-item>
            <el-descriptions-item label="作者">{{ bookOffShelfInfo.author }}</el-descriptions-item>
            <el-descriptions-item label="副本数">{{ bookOffShelfInfo.copies?.length || 0 }}</el-descriptions-item>
          </el-descriptions>

          <div class="action-bar mt-16">
            <el-button type="danger" size="large" :disabled="!bookOffShelfInfo" @click="handleOffShelf">
              确认下架全部副本
            </el-button>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 报废 -->
      <el-tab-pane label="报废" name="discard">
        <el-card shadow="never" class="card-wrapper">
          <div class="search-bar">
            <el-input v-model="discardBarCode" placeholder="请输入要报废的副本条码" size="large" style="width: 300px;" />
            <el-button type="danger" size="large" :icon="Delete" @click="handleDiscard">确认报废</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    </template>
    <!-- 非管理员：无权限提示 -->
    <template v-else>
      <el-result icon="warning" title="无权限访问" sub-title="图书采编仅限管理员操作，请联系管理员。">
        <template #extra>
          <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Plus, Search, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookDetail, acquireBook, offShelfBook, discardCopy } from '@/api/modules/books'
import logger from '@/utils/logger'

const COMPONENT = 'BookEditView > '
const authStore = useAuthStore()
const router = useRouter()
const isAdmin = computed(() => authStore.role === 'admin')
const activeTab = ref('acquire')

// === 新书入库 ===
const acquireFormRef = ref(null)
const acquireLoading = ref(false)

const initialAcquireForm = {
  ISBN: '',
  bookName: '',
  author: '',
  publisher: '',
  pubDate: '',
  category: '',
  introduction: '',
  copies: [],
}

const acquireForm = reactive({ ...initialAcquireForm })

const acquireRules = {
  ISBN: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    // FE-05: ISBN格式校验（10位或13位）
    { pattern: /^\d{9}[\dX]$|^\d{13}$/, message: 'ISBN格式不正确（10位或13位）', trigger: 'blur' },
  ],
  bookName: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '书名1-100字符', trigger: 'blur' },
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 100, message: '作者1-100字符', trigger: 'blur' },
  ],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
}

function addCopyRow() {
  acquireForm.copies.push({ barCode: '', place: '' })
}

function removeCopyRow(index) {
  acquireForm.copies.splice(index, 1)
}

async function handleAcquire() {
  logger.operation(`${COMPONENT}handleAcquire`, '▶ 进入', {
    ISBN: acquireForm.ISBN,
    bookName: acquireForm.bookName,
    copiesCount: acquireForm.copies.length,
  })

  let valid = false
  try {
    valid = await acquireFormRef.value.validate()
  } catch { /* ignore */ }
  if (!valid) {
    logger.validate(`${COMPONENT}handleAcquire`, '❌ 表单校验失败')
    return
  }

  if (acquireForm.copies.length === 0) {
    logger.validate(`${COMPONENT}handleAcquire`, '❌ 副本列表为空')
    ElMessage.warning('请至少添加一个副本')
    return
  }

  // FE-05: 副本条码不可重复
  const barCodes = acquireForm.copies.map(c => c.barCode)
  if (new Set(barCodes).size !== barCodes.length) {
    logger.validate(`${COMPONENT}handleAcquire`, '❌ 副本条码重复')
    ElMessage.warning('副本条码不可重复')
    return
  }

  acquireLoading.value = true
  try {
    await acquireBook({ ...acquireForm })
    logger.operation(`${COMPONENT}handleAcquire`, '✅ 成功', {
      ISBN: acquireForm.ISBN,
      bookName: acquireForm.bookName,
      addedCopies: acquireForm.copies.length,
    })
    ElMessage.success(`入库成功，新增 ${acquireForm.copies.length} 个副本`)
    Object.assign(acquireForm, { ...initialAcquireForm, copies: [] })
  } catch (e) {
    logger.error(`${COMPONENT}handleAcquire`, '❌ 入库失败', {
      ISBN: acquireForm.ISBN,
      bookName: acquireForm.bookName,
    })
  } finally {
    acquireLoading.value = false
  }
}

// === 图书下架 ===
const isbnOffShelf = ref('')
const bookOffShelfInfo = ref(null)

async function queryBookForOffShelf() {
  if (!isbnOffShelf.value) {
    ElMessage.warning('请输入 ISBN')
    return
  }
  try {
    const res = await getBookDetail(isbnOffShelf.value)
    bookOffShelfInfo.value = res.data
    ElMessage.success(`查询成功，共 ${res.data?.copies?.length || 0} 个副本`)
  } catch (e) {
    bookOffShelfInfo.value = null
    logger.error(`${COMPONENT}queryBookForOffShelf`, '❌ 查询失败')
  }
}

async function handleOffShelf() {
  const isbn = isbnOffShelf.value
  logger.operation(`${COMPONENT}handleOffShelf`, '▶ 进入', { ISBN: isbn })

  if (!isbn) {
    ElMessage.warning('请输入 ISBN')
    return
  }

  try {
    await ElMessageBox.confirm('确认下架该书全部副本？下架后所有副本状态将变为"下架"')
    logger.operation(`${COMPONENT}handleOffShelf`, '⏳ 用户已确认')
  } catch {
    logger.operation(`${COMPONENT}handleOffShelf`, '❌ 用户取消操作')
    return
  }

  try {
    await offShelfBook(isbn)
    logger.operation(`${COMPONENT}handleOffShelf`, '✅ 成功', { ISBN: isbn })
    ElMessage.success('下架成功')
    isbnOffShelf.value = ''
    bookOffShelfInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleOffShelf`, '❌ 下架失败', { ISBN: isbn })
  }
}

// === 报废 ===
const discardBarCode = ref('')

async function handleDiscard() {
  const barCode = discardBarCode.value
  logger.operation(`${COMPONENT}handleDiscard`, '▶ 进入', { barCode })

  if (!barCode) {
    ElMessage.warning('请输入副本条码')
    return
  }

  try {
    await ElMessageBox.confirm('确认报废该副本？报废后该副本将不可借出')
    logger.operation(`${COMPONENT}handleDiscard`, '⏳ 用户已确认')
  } catch {
    logger.operation(`${COMPONENT}handleDiscard`, '❌ 用户取消操作')
    return
  }

  try {
    await discardCopy(barCode)
    logger.operation(`${COMPONENT}handleDiscard`, '✅ 成功', { barCode })
    ElMessage.success('报废成功')
    discardBarCode.value = ''
  } catch (e) {
    logger.error(`${COMPONENT}handleDiscard`, '❌ 报废失败', { barCode })
  }
}

function onTabChange() {
  logger.operation(`${COMPONENT}onTabChange`, '🔄 切换Tab', { tab: activeTab.value })
}

onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', { role: authStore.role, isAdmin: isAdmin.value })
  if (!isAdmin.value) {
    logger.error(COMPONENT, '⛔ 非管理员访问被拦截', { role: authStore.role })
    ElMessage.error('无权限访问该页面')
    return
  }
})
</script>

<style scoped>
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 16px 0 8px;
}
</style>
