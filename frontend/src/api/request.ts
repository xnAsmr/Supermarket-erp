import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { getToken, removeToken } from '../utils/storage'
import type { Result } from '../types'

const instance: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

instance.interceptors.response.use(
  response => {
    const res = response.data as Result<any>
    if (res.code !== 200) {
      const error = new Error(res.message || '请求失败')
      return Promise.reject(error)
    }
    return res.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        removeToken()
        window.location.href = '/login'
      }
      if (data && data.message) {
        return Promise.reject(new Error(data.message))
      }
    }
    return Promise.reject(error)
  }
)

export async function get<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.get(url, { params, ...config }) as any
}

export async function post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.post(url, data, config) as any
}

export async function put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.put(url, data, config) as any
}

export async function del<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<T> {
  return instance.delete(url, { params, ...config }) as any
}

export default instance
