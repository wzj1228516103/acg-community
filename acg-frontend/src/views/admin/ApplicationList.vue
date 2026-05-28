<template>
  <div class="application-list-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="化妆师申请" name="artist" />
          <el-tab-pane label="商家申请" name="merchant" />
        </el-tabs>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="申请人" min-width="120">
          <template #default="{ row }">{{ row.applicantName || row.username || '-' }}</template>
        </el-table-column>
        <el-table-column label="申请信息" min-width="250">
          <template #default="{ row }">
            <div class="apply-info">
              <p v-if="row.realName">姓名：{{ row.realName }}</p>
              <p v-if="row.phone">电话：{{ row.phone }}</p>
              <p v-if="row.description">说明：{{ row.description }}</p>
              <p v-if="row.reason">理由：{{ row.reason }}</p>
              <p v-if="row.shopName">店铺：{{ row.shopName }}</p>
              <p v-if="row.qualification">资质：{{ row.qualification }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0 || row.status === 'PENDING'">
              <el-button type="success" link size="small" @click="handleReview(row, 'approve')">通过</el-button>
              <el-button type="danger" link size="small" @click="handleReview(row, 'reject')">驳回</el-button>
            </template>
            <span v-else class="processed-text">已处理</span>
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
import {
  getArtistApplicationsApi,
  reviewArtistApplicationApi,
  getMerchantApplicationsApi,
  reviewMerchantApplicationApi,
} from '@/api/admin'

const statusTextMap = { 0: '待审核', 1: '已通过', 2: '已驳回', PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
const statusTypeMap = { 0: 'warning', 1: 'success', 2: 'danger', PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
const statusText = (s) => statusTextMap[s] ?? '未知'
const statusTagType = (s) => statusTypeMap[s] ?? 'info'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('artist')

function getApi() {
  return activeTab.value === 'artist'
    ? { load: getArtistApplicationsApi, review: reviewArtistApplicationApi }
    : { load: getMerchantApplicationsApi, review: reviewMerchantApplicationApi }
}

async function loadData() {
  loading.value = true
  try {
    const { load } = getApi()
    const params = { page: page.value, size: pageSize.value }
    const res = await load(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  page.value = 1
  loadData()
}

async function handleReview(row, action) {
  const label = action === 'approve' ? '通过' : '驳回'
  try {
    await ElMessageBox.confirm(`确定要${label}该申请吗？`, '提示', { type: 'warning' })
    const { review } = getApi()
    await review(row.id, action)
    ElMessage.success(`已${label}`)
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.application-list-page {
  .table-card {
    border-radius: 12px;

    :deep(.el-tabs__header) {
      margin-bottom: 0;
    }
  }

  .apply-info {
    p {
      margin: 0;
      font-size: 13px;
      color: #606266;
      line-height: 1.8;
    }
  }

  .processed-text {
    font-size: 12px;
    color: #909399;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
