<template>
  <div class="payment-flow">
    <n-card title="支付方式对账" :bordered="false">
      <n-space vertical :size="16">
        <!-- 实际收入汇总 -->
        <n-card size="small" :bordered="false" style="background: linear-gradient(135deg, #f6ffed 0%, #e6fffb 100%)">
          <n-space justify="space-between" align="center">
            <n-space align="center" :size="16">
              <n-statistic label="今日实际收入">
                <template #prefix>¥</template>
                {{ realRevenue.toFixed(2) }}
              </n-statistic>
              <n-statistic label="今日储值卡消费">
                <template #prefix>¥</template>
                {{ storedValueSpend.toFixed(2) }}
              </n-statistic>
              <n-statistic label="今日总流水">
                <template #prefix>¥</template>
                {{ totalFlow.toFixed(2) }}
              </n-statistic>
            </n-space>
            <n-tag type="warning" size="small">储值卡消费不计入收入</n-tag>
          </n-space>
        </n-card>

        <!-- 支付方式余额卡片 -->
        <n-grid :cols="statCards.length || 5" :x-gap="12">
          <n-gi v-for="stat in statCards" :key="stat.code">
            <n-card size="small" :style="{ borderTop: `3px solid ${methodColor(stat.code)}` }">
              <n-space justify="space-between" align="center">
                <n-text depth="3" style="font-size: 12px">{{ stat.name }}</n-text>
                <n-tag v-if="stat.code === 'stored'" type="warning" size="tiny" :bordered="false">负债</n-tag>
                <n-button v-else size="tiny" text type="primary" @click="openRechargeModal(stat)">充值</n-button>
              </n-space>
              <n-text strong style="font-size: 20px; color: #18a058">
                ¥{{ (stat.balance || 0).toFixed(2) }}
              </n-text>
              <n-space style="margin-top: 8px">
                <n-text depth="3" style="font-size: 11px">
                  入 ¥{{ (stat.totalInflow || 0).toFixed(2) }}
                </n-text>
                <n-text depth="3" style="font-size: 11px">
                  出 ¥{{ (stat.totalOutflow || 0).toFixed(2) }}
                </n-text>
              </n-space>
              <n-text v-if="stat.code === 'stored'" depth="3" style="font-size: 10px; display: block; margin-top: 4px">
                会员预存款，非实际收入
              </n-text>
            </n-card>
          </n-gi>
        </n-grid>

        <!-- 趋势图表 -->
        <n-card title="每日收支趋势" size="small" :bordered="false">
          <n-space :size="12" style="margin-bottom: 12px">
            <n-date-picker
              v-model:value="dateRange"
              type="daterange"
              clearable
              style="width: 260px"
            />
            <n-button type="primary" @click="loadDailyFlow">查询</n-button>
          </n-space>
          <div ref="trendChartRef" style="height: 350px"></div>
        </n-card>

        <!-- 收支占比 -->
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-card title="实际收入占比（不含储值卡）" size="small" :bordered="false">
              <div ref="inflowPieRef" style="height: 300px"></div>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="支出占比" size="small" :bordered="false">
              <div ref="outflowPieRef" style="height: 300px"></div>
            </n-card>
          </n-gi>
        </n-grid>
      </n-space>
    </n-card>

    <!-- 充值弹窗 -->
    <n-modal v-model:show="showRechargeModal" preset="card" title="充值/调整余额" style="width: 400px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="支付方式">
          <n-text strong>{{ rechargeTarget?.name }}</n-text>
        </n-form-item>
        <n-form-item label="当前余额">
          <n-text type="success">¥{{ (rechargeTarget?.balance || 0).toFixed(2) }}</n-text>
        </n-form-item>
        <n-form-item label="调整金额">
          <n-input-number v-model:value="rechargeAmount" :precision="2" style="width: 100%" />
          <n-text depth="3" style="font-size: 11px; margin-top: 4px">正数为充值，负数为扣减</n-text>
        </n-form-item>
        <n-form-item label="调整后余额">
          <n-text :type="(rechargeTarget?.balance || 0) + rechargeAmount >= 0 ? 'success' : 'error'" strong>
            ¥{{ ((rechargeTarget?.balance || 0) + rechargeAmount).toFixed(2) }}
          </n-text>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="rechargeRemark" placeholder="如：期初余额/银行对账调整" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showRechargeModal = false">取消</n-button>
          <n-button type="primary" @click="handleRecharge">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import {
  NCard, NGrid, NGi, NText, NTag, NSpace, NDatePicker, NButton,
  NModal, NForm, NFormItem, NInput, NInputNumber, NStatistic, useMessage
} from 'naive-ui'
import * as echarts from 'echarts'
import { getPaymentMethods, rechargePaymentMethod } from '../../../api/paymentMethod'
import type { PaymentMethod } from '../../../api/paymentMethod'
import { getPaymentMethodStats, getDailyPaymentFlow } from '../../../api/order'
import type { PaymentMethodStatVO, DailyPaymentFlowVO } from '../../../api/order'

const message = useMessage()
const statCards = ref<PaymentMethod[]>([])
const dailyFlow = ref<DailyPaymentFlowVO[]>([])
const dateRange = ref<[number, number] | null>(null)

const trendChartRef = ref<HTMLElement>()
const inflowPieRef = ref<HTMLElement>()
const outflowPieRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let inflowPie: echarts.ECharts | null = null
let outflowPie: echarts.ECharts | null = null

const showRechargeModal = ref(false)
const rechargeTarget = ref<PaymentMethod | null>(null)
const rechargeAmount = ref(0)
const rechargeRemark = ref('')

