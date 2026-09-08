<template>
  <div class="report-stock">
    <n-space vertical :size="16">
      <n-grid :cols="4" :x-gap="16">
        <n-gi>
          <n-card>
            <n-statistic label="总库存价值" :value="totalValue" :precision="2">
              <template #prefix>¥</template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card>
            <n-statistic label="预警商品数" :value="totalLowStock">
              <template #suffix>
                <n-text depth="3" style="font-size: 14px">件</n-text>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card>
            <n-statistic label="商品分类数" :value="tableData.length">
              <template #suffix>
                <n-text depth="3" style="font-size: 14px">个</n-text>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card>
            <n-statistic label="SKU总数" :value="totalProductCount">
              <template #suffix>
                <n-text depth="3" style="font-size: 14px">个</n-text>
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
      </n-grid>

      <n-card title="分类库存报表" :bordered="false">
        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: StockCategoryReportVO) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive, h, onMounted } from 'vue'
import { NCard, NDataTable, NSpace, NStatistic, NGrid, NGi, NText } from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { getStockCategoryReport } from '../../../api/report'
import type { StockCategoryReportVO } from '../../../api/report'

const loading = ref(false)
const tableData = ref<StockCategoryReportVO[]>([])

const totalValue = computed(() => tableData.value.reduce((sum, item) => sum + (item.totalValue || 0), 0))
const totalLowStock = computed(() => tableData.value.reduce((sum, item) => sum + (item.lowStockCount || 0), 0))
const totalProductCount = computed(() => tableData.value.reduce((sum, item) => sum + (item.productCount || 0), 0))

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 20,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const columns: DataTableColumns<StockCategoryReportVO> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '分类', key: 'categoryName', width: 120 },
  { title: 'SKU数', key: 'productCount', width: 80 },
  {
    title: '库存总量',
    key: 'totalStock',
    width: 100,
    render(row) { return Number(row.totalStock || 0).toLocaleString() }
  },
  {
    title: '库存价值',
    key: 'totalValue',
    width: 120,
    render(row) { return h('span', { style: { fontWeight: 600 } }, `¥${Number(row.totalValue || 0).toLocaleString()}`) }
  },
  {
    title: '预警商品',
    key: 'lowStockCount',
    width: 100,
    render(row) {
      return h('span', {
        style: { color: (row.lowStockCount || 0) > 0 ? '#d03050' : undefined }
      }, row.lowStockCount || 0)
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getStockCategoryReport()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-stock { padding: 0; }
</style>
