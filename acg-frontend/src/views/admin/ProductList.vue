<template>
  <div class="product-list-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openDialog()">新增商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="getFirstImage(row.images)"
              :src="getFirstImage(row.images)"
              :preview-src-list="getAllImages(row.images)"
              fit="cover"
              class="product-thumb"
              preview-teleported
            >
              <template #error>
                <div class="image-error"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
            <span v-else class="no-image">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ row.categoryName || '-' }}</template>
        </el-table-column>
        <el-table-column label="商家" width="120">
          <template #default="{ row }">{{ row.merchantName || '-' }}</template>
        </el-table-column>
        <el-table-column label="描述" min-width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.description" placement="top" :show-after="500" :disabled="!row.description">
              <span class="desc-text">{{ row.description || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">{{ row.status === 0 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button :type="row.status === 0 ? 'warning' : 'success'" link size="small" @click="handleToggle(row)">
              {{ row.status === 0 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="600px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="商品名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="商品描述" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="选择分类">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片">
          <el-input v-model="formData.imagesStr" type="textarea" :rows="3" placeholder='JSON数组，如：["url1","url2"]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getAdminProductsApi, createProductApi, updateProductApi, updateProductStatusApi, getCategoriesApi } from '@/api/admin'

function parseImages(images) {
  if (!images) return []
  if (Array.isArray(images)) return images.filter(u => u && u.startsWith('http'))
  if (typeof images === 'string') {
    try {
      const arr = JSON.parse(images)
      if (Array.isArray(arr)) return arr.filter(u => u && u.startsWith('http'))
    } catch {
      return []
    }
  }
  return []
}

function getFirstImage(images) {
  const list = parseImages(images)
  return list.length > 0 ? list[0] : ''
}

function getAllImages(images) {
  return parseImages(images)
}

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const categoryList = ref([])

const searchForm = reactive({ keyword: '', categoryId: '' })

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const formData = reactive({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  categoryId: '',
  imagesStr: '[]',
})

const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

async function loadCategories() {
  try {
    const res = await getCategoriesApi()
    categoryList.value = res.data || []
  } catch {
    // handled
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...(searchForm.keyword && { keyword: searchForm.keyword }),
      ...(searchForm.categoryId && { categoryId: searchForm.categoryId }),
    }
    const res = await getAdminProductsApi(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  page.value = 1
  loadData()
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    formData.name = row.name
    formData.description = row.description || ''
    formData.price = row.price
    formData.stock = row.stock
    formData.categoryId = row.categoryId
    formData.imagesStr = row.images ? JSON.stringify(row.images) : '[]'
  } else {
    isEdit.value = false
    editId.value = null
    formData.name = ''
    formData.description = ''
    formData.price = 0
    formData.stock = 0
    formData.categoryId = ''
    formData.imagesStr = '[]'
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  let images = []
  try {
    images = JSON.parse(formData.imagesStr)
    if (!Array.isArray(images)) images = []
  } catch {
    images = []
  }

  const data = {
    name: formData.name,
    description: formData.description,
    price: formData.price,
    stock: formData.stock,
    categoryId: formData.categoryId,
    images,
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateProductApi(editId.value, data)
      ElMessage.success('编辑成功')
    } else {
      await createProductApi(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

async function handleToggle(row) {
  const newStatus = row.status === 0 ? 1 : 0
  const label = newStatus === 0 ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${label}该商品吗？`, '提示', { type: 'warning' })
    await updateProductStatusApi(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<style lang="scss" scoped>
.product-list-page {
  .search-card {
    margin-bottom: 16px;
    border-radius: 12px;

    :deep(.el-card__body) {
      padding-bottom: 4px;
    }
  }

  .table-card {
    border-radius: 12px;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .desc-text {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    color: #606266;
    max-width: 200px;
  }

  .product-thumb {
    width: 60px;
    height: 60px;
    border-radius: 6px;
  }

  .image-error {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 6px;
    color: #c0c4cc;
    font-size: 20px;
  }

  .no-image {
    font-size: 12px;
    color: #c0c4cc;
  }
}
</style>
