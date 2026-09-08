import { post } from './request'
import type { LoginResult } from '../types'

interface BackendUserInfo {
  id: number
  username: string
  realName: string
  role: string
  avatar: string | null
  phone: string | null
  email: string | null
  permissions?: string[]
}

interface BackendLoginResult {
  token: string
  refreshToken: string
  expiresIn: number
  userInfo: BackendUserInfo
}

export async function login(params: { username: string; password: string }): Promise<LoginResult> {
  const res = await post<BackendLoginResult>('/auth/login', params)
  return {
    token: res.token,
    refreshToken: res.refreshToken,
    expiresIn: res.expiresIn,
    userInfo: {
      id: String(res.userInfo.id),
      username: res.userInfo.username,
      realName: res.userInfo.realName,
      role: res.userInfo.role,
      avatar: res.userInfo.avatar || '',
      phone: res.userInfo.phone || '',
      email: res.userInfo.email || '',
      permissions: res.userInfo.permissions || []
    }
  }
}

export async function logout(): Promise<void> {
  await post('/auth/logout')
}
