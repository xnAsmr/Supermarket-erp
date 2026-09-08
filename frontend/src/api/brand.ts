import { get, post, put, del } from './request'
import type { PageResult } from '../types'

export interface BackendBrand {
  id: number
  name: string
  logo: string | null
  description: string | null
  status: number
  sort: number
  createTime: string
}

export interface BrandQueryParams {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}

export async function getBrands(params: BrandQueryParams): Promise<PageResult<BackendBrand>> {
  return get<PageResult<BackendBrand>>('/brands', params)
}

export async function getBrand(id: number): Promise<BackendBrand> {
  return get<BackendBrand>(`/brands/${id}`)
}

export async function createBrand(data: any): Promise<number> {
  return post<number>('/brands', data)
}

export async function updateBrand(id: number, data: any): Promise<void> {
  return put<void>(`/brands/${id}`, data)
}

export async function deleteBrand(id: number): Promise<void> {
  return del<void>(`/brands/${id}`)
}
