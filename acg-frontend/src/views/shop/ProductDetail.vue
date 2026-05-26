<template>
  <div class="product-detail-page">
    <div class="detail-container">
      <div class="detail-main">
        <div class="detail-left">
          <div class="main-image">
            <img :src="currentImage" :alt="product.name" />
          </div>
          <div v-if="imageList.length > 1" class="thumbnail-list">
            <div
              v-for="(img, index) in imageList"
              :key="index"
              class="thumbnail"
              :class="{ active: index === currentImageIndex }"
              @click="currentImageIndex = index"
            >
              <img :src="img" alt="" />
            </div>
          </div>
        </div>

        <div class="detail-right">
          <h1 class="product-name">{{ product.name }}</h1>
          <div class="product-price">¥{{ product.price }}</div>

          <div class="info-row">
            <span class="label">库存</span>
            <span class="value">{{ product.stock }} 件</span>
          </div>
          <div class="info-row">
            <span class="label">分类</span>
            <el-tag size="small">{{ product.categoryName || '未分类' }}</el-tag>
          </div>
          <div class="info-row">
            <span class="label">商家</span>
            <div class="merchant-info">
              <el-avatar :size="28" :src="product.merchantAvatarUrl">
                {{ product.merchantNickname?.[0] || 'M' }}
              </el-avatar>
              <span>{{ product.merchantNickname || '漫化商城' }}</span>
            </div>
          </div>

          <div class="quantity-row">
            <span class="label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="product.stock || 999" />
          </div>

          <div class="action-buttons">
            <el-button type="primary" size="large" round @click="handleAddToCart">
              <el-icon class="mr-1"><ShoppingCart /></el-icon>
              加入购物车
            </el-button>
            <el-button size="large" round class="buy-now-btn" @click="handleBuyNow">
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-bottom">
        <el-tabs>
          <el-tab-pane label="商品详情">
            <div class="description-content" v-html="product.description || '暂无商品详情'" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetailApi } from '@/api/product'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref({})
const quantity = ref(1)
const currentImageIndex = ref(0)
const loading = ref(true)

const imageList = computed(() => {
  try {
    return product.value.images ? JSON.parse(product.value.images) : []
  } catch {
    return []
  }
})

const currentImage = computed(() => {
  return imageList.value[currentImageIndex.value] || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+figure+collection+otaku&image_size=square'
})

function handleAddToCart() {
  cartStore.addItem({ ...product.value, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}

function handleBuyNow() {
  cartStore.addItem({ ...product.value, quantity: quantity.value })
  router.push('/checkout')
}

onMounted(async () => {
  try {
    const res = await getProductDetailApi(route.params.id)
    product.value = res.data || {}
  } catch {
    ElMessage.error('获取商品信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.product-detail-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-main {
  display: flex;
  gap: 48px;
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 32px;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 24px;
  }
}

.detail-left {
  flex: 1;
  max-width: 500px;

  .main-image {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 16px;
    overflow: hidden;
    background: #f3f4f6;
    margin-bottom: 16px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .thumbnail-list {
    display: flex;
    gap: 12px;

    .thumbnail {
      width: 72px;
      height: 72px;
      border-radius: 10px;
      overflow: hidden;
      cursor: pointer;
      border: 2px solid transparent;
      transition: border-color 0.3s;

      &.active {
        border-color: #a855f7;
      }

      &:hover {
        border-color: #ec4899;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
}

.detail-right {
  flex: 1;

  .product-name {
    font-size: 24px;
    font-weight: bold;
    color: #1f2937;
    margin-bottom: 16px;
    line-height: 1.4;
  }

  .product-price {
    font-size: 36px;
    font-weight: bold;
    color: #ec4899;
    margin-bottom: 24px;
    padding-bottom: 24px;
    border-bottom: 1px solid #f3f4f6;
  }

  .info-row {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    .label {
      width: 60px;
      font-size: 14px;
      color: #6b7280;
      flex-shrink: 0;
    }

    .value {
      font-size: 14px;
      color: #1f2937;
    }

    .merchant-info {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #1f2937;
    }
  }

  .quantity-row {
    display: flex;
    align-items: center;
    margin-bottom: 32px;
    margin-top: 8px;

    .label {
      width: 60px;
      font-size: 14px;
      color: #6b7280;
      flex-shrink: 0;
    }
  }

  .action-buttons {
    display: flex;
    gap: 16px;

    .buy-now-btn {
      background: linear-gradient(135deg, #ec4899, #a855f7);
      color: white;
      border: none;

      &:hover {
        background: linear-gradient(135deg, #db2777, #9333ea);
      }
    }
  }
}

.detail-bottom {
  background: white;
  border-radius: 20px;
  padding: 24px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  :deep(.el-tabs__item.is-active) {
    color: #a855f7;
  }

  :deep(.el-tabs__active-bar) {
    background: linear-gradient(135deg, #ec4899, #a855f7);
  }

  .description-content {
    padding: 16px 0;
    line-height: 1.8;
    color: #374151;
    font-size: 15px;

    img {
      max-width: 100%;
      border-radius: 8px;
    }
  }
}
</style>
