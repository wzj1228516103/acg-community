import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '首页' } },
      { path: 'shop', name: 'Shop', component: () => import('@/views/shop/index.vue'), meta: { title: '商城' } },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/shop/ProductDetail.vue'), meta: { title: '商品详情' } },
      { path: 'makeup-services', name: 'MakeupServices', component: () => import('@/views/makeup/index.vue'), meta: { title: '化妆师服务' } },
      { path: 'makeup-service/:id', name: 'MakeupServiceDetail', component: () => import('@/views/makeup/ServiceDetail.vue'), meta: { title: '服务详情' } },
      { path: 'cart', name: 'Cart', component: () => import('@/views/cart/index.vue'), meta: { title: '购物车', requireAuth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/views/cart/Checkout.vue'), meta: { title: '订单结算', requireAuth: true } },
      { path: 'orders', name: 'Orders', component: () => import('@/views/order/index.vue'), meta: { title: '我的订单', requireAuth: true } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/order/OrderDetail.vue'), meta: { title: '订单详情', requireAuth: true } },
      { path: 'payment-success', name: 'PaymentSuccess', component: () => import('@/views/order/PaymentSuccess.vue'), meta: { title: '支付成功', requireAuth: true } },
      { path: 'chat', name: 'Chat', component: () => import('@/views/chat/index.vue'), meta: { title: '聊天', requireAuth: true } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/user/Favorites.vue'), meta: { title: '我的收藏', requireAuth: true } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/Profile.vue'), meta: { title: '个人中心', requireAuth: true } },
      { path: 'profile-settings', name: 'ProfileSettings', component: () => import('@/views/user/Settings.vue'), meta: { title: '个人设置', requireAuth: true } },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'products', name: 'AdminProducts', component: () => import('@/views/admin/ProductList.vue'), meta: { title: '商品管理' } },
      { path: 'categories', name: 'AdminCategories', component: () => import('@/views/admin/CategoryList.vue'), meta: { title: '分类管理' } },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/OrderList.vue'), meta: { title: '订单管理' } },
      { path: 'makeup-services', name: 'AdminMakeupServices', component: () => import('@/views/admin/MakeupServiceList.vue'), meta: { title: '化妆服务' } },
      { path: 'applications', name: 'AdminApplications', component: () => import('@/views/admin/ApplicationList.vue'), meta: { title: '审核管理' } },
    ],
  },
  { path: '/login', name: 'Login', component: () => import('@/views/user/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/user/Register.vue'), meta: { title: '注册' } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '漫化'} — 漫化二次元社区`
  if (!localStorage.getItem('token')) {
    if (to.meta.requireAuth || to.matched.some(r => r.meta.requireAuth)) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }
  if (to.matched.some(r => r.meta.requireAdmin)) {
    try {
      const user = JSON.parse(localStorage.getItem('user'))
      if (!user || user.role < 3) {
        next({ path: '/' })
        return
      }
    } catch {
      next({ path: '/' })
      return
    }
  }
  next()
})

export default router
