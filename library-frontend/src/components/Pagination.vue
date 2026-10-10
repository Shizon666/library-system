<!-- 分页组件：对 el-pagination 的薄封装，total 变化时自动回第一页 -->
<template>
  <!-- total 为 0 时不渲染分页栏 -->
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :background="true"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },                                     // 总条数
  defaultPageSize: { type: Number, default: 10 },                          // 默认每页条数
  pageSizes: { type: Array, default: () => [10, 20, 50, 100] },            // 可选的每页条数列表
  layout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },
})

// 对外只发一个 change 事件，payload: { page, size }
const emit = defineEmits(['change'])

const currentPage = ref(1)                      // 当前页码
const pageSize = ref(props.defaultPageSize)     // 当前每页条数

// 监听 total 变化（如重新搜索），自动回到第一页
watch(() => props.total, () => {
  // 数据总量变化时重置到第一页
  currentPage.value = 1
})

// 每页条数切换，回到第一页并触发 change 事件
function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1   // 切换每页条数后回到首页
  emit('change', { page: currentPage.value, size: pageSize.value })
}

// 页码切换，触发 change 事件
function handleCurrentChange(page) {
  currentPage.value = page
  emit('change', { page: currentPage.value, size: pageSize.value })
}

/** 重置到第一页 */
function reset() {
  currentPage.value = 1
}

// 暴露 reset 方法，供父组件通过 ref 调用
defineExpose({ reset })
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
