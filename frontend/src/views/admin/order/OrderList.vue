<template>
  <div class="order-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchParams.keyword"
              placeholder="订单号/会员名/手机号"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-date-picker
              v-model:value="dateRange"
              type="daterange"
              clearable
              style="width: 260px"
              @update:value="handleDateChange"
            />
            <n-select
              v-model:value="searchParams.orderStatus"
              placeholder="订单状态"
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
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendOrderVO) => row.id"
          remote
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>

    <!-- 编辑备注弹窗 -->
    <n-modal v-model:show="showRemarkModal" preset="card" title="修改备注" style="width: 450px">
      <n-input
        v-model:value="editingRemark"
        type="textarea"
        :rows="3"
        placeholder="请输入备注"
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showRemarkModal = false">取消</n-button>
          <n-button type="primary" @click="handleSaveRemark">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NModal, NText, NDatePicker, useMessage, useDialog
} from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import {
  getOrders, cancelOrder, refundOrder, deleteOrder, updateOrderRemark
} from '../../../api/order'
import type { BackendOrderVO } from '../../../api/order'
import { formatDateTime } from '../../../utils/format'
import { hasPermission } from '../../../directives/permission'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const tableData = ref<BackendOrderVO[]>([])
const dateRange = ref<[number, number] | null>(null)

const showRemarkModal = ref(false)
const editingOrderId = ref(0)
const editingRemark = ref('')

const searchParams = reactive({
  keyword: '',
  orderStatus: undefined as number | undefined,
  startDate: '' as string | null,
  endDate: '' as string | null,
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

const statusOptions = [
  { label: '待支付', value: 0 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
  { label: '已退款', value: 4 }
]

function statusLabel(status: number) {
  const map: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已完成',
    3: '已取消',
    4: '已退款'
  }
  return map[status] ?? '未知'
}

function statusTagType(status: number) {
  const map: Record<number, 'success' | 'warning' | 'error' | 'info'> = {
    0: 'warning',
    1: 'success',
    2: 'success',
    3: 'error',
    4: 'info'
  }
  return map[status] ?? 'info'
}

const columns: DataTableColumns<BackendOrderVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '订单号', key: 'orderNo', width: 220 },
  {
    title: '金额',
    key: 'payAmount',
    width: 100,
    render(row) {
      return h('span', { style: { fontWeight: 600 } }, `¥${row.payAmount.toFixed(2)}`)
    }
  },
  {
    title: '支付方式',
    key: 'payMethodName',
    width: 100,
    render(row) {
      const colorMap: Record<string, string> = {
        cash: 'default',
        wechat: 'success',
        alipay: 'info',
        card: 'warning',
        stored: 'error',
        combined: 'purple'
      }
      const type = colorMap[row.payMethod || ''] || 'default'
      return h(NTag, { size: 'small', type: type as any, bordered: false }, { default: () => row.payMethodName || '-' })
    }
  },
  {
    title: '状态',
    key: 'orderStatus',
    width: 90,
    render(row) {
      return h(NTag, { size: 'small', type: statusTagType(row.orderStatus) }, { default: () => statusLabel(row.orderStatus) })
    }
  },
  { title: '收银员', key: 'cashierName', width: 100 },
  {
    title: '会员',
    key: 'memberName',
    width: 100,
    render(row) { return row.memberName || '-' }
  },
  {
    title: '时间',
    key: 'createTime',
    width: 160,
    render(row) { return formatDateTime(row.createTime) }
  },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render(row) {
      const buttons: any[] = []

      buttons.push(h(NButton, {
        size: 'small',
        type: 'primary',
        text: true,
        onClick: () => handleViewDetail(row)
      }, { default: () => '详情' }))

      if (row.orderStatus === 0 && hasPermission('oms:order:cancel')) {
        buttons.push(h(NButton, {
          size: 'small',
          type: 'warning',
          text: true,
          onClick: () => handleCancel(row)
        }, { default: () => '取消' }))
      }

      if ((row.orderStatus === 1 || row.orderStatus === 2) && hasPermission('oms:order:refund')) {
        buttons.push(h(NButton, {
          size: 'small',
          type: 'error',
          text: true,
          onClick: () => handleRefund(row)
        }, { default: () => '退款' }))
      }

      buttons.push(h(NButton, {
        size: 'small',
        text: true,
        onClick: () => handleEditRemark(row)
      }, { default: () => '备注' }))

      if (row.orderStatus === 0 || row.orderStatus === 3 || row.orderStatus === 4) {
        buttons.push(h(NButton, {
          size: 'small',
          type: 'error',
          text: true,
          onClick: () => handleDelete(row)
        }, { default: () => '删除' }))
      }

      return h(NSpace, { size: 2 }, { default: () => buttons })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await getOrders({
      ...searchParams,
      startDate: searchParams.startDate || undefined,
      endDate: searchParams.endDate || undefined
    })
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
  searchParams.keyword = ''
  searchParams.orderStatus = undefined
  searchParams.startDate = ''
  searchParams.endDate = ''
  dateRange.value = null
  searchParams.page = 1
  loadData()
}

function handleDateChange(value: [number, number] | null) {
  if (value && value.length === 2) {
    searchParams.startDate = new Date(value[0]).toISOString().slice(0, 10)
    searchParams.endDate = new Date(value[1]).toISOString().slice(0, 10)
  } else {
    searchParams.startDate = ''
    searchParams.endDate = ''
  }
  handleSearch()
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

function handleViewDetail(row: BackendOrderVO) {
  router.push({ path: '/admin/order/detail/' + row.id })
}

function handleCancel(row: BackendOrderVO) {
  dialog.warning({
    title: '取消订单',
    content: `确定要取消订单 ${row.orderNo} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await cancelOrder(row.id)
        message.success('订单已取消')
        loadData()
      } catch {
        message.error('操作失败')
      }
    }
  })
}

function handleRefund(row: BackendOrderVO) {
  dialog.error({
    title: '退款订单',
    content: `确定要对订单 ${row.orderNo} 进行退款吗？退款后库存将恢复，会员积分/余额将回退。`,
    positiveText: '确定退款',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await refundOrder(row.id)
        message.success('退款成功，库存已恢复')
        loadData()
      } catch {
        message.error('退款失败')
      }
    }
  })
}

function handleDelete(row: BackendOrderVO) {
  const needRestore = row.orderStatus === 1 || row.orderStatus === 2
  const content = needRestore
    ? `订单 ${row.orderNo} 已支付/已完成，删除后库存将自动恢复，会员积分/余额将回退。确定删除吗？`
    : `确定要删除订单 ${row.orderNo} 吗？`

  dialog.error({
    title: '删除订单',
    content,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteOrder(row.id)
        message.success('订单已删除')
        loadData()
      } catch {
        message.error('删除失败')
      }
    }
  })
}

function handleEditRemark(row: BackendOrderVO) {
  editingOrderId.value = row.id
  editingRemark.value = row.remark || ''
  showRemarkModal.value = true
}

async function handleSaveRemark() {
  try {
    await updateOrderRemark(editingOrderId.value, editingRemark.value)
    message.success('备注已保存')
    showRemarkModal.value = false
    loadData()
  } catch {
    message.error('保存失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-list {
  padding: 0;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
