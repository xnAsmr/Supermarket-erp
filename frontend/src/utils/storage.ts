const PREFIX = 'supermarket_erp_'

export function getItem<T>(key: string, defaultValue?: T): T | undefined {
  try {
    const value = localStorage.getItem(PREFIX + key)
    if (value === null) return defaultValue
    return JSON.parse(value) as T
  } catch {
    return defaultValue
  }
}

export function setItem<T>(key: string, value: T): void {
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify(value))
  } catch (e) {
    console.error('Failed to save to localStorage:', e)
  }
}

export function removeItem(key: string): void {
  localStorage.removeItem(PREFIX + key)
}

export function clear(): void {
  const keys = Object.keys(localStorage).filter(k => k.startsWith(PREFIX))
  keys.forEach(k => localStorage.removeItem(k))
}

export function getToken(): string | null {
  return getItem<string>('token') || null
}

export function setToken(token: string): void {
  setItem('token', token)
}

export function removeToken(): void {
  removeItem('token')
}

export function getRefreshToken(): string | null {
  return getItem<string>('refreshToken') || null
}

export function setRefreshToken(token: string): void {
  setItem('refreshToken', token)
}

export function removeRefreshToken(): void {
  removeItem('refreshToken')
}

export function getUser<T>(): T | null {
  return getItem<T>('user') || null
}

export function setUser<T>(user: T): void {
  setItem('user', user)
}

export function removeUser(): void {
  removeItem('user')
}

export function getTheme(): string {
  return getItem<string>('theme', 'light') || 'light'
}

export function setTheme(theme: string): void {
  setItem('theme', theme)
}

export function getLanguage(): string {
  return getItem<string>('language', 'zh-CN') || 'zh-CN'
}

export function setLanguage(lang: string): void {
  setItem('language', lang)
}
