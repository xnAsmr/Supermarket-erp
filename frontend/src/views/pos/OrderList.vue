<template>
  <div class="pos-order-list">
    <n-card title="今日订单" :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="订单号"
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
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendOrderVO) => row.id"
          :loading="loading"
          :pagination="pagination"
          remote
        />
      </n-space>
    </n-card>

    <n-modal v-model:show="showRefundModal" preset="dialog" title="确认退款" positive-text="确认退款" negative-text="取消"
      @positive-click="handleRefund" @negative-click="showRefundModal = false">
      <n-space vertical>
        <n-text>订单号：{{ refundTarget?.orderNo }}</n-text>
        <n-text>退款金额：</n-text>
        <n-input-number v-model:value="refundAmount" :precision="2" style="width: 100%" />
      </n-space>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag, NModal, NInputNumber, NText, useMessage } from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { getOrders, refundOrder } from '@/api/order'
import type { BackendOrderVO } from '@/api/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const loading = ref(false)
const searchKeyword = ref('')
const searchStatus = ref<number | null>(null)
const tableData = ref<BackendOrderVO[]>([])

const showRefundModal = ref(false)
const refundTarget = ref<BackendOrderVO | null>(null)
const refundAmount = ref(0)

const statusOptions = [
  { label: '已支付', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已退款', value: 4 }
]

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onChange: (page: number) => { pagination.page = page; loadData() },
  onUpdatePageSize: (size: number) => { pagination.pageSize = size; pagination.page = 1; loadData() }
})

function getTodayRange(): [Date, Date] {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  return [start, end]
}

function payMethodLabel(method: string | null) {
  if (method == null) return '未知'
  const map: Record<string, string> = { cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', stored: '储值卡', combined: '组合' }
  return map[method] ?? method
}

function payMethodTagType(method: string | null) {
  if (method == null) return 'default'
  const map: Record<string, 'warning' | 'success' | 'info' | 'default' | 'error'> = { cash: 'warning', wechat: 'success', alipay: 'info', card: 'default', stored: 'error', combined: 'warning' }
  return map[method] ?? 'default'
}

function statusLabel(status: number) {
  const map: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消', 4: '已退款' }
  return map[status] ?? '未知'
}

function statusTagType(status: number) {
  const map: Record<number, 'warning' | 'success' | 'error' | 'info'> = { 0: 'warning', 1: 'success', 2: 'success', 3: 'error', 4: 'info' }
  return map[status] ?? 'info'
}

const columns: DataTableColumns<BackendOrderVO> = [
  { title: '订单号', key: 'orderNo', width: 190 },
  {
    title: '金额',
    key: 'payAmount',
    width: 100,
    render(row) { return h('span', { style: { fontWeight: 600 } }, `¥${(row.payAmount ?? 0).toFixed(2)}`) }
  },
  {
    title: '支付方式',
    key: 'payMethod',
    width: 90,
    render(row) {
      return h(NTag, { size: 'small', type: payMethodTagType(row.payMethod) as any, bordered: false }, { default: () => row.payMethodName || payMethodLabel(row.payMethod) })
    }
  },
  { title: '收银员', key: 'cashierName', width: 80 },
  { title: '时间', key: 'createTime', width: 160 },
  {
    title: '状态',
    key: 'orderStatus',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: statusTagType(row.orderStatus) }, { default: () => statusLabel(row.orderStatus) })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render(row) {
      const btns = []
      btns.push(h(NButton, {
        size: 'small', type: 'primary', text: true,
        onClick: () => router.push({ path: '/pos/orderDetail', query: { id: row.id } })
      }, { default: () => '详情' }))
      if (row.orderStatus === 1 || row.orderStatus === 2) {
        btns.push(h(NButton, {
          size: 'small', type: 'error', text: true,
          onClick: () => openRefundModal(row)
        }, { default: () => '退款' }))
      }
      return h(NSpace, { size: 4 }, { default: () => btns })
    }
  }
]

function openRefundModal(row: BackendOrderVO) {
  refundTarget.value = row
  refundAmount.value = row.payAmount ?? 0
  showRefundModal.value = true
}

async function handleRefund() {
  if (!refundTarget.value) return
  if (refundAmount.value <= 0) {
    message.warning('退款金额必须大于0')
    return
  }
  try {
    await refundOrder(refundTarget.value.id)
    message.success('退款成功')
    showRefundModal.value = false
    loadData()
  } catch {
    message.error('退款失败')
  }
}

async function loadData() {
  loading.value = true
  try {
    const today = getTodayRange()
    const fmt = (d: Date) => {
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      const s = String(d.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${day} ${h}:${min}:${s}`
    }
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      startDate: fmt(today[0]),
      endDate: fmt(today[1]),
      operatorId: userStore.userInfo?.id
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchStatus.value != null) params.orderStatus = searchStatus.value
    const res = await getOrders(params)
    tableData.value = res.list
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

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

onMounted(() => { loadData() })
</script>

<style scoped>
.pos-order-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
