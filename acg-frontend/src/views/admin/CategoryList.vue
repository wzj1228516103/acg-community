<template>
  <div class="category-list-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>分类列表</span>
          <el-button type="success" @click="openDialog()">新增分类</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">{{ row.description || '-' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定要删除该分类吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="分类描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { ElMessage } from 'element-plus'
import { getCategoriesApi, createCategoryApi, updateCategoryApi, deleteCategoryApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const formData = reactive({
  name: '',
  description: '',
  sort: 0,
  status: 1,
})

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCategoriesApi()
    tableData.value = res.data || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    formData.name = row.name
    formData.description = row.description || ''
    formData.sort = row.sort ?? 0
    formData.status = row.status ?? 1
  } else {
    isEdit.value = false
    editId.value = null
    formData.name = ''
    formData.description = ''
    formData.sort = 0
    formData.status = 1
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const data = { ...formData }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateCategoryApi(editId.value, data)
      ElMessage.success('编辑成功')
    } else {
      await createCategoryApi(data)
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

async function handleDelete(id) {
  try {
    await deleteCategoryApi(id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // handled
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.category-list-page {
  .table-card {
    border-radius: 12px;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }
}
</style>
