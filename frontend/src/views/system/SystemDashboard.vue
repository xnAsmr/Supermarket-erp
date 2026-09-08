<template>
  <div class="system-dashboard">
    <n-space vertical :size="24">
      <!-- 统计卡片 -->
      <n-grid :cols="4" :x-gap="16" :y-gap="16">
        <n-gi>
          <n-card :bordered="false">
            <n-statistic label="租户总数" :value="stats.totalTenants">
              <template #prefix>
                <n-icon :component="BusinessOutline" color="#18a058" />
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card :bordered="false">
            <n-statistic label="活跃租户" :value="stats.activeTenants">
              <template #prefix>
                <n-icon :component="CheckmarkCircleOutline" color="#2080f0" />
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card :bordered="false">
            <n-statistic label="门店总数" :value="stats.totalStores">
              <template #prefix>
                <n-icon :component="StorefrontOutline" color="#f0a020" />
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card :bordered="false">
            <n-statistic label="系统用户" :value="stats.totalUsers">
              <template #prefix>
                <n-icon :component="PeopleOutline" color="#d03050" />
              </template>
            </n-statistic>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 图表区域 -->
      <n-grid :cols="2" :x-gap="16" :y-gap="16">
        <n-gi>
          <n-card title="租户套餐类型分布" :bordered="false">
            <div ref="planChartRef" style="height: 320px"></div>
          </n-card>
        </n-gi>
        <n-gi>
          <n-card title="租户状态分布" :bordered="false">
            <div ref="statusChartRef" style="height: 320px"></div>
          </n-card>
        </n-gi>
        <n-gi :span="2">
          <n-card title="近6个月租户开通趋势" :bordered="false">
            <div ref="trendChartRef" style="height: 320px"></div>
          </n-card>
        </n-gi>
      </n-grid>

      <!-- 即将到期租户 -->
      <n-card title="即将到期租户（30天内）" :bordered="false">
        <n-data-table
          :columns="expiringColumns"
          :data="stats.expiringTenants"
          :row-key="(row: ExpiringTenant) => row.id"
          :pagination="false"
          :loading="loading"
        />
      </n-card>

      <!-- 系统信息 -->
      <n-card title="系统信息" :bordered="false">
        <n-descriptions :column="2" label-placement="left" bordered>
          <n-descriptions-item label="系统版本">v1.6.0</n-descriptions-item>
          <n-descriptions-item label="运行环境">生产环境</n-descriptions-item>
          <n-descriptions-item label="数据库状态">
            <n-tag type="success">正常</n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="服务器时间">{{ currentTime }}</n-descriptions-item>
        </n-descriptions>
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted, onUnmounted, nextTick } from 'vue'
import {
  NCard, NGrid, NGi, NStatistic, NIcon, NSpace,
  NDataTable, NDescriptions, NDescriptionsItem, NTag
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import {
  BusinessOutline,
  CheckmarkCircleOutline,
  StorefrontOutline,
  PeopleOutline
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getTenantStats } from '../../api/tenant'
import type { TenantStats } from '../../api/tenant'

interface ExpiringTenant {
  id: number
  tenantName: string
  expireTime: string
  planType: number
}

const stats = reactive<TenantStats>({
  totalTenants: 0,
  activeTenants: 0,
  totalStores: 0,
  totalUsers: 0,
  planTypeDistribution: [],
  statusDistribution: [],
  monthlyTrend: [],
  expiringTenants: []
})
const loading = ref(false)

const currentTime = ref('')
let timer: ReturnType<typeof setInterval>

const planChartRef = ref<HTMLElement>()
const statusChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()
let planChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const planTypeNames: Record<number, string> = { 1: '基础版', 2: '专业版', 3: '企业版' }
const planTypeColors: Record<number, string> = { 1: '#909399', 2: '#f0a020', 3: '#18a058' }

const statusNames: Record<number, string> = { 0: '禁用', 1: '正常', 2: '到期' }
const statusColors: Record<number, string> = { 0: '#d03050', 1: '#18a058', 2: '#f0a020' }

const expiringColumns: DataTableColumns<ExpiringTenant> = [
  { title: '租户名称', key: 'tenantName', width: 200 },
  {
    title: '套餐类型',
    key: 'planType',
    width: 120,
    render(row) {
      const color = planTypeColors[row.planType] || '#909399'
      return h(NTag, { size: 'small', color: { color, textColor: '#fff' } }, {
        default: () => planTypeNames[row.planType] || '-'
      })
    }
  },
  { title: '到期时间', key: 'expireTime', width: 200, render(row) { return row.expireTime ? row.expireTime.slice(0, 10) : '-' } }
]

function initPlanChart() {
  if (!planChartRef.value) return
  planChart = echarts.init(planChartRef.value)
  const data = stats.planTypeDistribution.map(d => ({
    name: planTypeNames[d.planType] || `套餐${d.planType}`,
    value: d.count,
    itemStyle: { color: planTypeColors[d.planType] || '#909399' }
  }))
  planChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}: {c}' },
      data
    }]
  })
}

function initStatusChart() {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  const data = stats.statusDistribution.map(d => ({
    name: statusNames[d.status] || `状态${d.status}`,
    value: d.count,
    itemStyle: { color: statusColors[d.status] || '#909399' }
  }))
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      roseType: 'radius',
      radius: ['30%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}: {c}' },
      data
    }]
  })
}

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const months = stats.monthlyTrend.map(d => d.month)
  const counts = stats.monthlyTrend.map(d => d.count)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: months, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '新开通租户',
      type: 'line',
      smooth: true,
      data: counts,
      areaStyle: { color: 'rgba(24, 160, 88, 0.15)' },
      lineStyle: { color: '#18a058', width: 2 },
      itemStyle: { color: '#18a058' }
    }]
  })
}

function handleResize() {
  planChart?.resize()
  statusChart?.resize()
  trendChart?.resize()
}

async function loadStats() {
  loading.value = true
  try {
    const res = await getTenantStats()
    Object.assign(stats, res)
    await nextTick()
    initPlanChart()
    initStatusChart()
    initTrendChart()
  } catch {
    // keep zeros on error
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
  const updateTime = () => {
    const now = new Date()
    currentTime.value = now.toLocaleString('zh-CN')
  }
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  planChart?.dispose()
  statusChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.system-dashboard {
  padding: 0;
}
</style>
