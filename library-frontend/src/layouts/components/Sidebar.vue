<!-- 侧边栏导航：按角色控制菜单项可见性（公共/学生/管理员） -->
<template>
  <div class="sidebar">
    <!-- 展开态显示完整 logo 文字，折叠态只显示图标 -->
    <div class="sidebar-logo" v-if="!isCollapse">
      <span class="logo-text">📚 图书馆</span>
    </div>
    <div class="sidebar-logo sidebar-logo-collapsed" v-else>
      <span class="logo-icon">📚</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :collapse-transition="false"
      background-color="var(--color-sidebar)"
      text-color="var(--color-sidebar-text)"
      active-text-color="var(--color-sidebar-text-active)"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/home">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页</template>
      </el-menu-item>

      <el-menu-item index="/borrow">
        <el-icon><Reading /></el-icon>
        <template #title>借书</template>
      </el-menu-item>

      <el-menu-item index="/return">
        <el-icon><Finished /></el-icon>
        <template #title>还书</template>
      </el-menu-item>

      <!-- 以下菜单项仅登录后可见，再按角色细分 -->
      <template v-if="isLoggedIn">
        <!-- 图书检索：学生和管理员都可访问 -->
        <el-menu-item index="/books/search" v-if="isStudent || isAdmin">
          <el-icon><Search /></el-icon>
          <template #title>图书检索</template>
        </el-menu-item>

        <!-- 个人信息：仅学生可见，查看在借/历史/罚款 -->
        <el-menu-item index="/personal" v-if="isStudent">
          <el-icon><User /></el-icon>
          <template #title>个人信息</template>
        </el-menu-item>

        <!-- 借书证管理：仅管理员可见，办证/挂失/补办/注销 -->
        <el-menu-item index="/cards" v-if="isAdmin">
          <el-icon><CreditCard /></el-icon>
          <template #title>借书证管理</template>
        </el-menu-item>

        <!-- 编册入库：仅管理员可见，书目 CRUD + 副本管理 -->
        <el-menu-item index="/books/catalog" v-if="isAdmin">
          <el-icon><EditPen /></el-icon>
          <template #title>编册入库</template>
        </el-menu-item>

        <!-- 公告管理：仅管理员可见 -->
        <el-menu-item index="/notices/manage" v-if="isAdmin">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>

        <!-- 罚款管理：仅管理员可见，查询和收取罚款 -->
        <el-menu-item index="/fines" v-if="isAdmin">
          <el-icon><Money /></el-icon>
          <template #title>罚款管理</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineProps({
  isCollapse: { type: Boolean, default: false },
})

const route = useRoute()
const authStore = useAuthStore()
const activeMenu = computed(() => route.path)              // 当前路由路径，用于高亮菜单项
const isLoggedIn = computed(() => authStore.isLoggedIn)    // 是否已登录，控制菜单显隐
const isAdmin = computed(() => authStore.role === 'admin') // 是否管理员角色
const isStudent = computed(() => authStore.role === 'student') // 是否学生角色
</script>

<style scoped>
/* 侧边栏整体：纵向排列，撑满容器高度 */
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 展开态 logo 区域 */
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(168, 199, 159, 0.25);
}

/* 折叠态 logo 区域（与展开态样式相同） */
.sidebar-logo-collapsed {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(168, 199, 159, 0.25);
}

/* 展开态 logo 文字样式 */
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-sidebar-logo);
  letter-spacing: 2px;
}

/* 折叠态 logo 图标大小 */
.logo-icon {
  font-size: 24px;
}

/* 菜单容器：去掉右侧边框，占满剩余空间并可滚动 */
.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

/* 菜单项统一样式：圆角卡片风格 */
.sidebar-menu .el-menu-item {
  font-size: 14px;
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 8px;
}

/* 菜单项 hover 高亮 */
.sidebar-menu .el-menu-item:hover {
  background-color: rgba(168, 199, 159, 0.18) !important;
}

/* 当前激活菜单项高亮色（略暗于主色 #007b43） */
.sidebar-menu .el-menu-item.is-active {
  background-color: var(--color-sidebar-active) !important;
}
</style>
