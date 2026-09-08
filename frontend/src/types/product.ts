export interface Product {
  id: string
  code: string
  name: string
  barcode: string
  category: string
  categoryId: string
  brand: string
  unit: string
  specification: string
  purchasePrice: number
  sellingPrice: number
  memberPrice: number
  stock: number
  minStock: number
  maxStock: number
  supplier: string
  supplierId: string
  image: string
  status: number
  description: string
  createdAt: string
  updatedAt: string
}

export interface ProductCategory {
  id: string
  name: string
  code: string
  parentId: string
  level: number
  sort: number
  icon: string
  children?: ProductCategory[]
}

export interface ProductBrand {
  id: string
  name: string
  logo: string
  description: string
}

export interface Supplier {
  id: string
  name: string
  code: string
  contact: string
  phone: string
  address: string
  bankAccount: string
  bankName: string
  status: number
  remark: string
  createdAt: string
}

export interface ProductSearchParams {
  keyword?: string
  categoryId?: string
  brandId?: string
  status?: number
  minPrice?: number
  maxPrice?: number
  page: number
  pageSize: number
}
