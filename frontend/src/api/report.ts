import { get } from './request'

export interface DailyReportVO {
  statDate: string
  orderCount: number
  totalAmount: number
  discountAmount: number
  payAmount: number
  costAmount: number
  profitAmount: number
  cashAmount: number
  wechatAmount: number
  alipayAmount: number
  memberCount: number
}

export interface MonthlyReportVO {
  statYear: number
  statMonth: number
  orderCount: number
  totalAmount: number
  discountAmount: number
  payAmount: number
  costAmount: number
  profitAmount: number
}

export interface DashboardVO {
  todaySalesAmount: number
  todayOrderCount: number
  todayMemberCount: number
  monthSalesAmount: number
  monthOrderCount: number
  monthProfitAmount: number
  lowStockCount: number
}

export interface StockCategoryReportVO {
  id: number
  categoryName: string
  productCount: number
  totalStock: number
  totalValue: number
  lowStockCount: number
  monthlyInValue: number
  monthlyOutValue: number
}

export async function getDailyReport(date: string): Promise<DailyReportVO> {
  return get<DailyReportVO>('/reports/daily', { date })
}

export async function getMonthlyReport(year: number, month: number): Promise<MonthlyReportVO> {
  return get<MonthlyReportVO>('/reports/monthly', { year, month })
}

export async function getDashboard(): Promise<DashboardVO> {
  return get<DashboardVO>('/reports/dashboard')
}

export async function getStockCategoryReport(): Promise<StockCategoryReportVO[]> {
  return get<StockCategoryReportVO[]>('/reports/stock-category')
}

export interface MemberReportVO {
  totalMembers: number
  newMembersThisMonth: number
  activeMembers: number
  totalBalance: number
  totalConsume: number
}

export async function getMemberReport(year: number, month: number): Promise<MemberReportVO> {
  return get<MemberReportVO>('/reports/member', { year, month })
}
