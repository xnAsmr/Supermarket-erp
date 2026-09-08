<template>
  <div class="cashier">
    <div class="cashier-left">
      <div class="product-search">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索商品名称/编码/条码..."
          clearable
          size="large"
          @input="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-space :size="8" style="margin-top: 12px" wrap>
          <n-button
            :type="selectedCategory === null ? 'primary' : 'default'"
            size="small"
            @click="handleCategoryFilter(null)"
          >
            全部
          </n-button>
          <n-button
            v-for="cat in categories"
            :key="cat.id"
            :type="selectedCategory === cat.id ? 'primary' : 'default'"
            size="small"
            @click="handleCategoryFilter(cat.id)"
          >
            {{ cat.name }}
          </n-button>
        </n-space>
        <n-space v-if="subCategories.length > 0" :size="6" style="margin-top: 8px" wrap>
          <n-button
            :type="selectedSubCategory === null ? 'tertiary' : 'default'"
            size="small"
            @click="handleSubCategoryFilter(null)"
          >
            全部{{ currentCategoryName }}
          </n-button>
          <n-button
            v-for="sub in subCategories"
            :key="sub.id"
            :type="selectedSubCategory === sub.id ? 'tertiary' : 'default'"
            size="small"
            @click="handleSubCategoryFilter(sub.id)"
          >
            {{ sub.name }}
          </n-button>
        </n-space>
      </div>

      <div class="product-grid">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="product-card"
          @click="handleAddToCart(product)"
        >
          <div class="product-img">
            <img v-if="product.image" :src="product.image" :alt="product.name" style="width:100%;height:100%;object-fit:cover;border-radius:6px" />
            <n-icon v-else size="32" color="#ccc"><BagOutline /></n-icon>
          </div>
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-spec">{{ product.spec }}</div>
            <div class="product-stock">
              <n-text v-if="(product.shelfQuantity ?? 0) <= 0" type="error" strong>无货架库存</n-text>
              <n-text v-else-if="(product.shelfQuantity ?? 0) < 10" type="warning" strong>货架紧张: {{ product.shelfQuantity }}</n-text>
              <n-text v-else depth="3">货架: {{ product.shelfQuantity }} | 仓库: {{ product.warehouseQuantity }}</n-text>
            </div>
            <div class="product-price">
              <span v-if="posStore.currentMember" class="price-member">
                ¥{{ (product.vipPrice ?? product.salePrice).toFixed(2) }}
              </span>
              <span
                class="price-main"
                :class="{ 'price-original': posStore.currentMember }"
              >
                ¥{{ product.salePrice.toFixed(2) }}
              </span>
              <n-tag v-if="posStore.currentMember" type="warning" size="tiny" style="margin-left: 4px">
                {{ posStore.currentMember.levelName }}
              </n-tag>
            </div>
          </div>
          <n-button
            size="tiny"
            type="primary"
            circle
            class="add-btn"
            @click.stop="handleAddToCart(product)"
          >
            <template #icon><n-icon :component="AddOutline" /></template>
          </n-button>
        </div>
        <n-empty v-if="filteredProducts.length === 0" description="暂无商品" style="margin-top: 60px" />
      </div>
    </div>

    <div class="cashier-right">
      <!-- 会员搜索区域 -->
      <div class="member-section">
        <template v-if="!posStore.currentMember">
          <div class="member-search">
            <n-input
              v-model:value="memberPhone"
              placeholder="输入手机号查询会员"
              clearable
              size="small"
              maxlength="11"
              @keyup.enter="handleSearchMember"
            >
              <template #prefix>
                <n-icon :component="PersonOutline" />
              </template>
            </n-input>
            <n-button
              type="primary"
              size="small"
              :loading="memberSearching"
              @click="handleSearchMember"
            >
              查询
            </n-button>
          </div>
          <n-text depth="3" style="font-size: 11px; margin-top: 4px; display: block">
            输入会员手机号享受专属折扣
          </n-text>
        </template>
        <template v-else>
          <div class="member-info">
            <div class="member-avatar">
              <n-icon size="24" color="#18a058"><PersonOutline /></n-icon>
            </div>
            <div class="member-detail">
              <div class="member-name">
                {{ posStore.currentMember.name }}
                <n-tag :type="memberTagType" size="tiny" style="margin-left: 4px">
                  {{ posStore.currentMember.levelName }}
                </n-tag>
              </div>
              <div class="member-meta">
                <span>折扣: {{ (posStore.currentMember.discount * 10).toFixed(1) }}折</span>
                <span>积分: {{ posStore.currentMember.points }}</span>
                <span>余额: ¥{{ posStore.currentMember.balance.toFixed(2) }}</span>
              </div>
            </div>
            <n-button text type="error" size="tiny" @click="handleClearMember">
              取消
            </n-button>
          </div>
        </template>
      </div>

      <!-- 购物车头部 -->
      <div class="cart-header">
        <n-space justify="space-between" align="center">
          <n-text strong style="font-size: 16px">购物车</n-text>
          <n-button text type="error" size="small" @click="handleClearCart" v-if="posStore.cartItems.length > 0">
            清空
          </n-button>
        </n-space>
        <n-text depth="3" style="font-size: 12px; margin-top: 4px; display: block">
          共 {{ posStore.totalCount }} 件商品
        </n-text>
      </div>

      <!-- 购物车列表 -->
      <div class="cart-list">
        <div v-if="posStore.cartItems.length === 0" class="cart-empty">
          <n-icon size="48" color="#ddd"><CartOutline /></n-icon>
          <n-text depth="3">购物车为空</n-text>
        </div>
        <div
          v-for="item in posStore.cartItems"
          :key="item.id"
          class="cart-item"
        >
          <div class="cart-item-info">
            <div class="cart-item-name">{{ item.productName }}</div>
            <div class="cart-item-price">
              <span v-if="posStore.currentMember && item.originalPrice !== item.price" class="price-original-sm">
                ¥{{ item.originalPrice.toFixed(2) }}
              </span>
              ¥{{ item.price.toFixed(2) }}
            </div>
          </div>
          <div class="cart-item-actions">
            <n-input-number
              :value="item.quantity"
              :min="1"
              :max="999"
              size="tiny"
              style="width: 100px"
              @update:value="(val: number | null) => handleQuantityChange(item.id, val)"
            />
            <n-button text type="error" size="tiny" @click="handleRemoveItem(item.id)">
              <template #icon><n-icon :component="CloseOutline" /></template>
            </n-button>
          </div>
          <div class="cart-item-subtotal">
            ¥{{ (item.price * item.quantity).toFixed(2) }}
          </div>
        </div>
      </div>

      <!-- 优惠摘要 -->
      <div v-if="posStore.currentMember && posStore.totalDiscount > 0" class="discount-summary">
        <n-space justify="space-between" align="center">
          <n-text depth="3" style="font-size: 12px">
            <n-icon :component="CartOutline" size="14" style="margin-right: 2px" />
            会员优惠
          </n-text>
          <n-text type="success" style="font-size: 14px">
            -¥{{ posStore.totalDiscount.toFixed(2) }}
          </n-text>
        </n-space>
      </div>

      <!-- 合计 -->
      <div class="cart-summary">
        <!-- 有会员优惠时显示原价 -->
        <div v-if="posStore.currentMember && posStore.totalDiscount > 0" class="summary-row">
          <n-text depth="3" style="font-size: 13px">原价</n-text>
          <n-text delete depth="3" style="font-size: 13px">¥{{ posStore.totalAmount.toFixed(2) }}</n-text>
        </div>
        <div v-if="posStore.currentMember && posStore.totalDiscount > 0" class="summary-row">
          <n-text type="success" style="font-size: 13px">会员优惠</n-text>
          <n-text type="success" style="font-size: 13px">-¥{{ posStore.totalDiscount.toFixed(2) }}</n-text>
        </div>
        <n-divider style="margin: 8px 0" />
        <n-space justify="space-between" align="center">
          <n-text strong>应付</n-text>
          <n-text strong style="font-size: 24px; color: #d03050">
            ¥{{ posStore.finalAmount.toFixed(2) }}
          </n-text>
        </n-space>
        <n-space v-if="posStore.currentMember" justify="end" style="margin-top: 4px">
          <n-text depth="3" style="font-size: 12px">
            预计获得 <strong style="color: #f0a020">{{ posStore.estimatedPoints }}</strong> 积分
          </n-text>
        </n-space>
      </div>

      <!-- 结算按钮 -->
      <div class="cart-footer">
        <n-button
          type="primary"
          block
          size="large"
          :disabled="posStore.cartItems.length === 0"
          @click="handleCheckout"
        >
          结 算 ({{ posStore.totalCount }} 件)
        </n-button>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NInput, NIcon, NButton, NSpace, NText, NInputNumber, NEmpty, NTag,
  useMessage
} from 'naive-ui'
import {
  SearchOutline, AddOutline, CloseOutline, BagOutline, CartOutline,
  PersonOutline
} from '@vicons/ionicons5'
import { posSearchProduct, posGetMemberByPhone, posCheckout, posGetCategoryTree } from '@/api/pos'
import type { PosProduct } from '@/api/pos'
import { usePosStore } from '@/stores/pos'

