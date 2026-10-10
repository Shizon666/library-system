<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <h2 class="page-title">👤 个人信息</h2>

    <!-- 模块一：基本信息卡片，展示学号/姓名/学院/专业/类型/借书证号 -->
    <el-card shadow="never" class="card-wrapper">
      <template #header>
        <div class="card-header-row">
          <span class="card-header-title">基本信息</span>
          <!-- 修改密码按钮：打开密码修改弹窗 -->
          <el-button type="primary" @click="pwdDialogVisible = true">修改密码</el-button>
        </div>
      </template>
      <!-- 基本信息描述列表：两列布局 -->
      <el-descriptions v-loading="profileLoading" :column="2" border>
        <el-descriptions-item label="学号">{{ profile.sno || '—' }}</el-descriptions-item>   <!-- 学号字段 -->
        <el-descriptions-item label="姓名">{{ profile.studentName || '—' }}</el-descriptions-item> <!-- 姓名字段 -->
        <el-descriptions-item label="学院">{{ profile.college || '—' }}</el-descriptions-item> <!-- 所属学院 -->
        <el-descriptions-item label="专业">{{ profile.major || '—' }}</el-descriptions-item>  <!-- 所学专业 -->
        <el-descriptions-item label="学生类型">{{ profile.type || '—' }}</el-descriptions-item> <!-- 本科/研究生等 -->
        <el-descriptions-item label="借书证号">{{ profile.cardNo || '—' }}</el-descriptions-item> <!-- 借书证编号 -->
      </el-descriptions>
    </el-card>

    <!-- 模块二：借阅历史记录表格 -->
    <el-card shadow="never" class="card-wrapper mt-16">
      <template #header>
        <span class="card-header-title">📖 借阅记录</span>
      </template>
      <!-- 借阅记录表格：展示书名/作者/借书/应还/实还/状态/罚款 -->
      <el-table :data="history" v-loading="historyLoading" stripe empty-text="暂无借阅记录">
        <!-- 自增序号列 -->
        <el-table-column type="index" label="序号" width="70" />
        <!-- 书名：超长文本溢出省略 -->
        <el-table-column prop="bookName" label="书名" min-width="160" show-overflow-tooltip />
        <!-- 作者名 -->
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <!-- 借出日期 -->
        <el-table-column prop="borDate" label="借书日期" width="120" />
        <!-- 应归还的截止日期 -->
        <el-table-column prop="retDate" label="应还日期" width="120" />
        <!-- 实还日期：未还时显示"—" -->
        <el-table-column prop="realRetDate" label="实还日期" width="120">
          <template #default="{ row }">{{ row.realRetDate || '—' }}</template>
        </el-table-column>
        <!-- 还书状态列：用 Tag 颜色区分未归还/超时还/已归还 -->
        <el-table-column label="还书状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="retStatusType(row.retStatusText)">{{ row.retStatusText || '—' }}</el-tag>
          </template>
        </el-table-column>
        <!-- 罚款金额列：有罚款时红色显示，否则显示"—" -->
        <el-table-column label="罚款金额" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.fineAmount > 0 ? 'var(--color-danger)' : '' }">
              {{ formatAmount(row.fineAmount) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 模块三：罚款缴纳卡片，支持勾选多条批量支付 -->
    <el-card shadow="never" class="card-wrapper mt-16">
      <template #header>
        <div class="card-header-row">
          <span class="card-header-title">💰 缴纳罚款</span>
          <!-- 切换显示全部/仅未缴罚款 -->
          <el-button text type="primary" @click="toggleShowAllFines">
            {{ showAllFines ? '仅显示未缴' : '显示全部' }}
          </el-button>
        </div>
      </template>
      <!-- 罚款列表：支持多选，已缴行禁用勾选 -->
      <el-table
        ref="fineTableRef"
        :data="displayFines"
        v-loading="finesLoading"
        stripe
        empty-text="暂无罚款记录"
        @selection-change="onFineSelectionChange"
      >
        <!-- 只有未缴的行可以勾选 -->
        <el-table-column type="selection" width="50" :selectable="row => row.payStatus === 0" />
        <!-- 自增序号 -->
        <el-table-column type="index" label="序号" width="70" />
        <!-- 关联的书名 -->
        <el-table-column prop="bookName" label="书名" min-width="160" show-overflow-tooltip />
        <!-- 作者 -->
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <!-- 原应还日期 -->
        <el-table-column prop="retDate" label="应还日期" width="120" />
        <!-- 实还日期：未还显示"—" -->
        <el-table-column prop="realRetDate" label="实还日期" width="120">
          <template #default="{ row }">{{ row.realRetDate || '—' }}</template>
        </el-table-column>
        <!-- 罚款金额：红色加粗 -->
        <el-table-column label="罚款金额" width="100" align="center">
          <template #default="{ row }">
            <span style="color: var(--color-danger); font-weight: 600;">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <!-- 缴纳状态列：未缴红色/已缴绿色 Tag -->
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 0 ? 'danger' : 'success'">
              {{ row.payStatus === 0 ? '未缴' : '已缴' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <!-- 底部操作栏：已选金额合计 + 支付按钮 -->
      <div class="fine-pay-bar">
        <span class="fine-total">已选取金额合计：<strong>¥{{ selectedTotal.toFixed(2) }}</strong></span>
        <el-button type="primary" size="large" :disabled="selectedFines.length === 0" :loading="payLoading" @click="handlePay">
          支付
        </el-button>
      </div>
    </el-card>

    <!-- 模块四：修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" @closed="resetPwdForm">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <!-- 原密码输入 -->
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <!-- 新密码输入：6-20位 -->
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位新密码" />
        </el-form-item>
        <!-- 确认密码输入：需与新密码一致 -->
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <!-- 弹窗底部：取消和确认修改 -->
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 学生个人中心：四个模块 — 基本信息、借阅历史、罚款缴纳、修改密码。
 * 罚款模块默认只显示未缴记录，可切换"显示全部"；支持勾选多条批量支付。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getMyProfile, changePassword, getMyHistory, getMyFines } from '@/api/modules/me'
import { payFines } from '@/api/modules/fines'

const authStore = useAuthStore() // 全局认证状态
const sno = authStore.sno // 当前登录学生的学号
const cardNo = authStore.cardNo // 当前登录学生的借书证号

const profile = ref({}) // 个人信息对象
const profileLoading = ref(false) // 个人信息加载态
const history = ref([]) // 借阅历史列表
const historyLoading = ref(false) // 借阅历史加载态
const allFines = ref([]) // 全部罚款记录（含已缴和未缴）
const finesLoading = ref(false) // 罚款列表加载态
const showAllFines = ref(false) // 是否显示全部罚款（默认只显示未缴）
const selectedFines = ref([]) // 勾选的罚款记录对象数组
const payLoading = ref(false) // 支付操作加载态

const pwdDialogVisible = ref(false) // 修改密码弹窗是否可见
const pwdLoading = ref(false) // 密码修改操作加载态
const pwdFormRef = ref(null) // 密码表单的 ref，用于手动触发校验
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' }) // 密码表单数据

// 密码表单校验规则
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }], // 原密码必填
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' }, // 新密码必填
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }, // 长度限制
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' }, // 确认密码必填
    {
      // 自定义校验：确认密码必须与新密码一致
      validator: (_, value, callback) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

// 默认只显示未缴罚款，切换后显示全部（含已缴）
const displayFines = computed(() =>
  showAllFines.value ? allFines.value : allFines.value.filter(f => f.payStatus === 0)
)

// 已勾选罚款的金额合计，实时计算
const selectedTotal = computed(() =>
  selectedFines.value.reduce((sum, f) => sum + Number(f.amount || 0), 0)
)

// 根据还书状态文本返回 Tag 类型颜色
function retStatusType(text) {
  if (text === '未归还') return 'warning' // 橙色警告
  if (text === '超时还') return 'danger' // 红色危险
  return 'success' // 绿色正常
}

// 格式化金额：大于0显示"¥xx.xx"，否则显示"—"
function formatAmount(val) {
  const n = Number(val || 0)
  return n > 0 ? `¥${n.toFixed(2)}` : '—'
}

// 切换显示全部/仅未缴罚款，同时清空勾选
function toggleShowAllFines() {
  showAllFines.value = !showAllFines.value
  selectedFines.value = [] // 切换时清空勾选，避免金额混乱
}

// 罚款表格勾选变化回调
function onFineSelectionChange(rows) {
  selectedFines.value = rows // 直接存整行数据，方便后续取 fineId
}

// 重置密码表单：清空字段 + 清除校验状态
function resetPwdForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.resetFields() // 清除 Element 表单校验提示
}

