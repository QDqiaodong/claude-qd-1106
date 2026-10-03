import { createRouter, createWebHashHistory } from 'vue-router'

// 注意：main.js 里没有 app.use(router)，路由实例由 App.vue 直接驱动，
// 所以这里全部用静态 import —— <component :is> 拿到的是组件对象本身。
import ProductView from '../views/ProductView.vue'
import CustomerView from '../views/CustomerView.vue'
import SalesOrderView from '../views/SalesOrderView.vue'
import AfterSaleView from '../views/AfterSaleView.vue'

const routes = [
  { path: '/', redirect: '/product' },
  { path: '/product', name: 'product', component: ProductView },
  { path: '/customer', name: 'customer', component: CustomerView },
  { path: '/sales-order', name: 'sales-order', component: SalesOrderView },
  { path: '/after-sale', name: 'after-sale', component: AfterSaleView },
  { path: '/:pathMatch(.*)*', redirect: '/product' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})
