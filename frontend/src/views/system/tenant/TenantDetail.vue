<template>
  <div class="tenant-detail">
    <n-card :bordered="false">
      <template #header>
        <n-space align="center">
          <n-button quaternary @click="handleBack">
            <template #icon><n-icon :component="ArrowBackOutline" /></template>
          </n-button>
          <span>租户详情</span>
        </n-space>
      </template>

      <n-tabs type="line" animated>
        <n-tab-pane name="basic" tab="基本信息">
          <n-descriptions :column="2" label-placement="left" bordered>
            <n-descriptions-item label="租户编号">{{ tenant.tenantNo }}</n-descriptions-item>
            <n-descriptions-item label="租户名称">{{ tenant.tenantName }}</n-descriptions-item>
            <n-descriptions-item label="联系人">{{ tenant.contactName }}</n-descriptions-item>
            <n-descriptions-item label="联系电话">{{ tenant.contactPhone }}</n-descriptions-item>
            <n-descriptions-item label="联系邮箱">{{ tenant.contactEmail || '-' }}</n-descriptions-item>
            <n-descriptions-item label="地址">{{ tenant.address || '-' }}</n-descriptions-item>
            <n-descriptions-item label="营业执照号">{{ tenant.licenseNo || '-' }}</n-descriptions-item>
            <n-descriptions-item label="套餐类型">
              <n-tag :type="getPlanTypeTag(tenant.planType)">
                {{ getPlanTypeName(tenant.planType) }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="getStatusTag(tenant.status)">
                {{ getStatusName(tenant.status) }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="最大门店数">{{ tenant.maxStores }}</n-descriptions-item>
            <n-descriptions-item label="最大用户数">{{ tenant.maxUsers }}</n-descriptions-item>
            <n-descriptions-item label="到期时间">{{ tenant.expireTime }}</n-descriptions-item>
            <n-descriptions-item label="创建时间" :span="2">{{ tenant.createTime }}</n-descriptions-item>
          </n-descriptions>
        </n-tab-pane>

        <n-tab-pane name="stores" tab="门店列表">
          <n-space vertical :size="16">
            <n-space justify="end">
              <n-button type="primary" @click="handleAddStore">
                <template #icon><n-icon :component="AddOutline" /></template>
                新增门店
              </n-button>
            </n-space>
            <n-data-table
              :columns="storeColumns"
              :data="storeList"
              :row-key="(row: Store) => row.id"
              :pagination="storePagination"
              remote
            />
          </n-space>
        </n-tab-pane>

        <n-tab-pane name="config" tab="租户配置">
          <n-form label-placement="left" label-width="150">
            <n-divider content-placement="left">收银配置</n-divider>
            <n-form-item label="小票页脚文字">
              <n-input v-model:value="config.receiptFooter" placeholder="谢谢惠顾" />
            </n-form-item>
            <n-form-item label="自动锁屏时间（分钟）">
              <n-input-number v-model:value="config.autoLock" :min="0" :max="60" />
            </n-form-item>

            <n-divider content-placement="left">会员配置</n-divider>
            <n-form-item label="默认会员等级">
              <n-select
                v-model:value="config.defaultLevel"
                :options="[
                  { label: '普通会员', value: 1 },
                  { label: '银卡会员', value: 2 },
                  { label: '金卡会员', value: 3 },
                  { label: '钻石会员', value: 4 }
                ]"
              />
            </n-form-item>
            <n-form-item label="积分比例（1元=N积分）">
              <n-input-number v-model:value="config.pointRatio" :min="1" :max="10" />
            </n-form-item>

            <n-divider content-placement="left">库存配置</n-divider>
            <n-form-item label="启用库存预警">
              <n-switch v-model:value="config.stockWarnEnabled" />
            </n-form-item>
            <n-form-item label="默认预警数量">
              <n-input-number v-model:value="config.stockWarnQuantity" :min="1" :max="999" />
            </n-form-item>

            <n-form-item>
              <n-button type="primary" @click="handleSaveConfig">保存配置</n-button>
            </n-form-item>
          </n-form>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard, NButton, NSpace, NIcon, NTag, NDataTable, NTabs, NTabPane,
  NDescriptions, NDescriptionsItem, NForm, NFormItem, NInput, NInputNumber,
  NSelect, NSwitch, NDivider, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps } from 'naive-ui'
