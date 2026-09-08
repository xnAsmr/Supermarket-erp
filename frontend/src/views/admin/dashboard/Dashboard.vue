<template>
  <div class="dashboard">
    <n-space vertical :size="20">
      <div class="stats-grid">
        <n-card class="stat-card stat-sales" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">今日销售额</div>
              <div class="stat-value">{{ formatMoney(dashboardData.todaySalesAmount) }}</div>
              <div class="stat-compare">
                <n-text depth="3" style="font-size: 12px">本月 {{ formatMoney(dashboardData.monthSalesAmount) }}</n-text>
              </div>
            </div>
            <div class="stat-icon">
              <n-icon size="40" color="#fff"><TrendingUpOutline /></n-icon>
            </div>
          </div>
        </n-card>

        <n-card class="stat-card stat-orders" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">今日订单数</div>
              <div class="stat-value">{{ dashboardData.todayOrderCount }}</div>
              <div class="stat-compare">
                <n-text depth="3" style="font-size: 12px">本月 {{ dashboardData.monthOrderCount }} 单</n-text>
              </div>
            </div>
            <div class="stat-icon">
              <n-icon size="40" color="#fff"><DocumentTextOutline /></n-icon>
            </div>
          </div>
        </n-card>

        <n-card class="stat-card stat-members" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">会员数</div>
              <div class="stat-value">{{ dashboardData.todayMemberCount }}</div>
              <div class="stat-compare">
                <n-text depth="3" style="font-size: 12px">本月新增</n-text>
              </div>
            </div>
            <div class="stat-icon">
              <n-icon size="40" color="#fff"><PeopleOutline /></n-icon>
            </div>
          </div>
        </n-card>

        <n-card class="stat-card stat-alert" :bordered="false">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">库存预警</div>
              <div class="stat-value">{{ dashboardData.lowStockCount }}</div>
              <div class="stat-compare">
                <n-text depth="3" style="font-size: 12px">需及时补货</n-text>
              </div>
            </div>
            <div class="stat-icon">
              <n-icon size="40" color="#fff"><WarningOutline /></n-icon>
            </div>
          </div>
        </n-card>
      </div>

      <n-grid :cols="2" :x-gap="20">
        <n-grid-item>
          <n-card title="本月利润" :bordered="false">
            <div style="text-align: center; padding: 40px 0">
              <n-text strong style="font-size: 32px; color: #18a058">
                {{ formatMoney(dashboardData.monthProfitAmount) }}
              </n-text>
              <div style="margin-top: 8px; color: #999; font-size: 13px">本月毛利润</div>
            </div>
          </n-card>
        </n-grid-item>

        <n-grid-item>
          <n-card title="快捷操作" :bordered="false">
            <n-space vertical :size="12">
              <n-button block type="primary" @click="$router.push('/pos/cashier')">
                POS 收银
              </n-button>
              <n-button block @click="$router.push('/admin/report/sales')">
                查看销售报表
              </n-button>
              <n-button block @click="$router.push('/admin/stock')">
                库存管理
              </n-button>
            </n-space>
          </n-card>
        </n-grid-item>
      </n-grid>

      <n-card title="最近订单" :bordered="false">
        <n-data-table
          :columns="orderColumns"
          :data="recentOrders"
          :pagination="false"
          :bordered="false"
          size="small"
        />
      </n-card>
    </n-space>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import {
  NCard, NSpace, NGrid, NGridItem, NDataTable, NText, NIcon, NTag, NButton
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import {
  TrendingUpOutline,
  DocumentTextOutline,
  PeopleOutline,
  WarningOutline
} from '@vicons/ionicons5'
import { getDashboard } from '@/api/report'
import { getOrders } from '@/api/order'
import { formatMoney, formatDateTime } from '@/utils/format'
import type { BackendOrderVO } from '@/api/order'

const dashboardData = ref({
  todaySalesAmount: 0,
  todayOrderCount: 0,
  todayMemberCount: 0,
  monthSalesAmount: 0,
  monthOrderCount: 0,
  monthProfitAmount: 0,
  lowStockCount: 0
})

const recentOrders = ref<BackendOrderVO[]>([])

const orderColumns: DataTableColumns<BackendOrderVO> = [
  { title: '订单号', key: 'orderNo', width: 220 },
  {
    title: '金额',
    key: 'payAmount',
    width: 100,
    render(row) { return formatMoney(row.payAmount) }
  },
  {
    title: '支付方式',
    key: 'payMethod',
    width: 100,
    render(row) {
      const map: Record<string, { label: string; type: 'success' | 'info' | 'warning' | 'error' }> = {
        cash: { label: '现金', type: 'success' },
        wechat: { label: '微信', type: 'info' },
        alipay: { label: '支付宝', type: 'info' },
        card: { label: '银行卡', type: 'warning' },
        stored: { label: '储值卡', type: 'error' },
        combined: { label: '组合支付', type: 'info' }
      }
      const item = map[row.payMethod] || { label: row.payMethod || '未知', type: 'info' }
      return h(NTag, { size: 'small', type: item.type }, { default: () => item.label })
    }
  },
  {
    title: '状态',
    key: 'orderStatus',
    width: 80,
    render(row) {
      const map: Record<number, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
        1: { label: '已完成', type: 'success' },
        0: { label: '待处理', type: 'warning' },
        2: { label: '已取消', type: 'error' },
        3: { label: '已退款', type: 'info' }
      }
      const item = map[row.orderStatus] || { label: String(row.orderStatus), type: 'info' }
      return h(NTag, { size: 'small', type: item.type }, { default: () => item.label })
    }
  },
  { title: '收银员', key: 'cashierName', width: 100 },
  {
    title: '时间',
    key: 'createTime',
    width: 160,
    render(row) { return formatDateTime(row.createTime) }
  }
]

onMounted(async () => {
  const [dashData, orderRes] = await Promise.all([
    getDashboard(),
    getOrders({ page: 1, pageSize: 5 })
  ])
  dashboardData.value = dashData
  recentOrders.value = orderRes.list
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.stat-card :deep(.n-card__content) {
  padding: 20px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-compare {
  color: #999;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-sales .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-orders .stat-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-members .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-alert .stat-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }

.chart-placeholder {
  min-height: 240px;
}

.sales-chart {
  display: flex;
  flex-direction: column;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  padding: 0 10px;
}

.chart-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 80px;
  height: 100%;
  justify-content: flex-end;
}

.chart-bar {
  width: 40px;
  min-height: 8px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 4px 4px 0 0;
  transition: height 0.5s ease;
}

.chart-bar-label {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
  white-space: nowrap;
}

.chart-bar-value {
  font-size: 11px;
  color: #666;
  margin-bottom: 4px;
  font-weight: 500;
}

.top-products {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.top-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-rank {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #d9d9d9;
  flex-shrink: 0;
}

.rank-1 { background: #ff4d4f; }
.rank-2 { background: #ff7a45; }
.rank-3 { background: #ffa940; }

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 13px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-sales {
  text-align: right;
  flex-shrink: 0;
}

.sales-amount {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.sales-qty {
  font-size: 12px;
  color: #999;
}
</style>
