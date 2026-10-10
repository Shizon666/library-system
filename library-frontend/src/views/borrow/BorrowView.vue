<template>
  <div class="page-container">
    <!-- 页面大标题 -->
    <h2 class="page-title">📖 借书</h2>

    <!-- 第一步卡片：输入借书证号进行检索 -->
    <el-card v-if="step === 1" shadow="never" class="card-wrapper">
      <div class="search-bar">
        <!-- 输入学号或借书证号，回车也能触发检索 -->
        <el-input
          v-model="cardNo"
          placeholder="请输入学号或借书证号"
          size="large"
          style="width: 320px;"
          @keyup.enter="handleCardSearch"
        />
        <!-- 确认按钮：调用后端校验借书资格 -->
        <el-button type="primary" size="large" :loading="cardLoading" @click="handleCardSearch">确认</el-button>
        <!-- 重置按钮：清空所有状态回到初始 -->
        <el-button size="large" @click="resetAll">重置</el-button>
      </div>
    </el-card>

    <!-- 第二步：借书证验证通过后，展示选书借出界面 -->
    <template v-else>
      <!-- 借书证信息摘要卡片，展示证号、姓名、剩余可借数 -->
      <el-card shadow="never" class="card-wrapper">
        <el-descriptions :column="3" border size="default">
          <el-descriptions-item label="借书证号">{{ borrowCheck.cardNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ borrowCheck.studentName }}</el-descriptions-item>
          <!-- 实时显示还可借数量，绿色标签醒目提示 -->
          <el-descriptions-item label="还可借">
            <el-tag type="success">{{ remainingSlots }} 本</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 检索图书副本的搜索区域 -->
      <el-card shadow="never" class="card-wrapper mt-16">
        <div class="search-bar">
          <!-- 关键词输入框：支持书名/ISBN/副本号/作者/出版社/条形码 -->
          <el-input
            v-model="keyword"
            placeholder="书名 / ISBN / 副本号 / 作者 / 出版社，或扫描条形码"
            size="large"
            style="flex: 1; max-width: 520px;"
            @keyup.enter="handleBookSearch"
          />
          <!-- 搜索按钮：触发图书副本检索 -->
          <el-button type="primary" size="large" :loading="bookLoading" @click="handleBookSearch">确认</el-button>
          <!-- 返回按钮：回到第一步重新验证借书证 -->
          <el-button size="large" @click="backToStep1">返回</el-button>
        </div>

        <!-- 检索结果表格：列出匹配的副本信息 -->
        <el-table
          :data="bookResult"
          v-loading="bookLoading"
          stripe
          class="mt-16"
          empty-text="请输入书名/ISBN/副本号/作者后点击确认检索"
        >
          <el-table-column prop="barCode" label="副本号" width="150" />
          <el-table-column prop="ISBN" label="ISBN" width="150" />
          <el-table-column prop="bookName" label="书名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
          <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
          <el-table-column prop="place" label="藏书位置" width="140" show-overflow-tooltip />
          <!-- 副本状态列：用不同颜色标签区分可借/借出/注销 -->
          <el-table-column label="副本状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'" size="small">
                {{ row.statusText || (row.status === 1 ? '可借' : row.status === 0 ? '借出' : '注销') }}
              </el-tag>
            </template>
          </el-table-column>
          <!-- 操作列：只有可借状态的副本才能添加到待借列表 -->
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link :disabled="row.status !== 1" @click="addToPending(row)">
                {{ row.status === 1 ? '添加' : '不可借' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 搜索提示信息：没找到或全部借出时展示 -->
        <el-alert v-if="searchMessage" :title="searchMessage" :type="searchMessageType" show-icon :closable="false" class="mt-16" />
      </el-card>

      <!-- 待借列表卡片：用户选中的待借图书汇总 -->
      <el-card shadow="never" class="card-wrapper mt-16">
        <!-- 标题栏显示当前待借数量和可借上限 -->
        <template #header>
          <span class="section-title">📋 待借列表（{{ pendingList.length }} / {{ remainingSlots }}）</span>
        </template>
        <!-- 待借图书列表表格 -->
        <el-table :data="pendingList" stripe empty-text="检索图书后点击「添加」加入待借列表">
          <el-table-column type="index" label="序号" width="70" />
          <el-table-column prop="barCode" label="副本号" width="150" />
          <el-table-column prop="ISBN" label="ISBN" width="150" />
          <el-table-column prop="bookName" label="书名" min-width="160" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
          <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
          <el-table-column prop="place" label="藏书位置" width="140" show-overflow-tooltip />
          <!-- 移除按钮：从待借列表中删除该条 -->
          <el-table-column label="操作" width="80" fixed="right" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link @click="removeFromPending($index)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 底部操作栏：清空列表 和 确认借出 -->
        <div class="action-bar mt-16">
          <el-button size="large" :disabled="pendingList.length === 0" @click="clearPending">清空列表</el-button>
          <!-- 批量借出按钮：列表为空时禁用 -->
          <el-button
            type="primary"
            size="large"
            :loading="borrowLoading"
            :disabled="pendingList.length === 0"
            @click="handleBatchBorrow"
          >
            确认借出（{{ pendingList.length }} 本）
          </el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
/**
 * 借书页面，分两步：
 *   Step 1 — 输入学号/借书证号，校验是否可借（有效证、未达上限、无未缴罚款等）。
 *   Step 2 — 搜索图书副本，添加到待借列表，最后批量借出。
 * remainingSlots = 后端返回的剩余可借数 - 当前待借列表数量，实时更新。
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { checkBorrowEligibility } from '@/api/modules/cards'
import { searchCopyForBorrow, borrowBooks } from '@/api/modules/borrow'

// 拿到登录态 store，用来读取当前用户的借书证号
const authStore = useAuthStore()

// 当前步骤：1=输入借书证号，2=选书借出
const step = ref(1)
// 用户输入的借书证号
const cardNo = ref('')
// 借书证检索的加载态
const cardLoading = ref(false)
// 后端返回的借书资格校验结果（包含 canBorrow / remainingSlots / borrowLimit 等）
const borrowCheck = ref(null)

// 图书检索关键词（书名/ISBN/副本号/作者/出版社）
const keyword = ref('')
// 图书检索的加载态
const bookLoading = ref(false)
// 检索结果列表（副本数组）
const bookResult = ref([])
// 搜索无结果或全部借出时的提示文案
const searchMessage = ref('')
// 提示类型：warning / error
const searchMessageType = ref('warning')
// 待借列表：用户选中准备借出的副本
const pendingList = ref([])
// 批量借出接口的加载态
const borrowLoading = ref(false)

// 实时计算剩余可借数：后端返回的额度 - 已加入待借列表的数量
const remainingSlots = computed(() => {
  const total = borrowCheck.value?.remainingSlots ?? 0
  return Math.max(0, total - pendingList.value.length)
})

// 页面加载时如果已登录，自动填入当前用户的借书证号
onMounted(() => {
  if (authStore.cardNo) {
    cardNo.value = authStore.cardNo
  }
})

// 重置全部状态，回到第一步初始界面
function resetAll() {
  step.value = 1
  cardNo.value = authStore.cardNo || ''
  borrowCheck.value = null
  keyword.value = ''
  bookResult.value = []
  searchMessage.value = ''
  pendingList.value = []
}

// 返回第一步，清空图书检索相关状态但保留借书证信息
function backToStep1() {
  step.value = 1
  keyword.value = ''
  bookResult.value = []
  searchMessage.value = ''
  pendingList.value = []
}

// 清空待借列表
function clearPending() {
  pendingList.value = []
}

// 加入待借列表前检查：是否达到可借上限、是否重复添加
function addToPending(row) {
  // 检查是否还有剩余可借额度
  if (remainingSlots.value <= 0) {
    ElMessage.warning(`待借列表已达上限（还可借 ${borrowCheck.value?.remainingSlots ?? 0} 本）`)
    return
  }
  // 同一副本不能重复添加
  if (pendingList.value.some(item => item.barCode === row.barCode)) {
    ElMessage.warning('该副本已在待借列表中')
    return
  }
  // 用展开运算符拷贝一份，避免直接引用原对象
  pendingList.value.push({ ...row })
  ElMessage.success(`已添加《${row.bookName}》`)
  // 添加成功后清空搜索框和结果，方便继续搜索下一本
  keyword.value = ''
  bookResult.value = []
  searchMessage.value = ''
}

// 从待借列表中移除指定索引的条目
function removeFromPending(index) {
  pendingList.value.splice(index, 1)
}

// 第一步核心逻辑：校验借书证是否具备借书资格
async function handleCardSearch() {
  const no = cardNo.value.trim()
  // 空输入拦截
  if (!no) {
    ElMessage.warning('请输入借书证号')
    return
  }

  cardLoading.value = true
  try {
    // 调后端接口：校验借书证有效性、剩余可借数、是否有未缴罚款等
    const res = await checkBorrowEligibility(no)
    const data = res.data
    // 根据是否可借拼接不同的提示信息
    const msg = data.canBorrow
      ? `还可以借 ${data.remainingSlots} 本书（上限 ${data.borrowLimit} 本）`
      : data.reason

    // 弹窗确认，让用户知晓校验结果
    try {
      await ElMessageBox.confirm(msg, '借书证检索结果', {
        type: data.canBorrow ? 'success' : 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消',
      })
    } catch {
      // 用户点了取消，什么都不做
      return
    }

    // 不可借则重置页面
    if (!data.canBorrow) {
      resetAll()
      return
    }

    // 校验通过，保存结果并进入第二步
    borrowCheck.value = data
    pendingList.value = []
    step.value = 2
  } finally {
    cardLoading.value = false
  }
}

// 第二步核心逻辑：按关键词检索图书副本
async function handleBookSearch() {
  const kw = keyword.value.trim()
  // 空输入拦截
  if (!kw) {
    ElMessage.warning('请输入检索信息')
    return
  }
  // 待借列表已满就不让继续搜了
  if (remainingSlots.value <= 0) {
    ElMessage.warning(`待借列表已满（最多 ${borrowCheck.value?.remainingSlots ?? 0} 本）`)
    return
  }

  bookLoading.value = true
  bookResult.value = []
  searchMessage.value = ''
  try {
    // 调后端接口：按关键词检索可用副本
    const res = await searchCopyForBorrow(kw)
    const data = res.data
    if (data.found && data.copies && data.copies.length > 0) {
      // 有结果，填充表格
      bookResult.value = data.copies
      if (data.allBorrowed) {
        // 找到了书但所有副本都被借出了
        searchMessage.value = data.message || '该书所有副本已被借出，可查看副本状态'
        searchMessageType.value = 'warning'
      }
    } else {
      // 完全没找到这本书
      searchMessage.value = data.message || '不存在此书籍'
      searchMessageType.value = 'error'
    }
  } finally {
    bookLoading.value = false
  }
}

// 批量借出：把待借列表里所有 barCode 一次性提交，借出后刷新剩余数
async function handleBatchBorrow() {
  // 待借列表为空直接拦截
  if (pendingList.value.length === 0) {
    ElMessage.warning('请先添加待借图书')
    return
  }

  // 二次确认弹窗，防止误操作
  try {
    await ElMessageBox.confirm(
      `确认借出 ${pendingList.value.length} 本图书？`,
      '批量借书',
      { type: 'info' }
    )
  } catch {
    // 用户取消
    return
  }

  borrowLoading.value = true
  try {
    // 提交批量借出请求：传入借书证号 + 副本号列表
    const res = await borrowBooks({
      cardNo: borrowCheck.value.cardNo,
      barCodes: pendingList.value.map(item => item.barCode),
    })
    // 取后端返回的实际借出数量和应还日期
    const count = res.data?.count ?? pendingList.value.length
    const dueDate = res.data?.dueDate?.substring?.(0, 10) || res.data?.dueDate || ''
    ElMessage.success(`成功借出 ${count} 本，应还日期 ${dueDate}`)

    // 借出成功后更新剩余可借数，清空待借列表和搜索状态
    borrowCheck.value.remainingSlots -= count
    pendingList.value = []
    keyword.value = ''
    bookResult.value = []
    searchMessage.value = ''

    // 已达上限时提示用户
    if (borrowCheck.value.remainingSlots <= 0) {
      ElMessage.info('已达借阅上限')
    }
  } finally {
    borrowLoading.value = false
  }
}
</script>

<style scoped>
/* 待借列表卡片标题样式 */
.section-title {
  font-size: 15px;
  font-weight: 600;
}
</style>
