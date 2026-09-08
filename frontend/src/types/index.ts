export type { Product, ProductCategory, ProductBrand, Supplier, ProductSearchParams } from './product'
export type { Order, OrderItem, OrderSearchParams, OrderStatistics, OrderType, OrderStatus, PayMethod } from './order'
export type { Member, MemberLevel, MemberCard, MemberPointsLog, MemberRechargeLog, MemberSearchParams } from './member'
export type { StockRecord, StockType, Warehouse, StockCheck, StockCheckItem, StockSearchParams, StockAlert } from './stock'

export interface Result<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface PageParams {
  page: number
  pageSize: number
  keyword?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  refreshToken: string
  expiresIn: number
  userInfo: UserInfo
}

export interface UserInfo {
  id: string
  username: string
  realName: string
  role: string
  avatar: string
  phone: string
  email: string
  permissions: string[]
}

export interface Option {
  label: string
  value: string | number
  disabled?: boolean
}

export interface SelectOption extends Option {
  icon?: string
  color?: string
}