import { ArrowBackOutline, AddOutline } from '@vicons/ionicons5'
import type { Tenant, Store } from '../../../types/tenant'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const tenant = ref<Tenant>({
  id: 1,
  tenantNo: 'TENANT001',
  tenantName: '阳光超市',
  contactName: '张三',
  contactPhone: '13800138001',
  contactEmail: 'zhangsan@example.com',
  address: '北京市朝阳区xxx路xxx号',
  licenseNo: '91110105MA01xxxx',
  planType: 2,
  maxStores: 5,
  maxUsers: 20,
  expireTime: '2027-12-31',
  status: 1,
  createTime: '2026-01-15 10:00:00'
})

const storeList = ref<Store[]>([
  {
    id: 1,
    storeNo: 'STORE001',
    storeName: '阳光超市总店',
    tenantId: 1,
    address: '北京市朝阳区xxx路1号',
    contactPhone: '010-12345678',
    storeType: 1,
    businessHours: '08:00-22:00',
    status: 1,
    sort: 1,
    createTime: '2026-01-15 10:00:00'
  },
  {
    id: 2,
    storeNo: 'STORE002',
    storeName: '阳光超市分店',
    tenantId: 1,
    address: '北京市海淀区xxx路2号',
    contactPhone: '010-87654321',
    storeType: 1,
    businessHours: '09:00-21:00',
    status: 1,
    sort: 2,
    createTime: '2026-03-20 14:30:00'
  }
])

const config = reactive({
  receiptFooter: '谢谢惠顾',
  autoLock: 5,
  defaultLevel: 1,
  pointRatio: 1,
  stockWarnEnabled: true,
  stockWarnQuantity: 10
})

const storePagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10
})

function getPlanTypeName(type: number) {
  const map: Record<number, string> = { 1: '基础版', 2: '专业版', 3: '企业版' }
  return map[type] || '-'
}

function getPlanTypeTag(type: number) {
  const map: Record<number, 'info' | 'warning' | 'success'> = { 1: 'info', 2: 'warning', 3: 'success' }
  return map[type] || 'info'
}

function getStatusName(status: number) {
  const map: Record<number, string> = { 0: '禁用', 1: '正常', 2: '已到期' }
  return map[status] || '-'
}

function getStatusTag(status: number) {
  const map: Record<number, 'error' | 'success' | 'warning'> = { 0: 'error', 1: 'success', 2: 'warning' }
  return map[status] || 'info'
}

function getStoreStatusName(status: number) {
  const map: Record<number, string> = { 0: '装修中', 1: '营业中', 2: '暂停营业', 3: '已关闭' }
  return map[status] || '-'
}

function getStoreStatusTag(status: number) {
  const map: Record<number, 'info' | 'success' | 'warning' | 'error'> = {
    0: 'info', 1: 'success', 2: 'warning', 3: 'error'
  }
  return map[status] || 'info'
}

const storeColumns: DataTableColumns<Store> = [
  { title: '门店编号', key: 'storeNo', width: 120 },
  { title: '门店名称', key: 'storeName', width: 150 },
  { title: '地址', key: 'address', width: 200 },
  { title: '联系电话', key: 'contactPhone', width: 130 },
  {
    title: '门店类型',
    key: 'storeType',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: row.storeType === 1 ? 'info' : 'warning' }, {
        default: () => row.storeType === 1 ? '直营店' : '加盟店'
      })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: getStoreStatusTag(row.status) }, {
        default: () => getStoreStatusName(row.status)
      })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: () => handleEditStore(row)
          }, { default: () => '编辑' }),
          h(NButton, {
            size: 'small',
            type: row.status === 1 ? 'warning' : 'success',
            text: true,
            onClick: () => handleToggleStoreStatus(row)
          }, { default: () => row.status === 1 ? '停业' : '开业' })
        ]
      })
    }
  }
]

function handleBack() {
  router.push('/system/tenant/list')
}

function handleAddStore() {
  router.push('/system/store/add')
}

function handleEditStore(row: Store) {
  router.push(`/system/store/edit/${row.id}`)
}

function handleToggleStoreStatus(row: Store) {
  const newStatus = row.status === 1 ? 2 : 1
  row.status = newStatus
  message.success('状态更新成功')
}

function handleSaveConfig() {
  message.success('配置保存成功')
}

onMounted(() => {
  // 根据路由参数加载租户详情
  const tenantId = route.params.id
  if (tenantId) {
    // 模拟API调用加载数据
    console.log('加载租户详情:', tenantId)
  }
})
</script>

<style scoped>
.tenant-detail {
  padding: 0;
}
</style>
