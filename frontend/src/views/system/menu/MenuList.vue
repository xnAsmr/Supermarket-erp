<template>
  <div class="menu-list">
    <n-card :bordered="false">
      <n-space vertical :size="16">
        <div class="search-bar">
          <n-space :size="12">
            <n-input
              v-model:value="searchKeyword"
              placeholder="菜单名称"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>
            <n-button type="primary" @click="handleSearch">
              <template #icon><n-icon :component="SearchOutline" /></template>
              查询
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd(null)">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增菜单
          </n-button>
        </div>

        <n-data-table
          :columns="columns"
          :data="filteredData"
          :row-key="(row: BackendMenu) => row.id"
          :loading="loading"
          :default-expand-all="true"
          :pagination="false"
        />
      </n-space>
    </n-card>

    <!-- 菜单表单弹窗 -->
    <n-modal
      v-model:show="showFormModal"
      preset="card"
      :title="editingMenu ? '编辑菜单' : '新增菜单'"
      style="width: 560px"
    >
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="90">
        <n-form-item label="上级菜单" path="parentId">
          <n-tree-select
            v-model:value="formData.parentId"
            :options="menuTreeOptions"
            placeholder="请选择上级菜单（不选则为顶级）"
            clearable
            default-expand-all
          />
        </n-form-item>
        <n-form-item label="菜单类型" path="menuType">
          <n-radio-group v-model:value="formData.menuType">
            <n-radio :value="0">目录</n-radio>
            <n-radio :value="1">菜单</n-radio>
            <n-radio :value="2">按钮</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="菜单名称" path="menuName">
          <n-input v-model:value="formData.menuName" placeholder="请输入菜单名称" />
        </n-form-item>
        <n-form-item v-if="formData.menuType !== 2" label="路由路径" path="path">
          <n-input v-model:value="formData.path" placeholder="请输入路由路径" />
        </n-form-item>
        <n-form-item v-if="formData.menuType === 1" label="组件路径" path="component">
          <n-input v-model:value="formData.component" placeholder="请输入组件路径" />
        </n-form-item>
        <n-form-item v-if="formData.menuType === 2" label="权限标识" path="permission">
          <n-input v-model:value="formData.permission" placeholder="如：sys:user:add" />
        </n-form-item>
        <n-form-item v-if="formData.menuType !== 2" label="图标" path="icon">
          <n-input v-model:value="formData.icon" placeholder="图标名称" />
        </n-form-item>
        <n-form-item label="排序" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="显示状态" path="visible" v-if="formData.menuType !== 2">
          <n-switch v-model:value="formData.visible" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
          </n-switch>
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
import { ref, reactive, h, computed, onMounted } from 'vue'
import {
  NCard, NDataTable, NButton, NSpace, NInput, NIcon, NTag,
  NModal, NForm, NFormItem, NSwitch, NPopconfirm, NTreeSelect, NRadioGroup, NRadio,
  NInputNumber, useMessage
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { SearchOutline, AddOutline } from '@vicons/ionicons5'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '../../../api/system'
import type { BackendMenu } from '../../../api/system'

const message = useMessage()
const loading = ref(false)
const searchKeyword = ref('')

const tableData = ref<BackendMenu[]>([])

const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  function filterTree(nodes: BackendMenu[]): BackendMenu[] {
    return nodes.filter(n => {
      const nameMatch = n.menuName.toLowerCase().includes(kw)
      const childMatch = n.children && filterTree(n.children).length > 0
      return nameMatch || childMatch
    }).map(n => ({
      ...n,
      children: n.children ? filterTree(n.children) : undefined
    }))
  }
  return filterTree(tableData.value)
})

const menuTreeOptions = computed(() => {
  function toOptions(nodes: BackendMenu[]): any[] {
    return nodes.map(n => ({
      label: n.menuName,
      value: n.id,
      children: n.children && n.children.length > 0 ? toOptions(n.children) : undefined
    }))
  }
  return [{ label: '顶级菜单', value: 0 }, ...toOptions(tableData.value)]
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    tableData.value = res
  } catch (e) {
    message.error('加载菜单列表失败')
  } finally {
    loading.value = false
  }
}

function getMenuTypeLabel(type: number) {
  const map: Record<number, string> = { 0: '目录', 1: '菜单', 2: '按钮' }
  return map[type] || String(type)
}

function getMenuTypeTag(type: number) {
  const map: Record<number, 'info' | 'success' | 'warning'> = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[type] || 'info'
}

