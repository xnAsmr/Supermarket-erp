import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('../views/admin/dashboard/Dashboard.vue'),
    meta: { title: '经营看板', icon: 'Analytics' }
  },
  {
    path: 'product',
    name: 'Product',
    redirect: '/admin/product/list',
    meta: { title: '商品管理', icon: 'Cube' },
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('../views/admin/product/ProductList.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'add',
        name: 'ProductAdd',
        component: () => import('../views/admin/product/ProductForm.vue'),
        meta: { title: '新增商品' }
      },
      {
        path: 'brand',
        name: 'BrandList',
        component: () => import('../views/admin/brand/BrandList.vue'),
        meta: { title: '品牌列表' }
      },
      {
        path: 'edit/:id',
        name: 'ProductEdit',
        component: () => import('../views/admin/product/ProductForm.vue'),
        meta: { title: '编辑商品', hidden: true }
      }
    ]
  },
  {
    path: 'supplier',
    name: 'Supplier',
    redirect: '/admin/supplier/list',
    meta: { title: '供应商管理', icon: 'Business' },
    children: [
      {
        path: 'list',
        name: 'SupplierList',
        component: () => import('../views/admin/supplier/SupplierList.vue'),
        meta: { title: '供应商列表' }
      }
    ]
  },
  {
    path: 'stock',
    name: 'Stock',
    redirect: '/admin/stock/list',
    meta: { title: '库存管理', icon: 'Layers' },
    children: [
      {
        path: 'list',
        name: 'StockList',
        component: () => import('../views/admin/stock/StockList.vue'),
        meta: { title: '库存列表' }
      },
      {
        path: 'inbound',
        name: 'StockInbound',
        component: () => import('../views/admin/stock/StockInbound.vue'),
        meta: { title: '入库管理' }
      },
      {
        path: 'outbound',
        name: 'StockOutbound',
        component: () => import('../views/admin/stock/StockOutbound.vue'),
        meta: { title: '出库管理' }
      },
      {
        path: 'check',
        name: 'StockCheck',
        component: () => import('../views/admin/stock/StockCheck.vue'),
        meta: { title: '库存盘点' }
      }
    ]
  },
  {
    path: 'order',
    name: 'Order',
    redirect: '/admin/order/list',
    meta: { title: '订单管理', icon: 'DocumentText' },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('../views/admin/order/OrderList.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: 'detail/:id',
        name: 'OrderDetail',
        component: () => import('../views/admin/order/OrderDetail.vue'),
        meta: { title: '订单详情', hidden: true }
      },
      {
        path: 'payment-flow',
        name: 'PaymentFlow',
        component: () => import('../views/admin/order/PaymentFlow.vue'),
        meta: { title: '支付流水' }
      }
    ]
  },
  {
    path: 'member',
    name: 'Member',
    redirect: '/admin/member/list',
    meta: { title: '会员管理', icon: 'People' },
    children: [
      {
        path: 'list',
        name: 'MemberList',
        component: () => import('../views/admin/member/MemberList.vue'),
        meta: { title: '会员列表' }
      },
      {
        path: 'add',
        name: 'MemberAdd',
        component: () => import('../views/admin/member/MemberForm.vue'),
        meta: { title: '新增会员' }
      },
      {
        path: 'edit/:id',
        name: 'MemberEdit',
        component: () => import('../views/admin/member/MemberForm.vue'),
        meta: { title: '编辑会员', hidden: true }
      }
    ]
  },
  {
    path: 'report',
    name: 'Report',
    redirect: '/admin/report/sales',
    meta: { title: '报表统计', icon: 'BarChart' },
    children: [
      {
        path: 'sales',
        name: 'ReportSales',
        component: () => import('../views/admin/report/SalesReport.vue'),
        meta: { title: '销售报表' }
      },
      {
        path: 'stock',
        name: 'ReportStock',
        component: () => import('../views/admin/report/ReportStock.vue'),
        meta: { title: '库存报表' }
      },
      {
        path: 'member',
        name: 'ReportMember',
        component: () => import('../views/admin/report/ReportMember.vue'),
        meta: { title: '会员报表' }
      }
    ]
  },
  {
    path: 'system',
    name: 'System',
    redirect: '/admin/system/settings',
    meta: { title: '系统管理', icon: 'Settings' },
    children: [
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('../views/admin/system/SystemSettings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'users',
        name: 'SystemUsers',
        component: () => import('../views/admin/system/SystemUsers.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'logs',
        name: 'SystemLogs',
        component: () => import('../views/admin/system/SystemLogs.vue'),
        meta: { title: '操作日志' }
      }
    ]
  }
]

export default adminRoutes
