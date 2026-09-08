import { get, post, put, del } from './request'

export interface PaymentMethod {
  id: number
  name: string
  code: string
  icon: string | null
  sort: number
  status: number
  balance: number
  remark: string | null
  createTime: string
}

export async function getPaymentMethods(): Promise<PaymentMethod[]> {
  return get<PaymentMethod[]>('/payment-methods')
}

export async function createPaymentMethod(data: Partial<PaymentMethod>): Promise<number> {
  return post<number>('/payment-methods', data)
}

export async function updatePaymentMethod(id: number, data: Partial<PaymentMethod>): Promise<void> {
  return put<void>(`/payment-methods/${id}`, data)
}

export async function deletePaymentMethod(id: number): Promise<void> {
  return del<void>(`/payment-methods/${id}`)
}

export async function rechargePaymentMethod(id: number, amount: number, remark: string): Promise<void> {
  return put<void>(`/payment-methods/${id}/recharge`, { amount, remark })
}
