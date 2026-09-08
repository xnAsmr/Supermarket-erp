import { get, post, put, del } from './request'
import type { PageResult } from '../types'
import axios from 'axios'
import { getToken } from '../utils/storage'

export interface BackendProduct {
  id: number
  productNo: string
  barcode: string
  name: string
  shortName: string | null
  categoryId: number
  categoryName: string
  brandId: number | null
  brandName: string | null
  supplierId: number | null
  supplierName: string | null
  spec: string | null
  unit: string
  purchasePrice: number
  salePrice: number
  vipPrice: number | null
  weight: number | null
  image: string | null
  status: number
  isWeight: number
  isPromotion: number
  saleCount: number | null
  createTime: string
}

export interface BackendCategory {
  id: number
  name: string
  code: string
  parentId: number
  level: number
  sort: number
  icon: string | null
  status: number
  children?: BackendCategory[]
}

export interface ProductQueryParams {
  keyword?: string
  categoryId?: number
  status?: number
  isPromotion?: number
  page?: number
  pageSize?: number
}

export async function getProducts(params: ProductQueryParams): Promise<PageResult<BackendProduct>> {
  return get<PageResult<BackendProduct>>('/products', params)
}

export async function getProduct(id: number): Promise<BackendProduct> {
  return get<BackendProduct>(`/products/${id}`)
}

export async function createProduct(data: any): Promise<number> {
  return post<number>('/products', data)
}

export async function updateProduct(id: number, data: any): Promise<void> {
  return put<void>(`/products/${id}`, data)
}

export async function deleteProduct(id: number): Promise<void> {
  return del<void>(`/products/${id}`)
}

export async function updateProductStatus(id: number, status: number): Promise<void> {
  return put<void>(`/products/${id}/status`, null, { params: { status } })
}

export async function searchProducts(keyword: string): Promise<BackendProduct[]> {
  return get<BackendProduct[]>('/products/search', { keyword })
}

export async function getHotProducts(limit: number = 20): Promise<BackendProduct[]> {
  return get<BackendProduct[]>('/products/hot', { limit })
}

export async function getCategoryTree(): Promise<BackendCategory[]> {
  return get<BackendCategory[]>('/categories/tree')
}

export async function getCategoryChildren(parentId: number): Promise<BackendCategory[]> {
  return get<BackendCategory[]>('/categories/children', { parentId })
}

export async function createCategory(data: { name: string; code: string; parentId?: number }): Promise<number> {
  return post<number>('/categories', null, { params: data })
}

export async function updateCategory(id: number, data: { name: string; code: string }): Promise<void> {
  return put<void>(`/categories/${id}`, null, { params: data })
}

export async function deleteCategory(id: number): Promise<void> {
  return del<void>(`/categories/${id}`)
}

export async function uploadImage(file: File, category: string = 'product'): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const token = getToken() || ''
  const res = await axios.post(`/api/v1/upload/image/${category}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`
    }
  })
  if (res.data.code !== 200) {
    throw new Error(res.data.message || '上传失败')
  }
  return res.data.data as string
}
