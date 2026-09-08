import { get, post, put, del } from './request'
import type { PageResult } from '../types'

// ============ Tenant Management ============
export interface BackendTenant {
  id: number
  tenantNo: string
  tenantName: string
  contactName: string | null
  contactPhone: string | null
  contactEmail: string | null
  address: string | null
  licenseNo: string | null
  planType: number | null
  maxStores: number | null
  maxUsers: number | null
  expireTime: string | null
  status: number
  adminUserId: number | null
  remark: string | null
  createTime: string
  storeCount: number | null
}

export interface TenantQueryParams {
  page?: number
  pageSize?: number
}

export async function getTenants(params?: TenantQueryParams): Promise<PageResult<BackendTenant>> {
  return get<PageResult<BackendTenant>>('/admin/tenants', params || {})
}

export interface TenantStats {
  totalTenants: number
  activeTenants: number
  totalStores: number
  totalUsers: number
  planTypeDistribution: { planType: number; count: number }[]
  statusDistribution: { status: number; count: number }[]
  monthlyTrend: { month: string; count: number }[]
  expiringTenants: { id: number; tenantName: string; expireTime: string; planType: number }[]
}

export async function getTenantStats(): Promise<TenantStats> {
  return get<TenantStats>('/admin/tenants/stats')
}

export interface CurrentTenantUpdateParams {
  tenantName: string
  contactName: string | null
  contactPhone: string | null
  contactEmail: string | null
  address: string | null
  remark: string | null
}

export async function getCurrentTenant(): Promise<BackendTenant> {
  return get<BackendTenant>('/admin/tenants/current')
}

export async function updateCurrentTenant(data: CurrentTenantUpdateParams): Promise<void> {
  return put<void>('/admin/tenants/current', data)
}

export async function getTenant(id: number): Promise<BackendTenant> {
  return get<BackendTenant>(`/admin/tenants/${id}`)
}

export async function createTenant(data: any): Promise<number> {
  return post<number>('/admin/tenants', data)
}

export async function updateTenant(id: number, data: any): Promise<void> {
  return put<void>(`/admin/tenants/${id}`, data)
}

export async function updateTenantStatus(id: number, status: number): Promise<void> {
  return put<void>(`/admin/tenants/${id}/status`, null, { params: { status } })
}

export async function deleteTenant(id: number): Promise<void> {
  return del<void>(`/admin/tenants/${id}`)
}

// ============ Store Management ============
export interface BackendStore {
  id: number
  storeNo: string
  storeName: string
  tenantId: number
  address: string | null
  contactPhone: string | null
  managerId: number | null
  storeType: number | null
  businessHours: string | null
  logo: string | null
  status: number
  sort: number
  remark: string | null
  createTime: string
}

export interface StoreQueryParams {
  tenantId?: number
  page?: number
  pageSize?: number
}

export async function getStores(params?: StoreQueryParams): Promise<PageResult<BackendStore>> {
  return get<PageResult<BackendStore>>('/admin/stores', params || {})
}

export async function getStore(id: number): Promise<BackendStore> {
  return get<BackendStore>(`/admin/stores/${id}`)
}

export async function createStore(data: any): Promise<number> {
  return post<number>('/admin/stores', data)
}

export async function updateStore(id: number, data: any): Promise<void> {
  return put<void>(`/admin/stores/${id}`, data)
}

export async function updateStoreStatus(id: number, status: number): Promise<void> {
  return put<void>(`/admin/stores/${id}/status`, null, { params: { status } })
}

export async function deleteStore(id: number): Promise<void> {
  return del<void>(`/admin/stores/${id}`)
}
