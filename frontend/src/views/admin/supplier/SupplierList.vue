<template>
  <div class="supplier-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="供应商名称/联系人"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button v-permission="'pms:supplier:add'" type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增供应商
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendSupplier) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>

    <!-- 供应商表单弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingSupplier ? '编辑供应商' : '新增供应商'"
      style="width: 550px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="90">
        <n-form-item label="供应商名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入供应商名称" />
        </n-form-item>
        <n-form-item label="联系人" path="contact">
          <n-input v-model:value="formData.contact" placeholder="请输入联系人姓名" />
        </n-form-item>
        <n-form-item label="联系电话" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="formData.address" placeholder="请输入地址" />
        </n-form-item>
        <n-form-item label="开户银行" path="bankName">
          <n-input v-model:value="formData.bankName" placeholder="请输入开户银行" />
        </n-form-item>
        <n-form-item label="银行账号" path="bankAccount">
          <n-input v-model:value="formData.bankAccount" placeholder="请输入银行账号" />
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
  NCard, NDataTable, NButton, NSpace, NInput, NIcon, NTag,
  NModal, NForm, NFormItem, NPopconfirm, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getSuppliers, createSupplier, updateSupplier, deleteSupplier } from '../../../api/supplier'
import type { BackendSupplier, SupplierQueryParams } from '../../../api/supplier'

const message = useMessage()
const loading = ref(false)
const searchKeyword = ref('')

const tableData = ref<BackendSupplier[]>([])
const total = ref(0)

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

async function loadData() {
  loading.value = true
  try {
    const params: SupplierQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value

    const res = await getSuppliers(params)
    tableData.value = res.list
    total.value = res.total
    pagination.itemCount = res.total
  } catch (e) {
    message.error('加载供应商列表失败')
  } finally {
    loading.value = false
  }
}

const showFormModal = ref(false)
const editingSupplier = ref<BackendSupplier | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  name: '',
  contact: '',
  phone: '',
  address: '',
  bankName: '',
  bankAccount: ''
})

const formRules: FormRules = {
  name: { required: true, message: '请输入供应商名称', trigger: 'blur' }
}

const columns: DataTableColumns<BackendSupplier> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '供应商编号', key: 'supplierNo', width: 120 },
  { title: '供应商名称', key: 'name', width: 160 },
  { title: '联系人', key: 'contact', width: 100, render(row) { return row.contact || '-' } },
  { title: '联系电话', key: 'phone', width: 130, render(row) { return row.phone || '-' } },
  { title: '地址', key: 'address', width: 180, render(row) { return row.address || '-' } },
  { title: '开户银行', key: 'bankName', width: 140, render(row) { return row.bankName || '-' } },
  { title: '银行账号', key: 'bankAccount', width: 160, render(row) { return row.bankAccount || '-' } },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, {
        default: () => row.status === 1 ? '正常' : '禁用'
      })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
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
          h(NPopconfirm, {
            onPositiveClick: () => handleDelete(row)
          }, {
            trigger: () => h(NButton, { size: 'small', type: 'error', text: true }, { default: () => '删除' }),
            default: () => `确定要删除供应商 "${row.name}" 吗？`
          })
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
  pagination.page = 1
  loadData()
}

function resetForm() {
  formData.name = ''
  formData.contact = ''
  formData.phone = ''
  formData.address = ''
  formData.bankName = ''
  formData.bankAccount = ''
}

function handleAdd() {
  editingSupplier.value = null
  resetForm()
  showFormModal.value = true
}

function handleEdit(row: BackendSupplier) {
  editingSupplier.value = row
  formData.name = row.name
  formData.contact = row.contact || ''
  formData.phone = row.phone || ''
  formData.address = row.address || ''
  formData.bankName = row.bankName || ''
  formData.bankAccount = row.bankAccount || ''
  showFormModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (editingSupplier.value) {
      await updateSupplier(editingSupplier.value.id, formData)
      message.success('供应商更新成功')
    } else {
      await createSupplier(formData)
      message.success('供应商添加成功')
    }

    showFormModal.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

function handleDelete(row: BackendSupplier) {
  deleteSupplier(row.id).then(() => {
    message.success('供应商已删除')
    loadData()
  }).catch(() => {
    message.error('删除失败')
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.supplier-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
