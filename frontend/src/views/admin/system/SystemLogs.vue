<template>
  <div class="system-logs">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="操作人/模块"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-date-picker
              v-model:value="dateRange"
              type="daterange"
              clearable
              style="width: 260px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: OperationLog) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NInput, NIcon, NTag, NDatePicker } from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { getOperationLogs } from '../../../api/system'
import type { OperationLog, OperationLogQueryParams } from '../../../api/system'

const searchKeyword = ref('')
const dateRange = ref<[number, number] | null>(null)
const loading = ref(false)
const tableData = ref<OperationLog[]>([])

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
    loadData()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    loadData()
  }
})

function moduleTagType(module: string) {
  const map: Record<string, 'success' | 'warning' | 'info' | 'error'> = {
    '商品管理': 'info',
    '库存管理': 'warning',
    '订单管理': 'success',
    '会员管理': 'info',
    '系统设置': 'error',
    '报表管理': 'info',
    '品牌管理': 'info',
    '供应商管理': 'info',
    '门店管理': 'warning',
    '租户管理': 'error',
    '用户管理': 'info',
    '角色管理': 'info',
    '菜单管理': 'info'
  }
  return map[module] || 'info'
}

const columns: DataTableColumns<OperationLog> = [
  { title: '序号', key: 'index', width: 60, render(_row, index) { return (pagination.page! - 1) * pagination.pageSize! + index + 1 } },
  { title: '操作人', key: 'username', width: 100, render(row) { return row.username || '-' } },
  {
    title: '模块',
    key: 'module',
    width: 110,
    render(row) {
      return h(NTag, { size: 'small', type: moduleTagType(row.module) }, { default: () => row.module })
    }
  },
  { title: '操作', key: 'operation', width: 130 },
  {
    title: '结果',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'error' }, {
        default: () => row.status === 1 ? '成功' : '失败'
      })
    }
  },
  { title: '耗时(ms)', key: 'duration', width: 90, render(row) { return row.duration ?? '-' } },
  { title: 'IP', key: 'ip', width: 130, render(row) { return row.ip || '-' } },
  { title: '时间', key: 'createTime', width: 170 }
]

async function loadData() {
  loading.value = true
  try {
    const params: OperationLogQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.username = searchKeyword.value
    if (dateRange.value) {
      params.startDate = formatDate(dateRange.value[0])
      params.endDate = formatDate(dateRange.value[1])
    }
    const res = await getOperationLogs(params)
    tableData.value = res.list
    pagination.itemCount = res.total
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(ts: number): string {
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  dateRange.value = null
  pagination.page = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.system-logs { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
