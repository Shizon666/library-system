<!-- 通用状态标签：传入 status 码 + statusMap/typeMap 即可渲染，一行搞定 -->
<template>
  <el-tag :type="tagType" :size="size" :effect="effect">
    {{ label }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: [Number, String], required: true }, // 状态码，如 0/1/2
  statusMap: { type: Object, default: () => ({}) },   // { 状态码: 显示文字 } 映射
  typeMap: { type: Object, default: () => ({}) },      // { 状态码: tag类型 } 映射，不传则走默认
  size: { type: String, default: 'default' },
  effect: { type: String, default: 'light' },
})

// 内置默认配色：业务场景通用的颜色约定
const defaultTypeMap = {
  0: 'danger',    // 下架/注销 → 红色
  1: 'success',   // 可借/正常 → 绿色
  2: 'warning',   // 已借出/挂失 → 橙色
  3: 'info',      // 已报废/作废 → 灰色
}

// 根据 status 码从 statusMap 中取显示文字，无匹配则显示 "未知(xxx)"
const label = computed(() => {
  return props.statusMap[props.status] || `未知(${props.status})`
})

// 优先使用外部传入的 typeMap，否则走内置默认配色
const tagType = computed(() => {
  if (props.typeMap[props.status]) return props.typeMap[props.status] // 外部自定义优先
  return defaultTypeMap[props.status] || 'info' // 兜底灰色
})
</script>
