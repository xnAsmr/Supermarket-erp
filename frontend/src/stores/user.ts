import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '../types'
import { login as apiLogin, logout as apiLogout } from '../api/auth'
import {
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
  removeUser,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken
} from '../utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getUser<UserInfo>())
  const refreshToken = ref<string | null>(getRefreshToken())

  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username ?? '')
  const realName = computed(() => userInfo.value?.realName ?? '')
  const role = computed(() => userInfo.value?.role ?? '')

  async function login(params: { username: string; password: string }) {
    try {
      const result = await apiLogin(params)
      if (result && result.token) {
        token.value = result.token
        refreshToken.value = result.refreshToken
        userInfo.value = result.userInfo
        setToken(result.token)
        setRefreshToken(result.refreshToken)
        setUser(result.userInfo)
        return true
      }
      return false
    } catch {
      return false
    }
  }

  async function logout() {
    try {
      await apiLogout()
    } catch {
      // ignore logout error
    } finally {
      resetState()
    }
  }

  function resetState() {
    token.value = null
    refreshToken.value = null
    userInfo.value = null
    removeToken()
    removeRefreshToken()
    removeUser()
  }

  function setTokenValue(t: string) {
    token.value = t
    setToken(t)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    setUser(info)
  }

  return {
    token,
    userInfo,
    refreshToken,
    isLoggedIn,
    username,
    realName,
    role,
    login,
    logout,
    resetState,
    setTokenValue,
    setUserInfo
  }
})
