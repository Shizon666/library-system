# 图书馆管理系统前端代码生成提示词（Vue 3 版）

---

## 1. 角色定义（Role Definition）

你是一名资深前端工程师，精通 Vue 3、Vite、Vue Router、Pinia、Axios 和 Element Plus，具备良好的 UI/UX 设计能力和前后端联调经验。你的任务是根据《图书馆管理系统项目详细设计文档》及实际后端接口，生成一套完整、规范、可运行的 Vue 3 前端项目代码，覆盖系统全部 9 个页面及配套的路由、状态管理、API 封装和公共组件。

---

## 2. 任务目标（Task Objective）

本次任务为图书馆管理系统前端项目的**完整代码生成**。目标是根据设计文档和实际后端接口，生成基于 Vue 3 + Vite + Vue Router + Pinia + Axios 的前端项目源码。全部为 `.vue` 单文件组件（SFC），采用 Composition API + `<script setup>` 语法。

**生成文件清单（共 30+ 个文件）：**

### 第一阶段：项目脚手架与基础设施

| 文件路径 | 说明 |
| -------- | ---- |
| `package.json` | 项目依赖（vue3, vue-router, pinia, axios, element-plus, @element-plus/icons-vue） |
| `vite.config.js` | Vite 构建配置（代理 /api 到后端） |
| `index.html` | 入口 HTML |
| `src/main.js` | 应用入口（挂载 App、Router、Pinia、Element Plus） |
| `src/App.vue` | 根组件（含路由视图） |
| `src/router/index.js` | 路由配置（9 条路由 + 导航守卫） |
| `src/stores/auth.js` | 用户认证状态管理（Pinia） |
| `src/api/request.js` | Axios 实例与请求/响应拦截器 |
| `src/api/modules/auth.js` | 登录 API 模块 |
| `src/api/modules/books.js` | 图书相关 API 模块 |
| `src/api/modules/borrow.js` | 借还书 API 模块 |
| `src/api/modules/cards.js` | 借书证 API 模块 |
| `src/api/modules/fines.js` | 罚款 API 模块 |
| `src/api/modules/dashboard.js` | 首页统计 API 模块 |
| `src/api/modules/me.js` | 个人查询 API 模块 |
| `src/utils/logger.js` | **统一日志工具（操作跟踪、性能记录、错误快照）** |
| `src/assets/styles/variables.css` | CSS 变量与全局配色 |
| `src/assets/styles/common.css` | 全局公共样式 |
| `src/assets/styles/table.css` | 表格样式覆盖 |

### 第二阶段：布局与公共组件

| 文件路径 | 说明 |
| -------- | ---- |
| `src/layouts/MainLayout.vue` | 后台主布局（顶部导航 + 侧边栏 + 内容区） |
| `src/layouts/components/Topbar.vue` | 顶部导航栏组件 |
| `src/layouts/components/Sidebar.vue` | 侧边栏菜单组件 |
| `src/components/StatusBadge.vue` | 状态标签组件（通用） |
| `src/components/ConfirmDialog.vue` | 确认弹窗组件 |
| `src/components/Pagination.vue` | 分页组件 |

### 第三阶段：页面视图（9 个）

| 文件路径 | 说明 |
| -------- | ---- |
| `src/views/login/LoginView.vue` | 管理员/学生登录页 |
| `src/views/dashboard/DashboardView.vue` | 首页（数据统计 + 排行榜） |
| `src/views/book/BookSearchView.vue` | 图书检索页 |
| `src/views/borrow/BorrowView.vue` | 借书操作页 |
| `src/views/return/ReturnView.vue` | 还书操作页 |
| `src/views/card/CardManageView.vue` | 借书证管理页 |
| `src/views/book/BookEditView.vue` | 图书采编页 |
| `src/views/fine/FineManageView.vue` | 罚款管理页 |
| `src/views/personal/PersonalView.vue` | 个人查询页 |

---

## 3. 核心约束（Core Constraints）

### 3.1 技术栈约束

| 技术 | 用途 | 版本要求 |
| ---- | ---- | -------- |
| **Vue 3** | 前端框架（Composition API + `<script setup>`） | ^3.4 |
| **Vite** | 构建工具 | ^5.0 |
| **Vue Router** | 前端路由（hash 模式） | ^4.3 |
| **Pinia** | 状态管理 | ^2.1 |
| **Axios** | HTTP 请求封装 | ^1.7 |
| **Element Plus** | UI 组件库 | ^2.7 |
| **@element-plus/icons-vue** | 图标库 | ^2.3 |
| **ESLint + Prettier** | 代码规范 | 最新稳定版 |

**不允许使用：** Vue 2、React、jQuery、TypeScript（保持 JS）、Less/Sass（使用原生 CSS + CSS 变量）。

### 3.2 哑页面原则（与后端职责分离）

- **纯展示与交互**：页面只负责数据渲染和用户交互，不包含业务规则逻辑
- **不直接操作数据库**：所有数据通过调用 REST API 获取和提交
- **不实现业务校验**：借书上限、证件状态、罚款计算等业务规则由后端 Service 层处理
- **仅做前端格式校验**：如手机号格式、必填项检查、密码长度等（使用 Element Plus 表单校验规则）

### 3.3 质量标准

- **Vue SFC 规范**：每个 `.vue` 文件包含 `<template>`、`<script setup>`、`<style scoped>` 三部分
- **命名规范**：组件 PascalCase，视图以 `View` 结尾，API 模块 camelCase
- **注释规范**：每个 API 函数必须有 JSDoc 注释，关键逻辑行必须有行内注释
- **统一错误处理**：Axios 拦截器统一处理错误，组件内通过 `ElMessage` 提示
- **响应式设计**：页面在 1024px~1920px 宽度下均能正常显示
- **路由守卫**：未登录用户自动跳转登录页
- **基于角色（Role-Based）的访问控制**：
  - 系统分 **管理员（admin）** 和 **学生（student）** 两种角色
  - 借书证管理、图书采编等敏感操作页面仅对管理员开放，学生访问时无权限提示
  - 侧边栏菜单根据当前用户角色动态渲染，学生看不到管理员专属菜单项
  - 路由导航守卫中增加角色校验，非授权角色访问时重定向到首页并提示
  - 页面组件内部增加 `v-if` 角色判断，防止直接 URL 访问时展示空白内容

### 3.4 性能要求

- 组件按需加载（`() => import()` 懒加载）
- Element Plus 按需导入（使用 `unplugin-vue-components` 和 `unplugin-auto-import`）
- 表格数据量较大时考虑使用 `el-pagination` 分页
- 图标使用 Element Plus 图标库，避免外部图片依赖

---

## 4. 项目脚手架搭建

### 4.1 初始化命令

```bash
npm create vite@latest library-frontend -- --template vue
cd library-frontend
npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue
npm install -D unplugin-vue-components unplugin-auto-import
```

### 4.2 完整目录结构

```
library-frontend/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── main.js                        # 应用入口
│   ├── App.vue                        # 根组件
│   ├── router/
│   │   └── index.js                   # 路由配置 + 导航守卫
│   ├── stores/
│   │   └── auth.js                    # 用户认证状态（Pinia）
│   ├── api/
│   │   ├── request.js                 # Axios 实例 + 拦截器
│   │   └── modules/                   # 按业务模块拆分
│   │       ├── auth.js
│   │       ├── books.js
│   │       ├── borrow.js
│   │       ├── cards.js
│   │       ├── fines.js
│   │       ├── dashboard.js
│   │       └── me.js
│   ├── layouts/
│   │   ├── MainLayout.vue             # 主布局容器
│   │   └── components/
│   │       ├── Topbar.vue             # 顶部导航
│   │       └── Sidebar.vue            # 侧边栏
│   ├── components/
│   │   ├── StatusBadge.vue            # 状态标签
│   │   ├── ConfirmDialog.vue          # 确认弹窗
│   │   └── Pagination.vue             # 分页组件
│   ├── views/
│   │   ├── login/
│   │   │   └── LoginView.vue          # 登录页
│   │   ├── dashboard/
│   │   │   └── DashboardView.vue      # 首页
│   │   ├── book/
│   │   │   ├── BookSearchView.vue     # 图书检索
│   │   │   └── BookEditView.vue       # 图书采编
│   │   ├── borrow/
│   │   │   └── BorrowView.vue         # 借书
│   │   ├── return/
│   │   │   └── ReturnView.vue         # 还书
│   │   ├── card/
│   │   │   └── CardManageView.vue     # 借书证管理
│   │   ├── fine/
│   │   │   └── FineManageView.vue     # 罚款管理
│   │   └── personal/
│   │       └── PersonalView.vue       # 个人查询
│   ├── utils/
│   │   └── logger.js                 # 统一日志工具
│   └── assets/
│       └── styles/
│           ├── variables.css          # CSS 变量
│           ├── common.css             # 全局样式
│           └── table.css              # 表格样式
```

---

## 5. 第一阶段：基础设施层详细规范

### 5.1 Vite 配置 — `vite.config.js`

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({ resolvers: [ElementPlusResolver()] }),
    Components({ resolvers: [ElementPlusResolver()] }),
  ],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

### 5.2 CSS 变量 — `src/assets/styles/variables.css`

```css
:root {
  /* 配色方案 */
  --color-primary: #1E3A8A;
  --color-primary-light: #2563EB;
  --color-bg: #F3F4F6;
  --color-sidebar: #1E293B;
  --color-card-bg: #FFFFFF;
  --color-text-primary: #111827;
  --color-text-secondary: #6B7280;
  --color-border: #E5E7EB;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #EF4444;
  --color-info: #3B82F6;
  /* 布局尺寸 */
  --sidebar-width: 220px;
  --topbar-height: 56px;
}
```

### 5.3 Axios 请求封装 — `src/api/request.js`

**核心要求（基于后端实际情况）：**

- 创建 Axios 实例，`baseURL` 为 `''`（通过 Vite proxy 转发 `/api`）
- **后端无 JWT Token 机制**，请求拦截器无需注入 Authorization 头（后端已配置 CORS 允许跨域）
- 响应拦截器：
  - 成功时解包 `response.data`，返回 `{ code, message, data }` 结构
  - 业务错误（`code !== 0`）时通过 `ElMessage.error` 提示后端返回的 message
  - 网络错误时提示"网络连接失败，请检查网络"
- 导出 `get`、`post`、`put`、`del` 四个便捷方法

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import logger from '@/utils/logger'

const service = axios.create({
  baseURL: '',
  timeout: 15000,
})

// 请求拦截器：仅记录日志，后端无 JWT 无需注入 token
service.interceptors.request.use(
  (config) => {
    const source = `${config.method?.toUpperCase()} ${config.url}`
    const detail = {
      url: config.url,
      method: config.method?.toUpperCase(),
      params: config.params ? { ...config.params, password: undefined } : undefined,
      data: config.data ? trySafeStringify(config.data) : undefined,
    }
    logger.network(`Axios > ${source}`, '📤 请求', detail)
    config._reqTime = Date.now()
    return config
  },
  (error) => {
    logger.error('Axios > 请求拦截器', '❌ 失败', { message: error.message })
    return Promise.reject(error)
  }
)

// 响应拦截器：后端返回统一格式 Result<T> = { code, message, data }
service.interceptors.response.use(
  (response) => {
    const elapsed = Date.now() - (response.config?._reqTime || Date.now())
    const res = response.data
    const source = `${response.config?.method?.toUpperCase()} ${response.config?.url}`

    logger.network(`Axios > ${source}`, `📥 响应 ⏱${elapsed}ms`, {
      status: response.status,
      code: res?.code,
      message: res?.message,
      dataSize: res?.data ? `${JSON.stringify(res.data).length}B` : '0B',
    })

    if (res.code !== 0) {
      logger.error(`Axios > ${source}`, '❌ 业务错误', { code: res.code, message: res.message })
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message))
    }

    logger.network(`Axios > ${source}`, '✅ 成功', {
      code: res.code,
      dataSummary: res.data ? trySummarize(res.data) : null,
    })
    return res
  },
  (error) => {
    const elapsed = error.config?._reqTime ? Date.now() - error.config._reqTime : -1
    const source = `${error.config?.method?.toUpperCase()} ${error.config?.url}`

    if (error.response) {
      logger.error(`Axios > ${source}`, `❌ HTTP ${error.response.status}`, {
        status: error.response.status,
        data: error.response.data,
        elapsed: `${elapsed}ms`,
      })
      ElMessage.error(error.response.data?.message || `请求失败 (${error.response.status})`)
    } else if (error.request) {
      logger.error(`Axios > ${source}`, '❌ 网络无响应', { elapsed: `${elapsed}ms` })
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      logger.error(`Axios > ${source}`, '❌ 请求异常', { message: error.message })
      ElMessage.error('请求异常：' + error.message)
    }
    return Promise.reject(error)
  }
)

function trySafeStringify(obj) {
  try {
    const seen = new WeakSet()
    return JSON.parse(JSON.stringify(obj, (key, value) => {
      if (typeof value === 'object' && value !== null) { if (seen.has(value)) return '[Circular]' }
      if (/password/i.test(key)) return '***'
      return value
    }))
  } catch { return { error: 'Failed to serialize' } }
}

function trySummarize(data) {
  if (data === null || data === undefined) return 'null'
  if (Array.isArray(data)) return `Array(${data.length})`
  if (typeof data === 'object') {
    const s = {}
    for (const k of Object.keys(data)) {
      if (Array.isArray(data[k])) s[k] = `Array(${data[k].length})`
      else if (typeof data[k] === 'object' && data[k] !== null) s[k] = typeof data[k]
      else s[k] = data[k]
    }
    return s
  }
  return String(data).substring(0, 100)
}

export default service
```

### 5.4 API 模块（参考格式）

每个 API 模块按业务领域拆分，统一使用箭头函数导出：

```javascript
// src/api/modules/example.js
import service from '../request'

