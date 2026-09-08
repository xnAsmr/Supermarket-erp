<template>
  <div class="stock-check">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="盘点单号"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-select
              v-model:value="searchStatus"
              placeholder="状态"
              clearable
              :options="statusOptions"
              style="width: 130px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新建盘点
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="filteredData"
          :row-key="(row: CheckRecord) => row.id"
          :pagination="pagination"
        />
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, h } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'

interface CheckRecord {
  id: string
  checkNo: string
  status: string
  productCount: number
  surplusCount: number
  lossCount: number
  operator: string
  createdAt: string
}

const message = useMessage()
const searchKeyword = ref('')
const searchStatus = ref<string | null>(null)

const tableData = ref<CheckRecord[]>([
  { id: '1', checkNo: 'PD20260801001', status: 'completed', productCount: 120, surplusCount: 3, lossCount: 1, operator: '张晓明', createdAt: '2026-08-01 18:00:00' },
  { id: '2', checkNo: 'PD20260815001', status: 'in_progress', productCount: 85, surplusCount: 0, lossCount: 2, operator: '李婷', createdAt: '2026-08-15 09:30:00' },
  { id: '3', checkNo: 'PD20260820001', status: 'pending', productCount: 0, surplusCount: 0, lossCount: 0, operator: '王伟', createdAt: '2026-08-20 14:00:00' }
])

const statusOptions = [
  { label: '已完成', value: 'completed' },
  { label: '盘点中', value: 'in_progress' },
  { label: '待盘点', value: 'pending' }
]

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const filteredData = computed(() => {
  return tableData.value.filter(item => {
    const matchKeyword = !searchKeyword.value || item.checkNo.includes(searchKeyword.value)
    const matchStatus = !searchStatus.value || item.status === searchStatus.value
    return matchKeyword && matchStatus
  })
})

function statusLabel(status: string) {
  const map: Record<string, string> = { completed: '已完成', in_progress: '盘点中', pending: '待盘点' }
  return map[status] || status
}

function statusTagType(status: string) {
  const map: Record<string, 'success' | 'warning' | 'info'> = { completed: 'success', in_progress: 'warning', pending: 'info' }
  return map[status] || 'info'
}

const columns: DataTableColumns<CheckRecord> = [
  { title: '盘点单号', key: 'checkNo', width: 170 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render(row) {
      return h(NTag, { size: 'small', type: statusTagType(row.status) }, { default: () => statusLabel(row.status) })
    }
  },
  { title: '商品数', key: 'productCount', width: 80 },
  {
    title: '盘盈数',
    key: 'surplusCount',
    width: 80,
    render(row) {
      return h('span', { style: { color: row.surplusCount > 0 ? '#18a058' : undefined } }, row.surplusCount)
    }
  },
  {
    title: '盘亏数',
    key: 'lossCount',
    width: 80,
    render(row) {
      return h('span', { style: { color: row.lossCount > 0 ? '#d03050' : undefined } }, row.lossCount)
    }
  },
  { title: '操作人', key: 'operator', width: 100 },
  { title: '时间', key: 'createdAt', width: 160 }
]

function handleSearch() {}
function handleReset() {
  searchKeyword.value = ''
  searchStatus.value = null
}
function handleAdd() {
  message.info('新建盘点功能开发中')
}
</script>

<style scoped>
.stock-check { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
