<template>
  <div class="payment">
    <div class="payment-main">
      <!-- 订单确认 -->
      <n-card title="订单确认" :bordered="false" class="order-card">
        <div class="order-items">
          <div v-for="item in posStore.cartItems" :key="item.id" class="order-item">
            <span class="item-name">{{ item.productName }}</span>
            <span class="item-qty">x{{ item.quantity }}</span>
            <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
        <n-divider style="margin: 12px 0" />

        <!-- 会员优惠明细 -->
        <div v-if="posStore.currentMember" class="member-benefit">
          <n-space justify="space-between" align="center">
            <n-space align="center" :size="8">
              <n-icon :component="PersonOutline" size="16" color="#18a058" />
              <n-text strong>{{ posStore.currentMember.name }}</n-text>
              <n-tag :type="memberTagType" size="tiny">{{ posStore.currentMember.levelName }}</n-tag>
            </n-space>
            <n-button text type="error" size="tiny" @click="handleClearMember">取消会员</n-button>
          </n-space>
          <div class="benefit-detail" v-if="posStore.totalDiscount > 0">
            <n-space justify="space-between">
              <n-text depth="3" style="font-size: 12px">原价合计</n-text>
              <n-text delete depth="3" style="font-size: 12px">¥{{ posStore.totalAmount.toFixed(2) }}</n-text>
            </n-space>
            <n-space justify="space-between">
              <n-text type="success" style="font-size: 12px">
                <n-icon :component="GiftOutline" size="12" /> 会员优惠
              </n-text>
              <n-text type="success" style="font-size: 12px">-¥{{ posStore.totalDiscount.toFixed(2) }}</n-text>
            </n-space>
          </div>
          <div class="benefit-detail" v-else>
            <n-text depth="3" style="font-size: 12px">当前等级无额外折扣，享受积分奖励</n-text>
          </div>
        </div>

        <n-divider style="margin: 12px 0" />

        <n-space justify="space-between" align="center">
          <n-text>应付金额</n-text>
          <n-text strong style="font-size: 28px; color: #d03050">
            ¥{{ posStore.finalAmount.toFixed(2) }}
          </n-text>
        </n-space>
      </n-card>

      <!-- 支付方式 -->
      <n-card title="选择支付方式" :bordered="false" style="margin-top: 16px">
        <div class="pay-methods">
          <div
            v-for="method in availablePayMethods"
            :key="method.code"
            class="pay-method-card"
            :class="{ active: !isCombinedMode && selectedMethod === method.code, disabled: method.disabled }"
            @click="!method.disabled && selectSingleMethod(method.code)"
          >
            <div class="pay-icon">{{ method.icon }}</div>
            <div class="pay-name">{{ method.name }}</div>
            <div v-if="method.disabled" class="pay-disabled">{{ method.disabledReason }}</div>
            <div v-else-if="method.code === 'stored' && posStore.currentMember" class="pay-balance">
              余额 ¥{{ posStore.currentMember.balance.toFixed(2) }}
            </div>
          </div>
          <!-- 组合支付按钮 -->
          <div
            class="pay-method-card"
            :class="{ active: isCombinedMode }"
            @click="toggleCombinedMode"
          >
            <div class="pay-icon">🔀</div>
            <div class="pay-name">组合支付</div>
          </div>
        </div>

        <!-- 单一支付方式输入区 -->
        <template v-if="!isCombinedMode">
          <!-- 现金支付 -->
          <div v-if="selectedMethod === 'cash'" class="cash-input" style="margin-top: 20px">
            <n-space align="center" :size="12">
              <n-text>收款金额：</n-text>
              <n-input-number
                v-model:value="receivedAmount"
                :min="0"
                :precision="2"
                placeholder="请输入收款金额"
                size="large"
                style="width: 200px"
                @update:value="handleAmountChange"
              />
              <n-button size="large" @click="handleQuickAmount(posStore.finalAmount)">
                刚好
              </n-button>
              <n-button size="large" @click="handleQuickAmount(Math.ceil(posStore.finalAmount))">
                整数
              </n-button>
            </n-space>
            <div v-if="receivedAmount > 0" class="change-amount">
              <n-space align="center">
                <n-text depth="3">找零：</n-text>
                <n-text
                  strong
                  :style="{ color: changeAmount >= 0 ? '#18a058' : '#d03050', fontSize: '20px' }"
                >
                  ¥{{ Math.abs(changeAmount).toFixed(2) }}
                </n-text>
                <n-tag v-if="changeAmount < 0" type="error" size="small">收款不足</n-tag>
              </n-space>
            </div>
          </div>

          <!-- 储值卡支付 -->
          <div v-else-if="selectedMethod === 'stored'" class="stored-input" style="margin-top: 20px">
            <n-space align="center" :size="12">
              <n-text>储值卡余额：</n-text>
              <n-text strong style="font-size: 18px; color: #18a058">
                ¥{{ posStore.currentMember?.balance.toFixed(2) }}
              </n-text>
            </n-space>
            <div v-if="insufficientBalance" class="insufficient-notice">
              <n-alert type="warning" style="margin-top: 8px">
                储值余额不足，差额 ¥{{ (posStore.finalAmount - (posStore.currentMember?.balance || 0)).toFixed(2) }}
                请选择其他支付方式补足差额
              </n-alert>
            </div>
          </div>

          <!-- 其他支付方式 -->
          <div v-else class="confirm-area" style="margin-top: 20px">
            <n-space align="center">
              <n-text>确认金额：</n-text>
              <n-text strong style="font-size: 20px; color: #d03050">
                ¥{{ posStore.finalAmount.toFixed(2) }}
              </n-text>
            </n-space>
          </div>
        </template>

        <!-- 组合支付输入区 -->
        <template v-else>
          <div class="combined-pay-section" style="margin-top: 16px">
            <div v-for="(row, idx) in combinedPayments" :key="idx" class="combined-row">
              <n-select
                v-model:value="row.paymentMethod"
                :options="paymentMethodOptions"
                placeholder="选择支付方式"
                style="width: 150px"
              />
              <n-input-number
                v-model:value="row.amount"
                :min="0"
                :precision="2"
                placeholder="金额"
                style="width: 160px"
              />
              <n-text depth="3" v-if="row.paymentMethod === 'cash'">（现金）</n-text>
              <n-button
                v-if="combinedPayments.length > 1"
                type="error"
                text
                @click="removeCombinedRow(idx)"
              >
                删除
              </n-button>
            </div>
            <n-button dashed type="primary" style="margin-top: 12px" @click="addCombinedRow">
              + 添加支付方式
            </n-button>

            <n-divider style="margin: 12px 0" />
            <n-space justify="space-between" align="center">
              <n-text>应付金额：</n-text>
              <n-text strong style="color: #d03050">¥{{ posStore.finalAmount.toFixed(2) }}</n-text>
            </n-space>
            <n-space justify="space-between" align="center" style="margin-top: 4px">
              <n-text>已支付：</n-text>
              <n-text strong :type="combinedTotal >= posStore.finalAmount ? 'success' : 'error'">
                ¥{{ combinedTotal.toFixed(2) }}
              </n-text>
            </n-space>
            <n-space v-if="combinedTotal > posStore.finalAmount" justify="space-between" align="center" style="margin-top: 4px">
              <n-text>找零：</n-text>
              <n-text strong type="success">¥{{ (combinedTotal - posStore.finalAmount).toFixed(2) }}</n-text>
            </n-space>
          </div>
        </template>
      </n-card>

      <!-- 积分预估 -->
      <n-card v-if="posStore.currentMember" :bordered="false" style="margin-top: 16px; padding: 0">
        <n-space justify="space-between" align="center">
          <n-space align="center" :size="8">
            <n-icon :component="StarOutline" size="16" color="#f0a020" />
            <n-text>支付后预计获得</n-text>
          </n-space>
          <n-text strong style="font-size: 18px; color: #f0a020">
            +{{ posStore.estimatedPoints }} 积分
          </n-text>
        </n-space>
      </n-card>

      <!-- 底部按钮 -->
      <div class="pay-footer">
        <n-button size="large" @click="handleBack">返回</n-button>
        <n-button
          type="primary"
          size="large"
          :loading="paying"
          :disabled="!canPay"
          @click="handlePay"
        >
          确认支付 ¥{{ posStore.finalAmount.toFixed(2) }}
        </n-button>
      </div>
    </div>

    <!-- 支付成功弹窗 -->
    <n-modal
      v-model:show="showResult"
      preset="card"
      title="支付成功"
      style="width: 480px"
      :closable="false"
      mask-closable
    >
      <div class="pay-result">
        <div class="result-icon">✅</div>
        <n-text strong style="font-size: 20px">支付成功</n-text>
        <n-text depth="3" style="margin-top: 8px">订单号：{{ paidOrderNo }}</n-text>
        <n-text strong style="font-size: 28px; color: #18a058; margin-top: 16px">
          ¥{{ paidAmount.toFixed(2) }}
        </n-text>

        <!-- 会员支付成功信息 -->
        <div v-if="posStore.currentMember" class="member-result">
          <n-divider style="margin: 16px 0 12px" />
          <n-space direction="vertical" :size="8" style="width: 100%">
            <n-space justify="space-between">
              <n-text depth="3">会员</n-text>
              <n-text>{{ posStore.currentMember.name }} ({{ posStore.currentMember.levelName }})</n-text>
            </n-space>
            <n-space justify="space-between">
              <n-text depth="3">获得积分</n-text>
              <n-text type="success" strong>+{{ posStore.estimatedPoints }}</n-text>
            </n-space>
            <n-space justify="space-between">
              <n-text depth="3">当前积分</n-text>
              <n-text strong>{{ posStore.currentMember.points + posStore.estimatedPoints }}</n-text>
            </n-space>
            <n-space v-if="selectedMethod === 'stored'" justify="space-between">
              <n-text depth="3">储值卡余额</n-text>
              <n-text strong>¥{{ ((posStore.currentMember?.balance || 0) - posStore.finalAmount).toFixed(2) }}</n-text>
            </n-space>
          </n-space>
        </div>
      </div>
      <template #footer>
        <n-space justify="center">
          <n-button @click="handlePrint">打印小票</n-button>
          <n-button type="primary" @click="handleNewOrder">继续收银</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard, NButton, NSpace, NText, NInputNumber, NDivider, NTag, NModal,
  NAlert, NIcon, NSelect, useMessage
} from 'naive-ui'
import { PersonOutline, GiftOutline, StarOutline } from '@vicons/ionicons5'
import { usePosStore, type CartItem } from '@/stores/pos'
import { posCheckout, type CheckoutItem, type PaymentDetail } from '@/api/pos'
import { getPaymentMethods, type PaymentMethod } from '@/api/paymentMethod'

