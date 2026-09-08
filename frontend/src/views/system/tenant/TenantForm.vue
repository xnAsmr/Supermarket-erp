<template>
  <div class="tenant-form-page">
    <n-card :bordered="false">
      <template #header>
        <n-space align="center">
          <n-button quaternary @click="handleBack">
            <template #icon><n-icon :component="ArrowBackOutline" /></template>
          </n-button>
          <span>{{ isEdit ? '编辑租户' : '新增租户' }}</span>
        </n-space>
      </template>

      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="120"
        style="max-width: 600px"
      >
        <n-form-item label="租户名称" path="tenantName">
          <n-input v-model:value="formData.tenantName" placeholder="请输入租户名称（超市名称）" />
        </n-form-item>

        <n-form-item label="联系人" path="contactName">
          <n-input v-model:value="formData.contactName" placeholder="请输入联系人姓名" />
        </n-form-item>

        <n-form-item label="联系电话" path="contactPhone">
          <n-input v-model:value="formData.contactPhone" placeholder="请输入联系电话" />
        </n-form-item>

        <n-form-item label="联系邮箱" path="contactEmail">
          <n-input v-model:value="formData.contactEmail" placeholder="请输入联系邮箱（选填）" />
        </n-form-item>

        <n-form-item label="地址" path="address">
          <n-input v-model:value="formData.address" placeholder="请输入地址（选填）" />
        </n-form-item>

        <n-form-item label="营业执照号" path="licenseNo">
          <n-input v-model:value="formData.licenseNo" placeholder="请输入营业执照号（选填）" />
        </n-form-item>

        <n-form-item label="套餐类型" path="planType">
          <n-radio-group v-model:value="formData.planType">
            <n-space>
              <n-radio :value="1">基础版</n-radio>
              <n-radio :value="2">专业版</n-radio>
              <n-radio :value="3">企业版</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>

        <n-form-item label="最大门店数" path="maxStores">
          <n-input-number v-model:value="formData.maxStores" :min="1" :max="999" style="width: 200px" />
        </n-form-item>

        <n-form-item label="最大用户数" path="maxUsers">
          <n-input-number v-model:value="formData.maxUsers" :min="1" :max="9999" style="width: 200px" />
        </n-form-item>

        <n-form-item label="到期时间" path="expireTime">
          <n-date-picker
            v-model:value="expireTimestamp"
            type="date"
            style="width: 200px"
          />
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
  NCard, NForm, NFormItem, NInput, NInputNumber,
  NButton, NSpace, NIcon, NRadioGroup, NRadio,
  NDatePicker, useMessage
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { ArrowBackOutline } from '@vicons/ionicons5'
import { getTenant, createTenant, updateTenant } from '../../../api/tenant'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const formData = reactive({
  tenantName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  licenseNo: '',
  planType: 1,
  maxStores: 1,
  maxUsers: 5,
  remark: ''
})

const expireTimestamp = ref<number | null>(null)

const formRules: FormRules = {
  tenantName: { required: true, message: '请输入租户名称', trigger: 'blur' },
  contactName: { required: true, message: '请输入联系人', trigger: 'blur' },
  contactPhone: { required: true, message: '请输入联系电话', trigger: 'blur' },
  planType: { required: true, type: 'number', message: '请选择套餐类型', trigger: 'change' },
  maxStores: { required: true, type: 'number', message: '请输入最大门店数', trigger: 'blur' },
  maxUsers: { required: true, type: 'number', message: '请输入最大用户数', trigger: 'blur' }
}

function handleBack() {
  router.push('/system/tenant/list')
}

function formatDate(timestamp: number): string {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

async function handleSubmit() {
  try {
    if (!expireTimestamp.value) {
      message.warning('请选择到期时间')
      return
    }
    await formRef.value?.validate()
    saving.value = true

    const submitData = {
      ...formData,
      expireTime: formatDate(expireTimestamp.value)
    }

    if (isEdit.value) {
      await updateTenant(Number(route.params.id), submitData)
      message.success('租户更新成功')
    } else {
      await createTenant(submitData)
      message.success('租户创建成功')
    }

    router.push('/system/tenant/list')
  } catch (e: any) {
    if (e?.message) {
      message.error(e.message)
    }
  } finally {
    saving.value = false
  }
}

async function loadTenantData() {
  if (!isEdit.value) return
  try {
    const data = await getTenant(Number(route.params.id))
    formData.tenantName = data.tenantName
    formData.contactName = data.contactName
    formData.contactPhone = data.contactPhone
    formData.contactEmail = (data as any).contactEmail ?? ''
    formData.address = (data as any).address ?? ''
    formData.licenseNo = (data as any).licenseNo ?? ''
    formData.planType = (data as any).planType ?? 1
    formData.maxStores = data.maxStores ?? 1
    formData.maxUsers = data.maxUsers ?? 5
    formData.remark = (data as any).remark ?? ''
    if (data.expireTime) {
      expireTimestamp.value = new Date(data.expireTime).getTime()
    }
  } catch (e: any) {
    message.error('加载租户数据失败')
  }
}

onMounted(() => {
  loadTenantData()
})
</script>

<style scoped>
.tenant-form-page {
  padding: 0;
}
</style>
