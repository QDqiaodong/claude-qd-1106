<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'

const rows = ref([])
const loading = ref(false)
const filterCategory = ref('')
const filterStatus = ref('')

const categories = ['工具', '紧固件', '电工', '建材']
const statuses = ['在售', '停用']

const dialog = ref(false)
const editing = ref(false)
const form = reactive({ id: null, sku: '', name: '', category: '', price: '', stock: '', status: '在售' })
const err = ref('')

async function load() {
  loading.value = true
  try {
    rows.value = await api.listProducts(filterCategory.value || undefined, filterStatus.value || undefined)
  } finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, sku: '', name: '', category: '', price: '', stock: '', status: '在售' })
  err.value = ''
  dialog.value = true
}

function openEdit(r) {
  editing.value = true
  Object.assign(form, { id: r.id, sku: r.sku, name: r.name, category: r.category, price: r.price, stock: r.stock, status: r.status })
  err.value = ''
  dialog.value = true
}

async function save() {
  err.value = ''
  const payload = {
    sku: form.sku, name: form.name, category: form.category,
    price: Number(form.price), stock: Number(form.stock), status: form.status
  }
  try {
    if (editing.value) {
      await api.updateProduct(form.id, payload)
    } else {
      await api.createProduct(payload)
    }
    dialog.value = false
    await load()
    emit('changed')
  } catch (e) { err.value = e.message }
}

const emit = defineEmits(['changed'])

onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="title">商品 · 工具配件</h2>
      <div class="filters">
        <el-select v-model="filterCategory" placeholder="类别" clearable style="width:120px" @change="load">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width:120px" @change="load">
          <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增商品</el-button>
      </div>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="sku" label="货号" width="130" />
      <el-table-column prop="name" label="品名" min-width="200" />
      <el-table-column prop="category" label="类别" width="100" />
      <el-table-column prop="price" label="单价" width="100" />
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '在售' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑商品' : '新增商品'" width="440px">
      <el-form label-width="72px">
        <el-form-item label="货号"><el-input v-model="form.sku" placeholder="如 HW-1001" /></el-form-item>
        <el-form-item label="品名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="form.category" style="width:100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价"><el-input v-model="form.price" type="number" /></el-form-item>
        <el-form-item label="库存"><el-input v-model="form.stock" type="number" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <div v-if="err" class="err">{{ err }}</div>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.title { font-size: 18px; margin: 0; }
.filters { display: flex; gap: 10px; }
.err { color: #e06c5a; font-size: 13px; margin-top: 6px; }
</style>
