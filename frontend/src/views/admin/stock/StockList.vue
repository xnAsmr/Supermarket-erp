<template>
  <div class="stock-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-select
              v-model:value="searchParams.storeId"
              :options="storeOptions"
              placeholder="全部门店"
              clearable
              style="width: 180px"
              @update:value="handleStoreChange"
            />
            <n-select
              v-model:value="searchParams.categoryId"
              :options="categoryOptions"
              placeholder="全部分类"
              clearable
              style="width: 160px"
            />
            <n-select
              v-model:value="searchParams.stockStatus"
              :options="stockStatusOptions"
              placeholder="库存状态"
              clearable
              style="width: 130px"
            />
            <n-input
              v-model:value="searchParams.keyword"
              placeholder="商品名称/编码/条码"
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
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendStockVO) => row.productId"
          remote
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NInput, NIcon, NSelect, useMessage } from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { useRouter } from 'vue-router'
import { getStocks, type BackendStockVO } from '../../../api/stock'
import { getStores, type BackendStore } from '../../../api/tenant'
import { getCategoryTree, type BackendCategory } from '../../../api/product'

const message = useMessage()
const router = useRouter()
const loading = ref(false)
const tableData = ref<BackendStockVO[]>([])
const storeOptions = ref<{ label: string; value: number }[]>([])
const categoryOptions = ref<{ label: string; value: number }[]>([])

const stockStatusOptions = [
  { label: '全部', value: 0 },
  { label: '货架有货', value: 1 },
  { label: '货架无货', value: 2 }
]

const searchParams = reactive({
  keyword: '',
  storeId: null as number | null,
  categoryId: null as number | null,
  stockStatus: null as number | null,
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

const columns: DataTableColumns<BackendStockVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '商品名称', key: 'productName', width: 200, ellipsis: { tooltip: true } },
  { title: '条码', key: 'barcode', width: 140 },
  {
    title: '仓库库存',
    key: 'warehouseQuantity',
    width: 100,
    render(row) {
      const qty = row.warehouseQuantity ?? 0
      return h('span', { style: { color: qty === 0 ? '#999' : '#333' } }, qty)
    }
  },
  {
    title: '货架库存',
    key: 'shelfQuantity',
    width: 100,
    render(row) {
      const qty = row.shelfQuantity ?? 0
      return h('span', { style: { color: qty === 0 ? '#999' : '#333' } }, qty)
    }
  },
  {
    title: '待入库',
    key: 'pendingInQuantity',
    width: 90,
    render(row) {
      const qty = row.pendingInQuantity ?? 0
      return h('span', { style: { color: qty > 0 ? '#18a058' : '#999' } }, qty)
    }
  },
  {
    title: '待出库',
    key: 'pendingOutQuantity',
    width: 90,
    render(row) {
      const qty = row.pendingOutQuantity ?? 0
      return h('span', { style: { color: qty > 0 ? '#f0a020' : '#999' } }, qty)
    }
  },
  {
    title: '平均成本',
    key: 'avgCost',
    width: 100,
    render(row) { return `¥${(row.avgCost ?? 0).toFixed(2)}` }
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render(row) {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        text: true,
        onClick: () => handleViewDetail(row)
      }, { default: () => '详情' })
    }
  }
]

function handleViewDetail(row: BackendStockVO) {
  router.push({
    path: '/admin/stock/detail',
    query: { productId: row.productId, storeId: row.storeId ?? '' }
  })
}

function handleStoreChange() {
  searchParams.page = 1
  loadData()
}

async function loadStores() {
  try {
    const res = await getStores({ pageSize: 100 })
    storeOptions.value = res.list.map((s: BackendStore) => ({
      label: s.storeName,
      value: s.id
    }))
  } catch {
    // ignore
  }
}

function flattenCategoryTree(list: BackendCategory[]): { label: string; value: number }[] {
  const result: { label: string; value: number }[] = []
  const walk = (items: BackendCategory[], prefix: string) => {
    for (const item of items) {
      result.push({ label: prefix + item.name, value: item.id })
      if (item.children && item.children.length > 0) {
        walk(item.children, prefix + item.name + ' / ')
      }
    }
  }
  walk(list, '')
  return result
}

async function loadCategories() {
  try {
    const tree = await getCategoryTree()
    categoryOptions.value = flattenCategoryTree(tree)
  } catch {
    // ignore
  }
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: searchParams.page,
      pageSize: searchParams.pageSize
    }
    if (searchParams.storeId) params.storeId = searchParams.storeId
    if (searchParams.categoryId) params.categoryId = searchParams.categoryId
    if (searchParams.stockStatus !== null) params.stockStatus = searchParams.stockStatus
    if (searchParams.keyword) params.keyword = searchParams.keyword

    const res = await getStocks(params)
    tableData.value = res.list
    pagination.itemCount = res.total
    pagination.page = searchParams.page
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  searchParams.page = 1
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  searchParams.storeId = null
  searchParams.categoryId = null
  searchParams.stockStatus = null
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

const searchKeyword = ref('')

onMounted(() => {
  loadStores()
  loadCategories()
  loadData()
})
</script>

<style scoped>
.stock-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