const router = useRouter()
const posStore = usePosStore()
const message = useMessage()

const selectedMethod = ref('cash')
const receivedAmount = ref(0)
const paying = ref(false)
const showResult = ref(false)
const paidOrderNo = ref('')
const paidAmount = ref(0)
const dbPayMethods = ref<PaymentMethod[]>([])

const isCombinedMode = ref(false)
const combinedPayments = ref<Array<{ paymentMethod: string; amount: number }>>([
  { paymentMethod: 'cash', amount: 0 }
])

const memberTagType = computed(() => {
  if (!posStore.currentMember) return 'info'
  const map: Record<string, 'info' | 'success' | 'warning' | 'error'> = {
    '普通会员': 'info',
    '银卡会员': 'success',
    '金卡会员': 'warning',
    '钻石会员': 'error'
  }
  return map[posStore.currentMember.levelName] || 'info'
})

/** 储值卡余额是否不足 */
const insufficientBalance = computed(() => {
  if (selectedMethod.value !== 'stored') return false
  if (!posStore.currentMember) return true
  return posStore.currentMember.balance < posStore.finalAmount
})

/** 可用支付方式列表 */
const availablePayMethods = computed(() => {
  const iconMap: Record<string, string> = { cash: '💵', wechat: '💚', alipay: '💙', card: '💳', stored: '🎫' }
  const methods = dbPayMethods.value
    .filter(m => m.status === 1)
    .map(m => ({
      code: m.code,
      name: m.name,
      icon: iconMap[m.code] || '💰',
      disabled: m.code === 'stored' && !posStore.currentMember,
      disabledReason: m.code === 'stored' && !posStore.currentMember ? '需先选择会员' : ''
    }))
  return methods
})

