<!-- 主布局壳：左侧可折叠侧边栏 + 右侧顶栏/内容区 -->
<template>
  <el-container class="main-layout">
    <!-- aside 宽度跟随折叠状态动态切换 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <Sidebar :is-collapse="isCollapse" />
    </el-aside>

    <el-container>
      <el-header height="56px" class="layout-header">
        <Topbar @toggle-collapse="isCollapse = !isCollapse" :is-collapse="isCollapse" />
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Topbar from './components/Topbar.vue'
import Sidebar from './components/Sidebar.vue'

const isCollapse = ref(false) // 侧边栏是否折叠，由 Topbar 的按钮切换
</script>

<style scoped>
/* 整体布局容器，占满视口高度，禁止外层滚动 */
.main-layout {
  height: 100vh;
  overflow: hidden;
}

/* 左侧边栏：宽度跟随折叠状态平滑过渡 */
.layout-aside {
  background-color: var(--color-sidebar);
  transition: width 0.3s ease;
  overflow: hidden;
}

/* 顶部栏：白底 + 底部细线 + 微投影 */
.layout-header {
  background-color: var(--color-card-bg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 3px rgba(15, 30, 15, 0.06);
}

/* 主内容区：浅灰背景，独立滚动，高度扣掉顶栏 */
.layout-main {
  background-color: var(--color-bg);
  overflow-y: auto;
  height: calc(100vh - 56px);
}
</style>