const realRevenue = computed(() => {
  return statCards.value
    .filter(s => s.code !== 'stored')
    .reduce((sum, s) => sum + (s as any).totalInflow || 0, 0)
})

const storedValueSpend = computed(() => {
  const stored = statCards.value.find(s => s.code === 'stored')
  return stored ? (stored as any).totalOutflow || 0 : 0
})

const totalFlow = computed(() => {
  return statCards.value.reduce((sum, s) => sum + ((s as any).totalInflow || 0), 0)
})

const colorMap: Record<string, string> = {
  cash: '#8c8c8c',
  wechat: '#07c160',
  alipay: '#1677ff',
  card: '#f0a020',
  stored: '#d03050',
  combined: '#8b5cf6'
}

function methodColor(method: string) {
  return colorMap[method] || '#999'
}

function methodLabel(method: string) {
  const map: Record<string, string> = {
    cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', stored: '储值卡', combined: '组合'
  }
  return map[method] || method
}

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)

  const dates = [...new Set(dailyFlow.value.map(d => d.payDate))].sort()
  const methods = [...new Set(dailyFlow.value.map(d => d.paymentMethod))]

  const series: echarts.SeriesOption[] = methods.map(m => ({
    name: methodLabel(m),
    type: 'line',
    smooth: true,
    data: dates.map(date => {
      const item = dailyFlow.value.find(d => d.payDate === date && d.paymentMethod === m)
      return item ? item.inflow : 0
    }),
    itemStyle: { color: colorMap[m] || '#999' },
    areaStyle: { color: `${colorMap[m] || '#999'}20` }
  }))

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let result = params[0].axisValue + '<br/>'
        let total = 0
        params.forEach((p: any) => {
          total += p.value
          result += `${p.marker} ${p.seriesName}: ¥${p.value.toFixed(2)}<br/>`
        })
        result += `<b>合计: ¥${total.toFixed(2)}</b>`
        if (params.some((p: any) => p.seriesName === '储值卡')) {
          result += '<br/><span style="color:#d03050">* 储值卡不计入收入</span>'
        }
        return result
      }
    },
    legend: { data: methods.map(m => methodLabel(m)), top: 10 },
    grid: { left: '3%', right: '4%', top: 60, bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: dates.length > 14 ? 45 : 0 } },
    yAxis: { type: 'value', name: '金额(元)' },
    series
  })
}

function initInflowPie() {
  if (!inflowPieRef.value) return
  inflowPie = echarts.init(inflowPieRef.value)

  const data = statCards.value
    .filter(s => s.code !== 'stored' && (s as any).totalInflow > 0)
    .map(s => ({ name: s.name, value: (s as any).totalInflow || 0 }))

  inflowPie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      label: { formatter: '{b}\n¥{c}' },
      data
    }]
  })
}

function initOutflowPie() {
  if (!outflowPieRef.value) return
  outflowPie = echarts.init(outflowPieRef.value)

  const data = statCards.value
    .filter(s => (s as any).totalOutflow > 0)
    .map(s => ({ name: s.name, value: (s as any).totalOutflow || 0 }))

  outflowPie.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      label: { formatter: '{b}\n¥{c}' },
      data: data.length > 0 ? data : [{ name: '无退款', value: 0 }]
    }]
  })
}

async function loadStats() {
  try {
    const methods = await getPaymentMethods()
    const stats = await getPaymentMethodStats()
    statCards.value = methods.map(m => {
      const stat = stats.find(s => s.paymentMethod === m.code)
      return {
        ...m,
        totalInflow: stat?.totalInflow || 0,
        totalOutflow: stat?.totalOutflow || 0
      }
    })
    await nextTick()
    initInflowPie()
    initOutflowPie()
  } catch {
    message.error('加载统计失败')
  }
}

async function loadDailyFlow() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    message.warning('请选择日期范围')
    return
  }
  const fmtLocal = (ts: number) => {
    const d = new Date(ts)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
  const startDate = fmtLocal(dateRange.value[0])
  const endDate = fmtLocal(dateRange.value[1])
  try {
    dailyFlow.value = await getDailyPaymentFlow({ startDate, endDate })
    await nextTick()
    initTrendChart()
  } catch {
    message.error('加载趋势失败')
  }
}

function openRechargeModal(stat: PaymentMethod) {
  rechargeTarget.value = stat
  rechargeAmount.value = 0
  rechargeRemark.value = ''
  showRechargeModal.value = true
}

async function handleRecharge() {
  if (!rechargeTarget.value) return
  if (rechargeAmount.value === 0) {
    message.warning('请输入调整金额')
    return
  }
  const newBalance = (rechargeTarget.value.balance || 0) + rechargeAmount.value
  if (newBalance < 0) {
    message.error('调整后余额不能为负数')
    return
  }
  try {
    await rechargePaymentMethod(rechargeTarget.value.id, rechargeAmount.value, rechargeRemark.value)
    message.success('余额调整成功')
    showRechargeModal.value = false
    loadStats()
  } catch {
    message.error('调整失败')
  }
}

function handleResize() {
  trendChart?.resize()
  inflowPie?.resize()
  outflowPie?.resize()
}

onMounted(async () => {
  const today = new Date()
  const start = new Date(today.getFullYear(), today.getMonth(), 1)
  dateRange.value = [start.getTime(), today.getTime()]

  await loadStats()
  await loadDailyFlow()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  inflowPie?.dispose()
  outflowPie?.dispose()
})
</script>

<style scoped>
.payment-flow {
  padding: 0;
}
</style>
