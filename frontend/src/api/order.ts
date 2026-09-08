import { get, del, put } from './request'
import type { PageResult } from '../types'

export interface BackendOrderVO {
  id: number
  orderNo: string
  orderType: number
  memberId: number | null
  memberName: string | null
  totalAmount: number
  discountAmount: number
  payAmount: number
  profitAmount: number
  payMethod: string | null
  payMethodName: string | null
  payStatus: number
  orderStatus: number
  totalQuantity: number
  cashierName: string | null
  payTime: string | null
  createTime: string
  remark: string | null
  items: BackendOrderItemVO[]
}

export interface BackendOrderItemVO {
  id: number
  productId: number
  productName: string
  barcode: string
  spec: string | null
  unit: string
  quantity: number
  salePrice: number
  discountPrice: number | null
  totalPrice: number
  costAmount: number
  profitAmount: number
  isWeight: number
}

export interface OrderQueryParams {
  keyword?: string
  orderStatus?: number
  payStatus?: number
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

export interface PaymentFlowVO {
  payDate: string
  paymentMethod: string
  paymentMethodName: string
  totalAmount: number
  receivedAmount: number
  transactionCount: number
}

export interface PaymentFlowQuery {
  startDate?: string
  endDate?: string
  storeId?: number
  paymentMethod?: string
}

export interface PaymentMethodStatVO {
  paymentMethod: string
  paymentMethodName: string
  totalInflow: number
  totalOutflow: number
  balance: number
  totalInCount: number
  totalOutCount: number
}

export interface DailyPaymentFlowVO {
  payDate: string
  paymentMethod: string
  paymentMethodName: string
  inflow: number
  outflow: number
  inflowCount: number
  outflowCount: number
}

export async function getOrders(params: OrderQueryParams): Promise<PageResult<BackendOrderVO>> {
  return get<PageResult<BackendOrderVO>>('/orders', params)
}

export async function getOrder(id: number): Promise<BackendOrderVO> {
  return get<BackendOrderVO>(`/orders/${id}`)
}

export async function getPaymentFlow(params: PaymentFlowQuery): Promise<PaymentFlowVO[]> {
  return get<PaymentFlowVO[]>('/orders/payment-flow', params)
}

export async function getPaymentMethodStats(): Promise<PaymentMethodStatVO[]> {
  return get<PaymentMethodStatVO[]>('/orders/payment-method-stats')
}

export async function getDailyPaymentFlow(params: PaymentFlowQuery): Promise<DailyPaymentFlowVO[]> {
  return get<DailyPaymentFlowVO[]>('/orders/daily-payment-flow', params)
}

export async function getDailySettlement(date: string): Promise<DailyPaymentFlowVO[]> {
  return get<DailyPaymentFlowVO[]>('/orders/daily-settlement', { date })
}

export async function cancelOrder(id: number): Promise<void> {
  return put<void>(`/orders/${id}/cancel`)
}

export async function completeOrder(id: number): Promise<void> {
  return put<void>(`/orders/${id}/complete`)
}

export async function refundOrder(id: number): Promise<void> {
  return put<void>(`/orders/${id}/refund`)
}

export async function deleteOrder(id: number): Promise<void> {
  return del<void>(`/orders/${id}`)
}

export async function updateOrderRemark(id: number, remark: string): Promise<void> {
  return put<void>(`/orders/${id}/remark`, { remark })
}