const router = useRouter()
const posStore = usePosStore()
const message = useMessage()

const searchKeyword = ref('')
const selectedCategory = ref<number | null>(null)
const selectedSubCategory = ref<number | null>(null)
const products = ref<PosProduct[]>([])
const allCategories = ref<any[]>([])
const memberPhone = ref('')
const memberSearching = ref(false)

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

const categories = computed(() => {
  return allCategories.value.map((cat: any) => ({ id: cat.id, name: cat.name, children: cat.children || [] }))
})

const subCategories = computed(() => {
  if (!selectedCategory.value) return []
  const cat = allCategories.value.find((c: any) => c.id === selectedCategory.value)
  return cat?.children || []
})

const currentCategoryName = computed(() => {
  const cat = allCategories.value.find((c: any) => c.id === selectedCategory.value)
  return cat?.name || ''
})

const filteredProducts = computed(() => {
  let list = products.value
  if (selectedSubCategory.value) {
    list = list.filter((p: PosProduct) => p.categoryId === selectedSubCategory.value)
  } else if (selectedCategory.value) {
    const cat = allCategories.value.find((c: any) => c.id === selectedCategory.value)
    const childIds = (cat?.children || []).map((c: any) => c.id)
    list = list.filter((p: PosProduct) => childIds.includes(p.categoryId))
  }
  return list
})

