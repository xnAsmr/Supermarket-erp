import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  id: string
  productId: string
  productName: string
  barcode: string
  price: number           // 当前售价（可能是会员价）
  originalPrice: number   // 原价（非会员价）
  memberPrice: number     // 会员价
  quantity: number
  unit: string
  subtotal: number
  discount: number
  total: number
}

export interface MemberInfo {
  id: string
  name: string
  phone: string
  level: string
  levelName: string
  discount: number        // 折扣比例，如 0.95
  points: number
  balance: number         // 储值卡余额
  totalSpent: number
}

export const usePosStore = defineStore('pos', () => {
  const cartItems = ref<CartItem[]>([])
  const currentMember = ref<MemberInfo | null>(null)
  const offlineMode = ref(false)
  const offlineOrders = ref<any[]>([])

  const totalCount = computed(() =>
    cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  const totalAmount = computed(() =>
    cartItems.value.reduce((sum, item) => sum + item.originalPrice * item.quantity, 0)
  )

  const totalDiscount = computed(() =>
    cartItems.value.reduce((sum, item) => {
      const itemDiscount = (item.originalPrice - item.price) * item.quantity
      return sum + itemDiscount
    }, 0)
  )

  const finalAmount = computed(() =>
    cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  /** 会员折扣后的优惠金额（不含单品会员价优惠） */
  const memberDiscountAmount = computed(() => {
    if (!currentMember.value) return 0
    return totalDiscount.value
  })

  /** 预估可获得积分（每消费1元=1积分，会员等级加成） */
  const estimatedPoints = computed(() => {
    if (!currentMember.value) return 0
    const basePoints = Math.floor(finalAmount.value)
    // 金卡+20%，钻石+50%
    const levelBonus: Record<string, number> = {
      '普通会员': 1,
      '银卡会员': 1,
      '金卡会员': 1.2,
      '钻石会员': 1.5
    }
    const bonus = levelBonus[currentMember.value.levelName] || 1
    return Math.floor(basePoints * bonus)
  })

  function addItem(product: {
    productId: string
    productName: string
    barcode: string
    price: number
    originalPrice?: number
    memberPrice?: number
    quantity: number
    unit: string
  }) {
    const existing = cartItems.value.find(item => item.productId === product.productId)
    const originalPrice = product.originalPrice ?? product.price
    const memberPrice = product.memberPrice ?? product.price

    if (existing) {
      existing.quantity += product.quantity
      existing.subtotal = existing.price * existing.quantity
      existing.total = existing.subtotal - existing.discount
    } else {
      const subtotal = product.price * product.quantity
      cartItems.value.push({
        id: Date.now().toString() + Math.random().toString(36).slice(2, 6),
        productId: product.productId,
        productName: product.productName,
        barcode: product.barcode,
        price: product.price,
        originalPrice,
        memberPrice,
        quantity: product.quantity,
        unit: product.unit,
        subtotal,
        discount: 0,
        total: subtotal
      })
    }
  }

  function removeItem(id: string) {
    const index = cartItems.value.findIndex(item => item.id === id)
    if (index > -1) {
      cartItems.value.splice(index, 1)
    }
  }

  function updateQuantity(id: string, quantity: number) {
    const item = cartItems.value.find(item => item.id === id)
    if (item) {
      item.quantity = quantity
      item.subtotal = item.price * item.quantity
      item.total = item.subtotal - item.discount
    }
  }

  function clearCart() {
    cartItems.value = []
  }

  /** 设置/切换会员，重新计算购物车中所有商品的价格 */
  function setMember(member: MemberInfo | null) {
    currentMember.value = member
    // 重新计算购物车价格
    cartItems.value.forEach(item => {
      if (member) {
        // 使用会员价
        item.price = item.memberPrice
      } else {
        // 恢复原价
        item.price = item.originalPrice
      }
      item.subtotal = item.price * item.quantity
      item.total = item.subtotal - item.discount
    })
  }

  function setOfflineMode(val: boolean) {
    offlineMode.value = val
  }

  function addOfflineOrder(order: any) {
    offlineOrders.value.push(order)
  }

  function clearOfflineOrders() {
    offlineOrders.value = []
  }

  return {
    cartItems,
    currentMember,
    offlineMode,
    offlineOrders,
    totalCount,
    totalAmount,
    totalDiscount,
    finalAmount,
    memberDiscountAmount,
    estimatedPoints,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    setMember,
    setOfflineMode,
    addOfflineOrder,
    clearOfflineOrders
  }
})
