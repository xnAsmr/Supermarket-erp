<template>
  <div class="product-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchParams.keyword"
              placeholder="商品名称/编码/条码"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-select
              v-model:value="searchParams.categoryId"
              placeholder="商品分类"
              clearable
              :options="categoryOptions"
              style="width: 160px"
              @update:value="handleSearch"
            />
            <n-select
              v-model:value="searchParams.status"
              placeholder="状态"
              clearable
              :options="statusOptions"
              style="width: 120px"
              @update:value="handleSearch"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>

          <n-button v-permission="'pms:product:add'" type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增商品
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendProduct) => row.id"
          remote
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>

    <ProductForm
      v-model:show="showForm"
      :product-id="currentProductId"
      @success="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag, NPopconfirm, useMessage } from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline, AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getProducts, getCategoryTree, deleteProduct } from '../../../api/product'
import type { BackendProduct, BackendCategory } from '../../../api/product'
import { formatMoney } from '../../../utils/format'
import { hasPermission } from '../../../directives/permission'
import ProductForm from './ProductForm.vue'

const message = useMessage()
const loading = ref(false)
const tableData = ref<BackendProduct[]>([])
const showForm = ref(false)
const currentProductId = ref<number | undefined>(undefined)

const searchParams = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const categoryOptions = ref<{ label: string; value: number }[]>([])
const statusOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 }
]

const columns: DataTableColumns<BackendProduct> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '编号', key: 'productNo', width: 100 },
  { title: '名称', key: 'name', width: 180, ellipsis: { tooltip: true } },
  {
    title: '图片',
    key: 'image',
    width: 80,
    render(row) {
      if (row.image) {
        return h('img', {
          src: row.image,
          alt: row.name,
          style: { width: '40px', height: '40px', objectFit: 'cover', borderRadius: '4px' }
        })
      }
      return h('div', {
        style: {
          width: '40px',
          height: '40px',
          background: '#f5f5f5',
          borderRadius: '4px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#ccc',
          fontSize: '12px'
        }
      }, '暂无图片')
    }
  },
  { title: '分类', key: 'categoryName', width: 100 },
  {
    title: '售价',
    key: 'salePrice',
    width: 100,
    render(row) { return formatMoney(row.salePrice) }
  },
  {
    title: '会员价',
    key: 'vipPrice',
    width: 100,
    render(row) { return row.vipPrice != null ? formatMoney(row.vipPrice) : '-' }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(
        NTag,
        { size: 'small', type: row.status === 1 ? 'success' : 'default' },
        { default: () => row.status === 1 ? '上架' : '下架' }
      )
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
          hasPermission('pms:product:edit')
            ? h(NButton, {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleEdit(row.id)
              }, { default: () => '编辑', icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) })
            : null,
          hasPermission('pms:product:delete')
            ? h(NPopconfirm, {
                onPositiveClick: () => handleDelete(row.id)
              }, {
                trigger: () => h(NButton, {
                  size: 'small',
                  type: 'error',
                  text: true
                }, { default: () => '删除', icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) }),
                default: () => '确定删除该商品吗？'
              })
            : null
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await getProducts(searchParams)
    tableData.value = res.list
    pagination.itemCount = res.total
    pagination.page = searchParams.page
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  const res = await getCategoryTree()
  const options: { label: string; value: number }[] = []
  res.forEach((cat: BackendCategory) => {
    options.push({ label: cat.name, value: cat.id })
    if (cat.children) {
      cat.children.forEach((child: BackendCategory) => {
        options.push({ label: `  └ ${child.name}`, value: child.id })
      })
    }
  })
  categoryOptions.value = options
}

function handleSearch() {
  searchParams.page = 1
  loadData()
}

function handleReset() {
  searchParams.keyword = ''
  searchParams.categoryId = undefined
  searchParams.status = undefined
  searchParams.page = 1
  loadData()
}

function handlePageChange(page: number) {
  searchParams.page = page
  loadData()
}

function handlePageSizeChange(pageSize: number) {
  searchParams.pageSize = pageSize
  searchParams.page = 1
  loadData()
}

function handleAdd() {
  currentProductId.value = undefined
  showForm.value = true
}

function handleEdit(id: number) {
  currentProductId.value = id
  showForm.value = true
}

async function handleDelete(id: number) {
  await deleteProduct(id)
  message.success('删除成功')
  loadData()
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style scoped>
.product-list {
  padding: 0;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
