<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-logo">
        <el-icon :size="28"><Promotion /></el-icon>
        <span>漫化管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="#1e1b4b"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理后台</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="header-user">
            <el-avatar :size="32" :src="userStore.user?.avatarUrl">
              {{ userStore.user?.nickname?.[0] || 'A' }}
            </el-avatar>
            <span class="header-username">{{ userStore.user?.nickname || '管理员' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="home">
                <el-icon><HomeFilled /></el-icon>
                返回主站
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                <span style="color: #f56c6c">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, markRaw } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Promotion, DataLine, User, ShoppingBag, Menu, List,
  MagicStick, Document, ArrowDown, HomeFilled, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const menuItems = [
  { label: '仪表盘', icon: markRaw(DataLine), path: '/admin/dashboard' },
  { label: '用户管理', icon: markRaw(User), path: '/admin/users' },
  { label: '商品管理', icon: markRaw(ShoppingBag), path: '/admin/products' },
  { label: '分类管理', icon: markRaw(Menu), path: '/admin/categories' },
  { label: '订单管理', icon: markRaw(List), path: '/admin/orders' },
  { label: '化妆服务', icon: markRaw(MagicStick), path: '/admin/makeup-services' },
  { label: '审核管理', icon: markRaw(Document), path: '/admin/applications' },
]

const activeMenu = computed(() => route.path)

const titleMap = {
  '/admin/dashboard': '仪表盘',
  '/admin/users': '用户管理',
  '/admin/products': '商品管理',
  '/admin/categories': '分类管理',
  '/admin/orders': '订单管理',
  '/admin/makeup-services': '化妆服务',
  '/admin/applications': '审核管理',
}

const currentTitle = computed(() => titleMap[route.path] || '管理后台')

function handleCommand(command) {
  if (command === 'home') {
    router.push('/')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #1e1b4b;
  display: flex;
  flex-direction: column;

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: white;
    font-size: 18px;
    font-weight: bold;
    background: rgba(255, 255, 255, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  .sidebar-menu {
    border-right: none;
    flex: 1;
    padding-top: 8px;

    :deep(.el-menu-item) {
      height: 50px;
      line-height: 50px;
      margin: 2px 8px;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        background: rgba(168, 85, 247, 0.2) !important;
      }

      &.is-active {
        background: linear-gradient(135deg, #ec4899, #a855f7) !important;
        color: #fff !important;
        font-weight: 500;
      }
    }
  }
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f5f7fa;
}

.admin-header {
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;

  .header-user {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 8px;
    transition: background 0.3s;

    &:hover {
      background: #f5f7fa;
    }

    .header-username {
      font-size: 14px;
      color: #333;
    }
  }
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
