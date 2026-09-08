<template>
  <div class="sales-report">
    <n-space vertical :size="20">
      <n-card :bordered="false">
        <n-space :size="12" align="center">
          <n-date-picker
            v-model:value="dateRange"
            type="daterange"
            style="width: 280px"
          />
          <n-button type="primary" :loading="loading" :disabled="loading || cooldown > 0" @click="loadData">
            <template #icon><n-icon :component="SearchOutline" /></template>
            {{ cooldown > 0 ? `${cooldown}s后可查询` : '查询' }}
          </n-button>
          <n-button @click="resetDateRange" :disabled="loading">重置</n-button>
          <n-button @click="handleExport" :disabled="loading">导出</n-button>
        </n-space>
      </n-card>

      <n-spin :show="loading">
        <div class="summary-grid">
        <n-card class="summary-card" :bordered="false">
          <div class="summary-label">总销售额</div>
          <div class="summary-value">{{ formatMoney(summaryData.totalSales) }}</div>
        </n-card>
        <n-card class="summary-card" :bordered="false">
          <div class="summary-label">订单数</div>
          <div class="summary-value">{{ summaryData.totalOrders }}</div>
        </n-card>
        <n-card class="summary-card" :bordered="false">
          <div class="summary-label">客单价</div>
          <div class="summary-value">{{ formatMoney(summaryData.avgOrderAmount) }}</div>
        </n-card>
        <n-card class="summary-card" :bordered="false">
          <div class="summary-label">毛利润</div>
          <div class="summary-value profit">{{ formatMoney(summaryData.totalProfit) }}</div>
        </n-card>
      </div>
      </n-spin>

      <n-card title="趋势图" :bordered="false">
        <div ref="trendChartRef" style="height: 350px"></div>
      </n-card>

      <n-card title="每日明细" :bordered="false">
        <n-data-table
          :columns="columns"
          :data="dailyData"
          :bordered="false"
          :pagination="false"
          size="small"
          :row-key="(row: any) => row.date"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, h } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NDatePicker, NIcon, NSpin, useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getDailyReport } from '@/api/report'
import { formatMoney } from '@/utils/format'

const message = useMessage()
const loading = ref(false)
const cooldown = ref(0)
let cooldownTimer: ReturnType<typeof setInterval> | null = null
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
const dateRange = ref<[number, number]>(getDefaultDateRange())

interface DailyReport {
  date: string
  sales: number
  orders: number
  avgAmount: number
  profit: number
}

const summaryData = ref({
  totalSales: 0,
  totalOrders: 0,
  avgOrderAmount: 0,
  totalProfit: 0
})

const dailyData = ref<DailyReport[]>([])

const columns: DataTableColumns<DailyReport> = [
  { title: '日期', key: 'date', width: 120 },
  {
    title: '销售额',
    key: 'sales',
    width: 120,
    render(row) { return formatMoney(row.sales) }
  },
  { title: '订单数', key: 'orders', width: 80 },
  {
    title: '客单价',
    key: 'avgAmount',
    width: 100,
    render(row) { return formatMoney(row.avgAmount) }
  },
  {
    title: '毛利润',
    key: 'profit',
    width: 120,
    render(row) {
      return h('span', { style: { color: row.profit > 0 ? '#18a058' : '#d03050', fontWeight: 600 } }, formatMoney(row.profit))
    }
  },
  {
    title: '利润率',
    key: 'profit',
    width: 80,
    render(row) {
      const rate = row.sales > 0 ? (row.profit / row.sales * 100).toFixed(1) : '0.0'
      return `${rate}%`
    }
  }
]

async function loadData() {
  if (loading.value || cooldown.value > 0) return
  loading.value = true

  try {
    const [start, end] = dateRange.value
    const startDate = new Date(start)
    const endDate = new Date(end)

    const totalSales = ref(0)
    const totalOrders = ref(0)
    const totalProfit = ref(0)
    const dailyReports: DailyReport[] = []

    const current = new Date(startDate)
    while (current <= endDate) {
      const y = current.getFullYear()
      const m = String(current.getMonth() + 1).padStart(2, '0')
      const d = String(current.getDate()).padStart(2, '0')
      const dateStr = `${y}-${m}-${d}`
      try {
        const report = await getDailyReport(dateStr)
        dailyReports.push({
          date: report.statDate,
          sales: report.payAmount,
          orders: report.orderCount,
          avgAmount: report.orderCount > 0 ? report.payAmount / report.orderCount : 0,
          profit: report.profitAmount
        })
        totalSales.value += report.payAmount
        totalOrders.value += report.orderCount
        totalProfit.value += report.profitAmount
      } catch {
        dailyReports.push({
          date: dateStr,
          sales: 0,
          orders: 0,
          avgAmount: 0,
          profit: 0
        })
      }
      current.setDate(current.getDate() + 1)
    }

    summaryData.value = {
      totalSales: totalSales.value,
      totalOrders: totalOrders.value,
      avgOrderAmount: totalOrders.value > 0 ? totalSales.value / totalOrders.value : 0,
      totalProfit: totalProfit.value
    }
    dailyData.value = dailyReports
    await initTrendChart()
  } finally {
    loading.value = false
    cooldown.value = 3
    if (cooldownTimer) clearInterval(cooldownTimer)
    cooldownTimer = setInterval(() => {
      cooldown.value--
      if (cooldown.value <= 0) {
        clearInterval(cooldownTimer!)
        cooldownTimer = null
      }
    }, 1000)
  }
}

function handleExport() {
  message.info('导出功能开发中')
}

function getDefaultDateRange(): [number, number] {
  const now = new Date()
  const end = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6)
  return [start.getTime(), end.getTime()]
}

function resetDateRange() {
  dateRange.value = getDefaultDateRange()
  loadData()
}

async function initTrendChart() {
  if (!trendChartRef.value || dailyData.value.length === 0) return
  await nextTick()

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const dates = dailyData.value.map(d => d.date)
  const salesData = dailyData.value.map(d => d.sales)
  const profitData = dailyData.value.map(d => d.profit)
  const ordersData = dailyData.value.map(d => d.orders)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach((p: any) => {
          const unit = p.seriesName === '订单数' ? '单' : '元'
          result += `${p.marker} ${p.seriesName}: ${unit === '元' ? '¥' : ''}${p.value.toFixed(unit === '元' ? 2 : 0)}${unit !== '元' ? unit : ''}<br/>`
        })
        return result
      }
    },
    legend: { data: ['销售额', '毛利润', '订单数'], top: 10 },
    grid: { left: '3%', right: '4%', top: 60, bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: dates.length > 14 ? 45 : 0 } },
    yAxis: [
      { type: 'value', name: '金额(元)', position: 'left' },
      { type: 'value', name: '订单数', position: 'right' }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: salesData,
        itemStyle: { color: '#18a058' },
        areaStyle: { color: 'rgba(24,160,88,0.1)' }
      },
      {
        name: '毛利润',
        type: 'line',
        smooth: true,
        data: profitData,
        itemStyle: { color: '#2080f0' },
        areaStyle: { color: 'rgba(32,128,240,0.1)' }
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        data: ordersData,
        itemStyle: { color: 'rgba(24,160,88,0.3)' }
      }
    ]
  })
}

function handleResize() {
  trendChart?.resize()
}

onMounted(() => {
  dateRange.value = getDefaultDateRange()
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  if (cooldownTimer) clearInterval(cooldownTimer)
})
</script>

<style scoped>
.sales-report {
  padding: 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.summary-card {
  border-radius: 8px;
  text-align: center;
  padding: 8px 0;
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.summary-value.profit {
  color: #18a058;
}
</style>
