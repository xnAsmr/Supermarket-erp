import { get, post, put, del } from './request'
import type { PageResult } from '../types'

export interface BackendMember {
  id: number
  memberNo: string
  name: string
  phone: string
  gender: number | null
  birthday: string | null
  levelId: number
  levelName: string
  points: number
  balance: number
  totalConsume: number
  totalPoints: number
  email: string | null
  address: string | null
  status: number
  registerTime: string | null
  createTime: string
}

export interface MemberQueryParams {
  keyword?: string
  levelId?: number
  status?: number
  page?: number
  pageSize?: number
}

export interface MemberRechargeLog {
  id: number
  memberId: number
  memberName: string
  type: number
  amount: number
  giftAmount: number
  beforeBalance: number
  afterBalance: number
  payMethod: number | null
  operatorName: string | null
  remark: string | null
  createTime: string
}

export async function getMembers(params: MemberQueryParams): Promise<PageResult<BackendMember>> {
  return get<PageResult<BackendMember>>('/members', params)
}

export async function getMember(id: number): Promise<BackendMember> {
  return get<BackendMember>(`/members/${id}`)
}

export async function createMember(data: any): Promise<BackendMember> {
  return post<BackendMember>('/members', data)
}

export async function updateMember(id: number, data: any): Promise<BackendMember> {
  return put<BackendMember>(`/members/${id}`, data)
}

export async function deleteMember(id: number): Promise<void> {
  return del<void>(`/members/${id}`)
}

export async function rechargeMember(id: number, amount: number, giftAmount: number, payMethod: string, remark: string): Promise<void> {
  return put<void>(`/members/${id}/recharge`, { amount, giftAmount, payMethod, remark })
}

export async function getMemberRechargeLog(id: number, page: number = 1, pageSize: number = 20): Promise<PageResult<MemberRechargeLog>> {
  return get<PageResult<MemberRechargeLog>>(`/members/${id}/recharge-log`, { page, pageSize })
}

export interface MemberConsumeLog {
  id: number
  orderNo: string
  amount: number
  payAmount: number
  payMethod: string
  payMethodName: string
  orderStatus: number
  orderStatusName: string
  orderTime: string
  storeName: string
}

export async function getMemberConsumeLog(id: number, page: number = 1, pageSize: number = 20): Promise<PageResult<MemberConsumeLog>> {
  return get<PageResult<MemberConsumeLog>>(`/members/${id}/consume-log`, { page, pageSize })
}

export async function getMemberByPhone(phone: string): Promise<BackendMember> {
  return get<BackendMember>(`/members/by-phone/${phone}`)
}
