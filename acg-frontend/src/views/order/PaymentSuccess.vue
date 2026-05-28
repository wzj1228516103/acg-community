<template>
  <div class="payment-success-page">
    <div class="success-container">
      <el-card class="success-card" shadow="never">
        <div class="success-content">
          <div class="success-icon">
            <el-icon :size="64"><CircleCheck /></el-icon>
          </div>
          <h2 class="success-title">支付成功</h2>
          <p class="success-desc">您的订单已提交成功，我们会尽快为您处理</p>
          <div class="success-actions">
            <el-button type="primary" size="large" round @click="router.push('/orders')">查看订单</el-button>
            <el-button size="large" round @click="router.push('/shop')">继续购物</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style lang="scss" scoped>
.payment-success-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.success-container {
  max-width: 560px;
  margin: 0 auto;
}

.success-card {
  border-radius: 20px;
  border: none;
  padding: 60px 40px;
  text-align: center;

  .success-content {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .success-icon {
    width: 100px;
    height: 100px;
    background: linear-gradient(135deg, #ec4899, #a855f7);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin-bottom: 24px;
  }

  .success-title {
    font-size: 24px;
    font-weight: bold;
    color: #1f2937;
    margin-bottom: 12px;
  }

  .success-desc {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 32px;
  }

  .success-actions {
    display: flex;
    gap: 16px;
  }
}
</style>
