<!-- 顶部栏：侧边栏折叠开关 + 用户问候/角色标签 + 登录/退出 -->
<template>
  <div class="topbar">
    <div class="topbar-left">
      <!-- 折叠/展开图标跟随 isCollapse 状态切换 -->
      <el-button text @click="$emit('toggleCollapse')" class="collapse-btn">
        <el-icon :size="20">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
      </el-button>
      <span class="topbar-title">📚 图书馆管理系统</span>
    </div>

    <div class="topbar-right">
      <!-- 登录态：显示欢迎语 + 退出按钮；未登录：显示登录按钮 -->
      <template v-if="isLoggedIn">
        <span class="welcome-text">
          欢迎，{{ authStore.displayName || authStore.username || '用户' }}
          <el-tag size="small" :type="authStore.role === 'admin' ? 'danger' : 'success'" class="role-tag">
            {{ authStore.role === 'admin' ? '管理员' : '学生' }}
          </el-tag>
        </span>
        <el-button type="danger" text @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
      </template>
      <el-button v-else type="primary" @click="goLogin">登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

defineProps({
  isCollapse: { type: Boolean, default: false }, // 当前侧边栏是否折叠，控制图标显示
})

defineEmits(['toggleCollapse']) // 通知父组件切换折叠状态

const authStore = useAuthStore()
const router = useRouter()
const isLoggedIn = computed(() => authStore.isLoggedIn) // 是否已登录，控制右侧区域显示

// 跳转到登录页
function goLogin() {
  router.push('/login')
}

// 退出登录：二次确认后清空状态并跳转首页
function handleLogout() {
  ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    .then(() => {
      authStore.logout()   // 清空 Store 和 localStorage
      router.push('/home') // 退出后回到首页
    })
    .catch(() => {}) // 用户取消，不做任何操作
}
</script>

<style scoped>
/* 顶栏整体：左右两端对齐 */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

/* 左侧区域：折叠按钮 + 标题 */
.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 折叠/展开按钮颜色 */
.collapse-btn {
  color: var(--color-text-secondary);
  font-size: 20px;
}

/* 系统标题样式 */
.topbar-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: 1px;
}

/* 右侧区域：欢迎语 + 退出按钮 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 欢迎文字样式 */
.welcome-text {
  color: var(--color-text-secondary);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 角色标签左间距 */
.role-tag {
  margin-left: 4px;
}

/* 管理员标签（#E60012 明度降低 20%） */
.role-tag.el-tag--danger {
  --el-tag-text-color: #B8000E;
  --el-tag-bg-color: rgba(184, 0, 14, 0.1);
  --el-tag-border-color: rgba(184, 0, 14, 0.25);
  color: #B8000E;
  background-color: rgba(184, 0, 14, 0.1);
  border-color: rgba(184, 0, 14, 0.25);
}

/* 退出按钮 */
.topbar-right :deep(.el-button--danger.is-text) {
  --el-button-text-color: #B8000E;
  --el-button-hover-text-color: #CC1525;
  color: #B8000E;
}

.topbar-right :deep(.el-button--danger.is-text:hover),
.topbar-right :deep(.el-button--danger.is-text:focus) {
  color: #CC1525;
  background-color: rgba(184, 0, 14, 0.08);
}
</style>
