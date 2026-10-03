<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../api'

const orders = ref([])
const loading = ref(false)
const filterStatus = ref('')
const statuses = ['草稿', '已结算', '已作废']

const products = ref([])
const customers = ref([])

const selectedId = ref(null)
const selected = ref(null)
const items = ref([])
const err = ref('')

// 步骤条
const steps = ['选客', '选型', '确认', '完成']
const activeStep = computed(() => steps.indexOf(selected.value ? selected.value.step : '选客'))

// 当前订单可派发的时间线（按 step/status 推导）
const timeline = computed(() => {
  if (!selected.value) return []
  const o = selected.value
  const t = []
  t.push({ text: '开单创建（草稿）', type: 'primary' })
  if (['选型', '确认', '完成'].includes(o.step)) t.push({ text: '进入「选型」步骤', type: '' })
  if (['确认', '完成'].includes(o.step)) t.push({ text: '进入「确认」步骤', type: '' })
  if (o.status === '已结算') t.push({ text: '结算完成，金额 ¥' + o.total, type: 'success' })
  if (o.status === '已作废') t.push({ text: '订单已作废', type: 'danger' })
  return t
})

// 选客表单
const pickCustomer = ref(null)
// 选型加货表单
const itemForm = reactive({ productId: null, qty: 1 })

const emit = defineEmits(['changed'])

async function loadOrders() {
  loading.value = true
  try {
    orders.value = await api.listSalesOrders(filterStatus.value || undefined)
  } finally { loading.value = false }
}

async function loadSupport() {
  products.value = await api.listProducts(undefined, '在售')
  customers.value = await api.listCustomers()
}

async function selectOrder(row) {
  selectedId.value = row.id
  selected.value = row
  err.value = ''
  await reloadItems(row.id)
}

async function reloadItems(id) {
  items.value = await api.listOrderItems(id)
}

async function refreshSelected() {
  if (!selectedId.value) return
  selected.value = orders.value.find((o) => o.id === selectedId.value) || null
  if (selected.value) await reloadItems(selectedId.value)
}

async function goNextFromPick() {
  err.value = ''
  try {
    if (!pickCustomer.value) throw new Error('请先选择客户')
    await api.updateSalesOrder(selectedId.value, { customerId: pickCustomer.value, step: '选型' })
    await afterAction()
  } catch (e) { err.value = e.message }
}

async function goConfirm() {
  err.value = ''
  try {
    await api.updateSalesOrder(selectedId.value, { step: '确认' })
    await afterAction()
  } catch (e) { err.value = e.message }
}

async function addItem() {
  err.value = ''
  try {
    if (!itemForm.productId) throw new Error('请选择商品')
    await api.addOrderItem(selectedId.value, { productId: itemForm.productId, qty: Number(itemForm.qty) })
    itemForm.productId = null
    itemForm.qty = 1
    await afterAction()
  } catch (e) { err.value = e.message }
}

async function removeItem(itemId) {
  err.value = ''
  try {
    await api.removeOrderItem(selectedId.value, itemId)
    await afterAction()
  } catch (e) { err.value = e.message }
}

async function settle() {
  err.value = ''
  try {
    await api.settleOrder(selectedId.value)
    await afterAction()
  } catch (e) { err.value = e.message }
}

async function voidOrder() {
  err.value = ''
  try {
    await api.voidOrder(selectedId.value)
    await afterAction()
  } catch (e) { err.value = e.message }
}

async function newOrder() {
  err.value = ''
  try {
    const o = await api.createSalesOrder({})
    await loadOrders()
    await selectOrder(o)
  } catch (e) { err.value = e.message }
}

async function afterAction() {
  await loadOrders()
  await refreshSelected()
  emit('changed')
}

function customerName(id) {
  const c = customers.value.find((x) => x.id === id)
  return c ? c.name + '（' + c.memberLevel + '）' : '—'
}
function productName(id) {
  const p = products.value.find((x) => x.id === id)
  return p ? p.name + ' / ¥' + p.price : '商品#' + id
}
function subtotal(p, q) {
  return '¥' + (Number(p) * Number(q)).toFixed(2)
}

