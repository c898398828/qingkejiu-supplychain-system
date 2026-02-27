<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  BarChart3,
  CircleDollarSign,
  Boxes,
  Sprout,
  Factory,
  ShieldCheck,
  ShoppingCart,
  Truck,
  FileSearch,
  Users,
  Bell,
  Folder,
  ListTodo,
} from 'lucide-vue-next'
import {
  huzhuAnalysisService,
  huzhuDashboardService,
  type HuzhuDashboardOverview,
  type HuzhuMonthlyTrend,
  type HuzhuProductionAnalysis,
  type HuzhuSalesAnalysis,
  type HuzhuSupplyChainAnalysis,
} from '@/services/api'

const loading = ref(false)
const data = ref<HuzhuDashboardOverview | null>(null)
const production = ref<HuzhuProductionAnalysis | null>(null)
const sales = ref<HuzhuSalesAnalysis | null>(null)
const supplyChain = ref<HuzhuSupplyChainAnalysis | null>(null)
const monthlyTrend = ref<HuzhuMonthlyTrend | null>(null)

const formatNumber = (value: number | string | undefined | null) => {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toLocaleString('zh-CN') : '0'
}

const formatAmount = (value: number | string | undefined | null) => {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toLocaleString('zh-CN', { maximumFractionDigits: 2 }) : '0'
}

const headlineCards = computed(() => {
  return [
    {
      title: '种植总面积',
      value: `${formatNumber(data.value?.plantingArea)} 亩`,
      note: '高原基地耕作规模',
      icon: Sprout,
      tone: 'tone-emerald',
    },
    {
      title: '采购总额',
      value: `${formatAmount(data.value?.purchaseAmount)} 元`,
      note: '原料与辅料累计支出',
      icon: ShoppingCart,
      tone: 'tone-amber',
    },
    {
      title: '销售总额',
      value: `${formatAmount(data.value?.salesAmount)} 元`,
      note: '订单成交累计金额',
      icon: CircleDollarSign,
      tone: 'tone-rose',
    },
    {
      title: '库存总值',
      value: `${formatAmount(data.value?.inventoryValue)} 元`,
      note: '原料与成品账面价值',
      icon: Boxes,
      tone: 'tone-sky',
    },
  ]
})

const moduleCards = computed(() => {
  return [
    { label: '用户', value: data.value?.userCount || 0, icon: Users },
    { label: '订单', value: data.value?.orderCount || 0, icon: BarChart3 },
    { label: '配送', value: data.value?.deliveryCount || 0, icon: Truck },
    { label: '生产流程', value: data.value?.productionCount || 0, icon: Factory },
    { label: '质检记录', value: data.value?.qualityCount || 0, icon: ShieldCheck },
    { label: '库存条目', value: data.value?.inventoryCount || 0, icon: Boxes },
    { label: '任务', value: data.value?.taskCount || 0, icon: ListTodo },
    { label: '文档', value: data.value?.documentCount || 0, icon: Folder },
    { label: '公告', value: data.value?.noticeCount || 0, icon: Bell },
    { label: '追溯记录', value: data.value?.traceRecordCount || 0, icon: FileSearch },
  ]
})

const finishedRatePercent = computed(() => {
  const rate = Number(production.value?.finishedRate || 0)
  return Math.max(0, Math.min(100, Math.round(rate * 100)))
})

const turnoverPercent = computed(() => {
  const rate = Number(supplyChain.value?.inventoryTurnoverRate || 0)
  const normalized = Math.min(1, Math.max(0, rate))
  return Math.round(normalized * 100)
})

const monthlyRows = computed(() => {
  const labels = monthlyTrend.value?.labels || []
  const orders = monthlyTrend.value?.orderCountTrend || []
  const passRate = monthlyTrend.value?.qualityPassRateTrend || []
  return labels.map((label, index) => ({
    label: label.slice(5),
    orderCount: Number(orders[index] || 0),
    passRate: Number(passRate[index] || 0),
  }))
})

const lineSeries = computed(() => {
  const trend = monthlyTrend.value
  return [
    {
      key: 'purchase',
      name: '采购额',
      color: '#0ea5e9',
      data: (trend?.purchaseAmountTrend || []).map((item) => Number(item || 0)),
    },
    {
      key: 'sales',
      name: '销售额',
      color: '#f97316',
      data: (trend?.salesAmountTrend || []).map((item) => Number(item || 0)),
    },
    {
      key: 'production',
      name: '产量',
      color: '#10b981',
      data: (trend?.productionQtyTrend || []).map((item) => Number(item || 0)),
    },
  ]
})

const chartMax = computed(() => {
  const allValues = lineSeries.value.flatMap((series) => series.data)
  return Math.max(1, ...allValues)
})

const orderMax = computed(() => {
  const values = monthlyRows.value.map((item) => item.orderCount)
  return Math.max(1, ...values)
})

