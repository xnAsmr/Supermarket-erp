<template>
  <div class="role-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-select
              v-model:value="searchTenantId"
              placeholder="选择租户"
              clearable
              :options="tenantOptions"
              style="width: 200px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增角色
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="tableData"
          :row-key="(row: BackendRole) => row.id"
          :pagination="pagination"
          :loading="loading"
          remote
        />
      </n-space>
    </n-card>

    <!-- 角色表单弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingRole ? '编辑角色' : '新增角色'"
      style="width: 680px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="80">
        <n-form-item label="租户" path="tenantId">
          <n-select v-model:value="formData.tenantId" placeholder="请选择租户" :options="tenantOptions" :disabled="!!editingRole" />
        </n-form-item>
        <n-form-item label="角色名称" path="roleName">
          <n-input v-model:value="formData.roleName" placeholder="请输入角色名称" />
        </n-form-item>
        <n-form-item label="角色编码" path="roleCode">
          <n-input v-model:value="formData.roleCode" placeholder="请输入角色编码" :disabled="!!editingRole" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="formData.description" type="textarea" placeholder="请输入描述" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="菜单权限" path="menuIds">
          <n-card size="small" :bordered="true" style="width: 100%">
            <n-space vertical :size="8">
              <n-space :size="8">
                <n-button size="small" @click="handleCheckAll">全选</n-button>
                <n-button size="small" @click="handleUncheckAll">取消全选</n-button>
                <n-button size="small" @click="handleExpandAll">展开全部</n-button>
                <n-button size="small" @click="handleCollapseAll">折叠全部</n-button>
              </n-space>
              <n-tree
                ref="treeRef"
                :data="menuTreeData"
                :checked-keys="formData.menuIds"
                :default-expand-all="defaultExpandAll"
                checkable
                selectable
                :default-selected-keys="[]"
                @update:checked-keys="handleMenuCheck"
              />
            </n-space>
          </n-card>
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
import { ref, reactive, h, onMounted, nextTick } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NSelect, NIcon, NTag,
  NModal, NForm, NFormItem, NSwitch, NPopconfirm, NTree, useMessage
} from 'naive-ui'
import type { DataTableColumns, PaginationProps, FormInst, FormRules, TreeInst } from 'naive-ui'
import type { TreeOption } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getRoles, createRole, updateRole, deleteRole, getMenuTree, getRole } from '../../../api/system'
import type { BackendRole, BackendMenu } from '../../../api/system'
import { getTenants } from '../../../api/tenant'
import type { BackendTenant } from '../../../api/tenant'

const message = useMessage()
const loading = ref(false)
const searchTenantId = ref<number | null>(null)

const tableData = ref<BackendRole[]>([])
const menuTreeData = ref<TreeOption[]>([])
const treeRef = ref<TreeInst | null>(null)
const defaultExpandAll = ref(true)

const tenantOptions = ref<{ label: string; value: number }[]>([])

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

async function loadTenants() {
  try {
    const res = await getTenants({ page: 1, pageSize: 100 })
    tenantOptions.value = res.list.map((t: BackendTenant) => ({ label: t.tenantName, value: t.id }))
  } catch {
    tenantOptions.value = []
  }
}

async function loadMenuTree() {
  try {
    const menus = await getMenuTree()
    menuTreeData.value = convertToTreeOptions(menus)
  } catch {
    menuTreeData.value = []
  }
}

function convertToTreeOptions(menus: BackendMenu[]): TreeOption[] {
  return menus.map(menu => ({
    key: menu.id,
    label: getMenuTypeIcon(menu.menuType) + ' ' + menu.menuName,
    children: menu.children && menu.children.length > 0 ? convertToTreeOptions(menu.children) : undefined
  }))
}

function getMenuTypeIcon(type: number) {
  const map: Record<number, string> = { 0: '📁', 1: '📄', 2: '🔘' }
  return map[type] || '📄'
}

