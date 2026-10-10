<template>
  <!-- 整个还书页面的根容器 -->
  <div class="page-container">
    <h2 class="page-title">📤 还书</h2>

    <!-- 上方检索卡片：输入借书证号查询在借列表 -->
    <el-card shadow="never" class="card-wrapper">
      <!-- 搜索栏：输入框 + 检索按钮 + 重置按钮 -->
      <div class="search-bar">
        <!-- 借书证号输入框，回车也能触发检索 -->
        <el-input
          v-model="cardNo"
          placeholder="请输入借书证号"
          size="large"
          style="width: 320px;"
          @keyup.enter="handleCardSearch"
        />
        <!-- 检索按钮，加载中显示 loading 态 -->
        <el-button type="primary" size="large" :loading="cardLoading" @click="handleCardSearch">检索</el-button>
        <!-- 重置按钮：清空输入和已查询结果 -->
        <el-button size="large" @click="resetPanel">重置</el-button>
      </div>

      <!-- 检索成功后展示借书证基本信息：证号、学号、姓名 -->
      <el-descriptions v-if="cardInfo" :column="3" border class="mt-16">
        <el-descriptions-item label="借书证号">{{ cardInfo.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ cardInfo.sno }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ cardInfo.studentName || '—' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 下方卡片：当前在借图书列表 -->
    <el-card shadow="never" class="card-wrapper mt-16">
      <!-- 卡片标题 -->
      <template #header>
        <span class="section-title">当前所借图书</span>
      </template>
      <!-- 在借图书表格，支持加载态和斑马纹 -->
      <el-table
        :data="borrowings"
        v-loading="listLoading"
        stripe
        :row-class-name="row => row.overdueDays > 0 ? 'overdue-row' : ''"
        :empty-text="cardInfo ? '暂无在借图书' : '请先检索借书证号'"
      >
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="70" />
        <!-- 书名列，溢出文本用 tooltip 展示 -->
        <el-table-column prop="bookName" label="书名" min-width="160" show-overflow-tooltip />
        <!-- 作者列 -->
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <!-- 副本条码号 -->
        <el-table-column prop="barCode" label="副本号" width="150" />
        <!-- 借书日期列 -->
        <el-table-column prop="borDate" label="借书日期" width="120" />
        <!-- 应还日期列：超期则文字标红并显示超期天数 -->
        <el-table-column label="应还日期" width="150">
          <template #default="{ row }">
            <!-- 超期天数大于 0 时字体变红 -->
            <span :style="{ color: row.overdueDays > 0 ? 'var(--color-danger)' : '' }">
              {{ row.retDate }}
              <template v-if="row.overdueDays > 0">（超期 {{ row.overdueDays }} 天）</template>
            </span>
          </template>
        </el-table-column>
        <!-- 操作列：还书按钮，固定在右侧 -->
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 还书按钮，点击时根据条码显示对应 loading 态 -->
            <el-button
              type="primary"
              link
              :loading="returningBarCode === row.barCode"
              @click="handleReturn(row)"
            >
              还书
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 还书页面：输入借书证号查看当前在借列表。
 * 超期行会高亮显示（红色背景），应还日期列也会标红提示超期天数。
 * 还书时如有超期，弹窗确认罚款金额后再提交。
 */
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getBorrowingsByCard, returnBook } from '@/api/modules/borrow'

// 获取全局认证状态，用于读取当前用户借书证号
const authStore = useAuthStore()

const cardNo = ref('') // 用户输入的借书证号
const cardInfo = ref(null) // 当前检索到的借书证信息（证号/学号/姓名）
const cardLoading = ref(false) // 检索按钮的 loading 状态
const borrowings = ref([]) // 当前证号下所有在借图书列表
const listLoading = ref(false) // 借阅列表的加载状态
const returningBarCode = ref('') // 正在还书的条码号，用于控制单行按钮 loading

// 页面加载时，如果用户已登录则自动填入其借书证号并触发检索
onMounted(() => {
  if (authStore.cardNo) {
    cardNo.value = authStore.cardNo
    handleCardSearch()
  }
})

// 重置面板：恢复默认借书证号，清空已查询的证信息和借阅列表
function resetPanel() {
  cardNo.value = authStore.cardNo || ''
  cardInfo.value = null
  borrowings.value = []
}

// 根据借书证号查询在借列表，同时加载证信息和借阅数据
async function handleCardSearch() {
  const no = cardNo.value.trim() // 去除前后空格
  if (!no) {
    ElMessage.warning('请输入借书证号') // 空值校验
    return
  }

  cardLoading.value = true // 开启检索按钮 loading
  listLoading.value = true // 开启列表 loading
  try {
    // 调用后端接口，返回证信息 + 在借列表
    const { card, borrowings: list } = await getBorrowingsByCard(no)
    cardInfo.value = card // 存储借书证信息
    borrowings.value = list // 存储在借图书列表
  } catch {
    // 查询失败时清空结果，避免展示脏数据
    cardInfo.value = null
    borrowings.value = []
  } finally {
    cardLoading.value = false // 关闭检索按钮 loading
    listLoading.value = false // 关闭列表 loading
  }
}

// 还书前判断是否超期，超期则提示罚款金额（每天 1 元），确认后才提交
async function handleReturn(row) {
  const overdue = row.overdueDays > 0 // 判断是否超期
  const fineAmount = overdue ? row.overdueDays * 1 : 0 // 超期罚款按每天 1 元计算
  // 根据是否超期组装不同的确认提示文案
  const msg = overdue
    ? `该书已超期 ${row.overdueDays} 天，需缴纳罚款 ¥${fineAmount.toFixed(2)}，确认还书？`
    : '确认归还该图书？'

  try {
    // 弹出二次确认框，超期用 warning 样式，未超期用 info 样式
    await ElMessageBox.confirm(msg, '还书确认', { type: overdue ? 'warning' : 'info' })
  } catch {
    return // 用户点了取消，直接返回不操作
  }

  returningBarCode.value = row.barCode // 设置当前正在还书的条码，触发行内按钮 loading
  try {
    // 调用后端还书接口
    const res = await returnBook({ barCode: row.barCode })
    if (res.data?.overdue) {
      // 还书成功且产生超期罚款，提示罚款金额
      ElMessage.success(`还书成功，产生超期罚款 ¥${res.data.fineAmount}`)
    } else {
      ElMessage.success('还书成功')
    }
    // 还书后重新加载在借列表，刷新页面数据
    await handleCardSearch()
  } finally {
    returningBarCode.value = '' // 无论成功失败都清除按钮 loading
  }
}

</script>

<style scoped>
/* 表格上方区域标题样式 */
.section-title {
  font-size: 15px;
  font-weight: 600;
}

/* 超期行整行标红，通过 row-class-name 动态注入 */
:deep(.overdue-row) {
  background-color: var(--color-accent-bg) !important;
}
</style>
