// 租户相关类型定义

// 租户信息
export interface Tenant {
  id: number
  tenantNo: string
  tenantName: string
  contactName: string
  contactPhone: string
  contactEmail?: string
  logo?: string
  address?: string
  licenseNo?: string
  planType: 1 | 2 | 3  // 1-基础版 2-专业版 3-企业版
  maxStores: number
  maxUsers: number
  expireTime: string
  status: 0 | 1 | 2  // 0-禁用 1-正常 2-到期
  adminUserId?: number
  remark?: string
  createTime: string
  updateTime?: string
}

// 门店信息
export interface Store {
  id: number
  storeNo: string
  storeName: string
  tenantId: number
  address?: string
  contactPhone?: string
  managerId?: number
  storeType: 1 | 2  // 1-直营店 2-加盟店
  businessHours?: string
  logo?: string
  status: 0 | 1 | 2 | 3  // 0-装修中 1-营业中 2-暂停营业 3-已关闭
  sort: number
  remark?: string
  createTime: string
  updateTime?: string
}

// 租户配置
export interface TenantConfig {
  id: number
  tenantId: number
  configKey: string
  configValue: string
  configType: 'string' | 'number' | 'boolean' | 'json'
  remark?: string
}

// 租户查询参数
export interface TenantQuery {
  page: number
  pageSize: number
  keyword?: string
  status?: number | null
  planType?: number | null
}

// 门店查询参数
export interface StoreQuery {
  page: number
  pageSize: number
  keyword?: string
  status?: number | null
  tenantId?: number | null
}

// 租户创建DTO
export interface TenantCreateDTO {
  tenantName: string
  contactName: string
  contactPhone: string
  contactEmail?: string
  address?: string
  licenseNo?: string
  planType: number
  maxStores: number
  maxUsers: number
  expireTime: string
  remark?: string
}

// 租户更新DTO
export interface TenantUpdateDTO {
  tenantName?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  address?: string
  licenseNo?: string
  planType?: number
  maxStores?: number
  maxUsers?: number
  expireTime?: string
  status?: number
  remark?: string
}

// 门店创建DTO
export interface StoreCreateDTO {
  storeName: string
  tenantId: number
  address?: string
  contactPhone?: string
  managerId?: number
  storeType: number
  businessHours?: string
  remark?: string
}

// 门店更新DTO
export interface StoreUpdateDTO {
  storeName?: string
  address?: string
  contactPhone?: string
  managerId?: number
  storeType?: number
  businessHours?: string
  status?: number
  remark?: string
}

// 系统统计数据
export interface SystemStats {
  totalTenants: number
  activeTenants: number
  totalStores: number
  activeStores: number
  totalUsers: number
  todayOrders: number
  todayAmount: number
}