/** 接口描述 */
export const apiFunction = (params) => service.get('/url', { params })
export const apiPost = (data) => service.post('/url', data)
export const apiPut = (data) => service.put('/url', data)
export const apiDel = (url) => service.delete(url)
```

### 5.5 Pinia 状态管理 — `src/stores/auth.js`

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import logger from '@/utils/logger'

export const useAuthStore = defineStore('auth', () => {
  // ⚠️ 后端无 JWT Token，仅存储 userInfo（来自 LoginVO）
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!userInfo.value?.role)
  const displayName = computed(() => userInfo.value.displayName || '')
  const role = computed(() => userInfo.value.role || '')
  const sno = computed(() => userInfo.value.sno || '')
  const cardNo = computed(() => userInfo.value.cardNo || '')

  function setLogin(data) {
    // data = LoginVO: { role, username, sno, cardNo, displayName }
    userInfo.value = data
    localStorage.setItem('userInfo', JSON.stringify(data))
    logger.state('AuthStore > setLogin', '✅ 登录', {
      role: data?.role,
      displayName: data?.displayName,
      sno: data?.sno,
      cardNo: data?.cardNo,
    })
  }

  function logout() {
    const before = { role: role.value, displayName: displayName.value, sno: sno.value }
    userInfo.value = {}
    localStorage.removeItem('userInfo')
    logger.state('AuthStore > logout', '✅ 退出', { clearedUser: before })
  }

  return { userInfo, isLoggedIn, displayName, role, sno, cardNo, setLogin, logout }
})
```

> **说明：** 后端 `LoginVO` 仅返回 `{ role, username, sno, cardNo, displayName }`，不含 token。系统无 JWT 认证，登录状态仅靠 localStorage 中存储的 userInfo 判断。若后续后端增加了 JWT，再在 store 中添加 token 字段并在 Axios 拦截器中注入 Authorization 头。

### 5.6 路由配置 — `src/router/index.js`

使用 Vue Router 的 **hash 模式**（避免页面刷新时 404）。共 10 条路由：

| 路径 | 视图组件 | 元信息 | 访问角色 | 说明 |
| ---- | -------- | ------ | -------- | ---- |
| `/login` | `LoginView` | `{ noAuth: true }` | 全部 | 登录页（无需认证） |
| `/` | `MainLayout` → `DashboardView` | — | 全部 | 首页（仪表盘） |
| `/books/search` | `MainLayout` → `BookSearchView` | — | 全部 | 图书检索 |
| `/borrow` | `MainLayout` → `BorrowView` | — | 全部 | 借书 |
| `/return` | `MainLayout` → `ReturnView` | — | 全部 | 还书 |
| `/cards` | `MainLayout` → `CardManageView` | `{ roles: ['admin'] }` | **管理员** | 借书证管理 |
| `/books/edit` | `MainLayout` → `BookEditView` | `{ roles: ['admin'] }` | **管理员** | 图书采编 |
| `/fines` | `MainLayout` → `FineManageView` | — | 全部 | 罚款管理 |
| `/personal` | `MainLayout` → `PersonalView` | — | 全部 | 个人查询 |

**导航守卫**：从 `authStore.isLoggedIn` 判断登录状态，未登录且目标不是 `/login` 时重定向。同时从 `to.meta.roles` 判断角色权限，若当前用户角色不在允许列表中则重定向到首页。

```javascript
import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import logger from '@/utils/logger'

const routes = [ /* ... 路由表 ... */ ]
const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isLoggedIn
  const requiresAuth = !to.meta?.noAuth
  logger.navigation(`路由 > ${from.name || '初始'} → ${to.name}`, '▶ 跳转', { requiresAuth, isAuthenticated })
  if (requiresAuth && !isAuthenticated) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})
```
 * 全局前置守卫
 * 日志输出：
 *   [导航] 来源 → 目标 (路由名称) 是否需要认证
 *   [导航] ✅ 允许通过 / ❌ 重定向到登录页
 *   [导航] ⛔ 角色不匹配 → 重定向到首页（含角色信息）
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isLoggedIn
  const requiresAuth = !to.meta?.noAuth
  const allowedRoles = to.meta?.roles     // 允许访问的角色数组，如 ['admin']

  // ─── 导航日志：记录每次路由跳转 ───
  logger.navigation(`路由守卫 > ${from.name || '初始进入'} → ${to.name}`, '▶ 跳转', {
    fromPath: from.path || '/',
    toPath: to.path,
    toName: to.name,
    requiresAuth,
    isAuthenticated,
    userRole: authStore.role,
    allowedRoles,
    meta: to.meta,
  })

  // 第一步：未登录认证检查
  if (requiresAuth && !isAuthenticated) {
    logger.navigation(`路由守卫 > ${to.name}`, '❌ 未登录，重定向到 /login', {
      redirect: to.fullPath,
    })
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 第二步：角色权限检查（如果路由配置了 roles）
  if (isAuthenticated && allowedRoles && allowedRoles.length > 0) {
    if (!allowedRoles.includes(authStore.role)) {
      logger.navigation(`路由守卫 > ${to.name}`, '⛔ 角色不匹配，重定向到首页', {
        userRole: authStore.role,
        requiredRoles: allowedRoles,
      })
      ElMessage.error('无权限访问该页面')
      next({ path: '/dashboard' })
      return
    }
  }

  logger.navigation(`路由守卫 > ${to.name}`, '✅ 允许通过', { userRole: authStore.role })
  next()
})

/**
 * 后置守卫
 * 日志输出：导航完成
 */
router.afterEach((to) => {
  logger.navigation(`路由 > ${to.name}`, '✅ 导航完成', { path: to.path })
})
```

```javascript
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
      },
      {
        path: 'books/search',
        name: 'BookSearch',
        component: () => import('@/views/book/BookSearchView.vue'),
      },
      {
        path: 'borrow',
        name: 'Borrow',
        component: () => import('@/views/borrow/BorrowView.vue'),
      },
      {
        path: 'return',
        name: 'Return',
        component: () => import('@/views/return/ReturnView.vue'),
      },
      {
        path: 'cards',
        name: 'CardManage',
        component: () => import('@/views/card/CardManageView.vue'),
        meta: { roles: ['admin'] },       // ⚠️ 仅管理员可访问
      },
      {
        path: 'books/edit',
        name: 'BookEdit',
        component: () => import('@/views/book/BookEditView.vue'),
        meta: { roles: ['admin'] },       // ⚠️ 仅管理员可访问
      },
      {
        path: 'fines',
        name: 'FineManage',
        component: () => import('@/views/fine/FineManageView.vue'),
      },
      {
        path: 'personal',
        name: 'Personal',
        component: () => import('@/views/personal/PersonalView.vue'),
      },
    ],
  },
]
```

### 5.7 主布局 — `src/layouts/MainLayout.vue`

Element Plus 的 `el-container` + `el-aside` + `el-header` + `el-main` 经典后台布局。

**Topbar.vue**：显示 Logo、系统名称、当前用户欢迎语、退出按钮。
**Sidebar.vue**：使用 `el-menu` 实现菜单导航，菜单项与路由配置对应。`el-menu` 的 `default-active` 绑定当前路由路径。

> ⚠️ **角色过滤渲染**：侧边栏菜单项需根据当前用户角色动态渲染。`v-if="authStore.role === 'admin'"` 控制借书证管理和图书采编菜单项仅对管理员可见。学生登录后侧边栏仅显示首页、图书检索、借书、还书、罚款管理、个人查询六个菜单项。

```vue
<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

defineProps({
  isCollapse: { type: Boolean, default: false },
})

const route = useRoute()
const authStore = useAuthStore()
const activeMenu = computed(() => route.path)
const isAdmin = computed(() => authStore.role === 'admin')
</script>
```

侧边栏菜单使用 `v-if` 进行角色过滤的示例写法：

```html
<!-- 对所有角色可见的公共菜单 -->
<el-menu-item index="/dashboard">
  <el-icon><DataBoard /></el-icon>
  <template #title>首页</template>
</el-menu-item>

<!-- ...其他公共菜单... -->

<!-- 仅管理员可见的菜单 - 借书证管理 -->
<el-menu-item v-if="isAdmin" index="/cards">
  <el-icon><CreditCard /></el-icon>
  <template #title>借书证管理</template>
</el-menu-item>

<!-- 仅管理员可见的菜单 - 图书采编 -->
<el-menu-item v-if="isAdmin" index="/books/edit">
  <el-icon><EditPen /></el-icon>
  <template #title>图书采编</template>
</el-menu-item>
```

---

### 5.8 统一日志工具 — `src/utils/logger.js`

#### 5.8.1 设计目标

日志系统需要满足以下要求：

| 要求 | 说明 |
| ---- | ---- |
| **操作来源可辨** | 每条日志必须标识来自哪个页面、哪个组件、哪个函数 |
| **用户身份可溯** | 每条日志必须包含当前操作用户的角色和身份标识 |
| **步骤粒度精细** | 每个函数的每个关键步骤（进入、参数、请求开始/成功/失败、结果）都必须有日志 |
| **分类清晰** | 区分操作日志、网络日志、导航日志、错误日志 |
| **不影响生产** | 通过 `__DEV__` 环境变量控制，开发环境全量输出 |

#### 5.8.2 日志标签体系

每条日志的格式为：

```
[时间] [分类] [页面/组件] [函数名] [步骤标签] 用户={角色|标识} 详情={...}
```

日志分类标签：

| 标签 | 用途 | 颜色 |
| ---- | ---- | ---- |
| `[操作]` | 用户触发的事件（点击、提交、切换） | 蓝色 |
| `[网络]` | HTTP 请求与响应 | 紫色 |
| `[导航]` | 路由跳转与守卫 | 绿色 |
| `[状态]` | Pinia store 状态变更 | 橙色 |
| `[渲染]` | 组件挂载与数据初始化 | 灰色 |
| `[错误]` | 异常与错误捕获 | 红色 |
| `[校验]` | 表单校验与数据验证 | 黄色 |

步骤标签（每个函数内部的步骤标识）：

| 标签 | 含义 |
| ---- | ---- |
| `▶ 进入` | 函数开始执行 |
| `⏳ 参数` | 函数接收的参数 |
| `📤 请求` | 发起 HTTP 请求 |
| `📥 响应` | 收到 HTTP 响应 |
| `✅ 成功` | 操作成功完成 |
| `❌ 失败` | 操作失败/异常 |
| `🔚 结束` | 函数执行完毕，返回结果 |
| `⏱ 耗时` | 性能计时（ms） |

#### 5.8.3 完整代码

```javascript
/**
 * 统一日志工具
 * 
 * 功能：
 * 1. 结构化的操作日志，可追溯每一步的来源
 * 2. 自动注入当前用户上下文（角色、姓名、学号/证号）
 * 3. 支持分组日志（console.group）和性能计时
 * 4. 开发环境全量输出，生产环境可通过开关控制
 * 
 * 使用示例：
 *   logger.operation('BorrowView', 'queryCard', '▶ 进入', { cardNo: 'C2024001' })
 *   logger.network('GET', '/api/cards/C2024001', '📤 请求')
 *   logger.network('GET', '/api/cards/C2024001', '📥 响应', { status: 200, data: {...} })
 */

const LOG_ENABLED = true           // 是否启用日志（生产环境可关闭）
const LOG_LEVEL = 'debug'          // 'debug' | 'info' | 'warn' | 'error'

// 日志分类样式（console 样式字符串）
const STYLES = {
  operation: 'background: #1E3A8A; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
  network:   'background: #7C3AED; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
  navigation:'background: #059669; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
  state:     'background: #D97706; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
  render:    'background: #6B7280; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
  error:     'background: #DC2626; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
  validate:  'background: #CA8A04; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;',
}

// 分类前缀映射
const TAG_MAP = {
  operation: '[操作]',
  network:   '[网络]',
  navigation:'[导航]',
  state:     '[状态]',
  render:    '[渲染]',
  error:     '[错误]',
  validate:  '[校验]',
}

/**
 * 获取当前用户上下文
 * @returns {{ role: string, id: string, name: string }}
 */
function getUserContext() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return { role: '未登录', id: '-', name: '-' }
    const info = JSON.parse(raw)
    return {
      role: info.role || '未知',
      id: info.sno || info.cardNo || info.username || '-',
      name: info.displayName || info.studentName || '-',
    }
  } catch {
    return { role: '未知', id: '-', name: '-' }
  }
}

/**
 * 格式化时间戳
 * @returns {string} HH:mm:ss.SSS
 */
