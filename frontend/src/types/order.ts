export interface Order {
  id: string
  orderNo: string
  type: OrderType
  status: OrderStatus
  memberId: string
  memberName: string
  memberPhone: string
  cashierId: string
  cashierName: string
  items: OrderItem[]
  totalAmount: number
  discountAmount: number
  payAmount: number
  payMethod: PayMethod
  payTime: string
  remark: string
  createdAt: string
  updatedAt: string
}

export interface OrderItem {
  id: string
  productId: string
  productCode: string
  productName: string
  barcode: string
  unit: string
  price: number
  quantity: number
  amount: number
  discount: number
  payAmount: number
}

export type OrderType = 'sale' | 'return' | 'exchange'

export type OrderStatus = 'pending' | 'paid' | 'completed' | 'cancelled' | 'refunded'

export type PayMethod = 'cash' | 'wechat' | 'alipay' | 'card' | 'mixed'

export interface OrderSearchParams {
  keyword?: string
  type?: OrderType
  status?: OrderStatus
  payMethod?: PayMethod
  startDate?: string
  endDate?: string
  cashierId?: string
  memberId?: string
  minAmount?: number
  maxAmount?: number
  page: number
  pageSize: number
}

export interface OrderStatistics {
  todaySales: number
  todayOrders: number
  todayProfit: number
  monthSales: number
  monthOrders: number
  monthProfit: number
  averageOrderAmount: number
}
