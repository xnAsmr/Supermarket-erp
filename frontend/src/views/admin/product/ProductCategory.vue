<template>
  <div class="product-category">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="分类名称/编码"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
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

          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增分类
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="filteredData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendCategory) => row.id"
          default-expand-all
        />
      </n-space>
    </n-card>

    <n-modal
      v-model:show="showForm"
      :title="isEdit ? '编辑分类' : '新增分类'"
      preset="card"
      style="width: 520px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="上级分类" path="parentId">
          <n-select
            v-model:value="formData.parentId"
            :options="parentOptions"
            placeholder="无（一级分类）"
            clearable
          />
        </n-form-item>
        <n-form-item label="分类名称" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入分类名称" />
        </n-form-item>
        <n-form-item label="分类编码" path="code">
          <n-input v-model:value="formData.code" placeholder="请输入分类编码" />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="图标" path="icon">
          <n-input v-model:value="formData.icon" placeholder="图标名称（可选）" />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showForm = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NIcon, NTag, NModal,
  NForm, NFormItem, NInputNumber, NSelect, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getCategoryTree, createCategory, updateCategory, deleteCategory, type BackendCategory } from '../../../api/product'

const message = useMessage()
const loading = ref(false)
const tableData = ref<BackendCategory[]>([])
const showForm = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)
const searchKeyword = ref('')

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const formData = reactive({
  id: undefined as number | undefined,
  parentId: null as string | null,
  name: '',
  code: '',
  sort: 0,
  icon: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }]
}

const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(item => {
    if (item.name.toLowerCase().includes(kw) || item.code.toLowerCase().includes(kw)) {
      return true
    }
    if (item.children) {
      return item.children.some(child =>
        child.name.toLowerCase().includes(kw) || child.code.toLowerCase().includes(kw)
      )
    }
    return false
  })
})

const parentOptions = computed(() => {
  const options: { label: string; value: string }[] = []
  tableData.value.forEach(item => {
    options.push({ label: item.name, value: String(item.id) })
    if (item.children) {
      item.children.forEach(child => {
        options.push({ label: `  └ ${child.name}`, value: String(child.id) })
      })
    }
  })
  return options
})

const columns: DataTableColumns<BackendCategory> = [
  { title: '分类名称', key: 'name', width: 200 },
  { title: '分类编码', key: 'code', width: 120 },
  {
    title: '层级',
    key: 'level',
    width: 80,
    render(row) {
      return h(
        NTag,
        { size: 'small', type: row.level === 1 ? 'success' : 'info' },
        { default: () => row.level === 1 ? '一级' : '二级' }
      )
    }
  },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '子分类数',
    key: 'children',
    width: 100,
    render(row) {
      return row.children ? row.children.length : 0
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑', icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) }),
          h(NButton, {
            size: 'small',
            type: 'error',
            text: true,
            disabled: !!(row.children && row.children.length > 0),
            onClick: () => handleDelete(row)
          }, { default: () => '删除', icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) })
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    tableData.value = res
    pagination.itemCount = res.length
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  // 搜索通过 computed 实现
}

function handleReset() {
  searchKeyword.value = ''
}

function handleAdd() {
  isEdit.value = false
  formData.id = undefined
  formData.parentId = null
  formData.name = ''
  formData.code = ''
  formData.sort = 0
  formData.icon = ''
  showForm.value = true
}

function handleEdit(row: BackendCategory) {
  isEdit.value = true
  formData.id = row.id
  formData.parentId = row.parentId === 0 ? null : String(row.parentId)
  formData.name = row.name
  formData.code = row.code
  formData.sort = row.sort
  formData.icon = row.icon || ''
  showForm.value = true
}

async function handleDelete(row: BackendCategory) {
  if (row.children && row.children.length > 0) {
    message.warning('该分类下有子分类，无法删除')
    return
  }
  await deleteCategory(row.id)
  message.success('删除成功')
  loadData()
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && formData.id) {
      await updateCategory(formData.id, { name: formData.name, code: formData.code })
      message.success('更新成功')
    } else {
      await createCategory({
        name: formData.name,
        code: formData.code,
        parentId: formData.parentId ? Number(formData.parentId) : undefined
      })
      message.success('创建成功')
    }
    showForm.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.product-category {
  padding: 0;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