function timestamp() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${String(d.getMilliseconds()).padStart(3, '0')}`
}

/**
 * 核心日志方法
 * @param {string} category - 日志分类（operation/network/navigation/state/render/error/validate）
 * @param {string} source - 来源标识，格式 "页面 > 组件 > 函数"
 * @param {string} step - 步骤标签（▶ 进入 / ⏳ 参数 / 📤 请求 / 📥 响应 / ✅ 成功 / ❌ 失败 / 🔚 结束）
 * @param {object} [detail] - 详细信息对象
 * @param {string} [level='info'] - 日志级别
 */
function log(category, source, step, detail = null, level = 'info') {
  if (!LOG_ENABLED) return

  const user = getUserContext()
  const time = timestamp()
  const tag = TAG_MAP[category] || `[${category}]`
  const style = STYLES[category] || ''
  const userStr = `👤 ${user.role}|${user.id}|${user.name}`

  // 构建日志前缀
  const prefix = `%c${tag}%c ${time} ${source} ${step}`

  if (detail !== null && detail !== undefined) {
    const detailStr = typeof detail === 'object' ? detail : { value: detail }
    if (level === 'error') {
      console.error(prefix, style, '', userStr, detailStr)
    } else if (level === 'warn') {
      console.warn(prefix, style, '', userStr, detailStr)
    } else {
      console.log(prefix, style, '', userStr, detailStr)
    }
  } else {
    console.log(prefix, style, '', userStr)
  }
}

/**
 * 启动一个分组日志
 * @param {string} title - 分组标题
 */
function group(title) {
  if (!LOG_ENABLED) return
  const time = timestamp()
  console.group(`%c📦 ${time} ${title}`, 'font-weight: bold; font-size: 13px;')
}

/**
 * 结束当前分组日志
 */
function groupEnd() {
  if (!LOG_ENABLED) return
  console.groupEnd()
}

/**
 * 启动性能计时器
 * @param {string} label - 计时器标识
 */
function time(label) {
  if (!LOG_ENABLED) return
  console.time(`⏱ ${label}`)
}

/**
 * 结束性能计时器并输出耗时
 * @param {string} label - 计时器标识
 */
function timeEnd(label) {
  if (!LOG_ENABLED) return
  console.timeEnd(`⏱ ${label}`)
}

// ============ 导出便捷方法 ============

export const logger = {
  // 操作日志（用户触发的事件）
  operation: (source, step, detail, level) => log('operation', source, step, detail, level),

  // 网络日志（HTTP 请求/响应）
  network: (source, step, detail, level) => log('network', source, step, detail, level),

  // 导航日志（路由跳转）
  navigation: (source, step, detail, level) => log('navigation', source, step, detail, level),

  // 状态日志（Pinia store）
  state: (source, step, detail, level) => log('state', source, step, detail, level),

  // 渲染日志（组件生命周期）
  render: (source, step, detail, level) => log('render', source, step, detail, level),

  // 错误日志
  error: (source, step, detail) => log('error', source, step, detail, 'error'),

  // 校验日志
  validate: (source, step, detail, level) => log('validate', source, step, detail, level),

  // 分组日志
  group,
  groupEnd,

  // 性能计时
  time,
  timeEnd,
}

export default logger
```

#### 5.8.4 使用示例

```javascript
// 在任意页面/组件中使用：
import logger from '@/utils/logger'

// 示例 1：操作日志（用户点击了搜索按钮）
logger.operation('BookSearchView > handleSearch', '▶ 进入', { keyword: 'Java' })
// 输出：[操作] 14:30:25.123 BookSearchView > handleSearch ▶ 进入 👤 admin|admin|管理员 {keyword: "Java"}

// 示例 2：网络日志
logger.network('BookSearchView > searchBooks', '📤 请求', { url: '/api/books/search', params: { keyword: 'Java' } })
// 输出：[网络] 14:30:25.150 BookSearchView > searchBooks 📤 请求 👤 admin|admin|管理员 {...}

// 示例 3：分组日志（一个完整操作流程）
logger.group('借书流程 — C2024001')
logger.operation('BorrowView > queryCard', '▶ 进入', { cardNo: 'C2024001' })
logger.network('BorrowView > queryCard', '📤 请求', { url: '/api/cards/C2024001' })
logger.network('BorrowView > queryCard', '📥 响应', { status: 200, data: { sno: '2024101001', status: 1 } })
logger.operation('BorrowView > queryCard', '✅ 成功', { studentName: '张三', borrowCount: 2 })
logger.groupEnd()
```

#### 5.8.5 在 Axios 拦截器中集成

将 `logger` 集成到 `request.js` 的拦截器中，使每次 HTTP 通信自动输出网络日志（详见 5.3 节）。日志信息包括：

- 请求：方法、完整 URL、请求参数（不包含密码）、请求头（不包含 Authorization 值）
- 响应：HTTP 状态码、响应 data 的 code/message、响应数据大小（近似）
- 耗时：从请求发出到响应返回的毫秒数
- 错误：HTTP 错误时的状态码和错误信息

#### 5.8.6 在路由守卫中的集成

在 `router/index.js` 的导航守卫中集成 `logger.navigation`，输出每次路由跳转的日志：

- 来源路径 → 目标路径
- 路由名称
- 是否需要认证
- 导航结果（允许/重定向到登录）

#### 5.8.7 在 Pinia Store 中集成

在 `auth store` 的 `setLogin` / `logout` 方法中集成 `logger.state`，输出：

- 用户登录：角色、用户名/学号、显示名
- 用户退出：清除的 token 和 userInfo
- Token 过期：自动跳转登录页

---

## 6. 第二阶段：页面视图详细规范

### 6.1 通用视图结构

每个页面视图遵循以下模板结构。所有关键函数的第一行和最后一行必须输出 `logger.operation` 日志，每次 API 调用前后输出 `logger.network` 日志（格式见 5.8 节）。

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiFunction } from '@/api/modules/xxx'
import logger from '@/utils/logger'

const COMPONENT = 'XxxView > '   /* 组件标识，所有日志的来源前缀 */
const loading = ref(false)
const dataList = ref([])

async function loadData() {
  logger.render(`${COMPONENT}loadData`, '▶ 进入', { triggeredBy: 'onMounted' })
  loading.value = true
  try {
    const res = await apiFunction()
    dataList.value = res.data
    logger.render(`${COMPONENT}loadData`, '🔚 结束', { recordCount: res.data?.length || 0 })
  } catch (e) {
    logger.error(`${COMPONENT}loadData`, '❌ 加载失败', { errorMessage: e.message })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', {})
  loadData()
})
</script>
```

### 6.2 登录页 — `LoginView.vue`

#### 页面设计

- 全屏居中卡片式设计（居中容器，不包含侧边栏和顶栏）
- 使用 `el-card` 包含登录表单
- 使用 `el-tabs` 切换 **管理员登录** / **学生登录**
- 管理员登录：`el-input` 用户名 + 密码
- 学生登录：`el-input` 借书证号 + 密码
- "记住密码"复选框（localStorage 存储账号密码）
- `el-button` 登录按钮

#### 接口对接

> **日志要求：** 登录是系统的入口操作，日志必须清晰记录每次登录尝试，包括登录模式（管理员/学生）、登录账号、校验结果和跳转目标。

```javascript
import { login } from '@/api/modules/auth'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import logger from '@/utils/logger'

const COMPONENT = 'LoginView > '
const authStore = useAuthStore()
const router = useRouter()

/**
 * 处理登录
 * 
 * 日志流水（完整跟踪每一步）：
 *   ① [操作] LoginView > handleLogin ▶ 进入 → { role, username（脱敏） }
 *   ② [校验] LoginView > handleLogin ⏳ 表单校验 → { isValid, errors }
 *   ③ [网络] Axios > POST /api/login 📤 请求 → { role, username }
 *   ④ [网络] Axios > POST /api/login 📥 响应 → { status, 耗时ms }
 *   ⑤ [网络] Axios > POST /api/login ✅ 成功 → { role, displayName }
 *   ⑥ [状态] AuthStore > setLogin ▶ 进入 → { hasToken, userInfoKeys }
 *   ⑦ [状态] AuthStore > setLogin ✅ 成功 → { role, displayName }
 *   ⑧ [操作] LoginView > handleLogin ✅ 成功 → { role, displayName, redirectTo }
 *   ⑨ [导航] 路由守卫 > Login → Dashboard ▶ 跳转
 *   ⑩ 若校验不通过 → [校验] LoginView > handleLogin ❌ 校验失败 → { errors }
 *   ⑪ 若登录失败 → [错误] LoginView > handleLogin ❌ 登录失败 → { message }
 */
async function handleLogin() {
  // ─── 步骤 ①：记录进入登录操作 ───
  logger.operation(`${COMPONENT}handleLogin`, '▶ 进入', {
    role: activeTab.value,  // 'admin' 或 'student'
    username: activeTab.value === 'admin' ? form.value.username : 'cardNo:' + form.value.username?.substring(0, 4) + '***',
  })

  // ─── 步骤 ②：表单校验（Element Plus el-form 内置校验） ───
  let valid = false
  try {
    valid = await formRef.value.validate()
  } catch (e) {
    logger.validate(`${COMPONENT}handleLogin`, '❌ 校验异常', { error: e.message })
  }
  if (!valid) {
    logger.validate(`${COMPONENT}handleLogin`, '❌ 校验失败', {
      errors: formRef.value?.validateState || 'validate returned false',
    })
    return
  }
  logger.validate(`${COMPONENT}handleLogin`, '✅ 校验通过')

  try {
    // ─── 步骤 ③-⑤：HTTP 请求（Axios 拦截器自动输出网络日志） ───
    const res = await login({
      role: activeTab.value,
      username: form.value.username,
      password: form.value.password,
    })
    // ⚠️ 后端 LoginVO 返回 { role, username, sno, cardNo, displayName }，不含 token
    //    直接将 res.data 传入 authStore.setLogin

    // ─── 步骤 ⑥-⑦：存储登录状态（Pinia store 内部输出状态日志） ───
    authStore.setLogin(res.data)

    // ─── 步骤 ⑧：记录登录成功 ───
    logger.operation(`${COMPONENT}handleLogin`, '✅ 成功', {
      role: res.data?.role,
      displayName: res.data?.displayName,
      sno: res.data?.sno,
      cardNo: res.data?.cardNo,
      redirectTo: '/',
    })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // ─── 步骤 ⑪：记录登录失败（网络/业务错误在 Axios 拦截器中已记录） ───
    logger.error(`${COMPONENT}handleLogin`, '❌ 登录失败', {
      role: activeTab.value,
      username: form.value.username,
      errorMessage: e.message,
    })
  }
}
```

- 用户名/借书证号：必填
- 密码：必填，长度 6-20 位
- 使用 Element Plus `el-form` 的 `:rules` 属性

### 6.3 首页 — `DashboardView.vue`

#### 页面设计

使用 Element Plus 布局组件：

1. **统计卡片行**：4 个 `el-card`，使用 `el-row` + `el-col` 四列布局。
   后端 `DashboardStatsVO` 字段：`totalBooks(种数)`、`borrowedCopies(借出)`、`activeCards(有效证)`、`unpaidFines(未缴笔数)`。

2. **排行榜区域**（左右两栏，`el-row :gutter="16"`）：
   - 左侧：**当月借书学生排行**（TOP 10）— `el-table`
   - 右侧：**当月热门图书排行**（TOP 10）— `el-table`

#### 接口对接

> **日志要求：** 首页在 `onMounted` 时并行加载统计数据和排行榜，日志需记录两个请求的并发和各自的响应耗时。

```javascript
import { getStats, getRankings } from '@/api/modules/dashboard'
import logger from '@/utils/logger'

const COMPONENT = 'DashboardView > '
const stats = ref({})
const studentRankings = ref([])
const bookRankings = ref([])

/**
 * 加载首页数据（统计 + 排行榜）
 * 
 * 日志流水：
 *   ① [渲染] DashboardView > loadDashboard ▶ 进入（组件挂载触发）
 *   ② [操作] DashboardView > loadDashboard ⏳ 发起并行请求
 *   ③ [网络] Axios > GET /api/dashboard/stats   📤 请求
 *   ④ [网络] Axios > GET /api/dashboard/rankings 📤 请求
 *   ⑤ [网络] Axios > GET /api/dashboard/stats   📥 响应 → 耗时ms
 *   ⑥ [网络] Axios > GET /api/dashboard/rankings 📥 响应 → 耗时ms
 *   ⑦ [渲染] DashboardView > loadDashboard 🔚 结束 → { stats字段, 学生排行数, 图书排行数 }
 *   ⑧ 若有异常 → [错误] DashboardView > loadDashboard ❌ 失败
 */
async function loadDashboard() {
  logger.render(`${COMPONENT}loadDashboard`, '▶ 进入', { triggeredBy: 'onMounted' })
  logger.operation(`${COMPONENT}loadDashboard`, '⏳ 发起并行请求', {
    requests: ['GET /api/dashboard/stats', 'GET /api/dashboard/rankings'],
  })

  try {
    const [statsRes, rankRes] = await Promise.all([
      getStats(),
      getRankings(),
    ])

    stats.value = statsRes.data
    studentRankings.value = rankRes.data.studentRankings
    bookRankings.value = rankRes.data.bookRankings

    // 记录加载完成摘要
    logger.render(`${COMPONENT}loadDashboard`, '🔚 结束', {
      totalBooks: statsRes.data?.totalBooks,
      totalCopies: statsRes.data?.totalCopies,
      borrowedCopies: statsRes.data?.borrowedCopies,
      activeCards: statsRes.data?.activeCards,
      unpaidFines: statsRes.data?.unpaidFines,
      studentRankingCount: rankRes.data?.studentRankings?.length || 0,
      bookRankingCount: rankRes.data?.bookRankings?.length || 0,
    })
  } catch (e) {
    logger.error(`${COMPONENT}loadDashboard`, '❌ 加载首页失败', {
      errorMessage: e.message,
      statsLoaded: !!stats.value?.totalBooks,
      rankingsLoaded: studentRankings.value.length > 0,
    })
  }
}
```

**后端返回的 DashboardStatsVO 字段：**
- `totalBooks` — 馆藏图书**种数**
- `totalCopies` — 馆藏副本**总数**
- `availableCopies` — 可借副本数
- `borrowedCopies` — 已借出副本数
- `activeStudents` — 活跃学生数
- `activeCards` — 有效借书证数
- `currentBorrowings` — 当前借阅数
- `unpaidFines` — 未缴罚款笔数

**排行榜 RankingVO：**
- `studentRankings` — 数组，每项 `{ sno, studentName, borrowCount }`
- `bookRankings` — 数组，每项 `{ ISBN, bookName, borrowCount }`

### 6.4 图书检索页 — `BookSearchView.vue`

#### 页面设计

- **搜索区**：`el-input` 关键词 + `el-button` 搜索（带搜索图标）
- **图书列表**：`el-table` 展示搜索结果
- **图书详情**：点击某行后，下方展开 `el-descriptions` 展示详情，或使用右侧 `el-drawer`
  - 图书信息：ISBN、书名、作者、出版社、出版日期、分类、简介
  - 副本列表：`el-table` 展示该图书的全部副本（条码、馆藏地点、状态标签）

#### 接口对接

> **日志要求：** 图书检索涉及两个操作（搜索 + 查看详情），每个操作的每一步都需记录，包括搜索关键词、命中数量、查看详情的 ISBN。

```javascript
import { searchBooks, getBookDetail } from '@/api/modules/books'
import logger from '@/utils/logger'

