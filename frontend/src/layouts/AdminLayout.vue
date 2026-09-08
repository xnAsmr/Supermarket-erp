<template>
  <n-layout has-sider class="admin-layout">
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="appStore.sidebarCollapsed"
      show-trigger
      @collapse="appStore.sidebarCollapsed = true"
      @expand="appStore.sidebarCollapsed = false"
      class="admin-sider"
    >
      <div class="sider-logo">
        <img src="../assets/vue.svg" alt="Logo" class="logo-img" />
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">超市ERP</span>
      </div>
      <n-menu
        :collapsed="appStore.sidebarCollapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :options="menuOptions"
        :value="activeMenu"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="admin-header">
        <div class="header-left">
          <n-breadcrumb>
            <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </n-breadcrumb-item>
          </n-breadcrumb>
        </div>
        <div class="header-right">
          <n-switch :value="appStore.theme === 'dark'" @update:value="appStore.toggleTheme">
            <template #checked>🌙</template>
            <template #unchecked>☀️</template>
          </n-switch>
          <n-dropdown :options="userDropdownOptions" @select="handleUserAction">
            <n-button quaternary>
              <n-avatar
                v-if="userStore.userInfo?.avatar"
                :src="userStore.userInfo.avatar"
                :size="28"
                round
              />
              <n-avatar v-else :size="28" round>
                {{ userStore.realName?.charAt(0) || 'U' }}
              </n-avatar>
              <span class="username">{{ userStore.realName || userStore.username }}</span>
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>
      <n-layout-content class="admin-content">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NAvatar,
  NButton,
  NDropdown,
  NSwitch,
  NBreadcrumb,
  NBreadcrumbItem
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  Analytics,
  Cube,
  Layers,
  DocumentText,
  People,
  BarChart,
  Settings,
  Business,
  CartOutline,
  LayersOutline,
  DocumentTextOutline,
  PeopleOutline,
  BarChartOutline,
  SettingsOutline,
  WalletOutline,
  HomeOutline
} from '@vicons/ionicons5'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'
import { getCurrentMenus } from '../api/system'
import type { BackendMenu } from '../api/system'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

const menuOptions = ref<MenuOption[]>([])

const iconMap: Record<string, any> = {
  Analytics, Cube, Layers, DocumentText, People, BarChart, Settings, Business,
  CartOutline, LayersOutline, DocumentTextOutline, PeopleOutline,
  BarChartOutline, SettingsOutline, WalletOutline, HomeOutline
}

function renderIcon(iconName: string | null) {
  const icon = iconName ? iconMap[iconName] : null
  if (icon) {
    return () => h(icon, { style: { fontSize: '18px' } })
  }
  return undefined
}

function convertMenus(menus: BackendMenu[]): MenuOption[] {
  return menus
    .filter(m => m.menuType !== 2)
    .map(menu => {
      const option: MenuOption = {
        label: menu.menuName,
        key: menu.path || String(menu.id)
      }

      const icon = renderIcon(menu.icon)
      if (icon) {
        option.icon = icon
      }

      if (menu.children && menu.children.length > 0) {
        const children = convertMenus(menu.children)
        if (children.length > 0) {
          option.children = children
        }
      }

      return option
    })
}

async function loadMenus() {
  try {
    const menus = await getCurrentMenus()
    menuOptions.value = convertMenus(menus)
  } catch {
    menuOptions.value = []
  }
}

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title as string
  }))
})

const userDropdownOptions = [
  { label: '个人中心', key: 'profile' },
  { label: '切换到收银', key: 'pos' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' }
]

function handleMenuClick(key: string) {
  router.push(key)
}

async function handleUserAction(key: string) {
  switch (key) {
    case 'logout':
      await userStore.logout()
      router.push('/login')
      break
    case 'pos':
      router.push('/pos')
      break
    case 'profile':
      router.push('/admin/profile')
      break
  }
}

onMounted(() => {
  loadMenus()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-sider {
  display: flex;
  flex-direction: column;
}

.admin-sider :deep(.n-menu) {
  --n-border-radius: 0;
}

.sider-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64px;
  gap: 8px;
  border-bottom: 1px solid var(--n-border-color);
}

.logo-img {
  width: 32px;
  height: 32px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  margin-left: 8px;
}

.admin-content {
  padding: 16px;
  background: var(--n-color);
}
</style>
