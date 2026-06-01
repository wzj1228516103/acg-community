<template>
  <div class="profile-page">
    <div class="profile-container">
      <el-card class="profile-card" shadow="never">
        <div class="user-header">
          <el-avatar :size="80" :src="userStore.user?.avatarUrl">
            {{ userStore.user?.nickname?.[0] || 'U' }}
          </el-avatar>
          <div class="user-info">
            <h2 class="user-name">{{ userStore.user?.nickname || '未设置昵称' }}</h2>
            <div class="user-meta">
              <span class="user-phone">{{ userStore.user?.phone || '未绑定手机' }}</span>
              <el-tag :type="roleTagType" size="small">{{ roleText }}</el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <el-card v-if="isMerchant || isArtist" shadow="never" class="publish-card">
        <div class="publish-section">
          <div class="publish-title">
            <el-icon :size="20"><Promotion /></el-icon>
            <span>快捷发布</span>
          </div>
          <div class="publish-actions">
            <el-button v-if="isMerchant" type="warning" size="large" round @click="router.push('/publish-product')" class="publish-btn merchant-btn">
              <el-icon :size="20"><ShoppingBag /></el-icon>
              发布商品
            </el-button>
            <el-button v-if="isArtist" type="primary" size="large" round @click="router.push('/publish-service')" class="publish-btn artist-btn">
              <el-icon :size="20"><MagicStick /></el-icon>
              发布化妆服务
            </el-button>
          </div>
        </div>
      </el-card>

      <div class="quick-links">
        <div class="link-card" @click="router.push('/orders')">
          <div class="link-icon">
            <el-icon :size="28"><List /></el-icon>
          </div>
          <span class="link-text">我的订单</span>
        </div>
        <div class="link-card" @click="router.push('/orders')">
          <div class="link-icon">
            <el-icon :size="28"><MagicStick /></el-icon>
          </div>
          <span class="link-text">化妆订单</span>
        </div>
        <div class="link-card" @click="router.push('/favorites')">
          <div class="link-icon">
            <el-icon :size="28"><Star /></el-icon>
          </div>
          <span class="link-text">我的收藏</span>
        </div>
        <div class="link-card" @click="router.push('/profile-settings')">
          <div class="link-icon">
            <el-icon :size="28"><Setting /></el-icon>
          </div>
          <span class="link-text">个人设置</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { List, MagicStick, Star, Setting, ShoppingBag, Promotion } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const roleMap = {
  0: '普通用户',
  1: '化妆师',
  2: '商家',
  3: '管理员',
}

const roleTagTypeMap = {
  0: 'info',
  1: 'primary',
  2: 'warning',
  3: 'danger',
}

const roleText = computed(() => roleMap[userStore.user?.role] || '普通用户')
const roleTagType = computed(() => roleTagTypeMap[userStore.user?.role] || 'info')
const isMerchant = computed(() => userStore.user?.role === 2 || userStore.user?.role >= 3)
const isArtist = computed(() => userStore.user?.role === 1 || userStore.user?.role >= 3)
</script>

<style lang="scss" scoped>
.profile-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.profile-container {
  max-width: 640px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 20px;
  border: none;
  margin-bottom: 32px;

  :deep(.el-card__body) {
    padding: 0;
  }

  .user-header {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 40px 32px;
    background: linear-gradient(135deg, #ec4899, #a855f7, #6366f1);
    border-radius: 20px;
    color: white;

    .user-info {
      .user-name {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .user-meta {
        display: flex;
        align-items: center;
        gap: 12px;

        .user-phone {
          font-size: 14px;
          opacity: 0.85;
        }
      }
    }
  }
}

.publish-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;
  overflow: hidden;

  .publish-section {
    .publish-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
    }

    .publish-actions {
      display: flex;
      gap: 16px;
    }

    .publish-btn {
      flex: 1;
      height: 52px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
      }
    }

    .merchant-btn {
      background: linear-gradient(135deg, #f59e0b, #ef4444);
      border: none;
    }

    .artist-btn {
      background: linear-gradient(135deg, #ec4899, #a855f7);
      border: none;
    }
  }
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .link-card {
    background: white;
    border-radius: 16px;
    padding: 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    }

    .link-icon {
      width: 56px;
      height: 56px;
      background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(168, 85, 247, 0.1));
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #a855f7;
    }

    .link-text {
      font-size: 15px;
      font-weight: 500;
      color: #374151;
    }
  }
}
</style>
