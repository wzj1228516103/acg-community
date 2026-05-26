<template>
  <div class="makeup-page">
    <div class="makeup-container">
      <div class="page-header">
        <h1 class="page-title">专业化妆师服务</h1>
        <p class="page-desc">认证化妆师一对一服务，让您完美变身心中的角色</p>
      </div>

      <div v-if="loading" class="service-grid">
        <div v-for="i in 6" :key="i" class="service-skeleton">
          <el-skeleton animated>
            <template #template>
              <el-skeleton-item variant="image" style="width: 100%; height: 220px; border-radius: 16px 16px 0 0;" />
              <div style="padding: 16px;">
                <el-skeleton-item variant="h3" style="width: 70%; height: 20px; margin-bottom: 12px;" />
                <el-skeleton-item variant="text" style="width: 50%; height: 14px; margin-bottom: 12px;" />
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <el-skeleton-item variant="text" style="width: 30%; height: 14px;" />
                  <el-skeleton-item variant="text" style="width: 20%; height: 24px;" />
                </div>
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <div v-else class="service-grid">
        <div
          v-for="service in services"
          :key="service.id"
          class="service-card"
          @click="router.push('/makeup-service/' + service.id)"
        >
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
        <el-empty v-if="!loading && services.length === 0" description="暂无化妆师服务" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMakeupServicesApi } from '@/api/makeup'

const router = useRouter()
const loading = ref(true)
const services = ref([])

function getServiceImage(service) {
  try {
    const images = service.images ? JSON.parse(service.images) : []
    return images[0] || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+cosplay+makeup+service+professional&image_size=landscape_4_3'
  } catch {
    return 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+cosplay+makeup+service+professional&image_size=landscape_4_3'
  }
}

onMounted(async () => {
  try {
    const res = await getMakeupServicesApi({ page: 1, size: 20 })
    services.value = res.data?.records || []
  } catch {
    services.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.makeup-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.makeup-container {
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;

  .page-title {
    font-size: 32px;
    font-weight: bold;
    margin-bottom: 12px;
    background: linear-gradient(135deg, #ec4899, #a855f7);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .page-desc {
    font-size: 16px;
    color: #6b7280;
  }
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;

  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
}

.service-skeleton {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.service-card {
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
    height: 220px;
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

    .price {
      font-size: 20px;
      font-weight: bold;
      color: #ec4899;
    }
  }

  .view-btn {
    width: 100%;
  }
}
</style>
