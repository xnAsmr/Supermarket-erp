import { get, post, put, del } from './request'
import type { PageResult } from '../types'

export interface BackendSupplier {
  id: number
  supplierNo: string
  name: string
  contact: string | null
  phone: string | null
  address: string | null
  bankAccount: string | null
  bankName: string | null
  status: number
  createTime: string
}

export interface SupplierQueryParams {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}

export async function getSuppliers(params: SupplierQueryParams): Promise<PageResult<BackendSupplier>> {
  return get<PageResult<BackendSupplier>>('/suppliers', params)
}

export async function getSupplier(id: number): Promise<BackendSupplier> {
  return get<BackendSupplier>(`/suppliers/${id}`)
}

export async function createSupplier(data: any): Promise<number> {
  return post<number>('/suppliers', data)
}

export async function updateSupplier(id: number, data: any): Promise<void> {
  return put<void>(`/suppliers/${id}`, data)
}

export async function deleteSupplier(id: number): Promise<void> {
  return del<void>(`/suppliers/${id}`)
}
