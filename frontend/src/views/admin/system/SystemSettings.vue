<template>
  <div class="system-settings">
    <n-card :bordered="false">
      <n-tabs v-model:value="activeTab" type="line">
        <n-tab-pane name="basic" tab="基本设置">
          <n-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-placement="left"
            label-width="120"
            style="max-width: 600px; margin-top: 16px"
          >
            <n-form-item label="租户编号">
              <n-input :value="basicForm.tenantNo" disabled />
            </n-form-item>
            <n-form-item label="租户名称" path="tenantName">
              <n-input v-model:value="basicForm.tenantName" placeholder="请输入租户名称" />
            </n-form-item>
            <n-form-item label="联系人" path="contactName">
              <n-input v-model:value="basicForm.contactName" placeholder="请输入联系人" />
            </n-form-item>
            <n-form-item label="联系电话" path="contactPhone">
              <n-input v-model:value="basicForm.contactPhone" placeholder="请输入联系电话" />
            </n-form-item>
            <n-form-item label="联系邮箱" path="contactEmail">
              <n-input v-model:value="basicForm.contactEmail" placeholder="请输入联系邮箱（选填）" />
            </n-form-item>
            <n-form-item label="地址" path="address">
              <n-input v-model:value="basicForm.address" placeholder="请输入地址（选填）" />
            </n-form-item>
            <n-form-item label="备注" path="remark">
              <n-input v-model:value="basicForm.remark" type="textarea" placeholder="请输入备注（选填）" />
            </n-form-item>
            <n-form-item label="套餐信息">
              <n-text depth="3">{{ planInfoText }}</n-text>
            </n-form-item>
            <n-form-item>
              <n-button type="primary" :loading="saving" @click="handleSaveBasic">
                保存设置
              </n-button>
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <n-tab-pane name="payment" tab="支付方式">
          <n-space vertical :size="16" style="margin-top: 16px">
            <n-space>
              <n-button type="primary" @click="openPaymentModal()">新增支付方式</n-button>
            </n-space>
            <n-data-table
              :columns="payColumns"
              :data="payMethods"
              :bordered="false"
              :pagination="false"
              size="small"
            />
          </n-space>
        </n-tab-pane>

        <n-tab-pane name="logs" tab="操作日志">
          <n-data-table
            :columns="logColumns"
            :data="logs"
            :bordered="false"
            :pagination="{ pageSize: 10 }"
            size="small"
            style="margin-top: 16px"
          />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <n-modal v-model:show="showPaymentModal" preset="card" :title="editingPayment ? '编辑支付方式' : '新增支付方式'" style="width: 450px">
      <n-form :model="paymentForm" label-placement="left" label-width="80">
        <n-form-item label="名称">
          <n-input v-model:value="paymentForm.name" placeholder="如：微信支付" />
        </n-form-item>
        <n-form-item label="编码">
          <n-input v-model:value="paymentForm.code" placeholder="如：wechat" :disabled="!!editingPayment" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="paymentForm.sort" :min="0" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="paymentForm.status" :checked-value="1" :unchecked-value="0" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="paymentForm.remark" type="textarea" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPaymentModal = false">取消</n-button>
          <n-button type="primary" @click="handleSavePayment">确定</n-button>
        </n-space>
      </template>
    </n-modal>

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
import { ref, reactive, computed, onMounted, h } from 'vue'
import {
  NCard, NTabs, NTabPane, NForm, NFormItem, NInput, NInputNumber,
  NSwitch, NButton, NDataTable, NTag, NText, NSpace, NModal, useMessage
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { getPaymentMethods, createPaymentMethod, updatePaymentMethod, deletePaymentMethod, rechargePaymentMethod } from '@/api/paymentMethod'
import type { PaymentMethod } from '@/api/paymentMethod'
import { getCurrentTenant, updateCurrentTenant } from '@/api/tenant'
import type { BackendTenant } from '@/api/tenant'

const message = useMessage()
const activeTab = ref('basic')
const saving = ref(false)

const basicFormRef = ref<FormInst | null>(null)
const tenant = ref<BackendTenant | null>(null)

const basicForm = reactive({
  tenantNo: '-',
  tenantName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  remark: ''
})

const basicRules: FormRules = {
  tenantName: { required: true, message: '请输入租户名称', trigger: 'blur' }
}

const planTypeNames: Record<number, string> = { 1: '基础版', 2: '专业版', 3: '企业版' }

function formatDate(dt: string | null): string {
  return dt ? dt.slice(0, 10) : '-'
}

const planInfoText = computed(() => {
  const t = tenant.value
  if (!t) return '-'
  const planName = planTypeNames[t.planType ?? 0] || '自定义'
  const storeUsage = `${t.storeCount ?? 0}/${t.maxStores ?? '-'}`
  return `${planName} ｜ 到期时间 ${formatDate(t.expireTime)} ｜ 门店数 ${storeUsage} ｜ 用户上限 ${t.maxUsers ?? '-'}`
})

async function loadTenantInfo() {
  try {
    const data = await getCurrentTenant()
    tenant.value = data
    basicForm.tenantNo = data.tenantNo
    basicForm.tenantName = data.tenantName
    basicForm.contactName = data.contactName || ''
    basicForm.contactPhone = data.contactPhone || ''
    basicForm.contactEmail = data.contactEmail || ''
    basicForm.address = data.address || ''
    basicForm.remark = data.remark || ''
  } catch (e: any) {
    message.error(e?.message || '加载租户信息失败')
  }
}

const payMethods = ref<PaymentMethod[]>([])
const showPaymentModal = ref(false)
const editingPayment = ref<PaymentMethod | null>(null)
const paymentForm = reactive({
  name: '',
  code: '',
  sort: 0,
  status: 1,
  remark: ''
})

const showRechargeModal = ref(false)
const rechargeTarget = ref<PaymentMethod | null>(null)
const rechargeAmount = ref(0)
const rechargeRemark = ref('')

const payColumns: DataTableColumns<PaymentMethod> = [
  { title: '名称', key: 'name', width: 120 },
  { title: '编码', key: 'code', width: 120 },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'error' }, { default: () => row.status === 1 ? '启用' : '禁用' })
    }
  },
  {
    title: '余额',
    key: 'balance',
    width: 120,
    render(row) {
      return h(NText, { strong: true, type: 'success' }, { default: () => `¥${(row.balance || 0).toFixed(2)}` })
    }
  },
  { title: '备注', key: 'remark', width: 150 },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, { size: 'small', type: 'warning', text: true, onClick: () => openRechargeModal(row) }, { default: () => '充值' }),
          h(NButton, { size: 'small', type: 'primary', text: true, onClick: () => openPaymentModal(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', text: true, onClick: () => handleDeletePayment(row) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadPaymentMethods() {
  try {
    payMethods.value = await getPaymentMethods()
  } catch {
    message.error('加载支付方式失败')
  }
}

function openPaymentModal(row?: PaymentMethod) {
  if (row) {
    editingPayment.value = row
    paymentForm.name = row.name
    paymentForm.code = row.code
    paymentForm.sort = row.sort
    paymentForm.status = row.status
    paymentForm.remark = row.remark || ''
  } else {
    editingPayment.value = null
    paymentForm.name = ''
    paymentForm.code = ''
    paymentForm.sort = 0
    paymentForm.status = 1
    paymentForm.remark = ''
  }
  showPaymentModal.value = true
}

async function handleDeletePayment(row: PaymentMethod) {
  try {
    await deletePaymentMethod(row.id)
    message.success('删除成功')
    loadPaymentMethods()
  } catch {
    message.error('删除失败')
  }
}

async function handleSavePayment() {
  if (!paymentForm.name || !paymentForm.code) {
    message.warning('请填写名称和编码')
    return
  }
  try {
    if (editingPayment.value) {
      await updatePaymentMethod(editingPayment.value.id, { ...paymentForm })
      message.success('修改成功')
    } else {
      await createPaymentMethod({ ...paymentForm })
      message.success('新增成功')
    }
    showPaymentModal.value = false
    loadPaymentMethods()
  } catch {
    message.error('操作失败')
  }
}

function openRechargeModal(row: PaymentMethod) {
  rechargeTarget.value = row
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
    loadPaymentMethods()
  } catch {
    message.error('调整失败')
  }
}

interface OperationLog {
  id: string
  username: string
  module: string
  action: string
  detail: string
  ip: string
  createdAt: string
}

const logs = ref<OperationLog[]>([
  { id: '1', username: 'admin', module: '系统', action: '登录', detail: '管理员登录系统', ip: '192.168.1.100', createdAt: '2026-08-31 09:00:00' },
  { id: '2', username: 'admin', module: '商品', action: '新增', detail: '新增商品"可口可乐330ml"', ip: '192.168.1.100', createdAt: '2026-08-31 09:15:00' },
  { id: '3', username: 'liting', module: '收银', action: '结算', detail: '订单号 ORD20260831001，金额 ¥256.80', ip: '192.168.1.101', createdAt: '2026-08-31 09:30:00' }
])

const logColumns: DataTableColumns<OperationLog> = [
  { title: '用户', key: 'username', width: 100 },
  { title: '模块', key: 'module', width: 80 },
  { title: '操作', key: 'action', width: 80 },
  { title: '详情', key: 'detail', width: 300, ellipsis: { tooltip: true } },
  { title: 'IP', key: 'ip', width: 130 },
  { title: '时间', key: 'createdAt', width: 160 }
]

async function handleSaveBasic() {
  try {
    await basicFormRef.value?.validate()
    saving.value = true
    await updateCurrentTenant({
      tenantName: basicForm.tenantName,
      contactName: basicForm.contactName,
      contactPhone: basicForm.contactPhone,
      contactEmail: basicForm.contactEmail,
      address: basicForm.address,
      remark: basicForm.remark
    })
    message.success('保存成功')
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadTenantInfo()
  loadPaymentMethods()
})
</script>

<style scoped>
.system-settings {
  padding: 0;
}
</style>