async function loadProducts(keyword: string = '') {
  const res = await posSearchProduct(keyword)
  products.value = res
}

async function loadCategories() {
  const tree = await posGetCategoryTree()
  allCategories.value = tree
}

function handleSearch() {
  loadProducts(searchKeyword.value)
}

function handleCategoryFilter(categoryId: number | null) {
  selectedCategory.value = categoryId
  selectedSubCategory.value = null
}

function handleSubCategoryFilter(subId: number | null) {
  selectedSubCategory.value = subId
}

function handleAddToCart(product: PosProduct) {
  if ((product.shelfQuantity ?? 0) <= 0) {
    message.warning(`${product.name} 货架无库存，无法添加`)
    return
  }
  if ((product.shelfQuantity ?? 0) < 10) {
    message.warning(`${product.name} 货架库存不足 (${product.shelfQuantity})，请及时从仓库补货`)
  }
  const price = posStore.currentMember ? (product.vipPrice ?? product.salePrice) : product.salePrice
  posStore.addItem({
    productId: String(product.id),
    productName: product.name,
    barcode: product.barcode,
    price,
    originalPrice: product.salePrice,
    memberPrice: product.vipPrice ?? product.salePrice,
    quantity: 1,
    unit: product.unit
  })
  message.success(`已添加 ${product.name}`)
}

function handleQuantityChange(id: string, quantity: number | null) {
  posStore.updateQuantity(id, quantity ?? 1)
}

function handleRemoveItem(id: string) {
  posStore.removeItem(id)
}

function handleClearCart() {
  posStore.clearCart()
}

async function handleSearchMember() {
  const phone = memberPhone.value.trim()
  if (!phone) {
    message.warning('请输入手机号')
    return
  }
  if (!/^1\d{10}$/.test(phone)) {
    message.warning('手机号格式不正确')
    return
  }

  memberSearching.value = true
  try {
    const m = await posGetMemberByPhone(phone)
    if (m && m.id) {
      posStore.setMember({
        id: String(m.id),
        name: m.name,
        phone: m.phone,
        level: m.levelName,
        levelName: m.levelName,
        discount: m.discount ?? 1,
        points: m.points ?? 0,
        balance: m.balance ?? 0,
        totalSpent: m.totalConsume ?? 0
      })
      message.success(`会员 ${m.name} (${m.levelName}) 已识别`)
      memberPhone.value = ''
    } else {
      message.warning('未找到该会员')
    }
  } catch {
    message.error('查询失败')
  } finally {
    memberSearching.value = false
  }
}

function handleClearMember() {
  posStore.setMember(null)
  message.info('已取消会员')
}

async function handleCheckout() {
  if (posStore.cartItems.length === 0) return
  router.push('/pos/payment')
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.cashier {
  display: flex;
  height: 100%;
  overflow: hidden;
}

.cashier-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  overflow: hidden;
}

.product-search {
  margin-bottom: 16px;
}

.product-grid {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  align-content: start;
  padding-bottom: 16px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-img {
  width: 100%;
  height: 80px;
  background: #f5f5f5;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.product-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-spec {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.product-stock {
  font-size: 11px;
  margin-top: 3px;
}

.product-price {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-main {
  font-size: 16px;
  font-weight: 700;
  color: #d03050;
}

.price-original {
  font-size: 12px;
  font-weight: 400;
  color: #ccc;
  text-decoration: line-through;
}

.price-member {
  font-size: 14px;
  font-weight: 700;
  color: #f0a020;
}

.add-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}

.cashier-right {
  width: 380px;
  background: #fff;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #eee;
}

/* 会员区域 */
.member-section {
  padding: 12px 16px;
  background: #fafff8;
  border-bottom: 1px solid #e8f5e0;
}

.member-search {
  display: flex;
  gap: 8px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8f5e0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.member-detail {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.member-meta {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
  display: flex;
  gap: 10px;
}

/* 购物车 */
.cart-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.cart-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.cart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 160px;
  gap: 12px;
}

.cart-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.cart-item-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.cart-item-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 8px;
}

.cart-item-price {
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
}

.price-original-sm {
  font-size: 11px;
  color: #ccc;
  text-decoration: line-through;
  margin-right: 4px;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-item-subtotal {
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #d03050;
  margin-top: 4px;
}

/* 优惠摘要 */
.discount-summary {
  padding: 8px 16px;
  background: #fffbe6;
  border-top: 1px solid #fff1b8;
}

/* 合计 */
.cart-summary {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.cart-footer {
  padding: 12px 16px;
}
</style>
