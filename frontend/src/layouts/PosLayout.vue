<template>
  <div class="pos-layout">
    <div class="pos-header">
      <div class="pos-header-left">
        <img src="../assets/vue.svg" alt="Logo" class="pos-logo" />
        <span class="pos-title">收银台</span>
      </div>
      <div class="pos-header-center">
        <n-input
          v-model:value="searchKeyword"
          placeholder="扫码或输入商品编码..."
          clearable
          class="pos-search"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon><Search /></n-icon>
          </template>
        </n-input>
      </div>
      <div class="pos-header-right">
        <n-space align="center" :size="12">
          <n-button quaternary @click="router.push('/pos')">
            <template #icon><n-icon :component="CashOutline" /></template>
            收银
          </n-button>
          <n-button quaternary @click="router.push('/pos/orders')">
            <template #icon><n-icon :component="ReceiptOutline" /></template>
            今日订单
          </n-button>
          <n-icon :component="PersonOutline" size="16" />
          <n-text>{{ userStore.realName || userStore.username }}</n-text>
        </n-space>
        <n-button quaternary @click="handleSwitchToAdmin">
          管理后台
        </n-button>
        <n-button quaternary @click="handleLogout">
          退出
        </n-button>
      </div>
    </div>
    <div class="pos-main">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NInput, NIcon, NButton, NSpace, NText } from 'naive-ui'
import { Search, PersonOutline, CashOutline, ReceiptOutline } from '@vicons/ionicons5'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

function handleSearch() {
  if (searchKeyword.value.trim()) {
    // Trigger product search event
    window.dispatchEvent(
      new CustomEvent('pos-search', { detail: searchKeyword.value.trim() })
    )
    searchKeyword.value = ''
  }
}

function handleSwitchToAdmin() {
  router.push('/admin/dashboard')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.pos-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.pos-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.pos-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pos-logo {
  width: 28px;
  height: 28px;
}

.pos-title {
  font-size: 18px;
  font-weight: 600;
}

.pos-header-center {
  flex: 1;
  max-width: 500px;
  margin: 0 24px;
}

.pos-search {
  width: 100%;
}

.pos-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pos-main {
  flex: 1;
  overflow: auto;
}
</style>
