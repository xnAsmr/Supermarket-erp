<template>
  <div class="member-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchParams.keyword"
              placeholder="手机号/姓名/编号"
              clearable
              style="width: 220px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-select
              v-model:value="searchParams.levelId"
              placeholder="会员等级"
              clearable
              :options="levelOptions"
              style="width: 150px"
              @update:value="handleSearch"
            />
            <n-select
              v-model:value="searchParams.status"
              placeholder="状态"
              clearable
              :options="statusOptions"
              style="width: 120px"
              @update:value="handleSearch"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>

          <n-button v-permission="'ums:member:add'" type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增会员
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :row-key="(row: BackendMember) => row.id"
          remote
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>

    <!-- 新增会员弹窗 -->
    <n-modal
      v-model:show="showForm"
      title="新增会员"
      preset="card"
      style="width: 520px"
    >
      <n-form ref="formRef" :model="memberForm" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="memberForm.name" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="memberForm.phone" placeholder="请输入手机号" maxlength="11" />
        </n-form-item>
        <n-form-item label="性别" path="gender">
          <n-radio-group v-model:value="memberForm.gender">
            <n-radio :value="1">男</n-radio>
            <n-radio :value="2">女</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="生日" path="birthday">
          <n-date-picker v-model:value="memberForm.birthdayTs" type="date" clearable style="width: 100%" />
        </n-form-item>
        <n-form-item label="会员等级" path="levelId">
          <n-select v-model:value="memberForm.levelId" :options="levelOptions" placeholder="请选择会员等级" />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="memberForm.remark" type="textarea" placeholder="备注信息" />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showForm = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 充值弹窗 -->
    <n-modal v-model:show="showRechargeModal" preset="card" title="会员充值" style="width: 450px">
      <n-form label-placement="left" label-width="100">
        <n-form-item label="会员">
          <n-text strong>{{ rechargeTarget?.name }}</n-text>
          <n-text depth="3" style="margin-left: 8px">{{ rechargeTarget?.phone }}</n-text>
        </n-form-item>
        <n-form-item label="当前余额">
          <n-text type="success" strong>¥{{ (rechargeTarget?.balance || 0).toFixed(2) }}</n-text>
        </n-form-item>
        <n-form-item label="充值金额">
          <n-input-number v-model:value="rechargeAmount" :min="0" :precision="2" style="width: 100%" placeholder="请输入充值金额" />
        </n-form-item>
        <n-form-item label="赠送金额">
          <n-input-number v-model:value="rechargeGiftAmount" :min="0" :precision="2" style="width: 100%" placeholder="如：充100送10" />
        </n-form-item>
        <n-form-item label="支付方式">
          <n-select v-model:value="rechargePayMethod" :options="payMethodOptions" placeholder="请选择支付方式" />
        </n-form-item>
        <n-form-item label="充值后余额">
          <n-text type="success" strong>
            ¥{{ ((rechargeTarget?.balance || 0) + rechargeAmount + rechargeGiftAmount).toFixed(2) }}
          </n-text>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="rechargeRemark" placeholder="如：现金充值/微信充值" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showRechargeModal = false">取消</n-button>
          <n-button type="primary" @click="handleRecharge">确定充值</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NModal, NForm, NFormItem, NRadioGroup, NRadio, NDatePicker,
  NText, NInputNumber, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getMembers, createMember, deleteMember, rechargeMember } from '../../../api/member'
import type { BackendMember } from '../../../api/member'
import { getPaymentMethods, type PaymentMethod } from '../../../api/paymentMethod'
import { formatMoney } from '../../../utils/format'
import { hasPermission } from '../../../directives/permission'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const tableData = ref<BackendMember[]>([])
const showForm = ref(false)
const submitting = ref(false)
const formRef = ref<FormInst | null>(null)

const showRechargeModal = ref(false)
const rechargeTarget = ref<BackendMember | null>(null)
const rechargeAmount = ref(0)
const rechargeGiftAmount = ref(0)
const rechargePayMethod = ref<string | null>(null)
const rechargeRemark = ref('')
const dbPayMethods = ref<PaymentMethod[]>([])

const payMethodOptions = computed(() => {
  const iconMap: Record<string, string> = { cash: '💵', wechat: '💚', alipay: '💙', card: '💳', stored: '🎫' }
  return dbPayMethods.value
    .filter(m => m.status === 1 && m.code !== 'stored')
    .map(m => ({ label: `${iconMap[m.code] || '💰'} ${m.name}`, value: m.code }))
})

const searchParams = reactive({
  keyword: '',
  levelId: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const levelOptions = [
  { label: '普通会员', value: 1 },
  { label: '银卡会员', value: 2 },
  { label: '金卡会员', value: 3 },
  { label: '钻石会员', value: 4 }
]
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '已注销', value: 0 }
]

