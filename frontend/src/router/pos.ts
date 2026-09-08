import type { RouteRecordRaw } from 'vue-router'

const posRoutes: RouteRecordRaw[] = [
  {
    path: 'cashier',
    name: 'PosCashier',
    component: () => import('../views/pos/Cashier.vue'),
    meta: { title: '收银台' }
  },
  {
    path: 'payment',
    name: 'PosPayment',
    component: () => import('../views/pos/Payment.vue'),
    meta: { title: '支付' }
  },
  {
    path: 'orders',
    name: 'PosOrderList',
    component: () => import('../views/pos/OrderList.vue'),
    meta: { title: '今日订单' }
  }
]

export default posRoutes
