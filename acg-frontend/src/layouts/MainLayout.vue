<template>
  <div class="app-layout">
    <header class="app-header">
      <div class="header-container">
        <div class="header-left">
          <router-link to="/" class="logo">
            <div class="logo-icon">
              <el-icon :size="24"><Promotion /></el-icon>
            </div>
            <span class="logo-text">漫化</span>
          </router-link>
          <nav class="nav-links">
            <router-link v-for="link in navLinks" :key="link.path" :to="link.path" class="nav-link">
              {{ link.name }}
            </router-link>
          </nav>
        </div>
        <div class="header-right">
          <template v-if="userStore.isLoggedIn">
            <router-link to="/cart" class="header-action">
              <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
                <el-icon :size="20"><ShoppingCart /></el-icon>
              </el-badge>
            </router-link>
            <router-link to="/chat" class="header-action">
              <el-icon :size="20"><ChatDotSquare /></el-icon>
            </router-link>
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="userStore.user?.avatarUrl">
                  {{ userStore.user?.nickname?.[0] || 'U' }}
                </el-avatar>
                <span class="user-name">{{ userStore.user?.nickname || '用户' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="settings">个人设置</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>管理后台</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <span style="color: #f56c6c">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login">
              <el-button type="primary" round>登录</el-button>
            </router-link>
            <router-link to="/register">
              <el-button round>注册</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view />
    </main>

    <footer class="app-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="footer-logo">
              <el-icon :size="28"><Promotion /></el-icon>
              <span>漫化</span>
            </div>
            <p>专注于二次元文化的综合性社区平台</p>
          </div>
          <div class="footer-links">
            <h4>服务项目</h4>
            <router-link to="/shop">二次元周边商城</router-link>
            <router-link to="/makeup-services">专业化妆师服务</router-link>
            <router-link to="/chat">即时聊天功能</router-link>
          </div>
          <div class="footer-links">
            <h4>用户服务</h4>
            <span>7×24小时服务</span>
            <span>正品保证</span>
            <span>安全可靠</span>
          </div>
        </div>
        <div class="footer-stats">
          <div class="stat-item">
            <span class="stat-number">1000+</span>
            <span class="stat-label">认证化妆师</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">5000+</span>
            <span class="stat-label">精选商品</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">10W+</span>
            <span class="stat-label">活跃用户</span>
          </div>
        </div>
        <div class="footer-copyright">
          © 2025 漫化. All rights reserved.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Promotion, ShoppingCart, ChatDotSquare, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const navLinks = [
  { name: '首页', path: '/' },
  { name: '商城', path: '/shop' },
  { name: '化妆师服务', path: '/makeup-services' },
  { name: '聊天', path: '/chat' },
  { name: '个人中心', path: '/profile' },
]

function handleCommand(command) {
  switch (command) {
    case 'profile': router.push('/profile'); break
    case 'orders': router.push('/orders'); break
    case 'favorites': router.push('/favorites'); break
    case 'settings': router.push('/profile-settings'); break
    case 'admin': router.push('/admin'); break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style lang="scss" scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #ec4899, #a855f7, #6366f1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);

  .header-container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 64px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 48px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    text-decoration: none;
    color: white;

    .logo-icon {
      width: 36px;
      height: 36px;
      background: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #a855f7;
    }

    .logo-text {
      font-size: 24px;
      font-weight: bold;
    }
  }

  .nav-links {
    display: flex;
    gap: 8px;

    .nav-link {
      color: rgba(255, 255, 255, 0.85);
      text-decoration: none;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 15px;
      transition: all 0.3s;

      &:hover, &.router-link-active {
        background: rgba(255, 255, 255, 0.2);
        color: white;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-action {
      color: white;
      text-decoration: none;
      padding: 8px;
      border-radius: 50%;
      transition: background 0.3s;
      display: flex;
      align-items: center;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: white;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 20px;
      transition: background 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }

      .user-name {
        font-size: 14px;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.app-main {
  flex: 1;
  background: #f8fafc;
}

.app-footer {
  background: linear-gradient(135deg, #1e1b4b, #831843, #312e81);
  color: white;
  padding: 48px 0 24px;

  .footer-container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .footer-content {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr;
    gap: 48px;
    margin-bottom: 32px;

    .footer-brand {
      .footer-logo {
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 12px;
      }

      p {
        color: rgba(255, 255, 255, 0.7);
        font-size: 14px;
      }
    }

    .footer-links {
      h4 {
        font-size: 16px;
        margin-bottom: 16px;
      }

      a, span {
        display: block;
        color: rgba(255, 255, 255, 0.7);
        text-decoration: none;
        font-size: 14px;
        margin-bottom: 8px;
        transition: color 0.3s;

        &:hover {
          color: white;
        }
      }
    }
  }

  .footer-stats {
    display: flex;
    justify-content: center;
    gap: 64px;
    padding: 24px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 12px;
    margin-bottom: 24px;

    .stat-item {
      text-align: center;

      .stat-number {
        display: block;
        font-size: 28px;
        font-weight: bold;
        background: linear-gradient(135deg, #f472b6, #c084fc);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .stat-label {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.6);
      }
    }
  }

  .footer-copyright {
    text-align: center;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>
