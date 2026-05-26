<template>
  <div class="order-detail-page">
    <div class="detail-container">
      <h1 class="page-title">订单详情</h1>

      <el-card class="detail-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单信息</span>
            <el-tag :type="getStatusType(order.status)" size="default">{{ getStatusText(order.status) }}</el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.id }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span class="price-highlight">¥{{ order.totalAmount }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="detail-card" shadow="never">
        <template #header>
          <span class="card-title">商品清单</span>
        </template>
        <div v-for="(item, index) in orderItems" :key="index" class="order-item">
          <div class="item-image">
            <img :src="item.image || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+figure+collection+otaku&image_size=square'" :alt="item.name" />
          </div>
          <div class="item-info">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-price">¥{{ item.price }} x{{ item.quantity }}</span>
          </div>
          <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </el-card>

      <el-card v-if="order.receiverName" class="detail-card" shadow="never">
        <template #header>
          <span class="card-title">收货信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <div class="action-bar">
        <template v-if="order.status === 0">
          <el-button type="primary" size="large" round :loading="paying" @click="handlePay">立即支付</el-button>
          <el-button size="large" round :loading="cancelling" @click="handleCancel">取消订单</el-button>
        </template>
        <template v-else-if="order.status === 2">
          <el-button type="primary" size="large" round :loading="confirming" @click="handleConfirmReceive">确认收货</el-button>
        </template>
        <el-button size="large" round @click="router.push('/orders')">返回订单列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetailApi, payOrderApi, cancelOrderApi, confirmReceiveApi } from '@/api/order'

const route = useRoute()
const router = useRouter()

const order = ref({})
const loading = ref(true)
const paying = ref(false)
const cancelling = ref(false)
const confirming = ref(false)

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

const orderItems = computed(() => {
  try {
    return order.value.items ? JSON.parse(order.value.items) : []
  } catch {
    return []
  }
})

async function handlePay() {
  try {
    await ElMessageBox.confirm('确认支付此订单？', '确认支付', { type: 'info' })
  } catch {
    return
  }
  paying.value = true
  try {
    await payOrderApi(order.value.id)
    ElMessage.success('支付成功')
    order.value.status = 1
  } catch {
    // handled by interceptor
  } finally {
    paying.value = false
  }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '取消订单', { type: 'warning' })
  } catch {
    return
  }
  cancelling.value = true
  try {
    await cancelOrderApi(order.value.id)
    ElMessage.success('订单已取消')
    order.value.status = 4
  } catch {
    // handled by interceptor
  } finally {
    cancelling.value = false
  }
}

async function handleConfirmReceive() {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
  } catch {
    return
  }
  confirming.value = true
  try {
    await confirmReceiveApi(order.value.id)
    ElMessage.success('确认收货成功')
    order.value.status = 3
  } catch {
    // handled by interceptor
  } finally {
    confirming.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getOrderDetailApi(route.params.id)
    order.value = res.data || {}
  } catch {
    ElMessage.error('获取订单信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.order-detail-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.detail-container {
  max-width: 800px;
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

.detail-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }
}

.price-highlight {
  font-size: 16px;
  font-weight: bold;
  color: #ec4899;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }

  .item-image {
    width: 64px;
    height: 64px;
    border-radius: 10px;
    overflow: hidden;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .item-info {
    flex: 1;
    margin-left: 16px;
    display: flex;
    flex-direction: column;

    .item-name {
      font-size: 14px;
      color: #1f2937;
      margin-bottom: 4px;
    }

    .item-price {
      font-size: 13px;
      color: #9ca3af;
    }
  }

  .item-subtotal {
    font-size: 16px;
    font-weight: bold;
    color: #ec4899;
  }
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}
</style>
