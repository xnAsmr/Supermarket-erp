<template>
  <div class="store-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
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

    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingStore ? '编辑门店' : '新增门店'"
      style="width: 500px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="100">
        <n-form-item label="门店名称" path="storeName">
          <n-input v-model:value="formData.storeName" placeholder="请输入门店名称" />
        </n-form-item>
        <n-form-item label="门店类型" path="storeType">
          <n-select
            v-model:value="formData.storeType"
            placeholder="请选择门店类型"
            :options="storeTypeOptions"
          />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="formData.address" placeholder="请输入门店地址" />
        </n-form-item>
        <n-form-item label="联系电话" path="contactPhone">
          <n-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="营业时间" path="businessHours">
          <n-input v-model:value="formData.businessHours" placeholder="如 08:00-22:00" />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formData.remark" type="textarea" placeholder="请输入备注" />
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
import { getStores, createStore, updateStore, updateStoreStatus } from '../../../api/tenant'
import type { BackendStore } from '../../../api/tenant'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)
const showFormModal = ref(false)
const editingStore = ref<BackendStore | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const tableData = ref<BackendStore[]>([])

const statusOptions = [
  { label: '营业中', value: 1 },
  { label: '已关闭', value: 0 }
]

const storeTypeOptions = [
  { label: '直营店', value: 1 },
  { label: '加盟店', value: 2 }
]

const formData = reactive({
  storeName: '',
  storeType: 1 as number | null,
  address: '',
  contactPhone: '',
  businessHours: '',
  remark: ''
})

const formRules: FormRules = {
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

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
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
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '门店编号', key: 'storeNo', width: 120 },
  { title: '门店名称', key: 'storeName', width: 150 },
  { title: '地址', key: 'address', width: 180, render(row) { return row.address || '-' } },
  { title: '联系电话', key: 'contactPhone', width: 120, render(row) { return row.contactPhone || '-' } },
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
  { title: '营业时间', key: 'businessHours', width: 120, render(row) { return row.businessHours || '-' } },
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
  searchStatus.value = null
  pagination.page = 1
  loadData()
}

function resetForm() {
  formData.storeName = ''
  formData.storeType = 1
  formData.address = ''
  formData.contactPhone = ''
  formData.businessHours = ''
  formData.remark = ''
}

function handleAdd() {
  editingStore.value = null
  resetForm()
  showFormModal.value = true
}

function handleEdit(row: BackendStore) {
  editingStore.value = row
  formData.storeName = row.storeName
  formData.storeType = row.storeType || 1
  formData.address = row.address || ''
  formData.contactPhone = row.contactPhone || ''
  formData.businessHours = row.businessHours || ''
  formData.remark = row.remark || ''
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
        contactPhone: formData.contactPhone,
        businessHours: formData.businessHours,
        remark: formData.remark
      })
      message.success('门店更新成功')
    } else {
      await createStore({
        storeName: formData.storeName,
        storeType: formData.storeType,
        address: formData.address,
        contactPhone: formData.contactPhone,
        businessHours: formData.businessHours,
        remark: formData.remark
      })
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
  loadData()
})
</script>

<style scoped>
.store-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
