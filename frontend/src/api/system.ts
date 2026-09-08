import { get, post, put, del } from './request'
import type { PageResult } from '../types'

// ============ User Management ============
export interface BackendUser {
  id: number
  username: string
  name: string
  phone: string | null
  email: string | null
  avatar: string | null
  tenantId: number | null
  storeId: number | null
  storeName: string | null
  userType: number
  roleId: number | null
  roleName: string | null
  status: number
  lastLoginTime: string | null
  loginCount: number | null
  createTime: string
}

export interface UserQueryParams {
  keyword?: string
  userType?: number
  status?: number
  tenantId?: number
  page?: number
  pageSize?: number
}

export async function getUsers(params: UserQueryParams): Promise<PageResult<BackendUser>> {
  return get<PageResult<BackendUser>>('/system/users', params)
}

export async function getUser(id: number): Promise<BackendUser> {
  return get<BackendUser>(`/system/users/${id}`)
}

export async function createUser(data: any): Promise<number> {
  return post<number>('/system/users', data)
}

export async function updateUser(id: number, data: any): Promise<void> {
  return put<void>(`/system/users/${id}`, data)
}

export async function deleteUser(id: number): Promise<void> {
  return del<void>(`/system/users/${id}`)
}

export async function resetPassword(id: number): Promise<void> {
  return put<void>(`/system/users/${id}/reset-password`)
}

// ============ Profile ============
export interface UserProfile {
  id: number
  username: string
  name: string
  phone: string | null
  email: string | null
  avatar: string | null
  userType: number
  roleName: string | null
  createTime: string
}

export async function getProfile(): Promise<UserProfile> {
  return get<UserProfile>('/system/users/profile')
}

export async function updateProfile(data: { name?: string; phone?: string; email?: string; avatar?: string }): Promise<void> {
  return put<void>('/system/users/profile', data)
}

export async function updatePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
  return put<void>('/system/users/profile/password', data)
}

// ============ Role Management ============
export interface BackendRole {
  id: number
  tenantId: number
  roleName: string
  roleCode: string
  description: string | null
  status: number
  menuIds: number[]
}

export interface RoleQueryParams {
  tenantId?: number
  page?: number
  pageSize?: number
}

export async function getRoles(params?: RoleQueryParams): Promise<PageResult<BackendRole>> {
  return get<PageResult<BackendRole>>('/roles', params || {})
}

export async function getRole(id: number): Promise<BackendRole> {
  return get<BackendRole>(`/roles/${id}`)
}

export async function createRole(data: any): Promise<number> {
  return post<number>('/roles', data)
}

export async function updateRole(id: number, data: any): Promise<void> {
  return put<void>(`/roles/${id}`, data)
}

export async function deleteRole(id: number): Promise<void> {
  return del<void>(`/roles/${id}`)
}

// ============ Menu Management ============
export interface BackendMenu {
  id: number
  parentId: number
  menuName: string
  menuType: number
  path: string | null
  component: string | null
  permission: string | null
  icon: string | null
  sort: number
  visible: number
  status: number
  children?: BackendMenu[]
}

export async function getMenuTree(): Promise<BackendMenu[]> {
  return get<BackendMenu[]>('/menus/tree')
}

export async function getCurrentMenus(): Promise<BackendMenu[]> {
  return get<BackendMenu[]>('/menus/current')
}

export async function getMenuChildren(parentId: number): Promise<BackendMenu[]> {
  return get<BackendMenu[]>('/menus/children', { parentId })
}

export async function createMenu(data: any): Promise<number> {
  return post<number>('/menus', data)
}

export async function updateMenu(id: number, data: any): Promise<void> {
  return put<void>(`/menus/${id}`, data)
}

export async function deleteMenu(id: number): Promise<void> {
  return del<void>(`/menus/${id}`)
}

// ============ Dict Management ============
export interface BackendDict {
  id: number
  dictType: string
  dictCode: string
  dictName: string
  dictValue: string
  sort: number
  status: number
}

export async function getDictsByType(dictType: string): Promise<BackendDict[]> {
  return get<BackendDict[]>(`/dicts/type/${dictType}`)
}

export async function createDict(data: any): Promise<number> {
  return post<number>('/dicts', data)
}

export async function updateDict(id: number, data: any): Promise<void> {
  return put<void>(`/dicts/${id}`, data)
}

export async function deleteDict(id: number): Promise<void> {
  return del<void>(`/dicts/${id}`)
}

// ============ Operation Logs ============
export interface OperationLog {
  id: number
  tenantId: number
  tenantName: string | null
  module: string
  operation: string
  method: string
  url: string
  params: string | null
  result: string | null
  status: number
  errorMsg: string | null
  ip: string | null
  userId: number | null
  username: string | null
  duration: number | null
  createTime: string
}

export interface OperationLogQueryParams {
  module?: string
  username?: string
  status?: number
  tenantId?: number
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

export async function getOperationLogs(params: OperationLogQueryParams): Promise<PageResult<OperationLog>> {
  return get<PageResult<OperationLog>>('/operation-logs', params)
}
