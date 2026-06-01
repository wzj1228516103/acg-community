<template>
  <div class="dashboard-page">
    <div class="stat-cards">
      <div v-for="card in statCards" :key="card.key" class="stat-card" :style="{ background: card.bg }">
        <div class="stat-info">
          <div class="stat-number">{{ dashboardData[card.key] ?? 0 }}</div>
          <div class="stat-label">{{ card.label }}</div>
          <div v-if="card.subKey" class="stat-sub">今日 +{{ dashboardData[card.subKey] ?? 0 }}</div>
        </div>
        <el-icon class="stat-icon" :size="48"><component :is="card.icon" /></el-icon>
      </div>
    </div>

    <div class="chart-row">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span class="card-title">近7天用户注册趋势</span>
        </template>
        <div ref="userChartRef" class="chart-container"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span class="card-title">近7天订单趋势</span>
        </template>
        <div ref="orderChartRef" class="chart-container"></div>
      </el-card>
    </div>

    <div class="chart-row">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span class="card-title">近7天销售额趋势</span>
        </template>
        <div ref="amountChartRef" class="chart-container"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span class="card-title">订单状态分布</span>
        </template>
        <div ref="statusChartRef" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { User, ShoppingBag, List, MagicStick, Money, Document } from '@element-plus/icons-vue'
import { getDashboardApi } from '@/api/admin'
import * as echarts from 'echarts'

const dashboardData = ref({
  userCount: 0,
  todayUserCount: 0,
  orderCount: 0,
  todayOrderCount: 0,
  totalAmount: 0,
  productCount: 0,
  makeupServiceCount: 0,
  pendingApplications: 0,
  chartDates: [],
  chartDailyUsers: [],
  chartDailyOrders: [],
  chartDailyAmounts: [],
  orderStatusData: [],
})

const statCards = [
  { key: 'userCount', label: '用户总数', subKey: 'todayUserCount', icon: User, bg: 'linear-gradient(135deg, #ec4899, #a855f7)' },
  { key: 'orderCount', label: '订单总数', subKey: 'todayOrderCount', icon: List, bg: 'linear-gradient(135deg, #a855f7, #6366f1)' },
  { key: 'totalAmount', label: '总销售额', icon: Money, bg: 'linear-gradient(135deg, #f59e0b, #ef4444)' },
  { key: 'productCount', label: '商品总数', icon: ShoppingBag, bg: 'linear-gradient(135deg, #6366f1, #3b82f6)' },
  { key: 'makeupServiceCount', label: '化妆服务数', icon: MagicStick, bg: 'linear-gradient(135deg, #3b82f6, #06b6d4)' },
  { key: 'pendingApplications', label: '待审核申请', icon: Document, bg: 'linear-gradient(135deg, #10b981, #059669)' },
]

const userChartRef = ref(null)
const orderChartRef = ref(null)
const amountChartRef = ref(null)
const statusChartRef = ref(null)

let userChart = null
let orderChart = null
let amountChart = null
let statusChart = null

function initLineChart(el, dates, data, label, color) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' },
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' },
    },
    series: [
      {
        name: label,
        type: 'line',
        smooth: true,
        data: data,
        lineStyle: { width: 3, color },
        itemStyle: { color },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: color.replace(')', ',0.3)').replace('rgb', 'rgba') },
            { offset: 1, color: color.replace(')', ',0.02)').replace('rgb', 'rgba') },
          ]),
        },
        symbol: 'circle',
        symbolSize: 8,
      },
    ],
  })
  return chart
}

function initAmountChart(el, dates, data) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' },
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>${p.seriesName}: ¥${p.value.toFixed(2)}`
      },
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: {
        color: '#666',
        formatter: (v) => '¥' + v,
      },
    },
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: data,
        barWidth: '40%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f59e0b' },
            { offset: 1, color: '#ef4444' },
          ]),
        },
      },
    ],
  })
  return chart
}

function initPieChart(el, data) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' },
    },
    legend: {
      bottom: '5%',
      left: 'center',
      textStyle: { color: '#666' },
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' },
        },
        labelLine: { show: false },
        data: data.map((item, i) => ({
          ...item,
          itemStyle: {
            color: ['#f59e0b', '#3b82f6', '#8b5cf6', '#10b981'][i],
          },
        })),
      },
    ],
  })
  return chart
}

function handleResize() {
  userChart?.resize()
  orderChart?.resize()
  amountChart?.resize()
  statusChart?.resize()
}

async function loadDashboard() {
  try {
    const res = await getDashboardApi()
    dashboardData.value = { ...dashboardData.value, ...res.data }
    await nextTick()
    const d = dashboardData.value
    userChart = initLineChart(userChartRef.value, d.chartDates, d.chartDailyUsers, '新增用户', 'rgb(236, 72, 153)')
    orderChart = initLineChart(orderChartRef.value, d.chartDates, d.chartDailyOrders, '新增订单', 'rgb(99, 102, 241)')
    amountChart = initAmountChart(amountChartRef.value, d.chartDates, d.chartDailyAmounts)
    statusChart = initPieChart(statusChartRef.value, d.orderStatusData)
  } catch {
    // handled
  }
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  userChart?.dispose()
  orderChart?.dispose()
  amountChart?.dispose()
  statusChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  .stat-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 20px;
  }

  .stat-card {
    border-radius: 16px;
    padding: 28px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: white;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    .stat-info {
      .stat-number {
        font-size: 36px;
        font-weight: bold;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.85;
      }

      .stat-sub {
        font-size: 12px;
        opacity: 0.7;
        margin-top: 4px;
      }
    }

    .stat-icon {
      opacity: 0.3;
    }
  }

  .chart-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 20px;
  }

  .chart-card {
    border-radius: 12px;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .chart-container {
      width: 100%;
      height: 320px;
    }
  }
}
</style>