async function loadData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchTenantId.value) params.tenantId = searchTenantId.value

    const res = await getRoles(params)
    tableData.value = res.list
    pagination.itemCount = res.total
  } catch (e) {
    message.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

const columns: DataTableColumns<BackendRole> = [
  {
    title: '序号',
    key: 'index',
    width: 60,
    render(_row, index) {
      return (pagination.page - 1) * pagination.pageSize + index + 1
    }
  },
  {
    title: '所属租户',
    key: 'tenantId',
    width: 120,
    render(row) {
      const t = tenantOptions.value.find(o => o.value === row.tenantId)
      return t ? t.label : row.tenantId
    }
  },
  { title: '角色名称', key: 'roleName', width: 120 },
  { title: '角色编码', key: 'roleCode', width: 120 },
  { title: '描述', key: 'description', width: 180, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, {
        default: () => row.status === 1 ? '正常' : '禁用'
      })
    }
  },
  { title: '创建时间', key: 'createTime', width: 160 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
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
          h(NPopconfirm, {
            onPositiveClick: () => handleDelete(row)
          }, {
            trigger: () => h(NButton, { size: 'small', type: 'error', text: true }, { default: () => '删除' }),
            default: () => `确定要删除角色 "${row.roleName}" 吗？`
          })
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
  searchTenantId.value = null
  pagination.page = 1
  loadData()
}

const showFormModal = ref(false)
const editingRole = ref<BackendRole | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  tenantId: null as number | null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1 as number,
  menuIds: [] as number[]
})

const formRules: FormRules = {
  tenantId: { required: true, type: 'number', message: '请选择租户', trigger: 'change' },
  roleName: { required: true, message: '请输入角色名称', trigger: 'blur' },
  roleCode: { required: true, message: '请输入角色编码', trigger: 'blur' }
}

function resetForm() {
  formData.tenantId = null
  formData.roleName = ''
  formData.roleCode = ''
  formData.description = ''
  formData.status = 1
  formData.menuIds = []
}

function handleAdd() {
  editingRole.value = null
  resetForm()
  showFormModal.value = true
}

async function handleEdit(row: BackendRole) {
  editingRole.value = row
  formData.tenantId = row.tenantId
  formData.roleName = row.roleName
  formData.roleCode = row.roleCode
  formData.description = row.description || ''
  formData.status = row.status

  // Load role's menu IDs
  try {
    const roleDetail = await getRole(row.id)
    formData.menuIds = roleDetail.menuIds || []
  } catch {
    formData.menuIds = []
  }

  showFormModal.value = true
}

function handleMenuCheck(keys: number[]) {
  formData.menuIds = keys
}

function handleCheckAll() {
  const allKeys = getAllKeys(menuTreeData.value)
  formData.menuIds = allKeys
}

function handleUncheckAll() {
  formData.menuIds = []
}

function handleExpandAll() {
  defaultExpandAll.value = true
}

function handleCollapseAll() {
  defaultExpandAll.value = false
}

function getAllKeys(options: TreeOption[]): number[] {
  const keys: number[] = []
  function traverse(items: TreeOption[]) {
    for (const item of items) {
      keys.push(item.key as number)
      if (item.children) {
        traverse(item.children)
      }
    }
  }
  traverse(options)
  return keys
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    const payload: any = {
      roleName: formData.roleName,
      roleCode: formData.roleCode,
      description: formData.description,
      status: formData.status,
      menuIds: formData.menuIds
    }

    if (editingRole.value) {
      await updateRole(editingRole.value.id, payload)
      message.success('角色更新成功')
    } else {
      await createRole({
        tenantId: formData.tenantId,
        ...payload
      })
      message.success('角色创建成功')
    }

    showFormModal.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

function handleDelete(row: BackendRole) {
  deleteRole(row.id).then(() => {
    message.success('角色已删除')
    loadData()
  }).catch(() => {
    message.error('删除失败')
  })
}

onMounted(async () => {
  await Promise.all([loadTenants(), loadMenuTree()])
  loadData()
})
</script>

<style scoped>
.role-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
