<!-- 通用确认弹窗：支持 warning/danger 两种视觉风格，v-model 控制显隐 -->
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="confirm-body">
      <!-- 根据 type 显示不同图标：warning 黄色三角，danger 红色圆叉 -->
      <el-icon :size="48" :color="'var(--color-warning)'" v-if="type === 'warning'">
        <WarningFilled />
      </el-icon>
      <el-icon :size="48" :color="'var(--color-danger)'" v-else-if="type === 'danger'">
        <CircleCloseFilled />
      </el-icon>
      <p class="confirm-message">{{ message }}</p>
    </div>

    <template #footer>
      <div class="confirm-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button :type="type === 'danger' ? 'danger' : 'primary'" @click="handleConfirm" :loading="loading">
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },   // v-model 控制弹窗显隐
  title: { type: String, default: '确认操作' },      // 弹窗标题
  message: { type: String, default: '确认执行该操作？' }, // 提示文案
  type: { type: String, default: 'warning' },       // 视觉风格：'warning' | 'danger'
  confirmText: { type: String, default: '确认' },    // 确认按钮文字
  loading: { type: Boolean, default: false },       // 确认按钮加载态，防重复点击
  width: { type: String, default: '420px' },
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(props.modelValue) // 内部显隐状态，与外部 v-model 双向同步

// 外部值变化时同步到内部（父组件主动打开/关闭弹窗）
watch(() => props.modelValue, (val) => {
  visible.value = val
})

// 内部值变化时通知父组件（用户点击遮罩关闭等）
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 点击确认按钮，通知父组件执行业务逻辑
function handleConfirm() {
  emit('confirm')
}

// 点击取消按钮，关闭弹窗并通知父组件
function handleCancel() {
  visible.value = false
  emit('cancel')
}

// 弹窗关闭事件（点击遮罩或按 Esc 触发）
function handleClose() {
  emit('cancel')
}
</script>

<style scoped>
.confirm-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  gap: 16px;
}

.confirm-message {
  font-size: 15px;
  color: var(--color-text-primary);
  text-align: center;
  line-height: 1.8;
}

.confirm-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
