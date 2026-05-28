<template>
  <div class="order-list-page">
    <el-card shadow="never" class="table-card">
      <template #header>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane label="待付款" name="PENDING" />
          <el-tab-pane label="已支付" name="PAID" />
          <el-tab-pane label="已发货" name="SHIPPED" />
          <el-tab-pane label="已完成" name="COMPLETED" />
          <el-tab-pane label="已取消" name="CANCELLED" />
        </el-tabs>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe @row-click="viewDetail">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="用户" min-width="120">
          <template #default="{ row }">{{ row.username || row.nickname || '-' }}</template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货人" min-width="120">
          <template #default="{ row }">{{ row.receiverName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click.stop="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PAID'" type="success" link size="small" @click.stop="updateStatus(row, 'SHIPPED')">
              发货
            </el-button>
            <el-button v-if="row.status === 'SHIPPED'" type="success" link size="small" @click.stop="updateStatus(row, 'COMPLETED')">
              完成
            </el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link size="small" @click.stop="updateStatus(row, 'CANCELLED')">
              取消
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

    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentOrder.username || currentOrder.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentOrder.status)" size="small">{{ statusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span class="price-text">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货电话">{{ currentOrder.receiverPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.receiverAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrder.items?.length" class="order-items">
          <h4>商品明细</h4>
          <el-table :data="currentOrder.items" size="small" border>
            <el-table-column prop="productName" label="商品" min-width="150" />
            <el-table-column prop="quantity" label="数量" width="70" />
            <el-table-column label="单价" width="90">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="小计" width="90">
              <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrdersApi, updateOrderStatusApi } from '@/api/admin'

const statusTextMap = { PENDING: '待付款', PAID: '已支付', SHIPPED: '已发货', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusTypeMap = { PENDING: 'warning', PAID: 'primary', SHIPPED: '', COMPLETED: 'success', CANCELLED: 'info' }
const statusText = (s) => statusTextMap[s] || s
const statusTagType = (s) => statusTypeMap[s] || 'info'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('')

const detailVisible = ref(false)
const currentOrder = ref(null)

async function loadData() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...(activeTab.value && { status: activeTab.value }),
    }
    const res = await getOrdersApi(params)
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

function viewDetail(row) {
  currentOrder.value = row
  detailVisible.value = true
}

async function updateStatus(row, status) {
  const label = statusTextMap[status]
  try {
    await ElMessageBox.confirm(`确定要将订单状态更改为「${label}」吗？`, '提示', { type: 'warning' })
    await updateOrderStatusApi(row.id, status)
    ElMessage.success('状态更新成功')
    loadData()
  } catch {
    // cancelled
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.order-list-page {
  .table-card {
    border-radius: 12px;

    :deep(.el-tabs__header) {
      margin-bottom: 0;
    }
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

  .order-items {
    margin-top: 16px;

    h4 {
      margin-bottom: 8px;
      color: #333;
    }
  }
}
</style>
