<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../api'

const rows = ref([])
const loading = ref(false)
const filterLevel = ref('')

const levels = ['普通', '批发']

const dialog = ref(false)
const editing = ref(false)
const form = reactive({ id: null, name: '', phone: '', memberLevel: '普通' })
const err = ref('')

const emit = defineEmits(['changed'])

async function load() {
  loading.value = true
  try {
    rows.value = await api.listCustomers(filterLevel.value || undefined)
  } finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, name: '', phone: '', memberLevel: '普通' })
  err.value = ''
  dialog.value = true
}

function openEdit(r) {
  editing.value = true
  Object.assign(form, { id: r.id, name: r.name, phone: r.phone, memberLevel: r.memberLevel })
  err.value = ''
  dialog.value = true
}

async function save() {
  err.value = ''
  const payload = { name: form.name, phone: form.phone, memberLevel: form.memberLevel }
  try {
    if (editing.value) await api.updateCustomer(form.id, payload)
    else await api.createCustomer(payload)
    dialog.value = false
    await load()
    emit('changed')
  } catch (e) { err.value = e.message }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2 class="title">客户 · 普通 / 批发</h2>
      <div class="filters">
        <el-select v-model="filterLevel" placeholder="等级" clearable style="width:120px" @change="load">
          <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增客户</el-button>
      </div>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe>
      <el-table-column prop="name" label="客户名" min-width="160" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="memberLevel" label="等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.memberLevel === '批发' ? 'warning' : ''" size="small">{{ row.memberLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" :title="editing ? '编辑客户' : '新增客户'" width="420px">
      <el-form label-width="72px">
        <el-form-item label="客户名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="1 开头的 11 位号码" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.memberLevel" style="width:100%">
            <el-option v-for="l in levels" :key="l" :label="l" :value="l" />
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
