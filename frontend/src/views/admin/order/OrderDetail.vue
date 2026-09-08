<template>
  <div class="order-detail">
    <n-card :bordered="false">
      <template #header>
        <n-space align="center">
          <n-button text @click="router.back()">
            <template #icon><n-icon :component="ArrowBackOutline" /></template>
            返回
          </n-button>
          <span>订单详情</span>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-space vertical :size="16">
          <n-descriptions :column="3" label-placement="left" bordered>
            <n-descriptions-item label="订单号">{{ order?.orderNo }}</n-descriptions-item>
            <n-descriptions-item label="订单金额">
              <n-text strong style="color: #d03050">¥{{ order?.totalAmount.toFixed(2) }}</n-text>
            </n-descriptions-item>
            <n-descriptions-item label="利润">
              <n-text strong :style="{ color: (order?.profitAmount ?? 0) >= 0 ? '#18a058' : '#d03050' }">
                ¥{{ (order?.profitAmount ?? 0).toFixed(2) }}
              </n-text>
            </n-descriptions-item>
            <n-descriptions-item label="支付方式">
              {{ order?.payMethodName || payMethodLabel(order?.payMethod ?? 0) }}
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="statusTagType(order?.orderStatus ?? 0)" size="small">{{ statusLabel(order?.orderStatus ?? 0) }}</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="下单时间">{{ order?.createTime }}</n-descriptions-item>
            <n-descriptions-item label="收银员">{{ order?.cashierName }}</n-descriptions-item>
            <n-descriptions-item label="会员">{{ order?.memberName || '非会员' }}</n-descriptions-item>
            <n-descriptions-item label="支付时间">{{ order?.payTime || '-' }}</n-descriptions-item>
          </n-descriptions>

          <n-divider>订单商品</n-divider>

          <n-data-table
            :columns="columns"
            :data="order?.items ?? []"
            :row-key="(row: BackendOrderItemVO) => row.id"
            :pagination="false"
            bordered
          />

          <div class="total-row">
            <n-space justify="end">
              <n-text depth="3">共 {{ totalQuantity }} 件商品</n-text>
              <n-text depth="3">利润：</n-text>
              <n-text strong :style="{ color: totalProfit >= 0 ? '#18a058' : '#d03050', fontSize: '16px' }">
                ¥{{ totalProfit.toFixed(2) }}
              </n-text>
              <n-text strong style="font-size: 18px; color: #d03050">合计：¥{{ order?.totalAmount.toFixed(2) }}</n-text>
            </n-space>
          </div>
        </n-space>
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NDataTable, NSpace, NTag, NText, NDivider, NDescriptions, NDescriptionsItem, NSpin, NIcon, NButton } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { getOrder } from '../../../api/order'
import type { BackendOrderVO, BackendOrderItemVO } from '../../../api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const order = ref<BackendOrderVO | null>(null)

const totalQuantity = computed(() => order.value?.items?.reduce((sum, item) => sum + item.quantity, 0) ?? 0)

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

function payMethodLabel(method: number) {
  const map: Record<number, string> = {
    1: '现金',
    2: '微信',
    3: '支付宝',
    4: '银行卡'
  }
  return map[method] ?? '未知'
}

const columns: DataTableColumns<BackendOrderItemVO> = [
  { title: '商品名称', key: 'productName', width: 200 },
  {
    title: '进货价',
    key: 'costAmount',
    width: 90,
    render(row) { return `¥${(row.costAmount ?? 0).toFixed(2)}` }
  },
  {
    title: '售价',
    key: 'salePrice',
    width: 90,
    render(row) { return `¥${row.salePrice.toFixed(2)}` }
  },
  { title: '数量', key: 'quantity', width: 70 },
  {
    title: '小计',
    key: 'totalPrice',
    width: 100,
    render(row) {
      return h('span', { style: { fontWeight: 600 } }, `¥${row.totalPrice.toFixed(2)}`)
    }
  },
  {
    title: '利润',
    key: 'profitAmount',
    width: 100,
    render(row) {
      const profit = row.profitAmount ?? 0
      return h('span', { style: { color: profit >= 0 ? '#18a058' : '#d03050', fontWeight: 600 } }, `¥${profit.toFixed(2)}`)
    }
  }
]

const totalProfit = computed(() => order.value?.items?.reduce((sum, item) => sum + (item.profitAmount ?? 0), 0) ?? 0)

async function loadOrder() {
  const id = Number(route.params.id)
  if (!id) return
  loading.value = true
  try {
    order.value = await getOrder(id)
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadOrder() })
</script>

<style scoped>
.order-detail { padding: 0; }
.total-row { padding: 16px 0; border-top: 1px solid #efeff5; }
</style>
