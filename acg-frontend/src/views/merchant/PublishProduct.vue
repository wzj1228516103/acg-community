<template>
  <div class="publish-page">
    <div class="publish-container">
      <el-card shadow="never" class="publish-card">
        <template #header>
          <div class="card-header">
            <el-button link @click="router.back()">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <span class="title">发布商品</span>
          </div>
        </template>

        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" label-position="top">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item label="商品分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="价格 (元)" prop="price">
                <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="库存" prop="stock">
                <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商品描述" prop="description">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品描述" maxlength="2000" show-word-limit />
          </el-form-item>

          <el-form-item label="商品图片">
            <div class="image-upload">
              <el-upload
                action="/api/upload/image"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                :on-success="handleImageUploadSuccess"
                :on-error="handleImageUploadError"
                accept="image/*"
              >
                <el-button type="primary" size="small">上传图片</el-button>
              </el-upload>
              <div v-if="form.imageUrls.length" class="image-list">
                <div v-for="(url, index) in form.imageUrls" :key="url + index" class="image-item">
                  <el-image :src="url" fit="cover" class="image-thumb" :preview-src-list="form.imageUrls" preview-teleported />
                  <span class="image-index">{{ index + 1 }}{{ index === 0 ? '（封面）' : '' }}</span>
                  <el-button type="danger" :icon="Delete" circle size="small" @click="removeImageUrl(index)" />
                </div>
              </div>
              <div class="image-tip">支持 jpg、png、gif、webp、bmp，单张不超过 10MB；第一张图片将作为封面图</div>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" class="submit-btn">
              发布商品
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete } from '@element-plus/icons-vue'
import { createProductApi, getCategoriesApi } from '@/api/product'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

const form = reactive({
  name: '',
  categoryId: '',
  price: 0,
  stock: 0,
  description: '',
  imageUrls: [],
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: token } : {}
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
}

function removeImageUrl(index) {
  form.imageUrls.splice(index, 1)
}

function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

function handleImageUploadSuccess(response) {
  if (response.code === 200 && response.data) {
    form.imageUrls.push(response.data)
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

function handleImageUploadError() {
  ElMessage.error('图片上传失败，请重试')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const images = form.imageUrls.filter(u => u.trim() !== '')

  submitting.value = true
  try {
    await createProductApi({
      name: form.name,
      categoryId: form.categoryId,
      price: form.price,
      stock: form.stock,
      description: form.description,
      images: JSON.stringify(images),
    })
    ElMessage.success('发布成功，等待管理员审核')
    router.push('/profile')
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getCategoriesApi()
    categories.value = res.data || []
  } catch {
    // handled
  }
})
</script>

<style lang="scss" scoped>
.publish-page {
  padding: 32px 24px 64px;
  min-height: calc(100vh - 64px);
  background: #f8fafc;
}

.publish-container {
  max-width: 720px;
  margin: 0 auto;
}

.publish-card {
  border-radius: 16px;
  border: none;

  .card-header {
    display: flex;
    align-items: center;
    gap: 16px;

    .title {
      font-size: 20px;
      font-weight: 600;
      color: #1a1a1a;
    }
  }
}

.image-upload {
  width: 100%;

  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 12px;
  }

  .image-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    background: #fff;
  }

  .image-thumb {
    width: 72px;
    height: 72px;
    border-radius: 6px;
  }

  .image-index {
    font-size: 12px;
    color: #606266;
  }

  .image-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}

.submit-btn {
  width: 200px;
  height: 44px;
  font-size: 16px;
  border-radius: 10px;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  border: none;

  &:hover {
    opacity: 0.9;
  }
}
</style>
