<template>
  <div class="stock-outbound">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="单号"
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
          <n-button type="primary" @click="openCreate">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增出库
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendStockOutVO) => row.id"
          remote
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>

    <!-- 新增/编辑出库单 -->
    <n-drawer v-model:show="showForm" :width="600" placement="right">
      <n-drawer-content :title="editingId ? '编辑出库单' : '新增出库单'">
        <n-space vertical :size="16">
          <n-form label-placement="left" label-width="80">
            <n-form-item label="出库类型" required>
              <n-select
                v-model:value="form.outType"
                :options="outTypeOptions"
                placeholder="请选择出库类型"
              />
            </n-form-item>
            <n-form-item label="出库门店" required>
              <n-select
                v-model:value="form.storeId"
                :options="storeOptions"
                placeholder="请选择出库门店"
                @update:value="onStoreChange"
              />
            </n-form-item>
            <n-form-item v-if="form.outType === 2" label="目标门店">
              <n-select
                v-model:value="form.targetStoreId"
                :options="storeOptions"
                placeholder="调拨目标门店"
              />
            </n-form-item>
            <n-form-item label="备注">
              <n-input v-model:value="form.remark" placeholder="备注" />
            </n-form-item>
          </n-form>

          <n-divider>出库商品</n-divider>

          <n-space vertical :size="8">
            <n-space v-for="(item, index) in form.items" :key="index" :size="8" align="center">
              <n-select
                v-model:value="item.productId"
                :options="stockProductOptions"
                :disabled="!form.storeId"
                placeholder="选择商品"
                filterable
                style="width: 260px"
              />
              <n-input-number
                v-model:value="item.quantity"
                :min="1"
                :max="getItemStock(item.productId)"
                placeholder="数量"
                style="width: 100px"
              />
              <n-button type="error" text @click="form.items.splice(index, 1)">删除</n-button>
            </n-space>
            <n-button dashed @click="form.items.push({ productId: null, quantity: 1 })" block>+ 添加商品</n-button>
          </n-space>
        </n-space>

        <template #footer>
          <n-space justify="end">
            <n-button @click="showForm = false">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">提交</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>

    <!-- 详情 -->
    <n-drawer v-model:show="showDetail" :width="550" placement="right">
      <n-drawer-content :title="`出库单 ${detailData?.outNo}`">
        <n-descriptions :column="1" label-placement="left" bordered>
          <n-descriptions-item label="单号">{{ detailData?.outNo }}</n-descriptions-item>
          <n-descriptions-item label="出库类型">{{ outTypeLabel(detailData?.outType) }}</n-descriptions-item>
          <n-descriptions-item label="出库门店">{{ detailData?.storeName }}</n-descriptions-item>
          <n-descriptions-item v-if="detailData?.targetStoreName" label="目标门店">{{ detailData?.targetStoreName }}</n-descriptions-item>
          <n-descriptions-item label="总数量">{{ detailData?.totalQuantity }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="statusTagType(detailData?.status)" size="small">
              {{ statusLabel(detailData?.status) }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="创建人">{{ detailData?.operatorName ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="出库人">{{ detailData?.stockOutUserName ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="出库时间">{{ detailData?.stockOutTime ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="备注">{{ detailData?.remark ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="创建时间">{{ detailData?.createTime }}</n-descriptions-item>
        </n-descriptions>

        <n-divider>出库明细</n-divider>
        <n-data-table
          :columns="itemColumns"
          :data="detailData?.items ?? []"
          size="small"
          :bordered="false"
        />

        <n-divider />
        <n-space justify="end">
          <n-button v-if="detailData?.status === 0" type="info" @click="handleEditFromDetail">编辑</n-button>
          <n-button v-if="detailData?.status === 0" type="error" @click="handleDelete(detailData!)">删除</n-button>
          <n-button v-if="detailData?.status === 0" type="success" :loading="actionLoading" @click="handleConfirm">确认出库</n-button>
          <n-button v-if="detailData?.status === 1" type="warning" :loading="actionLoading" @click="handleRollback">撤销出库</n-button>
          <n-tag v-if="detailData?.status === 1" type="success" size="large">已出库完成</n-tag>
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
  getStockOutList, getStockOutDetail, createStockOut, updateStockOut,
  deleteStockOut, confirmStockOut, rollbackStockOut, getStocks,
  type BackendStockOutVO, type StockOutCreateDTO
} from '../../../api/stock'
import { getStores, type BackendStore } from '../../../api/tenant'

const message = useMessage()
const loading = ref(false)
const tableData = ref<BackendStockOutVO[]>([])
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)

const showForm = ref(false)
const submitting = ref(false)
const showDetail = ref(false)
const detailData = ref<BackendStockOutVO | null>(null)
const actionLoading = ref(false)
const editingId = ref<number | null>(null)

const storeOptions = ref<{ label: string; value: number }[]>([])
const stockProductOptions = ref<{ label: string; value: number; stock: number }[]>([])

const form = reactive({
  outType: null as number | null,
  storeId: null as number | null,
  targetStoreId: null as number | null,
  remark: '',
  items: [] as { productId: number | null; quantity: number }[]
})

const outTypeOptions = [
  { label: '销售出库', value: 1 },
  { label: '调拨出库', value: 2 },
  { label: '报损出库', value: 3 },
  { label: '其他出库', value: 4 }
]

const statusOptions = [
  { label: '待出库', value: 0 },
  { label: '已出库', value: 1 }
]

const searchParams = reactive({ page: 1, pageSize: 10 })

const pagination = reactive<PaginationProps>({
  page: 1, pageSize: 10, itemCount: 0,
  showSizePicker: true, pageSizes: [10, 20, 50]
})

function outTypeLabel(type?: number | null) {
  const map: Record<number, string> = { 1: '销售出库', 2: '调拨出库', 3: '报损出库', 4: '其他出库' }
  return type !== undefined && type !== null ? map[type] ?? String(type) : '-'
}

function statusLabel(status?: number | null) {
  const map: Record<number, string> = { 0: '待出库', 1: '已出库' }
  return status !== undefined && status !== null ? map[status] ?? String(status) : '-'
}

function statusTagType(status?: number | null) {
  const map: Record<number, 'warning' | 'success'> = { 0: 'warning', 1: 'success' }
  return status !== undefined && status !== null ? map[status] ?? 'info' : 'info'
}

function openCreate() {
  editingId.value = null
  form.outType = null
  form.storeId = null
  form.targetStoreId = null
  form.remark = ''
  form.items = []
  showForm.value = true
}

const columns: DataTableColumns<BackendStockOutVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '单号', key: 'outNo', width: 190 },
  {
    title: '类型', key: 'outType', width: 100,
    render(row) { return h(NTag, { size: 'small' }, { default: () => outTypeLabel(row.outType) }) }
  },
  { title: '出库门店', key: 'storeName', width: 120 },
  { title: '总数量', key: 'totalQuantity', width: 80 },
  {
    title: '状态', key: 'status', width: 90,
    render(row) { return h(NTag, { size: 'small', type: statusTagType(row.status) }, { default: () => statusLabel(row.status) }) }
  },
  { title: '创建时间', key: 'createTime', width: 170 },
  {
    title: '操作', key: 'actions', width: 160,
    fixed: 'right',
    render(row) {
      const btns = [
        h(NButton, { size: 'small', type: 'primary', text: true, onClick: () => handleViewDetail(row) }, { default: () => '详情' })
      ]
      if (row.status === 0) {
        btns.push(h(NButton, { size: 'small', type: 'info', text: true, onClick: () => handleEdit(row) }, { default: () => '编辑' }))
        btns.push(h(NButton, { size: 'small', type: 'error', text: true, onClick: () => handleDelete(row) }, { default: () => '删除' }))
      }
      return h(NSpace, { size: 4 }, { default: () => btns })
    }
  }
]

const itemColumns: DataTableColumns<BackendStockOutVO['items'][0]> = [
  { title: '#', key: 'index', width: 50, render(_, index) { return index + 1 } },
  { title: '商品', key: 'productName', width: 180, ellipsis: { tooltip: true } },
  { title: '数量', key: 'quantity', width: 80 },
  { title: '备注', key: 'remark', width: 120, render(row) { return row.remark ?? '-' } }
]

async function loadData() {
  loading.value = true
  try {
    const res = await getStockOutList(searchParams)
    let list = res.list ?? []
    if (searchKeyword.value) {
      const kw = searchKeyword.value.trim().toLowerCase()
      list = list.filter((item: BackendStockOutVO) => item.outNo?.toLowerCase().includes(kw))
    }
    if (searchStatus.value !== null) {
      list = list.filter((item: BackendStockOutVO) => item.status === searchStatus.value)
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
    const storeRes = await getStores({ pageSize: 100 })
    storeOptions.value = storeRes.list.map((s: BackendStore) => ({ label: s.storeName, value: s.id }))
  } catch {
    // ignore
  }
}

async function onStoreChange(storeId: number | null) {
  form.items = []
  stockProductOptions.value = []
  if (!storeId) return
  try {
    const res = await getStocks({ storeId, pageSize: 500 })
    stockProductOptions.value = (res.list ?? [])
      .filter((s: any) => (s.warehouseQuantity ?? 0) > 0)
      .map((s: any) => ({
        label: `${s.productName}（仓库: ${s.warehouseQuantity}）`,
        value: s.productId,
        stock: s.warehouseQuantity ?? 0
      }))
  } catch {
    // ignore
  }
}

function getItemStock(productId: number | null): number {
  if (!productId) return 999999
  const opt = stockProductOptions.value.find(p => p.value === productId)
  return opt ? opt.stock : 999999
}

async function handleViewDetail(row: BackendStockOutVO) {
  try {
    detailData.value = await getStockOutDetail(row.id)
    showDetail.value = true
  } catch (e: any) {
    message.error(e?.message || '加载详情失败')
  }
}

async function handleEdit(row: BackendStockOutVO) {
  try {
    const detail = await getStockOutDetail(row.id)
    editingId.value = row.id
    form.outType = detail.outType
    form.storeId = detail.storeId
    form.targetStoreId = detail.targetStoreId
    form.remark = detail.remark ?? ''
    form.items = detail.items.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
    showForm.value = true
  } catch (e: any) {
    message.error(e?.message || '加载失败')
  }
}

async function handleEditFromDetail() {
  if (!detailData.value) return
  await handleEdit(detailData.value)
  showDetail.value = false
}

async function handleSubmit() {
  if (!form.outType) { message.warning('请选择出库类型'); return }
  if (!form.storeId) { message.warning('请选择出库门店'); return }
  const validItems = form.items.filter(i => i.productId && i.quantity > 0)
  if (validItems.length === 0) { message.warning('请添加出库商品'); return }

  for (const item of validItems) {
    const stockOpt = stockProductOptions.value.find(p => p.value === item.productId)
    if (stockOpt && item.quantity > stockOpt.stock) {
      message.warning(`${stockOpt.label.split('（')[0]} 库存不足，当前库存 ${stockOpt.stock}`)
      return
    }
  }

  submitting.value = true
  try {
    const dto: StockOutCreateDTO = {
      outType: form.outType!,
      storeId: form.storeId!,
      targetStoreId: form.targetStoreId ?? undefined,
      remark: form.remark || undefined,
      items: validItems.map(i => ({ productId: i.productId!, quantity: i.quantity }))
    }
    if (editingId.value) {
      await updateStockOut(editingId.value, dto)
      message.success('更新成功')
    } else {
      await createStockOut(dto)
      message.success('出库单创建成功')
    }
    showForm.value = false
    loadData()
  } catch (e: any) {
    message.error(e?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: BackendStockOutVO) {
  try {
    await deleteStockOut(row.id)
    message.success('删除成功')
    showDetail.value = false
    loadData()
  } catch (e: any) {
    message.error(e?.message || '删除失败')
  }
}

async function handleConfirm() {
  if (!detailData.value) return
  actionLoading.value = true
  try {
    await confirmStockOut(detailData.value.id)
    message.success('出库成功，库存已扣减')
    detailData.value.status = 1
    detailData.value.stockOutTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    loadData()
  } catch (e: any) {
    message.error(e?.message || '出库失败')
  } finally {
    actionLoading.value = false
  }
}

async function handleRollback() {
  if (!detailData.value) return
  actionLoading.value = true
  try {
    await rollbackStockOut(detailData.value.id)
    message.success('已撤销出库，库存已恢复')
    detailData.value.status = 0
    detailData.value.stockOutUserName = null
    detailData.value.stockOutTime = null
    loadData()
  } catch (e: any) {
    message.error(e?.message || '撤销失败')
  } finally {
    actionLoading.value = false
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
.stock-outbound { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
