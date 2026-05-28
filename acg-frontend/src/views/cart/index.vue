<template>
  <div class="cart-page">
    <div class="cart-container">
      <h1 class="page-title">我的购物车</h1>

      <template v-if="cartStore.items.length > 0">
        <el-card class="cart-card" shadow="never">
          <div class="cart-header">
            <el-checkbox v-model="allSelected" @change="handleSelectAll">全选</el-checkbox>
            <span class="col-header">商品信息</span>
            <span class="col-header">单价</span>
            <span class="col-header">数量</span>
            <span class="col-header">小计</span>
            <span class="col-header">操作</span>
          </div>

          <div v-for="item in cartStore.items" :key="item.productId" class="cart-item">
            <el-checkbox :model-value="item.selected" @change="cartStore.toggleSelect(item.productId)" />
            <div class="item-info">
              <div class="item-image" @click="router.push('/product/' + item.productId)">
                <img :src="item.image || 'https://picsum.photos/seed/anime/400/400'" :alt="item.name" />
              </div>
              <span class="item-name">{{ item.name }}</span>
            </div>
            <div class="item-price">¥{{ item.price }}</div>
            <div class="item-quantity">
              <el-input-number
                :model-value="item.quantity"
                :min="1"
                :max="item.stock"
                size="small"
                @change="(val) => cartStore.updateQuantity(item.productId, val)"
              />
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            <div class="item-action">
              <el-button type="danger" link @click="cartStore.removeItem(item.productId)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </el-card>

        <div class="cart-bottom">
          <div class="cart-bottom-left">
            <el-checkbox v-model="allSelected" @change="handleSelectAll">全选</el-checkbox>
            <span class="selected-count">已选 {{ cartStore.selectedItems.length }} 件商品</span>
          </div>
          <div class="cart-bottom-right">
            <div class="total-info">
              <span>合计：</span>
              <span class="total-price">¥{{ cartStore.selectedTotalPrice.toFixed(2) }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              round
              :disabled="cartStore.selectedItems.length === 0"
              @click="router.push('/checkout')"
            >
              去结算
            </el-button>
          </div>
        </div>
      </template>

      <div v-else class="empty-cart">
        <el-empty description="购物车空空如也">
          <template #image>
            <div class="empty-icon">
              <el-icon :size="80"><ShoppingCart /></el-icon>
            </div>
          </template>
          <el-button type="primary" round @click="router.push('/shop')">去逛逛</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, ShoppingCart } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const allSelected = computed({
  get: () => cartStore.items.length > 0 && cartStore.items.every((item) => item.selected),
  set: () => {},
})

function handleSelectAll(val) {
  cartStore.items.forEach((item) => {
    if (item.selected !== val) {
      cartStore.toggleSelect(item.productId)
    }
  })
}
</script>

<style lang="scss" scoped>
.cart-page {
  padding: 32px 24px 120px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.cart-container {
  max-width: 1200px;
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

.cart-card {
  border-radius: 16px;
  border: none;

  :deep(.el-card__body) {
    padding: 0;
  }
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.06), rgba(168, 85, 247, 0.06));
  border-radius: 16px 16px 0 0;
  font-size: 14px;
  color: #6b7280;
  gap: 16px;

  .col-header {
    flex: 1;
    text-align: center;

    &:nth-child(2) {
      flex: 2.5;
      text-align: left;
    }
  }
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
  gap: 16px;

  &:last-child {
    border-bottom: none;
  }

  .item-info {
    flex: 2.5;
    display: flex;
    align-items: center;
    gap: 12px;

    .item-image {
      width: 80px;
      height: 80px;
      border-radius: 12px;
      overflow: hidden;
      flex-shrink: 0;
      cursor: pointer;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .item-name {
      font-size: 14px;
      color: #1f2937;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .item-price {
    flex: 1;
    text-align: center;
    color: #6b7280;
    font-size: 14px;
  }

  .item-quantity {
    flex: 1;
    display: flex;
    justify-content: center;
  }

  .item-subtotal {
    flex: 1;
    text-align: center;
    font-weight: bold;
    color: #ec4899;
    font-size: 16px;
  }

  .item-action {
    flex: 0.5;
    display: flex;
    justify-content: center;
  }
}

.cart-bottom {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.08);
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 50;

  .cart-bottom-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .selected-count {
      font-size: 14px;
      color: #6b7280;
    }
  }

  .cart-bottom-right {
    display: flex;
    align-items: center;
    gap: 24px;

    .total-info {
      font-size: 14px;
      color: #374151;

      .total-price {
        font-size: 24px;
        font-weight: bold;
        color: #ec4899;
      }
    }
  }
}

.empty-cart {
  padding: 80px 0;

  .empty-icon {
    color: #e5e7eb;
  }
}
</style>
