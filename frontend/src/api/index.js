import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

async function call(promise) {
  const response = await promise
  const body = response.data
  if (!body || body.ok === false) {
    throw new Error((body && body.message) || '请求失败')
  }
  return body.data
}

const params = (o) => ({ params: o })

export default {
  // ---------------- 商品 ----------------
  listProducts: (category, status) => call(http.get('/products', params(
    (category || status) ? { ...(category ? { category } : {}), ...(status ? { status } : {}) } : {}))),
  createProduct: (d) => call(http.post('/products', d)),
  updateProduct: (id, d) => call(http.put(`/products/${id}`, d)),

  // ---------------- 客户 ----------------
  listCustomers: (memberLevel) => call(http.get('/customers', params(memberLevel ? { memberLevel } : {}))),
  createCustomer: (d) => call(http.post('/customers', d)),
  updateCustomer: (id, d) => call(http.put(`/customers/${id}`, d)),

  // ---------------- 开单 ----------------
  listSalesOrders: (status) => call(http.get('/sales-orders', params(status ? { status } : {}))),
  createSalesOrder: (d) => call(http.post('/sales-orders', d)),
  updateSalesOrder: (id, d) => call(http.put(`/sales-orders/${id}`, d)),
  listOrderItems: (orderId) => call(http.get(`/sales-orders/${orderId}/items`)),
  addOrderItem: (orderId, d) => call(http.post(`/sales-orders/${orderId}/items`, d)),
  removeOrderItem: (orderId, itemId) => call(http.delete(`/sales-orders/${orderId}/items/${itemId}`)),
  settleOrder: (id) => call(http.post(`/sales-orders/${id}/settle`)),
  voidOrder: (id) => call(http.post(`/sales-orders/${id}/void`)),

  // ---------------- 售后 ----------------
  listAfterSales: (status) => call(http.get('/after-sales', params(status ? { status } : {}))),
  createAfterSale: (d) => call(http.post('/after-sales', d)),
  processAfterSale: (id) => call(http.post(`/after-sales/${id}/process`))
}
