import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getTheme, setTheme as saveTheme } from '../utils/storage'

export type ThemeMode = 'light' | 'dark'

export const useAppStore = defineStore('app', () => {
  const theme = ref<ThemeMode>(getTheme() as ThemeMode)
  const sidebarCollapsed = ref(false)
  const loading = ref(false)
  const fullscreenLoading = ref(false)

  function setTheme(t: ThemeMode) {
    theme.value = t
    saveTheme(t)
  }

  function toggleTheme() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setLoading(val: boolean) {
    loading.value = val
  }

  function setFullscreenLoading(val: boolean) {
    fullscreenLoading.value = val
  }

  return {
    theme,
    sidebarCollapsed,
    loading,
    fullscreenLoading,
    setTheme,
    toggleTheme,
    toggleSidebar,
    setLoading,
    setFullscreenLoading
  }
})
