export interface Member {
  id: string
  code: string
  name: string
  phone: string
  gender: 'male' | 'female' | 'unknown'
  level: MemberLevel
  points: number
  balance: number
  totalSpent: number
  totalPoints: number
  birthday: string
  email: string
  address: string
  avatar: string
  status: number
  remark: string
  createdAt: string
  updatedAt: string
}

export interface MemberLevel {
  id: string
  name: string
  code: string
  minPoints: number
  discount: number
  description: string
}

export interface MemberCard {
  id: string
  memberId: string
  memberCode: string
  cardNo: string
  cardType: 'normal' | 'silver' | 'gold' | 'diamond'
  balance: number
  status: number
  createdAt: string
  expireAt: string
}

export interface MemberPointsLog {
  id: string
  memberId: string
  memberName: string
  type: 'earn' | 'redeem' | 'adjust' | 'expire'
  points: number
  beforePoints: number
  afterPoints: number
  description: string
  relatedOrderNo: string
  operatorId: string
  operatorName: string
  createdAt: string
}

export interface MemberRechargeLog {
  id: string
  memberId: string
  memberName: string
  amount: number
  giftAmount: number
  payMethod: 'cash' | 'wechat' | 'alipay' | 'card'
  operatorId: string
  operatorName: string
  createdAt: string
}

export interface MemberSearchParams {
  keyword?: string
  levelId?: string
  status?: number
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}
