<template>
  <div class="checkout-page">
    <div class="checkout-container">
      <h1 class="page-title">订单结算</h1>

      <el-card class="checkout-card" shadow="never">
        <template #header>
          <span class="card-title">商品清单</span>
        </template>
        <div v-for="item in selectedItems" :key="item.productId" class="checkout-item">
          <div class="item-image">
            <img :src="item.image || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+figure+collection+otaku&image_size=square'" :alt="item.name" />
          </div>
          <div class="item-info">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-qty">x{{ item.quantity }}</span>
          </div>
          <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
        </div>
      </el-card>

      <el-card class="checkout-card" shadow="never">
        <template #header>
          <span class="card-title">收货信息</span>
        </template>
        <el-form ref="receiverFormRef" :model="receiverForm" :rules="receiverRules" label-width="80px">
          <el-form-item label="收货人" prop="name">
            <el-input v-model="receiverForm.name" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="receiverForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="收货地址" prop="address">
            <el-input v-model="receiverForm.address" type="textarea" :rows="2" placeholder="请输入详细收货地址" />
          </el-form-item>
        </el-form>
      </el-card>

      <div class="checkout-summary">
        <div class="summary-line">
          <span>商品合计：</span>
          <span>¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="summary-line">
          <span>运费：</span>
          <span>免运费</span>
        </div>
        <div class="summary-total">
          <span>应付金额：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <el-button
          type="primary"
          size="large"
          round
          :loading="submitting"
          style="width: 240px; margin-top: 16px;"
          @click="handleSubmitOrder"
        >
          提交订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrderApi } from '@/api/order'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const receiverFormRef = ref()
const submitting = ref(false)

const selectedItems = computed(() => cartStore.selectedItems)
const totalPrice = computed(() => cartStore.selectedTotalPrice)

const receiverForm = ref({
  name: '',
  phone: '',
  address: '',
})

const receiverRules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }],
}

async function handleSubmitOrder() {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择商品')
    return
  }
  try {
    await receiverFormRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await createOrderApi({
      items: selectedItems.value.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
      receiverName: receiverForm.value.name,
      receiverPhone: receiverForm.value.phone,
      receiverAddress: receiverForm.value.address,
    })
    cartStore.clearSelected()
    ElMessage.success('下单成功')
    router.push('/payment-success')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.checkout-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.checkout-container {
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

.checkout-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }
}

.checkout-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }

  .item-image {
    width: 72px;
    height: 72px;
    border-radius: 12px;
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

    .item-qty {
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

.checkout-summary {
  background: white;
  border-radius: 16px;
  padding: 24px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: right;

  .summary-line {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 12px;
  }

  .summary-total {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 16px;
    font-size: 16px;
    color: #1f2937;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;

    .total-price {
      font-size: 28px;
      font-weight: bold;
      color: #ec4899;
    }
  }
}
</style>
