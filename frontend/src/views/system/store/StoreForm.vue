<template>
  <div class="store-form-page">
    <n-card :bordered="false">
      <template #header>
        <n-space align="center">
          <n-button quaternary @click="handleBack">
            <template #icon><n-icon :component="ArrowBackOutline" /></template>
          </n-button>
          <span>{{ isEdit ? '编辑门店' : '新增门店' }}</span>
        </n-space>
      </template>

      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100"
        style="max-width: 500px"
      >
        <n-form-item label="所属租户" path="tenantId">
          <n-select
            v-model:value="formData.tenantId"
            placeholder="请选择租户"
            :options="tenantOptions"
            :disabled="!!isEdit"
          />
        </n-form-item>

        <n-form-item label="门店名称" path="storeName">
          <n-input v-model:value="formData.storeName" placeholder="请输入门店名称" />
        </n-form-item>

        <n-form-item label="门店类型" path="storeType">
          <n-radio-group v-model:value="formData.storeType">
            <n-space>
              <n-radio :value="1">直营店</n-radio>
              <n-radio :value="2">加盟店</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="联系电话" path="contactPhone">
          <n-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
        </n-form-item>

        <n-form-item label="地址" path="address">
          <n-input v-model:value="formData.address" placeholder="请输入门店地址" />
        </n-form-item>

        <n-form-item label="营业时间" path="businessHours">
          <n-input v-model:value="formData.businessHours" placeholder="如 08:00-22:00" />
        </n-form-item>

        <n-form-item label="排序号" path="sort">
          <n-input-number v-model:value="formData.sort" :min="0" :max="999" />
        </n-form-item>

        <n-form-item label="备注" path="remark">
          <n-input
            v-model:value="formData.remark"
            type="textarea"
            placeholder="请输入备注（选填）"
            :rows="3"
          />
        </n-form-item>

        <n-form-item>
          <n-space>
            <n-button type="primary" :loading="saving" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '立即创建' }}
            </n-button>
            <n-button @click="handleBack">取消</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard, NForm, NFormItem, NInput, NInputNumber, NSelect,
  NButton, NSpace, NIcon, NRadioGroup, NRadio, useMessage
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { getStore, createStore, updateStore, getTenants, type BackendTenant } from '../../../api/tenant'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const tenantOptions = ref<{ label: string; value: number }[]>([])

const formData = reactive({
  tenantId: null as number | null,
  storeName: '',
  storeType: 1,
  contactPhone: '',
  address: '',
  businessHours: '',
  sort: 0,
  remark: ''
})

const formRules: FormRules = {
  tenantId: { required: true, type: 'number', message: '请选择所属租户', trigger: 'change' },
  storeName: { required: true, message: '请输入门店名称', trigger: 'blur' },
  storeType: { required: true, type: 'number', message: '请选择门店类型', trigger: 'change' }
}

function handleBack() {
  router.push('/system/store/list')
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    saving.value = true

    const submitData = {
      storeName: formData.storeName,
      storeType: Number(formData.storeType),
      contactPhone: formData.contactPhone,
      address: formData.address,
      businessHours: formData.businessHours,
      sort: formData.sort,
      remark: formData.remark
    }

    if (isEdit.value) {
      await updateStore(Number(route.params.id), submitData)
      message.success('门店更新成功')
    } else {
      if (!formData.tenantId) {
        message.warning('请选择所属租户')
        saving.value = false
        return
      }
      await createStore(submitData, formData.tenantId)
      message.success('门店创建成功')
    }

    router.push('/system/store/list')
  } catch (e: any) {
    if (e?.message) {
      message.error(e.message)
    }
  } finally {
    saving.value = false
  }
}

async function loadTenants() {
  try {
    const res = await getTenants({ pageSize: 100 })
    tenantOptions.value = res.list.map((t: BackendTenant) => ({
      label: t.tenantName,
      value: t.id
    }))
  } catch {
    // ignore
  }
}

async function loadStoreData() {
  if (!isEdit.value) return
  try {
    const data = await getStore(Number(route.params.id))
    formData.tenantId = data.tenantId
    formData.storeName = data.storeName
    formData.storeType = Number(data.storeType) || 1
    formData.contactPhone = data.contactPhone ?? ''
    formData.address = data.address ?? ''
    formData.businessHours = data.businessHours ?? ''
    formData.sort = data.sort ?? 0
    formData.remark = data.remark ?? ''
  } catch (e: any) {
    message.error('加载门店数据失败')
  }
}

onMounted(() => {
  loadTenants()
  loadStoreData()
})
</script>

<style scoped>
.store-form-page {
  padding: 0;
}
</style>
