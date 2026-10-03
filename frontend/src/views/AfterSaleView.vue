<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../api'

const rows = ref([])
const loading = ref(false)
const filterStatus = ref('')
const statuses = ['待处理', '已处理']

const settledOrders = ref([])
const selected = ref(null)

const dialog = ref(false)
const form = reactive({ orderId: null, reason: '', handle: '维修' })
const err = ref('')

const handles = ['维修', '退换', '作废']

const emit = defineEmits(['changed'])

const timeline = computed(() => {
  if (!selected.value) return []
  const t = [{ text: '创建售后（' + selected.value.handle + '）', type: 'primary' }]
  if (selected.value.status === '已处理') {
    t.push({ text: selected.value.handle === '退换' ? '已处理：库存回滚' : '已处理', type: 'success' })
  } else {
    t.push({ text: '等待处理…', type: '', hollow: true })
  }
  return t
})

async function load() {
  loading.value = true
  try {
    rows.value = await api.listAfterSales(filterStatus.value || undefined)
  } finally { loading.value = false }
}
async function loadOrders() {
  settledOrders.value = await api.listSalesOrders('已结算')
}

function selectRow(row) {
  selected.value = row
}

async function openCreate() {
  err.value = ''
  await loadOrders()
  Object.assign(form, { orderId: null, reason: '', handle: '维修' })
  dialog.value = true
}

async function createAfterSale() {
  err.value = ''
  const payload = { orderId: form.orderId, reason: form.reason, handle: form.handle }
  try {
    await api.createAfterSale(payload)
    dialog.value = false
    await load()
    emit('changed')
  } catch (e) { err.value = e.message }
}

async function process(id) {
  err.value = ''
  try {
    await api.processAfterSale(id)
    await load()
    if (selected.value && selected.value.id === id) selected.value = rows.value.find((r) => r.id === id) || null
    emit('changed')
  } catch (e) { err.value = e.message }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="title">售后 · 维修 / 退换 / 作废</h2>
      <div class="filters">
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width:120px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="openCreate">新建售后</el-button>
      </div>
    </div>

    <div class="layout">
      <div class="left">
        <el-table :data="rows" v-loading="loading" highlight-current-row border @current-change="selectRow" style="cursor:pointer">
          <el-table-column prop="id" label="单号" width="70" />
          <el-table-column prop="orderId" label="关联开单" width="100" />
          <el-table-column prop="handle" label="处理" width="90" />
          <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === '已处理' ? 'success' : 'warning'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === '待处理'" link type="primary" @click="process(row.id)">处理</el-button>
              <span v-else class="done">已处理</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="right">
        <h3 class="tl-title">处理流程</h3>
        <el-timeline v-if="timeline.length">
          <el-timeline-item
            v-for="(t, i) in timeline"
            :key="i"
            :type="t.type || 'primary'"
            :hollow="i !== timeline.length - 1"
          >{{ t.text }}</el-timeline-item>
        </el-timeline>
        <div v-else class="empty">选择左侧一条售后查看流程。</div>
      </div>
    </div>

    <el-dialog v-model="dialog" title="新建售后" width="440px">
      <el-form label-width="72px">
        <el-form-item label="关联开单">
          <el-select v-model="form.orderId" style="width:100%" placeholder="只能选已结算单">
            <el-option v-for="o in settledOrders" :key="o.id" :label="'开单#' + o.id + ' ¥' + o.total" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理方式">
          <el-select v-model="form.handle" style="width:100%">
            <el-option v-for="h in handles" :key="h" :label="h" :value="h" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="2" /></el-form-item>
        <div v-if="err" class="err">{{ err }}</div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="createAfterSale">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.title { font-size: 18px; margin: 0; }
.filters { display: flex; gap: 10px; }
.layout { display: grid; grid-template-columns: 1fr 280px; gap: 18px; align-items: start; }
.right { background: #fff; border: 1px solid var(--line); border-radius: 12px; padding: 16px; }
.tl-title { font-size: 15px; margin: 0 0 12px; }
.empty { color: var(--muted); font-size: 13px; padding: 20px 0; text-align: center; }
.done { color: var(--muted); font-size: 12px; }
.err { color: #e06c5a; font-size: 13px; margin-top: 6px; }
</style>