const memberForm = reactive({
  name: '',
  phone: '',
  gender: 1 as number | null,
  birthdayTs: null as number | null,
  levelId: 1 as number | null,
  remark: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  levelId: [{ required: true, type: 'number', message: '请选择会员等级', trigger: 'change' }]
}

const columns: DataTableColumns<BackendMember> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '会员编号', key: 'memberNo', width: 110 },
  { title: '姓名', key: 'name', width: 100 },
  { title: '手机', key: 'phone', width: 130 },
  {
    title: '等级',
    key: 'levelName',
    width: 100,
    render(row) {
      const typeMap: Record<number, 'success' | 'info' | 'warning' | 'error'> = {
        1: 'info',
        2: 'success',
        3: 'warning',
        4: 'error'
      }
      return h(
        NTag,
        { size: 'small', type: typeMap[row.levelId] || 'default' },
        { default: () => row.levelName }
      )
    }
  },
  {
    title: '积分',
    key: 'points',
    width: 80,
    render(row) { return row.points.toLocaleString() }
  },
  {
    title: '余额',
    key: 'balance',
    width: 100,
    render(row) { return formatMoney(row.balance) }
  },
  {
    title: '累计消费',
    key: 'totalConsume',
    width: 110,
    render(row) { return formatMoney(row.totalConsume) }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(
        NTag,
        { size: 'small', type: row.status === 1 ? 'success' : 'info' },
        { default: () => row.status === 1 ? '正常' : '已注销' }
      )
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          hasPermission('ums:member:recharge')
            ? h(NButton, {
                size: 'small',
                type: 'warning',
                text: true,
                onClick: () => openRechargeModal(row)
              }, { default: () => '充值' })
            : null,
          h(NButton, {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: () => handleDetail(row)
          }, { default: () => '详情' }),
          hasPermission('ums:member:delete')
            ? h(NButton, {
                size: 'small',
                type: 'error',
                text: true,
                onClick: () => handleDelete(row.id)
              }, { default: () => '删除' })
            : null
        ]
      })
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await getMembers(searchParams)
    tableData.value = res.list
    pagination.itemCount = res.total
    pagination.page = searchParams.page
  } finally {
    loading.value = false
  }
}

async function loadPayMethods() {
  try {
    dbPayMethods.value = await getPaymentMethods()
  } catch {}
}

function handleSearch() {
  searchParams.page = 1
  loadData()
}

function handleReset() {
  searchParams.keyword = ''
  searchParams.levelId = undefined
  searchParams.status = undefined
  searchParams.page = 1
  loadData()
}

function handlePageChange(page: number) {
  searchParams.page = page
  loadData()
}

function handlePageSizeChange(pageSize: number) {
  searchParams.pageSize = pageSize
  searchParams.page = 1
  loadData()
}

function handleAdd() {
  memberForm.name = ''
  memberForm.phone = ''
  memberForm.gender = 1
  memberForm.birthdayTs = null
  memberForm.levelId = 1
  memberForm.remark = ''
  showForm.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const data: any = {
      name: memberForm.name,
      phone: memberForm.phone,
      gender: memberForm.gender
    }
    if (memberForm.birthdayTs) {
      data.birthday = new Date(memberForm.birthdayTs).toISOString().slice(0, 10)
    }
    if (memberForm.levelId) {
      data.levelId = memberForm.levelId
    }
    await createMember(data)
    message.success('创建成功')
    showForm.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await deleteMember(id)
  message.success('删除成功')
  loadData()
}

function handleDetail(row: BackendMember) {
  router.push(`/admin/member/detail/${row.id}`)
}

function openRechargeModal(row: BackendMember) {
  rechargeTarget.value = row
  rechargeAmount.value = 0
  rechargeGiftAmount.value = 0
  rechargePayMethod.value = null
  rechargeRemark.value = ''
  showRechargeModal.value = true
}

async function handleRecharge() {
  if (!rechargeTarget.value) return
  if (rechargeAmount.value <= 0) {
    message.warning('请输入充值金额')
    return
  }
  if (!rechargePayMethod.value) {
    message.warning('请选择支付方式')
    return
  }
  try {
    await rechargeMember(rechargeTarget.value.id, rechargeAmount.value, rechargeGiftAmount.value, rechargePayMethod.value, rechargeRemark.value)
    message.success('充值成功')
    showRechargeModal.value = false
    loadData()
  } catch (e: any) {
    message.error(e?.message || '充值失败')
  }
}

onMounted(() => {
  loadData()
  loadPayMethods()
})
</script>

<style scoped>
.member-list {
  padding: 0;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
