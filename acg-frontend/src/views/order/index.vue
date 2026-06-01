<template>
  <div class="orders-page">
    <div class="orders-container">
      <h1 class="page-title">我的订单</h1>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="已支付" name="1" />
        <el-tab-pane label="已发货" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>

      <div v-if="loading" class="order-list">
        <div v-for="i in 3" :key="i" class="order-skeleton">
          <el-skeleton animated>
            <template #template>
              <div style="padding: 24px;">
                <el-skeleton-item variant="text" style="width: 40%; height: 16px; margin-bottom: 16px;" />
                <div style="display: flex; gap: 16px; margin-bottom: 16px;">
                  <el-skeleton-item variant="image" style="width: 64px; height: 64px; border-radius: 8px;" />
                  <el-skeleton-item variant="text" style="width: 60%; height: 16px;" />
                </div>
                <div style="display: flex; justify-content: flex-end;">
                  <el-skeleton-item variant="text" style="width: 20%; height: 20px;" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <div v-else class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card" @click="router.push('/order/' + order.id)">
          <div class="order-header">
            <span class="order-id">订单号：{{ order.id }}</span>
            <span class="order-date">{{ order.createTime }}</span>
            <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
          </div>
          <div class="order-body">
            <div class="order-items">
              <div v-for="(item, index) in getOrderItems(order)" :key="index" class="order-item">
                <div class="item-image">
                  <img :src="item.image || 'https://picsum.photos/seed/anime/400/400'" :alt="item.name" />
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-qty">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
            <div class="order-total">
              <span>合计：</span>
              <span class="total-price">¥{{ order.totalAmount }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
      </div>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrdersApi } from '@/api/order'

const router = useRouter()

const loading = ref(true)
const orders = ref([])
const activeTab = ref('')
const page = ref(1)
const pageSize = 10
const total = ref(0)

const statusMap = {
  0: '待付款',
  1: '已支付',
  2: '已发货',
  3: '已完成',
  4: '已取消',
}

const statusTypeMap = {
  0: 'warning',
  1: 'primary',
  2: 'info',
  3: 'success',
  4: 'danger',
}

function getStatusText(status) {
  return statusMap[status] || '未知'
}

function getStatusType(status) {
  return statusTypeMap[status] || 'info'
}

function parseImage(img) {
  if (!img) return ''
  if (img.startsWith('[')) {
    try {
      const arr = JSON.parse(img)
      return Array.isArray(arr) && arr.length > 0 ? arr[0] : ''
    } catch {
      return ''
    }
  }
  if (img.startsWith('http')) return img
  return ''
}

function getOrderItems(order) {
  const items = order.items
  if (!items) return []
  if (Array.isArray(items)) {
    return items.map(item => ({
      name: item.productName,
      image: parseImage(item.productImage),
      price: item.price,
      quantity: item.quantity,
    }))
  }
  try {
    return JSON.parse(items)
  } catch {
    return []
  }
}

async function fetchOrders() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (activeTab.value !== '') params.status = activeTab.value
    const res = await getOrdersApi(params)
    orders.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.orders-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.orders-container {
  max-width: 960px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #ec4899, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
  border-radius: 20px;
  margin-right: 8px;

  &.is-active {
    color: white;
    background: linear-gradient(135deg, #ec4899, #a855f7);
  }
}

:deep(.el-tabs__active-bar) {
  display: none;
}

.order-list {
  margin-top: 24px;
}

.order-skeleton {
  background: white;
  border-radius: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
}

.order-card {
  background: white;
  border-radius: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }

  .order-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 24px;
    border-bottom: 1px solid #f3f4f6;
    background: linear-gradient(135deg, rgba(236, 72, 153, 0.04), rgba(168, 85, 247, 0.04));
    border-radius: 16px 16px 0 0;

    .order-id {
      font-size: 14px;
      color: #374151;
      font-weight: 500;
    }

    .order-date {
      font-size: 13px;
      color: #9ca3af;
      flex: 1;
    }
  }

  .order-body {
    padding: 16px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .order-items {
      flex: 1;
    }

    .order-item {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .item-image {
        width: 56px;
        height: 56px;
        border-radius: 8px;
        overflow: hidden;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .item-info {
        display: flex;
        align-items: center;
        gap: 8px;

        .item-name {
          font-size: 14px;
          color: #1f2937;
        }

        .item-qty {
          font-size: 13px;
          color: #9ca3af;
        }
      }
    }

    .order-total {
      text-align: right;
      font-size: 14px;
      color: #6b7280;

      .total-price {
        font-size: 20px;
        font-weight: bold;
        color: #ec4899;
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;

  :deep(.el-pagination.is-background) {
    .el-pager li:not(.is-disabled).is-active {
      background: linear-gradient(135deg, #ec4899, #a855f7);
    }
  }
}
</style>
