<template>
  <div class="settings-page">
    <div class="settings-container">
      <h1 class="page-title">个人设置</h1>

      <el-card class="settings-card" shadow="never">
        <template #header>
          <span class="card-title">基本信息</span>
        </template>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
          <el-form-item label="头像">
            <div class="avatar-upload">
              <el-avatar :size="80" :src="form.avatarUrl">
                {{ form.nickname?.[0] || 'U' }}
              </el-avatar>
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
              >
                <el-button size="small" round>更换头像</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" round :loading="saving" @click="handleSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="settings-card" shadow="never">
        <template #header>
          <span class="card-title">账户安全</span>
        </template>
        <div class="security-item">
          <div class="security-info">
            <span class="security-label">修改密码</span>
            <span class="security-desc">定期修改密码有助于保护账户安全</span>
          </div>
          <el-button round @click="showPasswordDialog = true">修改密码</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfileApi } from '@/api/user'

const userStore = useUserStore()

const formRef = ref()
const pwdFormRef = ref()
const saving = ref(false)
const changingPwd = ref(false)
const showPasswordDialog = ref(false)

const form = ref({
  nickname: '',
  phone: '',
  avatarUrl: '',
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

function handleAvatarUpload(options) {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatarUrl = e.target.result
  }
  reader.readAsDataURL(options.file)
}

async function handleSave() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    await updateProfileApi(form.value)
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } catch {
    // handled by interceptor
  } finally {
    saving.value = false
  }
}

async function handleChangePassword() {
  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }
  changingPwd.value = true
  try {
    await updateProfileApi({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
    })
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    // handled by interceptor
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
  if (userStore.user) {
    form.value.nickname = userStore.user.nickname || ''
    form.value.phone = userStore.user.phone || ''
    form.value.avatarUrl = userStore.user.avatarUrl || ''
  }
})
</script>

<style lang="scss" scoped>
.settings-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.settings-container {
  max-width: 640px;
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

.settings-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 24px;

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
  }
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .security-info {
    display: flex;
    flex-direction: column;

    .security-label {
      font-size: 15px;
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 4px;
    }

    .security-desc {
      font-size: 13px;
      color: #9ca3af;
    }
  }
}
</style>
