import { get, post, put, del } from './request'
import type { PageResult } from '../types'

export interface BackendStockVO {
  id: number
  tenantId: number
  storeId: number
  storeName: string
  productId: number
  productName: string
  barcode: string
  warehouseQuantity: number
  shelfQuantity: number
  pendingInQuantity: number
  pendingOutQuantity: number
  avgCost: number
}

export interface StockQueryParams {
  storeId?: number
  productId?: number
  categoryId?: number
  stockStatus?: number
  keyword?: string
  page?: number
  pageSize?: number
}

export interface BackendPurchaseInVO {
  id: number
  inNo: string
  supplierId: number
  supplierName: string
  storeId: number
  storeName: string
  totalAmount: number
  totalQuantity: number
  status: number
  operatorName: string | null
  reviewerName: string | null
  reviewTime: string | null
  reviewRemark: string | null
  stockInUserName: string | null
  stockInTime: string | null
  items: {
    id: number
    productId: number
    productName: string
    quantity: number
    purchasePrice: number
    totalPrice: number
    batchNo: string | null
    expireTime: string | null
  }[]
  createTime: string
}

export interface PurchaseInCreateDTO {
  supplierId: number
  storeId: number
  items: {
    productId: number
    quantity: number
    purchasePrice: number
    batchNo?: string
    expireTime?: string
  }[]
}

export async function getStocks(params: StockQueryParams): Promise<PageResult<BackendStockVO>> {
  return get<PageResult<BackendStockVO>>('/stock', params)
}

export interface StockDetailVO {
  productId: number
  productName: string
  barcode: string
  spec: string | null
  unit: string
  storeId: number | null
  storeName: string | null
  warehouseQuantity: number | null
  shelfQuantity: number | null
  pendingInQuantity: number | null
  pendingOutQuantity: number | null
  avgCost: number | null
  logs: {
    id: number
    logType: number
    logTypeName: string
    quantity: number
    beforeStock: number
    afterStock: number
    relatedNo: string | null
    operatorName: string | null
    createTime: string
  }[]
}

export async function getStockDetail(productId: number, storeId?: number): Promise<StockDetailVO> {
  const params: Record<string, any> = { productId }
  if (storeId) params.storeId = storeId
  return get<StockDetailVO>('/stock/detail', params)
}

export async function getPurchaseInList(params: { page?: number; pageSize?: number }): Promise<any> {
  return get<any>('/stock/purchase-in', params)
}

export async function getPurchaseInDetail(id: number): Promise<BackendPurchaseInVO> {
  return get<BackendPurchaseInVO>(`/stock/purchase-in/${id}`)
}

export async function createPurchaseIn(data: PurchaseInCreateDTO): Promise<number> {
  return post<number>('/stock/purchase-in', data)
}

export async function updatePurchaseIn(id: number, data: PurchaseInCreateDTO): Promise<void> {
  return put<void>(`/stock/purchase-in/${id}`, data)
}

export async function deletePurchaseIn(id: number): Promise<void> {
  return del<void>(`/stock/purchase-in/${id}`)
}

export async function reviewPurchaseIn(id: number): Promise<void> {
  return put<void>(`/stock/purchase-in/${id}/review`)
}

export async function revokeReviewPurchaseIn(id: number): Promise<void> {
  return put<void>(`/stock/purchase-in/${id}/revoke-review`)
}

export async function stockInPurchaseIn(id: number): Promise<void> {
  return put<void>(`/stock/purchase-in/${id}/stock-in`)
}

export async function rollbackStockInPurchaseIn(id: number): Promise<void> {
  return put<void>(`/stock/purchase-in/${id}/rollback-stock-in`)
}

export async function createStockCheck(storeId: number): Promise<number> {
  return post<number>('/stock/check', null, { params: { storeId } })
}

export async function completeStockCheck(id: number): Promise<void> {
  return put<void>(`/stock/check/${id}/complete`)
}

export async function adjustStock(storeId: number, productId: number, quantity: number, type: 'IN' | 'OUT'): Promise<void> {
  return post<void>('/stock/adjust', null, { params: { storeId, productId, quantity, type } })
}

export interface BackendStockOutVO {
  id: number
  outNo: string
  outType: number
  storeId: number
  storeName: string
  targetStoreId: number | null
  targetStoreName: string | null
  totalQuantity: number
  status: number
  operatorName: string | null
  reviewerName: string | null
  reviewTime: string | null
  stockOutUserName: string | null
  stockOutTime: string | null
  remark: string | null
  items: {
    id: number
    productId: number
    productName: string
    quantity: number
    remark: string | null
  }[]
  createTime: string
}

export interface StockOutCreateDTO {
  outType: number
  storeId: number
  targetStoreId?: number
  remark?: string
  items: {
    productId: number
    quantity: number
    remark?: string
  }[]
}

export async function getStockOutList(params: { page?: number; pageSize?: number }): Promise<PageResult<BackendStockOutVO>> {
  return get<PageResult<BackendStockOutVO>>('/stock-out', params)
}

export async function getStockOutDetail(id: number): Promise<BackendStockOutVO> {
  return get<BackendStockOutVO>(`/stock-out/${id}`)
}

export async function createStockOut(data: StockOutCreateDTO): Promise<number> {
  return post<number>('/stock-out', data)
}

export async function updateStockOut(id: number, data: StockOutCreateDTO): Promise<void> {
  return put<void>(`/stock-out/${id}`, data)
}

export async function deleteStockOut(id: number): Promise<void> {
  return del<void>(`/stock-out/${id}`)
}

export async function confirmStockOut(id: number): Promise<void> {
  return put<void>(`/stock-out/${id}/confirm`)
}

export async function rollbackStockOut(id: number): Promise<void> {
  return put<void>(`/stock-out/${id}/rollback-confirm`)
}
