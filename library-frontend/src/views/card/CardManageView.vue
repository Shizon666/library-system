<template>
  <!-- 整页容器 -->
      <div class="page-container card-manage-page">
    <!-- 管理员才可见整个页面，否则显示无权限提示 -->
    <template v-if="isAdmin">
      <h2 class="page-title">💳 借书证管理</h2>

      <!-- 四个 Tab 切换：新办 / 挂失 / 补办 / 注销 -->
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ======================== Tab 1：新办借书证 ======================== -->
        <el-tab-pane label="新办" name="create">
          <!-- 公共操作面板：绑定学号、加载态、学生信息，派发查询事件 -->
          <StudentCardOpPanel
            v-model:sno="opSno"
            :loading="queryLoading"
            :student-info="studentInfo"
            alert-title="证号规则：学号 + 三位序号。首次新办为 001；原证挂失后可再次新办，序号依次递增（如 2021101001001 → 2021101001002）。"
            @query="queryStudent"
          >
            <!-- 预览即将生成的证号，仅在可新办时展示 -->
            <template #extra-desc="{ studentInfo: info }">
              <!-- 有预览证号且满足新办条件时显示 -->
              <el-descriptions-item v-if="info.previewCardNo && canCreate" label="将生成证号">
                {{ info.previewCardNo }}
              </el-descriptions-item>
            </template>
            <!-- 新办操作按钮组 -->
            <template #actions>
              <!-- 确认新办：受 canCreate 控制是否可点击 -->
              <el-button type="primary" size="large" :loading="actionLoading" :disabled="!canCreate" @click="handleCreate">确认新办</el-button>
              <!-- 重置按钮：清空当前输入和查询结果 -->
              <el-button size="large" @click="resetOpPanel">重置</el-button>
            </template>
          </StudentCardOpPanel>
        </el-tab-pane>

        <!-- ======================== Tab 2：挂失借书证 ======================== -->
        <el-tab-pane label="挂失" name="loss">
          <!-- 挂失面板：复用同一组件，无额外提示文本 -->
          <StudentCardOpPanel
            v-model:sno="opSno"
            :loading="queryLoading"
            :student-info="studentInfo"
            @query="queryStudent"
          >
            <!-- 挂失操作按钮组 -->
            <template #actions>
              <!-- 确认挂失：仅"正常"状态可挂失 -->
              <el-button type="warning" size="large" :loading="actionLoading" :disabled="!canLoss" @click="handleLoss">确认挂失</el-button>
              <!-- 重置按钮 -->
              <el-button size="large" @click="resetOpPanel">重置</el-button>
            </template>
          </StudentCardOpPanel>
        </el-tab-pane>

        <!-- ======================== Tab 3：补办借书证 ======================== -->
        <el-tab-pane label="补办" name="replace">
          <!-- 补办面板：提示仅挂失/注销状态可补办 -->
          <StudentCardOpPanel
            v-model:sno="opSno"
            :loading="queryLoading"
            :student-info="studentInfo"
            alert-title="仅「挂失」或「注销」状态的借书证可补办，新证号在原证号末三位基础上 +1。"
            @query="queryStudent"
          >
            <!-- 预览补办后的新证号 -->
            <template #extra-desc="{ studentInfo: info }">
              <!-- 有预览证号且满足补办条件时显示新证号 -->
              <el-descriptions-item v-if="info.previewCardNo && canReplace" label="新证号">
                {{ info.previewCardNo }}
              </el-descriptions-item>
            </template>
            <!-- 补办操作按钮组 -->
            <template #actions>
              <!-- 确认补办：受 canReplace 控制 -->
              <el-button type="primary" size="large" :loading="actionLoading" :disabled="!canReplace" @click="handleReplace">确认补办</el-button>
              <!-- 重置按钮 -->
              <el-button size="large" @click="resetOpPanel">重置</el-button>
            </template>
          </StudentCardOpPanel>
        </el-tab-pane>

        <!-- ======================== Tab 4：注销借书证 ======================== -->
        <el-tab-pane label="注销" name="cancel">
          <!-- 注销面板：无额外提示，依赖按钮禁用态和弹窗提示 -->
          <StudentCardOpPanel
            v-model:sno="opSno"
            :loading="queryLoading"
            :student-info="studentInfo"
            @query="queryStudent"
          >
            <!-- 注销操作按钮组 -->
            <template #actions>
              <!-- 确认注销：需已毕业且未注销才可用 -->
              <el-button type="danger" size="large" :loading="actionLoading" :disabled="!canCancel" @click="handleCancel">确认注销</el-button>
              <!-- 重置按钮 -->
              <el-button size="large" @click="resetOpPanel">重置</el-button>
            </template>
          </StudentCardOpPanel>
        </el-tab-pane>
      </el-tabs>

      <!-- ======================== 底部：操作记录查询卡片 ======================== -->
      <el-card shadow="never" class="card-wrapper mt-16">
        <!-- 卡片标题 -->
        <template #header>
          <span class="card-header-title">📜 操作记录查询</span>
        </template>
        <!-- 学号输入框 + 查询按钮 -->
        <div class="search-bar">
          <!-- 学号输入：绑定 snoQuery -->
          <el-input v-model="snoQuery" placeholder="请输入学号" size="large" style="width: 280px;" />
          <!-- 查询按钮：点击触发 queryRecords -->
          <el-button type="primary" size="large" :icon="Search" @click="queryRecords" :loading="recordsLoading">查询</el-button>
        </div>
        <!-- 操作记录表格：展示序号、学号、原证号、新证号、操作类型 -->
        <el-table :data="records" v-loading="recordsLoading" stripe size="default" empty-text="请输入学号查询">
          <!-- 序号列 -->
          <el-table-column prop="serNum" label="序号" width="80" />
          <!-- 学号列 -->
          <el-table-column prop="sno" label="学号" width="130" />
          <!-- 原证号列 -->
          <el-table-column prop="originCardNo" label="原证号" width="160" />
          <!-- 新证号列 -->
          <el-table-column prop="newCardNo" label="新证号" width="160" />
          <!-- 操作类型列：用 Tag 颜色区分新办/挂失/补办/注销 -->
          <el-table-column label="操作类型" width="100">
            <!-- 自定义列内容：根据 opType 渲染彩色 Tag -->
            <template #default="{ row }">
              <el-tag :type="opTypeColor(row.opType)">{{ opTypeMap[row.opType] || row.opType || '未知' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- ======================== 非管理员：无权限提示 ======================== -->
    <template v-else>
      <!-- 无权限结果页：提示仅管理员可操作 -->
      <el-result icon="warning" title="无权限访问" sub-title="借书证管理仅限管理员操作，请联系管理员。">
        <!-- 底部操作区：返回首页按钮 -->
        <template #extra>
          <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>

<script setup>
/**
 * 借书证管理页面，四个 Tab 对应证件生命周期的四种操作：
 *   新办 — 首次或挂失后再次申领（证号 = 学号 + 递增序号）
 *   挂失 — 仅"正常"状态可挂失
 *   补办 — "挂失"或"注销"状态可补办，新证号序号 +1
 *   注销 — 需学生已毕业，注销后永久不可用
 * 底部可按学号查询历史操作记录。
 */
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCard,
  queryStudentCard,
  reportLoss,
  replaceCard,
  deleteCard,
  getCardRecords,
} from '@/api/modules/cards'
import StudentCardOpPanel from './components/StudentCardOpPanel.vue'
import logger from '@/utils/logger'

// 日志前缀，方便在控制台按前缀过滤本组件日志
const COMPONENT = 'CardManageView > '

// 获取全局认证 store，用于判断当前用户角色
const authStore = useAuthStore()

// 计算属性：当前用户是否为管理员
const isAdmin = computed(() => authStore.role === 'admin')

// 当前激活的 Tab 名称，默认进入"新办"
const activeTab = ref('create')

// 学号正则：4位入学年 + 1位(1本科/2研究生) + 5位序号，共10位
const SNO_PATTERN = /^\d{4}[12]\d{5}$/

// 用户在操作面板输入的学号
const opSno = ref('')

// 查询返回的学生+证件信息，包含 status/graduated/canCreate 等字段
const studentInfo = ref(null)

// 查询学生信息接口的加载态
const queryLoading = ref(false)

// 执行业务操作（新办/挂失/补办/注销）时的加载态
const actionLoading = ref(false)

// ---------- 四个操作按钮的启用条件，全部由后端返回字段推导 ----------

// 后端判断是否还能新办（序号是否还有余量）
const canCreate = computed(() => studentInfo.value?.canCreate === true)

// 仅"正常"状态(1)的证可挂失
const canLoss = computed(() => studentInfo.value?.status === 1)

// "注销"(0)或"挂失"(2)状态可补办
const canReplace = computed(() => studentInfo.value?.status === 0 || studentInfo.value?.status === 2)

// 已毕业(graduated===1)且当前证未注销(status!==0)才可注销
const canCancel = computed(() => studentInfo.value?.cardNo && studentInfo.value?.status !== 0 && studentInfo.value?.graduated === 1)

// ---------- 操作记录表格相关 ----------

// 操作类型映射表：数字 -> 中文名称
const opTypeMap = { 1: '新办', 2: '挂失', 3: '补办', 4: '注销' }

// 根据操作类型返回对应的 Tag 颜色，未知类型返回 info
function opTypeColor(type) {
  return { 1: 'success', 2: 'warning', 3: 'primary', 4: 'danger' }[type] || 'info'
}

// 底部记录查询的学号输入值
const snoQuery = ref('')

// 查询到的操作记录列表
const records = ref([])

// 记录查询接口的加载态
const recordsLoading = ref(false)

// ---------- Tab 切换 ----------

// Tab 切换时重置操作面板，避免残留上一个 Tab 的数据
function onTabChange() {
  resetOpPanel()
}

// ---------- 操作面板重置 ----------

// 清空操作面板：学号输入和查询结果归零
function resetOpPanel() {
  opSno.value = ''
  studentInfo.value = null
}

// ---------- 查询学生信息 ----------

// 根据学号查询学生+证件信息，校验格式后调接口
async function queryStudent() {
  // 去除首尾空格
  const sno = opSno.value.trim()
  // 前端校验：学号不能为空
  if (!sno) {
    ElMessage.warning('请输入学号')
    return
  }
  // 前端校验：必须符合10位学号格式
  if (!SNO_PATTERN.test(sno)) {
    ElMessage.warning('学号格式：10位（如 2021101001）')
    return
  }

  queryLoading.value = true
  // 先清空旧数据，防止闪烁
  studentInfo.value = null
  try {
    // 调后端查询接口获取学生+证件信息
    const res = await queryStudentCard(sno)
    studentInfo.value = res.data
    // 查询成功后校验当前 Tab 的操作条件是否满足
    validateQueryResult()
  } catch {
    // 接口异常则清空结果
    studentInfo.value = null
  } finally {
    queryLoading.value = false
  }
}

// ---------- 查询结果校验 ----------

// 查询学生信息后，根据当前 Tab 校验状态是否允许操作，不符合则弹提示
function validateQueryResult() {
  const info = studentInfo.value
  // 没查到数据就跳过校验
  if (!info) return

  // 每个 Tab 有独立的校验逻辑，不符合条件时给出明确提示
  const validators = {
    // 新办校验：正常使用中提示先挂失；已有证且不可新办也提示
    create: () => {
      if (info.status === 1) {
        return ElMessageBox.alert('借书证号正常使用，如需新办，请先挂失', '提示', { type: 'warning' })
      }
      if (!info.canCreate && info.cardNo) {
        ElMessage.warning('该学号已有借书证记录，请先挂失原证后再新办')
      }
    },
    // 挂失校验：无证 / 已挂失 / 已注销 / 其他不可挂失状态
    loss: () => {
      if (!info.cardNo) ElMessage.warning('该学号暂无借书证')
      else if (info.status === 2) ElMessage.warning('该借书证已挂失，无需重复操作')
      else if (info.status === 0) ElMessage.warning('该借书证已注销，无法挂失')
      else if (info.status !== 1) ElMessage.warning('当前借书证状态不可挂失')
    },
    // 补办校验：无证 / 正常使用中需先挂失 / 其他不可补办状态
    replace: () => {
      if (!info.cardNo) ElMessage.warning('该学号暂无借书证')
      else if (info.status === 1) ElMessage.warning('借书证正常使用中，如需补办，请先挂失')
      else if (info.status !== 0 && info.status !== 2) ElMessage.warning('仅「挂失」或「注销」状态可补办')
    },
    // 注销校验：无证 / 已注销 / 未毕业
    cancel: () => {
      if (!info.cardNo) ElMessage.warning('该学号暂无借书证')
      else if (info.status === 0) ElMessage.warning('该借书证已注销，无需重复操作')
      else if (info.graduated !== 1) ElMessage.warning('该学生尚未毕业，无法注销借书证')
    },
  }

  // 根据当前 Tab 名称调用对应的校验函数
  validators[activeTab.value]?.()
}

// ---------- 新办借书证 ----------

// 点击"确认新办"：前置条件检查后调后端接口
async function handleCreate() {
  // 前置条件：canCreate 为 true 且有学生信息
  if (!canCreate.value || !studentInfo.value) return
  actionLoading.value = true
  try {
    // 调后端新办接口，传入学号和姓名
    const res = await createCard({
      sno: studentInfo.value.sno,
      studentName: studentInfo.value.studentName,
    })
    // 取生成的证号：优先用接口返回值，其次用前端预览值
    const generatedCardNo = res.data?.cardNo || studentInfo.value.previewCardNo || ''
    ElMessage.success(`新办成功！借书证号：${generatedCardNo}`)
    // 成功后清空面板，准备下一次操作
    resetOpPanel()
  } catch (e) {
    // 记录错误日志，便于排查
    logger.error(`${COMPONENT}handleCreate`, '新办失败', { sno: studentInfo.value?.sno })
  } finally {
    actionLoading.value = false
  }
}

// ---------- 挂失借书证 ----------

// 点击"确认挂失"：二次确认后调后端挂失接口
async function handleLoss() {
  // 前置条件：canLoss 为 true 且有证号
  if (!canLoss.value || !studentInfo.value?.cardNo) return
  // 弹出二次确认对话框
  try {
    await ElMessageBox.confirm('确认挂失该借书证？挂失后可至「新办」或「补办」领取新证号。')
  } catch { return } // 用户点取消则直接退出
  actionLoading.value = true
  try {
    // 调后端挂失接口
    await reportLoss(studentInfo.value.cardNo)
    ElMessage.success('挂失成功')
    resetOpPanel()
  } catch (e) {
    logger.error(`${COMPONENT}handleLoss`, '挂失失败')
  } finally {
    actionLoading.value = false
  }
}

// ---------- 补办借书证 ----------

// 点击"确认补办"：二次确认（展示新证号）后调后端补办接口
async function handleReplace() {
  // 前置条件：canReplace 为 true 且有证号
  if (!canReplace.value || !studentInfo.value?.cardNo) return
  // 取前端预览的新证号用于确认弹窗展示
  const newCardNo = studentInfo.value.previewCardNo || ''
  // 二次确认：告知用户原证将作废
  try {
    await ElMessageBox.confirm(`确认补办？原证作废，新证号：${newCardNo}`)
  } catch { return }
  actionLoading.value = true
  try {
    // 调后端补办接口
    const res = await replaceCard(studentInfo.value.cardNo, {})
    // 取实际生成的证号：优先接口返回值，兜底用预览值
    const generated = res.data?.cardNo || res.data?.newCardNo || newCardNo
    ElMessage.success(`补办成功！新证号：${generated}`)
    resetOpPanel()
  } catch (e) {
    logger.error(`${COMPONENT}handleReplace`, '补办失败')
  } finally {
    actionLoading.value = false
  }
}

// ---------- 注销借书证 ----------

// 点击"确认注销"：二次确认后调后端注销接口，注销后不可恢复
async function handleCancel() {
  // 前置条件：canCancel 为 true 且有证号
  if (!canCancel.value || !studentInfo.value?.cardNo) return
  // 二次确认：强调注销后永久不可用
  try {
    await ElMessageBox.confirm('确认注销该借书证？注销后该证将永久不可使用')
  } catch { return }
  actionLoading.value = true
  try {
    // 调后端注销接口
    await deleteCard(studentInfo.value.cardNo)
    ElMessage.success('注销成功')
    resetOpPanel()
  } catch (e) {
    logger.error(`${COMPONENT}handleCancel`, '注销失败')
  } finally {
    actionLoading.value = false
  }
}

// ---------- 操作记录查询 ----------

// 按学号查询底部操作记录列表
async function queryRecords() {
  // 学号不能为空
  if (!snoQuery.value) { ElMessage.warning('请输入学号'); return }
  recordsLoading.value = true
  try {
    // 调后端记录查询接口
    const res = await getCardRecords(snoQuery.value)
    records.value = res.data || []
  } finally {
    recordsLoading.value = false
  }
}

// ---------- 生命周期 ----------

// 页面挂载时检查权限，非管理员弹出错误提示
onMounted(() => {
  if (!isAdmin.value) {
    ElMessage.error('无权限访问该页面')
  }
})
</script>

<style scoped>
/* 卡片头部标题样式 */
.card-header-title {
  font-size: 15px;
  font-weight: 600;
}

/* 借书证管理：主操作按钮统一 #5E7D67 */
.card-manage-page :deep(.el-button--primary:not(.is-link):not(.is-text)),
.card-manage-page :deep(.el-button--warning:not(.is-link):not(.is-text)),
.card-manage-page :deep(.el-button--danger:not(.is-link):not(.is-text)) {
  background-color: #5E7D67 !important;
  border-color: #5E7D67 !important;
  color: #FFFFFF !important;
}

.card-manage-page :deep(.el-button--primary:not(.is-link):not(.is-text):hover),
.card-manage-page :deep(.el-button--primary:not(.is-link):not(.is-text):focus),
.card-manage-page :deep(.el-button--warning:not(.is-link):not(.is-text):hover),
.card-manage-page :deep(.el-button--warning:not(.is-link):not(.is-text):focus),
.card-manage-page :deep(.el-button--danger:not(.is-link):not(.is-text):hover),
.card-manage-page :deep(.el-button--danger:not(.is-link):not(.is-text):focus) {
  background-color: #8EA295 !important;
  border-color: #8EA295 !important;
  color: #FFFFFF !important;
}

.card-manage-page :deep(.el-button--primary:not(.is-link):not(.is-text):active),
.card-manage-page :deep(.el-button--warning:not(.is-link):not(.is-text):active),
.card-manage-page :deep(.el-button--danger:not(.is-link):not(.is-text):active) {
  background-color: #4B6452 !important;
  border-color: #4B6452 !important;
}
</style>
