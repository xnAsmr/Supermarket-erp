<template>
  <div class="member-detail">
    <n-page-header title="会员详情" @back="handleBack">
      <template #extra>
        <n-space>
          <n-button @click="handleEdit">
            <template #icon><n-icon :component="CreateOutline" /></template>
            编辑
          </n-button>
        </n-space>
      </template>
    </n-page-header>

    <n-spin :show="loading">
      <div v-if="member" class="detail-content">
        <!-- 基本信息卡片 -->
        <n-card title="基本信息" :bordered="false">
          <div class="info-grid">
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">会员编号</n-text>
              <n-text strong>{{ member.memberNo }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">姓名</n-text>
              <n-text strong>{{ member.name }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">手机号</n-text>
              <n-text strong>{{ member.phone }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">性别</n-text>
              <n-text>{{ member.gender != null ? genderMap[member.gender as keyof typeof genderMap] : '-' }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">生日</n-text>
              <n-text>{{ member.birthday || '未设置' }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">状态</n-text>
              <n-tag :type="member.status === 1 ? 'success' : 'info'" size="small">
                {{ member.status === 1 ? '正常' : '已注销' }}
              </n-tag>
            </div>
          </div>
        </n-card>

        <!-- 会员权益卡片 -->
        <n-card title="会员权益" :bordered="false" style="margin-top: 16px">
          <div class="info-grid">
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">会员等级</n-text>
              <n-tag :type="memberTagType" size="small">{{ member.levelName }}</n-tag>
            </div>

            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">当前积分</n-text>
              <n-text strong style="font-size: 20px; color: #f0a020">{{ member.points.toLocaleString() }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">储值余额</n-text>
              <n-text strong style="font-size: 20px; color: #18a058">¥{{ member.balance.toFixed(2) }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">累计消费</n-text>
              <n-text strong style="font-size: 18px; color: #d03050">¥{{ member.totalConsume.toFixed(2) }}</n-text>
            </div>
            <div class="info-item">
              <n-text depth="3" style="font-size: 12px">累计积分</n-text>
              <n-text strong>{{ member.totalPoints.toLocaleString() }}</n-text>
            </div>
          </div>
        </n-card>

        <!-- 储值卡记录 -->
        <n-card title="储值卡记录" :bordered="false" style="margin-top: 16px">
          <n-tabs v-model:value="activeTab" type="line" @update:value="handleTabChange">
            <!-- 充值记录 -->
            <n-tab name="recharge">
              <n-space align="center">
                <span>充值记录</span>
                <n-badge :value="rechargeTotal" :max="999" />
              </n-space>
            </n-tab>
            <!-- 消费记录 -->
            <n-tab name="consume">
              <n-space align="center">
                <span>消费记录</span>
                <n-badge :value="consumeTotal" :max="999" />
              </n-space>
            </n-tab>
          </n-tabs>

          <!-- 充值记录表格 -->
          <div v-if="activeTab === 'recharge'" style="margin-top: 16px">
            <n-data-table
              :columns="rechargeColumns"
              :data="rechargeLogs"
              :loading="rechargeLoading"
              :pagination="rechargePagination"
              remote
              size="small"
              @update:page="handleRechargePageChange"
              @update:page-size="handleRechargePageSizeChange"
            />
          </div>

          <!-- 消费记录表格 -->
          <div v-if="activeTab === 'consume'" style="margin-top: 16px">
            <n-data-table
              :columns="consumeColumns"
              :data="consumeLogs"
              :loading="consumeLoading"
              :pagination="consumePagination"
              remote
              size="small"
              @update:page="handleConsumePageChange"
              @update:page-size="handleConsumePageSizeChange"
            />
          </div>
        </n-card>
      </div>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NPageHeader, NCard, NSpace, NText, NTag, NIcon, NSpin,
  NTabs, NTab, NDataTable, NBadge
} from 'naive-ui'
import { CreateOutline } from '@vicons/ionicons5'
import { getMember } from '../../../api/member'
import { getMemberRechargeLog, getMemberConsumeLog } from '../../../api/member'
import type { BackendMember, MemberRechargeLog, MemberConsumeLog } from '../../../api/member'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import type { Ref } from 'vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const member = ref<BackendMember | null>(null)
const activeTab = ref('recharge')

// 充值记录
const rechargeLogs = ref<MemberRechargeLog[]>([])
const rechargeLoading = ref(false)
const rechargeTotal = ref(0)
const rechargePagination = ref<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  prefix: ({ itemCount }: { itemCount: number }) => `共 ${itemCount} 条`
})

// 消费记录
const consumeLogs = ref<MemberConsumeLog[]>([])
const consumeLoading = ref(false)
const consumeTotal = ref(0)
const consumePagination = ref<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  prefix: ({ itemCount }: { itemCount: number }) => `共 ${itemCount} 条`
})

const genderMap: Record<number, string> = {
  1: '男',
  2: '女'
}

const memberTagType = computed(() => {
  if (!member.value) return 'info'
  const map: Record<number, 'info' | 'success' | 'warning' | 'error'> = {
    1: 'info',
    2: 'success',
    3: 'warning',
    4: 'error'
  }
  return map[member.value.levelId] || 'info'
})

// 充值记录列
const rechargeColumns: DataTableColumns<MemberRechargeLog> = [
  { title: '时间', key: 'createTime', width: 170 },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render(row) {
      return h(NTag, { type: 'success', size: 'small' }, { default: () => '充值' })
    }
  },
  { title: '充值金额', key: 'amount', width: 100 },
  { title: '赠送金额', key: 'giftAmount', width: 100 },
  {
    title: '支付方式',
    key: 'payMethod',
    width: 100,
    render(row) {
      const map: Record<string, string> = { cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', stored: '储值卡' }
      return h(NText, {}, { default: () => map[row.payMethod] || row.payMethod })
    }
  },
  { title: '操作前余额', key: 'beforeBalance', width: 110 },
  { title: '操作后余额', key: 'afterBalance', width: 110 },
  { title: '操作员', key: 'operatorName', width: 100 },
  { title: '备注', key: 'remark', width: 150 }
]

// 消费记录列
const consumeColumns: DataTableColumns<MemberConsumeLog> = [
  { title: '时间', key: 'orderTime', width: 170 },
  {
    title: '类型',
    key: 'type',
    width: 80,
    render() {
      return h(NTag, { type: 'error', size: 'small' }, { default: () => '消费' })
    }
  },
  { title: '订单号', key: 'orderNo', width: 180 },
  { title: '订单金额', key: 'amount', width: 100 },
  { title: '实付金额', key: 'payAmount', width: 100 },
  {
    title: '支付方式',
    key: 'payMethodName',
    width: 100
  },
  {
    title: '订单状态',
    key: 'orderStatusName',
    width: 90,
    render(row) {
      const typeMap: Record<number, 'success' | 'warning' | 'error' | 'info'> = {
        1: 'success', 2: 'success', 3: 'error', 4: 'info', 0: 'warning'
      }
      return h(NTag, { type: typeMap[row.orderStatus] || 'info', size: 'small' }, { default: () => row.orderStatusName })
    }
  },
  { title: '门店', key: 'storeName', width: 120 }
]

async function loadMember() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getMember(id)
    member.value = res
  } finally {
    loading.value = false
  }
}