const COMPONENT = 'BookSearchView > '

/**
 * 搜索图书
 * 
 * 日志流水：
 *   ① [操作] BookSearchView > handleSearch ▶ 进入 → { keyword }
 *   ② [校验] BookSearchView > handleSearch ⏳ 空值检查
 *   ③ [网络] Axios > GET /api/books/search 📤 请求 → { keyword }
 *   ④ [网络] Axios > GET /api/books/search 📥 响应 → { 耗时ms, 命中数 }
 *   ⑤ [操作] BookSearchView > handleSearch ✅ 成功 → { keyword, hitCount }
 *   ⑥ 若关键词为空 → [校验] BookSearchView > handleSearch ❌ 关键词为空
 *   ⑦ 若无结果 → [操作] BookSearchView > handleSearch ⚠️ 无匹配结果
 */
async function handleSearch() {
  // ─── 步骤 ① ───
  logger.operation(`${COMPONENT}handleSearch`, '▶ 进入', { keyword: keyword.value })

  // ─── 步骤 ②：空值校验 ───
  if (!keyword.value || keyword.value.trim() === '') {
    logger.validate(`${COMPONENT}handleSearch`, '❌ 关键词为空', { keyword: keyword.value })
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    const res = await searchBooks(keyword.value)
    bookList.value = res.data || []

    // ─── 步骤 ⑤ ───
    logger.operation(`${COMPONENT}handleSearch`, '✅ 成功', {
      keyword: keyword.value,
      hitCount: bookList.value.length,
      firstMatch: bookList.value.length > 0 ? bookList.value[0]?.bookName : '无结果',
    })
  } catch (e) {
    logger.error(`${COMPONENT}handleSearch`, '❌ 搜索失败', { keyword: keyword.value })
  }
}

/**
 * 查看图书详情（含全部副本）
 * 
 * 日志流水：
 *   ① [操作] BookSearchView > showDetail ▶ 进入 → { ISBN, bookName }
 *   ② [网络] Axios > GET /api/books/{isbn} 📤 请求
 *   ③ [网络] Axios > GET /api/books/{isbn} 📥 响应 → { 耗时ms }
 *   ④ [操作] BookSearchView > showDetail ✅ 成功 → { copiesCount }
 *   ⑤ 若有异常 → [错误] BookSearchView > showDetail ❌ 失败
 */
async function showDetail(isbn) {
  // ─── 步骤 ① ───
  const bookInfo = bookList.value.find(b => b.ISBN === isbn)
  logger.operation(`${COMPONENT}showDetail`, '▶ 进入', {
    ISBN: isbn,
    bookName: bookInfo?.bookName || '未知',
  })

  try {
    const res = await getBookDetail(isbn)
    currentBook.value = res.data
    // res.data = BookSearchVO = { ISBN, bookName, author, publisher, pubDate, introduction, category, copies: [...] }

    // ─── 步骤 ④ ───
    const copies = res.data?.copies || []
    const statusCount = {}
    copies.forEach(c => {
      const key = ['下架', '可借', '已借出', '已报废'][c.status] || `未知(${c.status})`
      statusCount[key] = (statusCount[key] || 0) + 1
    })
    logger.operation(`${COMPONENT}showDetail`, '✅ 成功', {
      ISBN: isbn,
      bookName: res.data?.bookName,
      copiesCount: copies.length,
      statusSummary: statusCount,
    })
  } catch (e) {
    logger.error(`${COMPONENT}showDetail`, '❌ 获取详情失败', { ISBN: isbn })
  }
}
```

#### 副本状态显示

| 状态码 | 显示文字 | Element Plus 组件 |
| ------ | -------- | ----------------- |
| 0 | 下架 | `el-tag type="danger"` |
| 1 | 可借 | `el-tag type="success"` |
| 2 | 已借出 | `el-tag type="warning"` |
| 3 | 已报废 | `el-tag type="info"` |

### 6.5 借书页 — `BorrowView.vue`

#### 页面设计

- **借书证扫描区**：`el-input` 输入借书证号 + `el-button` 查询 → 显示证件信息（证号、学号、状态）
  - ⚠️ 后端 `GET /api/cards/{cardNo}` 仅返回 `Libcard { cardNo, sno, status }`，不含学生姓名和借阅数量
- **副本条码录入区**：`el-input` 输入条码 + `el-button` 添加 → 动态添加到待借列表
- **待借列表**：`el-table` 显示已录入的待借图书（书名、条码），每行有删除按钮
- **借出按钮**：`el-button type="primary"` 提交所有待借图书

#### 接口对接

> ⚠️ **重要：** 实际后端缺少按条码查询单个副本的接口（`GET /api/books/copy?barCode=`），需要前端在与后端联调时协商补充该接口，或由后端从 `GET /api/books/{isbn}` 的副本列表中筛选。
>
> **日志要求：** 借书是系统中交互最复杂的流程（证号查询 → 逐条录入 → 确认借出），每个子操作都必须有完整的日志跟踪。特别是证号查询的结果（证件状态、当前借阅数）和借出结果（成功/失败明细）需要详细记录。

```javascript
import { getCardInfo } from '@/api/modules/cards'
import { getBookDetail } from '@/api/modules/books'   // queryCopy 中需要按条码查图书及副本
import { borrowBooks } from '@/api/modules/borrow'
import logger from '@/utils/logger'

const COMPONENT = 'BorrowView > '

/**
 * 查询借书证信息
 * 
 * 日志流水：
 *   ① [操作] BorrowView > queryCard ▶ 进入 → { cardNo }
 *   ② [网络] Axios > GET /api/cards/{cardNo} 📤 请求
 *   ③ [网络] Axios > GET /api/cards/{cardNo} 📥 响应 → { 耗时ms, status }
 *   ④ [操作] BorrowView > queryCard ✅ 成功 → { sno, status（中文）, borrowingCount? }
 *   ⑤ 若证件异常 → [操作] BorrowView > queryCard ⚠️ 证件状态异常 → { status }
 */
async function queryCard() {
  // ─── 步骤 ① ───
  const inputCardNo = cardNo.value
  logger.operation(`${COMPONENT}queryCard`, '▶ 进入', { cardNo: inputCardNo })

  if (!inputCardNo) {
    logger.validate(`${COMPONENT}queryCard`, '❌ 证号为空')
    ElMessage.warning('请输入借书证号')
    return
  }

  try {
    const res = await getCardInfo(inputCardNo)
    // res.data = Libcard = { cardNo, sno, status } — 只有证件信息
    cardInfo.value = res.data

    const statusMap = { 0: '注销', 1: '正常', 2: '挂失', 3: '作废' }
    const statusText = statusMap[res.data?.status] || `未知(${res.data?.status})`

    // ─── 步骤 ④ ───
    logger.operation(`${COMPONENT}queryCard`, '✅ 成功', {
      cardNo: res.data?.cardNo,
      sno: res.data?.sno,
      status: res.data?.status,
      statusText,
    })

    // ─── 步骤 ⑤：非正常状态提示 ───
    if (res.data?.status !== 1) {
      logger.operation(`${COMPONENT}queryCard`, '⚠️ 证件状态异常', {
        cardNo: inputCardNo,
        status: res.data?.status,
        statusText,
      })
      ElMessage.warning(`当前借书证状态为"${statusText}"，不可借书`)
    }
  } catch (e) {
    logger.error(`${COMPONENT}queryCard`, '❌ 查询失败', { cardNo: inputCardNo })
  }
}

/**
 * 查询副本信息（通过条码查询）
 * 
 * 日志流水：
 *   ① [操作] BorrowView > queryCopy ▶ 进入 → { barCode }
 *   ② [网络] Axios > GET /api/books/{isbn}（间接查询）📤 请求
 *   ③ [操作] BorrowView > queryCopy ✅ 成功 → { bookName, barCode }
 *   ④ 若条码已存在 → [校验] BorrowView > queryCopy ⚠️ 条码重复
 *   ⑤ 若副本不可借 → [校验] BorrowView > queryCopy ❌ 副本状态不可借
 */
async function queryCopy(barCode) {
  // ─── 步骤 ① ───
  logger.operation(`${COMPONENT}queryCopy`, '▶ 进入', { barCode })

  if (!barCode) {
    logger.validate(`${COMPONENT}queryCopy`, '❌ 条码为空')
    return
  }

  // 检查是否已在待借列表中
  if (pendingList.value.some(item => item.barCode === barCode)) {
    logger.validate(`${COMPONENT}queryCopy`, '⚠️ 条码重复', { barCode })
    ElMessage.warning('该条码已在待借列表中')
    return
  }

  // 暂无可用的后端接口，以下为替代方案（通过 GET /api/books/{isbn} 筛选）
  try {
    // 从条码中提取 ISBN（假设条码格式为 BC-{ISBN}-{seq}）
    const isbn = barCode.startsWith('BC-') ? barCode.split('-')[1] : null
    if (!isbn) {
      logger.error(`${COMPONENT}queryCopy`, '❌ 无法从条码提取 ISBN', { barCode })
      ElMessage.error('条码格式不正确')
      return
    }

    // 查询图书详情，从副本列表中匹配
    const res = await getBookDetail(isbn)
    const copies = res.data?.copies || []
    const matchedCopy = copies.find(c => c.barCode === barCode)

    if (!matchedCopy) {
      logger.operation(`${COMPONENT}queryCopy`, '❌ 未找到该副本', { barCode, isbn })
      ElMessage.error('未找到该副本信息')
      return
    }

    if (matchedCopy.status !== 1) {
      const statusMap = { 0: '下架', 1: '可借', 2: '已借出', 3: '已报废' }
      logger.operation(`${COMPONENT}queryCopy`, '❌ 副本不可借', {
        barCode,
        status: matchedCopy.status,
        statusText: statusMap[matchedCopy.status],
      })
      ElMessage.warning(`该副本状态为"${statusMap[matchedCopy.status]}"，不可借出`)
      return
    }

    // 添加到待借列表
    pendingList.value.push({
      barCode: matchedCopy.barCode,
      bookName: res.data?.bookName || '未知',
      place: matchedCopy.place || '未知',
    })

    // ─── 步骤 ③ ───
    logger.operation(`${COMPONENT}queryCopy`, '✅ 成功', {
      barCode: matchedCopy.barCode,
      bookName: res.data?.bookName,
      place: matchedCopy.place,
      pendingCount: pendingList.value.length,
    })
  } catch (e) {
    logger.error(`${COMPONENT}queryCopy`, '❌ 查询副本失败', { barCode })
  }
}

/**
 * 确认借书
 * 
 * 日志流水：
 *   ① [操作] BorrowView > handleBorrow ▶ 进入 → { cardNo, barCodes }
 *   ② [校验] BorrowView > handleBorrow ⏳ 检查待借列表是否为空
 *   ③ [网络] Axios > POST /api/borrow 📤 请求 → { cardNo, barCodes[] }
 *   ④ [网络] Axios > POST /api/borrow 📥 响应 → { 耗时ms }
 *   ⑤ [操作] BorrowView > handleBorrow ✅ 成功 → { count, dueDate, recNos }
 */
