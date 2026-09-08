export interface StockRecord {
  id: string
  recordNo: string
  type: StockType
  productId: string
  productCode: string
  productName: string
  barcode: string
  quantity: number
  beforeStock: number
  afterStock: number
  unit: string
  price: number
  totalAmount: number
  supplierId: string
  supplierName: string
  warehouseId: string
  warehouseName: string
  operatorId: string
  operatorName: string
  remark: string
  createdAt: string
}

export type StockType = 'in' | 'out' | 'adjust' | 'return' | 'transfer'

export interface Warehouse {
  id: string
  name: string
  code: string
  address: string
  manager: string
  phone: string
  status: number
  remark: string
}

export interface StockCheck {
  id: string
  checkNo: string
  warehouseId: string
  warehouseName: string
  status: 'pending' | 'in_progress' | 'completed' | 'cancelled'
  items: StockCheckItem[]
  total差异: number
  operatorId: string
  operatorName: string
  remark: string
  createdAt: string
  completedAt: string
}

export interface StockCheckItem {
  id: string
  productId: string
  productCode: string
  productName: string
  unit: string
  systemStock: number
  actualStock: number
  difference: number
  remark: string
}

export interface StockSearchParams {
  keyword?: string
  type?: StockType
  productId?: string
  warehouseId?: string
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}

export interface StockAlert {
  id: string
  productId: string
  productCode: string
  productName: string
  currentStock: number
  minStock: number
  maxStock: number
  alertType: 'low' | 'high' | 'expiring'
  createdAt: string
}
