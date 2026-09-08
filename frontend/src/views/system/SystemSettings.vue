<template>
  <div class="system-settings">
    <n-card title="系统设置" :bordered="false">
      <n-tabs type="line" animated>
        <n-tab-pane name="basic" tab="基本设置">
          <n-form label-placement="left" label-width="150" style="max-width: 600px">
            <n-divider content-placement="left">系统信息</n-divider>
            <n-form-item label="系统名称">
              <n-input v-model:value="settings.systemName" />
            </n-form-item>
            <n-form-item label="系统版本">
              <n-input v-model:value="settings.version" disabled />
            </n-form-item>
            <n-form-item label="版权信息">
              <n-input v-model:value="settings.copyright" />
            </n-form-item>

            <n-divider content-placement="left">安全设置</n-divider>
            <n-form-item label="Token 过期时间（小时）">
              <n-input-number v-model:value="settings.tokenExpireHours" :min="1" :max="720" />
            </n-form-item>
            <n-form-item label="密码最小长度">
              <n-input-number v-model:value="settings.passwordMinLength" :min="6" :max="20" />
            </n-form-item>
            <n-form-item label="登录失败锁定次数">
              <n-input-number v-model:value="settings.loginFailLockCount" :min="3" :max="10" />
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <n-tab-pane name="mail" tab="邮件设置">
          <n-form label-placement="left" label-width="150" style="max-width: 600px">
            <n-form-item label="SMTP 服务器">
              <n-input v-model:value="settings.smtpHost" placeholder="smtp.example.com" />
            </n-form-item>
            <n-form-item label="SMTP 端口">
              <n-input-number v-model:value="settings.smtpPort" :min="1" :max="65535" />
            </n-form-item>
            <n-form-item label="发件人邮箱">
              <n-input v-model:value="settings.smtpUsername" placeholder="admin@example.com" />
            </n-form-item>
            <n-form-item label="邮箱密码">
              <n-input v-model:value="settings.smtpPassword" type="password" show-password-on="click" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleTestMail">发送测试邮件</n-button>
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <n-tab-pane name="storage" tab="存储设置">
          <n-form label-placement="left" label-width="150" style="max-width: 600px">
            <n-form-item label="存储类型">
              <n-select
                v-model:value="settings.storageType"
                :options="[
                  { label: '本地存储', value: 'local' },
                  { label: '阿里云 OSS', value: 'aliyun' },
                  { label: '腾讯云 COS', value: 'tencent' },
                  { label: 'MinIO', value: 'minio' }
                ]"
              />
            </n-form-item>
            <n-form-item label="存储路径">
              <n-input v-model:value="settings.storagePath" placeholder="/upload" />
            </n-form-item>
            <n-form-item v-if="settings.storageType !== 'local'" label="Access Key">
              <n-input v-model:value="settings.storageAccessKey" />
            </n-form-item>
            <n-form-item v-if="settings.storageType !== 'local'" label="Secret Key">
              <n-input v-model:value="settings.storageSecretKey" type="password" show-password-on="click" />
            </n-form-item>
            <n-form-item v-if="settings.storageType !== 'local'" label="Bucket">
              <n-input v-model:value="settings.storageBucket" />
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <n-tab-pane name="cache" tab="缓存设置">
          <n-form label-placement="left" label-width="150" style="max-width: 600px">
            <n-form-item label="启用缓存">
              <n-switch v-model:value="settings.cacheEnabled" />
            </n-form-item>
            <n-form-item label="缓存类型">
              <n-select
                v-model:value="settings.cacheType"
                :options="[
                  { label: 'Caffeine（本地缓存）', value: 'caffeine' },
                  { label: 'Redis（分布式缓存）', value: 'redis' }
                ]"
              />
            </n-form-item>
            <n-form-item label="缓存过期时间（分钟）">
              <n-input-number v-model:value="settings.cacheExpireMinutes" :min="1" :max="1440" />
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <n-tab-pane name="tenant" tab="租户设置">
          <n-form label-placement="left" label-width="150" style="max-width: 600px">
            <n-form-item label="启用多租户">
              <n-switch v-model:value="settings.multiTenantEnabled" />
            </n-form-item>
            <n-form-item label="租户隔离方式">
              <n-select
                v-model:value="settings.tenantIsolation"
                :options="[
                  { label: '共享数据库 + tenant_id', value: 'shared_db' },
                  { label: '独立数据库', value: 'separate_db' },
                  { label: '独立 Schema', value: 'separate_schema' }
                ]"
              />
            </n-form-item>
            <n-form-item label="默认套餐类型">
              <n-select
                v-model:value="settings.defaultPlanType"
                :options="[
                  { label: '基础版', value: 1 },
                  { label: '专业版', value: 2 },
                  { label: '企业版', value: 3 }
                ]"
              />
            </n-form-item>
            <n-form-item label="试用期天数">
              <n-input-number v-model:value="settings.trialDays" :min="0" :max="90" />
            </n-form-item>
          </n-form>
        </n-tab-pane>

        <n-tab-pane name="payment" tab="支付方式">
          <n-space vertical :size="16">
            <n-space>
              <n-button type="primary" @click="showPaymentModal = true">新增支付方式</n-button>
            </n-space>
            <n-data-table
              :columns="paymentColumns"
              :data="paymentMethods"
              :row-key="(row: any) => row.id"
              :pagination="false"
              bordered
            />
          </n-space>
        </n-tab-pane>
      </n-tabs>

      <n-divider />
      <n-space justify="end">
        <n-button type="primary" @click="handleSave">保存设置</n-button>
      </n-space>
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
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, h, onMounted } from 'vue'
import {
  NCard, NTabs, NTabPane, NForm, NFormItem, NInput, NInputNumber,
  NSelect, NSwitch, NButton, NSpace, NDivider, NDataTable, NModal, NTag,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { getPaymentMethods, createPaymentMethod, updatePaymentMethod, deletePaymentMethod, type PaymentMethod } from '@/api/paymentMethod'

const message = useMessage()

const paymentMethods = ref<PaymentMethod[]>([])
const showPaymentModal = ref(false)
const editingPayment = ref<PaymentMethod | null>(null)
const paymentForm = reactive({
  name: '',
  code: '',
  sort: 0,
  status: 1,
  remark: ''
})

const paymentColumns: DataTableColumns<PaymentMethod> = [
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
  { title: '备注', key: 'remark', width: 150 },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', text: true, onClick: () => handleEditPayment(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'small', type: 'error', text: true, onClick: () => handleDeletePayment(row) }, { default: () => '删除' })
        ]
      })
    }
  }
]