async function handleBorrow() {
  // ─── 步骤 ① ───
  const barCodes = pendingList.value.map(item => item.barCode)
  logger.operation(`${COMPONENT}handleBorrow`, '▶ 进入', {
    cardNo: cardNo.value,
    barCodeCount: barCodes.length,
    barCodes,
  })

  // ─── 步骤 ②：校验 ───
  if (pendingList.value.length === 0) {
    logger.validate(`${COMPONENT}handleBorrow`, '❌ 待借列表为空')
    ElMessage.warning('请先添加待借图书')
    return
  }
  if (!cardNo.value) {
    logger.validate(`${COMPONENT}handleBorrow`, '❌ 借书证号为空')
    ElMessage.warning('请先查询借书证')
    return
  }

  try {
    const res = await borrowBooks({
      cardNo: cardNo.value,
      barCodes,
    })
    // ⚠️ 后端实际返回格式：{ recNos: [...], dueDate: "2026-06-24T...", count: 3 }
    const result = res.data

    logger.operation(`${COMPONENT}handleBorrow`, '✅ 成功`, {
      cardNo: cardNo.value,
      count: result.count,
      dueDate: result.dueDate,
      recNos: result.recNos,
    })
    ElMessage.success(`成功借出 ${result.count} 本，应还日期 ${result.dueDate?.substring(0, 10)}`)

    // 清空待借列表
    pendingList.value = []
    cardNo.value = ''
    cardInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleBorrow`, '❌ 借书失败', { cardNo: cardNo.value, errorMessage: e.message })
  }
}
```

### 6.6 还书页 — `ReturnView.vue`

#### 页面设计

- **条码扫描区**：`el-input` 输入副本条码 + `el-button` 还书
- **还书结果区**：`el-card` 或 `el-result` 显示本次还书详情
  - 图书信息（书名、ISBN）
  - 借出日期、应还日期
  - 是否超期：若超期，红色显示超期天数和罚款金额

#### 接口对接

> **日志要求：** 还书操作需要特别关注超期判断和罚款生成。日志必须记录条码输入、还书结果、超期细节（如有）和罚款详情。

```javascript
import { returnBook } from '@/api/modules/borrow'
import logger from '@/utils/logger'

const COMPONENT = 'ReturnView > '

/**
 * 还书
 * 
 * 日志流水：
 *   ① [操作] ReturnView > handleReturn ▶ 进入 → { barCode }
 *   ② [校验] ReturnView > handleReturn ⏳ 条码空值检查
 *   ③ [网络] Axios > POST /api/return 📤 请求 → { barCode }
 *   ④ [网络] Axios > POST /api/return 📥 响应 → { 耗时ms }
 *   ⑤ [操作] ReturnView > handleReturn ✅ 成功 → { bookName, 超期? }
 *   ⑥ 若超期 → [操作] ReturnView > handleReturn ⚠️ 超期 → { days, amount }
 *   ⑦ 若有罚款 → [操作] ReturnView > handleReturn 💰 产生罚款 → { fineId, amount }
 *   ⑧ 若条码为空 → [校验] ReturnView > handleReturn ❌ 条码为空
 */
async function handleReturn() {
  // ─── 步骤 ① ───
  const inputBarCode = barCode.value
  logger.operation(`${COMPONENT}handleReturn`, '▶ 进入', { barCode: inputBarCode })

  // ─── 步骤 ② ───
  if (!inputBarCode) {
    logger.validate(`${COMPONENT}handleReturn`, '❌ 条码为空')
    ElMessage.warning('请输入副本条码')
    return
  }

  try {
    const res = await returnBook({ barCode: inputBarCode })
    // ⚠️ 后端实际返回格式：{ recNo, overdue: boolean, overdueDays?, fineAmount?, fineId? }
    //    注意：不含 bookName/borDate/retDate 等字段
    result.value = res.data

    // ─── 步骤 ⑤：还书成功 ───
    const detail = {
      recNo: res.data?.recNo,
      hasOverdue: !!res.data?.overdue,
    }

    // ─── 步骤 ⑥：超期信息 ───
    if (res.data?.overdue) {
      detail.overdueDays = res.data.overdueDays
      detail.overdueAmount = res.data.fineAmount
      logger.operation(`${COMPONENT}handleReturn`, '⚠️ 超期', {
        barCode: inputBarCode,
        overdueDays: res.data.overdueDays,
        overdueAmount: res.data.fineAmount,
      })
    }

    // ─── 步骤 ⑦：罚款信息 ───
    if (res.data?.fineId) {
      detail.fineId = res.data.fineId
      detail.fineAmount = res.data.fineAmount
      logger.operation(`${COMPONENT}handleReturn`, '💰 产生罚款', {
        fineId: res.data.fineId,
        amount: res.data.fineAmount,
      })
    }

    logger.operation(`${COMPONENT}handleReturn`, '✅ 成功', detail)

    // 清空输入框，准备下一次还书
    barCode.value = ''
  } catch (e) {
    logger.error(`${COMPONENT}handleReturn`, '❌ 还书失败', { barCode: inputBarCode })
  }
}
```

### 6.7 借书证管理页 — `CardManageView.vue`

> ⚠️ **角色权限说明：** 借书证管理为**管理员专用页面**，学生无权限访问。页面入口在侧边栏已通过 `v-if="isAdmin"` 过滤，路由守卫已配置 `meta: { roles: ['admin'] }`。此外，页面组件内部需增加第二道防护——在 `onMounted` 中检查当前用户角色，若非管理员则显示"无权限访问"提示并阻止内容渲染。

#### 页面设计

- **权限拦截层**：使用 `v-if="isAdmin"` 控制整个页面内容渲染，非管理员时显示 `el-result` 无权限提示卡片
- **内容区**：仅管理员可见，使用 `el-tabs` 切换四个操作区域：

| Tab 名称 | 内容 |
| -------- | ---- |
| **新办** | 表单（学号、姓名、性别、学生类型、学院、专业、电话、新证号、密码）+ 新办按钮 |
| **挂失** | 借书证号输入 + 查询显示信息 + 挂失确认按钮 |
| **补办** | 原证号输入 + 查询显示信息 + 新证号输入 + 补办按钮 |
| **注销** | 借书证号输入 + 查询显示信息 + 注销确认按钮 |

**底部：操作记录查询**
- 输入学号 + 查询 → `el-table` 展示全部操作记录

#### 页面结构模板

```vue
<template>
  <div class="page-container">
    <!-- 权限拦截：非管理员显示无权限提示 -->
    <template v-if="isAdmin">
      <h2 class="page-title">💳 借书证管理</h2>
      <!-- ...正常内容... -->
    </template>
    <template v-else>
      <el-result icon="warning" title="无权限访问" sub-title="借书证管理仅限管理员操作，请联系管理员。">
        <template #extra>
          <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>
```

```javascript
// script setup 中引入角色判断
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.role === 'admin')

onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', { role: authStore.role, isAdmin: isAdmin.value })
  if (!isAdmin.value) {
    logger.error(COMPONENT, '⛔ 非管理员访问被拦截', { role: authStore.role })
    ElMessage.error('无权限访问该页面')
    return
  }
  // ...正常初始化逻辑
})
```

#### 接口对接

> ⚠️ **重要：** 实际后端接口与设计文档中的约定存在差异，前端需按以下实际接口对接。借书证管理涉及四个敏感操作（新办、挂失、补办、注销），每个操作都必须有完整的操作日志和用户确认记录。

```javascript
import { createCard, getCardInfo, reportLoss, replaceCard, deleteCard, getCardRecords } from '@/api/modules/cards'
import logger from '@/utils/logger'

const COMPONENT = 'CardManageView > '

/**
 * 查询借书证信息（各 Tab 共用）
 * 
 * 日志流水：
 *   ① [操作] CardManageView > queryCard ▶ 进入 → { cardNo, fromTab }
 *   ② [网络] Axios > GET /api/cards/{cardNo} 📤 请求
 *   ③ [操作] CardManageView > queryCard ✅ 成功 → { sno, status }
 */
async function queryCard() {
  logger.operation(`${COMPONENT}queryCard`, '▶ 进入', {
    cardNo: cardNo.value,
    fromTab: activeTab.value,
  })
  try {
    const res = await getCardInfo(cardNo.value)
    cardInfo.value = res.data
    // res.data = Libcard = { cardNo, sno, status }
    const statusMap = { 0: '注销', 1: '正常', 2: '挂失', 3: '作废' }
    logger.operation(`${COMPONENT}queryCard`, '✅ 成功', {
      cardNo: res.data?.cardNo,
      sno: res.data?.sno,
      status: res.data?.status,
      statusText: statusMap[res.data?.status] || '未知',
    })
  } catch (e) {
    logger.error(`${COMPONENT}queryCard`, '❌ 查询失败', { cardNo: cardNo.value })
  }
}

/**
 * 新办借书证
 * 
 * 日志流水：
 *   ① [操作] CardManageView > handleCreate ▶ 进入 → { sno, studentName }
 *   ② [校验] CardManageView > handleCreate ⏳ 表单校验
 *   ③ [网络] Axios > POST /api/cards 📤 请求 → { CardCreateDTO }
 *   ④ [操作] CardManageView > handleCreate ✅ 成功 → { cardNo, sno }
 */
async function handleCreate() {
  // ─── 步骤 ① ───
  logger.operation(`${COMPONENT}handleCreate`, '▶ 进入', {
    sno: form.value.sno,
    studentName: form.value.studentName,
    studentType: form.value.studentType === 1 ? '本科生' : '研究生',
  })

  // ─── 步骤 ② ───
  let valid = false
  try {
    valid = await formRef.value.validate()
  } catch (e) { /* ignore */ }
  if (!valid) {
    logger.validate(`${COMPONENT}handleCreate`, '❌ 表单校验失败')
    return
  }

  try {
    // CardCreateDTO = { sno, studentName, sex, studentType, college, major, phone, cardNo, password }
    await createCard(form.value)

    logger.operation(`${COMPONENT}handleCreate`, '✅ 成功', {
      cardNo: form.value.cardNo,
      sno: form.value.sno,
      studentName: form.value.studentName,
    })
    ElMessage.success('新办成功')
    // 重置表单
    form.value = { ...initialForm }
  } catch (e) {
    logger.error(`${COMPONENT}handleCreate`, '❌ 新办失败', { sno: form.value.sno })
  }
}

/**
 * 挂失借书证（需二次确认）
 * 
 * 日志流水：
 *   ① [操作] CardManageView > handleLoss ▶ 进入 → { cardNo }
 *   ② [校验] CardManageView > handleLoss ⏳ 用户确认
 *      - 用户点击确定 → ✅ 已确认
 *      - 用户点击取消 → ❌ 已取消
 *   ③ [网络] Axios > PUT /api/cards/{cardNo}/loss 📤 请求
 *   ④ [操作] CardManageView > handleLoss ✅ 成功 → { cardNo }
 */
async function handleLoss() {
  logger.operation(`${COMPONENT}handleLoss`, '▶ 进入', { cardNo: cardNo.value })

  // ─── 步骤 ②：二次确认 ───
  try {
    await ElMessageBox.confirm('确认挂失该借书证？挂失后该证将不可借书')
    logger.operation(`${COMPONENT}handleLoss`, '⏳ 用户已确认', { cardNo: cardNo.value })
  } catch {
    logger.operation(`${COMPONENT}handleLoss`, '❌ 用户取消操作')
    return
  }

  try {
    await reportLoss(cardNo.value)
    logger.operation(`${COMPONENT}handleLoss`, '✅ 成功', { cardNo: cardNo.value })
    ElMessage.success('挂失成功')
    cardInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleLoss`, '❌ 挂失失败', { cardNo: cardNo.value })
  }
}

/**
 * 补办借书证（需二次确认）
 * 
 * 日志流水：
 *   ① [操作] CardManageView > handleReplace ▶ 进入 → { originCardNo, newCardNo }
 *   ② [校验] CardManageView > handleReplace ⏳ 用户确认
 *   ③ [网络] Axios > PUT /api/cards/{cardNo}/replace 📤 请求 → { newCardNo }
 *   ④ [操作] CardManageView > handleReplace ✅ 成功 → { originCardNo, newCardNo }
 */
async function handleReplace() {
  logger.operation(`${COMPONENT}handleReplace`, '▶ 进入', {
    originCardNo: cardNo.value,
    newCardNo: newCardNo.value,
  })

  try {
    await ElMessageBox.confirm('确认补办？原借书证将作废，生成新证')
    logger.operation(`${COMPONENT}handleReplace`, '⏳ 用户已确认')
  } catch {
    logger.operation(`${COMPONENT}handleReplace`, '❌ 用户取消操作')
    return
  }

  try {
    await replaceCard(cardNo.value, { newCardNo: newCardNo.value })
    logger.operation(`${COMPONENT}handleReplace`, '✅ 成功', {
      originCardNo: cardNo.value,
      newCardNo: newCardNo.value,
    })
    ElMessage.success('补办成功')
    cardInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleReplace`, '❌ 补办失败', {
      originCardNo: cardNo.value,
      newCardNo: newCardNo.value,
    })
  }
}

/**
 * 注销借书证（需二次确认）
 * 
 * 日志流水：
 *   ① [操作] CardManageView > handleCancel ▶ 进入 → { cardNo }
 *   ② [校验] CardManageView > handleCancel ⏳ 用户确认
 *   ③ [网络] Axios > DELETE /api/cards/{cardNo} 📤 请求
 *   ④ [操作] CardManageView > handleCancel ✅ 成功 → { cardNo }
 */
async function handleCancel() {
  logger.operation(`${COMPONENT}handleCancel`, '▶ 进入', { cardNo: cardNo.value })

  try {
    await ElMessageBox.confirm('确认注销该借书证？注销后该证将永久不可使用')
    logger.operation(`${COMPONENT}handleCancel`, '⏳ 用户已确认')
  } catch {
    logger.operation(`${COMPONENT}handleCancel`, '❌ 用户取消操作')
    return
  }

  try {
    await deleteCard(cardNo.value)
    logger.operation(`${COMPONENT}handleCancel`, '✅ 成功', { cardNo: cardNo.value })
    ElMessage.success('注销成功')
    cardInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleCancel`, '❌ 注销失败', { cardNo: cardNo.value })
  }
}

/**
 * 查询操作记录
 * 
 * 日志流水：
 *   ① [操作] CardManageView > queryRecords ▶ 进入 → { sno }
 *   ② [网络] Axios > GET /api/cards/records 📤 请求 → { sno }
 *   ③ [操作] CardManageView > queryRecords ✅ 成功 → { recordCount, records }
 */
async function queryRecords() {
  logger.operation(`${COMPONENT}queryRecords`, '▶ 进入', { sno: sno.value })

  try {
    const res = await getCardRecords(sno.value)
    records.value = res.data || []
    // res.data = Cardrec[] = [{ serNum, sno, originCardNo, newCardNo, opType, opTime }]

    const opTypeMap = { 1: '新办', 2: '挂失', 3: '补办', 4: '注销' }
    logger.operation(`${COMPONENT}queryRecords`, '✅ 成功', {
      sno: sno.value,
      recordCount: records.value.length,
      operations: records.value.map(r => ({
        type: opTypeMap[r.opType] || `未知(${r.opType})`,
        time: r.opTime,
      })),
    })
  } catch (e) {
    logger.error(`${COMPONENT}queryRecords`, '❌ 查询失败', { sno: sno.value })
  }
}
```

#### 操作类型显示

| opType | 操作名称 | 对应操作 |
| ------ | -------- | -------- |
| 1 | 新办 | 新办 |
| 2 | 挂失 | 挂失 |
| 3 | 补办 | 补办 |
| 4 | 注销 | 注销 |

### 6.8 图书采编页 — `BookEditView.vue`

> ⚠️ **角色权限说明：** 图书采编为**管理员专用页面**（新书入库、副本新增、图书下架、报废均为馆藏管理操作），学生无权限访问。和借书证管理页一样，需要三重防护：侧边栏菜单过滤 + 路由守卫角色检查 + 页面组件内部角色判断。

#### 页面设计

- **权限拦截层**：使用 `v-if="isAdmin"` 控制页面内容渲染，非管理员显示 `el-result` 无权限提示卡片

```vue
<template>
  <div class="page-container">
    <template v-if="isAdmin">
      <h2 class="page-title">✏️ 图书采编</h2>
      <!-- 正常内容：el-tabs 切换四个操作区域 -->
    </template>
    <template v-else>
      <el-result icon="warning" title="无权限访问" sub-title="图书采编仅限管理员操作，请联系管理员。">
        <template #extra>
          <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
        </template>
      </el-result>
    </template>
  </div>
