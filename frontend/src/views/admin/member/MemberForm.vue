<template>
  <div class="member-form-page">
    <n-card :title="isEdit ? '编辑会员' : '新增会员'" :bordered="false">
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="80"
        style="max-width: 500px"
      >
        <n-form-item label="姓名" path="name">
          <n-input v-model:value="formData.name" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="formData.phone" placeholder="请输入手机号" maxlength="11" />
        </n-form-item>
        <n-form-item label="性别" path="gender">
          <n-radio-group v-model:value="formData.gender">
            <n-radio :value="1">男</n-radio>
            <n-radio :value="2">女</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="生日" path="birthday">
          <n-date-picker
            v-model:value="formData.birthdayTs"
            type="date"
            clearable
            style="width: 100%"
          />
        </n-form-item>
        <n-form-item label="会员等级" path="levelId">
          <n-select
            v-model:value="formData.levelId"
            :options="levelOptions"
            placeholder="请选择会员等级"
          />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input
            v-model:value="formData.remark"
            type="textarea"
            placeholder="备注信息"
            :rows="3"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="handleCancel">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NCard, NForm, NFormItem, NInput, NSelect, NDatePicker,
  NRadioGroup, NRadio, NButton, NSpace, useMessage
} from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { getMember, createMember, updateMember } from '../../../api/member'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const formData = reactive({
  name: '',
  phone: '',
  gender: 1 as number | null,
  birthdayTs: null as number | null,
  levelId: 1 as number | null,
  remark: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  levelId: [{ required: true, type: 'number', message: '请选择会员等级', trigger: 'change' }]
}

const levelOptions = [
  { label: '普通会员', value: 1 },
  { label: '银卡会员', value: 2 },
  { label: '金卡会员', value: 3 },
  { label: '钻石会员', value: 4 }
]

async function loadMember() {
  if (!isEdit.value) return
  const id = Number(route.params.id)
  const data = await getMember(id)
  formData.name = data.name
  formData.phone = data.phone
  formData.gender = data.gender
  formData.levelId = data.levelId
  if (data.birthday) {
    formData.birthdayTs = new Date(data.birthday).getTime()
  }
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
      name: formData.name,
      phone: formData.phone,
      gender: formData.gender,
      levelId: formData.levelId
    }
    if (formData.birthdayTs) {
      data.birthday = new Date(formData.birthdayTs).toISOString().slice(0, 19)
    }
    if (isEdit.value) {
      await updateMember(Number(route.params.id), data)
      message.success('更新成功')
    } else {
      await createMember(data)
      message.success('创建成功')
    }
    router.back()
  } finally {
    submitting.value = false
  }
}

function handleCancel() {
  router.back()
}

onMounted(() => {
  loadMember()
})
</script>

<style scoped>
.member-form-page { padding: 0; }
</style>
