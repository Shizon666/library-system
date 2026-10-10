// 认证状态管理：登录态持久化到 sessionStorage（关闭浏览器后失效），提供角色/学号等计算属性
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import logger from '@/utils/logger'

const USER_INFO_KEY = 'userInfo'

function readStoredUserInfo() {
  try {
    const raw = sessionStorage.getItem(USER_INFO_KEY) || localStorage.getItem(USER_INFO_KEY)
    if (localStorage.getItem(USER_INFO_KEY)) {
      // 迁移旧版 localStorage 登录态到 sessionStorage，避免长期自动登录
      sessionStorage.setItem(USER_INFO_KEY, localStorage.getItem(USER_INFO_KEY))
      localStorage.removeItem(USER_INFO_KEY)
    }
    return JSON.parse(raw || '{}')
  } catch {
    return {}
  }
}

export const useAuthStore = defineStore('auth', () => {
  const userInfo = ref(readStoredUserInfo())

  // ---- 计算属性：从 userInfo 派生常用字段 ----
  const isLoggedIn = computed(() => !!userInfo.value?.role)
  const displayName = computed(() => userInfo.value.displayName || '')
  const role = computed(() => userInfo.value.role || '')
  const sno = computed(() => userInfo.value.sno || '')
  const cardNo = computed(() => userInfo.value.cardNo || '')
  const username = computed(() => userInfo.value.username || '')

  function setLogin(data) {
    userInfo.value = data
    sessionStorage.setItem(USER_INFO_KEY, JSON.stringify(data))
    localStorage.removeItem(USER_INFO_KEY)
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
    sessionStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem(USER_INFO_KEY)
    logger.state('AuthStore > logout', '✅ 退出', { clearedUser: before })
  }

  return { userInfo, isLoggedIn, displayName, role, sno, cardNo, username, setLogin, logout }
})