</template>
```

```javascript
// script setup 中引入角色判断
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const isAdmin = computed(() => authStore.role === 'admin')

onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', { role: authStore.role, isAdmin: isAdmin.value })
  if (!isAdmin.value) {
    logger.error(COMPONENT, '⛔ 非管理员访问被拦截', { role: authStore.role })
    ElMessage.error('无权限访问该页面')
    return
  }
  // ...正常初始化逻辑
})
```

管理员可见的操作区域（使用 `el-tabs` 切换）：

| Tab 名称 | 内容 |
| -------- | ---- |
| **新书入库** | 表单（ISBN、书名、作者、出版社、出版日期、分类、简介）+ 副本列表动态添加（条码 + 馆藏地点为一行，可新增多行） |
| **副本新增** | 输入 ISBN 查图书信息 + 是否为新书选择 + 动态添加副本行 |
| **图书下架** | 输入 ISBN 查询 + 显示图书信息和副本数量 + 下架确认 |
| **报废** | 输入副本条码查询 + 显示副本信息 + 报废确认 |

#### 接口对接

> ⚠️ **重要：** 实际后端接口与设计文档存在差异，前端需按以下实际接口对接。
>
> **日志要求：** 图书采编涉及四种不同操作，每个操作需记录完整的操作前后对比（如入库前 ISBN 和入库后副本数量）。

```javascript
import { getBookDetail, acquireBook, discardCopy, offShelfBook } from '@/api/modules/books'
import logger from '@/utils/logger'

const COMPONENT = 'BookEditView > '

/**
 * 新书入库 / 新增副本
 * 
 * 日志流水：
 *   ① [操作] BookEditView > handleAcquire ▶ 进入 → { ISBN, bookName, copiesCount }
 *   ② [校验] BookEditView > handleAcquire ⏳ 表单校验
 *   ③ [网络] Axios > POST /api/books/acquire 📤 请求 → { BookAcquireDTO }
 *   ④ [操作] BookEditView > handleAcquire ✅ 成功 → { ISBN, copies: [...] }
 */
async function handleAcquire() {
  // ─── 步骤 ① ───
  logger.operation(`${COMPONENT}handleAcquire`, '▶ 进入', {
    ISBN: acquireForm.value.ISBN,
    bookName: acquireForm.value.bookName,
    copiesCount: acquireForm.value.copies?.length || 0,
    copies: acquireForm.value.copies?.map(c => ({ barCode: c.barCode, place: c.place })),
  })

  // ─── 步骤 ② ───
  let valid = false
  try {
    valid = await formRef.value.validate()
  } catch (e) { /* ignore */ }
  if (!valid) {
    logger.validate(`${COMPONENT}handleAcquire`, '❌ 表单校验失败')
    return
  }

  try {
    // BookAcquireDTO = {
    //   ISBN, bookName, author, publisher, pubDate, introduction, category,
    //   copies: [{ barCode, place }, ...]
    // }
    await acquireBook(acquireForm.value)

    logger.operation(`${COMPONENT}handleAcquire`, '✅ 成功', {
      ISBN: acquireForm.value.ISBN,
      bookName: acquireForm.value.bookName,
      addedCopies: acquireForm.value.copies?.length || 0,
      barCodes: acquireForm.value.copies?.map(c => c.barCode),
    })
    ElMessage.success(`入库成功，新增 ${acquireForm.value.copies?.length || 0} 个副本`)
    // 重置表单
    acquireForm.value = { ...initialAcquireForm }
  } catch (e) {
    logger.error(`${COMPONENT}handleAcquire`, '❌ 入库失败', {
      ISBN: acquireForm.value.ISBN,
      bookName: acquireForm.value.bookName,
    })
  }
}

/**
 * 整书下架
 * 
 * 日志流水：
 *   ① [操作] BookEditView > handleOffShelf ▶ 进入 → { ISBN }
 *   ② [校验] BookEditView > handleOffShelf ⏳ 用户确认
 *   ③ [网络] Axios > PUT /api/books/{isbn}/off-shelf 📤 请求
 *   ④ [操作] BookEditView > handleOffShelf ✅ 成功 → { ISBN }
 */
