<template>
  <div class="shop-page">
    <div class="shop-container">
      <div class="shop-header">
        <h1 class="page-title">二次元周边商城</h1>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索商品..."
            size="large"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <div class="category-tabs">
        <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
          <el-tab-pane label="全部商品" name="" />
          <el-tab-pane v-for="cat in categories" :key="cat.id" :label="cat.name" :name="String(cat.id)" />
        </el-tabs>
      </div>

      <div v-if="loading" class="product-grid">
        <div v-for="i in 8" :key="i" class="product-skeleton">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 200px; border-radius: 16px 16px 0 0;" />
              <div style="padding: 16px;">
                <el-skeleton-item variant="h3" style="width: 80%; height: 20px; margin-bottom: 12px;" />
                <el-skeleton-item variant="text" style="width: 40%; height: 14px; margin-bottom: 12px;" />
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <el-skeleton-item variant="text" style="width: 25%; height: 24px;" />
                  <el-skeleton-item variant="button" style="width: 30%; height: 32px;" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <div v-else class="product-grid">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="router.push('/product/' + product.id)"
        >
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

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchProducts"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getProductsApi, getCategoriesApi } from '@/api/product'

const router = useRouter()

const loading = ref(true)
const products = ref([])
const categories = ref([])
const keyword = ref('')
const activeCategory = ref('')
const page = ref(1)
const pageSize = 12
const total = ref(0)

function getProductImage(product) {
  try {
    const images = product.images ? JSON.parse(product.images) : []
    return images[0] || 'https://picsum.photos/seed/anime/400/400'
  } catch {
    return 'https://picsum.photos/seed/anime/400/400'
  }
}

async function fetchProducts() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (activeCategory.value) params.categoryId = activeCategory.value
    const res = await getProductsApi(params)
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchProducts()
}

function handleCategoryChange() {
  page.value = 1
  fetchProducts()
}

onMounted(async () => {
  try {
    const res = await getCategoriesApi()
    categories.value = res.data || []
  } catch {
    categories.value = []
  }
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.shop-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.shop-container {
  max-width: 1280px;
  margin: 0 auto;
}

.shop-header {
  text-align: center;
  margin-bottom: 32px;

  .page-title {
    font-size: 32px;
    font-weight: bold;
    margin-bottom: 24px;
    background: linear-gradient(135deg, #ec4899, #a855f7);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .search-bar {
    max-width: 560px;
    margin: 0 auto;

    :deep(.el-input__wrapper) {
      border-radius: 24px;
      box-shadow: 0 4px 20px rgba(168, 85, 247, 0.1);
      padding-left: 8px;
    }

    :deep(.el-input-group__append) {
      border-radius: 0 24px 24px 0;
      background: linear-gradient(135deg, #ec4899, #a855f7);

      .el-button {
        color: white;
        border: none;
      }
    }
  }
}

.category-tabs {
  margin-bottom: 32px;

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
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.product-skeleton {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.product-card {
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
    height: 200px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
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

    .price {
      font-size: 20px;
      font-weight: bold;
      color: #ec4899;
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 48px;

  :deep(.el-pagination.is-background) {
    .el-pager li:not(.is-disabled).is-active {
      background: linear-gradient(135deg, #ec4899, #a855f7);
    }
  }
}
</style>
