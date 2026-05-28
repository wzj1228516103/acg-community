<template>
  <div class="service-detail-page" v-loading="loading">
    <div class="detail-container" v-if="!loading">
      <div class="detail-left">
        <div class="main-image">
          <img :src="currentImage" :alt="service.name" />
          <div class="image-nav" v-if="imageList.length > 1">
            <button @click="currentImageIndex = (currentImageIndex - 1 + imageList.length) % imageList.length" class="nav-btn">&lt;</button>
            <button @click="currentImageIndex = (currentImageIndex + 1) % imageList.length" class="nav-btn">&gt;</button>
          </div>
        </div>
        <div class="thumbnail-list" v-if="imageList.length > 1">
          <img v-for="(img, index) in imageList" :key="index" :src="img"
               :class="{ active: index === currentImageIndex }" @click="currentImageIndex = index" />
        </div>
      </div>
      <div class="detail-right">
        <h1>{{ service.name }}</h1>
        <div class="artist-info">
          <el-avatar :size="48" :src="service.artistAvatar">
            {{ service.artistNickname?.[0] || 'M' }}
          </el-avatar>
          <div class="artist-detail">
            <span class="artist-name">{{ service.artistNickname || '化妆师' }}</span>
            <el-tag size="small" type="success">认证化妆师</el-tag>
          </div>
        </div>
        <div class="service-meta">
          <div class="meta-item">
            <span class="label">价格</span>
            <span class="price">¥{{ service.price }}</span>
          </div>
          <div class="meta-item">
            <span class="label">时长</span>
            <span>{{ service.duration }}分钟</span>
          </div>
        </div>
        <div class="description">
          <h3>服务描述</h3>
          <p>{{ service.description }}</p>
        </div>
        <div class="booking-form">
          <h3>预约服务</h3>
          <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-width="80px">
            <el-form-item label="选择时间" prop="slotId">
              <div v-if="slotsLoading" class="slots-loading">
                <el-icon class="is-loading"><Loading /></el-icon> 加载可用时间...
              </div>
              <div v-else-if="availableSlots.length === 0" class="no-slots">
                暂无可用时间，请联系化妆师设置
              </div>
              <div v-else class="slot-list">
                <div v-for="slot in availableSlots" :key="slot.id"
                     :class="['slot-item', { active: bookingForm.slotId === slot.id }]"
                     @click="bookingForm.slotId = slot.id">
                  <div class="slot-date">{{ formatDate(slot.startTime) }}</div>
                  <div class="slot-time">{{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</div>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="bookingForm.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="bookingForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="bookingForm.notes" type="textarea" :rows="3"
                placeholder="请输入备注信息（可选）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleBooking" round style="width: 100%">
                立即预约
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getMakeupServiceDetailApi, createMakeupBookingApi, getAvailableSlotsApi } from '@/api/makeup'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const service = ref({})
const currentImageIndex = ref(0)
const loading = ref(true)
const slotsLoading = ref(true)
const submitting = ref(false)
const bookingFormRef = ref()
const availableSlots = ref([])

const bookingForm = ref({
  slotId: null,
  contactName: '',
  phone: '',
  notes: '',
})

const bookingRules = {
  slotId: [{ required: true, message: '请选择预约时间段', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const imageList = computed(() => {
  try {
    return service.value.images ? JSON.parse(service.value.images) : []
  } catch {
    return []
  }
})

const currentImage = computed(() => {
  return imageList.value[currentImageIndex.value] || 'https://picsum.photos/seed/cosplay/640/400'
})

function formatDate(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  const m = d.getMonth() + 1
  const day = d.getDate()
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${m}月${day}日 ${weekdays[d.getDay()]}`
}

function formatTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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
    await createMakeupBookingApi({
      serviceId: service.value.id,
      slotId: bookingForm.value.slotId,
      contactName: bookingForm.value.contactName,
      phone: bookingForm.value.phone,
      notes: bookingForm.value.notes,
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
    try {
      const slotRes = await getAvailableSlotsApi(route.params.id)
      availableSlots.value = slotRes.data || []
    } catch {
      availableSlots.value = []
    } finally {
      slotsLoading.value = false
    }
  } catch {
    ElMessage.error('获取服务信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.service-detail-page {
  padding: 32px 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
}

.detail-left {
  .main-image {
    position: relative;
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 16px;

    img {
      width: 100%;
      height: 400px;
      object-fit: cover;
    }

    .image-nav {
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      transform: translateY(-50%);
      display: flex;
      justify-content: space-between;
      padding: 0 12px;

      .nav-btn {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.8);
        border: none;
        cursor: pointer;
        font-size: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .thumbnail-list {
    display: flex;
    gap: 8px;
    overflow-x: auto;

    img {
      width: 80px;
      height: 60px;
      object-fit: cover;
      border-radius: 8px;
      cursor: pointer;
      border: 2px solid transparent;
      transition: border-color 0.3s;

      &.active {
        border-color: #a855f7;
      }
    }
  }
}

.detail-right {
  h1 {
    font-size: 28px;
    font-weight: bold;
    color: #1f2937;
    margin-bottom: 20px;
  }

  .artist-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;

    .artist-detail {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .artist-name {
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
      }
    }
  }

  .service-meta {
    display: flex;
    gap: 32px;
    margin-bottom: 24px;

    .meta-item {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .label {
        font-size: 13px;
        color: #9ca3af;
      }

      .price {
        font-size: 28px;
        font-weight: bold;
        color: #ec4899;
      }

      span:not(.label):not(.price) {
        font-size: 16px;
        color: #1f2937;
      }
    }
  }

  .description {
    margin-bottom: 32px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: #6b7280;
      line-height: 1.8;
    }
  }

  .booking-form {
    background: #f9fafb;
    border-radius: 16px;
    padding: 24px;

    h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
    }
  }
}

.slots-loading {
  color: #9ca3af;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.no-slots {
  color: #9ca3af;
  font-size: 14px;
}

.slot-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;

  .slot-item {
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    padding: 12px 16px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: center;

    &:hover {
      border-color: #c084fc;
    }

    &.active {
      border-color: #a855f7;
      background: linear-gradient(135deg, rgba(168, 85, 247, 0.08), rgba(236, 72, 153, 0.08));
    }

    .slot-date {
      font-size: 13px;
      color: #6b7280;
      margin-bottom: 4px;
    }

    .slot-time {
      font-size: 15px;
      font-weight: 600;
      color: #1f2937;
    }
  }
}
</style>