async function handleOffShelf() {
  const isbn = isbnOffShelf.value
  logger.operation(`${COMPONENT}handleOffShelf`, '▶ 进入', { ISBN: isbn })

  if (!isbn) {
    logger.validate(`${COMPONENT}handleOffShelf`, '❌ ISBN 为空')
    ElMessage.warning('请输入 ISBN')
    return
  }

  // ─── 步骤 ② ───
  try {
    await ElMessageBox.confirm('确认下架该书全部副本？下架后所有副本状态将变为"下架"')
    logger.operation(`${COMPONENT}handleOffShelf`, '⏳ 用户已确认', { ISBN: isbn })
  } catch {
    logger.operation(`${COMPONENT}handleOffShelf`, '❌ 用户取消操作')
    return
  }

  try {
    await offShelfBook(isbn)
    logger.operation(`${COMPONENT}handleOffShelf`, '✅ 成功', { ISBN: isbn })
    ElMessage.success('下架成功')
    isbnOffShelf.value = ''
    bookOffShelfInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleOffShelf`, '❌ 下架失败', { ISBN: isbn })
  }
}

/**
 * 报废单个副本
 * 
 * 日志流水：
 *   ① [操作] BookEditView > handleDiscard ▶ 进入 → { barCode }
 *   ② [校验] BookEditView > handleDiscard ⏳ 用户确认
 *   ③ [网络] Axios > DELETE /api/books/copies/{barCode} 📤 请求
 *   ④ [操作] BookEditView > handleDiscard ✅ 成功 → { barCode }
 */
async function handleDiscard() {
  const barCode = discardBarCode.value
  logger.operation(`${COMPONENT}handleDiscard`, '▶ 进入', { barCode })

  if (!barCode) {
    logger.validate(`${COMPONENT}handleDiscard`, '❌ 条码为空')
    ElMessage.warning('请输入副本条码')
    return
  }

  try {
    await ElMessageBox.confirm('确认报废该副本？报废后该副本将不可借出')
    logger.operation(`${COMPONENT}handleDiscard`, '⏳ 用户已确认', { barCode })
  } catch {
    logger.operation(`${COMPONENT}handleDiscard`, '❌ 用户取消操作')
    return
  }

  try {
    await discardCopy(barCode)
    logger.operation(`${COMPONENT}handleDiscard`, '✅ 成功', { barCode })
    ElMessage.success('报废成功')
    discardBarCode.value = ''
    copyDiscardInfo.value = null
  } catch (e) {
    logger.error(`${COMPONENT}handleDiscard`, '❌ 报废失败', { barCode })
  }
}
```

### 6.9 罚款管理页 — `FineManageView.vue`

#### 页面设计

- **查询区**：`el-radio-group` 切换 **未缴(0)** / **已缴(1)** / **全部("")**
- **罚款列表**：`el-table` 展示，支持多选（`@selection-change`）
  - 列：罚款编号、借书证号、学号、学生姓名、图书条码、书名、罚款金额、产生时间、状态
  - 已缴记录不显示选择框
- **缴纳弹窗**：`el-dialog` 包含选中汇总金额 + `el-input` 操作员 + `el-input` 备注 + 确认缴纳按钮

#### 接口对接

> ⚠️ **注意：** 实际后端的罚款查询接口 `GET /api/fines` 仅支持 `payStatus` 参数，不支持按 `cardNo` 或 `sno` 筛选。若需要按学生查询，建议后端补充或前端在获取全部数据后本地过滤。
>
> **日志要求：** 罚款管理涉及罚款金额，日志必须详细记录每条罚款的金额、状态以及缴纳操作中的合并明细和汇总金额。

```javascript
import { getFines, payFines } from '@/api/modules/fines'
import logger from '@/utils/logger'

const COMPONENT = 'FineManageView > '

/**
 * 查询罚款列表
 * 
 * 日志流水：
 *   ① [渲染] FineManageView > loadFines ▶ 进入（Tab切换或手动刷新）
 *   ② [操作] FineManageView > loadFines ⏳ 参数 → { payStatus }
 *   ③ [网络] Axios > GET /api/fines 📤 请求 → { payStatus }
 *   ④ [网络] Axios > GET /api/fines 📥 响应 → { 耗时ms, 条数 }
 *   ⑤ [渲染] FineManageView > loadFines 🔚 结束 → { total, unpaid, paid }
 */
async function loadFines() {
  logger.render(`${COMPONENT}loadFines`, '▶ 进入', {
    triggeredBy: 'onMounted/Tab切换',
    payStatus: payStatus.value || '全部',
  })

  const params = {}
  if (payStatus.value !== '') {
    params.payStatus = payStatus.value  // 0=未缴 1=已缴
  }

  logger.operation(`${COMPONENT}loadFines`, '⏳ 查询参数', params)

  try {
    const res = await getFines(params)
    fineList.value = res.data || []
    // res.data = FineVO[] = [{ fineId, recNo, barCode, bookName, amount, payStatus, createTime, overdueDays }]

    const unpaid = fineList.value.filter(f => f.payStatus === 0)
    const paid = fineList.value.filter(f => f.payStatus === 1)
    const totalAmount = unpaid.reduce((sum, f) => sum + (f.amount || 0), 0)

    logger.render(`${COMPONENT}loadFines`, '🔚 结束', {
      totalCount: fineList.value.length,
      unpaidCount: unpaid.length,
      paidCount: paid.length,
      unpaidTotalAmount: totalAmount,
      averageAmount: fineList.value.length > 0
        ? (fineList.value.reduce((s, f) => s + (f.amount || 0), 0) / fineList.value.length).toFixed(2)
        : 0,
    })
  } catch (e) {
    logger.error(`${COMPONENT}loadFines`, '❌ 查询失败', { payStatus: payStatus.value })
  }
}

/**
 * 缴纳罚款（支持合并缴纳）
 * 
 * 日志流水：
 *   ① [操作] FineManageView > handlePay ▶ 进入 → { fineIds[], count, totalAmount }
 *   ② [校验] FineManageView > handlePay ⏳ 选择检查
 *   ③ [网络] Axios > POST /api/fines/pay 📤 请求 → { FinePayDTO }
 *   ④ [网络] Axios > POST /api/fines/pay 📥 响应 → { 耗时ms }
 *   ⑤ [操作] FineManageView > handlePay ✅ 成功 → { paymentId, amount, paidCount }
 *   ⑥ 若无选中 → [校验] FineManageView > handlePay ❌ 未选择罚款
 */
async function handlePay() {
  // ─── 步骤 ① ───
  const selectedIds = selectedFineIds.value
  const selectedFines = fineList.value.filter(f => selectedIds.includes(f.fineId))
  const totalAmount = selectedFines.reduce((sum, f) => sum + (f.amount || 0), 0)

  logger.operation(`${COMPONENT}handlePay`, '▶ 进入', {
    selectedCount: selectedIds.length,
    fineIds: selectedIds,
    totalAmount,
    details: selectedFines.map(f => ({
      fineId: f.fineId,
      bookName: f.bookName,
      amount: f.amount,
    })),
  })

  // ─── 步骤 ② ───
  if (selectedIds.length === 0) {
    logger.validate(`${COMPONENT}handlePay`, '❌ 未选择罚款记录')
    ElMessage.warning('请选择要缴纳的罚款记录')
    return
  }

  try {
    // FinePayDTO = { cardNo, fineIds, operator, remark }
    const res = await payFines({
      cardNo: cardNo.value,
      fineIds: selectedIds,
      operator: operator.value,
      remark: remark.value,
    })
    // 返回 FinePayment = { paymentId, cardNo, sno, paymentAmount, paymentTime, operator, remark }

    logger.operation(`${COMPONENT}handlePay`, '✅ 成功', {
      paymentId: res.data?.paymentId,
      paymentAmount: res.data?.paymentAmount,
      paidCount: selectedIds.length,
      cardNo: res.data?.cardNo,
      operator: operator.value || '未填写',
      remark: remark.value || '无备注',
    })

    ElMessage.success(`缴纳成功，共 ¥${totalAmount.toFixed(2)}`)
    // 重置选择状态
    selectedFineIds.value = []
    dialogVisible.value = false
    // 重新加载列表
    loadFines()
  } catch (e) {
    logger.error(`${COMPONENT}handlePay`, '❌ 缴纳失败', {
      fineIds: selectedIds,
      totalAmount,
    })
  }
}
```

### 6.10 个人查询页 — `PersonalView.vue`

#### 页面设计

使用 `el-tabs` 切换四个子页面：

| Tab 名称 | 数据 | 说明 |
| -------- | ---- | ---- |
| **当前在借** | `borrowings` | `el-table`：条码、书名、作者、借书日期、应还日期；超期红色标记 |
| **借书历史** | `history` | `el-table`：条码、书名、借书日期、应还日期、实际归还日期、状态 |
| **超期罚款** | `fines` (未缴) | `el-table`：罚款编号、图书条码、书名、超期天数、罚款金额、产生时间 |
| **已缴记录** | `payments` | `el-table`：缴纳编号、缴纳金额、缴纳时间、备注 |

#### 接口对接

> ⚠️ **重要：** 实际后端 `/api/me/*` 接口均需要 `sno` 参数，前端需从 `authStore.userInfo.sno` 获取。
>
> **日志要求：** 个人查询页有四个 Tab，每次切换 Tab 时触发对应数据加载。日志需记录当前 Tab、查询参数、数据条数以及超期标记情况。

```javascript
import { getMyBorrowings, getMyHistory, getMyFines, getMyPayments } from '@/api/modules/me'
import { useAuthStore } from '@/stores/auth'
import logger from '@/utils/logger'

const COMPONENT = 'PersonalView > '
const authStore = useAuthStore()
const sno = authStore.sno  // 从登录信息中获取学号

/**
 * 加载当前在借图书
 * 
 * 日志流水：
 *   ① [操作] PersonalView > loadBorrowings ▶ 进入（Tab切换到"当前在借"）
 *   ② [网络] Axios > GET /api/me/borrowings 📤 请求 → { sno }
 *   ③ [操作] PersonalView > loadBorrowings ✅ 成功 → { count, 超期数, 超期图书列表 }
 */
async function loadBorrowings() {
  logger.operation(`${COMPONENT}loadBorrowings`, '▶ 进入', {
    sno,
    tab: '当前在借',
    triggeredBy: 'Tab切换/onMounted',
  })

  try {
    const res = await getMyBorrowings(sno)
    borrowings.value = res.data || []
    // res.data = BorrowingVO[] = [{ recNo, barCode, bookName, ISBN, borDate, retDate, overdueDays, status }]

    const overdueItems = borrowings.value.filter(b => b.overdueDays > 0)
    logger.operation(`${COMPONENT}loadBorrowings`, '✅ 成功', {
      totalCount: borrowings.value.length,
      overdueCount: overdueItems.length,
      overdueBooks: overdueItems.map(b => ({
        bookName: b.bookName,
        barCode: b.barCode,
        overdueDays: b.overdueDays,
      })),
    })
  } catch (e) {
    logger.error(`${COMPONENT}loadBorrowings`, '❌ 加载失败', { sno })
  }
}

/**
 * 加载借书历史
 * 
 * 日志流水：
 *   ① [操作] PersonalView > loadHistory ▶ 进入（Tab切换到"借书历史"）
 *   ② [网络] Axios > GET /api/me/history 📤 请求 → { sno }
 *   ③ [操作] PersonalView > loadHistory ✅ 成功 → { total, 已还数, 未还数 }
 */
async function loadHistory() {
  logger.operation(`${COMPONENT}loadHistory`, '▶ 进入', {
    sno,
    tab: '借书历史',
    triggeredBy: 'Tab切换',
  })

  try {
    const res = await getMyHistory(sno)
    history.value = res.data || []
    // res.data = HistoryVO[] = [{ recNo, barCode, bookName, borDate, retDate, realRetDate, status }]

    const returned = history.value.filter(h => h.status === 0)
    const borrowed = history.value.filter(h => h.status === 1)
    logger.operation(`${COMPONENT}loadHistory`, '✅ 成功', {
      totalCount: history.value.length,
      returnedCount: returned.length,
      stillBorrowedCount: borrowed.length,
    })
  } catch (e) {
    logger.error(`${COMPONENT}loadHistory`, '❌ 加载失败', { sno })
  }
}

/**
 * 加载超期罚款（未缴）
 * 
 * 日志流水：
 *   ① [操作] PersonalView > loadFines ▶ 进入（Tab切换到"超期罚款"）
 *   ② [网络] Axios > GET /api/me/fines 📤 请求 → { sno }
 *   ③ [操作] PersonalView > loadFines ✅ 成功 → { count, 总金额, 明细 }
 */
async function loadFines() {
  logger.operation(`${COMPONENT}loadFines`, '▶ 进入', {
    sno,
    tab: '超期罚款',
    triggeredBy: 'Tab切换',
  })

  try {
    const res = await getMyFines(sno)
    const allFines = res.data || []
    // 筛选未缴罚款
    fines.value = allFines.filter(f => f.payStatus === 0)
    // res.data = FineVO[] = [{ fineId, recNo, barCode, bookName, amount, payStatus, createTime, overdueDays }]

    const totalAmount = fines.value.reduce((sum, f) => sum + (f.amount || 0), 0)
    logger.operation(`${COMPONENT}loadFines`, '✅ 成功', {
      unpaidCount: fines.value.length,
      totalUnpaidAmount: totalAmount,
      fineDetails: fines.value.map(f => ({
        fineId: f.fineId,
        bookName: f.bookName,
        amount: f.amount,
        overdueDays: f.overdueDays,
      })),
    })
  } catch (e) {
    logger.error(`${COMPONENT}loadFines`, '❌ 加载失败', { sno })
  }
}

/**
 * 加载已缴记录
 * 
 * 日志流水：
 *   ① [操作] PersonalView > loadPayments ▶ 进入（Tab切换到"已缴记录"）
 *   ② [网络] Axios > GET /api/me/payments 📤 请求 → { sno }
 *   ③ [操作] PersonalView > loadPayments ✅ 成功 → { count, 总金额, 明细 }
 */
async function loadPayments() {
  logger.operation(`${COMPONENT}loadPayments`, '▶ 进入', {
    sno,
    tab: '已缴记录',
    triggeredBy: 'Tab切换',
  })

  try {
    const res = await getMyPayments(sno)
    payments.value = res.data || []
    // res.data = FinePayment[] = [{ paymentId, cardNo, sno, paymentAmount, paymentTime, operator, remark }]

    const totalPaid = payments.value.reduce((sum, p) => sum + (p.paymentAmount || 0), 0)
    logger.operation(`${COMPONENT}loadPayments`, '✅ 成功', {
      paymentCount: payments.value.length,
      totalPaidAmount: totalPaid,
      latestPayment: payments.value.length > 0 ? {
        id: payments.value[0].paymentId,
        amount: payments.value[0].paymentAmount,
        time: payments.value[0].paymentTime,
      } : null,
    })
  } catch (e) {
    logger.error(`${COMPONENT}loadPayments`, '❌ 加载失败', { sno })
  }
}
```

#### 超期标记

在"当前在借"表格中，若 `overdueDays > 0`，该行文字显示为红色（`style="color: var(--color-danger)"`），并在应还日期后显示 `（超期 {overdueDays} 天）`。

---

## 7. API 对接规范

### 7.1 实际后端接口总表（与实际后端代码完全一致）

> ⚠️ **以下接口均基于实际后端 Controller 代码验证，与设计文档中的约定存在多处差异，请严格按此表对接。**

| 接口 | 方法 | 请求参数 | 响应 data | 调用页面 |
| ---- | ---- | -------- | --------- | -------- |
| `/api/login` | POST | `{ role, username, password }` | `LoginVO`（`{ role, username, sno, cardNo, displayName }`） | login |
| `/api/books/search?keyword=` | GET | `keyword` | `BookSearchVO[]`（含 copies 副本列表） | search |
| `/api/books/{isbn}` | GET | 路径参数 `isbn` | `BookSearchVO`（含 `copies: Bookcopy[]`） | search |
| `/api/books/acquire` | POST | `BookAcquireDTO`（含 `copies: [{barCode, place}]`） | — | edit-book |
| `/api/books/{isbn}/off-shelf` | PUT | 路径参数 `isbn` | — | edit-book |
| `/api/books/copies/{barCode}` | DELETE | 路径参数 `barCode` | — | edit-book |
| `/api/borrow` | POST | `{ cardNo, barCodes }` | `{ recNos[], dueDate, count }` | borrow |
| `/api/return` | POST | `{ barCode }` | `{ recNo, overdue, overdueDays?, fineAmount?, fineId? }` | return |
| `/api/cards` | POST | `CardCreateDTO` | — | card |
| `/api/cards/{cardNo}` | GET | 路径参数 `cardNo` | `Libcard`（`{ cardNo, sno, status }`） | card / borrow |
| `/api/cards/{cardNo}/loss` | PUT | 路径参数 `cardNo` | — | card |
| `/api/cards/{cardNo}/replace` | PUT | 路径参数 + `{ newCardNo }` | — | card |
| `/api/cards/{cardNo}` | DELETE | 路径参数 | — | card |
| `/api/cards/records?sno=` | GET | `sno` | `Cardrec[]` | card |
| `/api/fines?payStatus=` | GET | `payStatus`(可选 0/1) | `FineVO[]` | fine |
| `/api/fines/pay` | POST | `FinePayDTO` | `FinePayment` | fine |
| `/api/me/borrowings?sno=` | GET | `sno` | `BorrowingVO[]` | personal |
| `/api/me/history?sno=` | GET | `sno` | `HistoryVO[]` | personal |
| `/api/me/fines?sno=` | GET | `sno` | `FineVO[]` | personal |
| `/api/me/payments?sno=` | GET | `sno` | `FinePayment[]` | personal |
| `/api/dashboard/stats` | GET | — | `DashboardStatsVO` | index |
| `/api/dashboard/rankings` | GET | — | `RankingVO`（`{ studentRankings, bookRankings }`） | index |

### 7.2 接口差异说明（⚠️ 关键）

以下列出**实际后端接口**与**设计文档约定**之间的差异，前端开发时务必注意：

| 设计文档约定 | 实际后端接口 | 影响页面 |
| ------------ | ------------ | -------- |
| `POST /api/books`（新书入库） | `POST /api/books/acquire` | edit-book |
| 入参含 `copyCount`（数量） | 入参含 `copies[{barCode, place}]`（副本明细列表） | edit-book |
| `PUT /api/books/{isbn}/remove` | `PUT /api/books/{isbn}/off-shelf` | edit-book |
| `PUT /api/books/copy/{barCode}/scrap` | `DELETE /api/books/copies/{barCode}` | edit-book |
| `PUT /api/cards/{cardNo}`（opType区分） | `PUT /api/cards/{cardNo}/loss` 和 `/replace` 两个接口 | card |
| `GET /api/books/copies?isbn=` | `GET /api/books/{isbn}`（返回含 copies） | search |
| 个人接口无参数 | 个人接口均需要 `sno` 参数 | personal |
| `GET /api/fines?payStatus&cardNo&sno` | 仅支持 `payStatus` | fine |
| 罚款返回含 `cardNo, sno, studentName` | `FineVO` 不含这些字段 | fine |
| 登录返回 `{ token, userInfo }` | `LoginVO` 不含 token → 系统无 JWT 认证 | login |
| 借书返回 `{ successCount, failCount, results }` | 返回 `{ recNos[], dueDate, count }` → 前端需调整展示方式 | borrow |
| 还书返回 `{ barCode, bookName, borDate, retDate, overdue, fine }` | 返回 `{ recNo, overdue, overdueDays?, fineAmount?, fineId? }` → 不含书名/日期字段 | return |
| `GET /api/cards/{cardNo}` 返回扩展学生信息 | 仅返回 `Libcard { cardNo, sno, status }` 四个字段 | borrow |

### 7.3 缺失接口说明

实际后端中**缺少**以下接口，建议与后端协商补充：

1. **`GET /api/books/copy?barCode=`** — 按条码查询单个副本信息（借书页需要根据条码获取书名等信息）
2. **`GET /api/fines?cardNo=&sno=&startDate=&endDate=`** — 按更多条件筛选罚款（当前仅支持 payStatus）

若后端无法补充，前端可采取以下替代方案：
- 借书页：先通过 `GET /api/books/{isbn}` 获取图书的所有副本，再在前端按 barCode 筛选
- 罚款管理：仅在获取全部数据后在前端做本地过滤

### 7.4 统一返回格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

- `code === 0` 表示成功
- `code !== 0` 表示失败，`message` 包含错误描述
- `data` 为具体的业务数据（有可能为 null）

---

## 8. 安全规范

### 8.1 XSS 防护
- 使用 Vue 模板语法 `{{ }}` 自动转义，禁止使用 `v-html` 渲染用户输入
- 如需使用 `v-html`，必须通过 DOMPurify 等工具对内容进行消毒

### 8.2 登录状态管理
- 登录状态存储于 Pinia auth store 和 `localStorage`，key 为 `userInfo`
- **后端无 JWT Token 机制**，无需注入 Authorization 请求头
- 退出登录时清除 `userInfo` 并跳转登录页

### 8.3 敏感信息保护
- 密码输入框使用 `type="password"` 并配合 `el-input show-password`
- 前端日志禁止打印密码等敏感信息
- 不在 URL 中传递敏感参数

---

## 9. 项目开发与验证

### 9.1 启动步骤

```bash
# 安装依赖
npm install

# 启动开发服务器（Vite 代理自动转发 /api 到后端 8080）
npm run dev
```

### 9.2 前后端联调要点

1. 确保后端 Spring Boot 应用已启动（默认端口 8080）
2. Vite 代理配置已将 `/api` 转发到 `http://localhost:8080`
3. 首次登录使用管理员账号（由后端初始化脚本创建）
4. 浏览器访问 `http://localhost:3000` 即可进入系统

### 9.3 自验证清单

生成完成后，请逐项确认：

- [ ] `npm run dev` 能否正常启动？
- [ ] 登录页是否能正常切换管理员/学生模式？
- [ ] 首页统计卡片和排行榜数据是否正常显示？
- [ ] 图书检索能否按关键词搜索并查看详情？
- [ ] 借书流程：输入证号 → 添加条码 → 确认借出，是否完整？
- [ ] 还书流程：输入条码 → 显示结果（含超期信息），是否正常？
- [ ] 借书证管理四个操作（新办/挂失/补办/注销）是否可用？
- [ ] 图书采编四个操作（入库/新增副本/下架/报废）是否可用？
- [ ] 罚款管理能否按状态筛选并合并缴纳？
- [ ] 个人查询四个 Tab 是否都能正常加载数据？
- [ ] **路由守卫**是否能在未登录时正确跳转？
- [ ] **路由守卫**是否能正确拦截角色不匹配的访问并重定向？
- [ ] **侧边栏**：学生登录后是否看不到"借书证管理"和"图书采编"菜单项？
- [ ] **侧边栏**：管理员登录后是否能正常看到全部 8 个菜单项？
- [ ] **借书证管理**：学生通过直接 URL 访问 `/cards` 是否显示"无权限访问"提示？
- [ ] **图书采编**：学生通过直接 URL 访问 `/books/edit` 是否显示"无权限访问"提示？
- [ ] Axios 拦截器是否在请求失败时正确提示错误？
- [ ] 所有 API 是否与实际后端接口一致（参考 7.1 节接口总表）？

### 9.4 常见联调问题

| 问题 | 可能原因 | 排查方法 |
| ---- | -------- | -------- |
| 接口 404 | 接口路径与后端不符 | 检查浏览器 Network 请求 URL，参考 7.1 节纠正 |
| 登录后页面空白 | 路由守卫未正确跳转 | 检查 `authStore.isLoggedIn` 状态 |
| 表格数据为空 | 接口返回字段名与前端不匹配 | 对比 VO 类字段与前端变量名 |
| CORS 错误 | Vite 代理未配置 | 检查 `vite.config.js` 中的 proxy 配置 |
| Element Plus 组件无样式 | 按需导入配置缺失 | 检查 `unplugin-vue-components` 和 `unplugin-auto-import` |
| 学生能看到管理员菜单 | 侧边栏未做 `v-if="isAdmin"` 角色过滤 | 检查 `Sidebar.vue` 中菜单项的 `v-if` 条件 |
| 学生通过 URL 直接访问管理页 | 路由守卫缺少角色校验 | 检查 `router/index.js` 中 `beforeEach` 的角色检查逻辑 |

---

## 10. 最终交付

### 10.1 完成断言

```
[前端代码生成完成断言 — Vue 3 版]

✓ 项目脚手架：已配置（Vite + Vue 3 + Vue Router + Pinia + Element Plus + Axios）
  - vite.config.js（代理配置 /api → localhost:8080）
  - package.json（完整依赖）
  - index.html（入口）
  - src/main.js（挂载 Router、Pinia、Element Plus）
  - src/App.vue（<router-view /> 根组件）

✓ 基础设施层：已生成（9 个 API 模块 + 3 个样式文件 + 路由 + 状态管理）
  - API 封装：request.js（Axios 拦截器）
  - API 模块：auth / books / borrow / cards / fines / dashboard / me（7 个模块）
  - 样式系统：variables.css / common.css / table.css（CSS 变量体系）
  - 路由系统：10 条路由 + 导航守卫
  - 状态管理：Pinia auth store（userInfo + isLoggedIn）

✓ 布局与公共组件：已生成（2 个布局组件 + 3 个公共组件）
  - MainLayout.vue（el-container 经典后台布局）
  - Topbar.vue（顶部导航）
  - Sidebar.vue（el-menu 侧边栏导航）
  - StatusBadge.vue / ConfirmDialog.vue / Pagination.vue

✓ 页面视图：已生成（9 个 Vue SFC）
  - 登录页：管理员/学生双模式 Tab 切换
  - 首页：统计卡片 + 学生/图书排行榜
  - 图书检索：搜索 + 详情抽屉 + 副本馆藏列表
  - 借书：证号查询 + 动态条码录入 + 批量借出
  - 还书：条码扫描 + 超期判断 + 罚款提示
  - 借书证管理：新办/挂失/补办/注销 Tab + 记录查询
  - 图书采编：入库/下架/报废 Tab（adapt to actual APIs）
  - 罚款管理：状态筛选 + 多选合并缴纳
  - 个人查询：在借/历史/罚款/已缴四 Tab

✓ API 对接：已覆盖全部 22 个实际后端接口
  - 严格按后端 Controller 代码确认接口路径和参数
  - 标注了与实际不一致的接口差异（7.2 节）
  - 标注了缺失接口及替代方案（7.3 节）

✓ 样式规范：已遵循设计文档配色方案（#1E3A8A / #2563EB / #F3F4F6）
✓ 安全规范：Vue 自动 XSS 防护 + Token 管理 + 敏感信息保护
✓ 权限控制：基于角色的三重访问控制（侧边栏过滤 + 路由守卫拦截 + 页面组件拦截）
  - 侧边栏：`v-if="isAdmin"` 控制借书证管理和图书采编菜单项仅管理员可见
  - 路由守卫：`to.meta.roles` 校验角色，非授权角色重定向并提示
  - 页面组件：`v-if="isAdmin"` + `onMounted` 角色检查，双重保障
✓ 错误处理：Axios 拦截器统一处理 + 组件级 try-catch

[重要提示]
- 本项目基于 Vue 3 + Vite + Element Plus 构建
- 所有 API 路径以实际后端代码为准（见 7.1 节）
- 存在 7.2 节列出的接口差异，联调时请逐一核实
- 使用 `npm run dev` 启动开发服务器（端口 3000）
- 确保后端 Spring Boot 运行在 8080 端口
```

---

**[开始生成指令]**

请严格按照本文档要求，从前端项目脚手架开始，依次生成全部文件。每个文件必须包含完整代码，无省略、无占位符，可直接运行。生成顺序：

1. 项目配置文件（`package.json`、`vite.config.js`）
2. 入口文件（`index.html`、`src/main.js`、`src/App.vue`）
3. 基础设施（路由、状态管理、API 封装）
4. API 模块（7 个）
5. 样式文件（3 个）
6. 布局与公共组件（5 个）
7. 页面视图（9 个）

---

---

## 教学团队与指导老师锐评（基于数据库 + 后端代码审查）

> 以下评价基于实际审查的以下材料：
> - 11 张数据库表定义 + 测试数据（成员 A）
> - 7 个 Controller + 完整 Service 层 + MyBatis Mapper XML（成员 B）
> - 前端 Vue 提示词（本文档）

### 先说结论

前端提示词与前两阶段成果（数据库 + 后端）之间存在 **多处严重的脱节**，有些问题大到会让答辩现场翻车。好消息是，大部分问题可以通过认真读一遍后端的 Mapper XML 和 Service 代码来避免——而这份提示词显然没有做到这一点。

---

### 一、数据库设计质量：8/10（这个值得先肯定）

先给成员 A 点个赞：
- 11 张表，外键约束全齐，索引该加的地方都加了
- 状态码设计清晰（4 种副本状态、4 种证件状态、4 种操作类型）
- 测试数据覆盖了**所有边缘场景**：正常、挂失、注销、作废的证件；可借、借出、下架、报废的副本；超期记录、合并缴纳罚款
- 学号编码 `YYYY T MM NNN` 是有思考的

**这些东西前端提示词基本没利用上**，后面细说。

---

### 二、跨阶段脱节分析（这是重点）

#### ~~🔴 致命问题 1：借书页的条码查询根本走不通~~ ❌ 此条误判，撤回

经重新审查后端 `BorrowService.borrow()` 代码后确认：

```java
for (String barCode : dto.getBarCodes()) {
    Bookcopy copy = bookcopyMapper.findByBarCode(barCode);  // 直接按 barCode 查
    ...
    Book book = bookMapper.findByIsbn(copy.getBookISBN());  // ISBN 是内部获取的
    ...
}
```

**后端借书流程只依赖条码本身**，不需要前端提取 ISBN：
1. 前端提交 `{cardNo: "C2024001", barCodes: ["BC-2024001", "BC-2024002"]}`
2. 后端 `bookcopyMapper.findByBarCode("BC-2024001")` 直接按条码查副本
3. ISBN 是在 Service 层通过 `copy.getBookISBN()` 内部获取的，前端无需关心

数据库设计本身是合理的：`bookcopy` 表以 `barCode` 做主键，`borrowrec` 外键直接引用 `bookcopy.barCode`，条码不需要编码 ISBN。借书功能**可以正常工作**。

**但提示词中 `queryCopy` 函数的实现确实有问题**——它试图从条码提取 ISBN 并调用 `getBookDetail(isbn)`，这个逻辑是多余的。提示词应改为：直接收集条码列表，提交借书请求即可，不需要在录入时查书名。待借列表可以只显示条码，借书成功后后端返回的 `dueDate` 和 `count` 足以提示用户操作结果。

#### 🔴 致命问题 2：查了证号依然不知道借书人是谁

看后端 `LibcardMapper.xml`：

```sql
SELECT cardNo, sno, password, status FROM libcard WHERE cardNo = #{cardNo}
```

返回只有 `cardNo, sno, password, status` ——没有学生姓名、没有学生类型、没有当前借阅数。

前端提示词在借书页设计中说：

> 显示证件信息（证号、学号、状态）

然后就没有然后了。**借书员在界面上只能看到 C2024001 和 2024101001 这样的编号，看不到学生姓名。** 这合理吗？你让管理员对着一个学号去猜这是谁？

**解决方案**：借书时应该调用两个接口：
1. `GET /api/cards/{cardNo}` → 拿 sno
2. `GET /api/me/borrowings?sno={sno}` → 拿当前借阅列表（BorrowingVO 包含 bookName）

这样既能显示学生姓名（从借阅记录反推），又能算出当前借阅数。但提示词里完全没有提到这个方案。

#### 🟡 严重问题 3：还书结果不返回书名，前端怎么展示？

后端 `BorrowService.returnBook` 返回：

```java
result.put("recNo", rec.getRecNo());
result.put("overdue", true/false);
result.put("overdueDays", days);
result.put("fineAmount", amount);
result.put("fineId", fine.getFineId());
```

没有 `bookName`，没有 `barCode`，没有借书日期。

前端提示词目前展示的是：

```
detail: { recNo: ..., hasOverdue: ..., overdueDays: ..., fineAmount: ..., fineId: ... }
```

用户看到的是"还书成功，超期 5 天，罚款 ¥5.00"——**但用户不知道还的是哪本书！** 图书管理员扫完条码只知道"还成功了"，但界面不显示书名，这是一个基本的 UX 缺陷。

**建议**：还书后在前端发起一个 `GET /api/books/copies/{barCode}` 的查询（实际后端有这个接口吗？没有。那就需要后端在 returnBook 的返回值里加入 `bookName`）。

#### 🟡 严重问题 4：FineVO 缺关键字段，罚款管理页展示不全

看 `FineMapper.xml` 的 `findAll`：

```sql
SELECT f.fineId, f.recNo, br.barCode, b.bookName, f.amount, f.payStatus, f.createTime,
       o.overdueDays, br.sno
```

虽然 SQL 里查了 `br.sno`，但 `FineVO.java` 里**没有 sno 字段**：

```java
public class FineVO {
    private Integer fineId; private Integer recNo; private String barCode;
    private String bookName; private BigDecimal amount; private Integer payStatus;
    private LocalDateTime createTime; private Integer overdueDays;
}
```

更没有 `cardNo` 和 `studentName`。

前端提示词中罚款列表的列设计是：

> 罚款编号、借书证号、学号、学生姓名、图书条码、书名、罚款金额、产生时间、状态

其中 **借书证号、学号、学生姓名** 三个字段后端根本不会返回！展示出来全是空的。

**更致命的是**：缴纳罚款的 `FinePayDTO` 要求必传 `cardNo`（`@NotBlank`），但罚款列表里没有 cardNo——管理员从界面上无法知道要填哪个 cardNo 才能缴纳。

#### 🟡 问题 5：借书接口不返回逐本结果

后端 `borrow` 方法在任何异常时直接抛 `BusinessException` 终止事务：

```java
for (String barCode : dto.getBarCodes()) {
    Bookcopy copy = bookcopyMapper.findByBarCode(barCode);
    if (copy == null) throw new BusinessException(...);  // 直接终止
    if (copy.getStatus() != 1) throw new BusinessException(...);  // 直接终止
    ...
}
```

这意味着借 5 本书，第 3 本失败时**全部失败**（事务回滚），不会出现"成功 2 本，失败 1 本"的情况。

前端提示词却设计了：

```javascript
logger.operation(`${COMPONENT}handleBorrow`, '✅ 成功`, { count: result.count, ... });
```

这倒是对的。但 JSDoc 里还残留着旧思路：

```
// 之前: { successCount, failCount, results }
// 已修正为: { recNos, dueDate, count }
```

这个修正对了。但问题是——如果中途有一本失败，**整个借书请求失败**，前端显示的是"网络错误"而不是"第 3 本失败"。用户体验差，但这是后端设计决定的，前端只能接受。

---

### 三、前端提示词本身的亮点（公平地说）

| 方向 | 评价 |
|------|------|
| **技术栈** | Vue 3 + Vite + Pinia + Element Plus，作为课程设计要求，不算过分 |
| **后端接口对齐意识** | 7.1 节的实际接口表和 7.2 节的差异对比确实花了功夫 |
| **日志系统** | `logger.js` 的完整性和规范度值得答辩时展示 |
| **API 模块化** | 按业务拆 7 个模块，结构规整 |

但这些亮点被上面那些"跨阶段脱节"问题严重拖累。

---



---

### 五、答辩预判：老师可能会问的问题

| 问题 | 你的应对 |
|------|---------|
| "扫了条码为什么查不到书名？" | 承认条码格式不含 ISBN，后端缺少按条码查详情的接口。说明这是两边的设计没有对齐，解释修复方案。**不要狡辩，直接承认。"我们在联调时发现了这个问题，计划是……"** |
| "缴纳罚款时怎么知道学生的 cardNo？" | 承认 FineVO 缺少 cardNo 字段。说明当前的交互方案是让管理员手动输入 cardNo，或者在后端补充该字段。 |
| "Vue 的日志系统有什么用？" | 这是你能拿分的地方。解释 logger.js 的设计——操作来源可辨、用户身份可溯、步骤粒度精细。演示 console 中的日志输出。 |
| "你们团队是怎么分工的？" | 成员 A 数据库，成员 B 后端，成员 C 前端页面，成员 D 前端脚本+联调。说明前端提示词是成员 D 写的。**如果被问到自己发现了哪些跨阶段问题，如实回答上面列出的那些。** |

---

**结论：6/10** — 数据库和后端工作扎实（8 分水平），前端提示词工作量大但存在多处与实际实现脱节的问题。如果答辩前能把 P0 和 P1 的问题解决，可以到 8 分。带着 P0 问题去答辩，大概率会被问到下不来台。

---

**[锐评结束]**
