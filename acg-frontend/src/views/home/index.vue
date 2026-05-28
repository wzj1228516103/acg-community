<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="hero-container">
        <h1 class="hero-title">漫化 — 二次元文化综合社区平台</h1>
        <p class="hero-desc">
          专注于二次元文化的综合性社区平台，融合社交互动、购物消费和即时聊天功能。认证化妆师服务，让您完美变身心中的角色。
        </p>
        <div class="hero-buttons">
          <el-button type="primary" size="large" round @click="$router.push('/makeup-services')">
            化妆师服务
          </el-button>
          <el-button size="large" round @click="$router.push('/profile')">
            个人中心
          </el-button>
        </div>
      </div>
    </section>

    <section class="stats-section">
      <div class="stats-container">
        <div class="stat-card">
          <span class="stat-number">1000+</span>
          <span class="stat-label">认证化妆师</span>
        </div>
        <div class="stat-card">
          <span class="stat-number">5000+</span>
          <span class="stat-label">精选商品</span>
        </div>
        <div class="stat-card">
          <span class="stat-number">10W+</span>
          <span class="stat-label">活跃用户</span>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-container">
        <h2 class="section-title">热门化妆师服务</h2>
        <div class="service-grid">
          <div v-for="service in makeupServices" :key="service.id" class="service-card" @click="$router.push(`/makeup-service/${service.id}`)">
            <div class="card-image">
              <img :src="getServiceImage(service)" :alt="service.name" />
              <div class="card-badge">{{ service.duration }}分钟</div>
            </div>
            <div class="card-content">
              <h3>{{ service.name }}</h3>
              <div class="card-info">
                <div class="artist-info">
                  <el-avatar :size="24" :src="service.artistAvatar">
                    {{ service.artistNickname?.[0] || 'M' }}
                  </el-avatar>
                  <span>{{ service.artistNickname || '化妆师' }}</span>
                </div>
                <span class="price">¥{{ service.price }}</span>
              </div>
              <el-button type="primary" round size="small" class="view-btn">查看服务</el-button>
            </div>
          </div>
          <el-empty v-if="!loading && makeupServices.length === 0" description="暂无化妆师服务" />
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-container">
        <h2 class="section-title">热门商品推荐</h2>
        <div class="product-grid">
          <div v-for="product in products" :key="product.id" class="product-card" @click="$router.push(`/product/${product.id}`)">
            <div class="card-image">
              <img :src="getProductImage(product)" :alt="product.name" />
            </div>
            <div class="card-content">
              <h3>{{ product.name }}</h3>
              <div class="card-meta">
                <el-tag size="small" type="info">{{ product.categoryName || '未分类' }}</el-tag>
                <span class="merchant">{{ product.merchantNickname || '漫化商城' }}</span>
              </div>
              <div class="card-footer">
                <span class="price">¥{{ product.price }}</span>
                <el-button type="primary" round size="small">查看详情</el-button>
              </div>
            </div>
          </div>
          <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
        </div>
      </div>
    </section>

    <section class="section services-section">
      <div class="section-container">
        <h2 class="section-title">我们的服务</h2>
        <div class="services-grid">
          <div v-for="service in platformServices" :key="service.title" class="platform-service-card">
            <div class="service-icon">
              <el-icon :size="32"><component :is="service.icon" /></el-icon>
            </div>
            <h3>{{ service.title }}</h3>
            <p>{{ service.desc }}</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, markRaw } from 'vue'
import { ShoppingBag, Scissor, MagicStick, ChatDotSquare, ChatLineRound } from '@element-plus/icons-vue'
import { getMakeupServicesApi } from '@/api/makeup'
import { getProductsApi } from '@/api/product'

const loading = ref(true)
const makeupServices = ref([])
const products = ref([])

const platformServices = [
  { icon: markRaw(ShoppingBag), title: '二次元周边商城', desc: '精选正版周边商品，品质保证' },
  { icon: markRaw(Scissor), title: '专业化妆师服务', desc: '认证化妆师一对一服务' },
  { icon: markRaw(MagicStick), title: 'Cosplay服装定制', desc: '个性化定制服务' },
  { icon: markRaw(ChatDotSquare), title: '社区交流互动', desc: '与同好分享交流' },
  { icon: markRaw(ChatLineRound), title: '即时聊天功能', desc: '与化妆师、商家实时沟通' },
]