const toLinePoints = (values: number[]) => {
  const width = 100
  const height = 36
  const padX = 4
  const padY = 4
  if (!values.length) {
    return [] as Array<{ x: number; y: number }>
  }
  if (values.length === 1) {
    return [{ x: width / 2, y: height / 2 }]
  }
  const stepX = (width - padX * 2) / (values.length - 1)
  return values.map((value, index) => {
    const x = padX + index * stepX
    const ratio = value / chartMax.value
    const y = height - padY - ratio * (height - padY * 2)
    return { x: Number(x.toFixed(2)), y: Number(y.toFixed(2)) }
  })
}

const toPolyline = (values: number[]) => {
  return toLinePoints(values)
    .map((point) => `${point.x},${point.y}`)
    .join(' ')
}

const barHeight = (value: number) => {
  return `${Math.max(6, Math.round((value / orderMax.value) * 100))}%`
}

const load = async () => {
  loading.value = true
  try {
    const [overviewRes, productionRes, salesRes, supplyRes, trendRes] = await Promise.all([
      huzhuDashboardService.getOverview(),
      huzhuAnalysisService.getProduction(),
      huzhuAnalysisService.getSales(),
      huzhuAnalysisService.getSupplyChain(),
      huzhuAnalysisService.getMonthlyTrend(12),
    ])
    data.value = overviewRes.data
    production.value = productionRes.data
    sales.value = salesRes.data
    supplyChain.value = supplyRes.data
    monthlyTrend.value = trendRes.data
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="dashboard-page">
    <section class="hero card">
      <div>
        <p class="hero-kicker">运营总览</p>
        <h2>互助青稞酒产业链驾驶舱</h2>
        <p class="hero-sub">覆盖采购、生产、销售、协同与追溯关键指标</p>
      </div>
      <button class="refresh-btn" :disabled="loading" @click="load">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </section>

    <section class="headline-grid">
      <article v-for="card in headlineCards" :key="card.title" class="headline-card card" :class="card.tone">
        <div class="headline-icon">
          <component :is="card.icon" :size="18" />
        </div>
        <p class="headline-title">{{ card.title }}</p>
        <strong class="headline-value">{{ card.value }}</strong>
        <p class="headline-note">{{ card.note }}</p>
      </article>
    </section>

    <section class="card">
      <div class="section-head">
        <h3>模块数据总览</h3>
        <span>共 {{ moduleCards.length }} 个模块</span>
      </div>
      <div class="module-grid">
        <article v-for="item in moduleCards" :key="item.label" class="module-item">
          <div class="module-left">
            <component :is="item.icon" :size="17" />
            <span>{{ item.label }}</span>
          </div>
          <strong>{{ formatNumber(item.value) }}</strong>
        </article>
      </div>
    </section>

    <section class="analysis-grid">
      <article class="card analysis-card">
        <div class="section-head">
          <h3>生产效率</h3>
          <span>流程完工占比</span>
        </div>
        <strong class="analysis-number">{{ finishedRatePercent }}%</strong>
        <div class="meter">
          <div class="meter-fill" :style="{ width: `${finishedRatePercent}%` }"></div>
        </div>
        <p class="analysis-note">已完成 {{ production?.finishedTasks || 0 }} / 总数 {{ production?.totalTasks || 0 }}</p>
      </article>

      <article class="card analysis-card">
        <div class="section-head">
          <h3>销售执行</h3>
          <span>订单达成规模</span>
        </div>
        <strong class="analysis-number">{{ formatNumber(sales?.orderCount) }}</strong>
        <p class="analysis-note">累计销售额 {{ formatAmount(sales?.salesAmount) }} 元</p>
      </article>

      <article class="card analysis-card">
        <div class="section-head">
          <h3>供应链周转</h3>
          <span>采购与库存比值</span>
        </div>
        <strong class="analysis-number">{{ Number(supplyChain?.inventoryTurnoverRate || 0).toFixed(2) }}</strong>
        <div class="meter">
          <div class="meter-fill meter-fill-warm" :style="{ width: `${turnoverPercent}%` }"></div>
        </div>
        <p class="analysis-note">库存价值 {{ formatAmount(supplyChain?.inventoryValue) }} 元</p>
      </article>
    </section>

    <section class="chart-grid">
      <article class="card chart-card">
        <div class="section-head">
          <h3>月度经营趋势</h3>
          <span>近 12 个月</span>
        </div>
        <div class="legend-row">
          <span v-for="series in lineSeries" :key="series.key" class="legend-item">
            <i :style="{ backgroundColor: series.color }"></i>
            {{ series.name }}
          </span>
        </div>
        <div class="line-chart-wrap">
          <svg viewBox="0 0 100 36" preserveAspectRatio="none" class="line-chart-svg">
            <line x1="4" y1="32" x2="96" y2="32" class="axis-line" />
            <polyline
              v-for="series in lineSeries"
              :key="series.key"
              :points="toPolyline(series.data)"
              fill="none"
              :stroke="series.color"
              stroke-width="1.8"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <div class="x-axis-row">
            <span v-for="item in monthlyRows" :key="item.label">{{ item.label }}</span>
          </div>
        </div>
      </article>

      <article class="card chart-card">
        <div class="section-head">
          <h3>订单与质检通过率</h3>
          <span>月度对比</span>
        </div>
        <div class="bar-chart-wrap">
          <article v-for="row in monthlyRows" :key="`bar-${row.label}`" class="bar-item">
            <div class="bar-slot">
              <div class="bar-fill" :style="{ height: barHeight(row.orderCount) }"></div>
            </div>
            <strong>{{ row.orderCount }}</strong>
            <span class="bar-label">{{ row.label }}</span>
            <span class="pass-rate">通过率 {{ row.passRate.toFixed(0) }}%</span>
          </article>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 14px;
}

.card {
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.8));
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background:
    radial-gradient(circle at 90% 10%, rgba(14, 165, 233, 0.18), transparent 38%),
    radial-gradient(circle at 12% 80%, rgba(56, 189, 248, 0.12), transparent 42%),
    linear-gradient(160deg, rgba(255, 255, 255, 0.96), rgba(243, 250, 255, 0.86));
}

