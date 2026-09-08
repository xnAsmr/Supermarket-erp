<template>
  <div class="profile-page">
    <n-card title="个人中心" :bordered="false">
      <n-grid :cols="2" :x-gap="24">
        <n-grid-item :span="1">
          <div class="profile-info">
            <div class="avatar-section">
              <n-avatar :size="80" round v-if="profile.avatar">
                <img :src="profile.avatar" alt="avatar" style="width: 100%; height: 100%; object-fit: cover" />
              </n-avatar>
              <n-avatar :size="80" round v-else style="font-size: 32px; background-color: #18a058">
                {{ profile.name?.charAt(0) || 'U' }}
              </n-avatar>
              <n-upload
                :show-file-list="false"
                accept="image/*"
                :custom-request="handleAvatarUpload"
              >
                <n-button size="small" text type="primary" style="margin-left: 12px">更换头像</n-button>
              </n-upload>
            </div>
            <n-descriptions :column="1" label-placement="left" bordered size="small" style="margin-top: 20px">
              <n-descriptions-item label="用户名">{{ profile.username }}</n-descriptions-item>
              <n-descriptions-item label="角色">{{ profile.roleName || '-' }}</n-descriptions-item>
              <n-descriptions-item label="注册时间">{{ profile.createTime || '-' }}</n-descriptions-item>
            </n-descriptions>
          </div>
        </n-grid-item>
        <n-grid-item :span="1">
          <n-tabs type="line" v-model:value="activeTab">
            <n-tab-pane name="basic" tab="基本信息">
              <n-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-placement="left"
                label-width="80"
                style="margin-top: 16px"
              >
                <n-form-item label="姓名" path="name">
                  <n-input v-model:value="basicForm.name" placeholder="请输入姓名" />
                </n-form-item>
                <n-form-item label="手机" path="phone">
                  <n-input v-model:value="basicForm.phone" placeholder="请输入手机号" />
                </n-form-item>
                <n-form-item label="邮箱" path="email">
                  <n-input v-model:value="basicForm.email" placeholder="请输入邮箱" />
                </n-form-item>
                <n-form-item>
                  <n-button type="primary" :loading="savingBasic" @click="handleSaveBasic">保存</n-button>
                </n-form-item>
              </n-form>
            </n-tab-pane>
            <n-tab-pane name="password" tab="修改密码">
              <n-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-placement="left"
                label-width="80"
                style="margin-top: 16px"
              >
                <n-form-item label="旧密码" path="oldPassword">
                  <n-input v-model:value="pwdForm.oldPassword" type="password" show-password-on="click" placeholder="请输入旧密码" />
                </n-form-item>
                <n-form-item label="新密码" path="newPassword">
                  <n-input v-model:value="pwdForm.newPassword" type="password" show-password-on="click" placeholder="请输入新密码" />
                </n-form-item>
                <n-form-item label="确认密码" path="confirmPassword">
                  <n-input v-model:value="pwdForm.confirmPassword" type="password" show-password-on="click" placeholder="请再次输入新密码" />
                </n-form-item>
                <n-form-item>
                  <n-button type="primary" :loading="savingPwd" @click="handleSavePassword">修改密码</n-button>
                </n-form-item>
              </n-form>
            </n-tab-pane>
          </n-tabs>
        </n-grid-item>
      </n-grid>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  NCard, NGrid, NGridItem, NAvatar, NUpload, NButton, NForm, NFormItem,
  NInput, NTabs, NTabPane, NDescriptions, NDescriptionsItem, useMessage
} from 'naive-ui'
import type { FormInst, FormRules, UploadCustomRequestOptions } from 'naive-ui'
import { getProfile, updateProfile, updatePassword } from '../../../api/system'
import type { UserProfile } from '../../../api/system'
import { uploadImage } from '../../../api/product'
import { useUserStore } from '../../../stores/user'

const message = useMessage()
const userStore = useUserStore()

const activeTab = ref('basic')

const profile = ref<UserProfile>({
  id: 0,
  username: '',
  name: '',
  phone: null,
  email: null,
  avatar: null,
  userType: 0,
  roleName: null,
  createTime: ''
})

// Basic info form
const basicFormRef = ref<FormInst | null>(null)
const savingBasic = ref(false)
const basicForm = reactive({
  name: '',
  phone: '',
  email: ''
})
const basicRules: FormRules = {
  name: { required: true, message: '请输入姓名', trigger: 'blur' }
}

// Password form
const pwdFormRef = ref<FormInst | null>(null)
const savingPwd = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdRules: FormRules = {
  oldPassword: { required: true, message: '请输入旧密码', trigger: 'blur' },
  newPassword: { required: true, message: '请输入新密码', trigger: 'blur' },
  confirmPassword: { required: true, message: '请确认新密码', trigger: 'blur' }
}

async function loadProfile() {
  try {
    const res = await getProfile()
    profile.value = res
    basicForm.name = res.name
    basicForm.phone = res.phone || ''
    basicForm.email = res.email || ''
  } catch {
    message.error('加载个人信息失败')
  }
}

async function handleSaveBasic() {
  try {
    await basicFormRef.value?.validate()
  } catch {
    return
  }
  savingBasic.value = true
  try {
    await updateProfile({
      name: basicForm.name,
      phone: basicForm.phone,
      email: basicForm.email
    })
    profile.value.name = basicForm.name
    profile.value.phone = basicForm.phone
    profile.value.email = basicForm.email
    userStore.setUserInfo({
      ...userStore.userInfo!,
      realName: basicForm.name,
      phone: basicForm.phone,
      email: basicForm.email
    })
    message.success('保存成功')
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    savingBasic.value = false
  }
}

async function handleSavePassword() {
  if (!pwdForm.oldPassword) {
    message.warning('请输入旧密码')
    return
  }
  if (!pwdForm.newPassword) {
    message.warning('请输入新密码')
    return
  }
  if (pwdForm.newPassword.length < 6) {
    message.warning('新密码长度不能少于6位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    message.warning('两次密码输入不一致')
    return
  }

  savingPwd.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    message.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e: any) {
    message.error(e?.message || '密码修改失败')
  } finally {
    savingPwd.value = false
  }
}

async function handleAvatarUpload({ file }: UploadCustomRequestOptions) {
  if (!file.file) return
  if (file.file.size > 2 * 1024 * 1024) {
    message.warning('图片大小不能超过2MB')
    return
  }
  try {
    const url = await uploadImage(file.file)
    await updateProfile({ avatar: url })
    profile.value.avatar = url
    userStore.setUserInfo({
      ...userStore.userInfo!,
      avatar: url
    })
    message.success('头像更新成功')
  } catch (e: any) {
    message.error(e?.message || '头像上传失败')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  padding: 0;
}
.avatar-section {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #eee;
}
</style>
