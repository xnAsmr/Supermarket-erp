import { get, post } from './request'
import type { BackendCategory } from './product'

export interface PosProduct {
  id: number
  name: string
  barcode: string
  spec: string | null
  unit: string
  salePrice: number
  vipPrice: number | null
  image: string | null
  categoryName: string
  categoryId: number
  shelfQuantity: number | null
  warehouseQuantity: number | null
}

export interface CheckoutItem {
  productId: number
  quantity: number
  salePrice: number
  isWeight?: number
}

export interface PaymentDetail {
  paymentMethod: string
  amount: number
  receivedAmount?: number
}

export interface CheckoutDTO {
  storeId: number
  memberId?: number
  items: CheckoutItem[]
  payments: PaymentDetail[]
}

export interface CheckoutVO {
  orderId: number
  orderNo: string
  totalAmount: number
  discountAmount: number
  payAmount: number
  changeAmount: number
  items: {
    id: number
    productId: number
    productName: string
    barcode: string
    spec: string | null
    unit: string
    quantity: number
    salePrice: number
    totalPrice: number
    isWeight: number
  }[]
}

export interface PaymentMethodVO {
  code: number
  name: string
  icon: string
}

export async function posSearchProduct(keyword: string): Promise<PosProduct[]> {
  return get<PosProduct[]>('/products/search', { keyword })
}

export async function posGetHotProducts(limit: number = 20): Promise<PosProduct[]> {
  return get<PosProduct[]>('/products/hot', { limit })
}

export async function posGetCategoryTree(): Promise<BackendCategory[]> {
  return get<BackendCategory[]>('/categories/tree')
}

export async function posGetMemberByPhone(phone: string): Promise<any> {
  return get<any>(`/members/by-phone/${phone}`)
}

export async function posCheckout(data: CheckoutDTO): Promise<CheckoutVO> {
  return post<CheckoutVO>('/pos/checkout', data)
}

export async function posGetPaymentMethods(): Promise<PaymentMethodVO[]> {
  return get<PaymentMethodVO[]>('/payment-methods')
}
