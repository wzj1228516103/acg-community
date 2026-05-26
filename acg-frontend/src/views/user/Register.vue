<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <div class="register-logo">
          <el-icon :size="32"><Promotion /></el-icon>
        </div>
        <h2>注册漫化</h2>
        <p>加入二次元文化综合社区平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名/手机号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（6-20位）" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" show-password @keyup.enter="handleRegister" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" round style="width:100%" @click="handleRegister">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
        <span class="divider">|</span>
        <router-link to="/">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

async function handleRegister() {
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ec4899, #a855f7, #6366f1);
  padding: 24px;
}

.register-card {
  background: white;
  border-radius: 24px;
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 36px;

  .register-logo {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, #ec4899, #a855f7);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin: 0 auto 16px;
  }

  h2 { font-size: 24px; font-weight: bold; color: #1f2937; margin-bottom: 8px; }
  p { font-size: 14px; color: #6b7280; }
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
  margin-top: 24px;

  a { color: #a855f7; font-weight: 500; }
  .divider { margin: 0 12px; color: #d1d5db; }
}
</style>
