<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import router from './router'
import api from './api'

import ProductView from './views/ProductView.vue'
import CustomerView from './views/CustomerView.vue'
import SalesOrderView from './views/SalesOrderView.vue'
import AfterSaleView from './views/AfterSaleView.vue'

// ------------------------------------------------------------------
// 导航形态：顶部横向导航（el-menu horizontal）。
// 四个业务模块一字排开，右侧显示各模块计数（loadCounts）。
// ------------------------------------------------------------------

const modules = [
  { path: '/product',     label: '商品',   sub: '工具配件库存', comp: ProductView },
  { path: '/customer',    label: '客户',   sub: '普通批发',     comp: CustomerView },
  { path: '/sales-order', label: '开单',   sub: '步骤条结算',   comp: SalesOrderView },
  { path: '/after-sale',  label: '售后',   sub: '维修退换',     comp: AfterSaleView }
]

const currentPath = ref(router.currentRoute.value.path)
const counts = ref({})

const activeIndex = computed(() => currentPath.value)

const currentView = computed(() => {
  const m = modules.find((mm) => mm.path === currentPath.value)
  return m ? m.comp : ProductView
})

async function loadCounts() {
  try {
    const [p, c, s, a] = await Promise.all([
      api.listProducts(), api.listCustomers(),
      api.listSalesOrders(), api.listAfterSales()
    ])
    counts.value = {
      '/product': p.length,
      '/customer': c.length,
      '/sales-order': s.length,
      '/after-sale': a.length
    }
  } catch (e) { /* 计数失败不影响主流程 */ }
}

function handleSelect(path) {
  router.push(path)
}

watch(() => router.currentRoute.value.path, (p) => { currentPath.value = p })

onMounted(async () => {
  currentPath.value = router.currentRoute.value.path
  await loadCounts()
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">五</span>
        <div class="brand-text">
          <div class="brand-title">五金店 · 工具配件与开单销售</div>
          <div class="brand-sub">钢青主题 · 顶部横向导航 · 开单走步骤条</div>
        </div>
      </div>
    </header>

    <el-menu
      :default-active="activeIndex"
      mode="horizontal"
      class="mainnav"
      @select="handleSelect"
    >
      <el-menu-item
        v-for="m in modules"
        :key="m.path"
        :index="m.path"
      >
        <span class="nav-label">{{ m.label }}</span>
        <span class="nav-sub">{{ m.sub }}</span>
        <span class="nav-count" v-if="counts[m.path] !== undefined">{{ counts[m.path] }}</span>
      </el-menu-item>
    </el-menu>

    <main class="content">
      <component :is="currentView" @changed="loadCounts" />
    </main>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: flex; flex-direction: column; }

.topbar {
  display: flex; align-items: center; gap: 16px;
  padding: 0 22px; height: 64px;
  background: linear-gradient(100deg, var(--primary-deep) 0%, var(--primary) 60%, var(--primary-soft) 100%);
  color: #fff;
  box-shadow: 0 2px 12px rgba(0, 105, 95, 0.22);
}
.brand { display: flex; align-items: center; gap: 12px; }
.brand-mark {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: #fff; color: var(--primary-deep); font-weight: 700; font-size: 20px;
}
.brand-title { font-size: 17px; font-weight: 600; }
.brand-sub { font-size: 12px; opacity: 0.85; }

.mainnav {
  --el-menu-bg-color: #fff;
  --el-menu-text-color: var(--ink);
  --el-menu-active-color: var(--primary);
  --el-menu-hover-bg-color: var(--primary-wash);
  border-bottom: 1px solid var(--line);
  padding: 0 12px;
}
.mainnav :deep(.el-menu-item) {
  display: flex; align-items: baseline; gap: 6px; height: 56px; line-height: 56px;
}
.nav-label { font-size: 16px; font-weight: 600; }
.nav-sub { font-size: 11px; color: var(--muted); }
.nav-count {
  min-width: 20px; height: 20px; padding: 0 6px; border-radius: 10px;
  background: var(--primary); color: #fff; font-size: 12px;
  display: inline-flex; align-items: center; justify-content: center;
}

.content { flex: 1; padding: 20px 26px 40px; }
</style>
