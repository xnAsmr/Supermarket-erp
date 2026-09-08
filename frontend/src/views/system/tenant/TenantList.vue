<template>
  <div class="tenant-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="租户名称/编号"
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
              style="width: 120px"
            />
            <n-select
              v-model:value="searchPlanType"
              placeholder="套餐类型"
              clearable
              :options="planTypeOptions"
              style="width: 120px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增租户
          </n-button>
        </div>

        <!-- 数据表格 -->
        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendTenant) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>

    <!-- 租户详情抽屉 -->
    <n-drawer v-model:show="showDetailDrawer" :width="500" placement="right">
      <n-drawer-content :title="currentTenant?.tenantName">
        <n-descriptions :column="1" label-placement="left" bordered>
          <n-descriptions-item label="租户编号">{{ currentTenant?.tenantNo }}</n-descriptions-item>
          <n-descriptions-item label="租户名称">{{ currentTenant?.tenantName }}</n-descriptions-item>
          <n-descriptions-item label="联系人">{{ currentTenant?.contactName }}</n-descriptions-item>
          <n-descriptions-item label="联系电话">{{ currentTenant?.contactPhone }}</n-descriptions-item>
          <n-descriptions-item label="套餐类型">
            <n-tag :type="getPlanTypeTag(currentTenant?.planType)">
              {{ getPlanTypeName(currentTenant?.planType) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="getStatusTag(currentTenant?.status)">
              {{ getStatusName(currentTenant?.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="最大门店数">{{ currentTenant?.maxStores ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="最大用户数">{{ currentTenant?.maxUsers ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="到期时间">{{ currentTenant?.expireTime }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ currentTenant?.createTime }}</n-descriptions-item>
        </n-descriptions>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NDrawer, NDrawerContent, NDescriptions, NDescriptionsItem,
  useMessage, useDialog
} from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getTenants, updateTenantStatus } from '../../../api/tenant'
import type { BackendTenant } from '../../../api/tenant'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)
const searchPlanType = ref<string | null>(null)
const showDetailDrawer = ref(false)
const currentTenant = ref<BackendTenant | null>(null)

const tableData = ref<BackendTenant[]>([])

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

const planTypeOptions = [
  { label: '基础版', value: 1 },
  { label: '专业版', value: 2 },
  { label: '企业版', value: 3 }
]

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
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

function getPlanTypeName(type?: number | null) {
  const map: Record<number, string> = { 1: '基础版', 2: '专业版', 3: '企业版' }
  return type !== undefined && type !== null ? map[type] || String(type) : '-'
}

function getPlanTypeTag(type?: number | null) {
  const map: Record<number, 'info' | 'warning' | 'success'> = { 1: 'info', 2: 'warning', 3: 'success' }
  return type !== undefined && type !== null ? map[type] || 'info' : 'info'
}

function getStatusName(status?: number) {
  const map: Record<number, string> = { 0: '禁用', 1: '正常' }
  return status !== undefined ? map[status] : '-'
}

function getStatusTag(status?: number) {
  const map: Record<number, 'error' | 'success'> = { 0: 'error', 1: 'success' }
  return status !== undefined ? map[status] : 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchStatus.value !== null) params.status = searchStatus.value

    const res = await getTenants(params)
    tableData.value = res.list
    pagination.itemCount = res.total
  } catch (e) {
    message.error('加载租户列表失败')
  } finally {
    loading.value = false
  }
}

const columns: DataTableColumns<BackendTenant> = [
  { title: '租户编号', key: 'tenantNo', width: 120 },
  { title: '租户名称', key: 'tenantName', width: 150 },
  { title: '联系人', key: 'contactName', width: 100 },
  { title: '联系电话', key: 'contactPhone', width: 130 },
  {
    title: '套餐类型',
    key: 'planType',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: getPlanTypeTag(row.planType) }, {
        default: () => getPlanTypeName(row.planType)
      })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: getStatusTag(row.status) }, {
        default: () => getStatusName(row.status)
      })
    }
  },
  { title: '门店数', key: 'storeCount', width: 80, render(row) { return row.storeCount ?? 0 } },
  { title: '到期时间', key: 'expireTime', width: 120 },
  {
    title: '操作',
    key: 'actions',
    width: 250,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: () => handleView(row)
          }, { default: () => '详情' }),
          h(NButton, {
            size: 'small',
            type: 'info',
            text: true,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small',
            type: row.status === 1 ? 'warning' : 'success',
            text: true,
            onClick: () => handleToggleStatus(row)
          }, { default: () => row.status === 1 ? '禁用' : '启用' })
        ]
      })
    }
  }
]

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  searchStatus.value = null
  searchPlanType.value = null
  pagination.page = 1
  loadData()
}

function handleAdd() {
  router.push('/system/tenant/add')
}

function handleView(row: BackendTenant) {
  currentTenant.value = row
  showDetailDrawer.value = true
}

function handleEdit(row: BackendTenant) {
  router.push(`/system/tenant/edit/${row.id}`)
}

function handleToggleStatus(row: BackendTenant) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'

  dialog.warning({
    title: `确定要${action}吗？`,
    content: `租户 "${row.tenantName}" 将被${action}`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await updateTenantStatus(row.id, newStatus)
        row.status = newStatus
        message.success(`${action}成功`)
      } catch (e) {
        message.error(`${action}失败`)
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.tenant-list {
  padding: 0;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
