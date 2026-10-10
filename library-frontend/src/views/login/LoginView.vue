<template>
  <!-- 登录页全屏背景容器，居中布局 -->
  <div class="login-page">
    <!-- 登录白色卡片，带阴影圆角 -->
    <div class="login-card">
      <!-- 顶部标题区域：图标 + 中英文标题 -->
      <div class="login-header">
        <h1 class="login-title">📚 图书馆管理系统</h1>
        <p class="login-subtitle">Library Management System</p>
      </div>

      <!-- 管理员/学生登录模式切换标签页 -->
      <el-tabs v-model="activeTab" class="login-tabs" @tab-change="onTabChange">
        <el-tab-pane label="管理员登录" name="admin" />
        <el-tab-pane label="学生登录" name="student" />
      </el-tabs>

      <!-- 登录表单，支持回车提交 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <!-- 用户名/借书证号输入框，标签随角色切换动态变化 -->
        <el-form-item :label="activeTab === 'admin' ? '用户名' : '借书证号'" prop="username">
          <el-input
            v-model="form.username"
            :placeholder="activeTab === 'admin' ? '请输入管理员用户名' : '请输入借书证号'"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <!-- 密码输入框，支持明文切换 -->
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 记住密码复选框 -->
        <el-form-item>
          <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
        </el-form-item>

        <!-- 登录按钮，提交时显示加载状态 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页：管理员用用户名登录，学生用借书证号登录。
 * 切 Tab 会清空表单；勾选"记住密码"会把凭据存 localStorage。
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'
import logger from '@/utils/logger'

// 日志前缀，方便在控制台筛选本组件的日志
const COMPONENT = 'LoginView > '
// 路由实例，用于登录成功后跳转
const router = useRouter()
// 当前路由信息，用于读取 redirect / keyword 查询参数
const route = useRoute()
// 认证状态管理，登录成功后写入用户信息
const authStore = useAuthStore()

// 当前激活的登录模式：'admin' 或 'student'
const activeTab = ref('admin')
// 登录按钮的加载状态，防止重复点击
const loading = ref(false)
// 是否勾选了"记住密码"
const rememberMe = ref(false)
// el-form 组件引用，用于手动触发表单校验和重置
const formRef = ref(null)

// 登录表单数据，包含用户名和密码两个字段
const form = reactive({
  username: '',
  password: '',
})

// 表单校验规则：用户名必填，密码必填且 6-20 位
const rules = {
  username: [{ required: true, message: '请输入用户名/借书证号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' },
  ],
}

// 组件挂载时：如果有 keyword 参数则切到学生模式；同时恢复记住的密码
onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', {})
  // 从搜索页跳转过来时自动切到学生登录模式
  if (route.query.keyword) {
    activeTab.value = 'student'
  }
  // 读取记住的密码
  const saved = localStorage.getItem('rememberedLogin')
  if (saved) {
    try {
      const data = JSON.parse(saved)
      activeTab.value = data.role || 'admin'
      form.username = data.username || ''
      form.password = data.password || ''
      rememberMe.value = true
    } catch { /* ignore */ }
  }
})

// 切换登录角色时重置表单，避免管理员密码填到学生表单里
function onTabChange() {
  logger.operation(`${COMPONENT}onTabChange`, '🔄 切换模式', { role: activeTab.value })
  formRef.value?.resetFields()
}

// 登录主流程：校验表单 -> 调接口 -> 存凭据 -> 跳转首页
async function handleLogin() {
  logger.operation(`${COMPONENT}handleLogin`, '▶ 进入', {
    role: activeTab.value,
    username: activeTab.value === 'admin'
      ? form.username
      : 'cardNo:' + form.username?.substring(0, 4) + '***',
  })

  // 表单校验
  let valid = false
  try {
    valid = await formRef.value.validate()
  } catch (e) {
    logger.validate(`${COMPONENT}handleLogin`, '❌ 校验异常', { error: e.message })
  }
  if (!valid) {
    logger.validate(`${COMPONENT}handleLogin`, '❌ 校验失败')
    return
  }
  logger.validate(`${COMPONENT}handleLogin`, '✅ 校验通过')

  loading.value = true
  try {
    // 调用后端登录接口，传入角色、用户名、密码
    const res = await login({
      role: activeTab.value,
      username: form.username,
      password: form.password,
    })

    // 勾选了记住密码则写入 localStorage，否则清除
    if (rememberMe.value) {
      localStorage.setItem('rememberedLogin', JSON.stringify({
        role: activeTab.value,
        username: form.username,
        password: form.password,
      }))
    } else {
      localStorage.removeItem('rememberedLogin')
    }

    // 把登录响应写入全局认证状态（token、用户信息等）
    authStore.setLogin(res.data)

    logger.operation(`${COMPONENT}handleLogin`, '✅ 成功', {
      role: res.data?.role,
      displayName: res.data?.displayName,
      sno: res.data?.sno,
      cardNo: res.data?.cardNo,
    })
    ElMessage.success('登录成功')

    // 登录成功后跳转：优先回 redirect 来源页，否则按角色进首页
    const redirect = route.query.redirect
    const keyword = route.query.keyword
    if (redirect) {
      // 带 keyword 的跳转需要透传查询参数（比如从搜索页来的）
      router.push(keyword ? { path: redirect, query: { keyword } } : redirect)
    } else if (res.data?.role === 'admin') {
      // 管理员进后台仪表盘
      router.push('/dashboard')
    } else {
      // 学生进前台首页
      router.push('/home')
    }
  } catch (e) {
    logger.error(`${COMPONENT}handleLogin`, '❌ 登录失败', {
      role: activeTab.value,
      username: form.username,
      errorMessage: e.message,
    })
  } finally {
    // 无论成功失败都要关闭加载状态
    loading.value = false
  }
}
</script>

<style scoped>
/* 全屏登录页：背景图 + Flex 居中 */
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-image: url('@/assets/images/login-bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  overflow: hidden;
}

/* 登录卡片：85% 不透明度 */
.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(15, 30, 15, 0.25);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(6px);
}

/* 标题区域居中 + 底部间距 */
.login-header {
  text-align: center;
  margin-bottom: 24px;
}

/* 主标题：图书馆管理系统 */
.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
  margin-bottom: 4px;
}

/* 副标题：英文说明 */
.login-subtitle {
  font-size: 13px;
  color: var(--color-text-secondary);
  letter-spacing: 2px;
}

/* 标签页底部留白 */
.login-tabs {
  margin-bottom: 8px;
}

/* 隐藏标签页底部默认分割线 */
.login-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

/* 登录按钮：全宽 + 字间距 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

/* 底部测试账号提示 */
.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: var(--color-text-secondary);
}
</style>
