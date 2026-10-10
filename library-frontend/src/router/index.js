// 路由配置：meta.noAuth 控制公开路由，meta.roles 控制角色权限，导航守卫统一拦截
import { createRouter, createWebHashHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import logger from '@/utils/logger'

const routes = [
  // ── 公开路由（无需登录） ──────────────────────────
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/portal/HomePortalView.vue'),
        meta: { noAuth: true },
      },
      {
        path: 'borrow',
        name: 'Borrow',
        component: () => import('@/views/borrow/BorrowView.vue'),
        // 需要登录才能借书，未登录会跳转到登录页
      },
      {
        path: 'return',
        name: 'Return',
        component: () => import('@/views/return/ReturnView.vue'),
        // 需要登录才能还书，未登录会跳转到登录页
      },

      // ── 管理员专属 ────────────────────────────────
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'cards',
        name: 'CardManage',
        component: () => import('@/views/card/CardManageView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'books/catalog',
        name: 'BookCatalog',
        component: () => import('@/views/book/BookCatalogView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'books/add',
        name: 'BookAdd',
        component: () => import('@/views/book/BookFormView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'books/:isbn/edit',
        name: 'BookEditForm',
        component: () => import('@/views/book/BookFormView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'books/:isbn/copies',
        name: 'BookCopies',
        component: () => import('@/views/book/BookCopiesView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'notices/manage',
        name: 'NoticeManage',
        component: () => import('@/views/notice/NoticeManageView.vue'),
        meta: { roles: ['admin'] },
      },
      {
        path: 'books/edit',
        redirect: '/books/catalog',
      },

      // ── 已登录即可访问（管理员 + 学生） ──────────
      {
        path: 'books/search',
        name: 'BookSearch',
        component: () => import('@/views/book/BookSearchView.vue'),
      },
      {
        path: 'fines',
        name: 'FineManage',
        component: () => import('@/views/fine/FineManageView.vue'),
      },

      // ── 学生专属 ──────────────────────────────────
      {
        path: 'personal',
        name: 'Personal',
        component: () => import('@/views/personal/PersonalView.vue'),
        meta: { roles: ['student'] },
      },
    ],
  },
]

// 创建路由实例，使用 Hash 模式（兼容静态部署，无需后端配合 rewrite）
const router = createRouter({ history: createWebHashHistory(), routes })

/** 是否为从地址栏/外链进入站点（非页面内跳转、非 F5 刷新） */
function isFreshSiteVisit() {
  const nav = performance.getEntriesByType('navigation')[0]
  return nav?.type === 'navigate'
}

// 导航守卫：未登录访问需认证页面 → 跳登录页；角色不匹配 → 回首页
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 从地址栏打开 localhost:3000 进入门户首页时，展示游客态（清除上次残留的登录态）
  const isAppLaunch = from.matched.length === 0
  const isPortalHome = to.path === '/home' || to.path === '/'
  if (isAppLaunch && isPortalHome && isFreshSiteVisit()) {
    authStore.logout()
  }

  const isAuthenticated = authStore.isLoggedIn
  const requiresAuth = !to.meta?.noAuth
  const allowedRoles = to.meta?.roles

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

  if (requiresAuth && !isAuthenticated) {
    logger.navigation(`路由守卫 > ${to.name}`, '❌ 未登录，重定向到 /login', {
      redirect: to.fullPath,
    })
    next({ path: '/login', query: { redirect: to.fullPath, ...to.query } })
    return
  }

  if (isAuthenticated && allowedRoles && allowedRoles.length > 0) {
    if (!allowedRoles.includes(authStore.role)) {
      logger.navigation(`路由守卫 > ${to.name}`, '⛔ 角色不匹配，重定向到首页', {
        userRole: authStore.role,
        requiredRoles: allowedRoles,
      })
      ElMessage.error('无权限访问该页面')
      next({ path: authStore.role === 'admin' ? '/dashboard' : '/home' })
      return
    }
  }

  logger.navigation(`路由守卫 > ${to.name}`, '✅ 允许通过', { userRole: authStore.role })
  next()
})

// 后置钩子：每次导航完成后记录日志，便于调试路由跳转
router.afterEach((to) => {
  logger.navigation(`路由 > ${to.name}`, '✅ 导航完成', { path: to.path })
})

export default router