const paymentMethodOptions = computed(() => {
  const iconMap: Record<string, string> = { cash: '💵', wechat: '💚', alipay: '💙', card: '💳', stored: '🎫' }
  return dbPayMethods.value
    .filter(m => m.status === 1)
    .map(m => ({
      label: `${iconMap[m.code] || '💰'} ${m.name}`,
      value: m.code,
      disabled: m.code === 'stored' && !posStore.currentMember
    }))
})

const combinedTotal = computed(() => {
  return combinedPayments.value.reduce((sum, r) => sum + (r.amount || 0), 0)
})

const changeAmount = computed(() => {
  if (selectedMethod.value !== 'cash') return 0
  return (receivedAmount.value || 0) - posStore.finalAmount
})

const canPay = computed(() => {
  if (posStore.cartItems.length === 0) return false
  if (isCombinedMode.value) {
    return combinedTotal.value >= posStore.finalAmount
  }
  if (selectedMethod.value === 'cash') {
    return receivedAmount.value >= posStore.finalAmount
  }
  if (selectedMethod.value === 'stored') {
    return !insufficientBalance.value
  }
  return true
})

function handleAmountChange(val: number | null) {
  receivedAmount.value = val || 0
}

function handleQuickAmount(amount: number) {
  receivedAmount.value = amount
}

