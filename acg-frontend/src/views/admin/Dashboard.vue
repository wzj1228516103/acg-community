<template>
  <div class="dashboard-page">
    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.key" class="stat-card" :style="{ background: card.bg }">
        <div class="stat-info">
          <div class="stat-number">{{ dashboardData[card.key] ?? 0 }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
        <el-icon class="stat-icon" :size="48"><component :is="card.icon" /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, ShoppingBag, List, MagicStick } from '@element-plus/icons-vue'
import { getDashboardApi } from '@/api/admin'

const dashboardData = ref({
  userCount: 0,
  orderCount: 0,
  productCount: 0,
  makeupServiceCount: 0,
})

const statCards = [
  { key: 'userCount', label: '用户总数', icon: User, bg: 'linear-gradient(135deg, #ec4899, #a855f7)' },
  { key: 'orderCount', label: '订单总数', icon: List, bg: 'linear-gradient(135deg, #a855f7, #6366f1)' },
  { key: 'productCount', label: '商品总数', icon: ShoppingBag, bg: 'linear-gradient(135deg, #6366f1, #3b82f6)' },
  { key: 'makeupServiceCount', label: '化妆服务数', icon: MagicStick, bg: 'linear-gradient(135deg, #3b82f6, #06b6d4)' },
]

async function loadDashboard() {
  try {
    const res = await getDashboardApi()
    dashboardData.value = res.data
  } catch {
    // handled by interceptor
  }
}

onMounted(loadDashboard)
</script>

<style lang="scss" scoped>
.dashboard-page {
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }

  .stat-card {
    border-radius: 16px;
    padding: 28px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: white;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    .stat-info {
      .stat-number {
        font-size: 36px;
        font-weight: bold;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.85;
      }
    }

    .stat-icon {
      opacity: 0.3;
    }
  }
}
</style>
