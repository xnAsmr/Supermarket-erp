<template>
  <div class="login-page">
    <!-- 左：品牌文案区 -->
    <div class="split-left">
      <div class="left-inner">
        <!-- ASCII 风格品牌字标 -->
        <div class="brand-mark">
          <span>____</span>
          <span>[灵犀]</span>
          <span>____</span>
        </div>

        <h1 class="display-xl">灵犀智能零售</h1>
        <div class="hairline"></div>

        <p class="left-lead">
          <span class="bracket">[+]</span> 商超收银出入库系统
          <span class="muted">POS / 库存 / 会员 / 报表</span>
        </p>
        <p class="left-lead">
          <span class="bracket">[+]</span> 多租户 RBAC 权限控制
          <span class="muted">角色 · 菜单 · 按钮级鉴权</span>
        </p>
        <p class="left-lead">
          <span class="bracket">[+]</span> 财务日结 + 操作日志
          <span class="muted">AOP 自动审计，租户隔离</span>
        </p>

        <div class="hairline"></div>

        <!-- 暗色 TUI 风格提示条 -->
        <div class="tui-snippet">
          <span class="tui-prompt">$</span>
          <span class="tui-cmd">lixi login --secure</span>
          <span class="tui-keys">tab switch  ctrl-p commands</span>
        </div>
      </div>
    </div>

    <!-- 右：登录表单区 -->
    <div class="split-right">
      <div class="right-inner">
        <div class="section-label">[登录]</div>

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
            <button class="link-forgot" type="button">忘记密码？</button>
          </div>

          <button class="login-button" type="button" :disabled="loading" @click="handleLogin">
            {{ loading ? '登录中…' : '[ 登 录 ]' }}
          </button>
        </n-form>

        <!-- 演示账号 -->
        <div class="demo-section">
          <div class="demo-header">
            <span class="demo-arrow">[+]</span>
            <span class="demo-text">演示账号</span>
          </div>
          <div class="demo-accounts">
            <button class="demo-card" type="button" @click="fillAccount('admin', '123456')">
              <span class="demo-name">租户管理员</span>
              <span class="demo-cred">admin / 123456</span>
            </button>
            <button class="demo-card" type="button" @click="fillAccount('sysadmin', '123456')">
              <span class="demo-name">系统管理员</span>
              <span class="demo-cred">sysadmin / 123456</span>
            </button>
          </div>
        </div>

        <div class="copyright">
          © 2026 灵犀 LingXi · 商超收银出入库系统
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NCheckbox, useMessage } from 'naive-ui'
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
/* ===== 基础画布：奶油色 + 等宽字体 ===== */
.login-page {
  display: flex;
  width: 100vw;
  min-height: 100vh;
  background: #fdfcfc;
  font-family: 'JetBrains Mono', 'IBM Plex Mono', ui-monospace, SFMono-Regular,
    Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace;
  color: #201d1d;
  -webkit-font-smoothing: antialiased;
}

/* ===== 左右 50/50 分栏 ===== */
.split-left,
.split-right {
  width: 50%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}

.split-left {
  background: #fdfcfc;
}

.split-right {
  background: #f8f7f7;
  border-left: 1px solid rgba(15, 0, 0, 0.12);
}

.left-inner {
  width: 100%;
  max-width: 360px;
}

.right-inner {
  width: 100%;
  max-width: 480px;
}

/* ===== 左栏：品牌区 ===== */
.brand-mark {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #201d1d;
  margin-bottom: 24px;
  letter-spacing: 1px;
}

.brand-mark span:first-child,
.brand-mark span:last-child {
  color: #9a9898;
}

.display-xl {
  font-size: 38px;
  font-weight: 700;
  line-height: 1.5;
  color: #201d1d;
  margin: 0;
}

.hairline {
  height: 1px;
  background: rgba(15, 0, 0, 0.12);
  margin: 24px 0;
}

.left-lead {
  font-size: 16px;
  font-weight: 400;
  line-height: 1.5;
  color: #424245;
  margin: 0 0 12px 0;
}

.bracket {
  color: #201d1d;
  font-weight: 700;
  margin-right: 4px;
}

.muted {
  color: #646262;
}

.tui-snippet {
  background: #201d1d;
  border-radius: 4px;
  padding: 16px;
  margin-top: 8px;
  font-size: 16px;
  line-height: 1.5;
}

.tui-prompt {
  color: #30d158;
  margin-right: 6px;
}

.tui-cmd {
  color: #fdfcfc;
}

.tui-keys {
  display: block;
  color: #9a9898;
  font-size: 14px;
  margin-top: 8px;
}

/* ===== 右栏：表单区 ===== */
.section-label {
  font-size: 16px;
  font-weight: 700;
  color: #201d1d;
  margin-bottom: 32px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.link-forgot {
  background: none;
  border: none;
  padding: 0;
  font-family: inherit;
  font-size: 14px;
  color: #201d1d;
  cursor: pointer;
}

.link-forgot:hover {
  color: #007aff;
}

.login-button {
  width: 100%;
  height: 40px;
  background: #201d1d;
  color: #fdfcfc;
  border: none;
  border-radius: 4px;
  font-family: inherit;
  font-size: 16px;
  font-weight: 500;
  line-height: 2;
  cursor: pointer;
  transition: background 0.15s ease;
  padding: 4px 20px;
}

.login-button:hover {
  background: #302c2c;
}

.login-button:active {
  background: #0f0000;
}

.login-button:disabled {
  background: #f1eeee;
  color: #9a9898;
  cursor: not-allowed;
}

/* 演示账号 */
.demo-section {
  margin-top: 40px;
}

.demo-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
}

.demo-arrow {
  color: #201d1d;
  font-weight: 700;
}

.demo-text {
  font-size: 14px;
  color: #646262;
}

.demo-accounts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.demo-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 10px 12px;
  background: #f1eeee;
  border: 1px solid rgba(15, 0, 0, 0.12);
  border-radius: 4px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: border-color 0.15s ease;
}

.demo-card:hover {
  border-color: #201d1d;
}

.demo-card:active {
  background: #e8e6e5;
}

.demo-name {
  font-size: 16px;
  font-weight: 500;
  color: #201d1d;
}

.demo-cred {
  font-size: 14px;
  color: #6e6e73;
}

.copyright {
  margin-top: 24px;
  font-size: 14px;
  color: #646262;
}

/* ===== Naive UI 覆盖：单色等宽输入框 ===== */
:deep(.n-form-item-label__text) {
  font-family: inherit;
  font-size: 16px;
  font-weight: 500;
  color: #201d1d;
}

:deep(.n-input) {
  border-radius: 4px;
  font-family: inherit;
}

:deep(.n-input .n-input__input) {
  font-family: inherit;
  font-size: 16px;
  color: #201d1d;
}

:deep(.n-input .n-input__border) {
  border: 1px solid rgba(15, 0, 0, 0.12);
}

:deep(.n-input--focus .n-input__border) {
  border: 1px solid #201d1d;
  box-shadow: none;
}

:deep(.n-input__placeholder) {
  color: #6e6e73;
  font-family: inherit;
}

:deep(.n-checkbox .n-checkbox__label) {
  font-family: inherit;
  font-size: 14px;
  color: #424245;
}

:deep(.n-checkbox .n-checkbox-box) {
  border-radius: 4px;
}

:deep(.n-checkbox.n-checkbox--checked .n-checkbox-box) {
  background-color: #201d1d;
  border-color: #201d1d;
}
</style>