function handleClearMember() {
  posStore.setMember(null)
  selectedMethod.value = 'cash'
  isCombinedMode.value = false
  combinedPayments.value = [{ paymentMethod: 'cash', amount: 0 }]
  message.info('已取消会员')
}

function selectSingleMethod(code: string) {
  isCombinedMode.value = false
  selectedMethod.value = code
}

function toggleCombinedMode() {
  isCombinedMode.value = !isCombinedMode.value
  if (isCombinedMode.value) {
    combinedPayments.value = [{ paymentMethod: 'cash', amount: posStore.finalAmount }]
  }
}

function addCombinedRow() {
  combinedPayments.value.push({ paymentMethod: 'wechat', amount: 0 })
}

function removeCombinedRow(idx: number) {
  combinedPayments.value.splice(idx, 1)
}

async function handlePay() {
  paying.value = true
  try {
    const items: CheckoutItem[] = posStore.cartItems.map((item: CartItem) => ({
      productId: Number(item.productId),
      quantity: item.quantity,
      salePrice: item.price
    }))

    let payments: PaymentDetail[]

    if (isCombinedMode.value) {
      payments = combinedPayments.value
        .filter(r => r.amount > 0 && r.paymentMethod)
        .map(r => ({
          paymentMethod: r.paymentMethod,
          amount: r.amount,
          receivedAmount: r.paymentMethod === 'cash' ? r.amount : undefined
        }))
    } else {
      payments = [{
        paymentMethod: selectedMethod.value,
        amount: posStore.finalAmount,
        receivedAmount: selectedMethod.value === 'cash' ? receivedAmount.value : undefined
      }]
    }

    const res = await posCheckout({
      storeId: 1,
      memberId: posStore.currentMember?.id ? Number(posStore.currentMember.id) : undefined,
      items,
      payments
    })

    paidOrderNo.value = res.orderNo
    paidAmount.value = res.payAmount
    showResult.value = true
  } catch {
    message.error('支付失败')
  } finally {
    paying.value = false
  }
}

function handleBack() {
  router.push('/pos/cashier')
}

function handlePrint() {
  message.info('打印功能开发中')
}

function handleNewOrder() {
  posStore.clearCart()
  showResult.value = false
  router.push('/pos/cashier')
}

async function loadPayMethods() {
  try {
    dbPayMethods.value = await getPaymentMethods()
  } catch {}
}

onMounted(() => {
  loadPayMethods()
})
</script>

<style scoped>
.payment {
  height: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
  overflow-y: auto;
  background: #f5f5f5;
}

.payment-main {
  width: 100%;
  max-width: 600px;
}

.order-card {
  border-radius: 8px;
}

.order-items {
  max-height: 180px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.item-name {
  flex: 1;
  font-size: 13px;
  color: #333;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 12px;
}

.item-qty {
  font-size: 12px;
  color: #999;
  margin-right: 12px;
  flex-shrink: 0;
}

.item-price {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  flex-shrink: 0;
}

/* 会员优惠区域 */
.member-benefit {
  padding: 10px 12px;
  background: #f6ffed;
  border-radius: 6px;
  border: 1px solid #b7eb8f;
}

.benefit-detail {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #d9f7be;
}

/* 支付方式 */
.pay-methods {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.pay-method-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.pay-method-card:hover:not(.disabled) {
  border-color: #18a058;
}

.pay-method-card.active {
  border-color: #18a058;
  background: #f0fff4;
}

.pay-method-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pay-icon {
  font-size: 28px;
}

.pay-name {
  font-size: 12px;
  color: #666;
}

.pay-disabled {
  font-size: 10px;
  color: #d03050;
}

.pay-balance {
  font-size: 10px;
  color: #18a058;
}

/* 现金输入 */
.change-amount {
  margin-top: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
}

.confirm-area,
.stored-input {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 6px;
}

/* 组合支付 */
.combined-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.combined-pay-section {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

/* 底部按钮 */
.pay-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  gap: 16px;
}

/* 支付成功 */
.pay-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.result-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.member-result {
  width: 100%;
  text-align: left;
}
</style>
