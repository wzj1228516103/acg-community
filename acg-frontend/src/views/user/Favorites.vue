<template>
  <div class="favorites-page">
    <div class="favorites-container">
      <h1 class="page-title">我的收藏</h1>

      <el-tabs v-model="activeTab" @tab-change="fetchFavorites">
        <el-tab-pane label="商品收藏" name="product" />
        <el-tab-pane label="化妆师收藏" name="makeup" />
      </el-tabs>

      <div v-if="loading" class="favorites-grid">
        <div v-for="i in 4" :key="i" class="favorite-skeleton">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 200px; border-radius: 16px 16px 0 0;" />
              <div style="padding: 16px;">
                <el-skeleton-item variant="h3" style="width: 70%; height: 20px; margin-bottom: 12px;" />
                <el-skeleton-item variant="text" style="width: 30%; height: 24px;" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <div v-else class="favorites-grid">
        <div
          v-for="item in favorites"
          :key="item.id"
          class="favorite-card"
        >
          <div class="card-image" @click="navigateToDetail(item)">
            <img :src="getItemImage(item)" :alt="item.name" />
            <el-button
              class="unfav-btn"
              circle
              @click.stop="handleUnfavorite(item)"
            >
              <el-icon :size="18" style="color: #ec4899;"><StarFilled /></el-icon>
            </el-button>
          </div>
          <div class="card-content" @click="navigateToDetail(item)">
            <h3>{{ item.name }}</h3>
            <div class="card-footer">
              <span class="price">¥{{ item.price }}</span>
              <span v-if="activeTab === 'product'" class="merchant">{{ item.merchantNickname || '' }}</span>
              <span v-else class="merchant">{{ item.artistNickname || '' }}</span>
            </div>
          </div>
        </div>
        <el-empty v-if="!loading && favorites.length === 0" :description="activeTab === 'product' ? '暂无商品收藏' : '暂无化妆师收藏'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavoritesApi, toggleFavoriteApi } from '@/api/favorite'

const router = useRouter()

const loading = ref(true)
const favorites = ref([])
const activeTab = ref('product')

function getItemImage(item) {
  try {
    const images = item.images ? JSON.parse(item.images) : []
    return images[0] || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+figure+collection+otaku&image_size=square'
  } catch {
    return 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+figure+collection+otaku&image_size=square'
  }
}

function navigateToDetail(item) {
  if (activeTab.value === 'product') {
    router.push('/product/' + item.itemId)
  } else {
    router.push('/makeup-service/' + item.itemId)
  }
}

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await getFavoritesApi(activeTab.value)
    favorites.value = res.data || []
  } catch {
    favorites.value = []
  } finally {
    loading.value = false
  }
}

async function handleUnfavorite(item) {
  try {
    await toggleFavoriteApi(activeTab.value, item.itemId)
    ElMessage.success('已取消收藏')
    favorites.value = favorites.value.filter((f) => f.itemId !== item.itemId)
  } catch {
    // handled by interceptor
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.favorites-container {
  max-width: 1280px;
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

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-top: 24px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.favorite-skeleton {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.favorite-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }

  .card-image {
    position: relative;
    height: 200px;
    overflow: hidden;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .unfav-btn {
      position: absolute;
      top: 12px;
      right: 12px;
      background: rgba(255, 255, 255, 0.9);
      border: none;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);

      &:hover {
        background: white;
      }
    }
  }

  .card-content {
    padding: 16px;
    cursor: pointer;

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

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price {
        font-size: 20px;
        font-weight: bold;
        color: #ec4899;
      }

      .merchant {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }
}
</style>