// 加载个人信息
async function loadProfile() {
  profileLoading.value = true
  try {
    const res = await getMyProfile() // 调后端个人信息接口
    profile.value = res.data || {}
  } finally {
    profileLoading.value = false
  }
}

// 加载借阅历史记录
async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await getMyHistory(sno) // 按学号查询借阅记录
    history.value = res.data || []
  } finally {
    historyLoading.value = false
  }
}

// 加载罚款记录（含已缴和未缴）
async function loadFines() {
  finesLoading.value = true
  try {
    const res = await getMyFines(sno) // 按学号查询罚款
    allFines.value = res.data || []
  } finally {
    finesLoading.value = false
  }
}

// 提交修改密码：先校验表单，通过后调接口
async function submitPassword() {
  let valid = false
  try { valid = await pwdFormRef.value.validate() } catch { /* 校验失败不处理 */ }
  if (!valid) return // 表单校验未通过，直接退出

  pwdLoading.value = true
  try {
    await changePassword({ // 调后端修改密码接口
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdDialogVisible.value = false // 关闭弹窗
  } finally {
    pwdLoading.value = false
  }
}

// 批量支付罚款：二次确认 -> 调接口 -> 刷新列表
async function handlePay() {
  if (selectedFines.value.length === 0) return // 未勾选则不处理
  // 二次确认支付金额
  try {
    await ElMessageBox.confirm(`确认支付 ¥${selectedTotal.value.toFixed(2)} ？`, '缴纳罚款')
  } catch { return } // 用户取消

  payLoading.value = true
  try {
    await payFines({ // 调后端批量缴纳接口
      cardNo,
      fineIds: selectedFines.value.map(f => f.fineId), // 提取罚款 ID 列表
    })
    ElMessage.success('支付成功')
    selectedFines.value = [] // 清空勾选
    await loadFines()      // 刷新罚款列表（已缴状态会变）
    await loadHistory()    // 刷新借阅记录（实还日期会更新）
  } finally {
    payLoading.value = false
  }
}

// 页面挂载时并行加载三个模块的数据
onMounted(() => {
  loadProfile()  // 加载个人信息
  loadHistory()  // 加载借阅记录
  loadFines()    // 加载罚款记录
})
</script>

<style scoped>
/* 卡片头部行：标题和按钮左右分布 */
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
/* 卡片标题文字加粗 */
.card-header-title {
  font-size: 15px;
  font-weight: 600;
}
/* 罚款底部操作栏：金额和支付按钮靠右对齐 */
.fine-pay-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}
/* 已选金额文字样式 */
.fine-total {
  font-size: 15px;
  color: var(--color-text-secondary);
}
/* 金额数字红色高亮 */
.fine-total strong {
  color: var(--color-danger);
  font-size: 18px;
}
</style>