.hero-kicker {
  margin: 0;
  color: #0284c7;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
}

.hero h2 {
  margin: 6px 0;
  font-size: 26px;
  line-height: 1.2;
}

.hero-sub {
  margin: 0;
  color: #64748b;
}

.refresh-btn {
  border: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, #0284c7, #0ea5e9);
  color: #fff;
  padding: 10px 14px;
  cursor: pointer;
  white-space: nowrap;
}

.refresh-btn:disabled {
  opacity: 0.7;
  cursor: default;
}

.headline-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.headline-card {
  position: relative;
  overflow: hidden;
}

.headline-card::after {
  content: '';
  position: absolute;
  right: -24px;
  top: -20px;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  opacity: 0.35;
}

.tone-emerald::after {
  background: rgba(16, 185, 129, 0.36);
}

.tone-amber::after {
  background: rgba(245, 158, 11, 0.36);
}

.tone-rose::after {
  background: rgba(244, 63, 94, 0.3);
}

.tone-sky::after {
  background: rgba(14, 165, 233, 0.3);
}

.headline-icon {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: rgba(148, 163, 184, 0.16);
  color: #0f172a;
}

.headline-title {
  margin: 10px 0 6px;
  font-size: 14px;
  color: #475569;
}

.headline-value {
  font-size: 30px;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.headline-note {
  margin: 8px 0 0;
  font-size: 12px;
  color: #64748b;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 10px;
}

.section-head h3 {
  margin: 0;
  font-size: 18px;
}

.section-head span {
  color: #64748b;
  font-size: 12px;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 8px;
}

.module-item {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-left {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #334155;
  font-size: 13px;
}

.analysis-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.analysis-card {
  display: grid;
  gap: 8px;
}

.analysis-number {
  font-size: 34px;
  line-height: 1.1;
}

.meter {
  height: 9px;
  border-radius: 999px;
  background: #e2e8f0;
  overflow: hidden;
}

.meter-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #06b6d4, #0ea5e9);
}

.meter-fill-warm {
  background: linear-gradient(90deg, #f59e0b, #f97316);
}

.analysis-note {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 12px;
}

.chart-card {
  overflow: hidden;
}

.legend-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #475569;
}

.legend-item i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.line-chart-wrap {
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
  border: 1px solid #e2e8f0;
  padding: 10px 10px 8px;
}

.line-chart-svg {
  width: 100%;
  height: 180px;
}

.axis-line {
  stroke: #cbd5e1;
  stroke-width: 0.8;
}

.x-axis-row {
  display: grid;
  grid-template-columns: repeat(12, minmax(24px, 1fr));
  gap: 4px;
  margin-top: 4px;
}

.x-axis-row span {
  color: #64748b;
  font-size: 11px;
  text-align: center;
}

.bar-chart-wrap {
  display: grid;
  grid-template-columns: repeat(6, minmax(52px, 1fr));
  gap: 10px;
}

.bar-item {
  display: grid;
  justify-items: center;
  gap: 4px;
  padding: 10px 6px;
  border-radius: 12px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.95), rgba(226, 232, 240, 0.8));
  border: 1px solid rgba(203, 213, 225, 0.85);
}

.bar-slot {
  width: 24px;
  height: 90px;
  border-radius: 999px;
  background: #dbeafe;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, #38bdf8, #0284c7);
  transition: height 0.45s ease;
}

.bar-item strong {
  font-size: 14px;
}

.bar-label {
  font-size: 11px;
  color: #334155;
}

.pass-rate {
  font-size: 11px;
  color: #166534;
  background: #dcfce7;
  padding: 2px 6px;
  border-radius: 999px;
}

@media (max-width: 1080px) {
  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero h2 {
    font-size: 22px;
  }

  .bar-chart-wrap {
    grid-template-columns: repeat(4, minmax(52px, 1fr));
  }

  .x-axis-row {
    grid-template-columns: repeat(6, minmax(24px, 1fr));
  }
}
</style>