onMounted(async () => {
  await Promise.all([loadOrders(), loadSupport()])
  if (orders.value.length) await selectOrder(orders.value[0])
})
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="title">开单 · 步骤条结算</h2>
      <div class="filters">
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width:120px" @change="loadOrders">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="newOrder">新建开单</el-button>
      </div>
    </div>

    <div class="layout">
      <!-- 左：订单列表 + 步骤 + 明细 -->
      <div class="left">
        <el-table :data="orders" v-loading="loading" highlight-current-row border
                  @current-change="selectOrder" style="cursor:pointer">
          <el-table-column prop="id" label="单号" width="70" />
          <el-table-column label="客户" min-width="120">
            <template #default="{ row }">{{ customerName(row.customerId) }}</template>
          </el-table-column>
          <el-table-column prop="step" label="步骤" width="80" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === '已结算' ? 'success' : (row.status === '已作废' ? 'info' : 'warning')" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="total" label="金额" width="100" />
        </el-table>

        <div v-if="selected" class="detail">
          <el-steps :active="activeStep" align-center finish-status="success" class="steps">
            <el-step v-for="s in steps" :key="s" :title="s" />
          </el-steps>

          <div class="step-body">
            <!-- 选客 -->
            <div v-if="selected.step === '选客'">
              <el-select v-model="pickCustomer" placeholder="选择客户" style="width:240px" @change="err=''" @visible-change="loadSupport">
                <el-option v-for="c in customers" :key="c.id" :label="c.name + '（' + c.memberLevel + '）' + ' / ' + c.phone" :value="c.id" />
              </el-select>
              <el-button type="primary" style="margin-left:10px" @click="goNextFromPick">选好客户，进入选型</el-button>
            </div>

            <!-- 选型 -->
            <div v-else-if="selected.step === '选型'">
              <div class="addrow">
                <el-select v-model="itemForm.productId" placeholder="选择商品" style="width:260px" filterable>
                  <el-option v-for="p in products" :key="p.id" :label="p.name + ' ¥' + p.price + ' 库存' + p.stock" :value="p.id" />
                </el-select>
                <el-input-number v-model="itemForm.qty" :min="1" style="margin:0 10px" />
                <el-button type="primary" @click="addItem">加购</el-button>
                <el-button style="margin-left:10px" @click="goConfirm">进入确认</el-button>
              </div>
              <el-table :data="items" border size="small" style="margin-top:10px">
                <el-table-column label="商品" min-width="180">
                  <template #default="{ row }">{{ productName(row.productId) }}</template>
                </el-table-column>
                <el-table-column prop="qty" label="数量" width="80" />
                <el-table-column label="小计" width="110">
                  <template #default="{ row }">{{ subtotal(productName(row.productId).match(/¥([\d.]+)/)?.[1] || 0, row.qty) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ row }">
                    <el-button link type="danger" @click="removeItem(row.id)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 确认 -->
            <div v-else-if="selected.step === '确认'">
              <el-alert type="warning" :closable="false" title="请核对商品与金额，确认后点击结算（库存将扣减）" />
              <el-table :data="items" border size="small" style="margin-top:10px">
                <el-table-column label="商品" min-width="180">
                  <template #default="{ row }">{{ productName(row.productId) }}</template>
                </el-table-column>
                <el-table-column prop="qty" label="数量" width="80" />
                <el-table-column label="小计" width="110">
                  <template #default="{ row }">{{ subtotal(productName(row.productId).match(/¥([\d.]+)/)?.[1] || 0, row.qty) }}</template>
                </el-table-column>
              </el-table>
              <div class="confirm-foot">
                <span>当前金额：<b>¥{{ selected.total }}</b>（批发客户享 9 折）</span>
                <el-button type="success" @click="settle">结算</el-button>
              </div>
            </div>

            <!-- 完成 -->
            <div v-else-if="selected.step === '完成'">
              <el-result icon="success" :title="'已结算 ¥' + selected.total" sub-title="订单完成，可在售后模块发起维修/退换" />
            </div>
          </div>

          <div v-if="selected.status === '草稿'" class="voidrow">
            <el-button type="danger" plain @click="voidOrder">作废此草稿</el-button>
          </div>
          <div v-if="err" class="err">{{ err }}</div>
        </div>
        <div v-else class="empty">从左侧选择一张开单，或点击「新建开单」。</div>
      </div>

      <!-- 右：状态历史时间线 -->
      <div class="right">
        <h3 class="tl-title">状态历史</h3>
        <el-timeline v-if="timeline.length">
          <el-timeline-item
            v-for="(t, i) in timeline"
            :key="i"
            :type="t.type || 'primary'"
            :hollow="i !== timeline.length - 1"
          >{{ t.text }}</el-timeline-item>
        </el-timeline>
        <div v-else class="empty">暂无历史。</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.title { font-size: 18px; margin: 0; }
.filters { display: flex; gap: 10px; }
.layout { display: grid; grid-template-columns: 1fr 280px; gap: 18px; align-items: start; }
.detail { margin-top: 14px; background: #fff; border: 1px solid var(--line); border-radius: 12px; padding: 16px; }
.steps { margin-bottom: 14px; }
.step-body { min-height: 120px; }
.addrow { display: flex; align-items: center; flex-wrap: wrap; }
.confirm-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; }
.voidrow { margin-top: 14px; }
.err { color: #e06c5a; font-size: 13px; margin-top: 8px; }
.right { background: #fff; border: 1px solid var(--line); border-radius: 12px; padding: 16px; }
.tl-title { font-size: 15px; margin: 0 0 12px; }
.empty { color: var(--muted); font-size: 13px; padding: 20px 0; text-align: center; }
</style>
