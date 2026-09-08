<template>
  <div class="user-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="用户名/姓名"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-select
              v-model:value="searchRole"
              placeholder="角色"
              clearable
              :options="roleOptions"
              style="width: 130px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增用户
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendUser) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>

    <!-- 用户表单弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingUser ? '编辑用户' : '新增用户'"
      style="width: 500px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名" :disabled="!!editingUser" />
        </n-form-item>
        <n-form-item v-if="!editingUser" label="密码" path="password">
          <n-input v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入真实姓名" />
        </n-form-item>
        <n-form-item label="手机" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号码" />
        </n-form-item>
        <n-form-item label="租户" path="tenantId">
          <n-select v-model:value="formData.tenantId" placeholder="请选择租户" :options="tenantOptions" clearable />
        </n-form-item>
        <n-form-item v-if="formData.tenantId" label="角色" path="roleId">
          <n-select v-model:value="formData.roleId" placeholder="请选择角色" :options="roleOptions" :loading="loadingRoles" clearable />
        </n-form-item>
        <n-form-item v-if="showStore" label="门店" path="storeId">
          <n-select v-model:value="formData.storeId" placeholder="请选择门店" :options="storeOptions" :loading="loadingStores" clearable />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showFormModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted, computed, watch } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NModal, NForm, NFormItem, NSwitch, NPopconfirm, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getUsers, createUser, updateUser, deleteUser, resetPassword, getRoles } from '../../../api/system'
import type { BackendUser, UserQueryParams, BackendRole } from '../../../api/system'
import { getTenants } from '../../../api/tenant'
import { getStores } from '../../../api/tenant'
import type { BackendTenant, BackendStore } from '../../../api/tenant'

const message = useMessage()
const loading = ref(false)
const searchKeyword = ref('')
const searchRole = ref<number | null>(null)

const tableData = ref<BackendUser[]>([])
const total = ref(0)

const roleOptions = ref<{ label: string; value: number }[]>([])
const tenantOptions = ref<{ label: string; value: number }[]>([])
const storeOptions = ref<{ label: string; value: number }[]>([])
const loadingStores = ref(false)
const loadingRoles = ref(false)

const showTenant = computed(() => formData.userType !== null)
const showStore = computed(() => formData.userType === 2 || formData.userType === 4 || formData.userType === 5)

async function loadTenants() {
  try {
    const res = await getTenants({ page: 1, pageSize: 100 })
    tenantOptions.value = res.list.map((t: BackendTenant) => ({ label: t.tenantName, value: t.id }))
  } catch {
    tenantOptions.value = []
  }
}

async function loadRoles(tenantId: number | null) {
  if (!tenantId) {
    roleOptions.value = []
    return
  }
  loadingRoles.value = true
  try {
    const res = await getRoles({ tenantId, page: 1, pageSize: 100 })
    roleOptions.value = res.list.map((r: BackendRole) => ({ label: r.roleName, value: r.id }))
  } catch {
    roleOptions.value = []
  } finally {
    loadingRoles.value = false
  }
}

async function loadStores(tenantId: number | null) {
  if (!tenantId) {
    storeOptions.value = []
    return
  }
  loadingStores.value = true
  try {
    const res = await getStores({ tenantId, page: 1, pageSize: 100 })
    storeOptions.value = res.list.map((s: BackendStore) => ({ label: s.storeName, value: s.id }))
  } catch {
    storeOptions.value = []
  } finally {
    loadingStores.value = false
  }
}

const pagination = reactive<PaginationProps>({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page: number) => {
    pagination.page = page
    loadData()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    loadData()
  }
})

function userTypeLabel(type: number) {
  const map: Record<number, string> = { 1: '租户管理员', 2: '收银员', 3: '系统管理员', 4: '店长', 5: '仓管员' }
  return map[type] || '未知'
}

function userTypeTagType(type: number) {
  const map: Record<number, 'error' | 'success' | 'info' | 'warning'> = { 1: 'error', 2: 'success', 3: 'warning', 4: 'info', 5: 'info' }
  return map[type] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const params: UserQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (searchRole.value) params.userType = searchRole.value

    const res = await getUsers(params)
    tableData.value = res.list
    total.value = res.total
    pagination.itemCount = res.total
  } catch (e) {
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const showFormModal = ref(false)
const editingUser = ref<BackendUser | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  userType: null as number | null,
  tenantId: null as number | null,
  storeId: null as number | null,
  roleId: null as number | null,
  status: 1 as number
})

