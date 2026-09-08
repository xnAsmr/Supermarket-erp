<template>
  <div class="stock-inbound">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="单号/供应商"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
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
              @update:value="handleSearch"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="editingId = null; createForm.storeId = null; createForm.supplierId = null; createForm.items = []; showCreate = true">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增入库
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendPurchaseInVO) => row.id"
          remote
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>

    <!-- 新增入库单 -->
    <n-drawer v-model:show="showCreate" :width="600" placement="right">
      <n-drawer-content :title="editingId ? '编辑入库单' : '新增入库单'">
        <n-space vertical :size="16">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="门店" required>
              <n-select
                v-model:value="createForm.storeId"
                :options="storeOptions"
                placeholder="请选择门店"
              />
            </n-form-item>
            <n-form-item label="供应商" required>
              <n-select
                v-model:value="createForm.supplierId"
                :options="supplierOptions"
                placeholder="请选择供应商"
                filterable
              />
            </n-form-item>
          </n-form>

          <n-divider>入库商品</n-divider>

          <n-space vertical :size="8">
            <n-space v-for="(item, index) in createForm.items" :key="index" :size="8" align="center">
              <n-select
                v-model:value="item.productId"
                :options="productOptions"
                placeholder="选择商品"
                filterable
                style="width: 200px"
                @update:value="(v) => onProductSelect(v, index)"
              />
              <n-input-number
                v-model:value="item.quantity"
                :min="1"
                placeholder="数量"
                style="width: 100px"
              />
              <n-input-number
                v-model:value="item.purchasePrice"
                :min="0"
                :precision="2"
                placeholder="采购价"
                style="width: 120px"
              />
              <n-button
                type="error"
                text
                @click="createForm.items.splice(index, 1)"
              >
                删除
              </n-button>
            </n-space>
            <n-button dashed @click="addItem" block>+ 添加商品</n-button>
          </n-space>

          <div style="text-align: right; font-weight: 600; margin-top: 8px">
            合计：¥{{ createFormTotal.toFixed(2) }}
          </div>
        </n-space>

        <template #footer>
          <n-space justify="end">
            <n-button @click="showCreate = false">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="handleCreate">提交</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>

    <!-- 入库单详情 -->
    <n-drawer v-model:show="showDetail" :width="550" placement="right">
      <n-drawer-content :title="`入库单 ${detailData?.inNo}`">
        <n-descriptions :column="1" label-placement="left" bordered>
          <n-descriptions-item label="单号">{{ detailData?.inNo }}</n-descriptions-item>
          <n-descriptions-item label="供应商">{{ detailData?.supplierName }}</n-descriptions-item>
          <n-descriptions-item label="门店">{{ detailData?.storeName }}</n-descriptions-item>
          <n-descriptions-item label="总数量">{{ detailData?.totalQuantity }}</n-descriptions-item>
          <n-descriptions-item label="总金额">¥{{ (detailData?.totalAmount ?? 0).toFixed(2) }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="statusTagType(detailData?.status)" size="small">
              {{ statusLabel(detailData?.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="操作人">{{ detailData?.operatorName ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="审核人">{{ detailData?.reviewerName ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="审核时间">{{ detailData?.reviewTime ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="入库人">{{ detailData?.stockInUserName ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="入库时间">{{ detailData?.stockInTime ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ detailData?.createTime }}</n-descriptions-item>
        </n-descriptions>

        <n-divider>入库明细</n-divider>
        <n-data-table
          :columns="itemColumns"
          :data="detailData?.items ?? []"
          size="small"
          :bordered="false"
        />

        <n-divider />
        <n-space justify="end">
          <n-button
            v-if="detailData?.status === 0"
            type="warning"
            :loading="actionLoading"
            @click="handleReview"
          >审核通过</n-button>
          <n-button
            v-if="detailData?.status === 1"
            type="info"
            :loading="actionLoading"
            @click="handleRevokeReview"
          >撤销审核</n-button>
          <n-button
            v-if="detailData?.status === 1"
            type="success"
            :loading="actionLoading"
            @click="handleStockIn"
          >确认入库</n-button>
          <n-button
            v-if="detailData?.status === 2"
            type="warning"
            :loading="actionLoading"
            @click="handleRollbackStockIn"
          >回退入库</n-button>
          <n-tag v-if="detailData?.status === 2" type="success" size="large">已入库完成</n-tag>
        </n-space>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NDrawer, NDrawerContent, NDescriptions, NDescriptionsItem, NDivider,
  NForm, NFormItem, NInputNumber, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import {
  getPurchaseInList, getPurchaseInDetail, createPurchaseIn, updatePurchaseIn,
  deletePurchaseIn, reviewPurchaseIn, revokeReviewPurchaseIn,
  stockInPurchaseIn, rollbackStockInPurchaseIn,
  type BackendPurchaseInVO, type PurchaseInCreateDTO
} from '../../../api/stock'
import { getStores, type BackendStore } from '../../../api/tenant'
import { getSuppliers, type BackendSupplier } from '../../../api/supplier'
import { getProducts, type BackendProduct } from '../../../api/product'

const message = useMessage()
const loading = ref(false)
const tableData = ref<BackendPurchaseInVO[]>([])
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)

const showCreate = ref(false)
const submitting = ref(false)
const showDetail = ref(false)
const detailData = ref<BackendPurchaseInVO | null>(null)
const actionLoading = ref(false)

const editingId = ref<number | null>(null)

const storeOptions = ref<{ label: string; value: number }[]>([])
const supplierOptions = ref<{ label: string; value: number }[]>([])
const productOptions = ref<{ label: string; value: number; purchasePrice: number }[]>([])

const createForm = reactive({
  storeId: null as number | null,
  supplierId: null as number | null,
  items: [] as { productId: number | null; quantity: number; purchasePrice: number }[]
})

const createFormTotal = computed(() => {
  return createForm.items.reduce((sum, item) => {
    return sum + (item.quantity || 0) * (item.purchasePrice || 0)
  }, 0)
})

const searchParams = reactive({ page: 1, pageSize: 10 })

const statusOptions = [
  { label: '待审核', value: 0 },
  { label: '已审核', value: 1 },
  { label: '已入库', value: 2 }
]

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

function statusLabel(status?: number | null) {
  const map: Record<number, string> = { 0: '待审核', 1: '已审核', 2: '已入库' }
  return status !== undefined && status !== null ? map[status] ?? String(status) : '-'
}

function statusTagType(status?: number | null) {
  const map: Record<number, 'warning' | 'info' | 'success'> = { 0: 'warning', 1: 'info', 2: 'success' }
  return status !== undefined && status !== null ? map[status] ?? 'info' : 'info'
}

function addItem() {
  createForm.items.push({ productId: null, quantity: 1, purchasePrice: 0 })
}

function onProductSelect(productId: number | null, index: number) {
  if (!productId) return
  const product = productOptions.value.find(p => p.value === productId)
  if (product) {
    createForm.items[index].purchasePrice = product.purchasePrice
  }
}

const columns: DataTableColumns<BackendPurchaseInVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '单号', key: 'inNo', width: 190 },
  { title: '供应商', key: 'supplierName', width: 140, ellipsis: { tooltip: true } },
  { title: '门店', key: 'storeName', width: 110 },
  { title: '总数量', key: 'totalQuantity', width: 80 },
  {
    title: '总金额',
    key: 'totalAmount',
    width: 110,
    render(row) { return h('span', { style: { fontWeight: 600 } }, `¥${(row.totalAmount ?? 0).toFixed(2)}`) }
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render(row) {
      return h(NTag, { size: 'small', type: statusTagType(row.status) }, {
        default: () => statusLabel(row.status)
      })
    }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render(row) {
      const btns = [
        h(NButton, {
          size: 'small',
          type: 'primary',
          text: true,
          onClick: () => handleViewDetail(row)
        }, { default: () => '详情' })
      ]
      if (row.status === 0) {
        btns.push(h(NButton, {
          size: 'small',
          type: 'info',
          text: true,
          onClick: () => handleEdit(row)
        }, { default: () => '编辑' }))
        btns.push(h(NButton, {
          size: 'small',
          type: 'error',
          text: true,
          onClick: () => handleDelete(row)
        }, { default: () => '删除' }))
      }
      return h(NSpace, { size: 4 }, { default: () => btns })
    }
  }
]

const itemColumns: DataTableColumns<BackendPurchaseInVO['items'][0]> = [
  { title: '#', key: 'index', width: 50, render(_, index) { return index + 1 } },
  { title: '商品', key: 'productName', width: 160, ellipsis: { tooltip: true } },
  { title: '数量', key: 'quantity', width: 70 },
  {
    title: '采购价',
    key: 'purchasePrice',
    width: 90,
    render(row) { return `¥${(row.purchasePrice ?? 0).toFixed(2)}` }
  },
  {
    title: '小计',
    key: 'totalPrice',
    width: 100,
    render(row) { return h('span', { style: { fontWeight: 600 } }, `¥${(row.totalPrice ?? 0).toFixed(2)}`) }
  },
  { title: '批次号', key: 'batchNo', width: 100, render(row) { return row.batchNo ?? '-' } }
]

async function loadData() {
  loading.value = true
  try {
    const res = await getPurchaseInList(searchParams)
    let list = res.list ?? res.records ?? []
    if (searchKeyword.value) {
      const kw = searchKeyword.value.trim().toLowerCase()
      list = list.filter((item: BackendPurchaseInVO) =>
        item.inNo?.toLowerCase().includes(kw) ||
        item.supplierName?.toLowerCase().includes(kw)
      )
    }
    if (searchStatus.value !== null) {
      list = list.filter((item: BackendPurchaseInVO) => item.status === searchStatus.value)
    }
    tableData.value = list
    pagination.itemCount = res.total ?? list.length
    pagination.page = searchParams.page
  } catch (e: any) {
    message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [storeRes, supplierRes, productRes] = await Promise.all([
      getStores({ pageSize: 100 }),
      getSuppliers({ pageSize: 100 }),
      getProducts({ pageSize: 200 })
    ])
    storeOptions.value = storeRes.list.map((s: BackendStore) => ({ label: s.storeName, value: s.id }))
    supplierOptions.value = supplierRes.list.map((s: BackendSupplier) => ({ label: s.name, value: s.id }))
    productOptions.value = productRes.list.map((p: BackendProduct) => ({
      label: `${p.name}${p.spec ? ' (' + p.spec + ')' : ''}`,
      value: p.id,
      purchasePrice: p.purchasePrice
    }))
  } catch {
    // ignore
  }
}

async function handleViewDetail(row: BackendPurchaseInVO) {
  try {
    detailData.value = await getPurchaseInDetail(row.id)
    showDetail.value = true
  } catch (e: any) {
    message.error(e?.message || '加载详情失败')
  }
}

async function handleEdit(row: BackendPurchaseInVO) {
  try {
    const detail = await getPurchaseInDetail(row.id)
    createForm.storeId = detail.storeId
    createForm.supplierId = detail.supplierId
    createForm.items = detail.items.map(item => ({
      productId: item.productId,
      quantity: item.quantity,
      purchasePrice: item.purchasePrice
    }))
    editingId.value = row.id
    showCreate.value = true
  } catch (e: any) {
    message.error(e?.message || '加载失败')
  }
}

async function handleDelete(row: BackendPurchaseInVO) {
  try {
    await deletePurchaseIn(row.id)
    message.success('删除成功')
    loadData()
  } catch (e: any) {
    message.error(e?.message || '删除失败')
  }
}

async function handleReview() {
  if (!detailData.value) return
  actionLoading.value = true
  try {
    await reviewPurchaseIn(detailData.value.id)
    message.success('审核通过')
    detailData.value.status = 1
    loadData()
  } catch (e: any) {
    message.error(e?.message || '审核失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleRevokeReview() {
  if (!detailData.value) return
  actionLoading.value = true
  try {
    await revokeReviewPurchaseIn(detailData.value.id)
    message.success('已撤销审核')
    detailData.value.status = 0
    detailData.value.reviewerName = null
    detailData.value.reviewTime = null
    loadData()
  } catch (e: any) {
    message.error(e?.message || '撤销失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleStockIn() {
  if (!detailData.value) return
  actionLoading.value = true
  try {
    await stockInPurchaseIn(detailData.value.id)
    message.success('入库成功，库存已更新')
    detailData.value.status = 2
    detailData.value.stockInTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    loadData()
  } catch (e: any) {
    message.error(e?.message || '入库失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleRollbackStockIn() {
  if (!detailData.value) return
  actionLoading.value = true
  try {
    await rollbackStockInPurchaseIn(detailData.value.id)
    message.success('已回退入库，库存已扣减')
    detailData.value.status = 1
    detailData.value.stockInUserName = null
    detailData.value.stockInTime = null
    loadData()
  } catch (e: any) {
    message.error(e?.message || '回退失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleCreate() {
  if (!createForm.storeId) { message.warning('请选择门店'); return }
  if (!createForm.supplierId) { message.warning('请选择供应商'); return }
  if (createForm.items.length === 0) { message.warning('请添加入库商品'); return }

  const validItems = createForm.items.filter(i => i.productId && i.quantity > 0)
  if (validItems.length === 0) { message.warning('请填写完整的商品和数量'); return }

  submitting.value = true
  try {
    const dto: PurchaseInCreateDTO = {
      storeId: createForm.storeId!,
      supplierId: createForm.supplierId!,
      items: validItems.map(i => ({
        productId: i.productId!,
        quantity: i.quantity,
        purchasePrice: i.purchasePrice
      }))
    }
    if (editingId.value) {
      await updatePurchaseIn(editingId.value, dto)
      message.success('入库单更新成功')
    } else {
      await createPurchaseIn(dto)
      message.success('入库单创建成功')
    }
    showCreate.value = false
    editingId.value = null
    createForm.storeId = null
    createForm.supplierId = null
    createForm.items = []
    loadData()
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleSearch() { searchParams.page = 1; loadData() }
function handleReset() {
  searchKeyword.value = ''
  searchStatus.value = null
  searchParams.page = 1
  loadData()
}
function handlePageChange(page: number) { searchParams.page = page; loadData() }
function handlePageSizeChange(pageSize: number) {
  searchParams.pageSize = pageSize
  searchParams.page = 1
  loadData()
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<style scoped>
.stock-inbound { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
