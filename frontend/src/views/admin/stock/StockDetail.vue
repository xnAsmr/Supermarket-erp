<template>
  <div class="stock-detail">
    <n-card :bordered="false">
      <template #header>
        <n-space align="center">
          <n-button text @click="router.back()">
            <template #icon><n-icon :component="ArrowBackOutline" /></template>
            返回
          </n-button>
          <span>库存详情</span>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-descriptions :column="2" label-placement="left" bordered>
          <n-descriptions-item label="商品名称" :span="2">{{ detail?.productName }}</n-descriptions-item>
          <n-descriptions-item label="条码">{{ detail?.barcode }}</n-descriptions-item>
          <n-descriptions-item label="规格">{{ detail?.spec ?? '-' }}</n-descriptions-item>
          <n-descriptions-item label="单位">{{ detail?.unit }}</n-descriptions-item>
          <n-descriptions-item label="门店">{{ detail?.storeName ?? '全部' }}</n-descriptions-item>
          <n-descriptions-item label="仓库库存">
            <n-tag :type="(detail?.warehouseQuantity ?? 0) > 0 ? 'success' : 'error'" size="small">
              {{ detail?.warehouseQuantity ?? 0 }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="货架库存">
            <n-tag :type="(detail?.shelfQuantity ?? 0) > 0 ? 'success' : 'error'" size="small">
              {{ detail?.shelfQuantity ?? 0 }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="待入库">
            <n-tag :type="(detail?.pendingInQuantity ?? 0) > 0 ? 'success' : 'info'" size="small">
              {{ detail?.pendingInQuantity ?? 0 }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="待出库">
            <n-tag :type="(detail?.pendingOutQuantity ?? 0) > 0 ? 'warning' : 'info'" size="small">
              {{ detail?.pendingOutQuantity ?? 0 }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="平均成本">¥{{ (detail?.avgCost ?? 0).toFixed(2) }}</n-descriptions-item>
        </n-descriptions>

        <n-divider>出入库记录（最近50条）</n-divider>
        <n-data-table
          :columns="logColumns"
          :data="detail?.logs ?? []"
          :bordered="false"
          :max-height="500"
        />
      </n-spin>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NCard, NDataTable, NSpace, NIcon, NTag, NSpin, NDescriptions, NDescriptionsItem, NDivider, NButton, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { getStockDetail, type StockDetailVO } from '../../../api/stock'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const loading = ref(false)
const detail = ref<StockDetailVO | null>(null)

const logColumns: DataTableColumns<StockDetailVO['logs'][0]> = [
  { title: '类型', key: 'logTypeName', width: 90 },
  {
    title: '变动数量',
    key: 'quantity',
    width: 90,
    render(row) { return Number(row.quantity) }
  },
  {
    title: '变动前',
    key: 'beforeStock',
    width: 80,
    render(row) { return Number(row.beforeStock) }
  },
  {
    title: '变动后',
    key: 'afterStock',
    width: 80,
    render(row) { return Number(row.afterStock) }
  },
  { title: '关联单号', key: 'relatedNo', width: 180, ellipsis: { tooltip: true } },
  { title: '操作人', key: 'operatorName', width: 80 },
  { title: '时间', key: 'createTime', width: 170 }
]

async function loadData() {
  const productId = Number(route.query.productId)
  const storeId = route.query.storeId ? Number(route.query.storeId) : undefined
  if (!productId) {
    message.error('缺少商品ID')
    return
  }
  loading.value = true
  try {
    detail.value = await getStockDetail(productId, storeId)
  } catch (e: any) {
    message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stock-detail { padding: 0; }
</style>