async function loadRechargeLogs() {
  if (!route.params.id) return
  rechargeLoading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getMemberRechargeLog(id, rechargePagination.value.page || 1, rechargePagination.value.pageSize || 10)
    rechargeLogs.value = res.list
    rechargeTotal.value = res.total
    rechargePagination.value.itemCount = res.total
  } finally {
    rechargeLoading.value = false
  }
}

async function loadConsumeLogs() {
  if (!route.params.id) return
  consumeLoading.value = true
  try {
    const id = Number(route.params.id)
    const res = await getMemberConsumeLog(id, consumePagination.value.page || 1, consumePagination.value.pageSize || 10)
    consumeLogs.value = res.list
    consumeTotal.value = res.total
    consumePagination.value.itemCount = res.total
  } finally {
    consumeLoading.value = false
  }
}

function handleTabChange(tab: string) {
  if (tab === 'recharge') loadRechargeLogs()
  else if (tab === 'consume') loadConsumeLogs()
}

function handleRechargePageChange(page: number) {
  rechargePagination.value.page = page
  loadRechargeLogs()
}

function handleRechargePageSizeChange(pageSize: number) {
  rechargePagination.value.pageSize = pageSize
  rechargePagination.value.page = 1
  loadRechargeLogs()
}

function handleConsumePageChange(page: number) {
  consumePagination.value.page = page
  loadConsumeLogs()
}

function handleConsumePageSizeChange(pageSize: number) {
  consumePagination.value.pageSize = pageSize
  consumePagination.value.page = 1
  loadConsumeLogs()
}

function handleBack() {
  router.push('/admin/member/list')
}

function handleEdit() {
  router.push(`/admin/member/edit/${route.params.id}`)
}

onMounted(() => {
  loadMember()
  loadRechargeLogs()
})
</script>

<style scoped>
.member-detail {
  padding: 0;
}

.detail-content {
  margin-top: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
</style>
