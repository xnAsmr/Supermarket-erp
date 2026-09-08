<template>
  <div class="brand-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="品牌名称"
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
          <n-button v-permission="'pms:brand:add'" type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增品牌
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendBrand) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>

    <!-- 品牌表单弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingBrand ? '编辑品牌' : '新增品牌'"
      style="width: 500px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="品牌名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入品牌名称" />
        </n-form-item>
        <n-form-item label="品牌Logo" path="logo">
          <n-input v-model:value="formData.logo" placeholder="请输入Logo URL" />
        </n-form-item>
        <n-form-item label="品牌描述" path="description">
          <n-input v-model:value="formData.description" type="textarea" placeholder="请输入品牌描述" :rows="3" />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" placeholder="排序号" style="width: 100%" />
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
  NModal, NForm, NFormItem, NInputNumber, NPopconfirm, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getBrands, createBrand, updateBrand, deleteBrand } from '../../../api/brand'
import type { BackendBrand, BrandQueryParams } from '../../../api/brand'

const message = useMessage()
const loading = ref(false)
const searchKeyword = ref('')

const tableData = ref<BackendBrand[]>([])
const total = ref(0)

const pagination = ref<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.value.page = page
    loadData()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadData()
  }
})

async function loadData() {
  loading.value = true
  try {
    const params: BrandQueryParams = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value

    const res = await getBrands(params)
    tableData.value = res.list
    total.value = res.total
    pagination.value = { ...pagination.value, itemCount: res.total, page: pagination.value.page, pageSize: pagination.value.pageSize }
  } catch (e) {
    message.error('加载品牌列表失败')
  } finally {
    loading.value = false
  }
}

const showFormModal = ref(false)
const editingBrand = ref<BackendBrand | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  name: '',
  logo: '',
  description: '',
  sort: 0
})

const formRules: FormRules = {
  name: { required: true, message: '请输入品牌名称', trigger: 'blur' }
}

const columns: DataTableColumns<BackendBrand> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.value.page! - 1) * pagination.value.pageSize! + index + 1
    }
  },
  { title: '品牌名称', key: 'name', width: 160 },
  {
    title: 'Logo',
    key: 'logo',
    width: 80,
    render(row) {
      if (row.logo) {
        return h('img', { src: row.logo, style: { height: '24px', maxWidth: '60px', objectFit: 'contain' } })
      }
      return '-'
    }
  },
  { title: '描述', key: 'description', width: 200, render(row) { return row.description || '-' } },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, {
        default: () => row.status === 1 ? '启用' : '禁用'
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
            default: () => `确定要删除品牌 "${row.name}" 吗？`
          })
        ]
      })
    }
  }
]

function handleSearch() {
  pagination.value.page = 1
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  pagination.value.page = 1
  loadData()
}

function resetForm() {
  formData.name = ''
  formData.logo = ''
  formData.description = ''
  formData.sort = 0
}

function handleAdd() {
  editingBrand.value = null
  resetForm()
  showFormModal.value = true
}

function handleEdit(row: BackendBrand) {
  editingBrand.value = row
  formData.name = row.name
  formData.logo = row.logo || ''
  formData.description = row.description || ''
  formData.sort = row.sort || 0
  showFormModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (editingBrand.value) {
      await updateBrand(editingBrand.value.id, formData)
      message.success('品牌更新成功')
    } else {
      await createBrand(formData)
      message.success('品牌添加成功')
    }

    showFormModal.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

function handleDelete(row: BackendBrand) {
  deleteBrand(row.id).then(() => {
    message.success('品牌已删除')
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
.brand-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
