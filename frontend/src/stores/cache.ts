import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface CacheVersion {
  products: number
  members: number
  stock: number
  system: number
}

export type SyncStatus = 'idle' | 'syncing' | 'error' | 'success'

export const useCacheStore = defineStore('cache', () => {
  const dataVersion = ref<CacheVersion>({
    products: 0,
    members: 0,
    stock: 0,
    system: 0
  })
  const syncStatus = ref<SyncStatus>('idle')
  const isOffline = ref(!navigator.onLine)
  const lastSyncTime = ref<number>(0)
  const pendingChanges = ref<any[]>([])

  function updateVersion(key: keyof CacheVersion) {
    dataVersion.value[key] = Date.now()
  }

  function setSyncStatus(status: SyncStatus) {
    syncStatus.value = status
  }

  function setOffline(val: boolean) {
    isOffline.value = val
  }

  function recordSyncTime() {
    lastSyncTime.value = Date.now()
  }

  function addPendingChange(change: any) {
    pendingChanges.value.push({
      ...change,
      timestamp: Date.now()
    })
  }

  function clearPendingChanges() {
    pendingChanges.value = []
  }

  function $reset() {
    dataVersion.value = { products: 0, members: 0, stock: 0, system: 0 }
    syncStatus.value = 'idle'
    isOffline.value = !navigator.onLine
    lastSyncTime.value = 0
    pendingChanges.value = []
  }

  return {
    dataVersion,
    syncStatus,
    isOffline,
    lastSyncTime,
    pendingChanges,
    updateVersion,
    setSyncStatus,
    setOffline,
    recordSyncTime,
    addPendingChange,
    clearPendingChanges,
    $reset
  }
})
