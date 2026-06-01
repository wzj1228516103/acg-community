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
            <div class="image-urls">
              <div v-for="(url, index) in form.imageUrls" :key="index" class="image-url-row">
                <el-input v-model="form.imageUrls[index]" placeholder="输入图片URL地址" clearable>
                  <template #prepend>{{ index + 1 }}</template>
                </el-input>
                <el-button type="danger" :icon="Delete" circle size="small" @click="removeImageUrl(index)" v-if="form.imageUrls.length > 1" />
              </div>
              <el-button type="primary" link @click="addImageUrl" class="add-url-btn">
                <el-icon><Plus /></el-icon> 添加图片URL
              </el-button>
              <div class="image-tip">请输入图片的URL地址，第一张将作为封面图</div>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Delete, Plus } from '@element-plus/icons-vue'
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
  imageUrls: [''],
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
}

function addImageUrl() {
  form.imageUrls.push('')
}

function removeImageUrl(index) {
  form.imageUrls.splice(index, 1)
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

.image-urls {
  width: 100%;

  .image-url-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;
    align-items: center;
  }

  .add-url-btn {
    margin-top: 4px;
    margin-bottom: 8px;
  }

  .image-tip {
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
