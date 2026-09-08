<script setup lang="ts">
import { computed } from 'vue'
import { darkTheme, zhCN, dateZhCN } from 'naive-ui'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()
const theme = computed(() => appStore.theme === 'dark' ? darkTheme : undefined)
</script>

<template>
  <n-config-provider :theme="theme" :locale="zhCN" :date-locale="dateZhCN">
    <n-loading-bar-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-message-provider>
            <router-view />
          </n-message-provider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>
