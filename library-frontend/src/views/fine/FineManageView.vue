<template>
  <!-- 整体页面容器 -->
  <div class="page-container">
    <!-- 页面标题 -->
    <h2 class="page-title">💰 罚款管理</h2>

    <!-- 主内容卡片 -->
    <el-card shadow="never" class="card-wrapper">
      <!-- 筛选操作栏 -->
      <div class="search-bar">
        <!-- 筛选标签 -->
        <span class="filter-label">缴纳状态：</span>
        <!-- 缴纳状态单选组：全部 / 未缴 / 已缴，切换自动刷新 -->
        <el-radio-group v-model="payStatus" @change="loadFines">
          <el-radio-button label="">全部</el-radio-button> <!-- 不筛选 -->
          <el-radio-button label="0">未缴</el-radio-button> <!-- 只看未缴 -->
          <el-radio-button label="1">已缴</el-radio-button> <!-- 只看已缴 -->
        </el-radio-group>
        <!-- 手动刷新按钮 -->
        <el-button type="primary" :icon="RefreshRight" @click="loadFines" class="ml-8">刷新</el-button>
        <!-- 批量缴纳按钮：无选中时置灰 -->
        <el-button
          type="success"
          :icon="Money"
          :disabled="selectedFineIds.length === 0"
          @click="openPayDialog"
        >
          缴纳选中 ({{ selectedFineIds.length }})
        </el-button>
      </div>

      <!-- 罚款数据表格：多选 + 斑马纹 + 固定表头滚动 -->
      <el-table
        :data="fineList"
        v-loading="loading"
        stripe
        size="default"
        @selection-change="onSelectionChange"
        max-height="500"
        empty-text="暂无罚款记录"
      >
        <!-- 多选列：已缴行不可勾选 -->
        <el-table-column type="selection" width="50" :selectable="row => row.payStatus === 0" />
        <!-- 罚款编号 -->
        <el-table-column prop="fineId" label="罚款编号" width="90" />
        <!-- 学号列 -->
        <el-table-column prop="sno" label="学号" width="120" show-overflow-tooltip />
        <!-- 学生姓名列 -->
        <el-table-column prop="studentName" label="姓名" width="100" show-overflow-tooltip />
        <!-- 图书条码列 -->
        <el-table-column prop="barCode" label="图书条码" width="140" show-overflow-tooltip />
        <!-- 书名列：自适应宽度 -->
        <el-table-column prop="bookName" label="书名" min-width="150" show-overflow-tooltip />
        <!-- 罚款金额列：红色加粗醒目展示 -->
        <el-table-column prop="amount" label="罚款金额" width="110" align="center">
          <template #default="{ row }">
            <span style="color: var(--color-danger); font-weight: bold;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <!-- 超期天数列 -->
        <el-table-column prop="overdueDays" label="超期天数" width="90" align="center" />
        <!-- 罚款产生日期列 -->
        <el-table-column prop="createTime" label="产生日期" width="120" />
        <!-- 缴纳状态列：红/绿 Tag 区分 -->
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 0 ? 'danger' : 'success'">
              {{ row.payStatus === 0 ? '未缴' : '已缴' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 缴纳弹窗：确认选中条目并填写缴纳信息 -->
    <el-dialog
      v-model="dialogVisible"
      title="缴纳罚款"
      width="480px"
      :close-on-click-modal="false"
    >
      <!-- 缴纳概览：选中笔数 + 合计金额 -->
      <el-descriptions :column="1" border size="default">
        <el-descriptions-item label="选中条数">{{ selectedFineIds.length }} 笔</el-descriptions-item>
        <el-descriptions-item label="合计金额">
          <!-- 合计金额大号红色加粗 -->
          <span style="color: var(--color-danger); font-weight: bold; font-size: 18px;">
            ¥{{ totalAmount.toFixed(2) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 缴纳信息表单 -->
      <el-form label-width="80px" class="mt-16">
        <!-- 借书证号：学生自动锁定，管理员手动输入 -->
        <el-form-item label="借书证号" required>
          <el-input
            v-model="payCardNo"
            :placeholder="isStudent ? '本人借书证号' : '请输入借书证号（必填）'"
            :disabled="isStudent"
          />
        </el-form-item>
        <!-- 操作员字段：仅管理员角色可见 -->
        <el-form-item label="操作员" v-if="!isStudent">
          <el-input v-model="operator" placeholder="操作员（选填）" />
        </el-form-item>
        <!-- 备注输入区 -->
        <el-form-item label="备注">
          <el-input v-model="remark" type="textarea" :rows="2" placeholder="备注（选填）" />
        </el-form-item>
      </el-form>

      <!-- 弹窗底部操作按钮 -->
      <template #footer>
        <!-- 取消按钮 -->
        <el-button @click="dialogVisible = false">取消</el-button>
        <!-- 确认缴纳按钮：带 loading 防重复提交 -->
        <el-button type="primary" :loading="payLoading" @click="handlePay">
          确认缴纳 ¥{{ totalAmount.toFixed(2) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 罚款管理页面：支持按缴纳状态筛选，勾选多条未缴罚款后批量缴纳。
 * 学生角色进入时借书证号自动填充且不可改，管理员需手动输入。
 */
import { ref, computed, onMounted } from 'vue'
import { Money, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFines, payFines } from '@/api/modules/fines'
import { useAuthStore } from '@/stores/auth'
import logger from '@/utils/logger'

// 日志前缀，方便在控制台过滤本组件日志
const COMPONENT = 'FineManageView > '

// 获取全局认证 store
const authStore = useAuthStore()
// 计算属性：当前用户是否为学生
const isStudent = computed(() => authStore.role === 'student')
// 表格数据加载状态，控制 v-loading 骨架
const loading = ref(false)
// 后端返回的罚款列表数据
const fineList = ref([])
// 筛选条件：''=全部 '0'=未缴 '1'=已缴，默认展示未缴
const payStatus = ref('0')

// 当前表格多选勾选的罚款 ID 数组
const selectedFineIds = ref([])
// 缴纳弹窗的显示开关
const dialogVisible = ref(false)
// 弹窗内借书证号输入值
const payCardNo = ref('')
// 弹窗内操作员姓名（管理员角色才用）
const operator = ref('')
// 弹窗内备注文本
const remark = ref('')
// 缴纳请求进行中，防止重复点击
const payLoading = ref(false)

// 实时计算已勾选罚款的总金额
const totalAmount = computed(() => {
  return fineList.value
    .filter(f => selectedFineIds.value.includes(f.fineId)) // 筛出已勾选项
    .reduce((sum, f) => sum + (f.amount || 0), 0) // 累加金额
})

// 表格多选变化回调，同步勾选 ID 到响应式变量
function onSelectionChange(rows) {
  selectedFineIds.value = rows.map(r => r.fineId) // 映射出纯 ID 数组
}

// 请求后端拉取罚款列表，支持按缴纳状态筛选
async function loadFines() {
  logger.render(`${COMPONENT}loadFines`, '▶ 进入', {
    triggeredBy: 'onMounted/Tab切换',
    payStatus: payStatus.value || '全部',
  })

  const params = {}
  // 空串代表"全部"，此时不传 payStatus 参数
  if (payStatus.value !== '') {
    params.payStatus = payStatus.value
  }

  // 开启表格 loading
  loading.value = true
  try {
    const res = await getFines(params) // 调用罚款查询接口
    fineList.value = res.data || [] // 赋值给表格数据源

    // 分别统计未缴和已缴的数量，用于日志排查
    const unpaid = fineList.value.filter(f => f.payStatus === 0)
    const paid = fineList.value.filter(f => f.payStatus === 1)
    const totalUnpaid = unpaid.reduce((sum, f) => sum + (f.amount || 0), 0) // 未缴总金额

    logger.render(`${COMPONENT}loadFines`, '🔚 结束', {
      totalCount: fineList.value.length,
      unpaidCount: unpaid.length,
      paidCount: paid.length,
      unpaidTotalAmount: totalUnpaid,
    })
  } catch (e) {
    // 查询失败时记录错误日志
    logger.error(`${COMPONENT}loadFines`, '❌ 查询失败', { payStatus: payStatus.value })
  } finally {
    // 无论成功失败都关闭 loading
    loading.value = false
  }
}

// 打开缴纳弹窗：学生自动填证号，管理员清空等手动输入
function openPayDialog() {
  payCardNo.value = isStudent.value ? authStore.cardNo : '' // 学生自动填充
  operator.value = '' // 清空操作员
  remark.value = '' // 清空备注
  dialogVisible.value = true // 弹窗可见
}

// 确认缴纳逻辑：校验 -> 调接口 -> 成功后刷新列表
async function handlePay() {
  // 借书证号是必填项，为空时提示并中断
  if (!payCardNo.value) {
    ElMessage.warning('请输入借书证号')
    return
  }

  // 取出当前勾选的罚款 ID 列表
  const ids = selectedFineIds.value
  logger.operation(`${COMPONENT}handlePay`, '▶ 进入', {
    selectedCount: ids.length,
    fineIds: ids,
    totalAmount: totalAmount.value,
  })

  // 开启按钮 loading 防重复提交
  payLoading.value = true
  try {
    await payFines({ // 调用批量缴纳接口
      cardNo: payCardNo.value,
      fineIds: ids,
      operator: operator.value,
      remark: remark.value,
    })

    // 缴纳成功日志
    logger.operation(`${COMPONENT}handlePay`, '✅ 成功', {
      paidCount: ids.length,
      totalAmount: totalAmount.value,
      cardNo: payCardNo.value,
    })
    ElMessage.success(`缴纳成功，共 ¥${totalAmount.value.toFixed(2)}`)
    selectedFineIds.value = [] // 清空表格勾选
    dialogVisible.value = false // 关闭弹窗
    loadFines() // 刷新列表反映最新状态
  } catch (e) {
    // 缴纳失败记录错误
    logger.error(`${COMPONENT}handlePay`, '❌ 缴纳失败', { fineIds: ids, totalAmount: totalAmount.value })
  } finally {
    // 无论成败都释放 loading
    payLoading.value = false
  }
}

// 组件挂载后自动拉取罚款列表
onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', {})
  loadFines() // 首次加载
})
</script>

<style scoped>
.filter-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-right: 8px;
}
</style>
