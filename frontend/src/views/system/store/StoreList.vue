<template>
  <div class="store-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="门店名称/编号"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-select
              v-model:value="searchTenantId"
              placeholder="所属租户"
              clearable
              :options="tenantOptions"
              style="width: 180px"
            />
            <n-select
              v-model:value="searchStatus"
              placeholder="状态"
              clearable
              :options="statusOptions"
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
            新增门店
          </n-button>
        </div>

        <!-- 数据表格 -->
        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendStore) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>

    <!-- 新增/编辑门店弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingStore ? '编辑门店' : '新增门店'"
      style="width: 500px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="所属租户" path="tenantId">
          <n-select
            v-model:value="formData.tenantId"
            placeholder="请选择租户"
            :options="tenantOptions"
            :disabled="!!editingStore"
          />
        </n-form-item>
        <n-form-item label="门店名称" path="storeName">
          <n-input v-model:value="formData.storeName" placeholder="请输入门店名称" />
        </n-form-item>
        <n-form-item label="门店类型" path="storeType">
          <n-select
            v-model:value="formData.storeType"
            placeholder="请选择门店类型"
            :options="[
              { label: '直营店', value: 1 },
              { label: '加盟店', value: 2 }
            ]"
          />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="formData.address" placeholder="请输入门店地址" />
        </n-form-item>
        <n-form-item label="营业时间" path="businessHours">
          <n-input v-model:value="formData.businessHours" placeholder="如 08:00-22:00" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showFormModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NModal, NForm, NFormItem, useMessage, useDialog
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getStores, createStore, updateStore, updateStoreStatus, getTenants } from '../../../api/tenant'
import type { BackendStore, BackendTenant } from '../../../api/tenant'
const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const searchKeyword = ref('')
const searchTenantId = ref<number | null>(null)
const searchStatus = ref<number | null>(null)
const showFormModal = ref(false)
const editingStore = ref<BackendStore | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const tableData = ref<BackendStore[]>([])
const tenantOptions = ref<{ label: string; value: number }[]>([])

const statusOptions = [
  { label: '营业中', value: 1 },
  { label: '已关闭', value: 0 }
]

const formData = reactive({
  tenantId: null as number | null,
  storeName: '',
  storeType: 1 as number | null,
  address: '',
  businessHours: ''
})

const formRules: FormRules = {
  tenantId: { required: true, type: 'number', message: '请选择所属租户', trigger: 'change' },
  storeName: { required: true, message: '请输入门店名称', trigger: 'blur' }
}

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

function getStoreTypeName(type: number | null) {
  const map: Record<number, string> = { 1: '直营店', 2: '加盟店' }
  return type != null ? map[type] || String(type) : '-'
}

function getStoreStatusName(status: number) {
  const map: Record<number, string> = { 0: '已关闭', 1: '营业中' }
  return map[status] || '-'
}

function getStoreStatusTag(status: number) {
  const map: Record<number, 'success' | 'error'> = { 0: 'error', 1: 'success' }
  return map[status] || 'info'
}

async function loadTenantOptions() {
  try {
    const res = await getTenants({ page: 1, pageSize: 100 })
    tenantOptions.value = res.list.map((t: BackendTenant) => ({ label: t.tenantName, value: t.id }))
  } catch (e) {
    // 忽略
  }
}

function getTenantName(tenantId: number) {
  const tenant = tenantOptions.value.find(t => t.value === tenantId)
  return tenant ? tenant.label : '-'
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchTenantId.value) params.tenantId = searchTenantId.value
    if (searchStatus.value !== null) params.status = searchStatus.value

    const res = await getStores(params)
    tableData.value = res.list
    pagination.itemCount = res.total
  } catch (e) {
    message.error('加载门店列表失败')
  } finally {
    loading.value = false
  }
}

const columns: DataTableColumns<BackendStore> = [
  { title: '门店编号', key: 'storeNo', width: 120 },
  { title: '门店名称', key: 'storeName', width: 150 },
  {
    title: '所属租户',
    key: 'tenantId',
    width: 120,
    render(row) {
      return getTenantName(row.tenantId)
    }
  },
  { title: '地址', key: 'address', width: 180, render(row) { return row.address || '-' } },
  {
    title: '门店类型',
    key: 'storeType',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.storeType === 1 ? 'info' : 'warning' }, {
        default: () => getStoreTypeName(row.storeType)
      })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: getStoreStatusTag(row.status) }, {
        default: () => getStoreStatusName(row.status)
      })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small',
            type: row.status === 1 ? 'warning' : 'success',
            text: true,
            onClick: () => handleToggleStatus(row)
          }, { default: () => row.status === 1 ? '停业' : '开业' })
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
  searchTenantId.value = null
  searchStatus.value = null
  pagination.page = 1
  loadData()
}

function resetForm() {
  formData.tenantId = null
  formData.storeName = ''
  formData.storeType = 1
  formData.address = ''
  formData.businessHours = ''
}

function handleAdd() {
  editingStore.value = null
  resetForm()
  showFormModal.value = true
}

function handleEdit(row: BackendStore) {
  editingStore.value = row
  formData.tenantId = row.tenantId
  formData.storeName = row.storeName
  formData.storeType = row.storeType || 1
  formData.address = row.address || ''
  formData.businessHours = row.businessHours || ''
  showFormModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (editingStore.value) {
      await updateStore(editingStore.value.id, {
        storeName: formData.storeName,
        storeType: formData.storeType,
        address: formData.address,
        businessHours: formData.businessHours
      })
      message.success('门店更新成功')
    } else {
      await createStore({
        storeName: formData.storeName,
        storeType: formData.storeType,
        address: formData.address,
        businessHours: formData.businessHours
      }, formData.tenantId!)
      message.success('门店创建成功')
    }

    showFormModal.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

function handleToggleStatus(row: BackendStore) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '开业' : '停业'

  dialog.warning({
    title: `确定要${action}吗？`,
    content: `门店 "${row.storeName}" 将被设置为${action}状态`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await updateStoreStatus(row.id, newStatus)
        row.status = newStatus
        message.success(`${action}操作成功`)
      } catch (e) {
        message.error(`${action}失败`)
      }
    }
  })
}

onMounted(() => {
  loadTenantOptions()
  loadData()
})
</script>

<style scoped>
.store-list {
  padding: 0;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
