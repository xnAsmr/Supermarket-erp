export function formatMoney(amount: number | null | undefined, symbol: string = '¥'): string {
  if (amount == null || isNaN(amount)) return `${symbol}0.00`
  const fixed = amount.toFixed(2)
  const parts = fixed.split('.')
  const intPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return `${symbol}${intPart}.${parts[1]}`
}

export function parseMoney(str: string): number {
  const num = parseFloat(str.replace(/[^0-9.-]/g, ''))
  return isNaN(num) ? 0 : num
}

export function formatDate(date: string | Date, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  const d = typeof date === 'string' ? new Date(date) : date
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

export function formatDateShort(date: string | Date): string {
  return formatDate(date, 'YYYY-MM-DD')
}

export function formatDateTime(date: string | Date): string {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

export function formatRelativeTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  return formatDate(d, 'YYYY-MM-DD')
}

export function formatPhone(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

export function formatCardNo(cardNo: string): string {
  if (!cardNo) return ''
  return cardNo.replace(/(.{4})/g, '$1 ').trim()
}

export function formatStock(quantity: number, unit: string = '件'): string {
  return `${quantity}${unit}`
}

export function formatPercent(value: number, decimals: number = 2): string {
  return `${(value * 100).toFixed(decimals)}%`
}