watch(() => formData.userType, () => {
  formData.tenantId = null
  formData.storeId = null
  formData.roleId = null
  storeOptions.value = []
  roleOptions.value = []
})

watch(() => formData.tenantId, (val) => {
  formData.storeId = null
  formData.roleId = null
  if (val) {
    loadRoles(val)
    if (showStore.value) {
      loadStores(val)
    }
  } else {
    storeOptions.value = []
    roleOptions.value = []
  }
})

const formRules = computed<FormRules>(() => ({
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: editingUser.value ? {} : { required: true, message: '请输入密码', trigger: 'blur' },
  name: { required: true, message: '请输入姓名', trigger: 'blur' },
  phone: { required: true, message: '请输入手机号', trigger: 'blur' },
  tenantId: { required: true, type: 'number', message: '请选择租户', trigger: 'change' },
  roleId: formData.tenantId ? { required: true, type: 'number', message: '请选择角色', trigger: 'change' } : {},
  ...(showStore.value ? { storeId: { required: true, type: 'number', message: '请选择门店', trigger: 'change' } } : {})
}))

const columns: DataTableColumns<BackendUser> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  { title: '用户名', key: 'username', width: 120 },
  { title: '姓名', key: 'name', width: 100 },
  { title: '手机', key: 'phone', width: 130 },
  {
    title: '角色',
    key: 'roleName',
    width: 110,
    render(row) {
      return row.roleName || '-'
    }
  },
  {
    title: '所属租户',
    key: 'tenantId',
    width: 120,
    render(row) {
      if (!row.tenantId) return '-'
      const t = tenantOptions.value.find(o => o.value === row.tenantId)
      return t ? t.label : row.tenantId
    }
  },
  {
    title: '所属门店',
    key: 'storeId',
    width: 120,
    render(row) {
      if (!row.storeId) return '-'
      return row.storeId
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '正常' : '禁用' })
    }
  },
  { title: '最后登录', key: 'lastLoginTime', width: 160, render(row) { return row.lastLoginTime || '-' } },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            text: true,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          row.username !== 'admin'
            ? h(NPopconfirm, {
                onPositiveClick: () => handleResetPassword(row)
              }, {
                trigger: () => h(NButton, { size: 'small', type: 'warning', text: true }, { default: () => '重置密码' }),
                default: () => `确定要将 "${row.name}" 的密码重置为 123456 吗？`
              })
            : null,
        ]
      })
    }
  }
]

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  searchRole.value = null
  pagination.page = 1
  loadData()
}

function resetForm() {
  formData.username = ''
  formData.password = ''
  formData.name = ''
  formData.phone = ''
  formData.userType = null
  formData.tenantId = null
  formData.storeId = null
  formData.roleId = null
  formData.status = 1
  storeOptions.value = []
  roleOptions.value = []
}

async function handleAdd() {
  editingUser.value = null
  resetForm()
  await loadTenants()
  showFormModal.value = true
}

async function handleEdit(row: BackendUser) {
  editingUser.value = row
  formData.username = row.username
  formData.password = ''
  formData.name = row.name
  formData.phone = row.phone || ''
  formData.userType = row.userType
  formData.tenantId = (row as any).tenantId || null
  formData.storeId = (row as any).storeId || null
  formData.roleId = (row as any).roleId || null
  formData.status = row.status
  await loadTenants()
  if (formData.tenantId) {
    await loadRoles(formData.tenantId)
    if (showStore.value) {
      await loadStores(formData.tenantId)
    }
  }
  showFormModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    if (editingUser.value) {
      await updateUser(editingUser.value.id, {
        name: formData.name,
        phone: formData.phone,
        userType: formData.userType,
        tenantId: formData.tenantId,
        storeId: formData.storeId,
        roleId: formData.roleId,
        status: formData.status
      })
      message.success('用户更新成功')
    } else {
      await createUser({
        username: formData.username,
        password: formData.password,
        name: formData.name,
        phone: formData.phone,
        userType: formData.userType,
        tenantId: formData.tenantId,
        storeId: formData.storeId,
        roleId: formData.roleId,
        status: formData.status
      })
      message.success('用户添加成功')
    }

    showFormModal.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

function handleDelete(row: BackendUser) {
  deleteUser(row.id).then(() => {
    message.success('用户已删除')
    loadData()
  }).catch(() => {
    message.error('删除失败')
  })
}

async function handleResetPassword(row: BackendUser) {
  try {
    await resetPassword(row.id)
    message.success(`用户 "${row.name}" 密码已重置为 123456`)
  } catch {
    message.error('重置密码失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