const PLACEHOLDER_SERVICE = 'https://picsum.photos/seed/makeup/640/400'
const PLACEHOLDER_PRODUCT = 'https://picsum.photos/seed/anime/400/400'

function getServiceImage(service) {
  try {
    const images = service.images ? JSON.parse(service.images) : []
    return images[0] || PLACEHOLDER_SERVICE
  } catch {
    return PLACEHOLDER_SERVICE
  }
}

function getProductImage(product) {
  try {
    const images = product.images ? JSON.parse(product.images) : []
    return images[0] || PLACEHOLDER_PRODUCT
  } catch {
    return PLACEHOLDER_PRODUCT
  }
}

onMounted(async () => {
  try {
    const [makeupRes, productRes] = await Promise.all([
      getMakeupServicesApi({ page: 1, size: 3 }).catch(() => ({ data: { records: [] } })),
      getProductsApi({ page: 1, size: 4 }).catch(() => ({ data: { records: [] } })),
    ])
    makeupServices.value = makeupRes.data?.records || []
    products.value = productRes.data?.records || []
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.home-page {
  .hero-section {
    background: linear-gradient(135deg, #ec4899, #a855f7, #6366f1);
    padding: 80px 24px;
    text-align: center;
    color: white;

    .hero-container {
      max-width: 800px;
      margin: 0 auto;
    }

    .hero-title {
      font-size: 42px;
      font-weight: bold;
      margin-bottom: 20px;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    }

    .hero-desc {
      font-size: 18px;
      opacity: 0.9;
      margin-bottom: 32px;
      line-height: 1.6;
    }

    .hero-buttons {
      display: flex;
      gap: 16px;
      justify-content: center;
    }
  }

  .stats-section {
    padding: 48px 24px;
    background: white;

    .stats-container {
      max-width: 1280px;
      margin: 0 auto;
      display: flex;
      justify-content: center;
      gap: 48px;
    }

    .stat-card {
      text-align: center;
      padding: 24px 48px;
      border-radius: 16px;
      background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(168, 85, 247, 0.1));

      .stat-number {
        display: block;
        font-size: 36px;
        font-weight: bold;
        background: linear-gradient(135deg, #ec4899, #a855f7);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .stat-label {
        font-size: 14px;
        color: #6b7280;
        margin-top: 4px;
      }
    }
  }

  .section {
    padding: 64px 24px;

    .section-container {
      max-width: 1280px;
      margin: 0 auto;
    }

    .section-title {
      font-size: 28px;
      font-weight: bold;
      text-align: center;
      margin-bottom: 40px;
      background: linear-gradient(135deg, #ec4899, #a855f7);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }

  .service-grid, .product-grid {
    display: grid;
    gap: 24px;
  }

  .service-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  }

  .service-card, .product-card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s, box-shadow 0.3s;
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    }

    .card-image {
      position: relative;
      height: 200px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .card-badge {
        position: absolute;
        top: 12px;
        right: 12px;
        background: rgba(168, 85, 247, 0.9);
        color: white;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
      }
    }

    .card-content {
      padding: 16px;

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: #1f2937;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }

    .card-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .artist-info {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: #6b7280;
      }
    }

    .card-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;

      .merchant {
        font-size: 12px;
        color: #9ca3af;
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .price {
      font-size: 20px;
      font-weight: bold;
      color: #ec4899;
    }
  }

  .services-section {
    background: linear-gradient(135deg, #fdf2f8, #f5f3ff);

    .services-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 24px;
    }

    .platform-service-card {
      background: white;
      border-radius: 16px;
      padding: 32px 24px;
      text-align: center;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
      transition: transform 0.3s;

      &:hover {
        transform: translateY(-4px);
      }

      .service-icon {
        width: 64px;
        height: 64px;
        margin: 0 auto 16px;
        background: linear-gradient(135deg, #ec4899, #a855f7);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
      }

      h3 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 8px;
        color: #1f2937;
      }

      p {
        font-size: 13px;
        color: #6b7280;
      }
    }
  }
}
</style>
