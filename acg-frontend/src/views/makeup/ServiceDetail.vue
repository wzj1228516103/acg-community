<template>
  <div class="service-detail-page">
    <div class="detail-container">
      <div class="detail-main">
        <div class="detail-left">
          <div class="main-image">
            <img :src="currentImage" :alt="service.name" />
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
          <h1 class="service-name">{{ service.name }}</h1>
          <div class="service-price">¥{{ service.price }}</div>

          <div class="info-row">
            <span class="label">服务时长</span>
            <span class="value">{{ service.duration }} 分钟</span>
          </div>

          <div class="artist-section">
            <div class="artist-info">
              <el-avatar :size="48" :src="service.artistAvatar">
                {{ service.artistNickname?.[0] || 'M' }}
              </el-avatar>
              <div class="artist-detail">
                <span class="artist-name">{{ service.artistNickname || '化妆师' }}</span>
                <span class="artist-role">认证化妆师</span>
              </div>
            </div>
          </div>

          <el-divider />

          <h3 class="form-title">预约服务</h3>
          <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-width="80px">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="bookingForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="bookingForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="预约日期" prop="date">
              <el-date-picker
                v-model="bookingForm.date"
                type="date"
                placeholder="选择预约日期"
                style="width: 100%"
                :disabled-date="disabledDate"
              />
            </el-form-item>
            <el-form-item label="备注" prop="notes">
              <el-input v-model="bookingForm.notes" type="textarea" :rows="3" placeholder="请输入备注信息（可选）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="large" round :loading="submitting" style="width: 100%;" @click="handleBooking">
                立即预约
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="detail-bottom">
        <el-tabs>
          <el-tab-pane label="服务详情">
            <div class="description-content" v-html="service.description || '暂无服务详情'" />
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
import { getMakeupServiceDetailApi } from '@/api/makeup'
import { createOrderApi } from '@/api/order'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const service = ref({})
const currentImageIndex = ref(0)
const loading = ref(true)
const submitting = ref(false)
const bookingFormRef = ref()

const bookingForm = ref({
  contactName: '',
  phone: '',
  date: '',
  notes: '',
})

const bookingRules = {
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  date: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
}

const imageList = computed(() => {
  try {
    return service.value.images ? JSON.parse(service.value.images) : []
  } catch {
    return []
  }
})

const currentImage = computed(() => {
  return imageList.value[currentImageIndex.value] || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=anime+cosplay+makeup+service+professional&image_size=landscape_4_3'
})

function disabledDate(time) {
  return time.getTime() < Date.now() - 86400000
}

async function handleBooking() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await bookingFormRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await createOrderApi({
      serviceId: service.value.id,
      contactName: bookingForm.value.contactName,
      phone: bookingForm.value.phone,
      appointmentDate: bookingForm.value.date,
      notes: bookingForm.value.notes,
      type: 'makeup',
    })
    ElMessage.success('预约成功！')
    router.push('/orders')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getMakeupServiceDetailApi(route.params.id)
    service.value = res.data || {}
  } catch {
    ElMessage.error('获取服务信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.service-detail-page {
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
    aspect-ratio: 4 / 3;
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

  .service-name {
    font-size: 24px;
    font-weight: bold;
    color: #1f2937;
    margin-bottom: 16px;
  }

  .service-price {
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
      width: 80px;
      font-size: 14px;
      color: #6b7280;
      flex-shrink: 0;
    }

    .value {
      font-size: 14px;
      color: #1f2937;
    }
  }

  .artist-section {
    margin-bottom: 16px;

    .artist-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background: linear-gradient(135deg, rgba(236, 72, 153, 0.06), rgba(168, 85, 247, 0.06));
      border-radius: 12px;
    }

    .artist-detail {
      display: flex;
      flex-direction: column;

      .artist-name {
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
      }

      .artist-role {
        font-size: 12px;
        color: #a855f7;
        margin-top: 2px;
      }
    }
  }

  .form-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
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
