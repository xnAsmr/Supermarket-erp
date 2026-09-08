<template>
  <div class="system-users">
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
              v-model:value="searchRoleId"
              placeholder="角色"
              clearable
              :options="roleOptions"
              style="width: 130px"
            />
            <n-select
              v-model:value="searchTenantId"
              placeholder="租户"
              clearable
              :options="tenantOptions"
              :loading="loadingTenants"
              style="width: 160px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button v-permission="'sys:user:add'" type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            添加用户
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendUser) => row.id"
          :pagination="pagination"
          remote
        />
      </n-space>
    </n-card>

    <!-- 用户表单弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingUser ? '编辑用户' : '添加用户'"
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
        <n-form-item label="角色" path="roleId">
          <n-select v-model:value="formData.roleId" placeholder="请选择角色" :options="roleOptions" :loading="loadingRoles" clearable />
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

    <!-- 重置密码弹窗 -->
    <n-modal
      v-model:show="showResetPwdModal"
      preset="card"
      title="重置密码"
      style="width: 400px"
    >
      <n-alert type="warning" style="margin-bottom: 16px">
        确定要重置用户 <strong>{{ resettingUser?.name }}</strong> 的密码吗？
      </n-alert>
      <n-form label-placement="left" label-width="80">
        <n-form-item label="新密码">
          <n-input v-model:value="newPassword" type="password" show-password-on="click" placeholder="请输入新密码" />
        </n-form-item>
        <n-form-item label="确认密码">
          <n-input v-model:value="confirmPassword" type="password" show-password-on="click" placeholder="请再次输入密码" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showResetPwdModal = false">取消</n-button>
          <n-button type="primary" :loading="resetting" @click="handleResetPassword">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NModal, NForm, NFormItem, NSwitch, NPopconfirm, NAlert, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getUsers, createUser, updateUser, deleteUser, resetPassword, getRoles } from '../../../api/system'
import type { BackendUser, UserQueryParams, BackendRole } from '../../../api/system'
import { getTenants } from '../../../api/tenant'
import { hasPermission } from '../../../directives/permission'
import type { BackendTenant } from '../../../api/tenant'

const message = useMessage()
const loading = ref(false)
const searchKeyword = ref('')
const searchRoleId = ref<number | null>(null)
const searchTenantId = ref<number | null>(null)

const tableData = ref<BackendUser[]>([])
const total = ref(0)

const roleOptions = ref<{ label: string; value: number }[]>([])
const loadingRoles = ref(false)
const roleMap = ref<Map<number, string>>(new Map())

const tenantOptions = ref<{ label: string; value: number }[]>([])
const loadingTenants = ref(false)

async function loadTenants() {
  loadingTenants.value = true
  try {
    const res = await getTenants({ page: 1, pageSize: 200 })
    tenantOptions.value = res.list.map((t: BackendTenant) => ({ label: t.tenantName, value: t.id }))
  } catch {
    tenantOptions.value = []
  } finally {
    loadingTenants.value = false
  }
}

async function loadRoles() {
  loadingRoles.value = true
  try {
    const res = await getRoles({ page: 1, pageSize: 100 })
    roleOptions.value = res.list.map((r: BackendRole) => ({ label: r.roleName, value: r.id }))
    roleMap.value = new Map(res.list.map((r: BackendRole) => [r.id, r.roleName]))
  } catch {
    roleOptions.value = []
  } finally {
    loadingRoles.value = false
  }
}

function getRoleName(roleId: number | null) {
  if (!roleId) return '-'
  return roleMap.value.get(roleId) || '-'
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
  const map: Record<number, string> = { 1: '租户管理员', 3: '系统管理员' }
  return map[type] || '普通用户'
}

function userTypeTagType(type: number) {
  const map: Record<number, 'error' | 'success' | 'info' | 'warning'> = { 1: 'error', 3: 'warning' }
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
    if (searchRoleId.value) params.userType = searchRoleId.value
    if (searchTenantId.value) params.tenantId = searchTenantId.value

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

// 表单相关
const showFormModal = ref(false)
const editingUser = ref<BackendUser | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  roleId: null as number | null,
  status: 1 as number
})

const formRules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' },
  name: { required: true, message: '请输入姓名', trigger: 'blur' },
  phone: { required: true, message: '请输入手机号', trigger: 'blur' },
  roleId: { required: true, type: 'number', message: '请选择角色', trigger: 'change' }
}

// 重置密码相关
const showResetPwdModal = ref(false)
const resettingUser = ref<BackendUser | null>(null)
const newPassword = ref('')
const confirmPassword = ref('')
const resetting = ref(false)

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
  {
    title: '租户',
    key: 'tenantName',
    width: 130,
    render(row) {
      const name = (row as any).tenantName
      return name || '-'
    }
  },
  { title: '手机', key: 'phone', width: 130 },
  {
    title: '角色',
    key: 'roleId',
    width: 110,
    render(row) {
      const roleName = getRoleName((row as any).roleId)
      return h(NTag, { size: 'small', type: 'info' }, { default: () => roleName })
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
    width: 200,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, {
        default: () => [
          hasPermission('sys:user:edit')
            ? h(NButton, {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleEdit(row)
              }, { default: () => '编辑' })
            : null,
          hasPermission('sys:user:resetpwd')
            ? h(NButton, {
                size: 'small',
                type: 'warning',
                text: true,
                onClick: () => handleResetPwd(row)
              }, { default: () => '重置密码' })
            : null,
          hasPermission('sys:user:delete') && row.username !== 'admin'
            ? h(NPopconfirm, {
                onPositiveClick: () => handleDelete(row)
              }, {
                trigger: () => h(NButton, { size: 'small', type: 'error', text: true }, { default: () => '删除' }),
                default: () => `确定要删除用户 "${row.name}" 吗？`
              })
            : null
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
  searchRoleId.value = null
  searchTenantId.value = null
  pagination.page = 1
  loadData()
}

function resetForm() {
  formData.username = ''
  formData.password = ''
  formData.name = ''
  formData.phone = ''
  formData.roleId = null
  formData.status = 1
}

function handleAdd() {
  editingUser.value = null
  resetForm()
  showFormModal.value = true
}

function handleEdit(row: BackendUser) {
  editingUser.value = row
  formData.username = row.username
  formData.password = ''
  formData.name = row.name
  formData.phone = row.phone || ''
  formData.roleId = (row as any).roleId || null
  formData.status = row.status
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

function handleResetPwd(row: BackendUser) {
  resettingUser.value = row
  newPassword.value = ''
  confirmPassword.value = ''
  showResetPwdModal.value = true
}

async function handleResetPassword() {
  if (!newPassword.value) {
    message.warning('请输入新密码')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    message.warning('两次密码输入不一致')
    return
  }
  if (newPassword.value.length < 6) {
    message.warning('密码长度不能少于6位')
    return
  }

  resetting.value = true
  try {
    await resetPassword(resettingUser.value!.id)
    message.success(`用户 "${resettingUser.value!.name}" 密码已重置`)
    showResetPwdModal.value = false
  } catch (e) {
    message.error('重置密码失败')
  } finally {
    resetting.value = false
  }
}

onMounted(() => {
  loadRoles()
  loadTenants()
  loadData()
})
</script>

<style scoped>
.system-users { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
