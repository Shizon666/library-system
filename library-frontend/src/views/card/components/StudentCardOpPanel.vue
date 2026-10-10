<!-- 学生信息查询面板：借书证相关操作的公共区域，通过 slot 扩展额外字段和按钮 -->
<template>
  <el-card shadow="never" class="card-wrapper">
    <!-- alertTitle 有值时才显示顶部提示条 -->
    <el-alert
      v-if="alertTitle"
      type="info"
      :closable="false"
      show-icon
      class="mb-16"
      :title="alertTitle"
    />
    <div class="search-bar">
      <el-input
        :model-value="sno"
        placeholder="请输入学号"
        size="large"
        style="width: 320px;"
        @update:model-value="$emit('update:sno', $event)"
        @keyup.enter="$emit('query')"
      />
      <el-button type="primary" size="large" :icon="Search" :loading="loading" @click="$emit('query')">
        查询
      </el-button>
    </div>
    <el-descriptions v-if="studentInfo" :column="2" border class="mt-16">
      <el-descriptions-item label="学号">{{ studentInfo.sno }}</el-descriptions-item>
      <el-descriptions-item label="姓名">
        {{ studentInfo.studentName || '—' }}
        <el-tag v-if="studentInfo.studentType" size="small" type="info" style="margin-left: 6px;">{{ studentInfo.studentType }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="学院">{{ studentInfo.college || '—' }}</el-descriptions-item>
      <el-descriptions-item label="专业">{{ studentInfo.major || '—' }}</el-descriptions-item>
      <el-descriptions-item label="目前借书证号">{{ studentInfo.cardNo || '暂无' }}</el-descriptions-item>
      <el-descriptions-item label="借书证状态">
        <StatusBadge v-if="studentInfo.status != null" :status="studentInfo.status" :status-map="STATUS_MAP" />
        <span v-else>暂无</span>
      </el-descriptions-item>
      <el-descriptions-item label="毕业状态">
        <el-tag :type="studentInfo.graduated === 1 ? 'success' : 'warning'" size="small">
          {{ studentInfo.graduated === 1 ? '已毕业' : '未毕业' }}
        </el-tag>
      </el-descriptions-item>
      <!-- extra-desc 插槽：调用方按需追加描述项，作用域传出 studentInfo -->
      <slot name="extra-desc" :student-info="studentInfo" />
    </el-descriptions>
    <!-- actions 插槽：调用方放入操作按钮（办证/挂失/注销等） -->
    <div class="action-bar mt-16">
      <slot name="actions" />
    </div>
  </el-card>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'
import StatusBadge from '@/components/StatusBadge.vue'

// 借书证状态映射：0=注销 1=正常 2=挂失
const STATUS_MAP = { 0: '注销', 1: '正常', 2: '挂失' }

// 组件属性定义
defineProps({
  sno: { type: String, default: '' },           // 学号输入值（支持 v-model:sno 双向绑定）
  loading: { type: Boolean, default: false },    // 查询按钮加载态，防止重复点击
  studentInfo: { type: Object, default: null },  // 查询到的学生信息对象，null 时不渲染详情区域
  alertTitle: { type: String, default: '' },     // 顶部提示条文案，空字符串则不显示
})

// 支持 v-model:sno 双向绑定和查询按钮点击事件
defineEmits(['update:sno', 'query'])
</script>

<style scoped>
.mb-16 {
  margin-bottom: 16px;
}
</style>
