<template>
  <div class="makeup-service-list-page">
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="serviceName" label="服务名称" min-width="160" />
        <el-table-column label="化妆师" min-width="120">
          <template #default="{ row }">{{ row.artistName || '-' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100">
          <template #default="{ row }">{{ row.duration }}分钟</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(val) => handleStatusChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMakeupServicesApi, updateMakeupServiceStatusApi } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

async function loadData() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    const res = await getMakeupServicesApi(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleStatusChange(row, val) {
  const status = val ? 1 : 0
  try {
    await updateMakeupServiceStatusApi(row.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch {
    // handled
  }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${label}该化妆服务吗？`, '提示', { type: 'warning' })
    await updateMakeupServiceStatusApi(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.makeup-service-list-page {
  .table-card {
    border-radius: 12px;
  }

  .price-text {
    color: #f56c6c;
    font-weight: 600;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
