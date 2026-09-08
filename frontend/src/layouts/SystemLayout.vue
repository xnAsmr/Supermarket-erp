<template>
  <n-layout has-sider style="height: 100vh">
    <!-- 侧边栏 -->
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="220"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      :native-scrollbar="false"
      style="height: 100vh"
    >
      <div class="logo">
        <img src="../assets/vue.svg" alt="Logo" style="width: 32px; height: 32px" />
        <span v-if="!collapsed" class="logo-text">系统管理后台</span>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="22"
        :options="menuOptions"
        :value="activeKey"
        @update:value="handleMenuClick"
      />
    </n-layout-sider>

    <!-- 主内容区 -->
    <n-layout>
      <!-- 顶栏 -->
      <n-layout-header bordered style="height: 60px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between">
        <n-space align="center">
          <n-breadcrumb>
            <n-breadcrumb-item>
              <n-icon :component="HomeOutline" />
              首页
            </n-breadcrumb-item>
            <n-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</n-breadcrumb-item>
          </n-breadcrumb>
        </n-space>
        <n-space align="center" :size="16">
          <n-text depth="3" style="font-size: 14px">系统管理员</n-text>
          <n-dropdown :options="userDropdownOptions" @select="handleUserAction">
            <n-button quaternary>
              <template #icon>
                <n-icon :component="PersonOutline" />
              </template>
              管理员
            </n-button>
          </n-dropdown>
        </n-space>
      </n-layout-header>

      <!-- 内容区 -->
      <n-layout-content :content-style="{ padding: '24px' }" :native-scrollbar="false">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import type { MenuOption, DropdownOption } from 'naive-ui'
import {
  HomeOutline,
  PersonOutline,
  BusinessOutline,
  StorefrontOutline,
  PeopleOutline,
  SettingsOutline,
  LogOutOutline,
  BarChartOutline,
  DocumentTextOutline,
  ShieldOutline,
  MenuOutline
} from '@vicons/ionicons5'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)

const currentTitle = computed(() => {
  return route.meta?.title as string || ''
})

// 菜单配置
const menuOptions: MenuOption[] = [
  {
    label: '系统概览',
    key: 'dashboard',
    icon: () => h(NIcon, null, { default: () => h(BarChartOutline) })
  },
  {
    label: '租户管理',
    key: 'tenant',
    icon: () => h(NIcon, null, { default: () => h(BusinessOutline) }),
    children: [
      {
        label: '租户列表',
        key: 'tenant-list'
      }
    ]
  },
  {
    label: '门店管理',
    key: 'store',
    icon: () => h(NIcon, null, { default: () => h(StorefrontOutline) }),
    children: [
      {
        label: '门店列表',
        key: 'store-list'
      }
    ]
  },
  {
    label: '系统用户',
    key: 'user',
    icon: () => h(NIcon, null, { default: () => h(PeopleOutline) }),
    children: [
      {
        label: '用户列表',
        key: 'user-list'
      }
    ]
  },
  {
    label: '权限管理',
    key: 'permission',
    icon: () => h(NIcon, null, { default: () => h(ShieldOutline) }),
    children: [
      {
        label: '角色管理',
        key: 'role-list'
      },
      {
        label: '菜单管理',
        key: 'menu-list'
      }
    ]
  },
  {
    label: '操作日志',
    key: 'logs',
    icon: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
  },
  {
    label: '系统设置',
    key: 'settings',
    icon: () => h(NIcon, null, { default: () => h(SettingsOutline) })
  }
]

// 当前激活的菜单
const activeKey = computed(() => {
  const path = route.path
  if (path.includes('tenant')) return 'tenant-list'
  if (path.includes('store')) return 'store-list'
  if (path.includes('user')) return 'user-list'
  if (path.includes('role')) return 'role-list'
  if (path.includes('menu')) return 'menu-list'
  if (path.includes('logs')) return 'logs'
  if (path.includes('settings')) return 'settings'
  return 'dashboard'
})

// 菜单点击
function handleMenuClick(key: string) {
  const routeMap: Record<string, string> = {
    'dashboard': '/system/dashboard',
    'tenant-list': '/system/tenant/list',
    'store-list': '/system/store/list',
    'user-list': '/system/user/list',
    'role-list': '/system/role/list',
    'menu-list': '/system/menu/list',
    'logs': '/system/logs',
    'settings': '/system/settings'
  }
  if (routeMap[key]) {
    router.push(routeMap[key])
  }
}

// 用户下拉菜单
const userDropdownOptions: DropdownOption[] = [
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
  }
]

async function handleUserAction(key: string) {
  if (key === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.logo-text {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  white-space: nowrap;
}
</style>
