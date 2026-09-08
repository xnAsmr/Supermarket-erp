import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '../stores/user'

const AdminLayout = () => import('../layouts/AdminLayout.vue')
const PosLayout = () => import('../layouts/PosLayout.vue')
const SystemLayout = () => import('../layouts/SystemLayout.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginPage.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'admin',
        redirect: '/admin/dashboard',
        children: [
          {
            path: 'dashboard',
            name: 'Dashboard',
            component: () => import('../views/admin/dashboard/Dashboard.vue'),
            meta: { title: '经营看板' }
          },
          {
            path: 'product',
            name: 'Product',
            redirect: '/admin/product/list',
            meta: { title: '商品管理' },
            children: [
              {
                path: 'list',
                name: 'ProductList',
                component: () => import('../views/admin/product/ProductList.vue'),
                meta: { title: '商品列表' }
              },
              {
                path: 'category',
                name: 'ProductCategory',
                component: () => import('../views/admin/product/ProductCategory.vue'),
                meta: { title: '商品分类' }
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
                meta: { title: '编辑商品' }
              }
            ]
          },
          {
            path: 'supplier',
            name: 'Supplier',
            redirect: '/admin/supplier/list',
            meta: { title: '供应商管理' },
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
            path: 'store',
            name: 'Store',
            redirect: '/admin/store/list',
            meta: { title: '门店管理' },
            children: [
              {
                path: 'list',
                name: 'StoreList',
                component: () => import('../views/admin/store/StoreList.vue'),
                meta: { title: '门店列表' }
              }
            ]
          },
          {
            path: 'stock',
            name: 'Stock',
            redirect: '/admin/stock/list',
            meta: { title: '库存管理' },
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
              },
              {
                path: 'detail',
                name: 'StockDetail',
                component: () => import('../views/admin/stock/StockDetail.vue'),
                meta: { title: '库存详情' }
              }
            ]
          },
          {
            path: 'order',
            name: 'Order',
            redirect: '/admin/order/list',
            meta: { title: '订单管理' },
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
                meta: { title: '订单详情' }
              },
              {
                path: 'payment-flow',
                name: 'PaymentFlow',
                component: () => import('../views/admin/order/PaymentFlow.vue'),
                meta: { title: '支付流水' }
              },
              {
                path: 'daily-settlement',
                name: 'DailySettlement',
                component: () => import('../views/admin/order/DailySettlement.vue'),
                meta: { title: '财务日结' }
              }
            ]
          },
          {
            path: 'member',
            name: 'Member',
            redirect: '/admin/member/list',
            meta: { title: '会员管理' },
            children: [
              {
                path: 'list',
                name: 'MemberList',
                component: () => import('../views/admin/member/MemberList.vue'),
                meta: { title: '会员列表' }
              },
              {
                path: 'edit/:id',
                name: 'MemberEdit',
                component: () => import('../views/admin/member/MemberForm.vue'),
                meta: { title: '编辑会员' }
              },
              {
                path: 'detail/:id',
                name: 'MemberDetail',
                component: () => import('../views/admin/member/MemberDetail.vue'),
                meta: { title: '会员详情' }
              }
            ]
          },
          {
            path: 'report',
            name: 'Report',
            redirect: '/admin/report/sales',
            meta: { title: '报表统计' },
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
            meta: { title: '系统管理' },
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
                path: 'roles',
                name: 'SystemRoles',
                component: () => import('../views/admin/system/RoleList.vue'),
                meta: { title: '角色管理' }
              },
              {
                path: 'logs',
                name: 'SystemLogs',
                component: () => import('../views/admin/system/SystemLogs.vue'),
                meta: { title: '操作日志' }
              }
            ]
          },
          {
            path: 'profile',
            name: 'Profile',
            component: () => import('../views/admin/profile/Profile.vue'),
            meta: { title: '个人中心' }
          }
        ]
      }
    ]
  },
  {
    path: '/system',
    component: SystemLayout,
    redirect: '/system/dashboard',
    meta: { title: '系统管理后台', requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'SystemDashboard',
        component: () => import('../views/system/SystemDashboard.vue'),
        meta: { title: '系统概览' }
      },
      {
        path: 'tenant',
        name: 'Tenant',
        redirect: '/system/tenant/list',
        meta: { title: '租户管理' },
        children: [
          {
            path: 'list',
            name: 'TenantList',
            component: () => import('../views/system/tenant/TenantList.vue'),
            meta: { title: '租户列表' }
          },
          {
            path: 'add',
            name: 'TenantAdd',
            component: () => import('../views/system/tenant/TenantForm.vue'),
            meta: { title: '新增租户' }
          },
          {
            path: 'edit/:id',
            name: 'TenantEdit',
            component: () => import('../views/system/tenant/TenantForm.vue'),
            meta: { title: '编辑租户' }
          },
          {
            path: 'detail/:id',
            name: 'TenantDetail',
            component: () => import('../views/system/tenant/TenantDetail.vue'),
            meta: { title: '租户详情' }
          }
        ]
      },
      {
        path: 'store',
        name: 'SystemStore',
        redirect: '/system/store/list',
        meta: { title: '门店管理' },
        children: [
          {
            path: 'list',
            name: 'SystemStoreList',
            component: () => import('../views/system/store/StoreList.vue'),
            meta: { title: '门店列表' }
          },
          {
            path: 'add',
            name: 'StoreAdd',
            component: () => import('../views/system/store/StoreForm.vue'),
            meta: { title: '新增门店' }
          },
          {
            path: 'edit/:id',
            name: 'StoreEdit',
            component: () => import('../views/system/store/StoreForm.vue'),
            meta: { title: '编辑门店' }
          }
        ]
      },
      {
        path: 'user',
        name: 'SystemUser',
        redirect: '/system/user/list',
        meta: { title: '系统用户' },
        children: [
          {
            path: 'list',
            name: 'SystemUserList',
            component: () => import('../views/system/user/UserList.vue'),
            meta: { title: '用户列表' }
          }
        ]
      },
      {
        path: 'role',
        name: 'SystemRole',
        redirect: '/system/role/list',
        meta: { title: '角色管理' },
        children: [
          {
            path: 'list',
            name: 'SystemRoleList',
            component: () => import('../views/system/role/RoleList.vue'),
            meta: { title: '角色列表' }
          }
        ]
      },
      {
        path: 'menu',
        name: 'SystemMenu',
        redirect: '/system/menu/list',
        meta: { title: '菜单管理' },
        children: [
          {
            path: 'list',
            name: 'SystemMenuList',
            component: () => import('../views/system/menu/MenuList.vue'),
            meta: { title: '菜单列表' }
          }
        ]
      },
      {
        path: 'settings',
        name: 'SystemSettingsManage',
        component: () => import('../views/system/SystemSettings.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'logs',
        name: 'SystemLogsManage',
        component: () => import('../views/system/logs/SystemLogs.vue'),
        meta: { title: '操作日志' }
      }
    ]
  },
  {
    path: '/pos',
    component: PosLayout,
    redirect: '/pos/cashier',
    meta: { title: '收银台' },
    children: [
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
      },
      {
        path: 'orderDetail',
        name: 'PosOrderDetail',
        component: () => import('../views/pos/OrderDetail.vue'),
        meta: { title: '订单详情' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const whiteList = ['/login', '/pos']

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 超市ERP` : '超市ERP'

  const userStore = useUserStore()

  if (userStore.isLoggedIn) {
    if (to.path === '/login') {
      next('/')
    } else {
      next()
    }
  } else {
    if (whiteList.some(path => to.path.startsWith(path))) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