async function loadPaymentMethods() {
  try {
    paymentMethods.value = await getPaymentMethods()
  } catch {}
}

function handleEditPayment(row: PaymentMethod) {
  editingPayment.value = row
  paymentForm.name = row.name
  paymentForm.code = row.code
  paymentForm.sort = row.sort
  paymentForm.status = row.status
  paymentForm.remark = row.remark || ''
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
    editingPayment.value = null
    paymentForm.name = ''
    paymentForm.code = ''
    paymentForm.sort = 0
    paymentForm.status = 1
    paymentForm.remark = ''
    loadPaymentMethods()
  } catch {
    message.error('操作失败')
  }
}

onMounted(() => {
  loadPaymentMethods()
})

const settings = reactive({
  // 基本设置
  systemName: '超市收银出入库管理系统',
  version: 'v1.6.0',
  copyright: '© 2026 Supermarket ERP',
  
  // 安全设置
  tokenExpireHours: 2,
  passwordMinLength: 6,
  loginFailLockCount: 5,
  
  // 邮件设置
  smtpHost: '',
  smtpPort: 465,
  smtpUsername: '',
  smtpPassword: '',
  
  // 存储设置
  storageType: 'local',
  storagePath: '/upload',
  storageAccessKey: '',
  storageSecretKey: '',
  storageBucket: '',
  
  // 缓存设置
  cacheEnabled: true,
  cacheType: 'caffeine',
  cacheExpireMinutes: 30,
  
  // 租户设置
  multiTenantEnabled: true,
  tenantIsolation: 'shared_db',
  defaultPlanType: 1,
  trialDays: 15
})

function handleTestMail() {
  message.info('测试邮件功能开发中')
}

function handleSave() {
  message.success('设置保存成功')
}
</script>

<style scoped>
.system-settings {
  padding: 0;
}
</style>
