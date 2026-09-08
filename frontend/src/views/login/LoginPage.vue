<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <!-- Logo & Brand -->
        <div class="brand">
          <h1 class="brand-name">灵犀</h1>
          <p class="brand-slogan">心有灵犀，智管商业</p>
        </div>

        <!-- Login Form -->
        <n-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-placement="top"
          size="large"
          @submit.prevent
        >
          <n-form-item path="username" label="用户名">
            <n-input
              v-model:value="formData.username"
              placeholder="请输入用户名"
              @keyup.enter="handleLogin"
            />
          </n-form-item>

          <n-form-item path="password" label="密码">
            <n-input
              v-model:value="formData.password"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              @keyup.enter="handleLogin"
            />
          </n-form-item>

          <div class="login-options">
            <n-checkbox v-model:checked="rememberMe">记住我</n-checkbox>
            <n-button text type="primary" size="small">忘记密码？</n-button>
          </div>

          <n-button
            class="login-button"
            type="primary"
            block
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </n-button>
        </n-form>

        <!-- Demo Accounts -->
        <div class="demo-section">
          <div class="demo-header">
            <span class="demo-line"></span>
            <span class="demo-text">演示账号</span>
            <span class="demo-line"></span>
          </div>
          <div class="demo-accounts">
            <div class="demo-card" @click="fillAccount('admin', '123456')">
              <div class="demo-info">
                <span class="demo-name">管理员</span>
                <span class="demo-cred">admin / 123456</span>
              </div>
            </div>
            <div class="demo-card" @click="fillAccount('sysadmin', '123456')">
              <div class="demo-info">
                <span class="demo-name">系统管理员</span>
                <span class="demo-cred">sysadmin / 123456</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="login-footer">
        <p>© 2026 灵犀 LingXi. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NCheckbox, useMessage } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const rememberMe = ref(false)

const formData = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

function fillAccount(username: string, password: string) {
  formData.username = username
  formData.password = password
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const success = await userStore.login({
      username: formData.username,
      password: formData.password
    })
    if (success) {
      message.success('登录成功')
      if (userStore.role === 'system_admin') {
        router.push('/system/dashboard')
      } else {
        router.push('/admin/dashboard')
      }
    } else {
      message.error('用户名或密码错误')
    }
  } catch (error) {
    message.error('登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 400px;
  padding: 0 24px;
}

.login-card {
  width: 100%;
  padding: 48px 0;
}

.brand {
  text-align: center;
  margin-bottom: 48px;
}

.logo {
  margin-bottom: 24px;
}

.brand-name {
  font-size: 32px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

.login-button {
  margin-top: 8px;
  height: 48px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  background: #1a1a2e;
  border: none;
}

.login-button:hover {
  background: #2d2d44;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

/* Demo Section */
.demo-section {
  margin-top: 40px;
}

.demo-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.demo-line {
  flex: 1;
  height: 1px;
  background: #f0f0f0;
}

.demo-text {
  font-size: 12px;
  color: #c0c0c0;
  white-space: nowrap;
}

.demo-accounts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.demo-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.demo-card:hover {
  background: #f0f0f0;
}

.demo-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.demo-name {
  font-size: 14px;
  color: #374151;
}

.demo-cred {
  font-size: 12px;
  color: #9ca3af;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

/* Footer */
.login-footer {
  margin-top: 24px;
}

.login-footer p {
  font-size: 12px;
  color: #d1d5db;
  margin: 0;
}

/* Naive UI overrides */
:deep(.n-form-item-label__text) {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

:deep(.n-input) {
  border-radius: 8px;
}

:deep(.n-input--focus .n-input__border) {
  border-color: #1a1a2e;
  box-shadow: 0 0 0 1px #1a1a2e;
}

:deep(.n-checkbox .n-checkbox__label) {
  font-size: 14px;
  color: #6b7280;
}
</style>