const columns: DataTableColumns<BackendMenu> = [
  { title: '菜单名称', key: 'menuName', width: 200 },
  {
    title: '类型',
    key: 'menuType',
    width: 80,
    render(row) {
      return h(NTag, { size: 'small', type: getMenuTypeTag(row.menuType) }, {
        default: () => getMenuTypeLabel(row.menuType)
      })
    }
  },
  { title: '图标', key: 'icon', width: 100, render(row) { return row.icon || '-' } },
  { title: '路由路径', key: 'path', width: 150, render(row) { return row.path || '-' } },
  { title: '组件路径', key: 'component', width: 180, render(row) { return row.component || '-' } },
  { title: '权限标识', key: 'permission', width: 150, render(row) { return row.permission || '-' } },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '可见',
    key: 'visible',
    width: 70,
    render(row) {
      if (row.menuType === 2) return '-'
      return h(NTag, { size: 'small', type: row.visible === 1 ? 'success' : 'default' }, {
        default: () => row.visible === 1 ? '显示' : '隐藏'
      })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 70,
    render(row) {
      return h(NTag, { size: 'small', type: row.status === 1 ? 'success' : 'default' }, {
        default: () => row.status === 1 ? '正常' : '禁用'
      })
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
          h(NButton, {
            size: 'small',
            type: 'info',
            text: true,
            onClick: () => handleEdit(row)
          }, { default: () => '编辑' }),
          row.menuType !== 2
            ? h(NButton, {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleAdd(row)
              }, { default: () => '新增子菜单' })
            : null,
          h(NPopconfirm, {
            onPositiveClick: () => handleDelete(row)
          }, {
            trigger: () => h(NButton, { size: 'small', type: 'error', text: true }, { default: () => '删除' }),
            default: () => `确定要删除菜单 "${row.menuName}" 吗？`
          })
        ]
      })
    }
  }
]

function handleSearch() {
  loadData()
}

function handleReset() {
  searchKeyword.value = ''
  loadData()
}

const showFormModal = ref(false)
const editingMenu = ref<BackendMenu | null>(null)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  parentId: 0 as number,
  menuType: 1 as number,
  menuName: '',
  path: '',
  component: '',
  permission: '',
  icon: '',
  sort: 0,
  visible: 1 as number,
  status: 1 as number
})

const formRules: FormRules = {
  menuName: { required: true, message: '请输入菜单名称', trigger: 'blur' },
  menuType: { required: true, message: '请选择菜单类型', trigger: 'change' }
}

function resetForm(parentId?: number | null) {
  formData.parentId = parentId ?? 0
  formData.menuType = 1
  formData.menuName = ''
  formData.path = ''
  formData.component = ''
  formData.permission = ''
  formData.icon = ''
  formData.sort = 0
  formData.visible = 1
  formData.status = 1
}

function handleAdd(parent: BackendMenu | null) {
  editingMenu.value = null
  resetForm(parent?.id ?? 0)
  showFormModal.value = true
}

function handleEdit(row: BackendMenu) {
  editingMenu.value = row
  formData.parentId = row.parentId ?? 0
  formData.menuType = row.menuType
  formData.menuName = row.menuName
  formData.path = row.path || ''
  formData.component = row.component || ''
  formData.permission = row.permission || ''
  formData.icon = row.icon || ''
  formData.sort = row.sort
  formData.visible = row.visible
  formData.status = row.status
  showFormModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    const payload: any = {
      parentId: formData.parentId || 0,
      menuType: formData.menuType,
      menuName: formData.menuName,
      path: formData.path || null,
      component: formData.component || null,
      permission: formData.permission || null,
      icon: formData.icon || null,
      sort: formData.sort,
      visible: formData.visible,
      status: formData.status
    }

    if (editingMenu.value) {
      await updateMenu(editingMenu.value.id, payload)
      message.success('菜单更新成功')
    } else {
      await createMenu(payload)
      message.success('菜单创建成功')
    }

    showFormModal.value = false
    loadData()
  } catch (e: any) {
    if (e?.message) message.error(e.message)
  } finally {
    saving.value = false
  }
}

function handleDelete(row: BackendMenu) {
  deleteMenu(row.id).then(() => {
    message.success('菜单已删除')
    loadData()
  }).catch(() => {
    message.error('删除失败')
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.menu-list { padding: 0; }
.search-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
