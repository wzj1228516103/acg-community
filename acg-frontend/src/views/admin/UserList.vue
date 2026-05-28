<template>
  <div class="user-list-page">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="用户名/昵称/手机号" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable>
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch :model-value="!row.deleted" @change="(val) => handleStatusChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(role) => handleRoleChange(row, role)">
              <el-button type="primary" link size="small">
                更改角色 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="r in roleOptions" :key="r.value" :command="r.value">
                    {{ r.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersApi, updateUserStatusApi, updateUserRoleApi } from '@/api/admin'

const roleOptions = [
  { value: 0, label: '普通用户' },
  { value: 1, label: '认证化妆师' },
  { value: 2, label: '认证商家' },
  { value: 3, label: '管理员' },
  { value: 4, label: '超级管理员' },
]

const roleTextMap = { 0: 'USER', 1: 'MAKEUP_ARTIST', 2: 'MERCHANT', 3: 'ADMIN', 4: 'SUPER_ADMIN' }
const roleLabelMap = { 0: '普通用户', 1: '认证化妆师', 2: '认证商家', 3: '管理员', 4: '超级管理员' }
const roleTagTypeMap = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'danger', 4: 'danger' }

const roleText = (role) => roleLabelMap[role] || '普通用户'
const roleTagType = (role) => roleTagTypeMap[role] || 'info'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({ keyword: '', role: '' })

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...(searchForm.keyword && { keyword: searchForm.keyword }),
      ...(searchForm.role !== '' && { role: searchForm.role }),
    }
    const res = await getUsersApi(params)
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
  searchForm.role = ''
  page.value = 1
  loadData()
}

async function handleStatusChange(row, val) {
  const deleted = val ? 0 : 1
  try {
    await ElMessageBox.confirm(`确定要${val ? '启用' : '禁用'}该用户吗？`, '提示', { type: 'warning' })
    await updateUserStatusApi(row.id, deleted)
    ElMessage.success('操作成功')
    loadData()
  } catch {
    // cancelled
  }
}

async function handleRoleChange(row, role) {
  try {
    await ElMessageBox.confirm(`确定要将该用户角色更改为「${roleLabelMap[role]}」吗？`, '提示', { type: 'warning' })
    await updateUserRoleApi(row.id, role)
    ElMessage.success('角色更改成功')
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.user-list-page {
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
}
</style>
