<template>
  <div class="daily-settlement">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <!-- 查询条件 -->
        <div class="search-bar">
          <n-space :size="12" align="center">
            <n-date-picker
              v-model:value="settlementDate"
              type="date"
              clearable
              style="width: 160px"
            />
            <n-button type="primary" @click="handleQuery">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
          </n-space>
          <n-tag v-if="hasChecked" type="success" size="small">已对账</n-tag>
          <n-tag v-else type="warning" size="small">未对账</n-tag>
        </div>

        <!-- 汇总卡片 -->
        <n-card size="small" :bordered="false" style="background: linear-gradient(135deg, #f6ffed 0%, #e6fffb 100%)">
          <n-space justify="space-between" align="center">
            <n-space align="center" :size="16">
              <n-statistic label="实际收入">
                <template #prefix>¥</template>
                {{ totalInflow.toFixed(2) }}
              </n-statistic>
              <n-statistic label="支出/退款">
                <template #prefix>¥</template>
                {{ totalOutflow.toFixed(2) }}
              </n-statistic>
              <n-statistic label="净额">
                <template #prefix>¥</template>
                {{ (totalInflow - totalOutflow).toFixed(2) }}
              </n-statistic>
            </n-space>
            <n-button type="primary" :disabled="tableData.length === 0 || hasChecked" @click="handleSettle">
              确认日结
            </n-button>
          </n-space>
        </n-card>

        <!-- 明细表格 -->
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :row-key="(row: DailyPaymentFlowVO) => row.payDate + '-' + row.paymentMethod"
          :pagination="false"
        />
      </n-space>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { NCard, NDataTable, NButton, NSpace, NIcon, NTag, NDatePicker, NStatistic, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { getDailySettlement } from '../../../api/order'
import type { DailyPaymentFlowVO } from '../../../api/order'

const message = useMessage()
const loading = ref(false)
const settlementDate = ref<number | null>(Date.now())
const tableData = ref<DailyPaymentFlowVO[]>([])
const hasChecked = ref(false)

const methodColor: Record<string, string> = {
  cash: '#8c8c8c',
  wechat: '#07c160',
  alipay: '#1677ff',
  card: '#f0a020',
  stored: '#d03050',
  combined: '#8b5cf6'
}

const totalInflow = computed(() => tableData.value.reduce((sum, d) => sum + (d.inflow || 0), 0))
const totalOutflow = computed(() => tableData.value.reduce((sum, d) => sum + (d.outflow || 0), 0))

const columns: DataTableColumns<DailyPaymentFlowVO> = [
  {
    title: '支付方式',
    key: 'paymentMethodName',
    width: 160,
    render(row) {
      return h(NSpace, { size: 6, align: 'center' }, {
        default: () => [
          h('span', {
            style: { display: 'inline-block', width: '10px', height: '10px', borderRadius: '50%', background: methodColor[row.paymentMethod] || '#999' }
          }),
          h('span', null, { default: () => row.paymentMethodName })
        ]
      })
    }
  },
  {
    title: '收入',
    key: 'inflow',
    width: 120,
    render(row) {
      return h('span', { style: { color: '#18a058', fontWeight: 600 } }, { default: () => `¥${(row.inflow || 0).toFixed(2)}` })
    }
  },
  {
    title: '支出/退款',
    key: 'outflow',
    width: 120,
    render(row) {
      return h('span', { style: { color: '#d03050', fontWeight: 600 } }, { default: () => `¥${(row.outflow || 0).toFixed(2)}` })
    }
  },
  {
    title: '笔数（收入/支出）',
    key: 'counts',
    width: 150,
    render(row) {
      return `${row.inflowCount || 0} / ${row.outflowCount || 0}`
    }
  }
]

function formatDate(ts: number): string {
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadData() {
  if (!settlementDate.value) {
    message.warning('请选择日期')
    return
  }
  loading.value = true
  try {
    const date = formatDate(settlementDate.value)
    const res = await getDailySettlement(date)
    tableData.value = res
    hasChecked.value = false
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  loadData()
}

function handleSettle() {
  message.success('日结完成')
  hasChecked.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.daily-settlement { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
