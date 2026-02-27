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
      iconTone: 'icon-emerald',
    },
    {
      title: '采购总额',
      value: `${formatAmount(data.value?.purchaseAmount)} 元`,
      note: '原料与辅料累计支出',
      icon: ShoppingCart,
      tone: 'tone-amber',
      iconTone: 'icon-amber',
    },
    {
      title: '销售总额',
      value: `${formatAmount(data.value?.salesAmount)} 元`,
      note: '订单成交累计金额',
      icon: CircleDollarSign,
      tone: 'tone-rose',
      iconTone: 'icon-rose',
    },
    {
      title: '库存总值',
      value: `${formatAmount(data.value?.inventoryValue)} 元`,
      note: '原料与成品账面价值',
      icon: Boxes,
      tone: 'tone-sky',
      iconTone: 'icon-sky',
    },
  ]
})

const moduleCards = computed(() => {
  return [
    { label: '用户', value: data.value?.userCount || 0, icon: Users, tone: 'module-sky' },
    { label: '订单', value: data.value?.orderCount || 0, icon: BarChart3, tone: 'module-indigo' },
    { label: '配送', value: data.value?.deliveryCount || 0, icon: Truck, tone: 'module-cyan' },
    { label: '生产流程', value: data.value?.productionCount || 0, icon: Factory, tone: 'module-slate' },
    { label: '质检记录', value: data.value?.qualityCount || 0, icon: ShieldCheck, tone: 'module-emerald' },
    { label: '库存条目', value: data.value?.inventoryCount || 0, icon: Boxes, tone: 'module-blue' },
    { label: '任务', value: data.value?.taskCount || 0, icon: ListTodo, tone: 'module-violet' },
    { label: '文档', value: data.value?.documentCount || 0, icon: Folder, tone: 'module-amber' },
    { label: '公告', value: data.value?.noticeCount || 0, icon: Bell, tone: 'module-rose' },
    { label: '追溯记录', value: data.value?.traceRecordCount || 0, icon: FileSearch, tone: 'module-teal' },
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
  return normalizedTrend.value.labels.map((label, index) => ({
    label: label.slice(5),
    orderCount: Number(normalizedTrend.value.orderCountTrend[index] || 0),
    passRate: Number(normalizedTrend.value.qualityPassRateTrend[index] || 0),
  }))
})

const formatMonthKey = (date: Date) => {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  return `${year}-${month}`
}

const normalizeMonthKey = (value: string) => {
  const m = value.match(/^(\d{4})-(\d{1,2})/)
  if (!m?.[1] || !m?.[2]) return value
  return `${m[1]}-${m[2].padStart(2, '0')}`
}

const monthIndexOf = (value: string) => {
  const normalized = normalizeMonthKey(value)
  const m = normalized.match(/^(\d{4})-(\d{2})$/)
  if (!m?.[1] || !m?.[2]) return null
  const year = Number(m[1])
  const month = Number(m[2])
  if (!Number.isFinite(year) || !Number.isFinite(month) || month < 1 || month > 12) return null
  return year * 12 + (month - 1)
}

const monthKeyFromIndex = (index: number) => {
  const year = Math.floor(index / 12)
  const month = (index % 12) + 1
  return `${year}-${`${month}`.padStart(2, '0')}`
}

const recentMonthKeys = (count: number) => {
  const result: string[] = []
  const now = new Date()
  for (let i = count - 1; i >= 0; i--) {
    result.push(formatMonthKey(new Date(now.getFullYear(), now.getMonth() - i, 1)))
  }
  return result
}

const normalizedTrend = computed(() => {
  const trend = monthlyTrend.value
  const keys = recentMonthKeys(12)
  const sourceLabels = trend?.labels || []
  const currentIndex = monthIndexOf(formatMonthKey(new Date())) ?? 0
  const sourceIndexes = sourceLabels.map(monthIndexOf).filter((item): item is number => item !== null)
  const sourceMaxIndex = sourceIndexes.length ? Math.max(...sourceIndexes) : currentIndex
  const futureOffset = Math.max(0, sourceMaxIndex - currentIndex)
  const labelIndexMap = new Map<string, number>()

  sourceLabels.forEach((rawLabel, index) => {
    const normalized = normalizeMonthKey(rawLabel)
    const idx = monthIndexOf(normalized)
    if (idx === null) return
    const rebased = monthKeyFromIndex(idx - futureOffset)
    labelIndexMap.set(rebased, index)
  })

  const pick = (arr: Array<number | string | null | undefined>) =>
    keys.map((key) => {
      const idx = labelIndexMap.get(key)
      return idx === undefined ? 0 : Number(arr[idx] || 0)
    })

  return {
    labels: keys,
    purchaseAmountTrend: pick(trend?.purchaseAmountTrend || []),
    salesAmountTrend: pick(trend?.salesAmountTrend || []),
    productionQtyTrend: pick(trend?.productionQtyTrend || []),
    orderCountTrend: pick(trend?.orderCountTrend || []),
    qualityPassRateTrend: pick(trend?.qualityPassRateTrend || []),
  }
})

const lineSeries = computed(() => {
  return [
    {
      key: 'purchase',
      name: '采购额',
      color: '#0ea5e9',
      icon: ShoppingCart,
      data: normalizedTrend.value.purchaseAmountTrend.map((item) => Number(item || 0)),
    },
    {
      key: 'sales',
      name: '销售额',
      color: '#f97316',
      icon: CircleDollarSign,
      data: normalizedTrend.value.salesAmountTrend.map((item) => Number(item || 0)),
    },
    {
      key: 'production',
      name: '产量',
      color: '#10b981',
      icon: Factory,
      data: normalizedTrend.value.productionQtyTrend.map((item) => Number(item || 0)),
    },
  ]
})

const chartMax = computed(() => {
  const allValues = lineSeries.value.flatMap((series) => series.data)
  return Math.max(1, ...allValues)
})

const yTicks = computed(() => {
  const max = chartMax.value
  return [1, 0.75, 0.5, 0.25, 0].map((ratio) => {
    const value = Math.round(max * ratio)
    return {
      ratio,
      value,
      label: value >= 10000 ? `${Math.round(value / 1000) / 10}k` : `${value}`,
    }
  })
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

const toAreaPath = (values: number[]) => {
  const points = toLinePoints(values)
  if (!points.length) return ''
  const first = points[0]!
  const last = points[points.length - 1]!
  const polyline = points.map((point) => `${point.x},${point.y}`).join(' ')
  return `M ${first.x} 32 L ${polyline} L ${last.x} 32 Z`
}

const endPointOf = (values: number[]) => {
  const points = toLinePoints(values)
  return points.length ? points[points.length - 1]! : null
}

const latestValue = (key: 'purchase' | 'sales' | 'production') => {
  const series = lineSeries.value.find((item) => item.key === key)
  const arr = series?.data || []
  return Number(arr[arr.length - 1] || 0)
}

const prevValue = (key: 'purchase' | 'sales' | 'production') => {
  const series = lineSeries.value.find((item) => item.key === key)
  const arr = series?.data || []
  return Number(arr[arr.length - 2] || 0)
}

const ratioText = (curr: number, prev: number) => {
  if (!prev) return curr ? '+100%' : '0%'
  const ratio = ((curr - prev) / prev) * 100
  const fixed = Math.abs(ratio).toFixed(1)
  return `${ratio >= 0 ? '+' : '-'}${fixed}%`
}

const trendSnapshot = computed(() => {
  const purchase = latestValue('purchase')
  const sales = latestValue('sales')
  const production = latestValue('production')
  return [
    {
      label: '本月采购额',
      value: formatAmount(purchase),
      trend: ratioText(purchase, prevValue('purchase')),
      tone: 'snap-sky',
    },
    {
      label: '本月销售额',
      value: formatAmount(sales),
      trend: ratioText(sales, prevValue('sales')),
      tone: 'snap-amber',
    },
    {
      label: '本月产量',
      value: formatNumber(production),
      trend: ratioText(production, prevValue('production')),
      tone: 'snap-emerald',
    },
  ]
})

const barHeight = (value: number) => {
  return `${Math.max(6, Math.round((value / orderMax.value) * 100))}%`
}

const rateTone = (passRate: number) => {
  if (passRate >= 95) return 'high'
  if (passRate >= 90) return 'mid'
  return 'low'
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
        <h2>互助青稞酒产业链数据中心</h2>
        <p class="hero-sub">覆盖采购、生产、销售、协同与追溯关键指标</p>
      </div>
      <button class="refresh-btn" :disabled="loading" @click="load">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </section>

    <section class="headline-grid">
      <article v-for="card in headlineCards" :key="card.title" class="headline-card card" :class="card.tone">
        <div class="headline-icon" :class="card.iconTone">
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
            <span class="module-icon" :class="item.tone">
              <component :is="item.icon" :size="14" />
            </span>
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
          <h3 class="chart-title">
            <span class="title-icon title-icon-sky">
              <BarChart3 :size="14" />
            </span>
            月度经营趋势
          </h3>
          <span>近 12 个月</span>
        </div>
        <div class="legend-row">
          <span v-for="series in lineSeries" :key="series.key" class="legend-item">
            <span class="legend-icon" :style="{ color: series.color, borderColor: `${series.color}55` }">
              <component :is="series.icon" :size="12" />
            </span>
            <i class="legend-line" :style="{ backgroundColor: series.color }"></i>
            {{ series.name }}
          </span>
        </div>
        <div class="line-chart-wrap">
          <div class="y-axis-row">
            <span v-for="tick in yTicks" :key="`tick-${tick.ratio}`">{{ tick.label }}</span>
          </div>
          <svg viewBox="0 0 100 36" preserveAspectRatio="none" class="line-chart-svg">
            <g class="grid-group">
              <line v-for="tick in yTicks" :key="`grid-${tick.ratio}`" x1="4" :y1="4 + tick.ratio * 28" x2="96" :y2="4 + tick.ratio * 28" class="grid-line" />
            </g>
            <line x1="4" y1="32" x2="96" y2="32" class="axis-line" />
            <path
              v-for="series in lineSeries"
              :key="`area-${series.key}`"
              :d="toAreaPath(series.data)"
              :fill="series.color"
              class="area-fill"
            />
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
            <g v-for="series in lineSeries" :key="`dots-${series.key}`">
              <circle
                v-for="(point, idx) in toLinePoints(series.data)"
                :key="`dot-${series.key}-${idx}`"
                :cx="point.x"
                :cy="point.y"
                r="0.62"
                :fill="series.color"
                class="line-dot"
              />
              <text
                v-if="endPointOf(series.data)"
                :x="(endPointOf(series.data)?.x || 0) - 0.8"
                :y="(endPointOf(series.data)?.y || 0) - 1.2"
                class="line-end-label"
                :fill="series.color"
              >
                {{ formatNumber(series.data[series.data.length - 1] || 0) }}
              </text>
            </g>
          </svg>
          <div class="x-axis-row">
            <span v-for="item in monthlyRows" :key="item.label">{{ item.label }}</span>
          </div>
        </div>
        <div class="trend-snapshot">
          <article v-for="item in trendSnapshot" :key="item.label" class="snapshot-item" :class="item.tone">
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
            <span>{{ item.trend }}</span>
          </article>
        </div>
      </article>

      <article class="card chart-card">
        <div class="section-head">
          <h3 class="chart-title">
            <span class="title-icon title-icon-emerald">
              <ShieldCheck :size="14" />
            </span>
            订单与质检通过率
          </h3>
          <span>月度对比</span>
        </div>
        <div class="bar-chart-wrap">
          <article
            v-for="row in monthlyRows"
            :key="`bar-${row.label}`"
            class="bar-item"
            :class="`bar-item-${rateTone(row.passRate)}`"
          >
            <span class="bar-month-chip">{{ row.label }}</span>
            <span
              class="bar-icon-badge"
              :class="`bar-icon-${rateTone(row.passRate)}`"
            >
              <ShieldCheck :size="12" />
            </span>
            <div class="bar-slot">
              <div class="bar-fill" :class="`bar-fill-${rateTone(row.passRate)}`" :style="{ height: barHeight(row.orderCount) }"></div>
            </div>
            <strong class="bar-order">{{ row.orderCount }}</strong>
            <span class="bar-order-label">订单量</span>
            <span
              class="pass-rate"
              :class="`pass-rate-${rateTone(row.passRate)}`"
            >
              通过率 {{ row.passRate.toFixed(0) }}%
            </span>
            <span class="pass-rate-track">
              <i :class="`pass-rate-track-fill pass-rate-track-fill-${rateTone(row.passRate)}`" :style="{ width: `${Math.max(0, Math.min(100, row.passRate))}%` }"></i>
            </span>
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
  border: 1px solid rgba(148, 163, 184, 0.22);
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.08);
}

.headline-icon.icon-emerald {
  color: #047857;
  background: linear-gradient(160deg, rgba(16, 185, 129, 0.2), rgba(255, 255, 255, 0.86));
}

.headline-icon.icon-amber {
  color: #b45309;
  background: linear-gradient(160deg, rgba(245, 158, 11, 0.22), rgba(255, 255, 255, 0.86));
}

.headline-icon.icon-rose {
  color: #be123c;
  background: linear-gradient(160deg, rgba(244, 63, 94, 0.2), rgba(255, 255, 255, 0.86));
}

.headline-icon.icon-sky {
  color: #0369a1;
  background: linear-gradient(160deg, rgba(14, 165, 233, 0.2), rgba(255, 255, 255, 0.86));
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
  border: 1px solid #d8e3f1;
  background: linear-gradient(165deg, #f8fbff, #f3f8ff);
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.module-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(30, 64, 120, 0.1);
}

.module-left {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #334155;
  font-size: 13px;
}

.module-icon {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(148, 163, 184, 0.25);
  color: #1e3a8a;
  background: #fff;
}

.module-icon.module-sky { color: #0284c7; background: #e0f2fe; }
.module-icon.module-indigo { color: #3730a3; background: #e0e7ff; }
.module-icon.module-cyan { color: #0e7490; background: #cffafe; }
.module-icon.module-slate { color: #334155; background: #e2e8f0; }
.module-icon.module-emerald { color: #047857; background: #d1fae5; }
.module-icon.module-blue { color: #1d4ed8; background: #dbeafe; }
.module-icon.module-violet { color: #6d28d9; background: #ede9fe; }
.module-icon.module-amber { color: #b45309; background: #fef3c7; }
.module-icon.module-rose { color: #be123c; background: #ffe4e6; }
.module-icon.module-teal { color: #0f766e; background: #ccfbf1; }

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
  color: #334155;
  padding: 3px 8px 3px 5px;
  border-radius: 999px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.legend-icon {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 1px solid;
}

.legend-line {
  width: 12px;
  height: 3px;
  border-radius: 999px;
  display: inline-block;
}

.line-chart-wrap {
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fafc, #f1f5f9);
  border: 1px solid #e2e8f0;
  padding: 10px 10px 8px;
  display: grid;
  grid-template-columns: 36px 1fr;
  column-gap: 8px;
  align-items: stretch;
}

.y-axis-row {
  display: grid;
  grid-template-rows: repeat(5, 1fr);
  align-items: center;
  justify-items: end;
  color: #64748b;
  font-size: 10px;
  padding-top: 3px;
}

.line-chart-svg {
  width: 100%;
  height: 180px;
}

.axis-line {
  stroke: #cbd5e1;
  stroke-width: 0.8;
}

.grid-line {
  stroke: rgba(148, 163, 184, 0.28);
  stroke-width: 0.45;
  stroke-dasharray: 1.2 1.2;
}

.area-fill {
  opacity: 0.08;
}

.line-dot {
  opacity: 0.96;
}

.line-end-label {
  font-size: 1.8px;
  font-weight: 700;
}

.x-axis-row {
  grid-column: 2;
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

.trend-snapshot {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.snapshot-item {
  border-radius: 10px;
  padding: 8px 10px;
  border: 1px solid #dbe4f1;
  background: #f8fbff;
}

.snapshot-item p {
  margin: 0;
  color: #64748b;
  font-size: 11px;
}

.snapshot-item strong {
  display: block;
  margin-top: 3px;
  font-size: 16px;
  line-height: 1.15;
}

.snapshot-item span {
  margin-top: 2px;
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
}

.snapshot-item.snap-sky {
  background: linear-gradient(160deg, #eef8ff, #f8fbff);
}

.snapshot-item.snap-sky span {
  color: #0369a1;
}

.snapshot-item.snap-amber {
  background: linear-gradient(160deg, #fff8ec, #fffbf2);
}

.snapshot-item.snap-amber span {
  color: #b45309;
}

.snapshot-item.snap-emerald {
  background: linear-gradient(160deg, #ecfdf5, #f5fffa);
}

.snapshot-item.snap-emerald span {
  color: #047857;
}

.bar-chart-wrap {
  display: grid;
  grid-template-columns: repeat(6, minmax(52px, 1fr));
  gap: 12px;
}

.bar-item {
  position: relative;
  overflow: hidden;
  display: grid;
  justify-items: center;
  gap: 5px;
  padding: 10px 8px 9px;
  border-radius: 14px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.98), rgba(232, 238, 247, 0.88));
  border: 1px solid rgba(203, 213, 225, 0.85);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.bar-item::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 42%;
  opacity: 0.5;
  pointer-events: none;
}

.bar-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(15, 23, 42, 0.1);
}

.bar-item-high {
  border-color: rgba(52, 211, 153, 0.46);
}

.bar-item-high::after {
  background: linear-gradient(180deg, rgba(167, 243, 208, 0), rgba(52, 211, 153, 0.14));
}

.bar-item-mid {
  border-color: rgba(96, 165, 250, 0.45);
}

.bar-item-mid::after {
  background: linear-gradient(180deg, rgba(147, 197, 253, 0), rgba(96, 165, 250, 0.14));
}

.bar-item-low {
  border-color: rgba(251, 191, 36, 0.52);
}

.bar-item-low::after {
  background: linear-gradient(180deg, rgba(252, 211, 77, 0), rgba(251, 191, 36, 0.16));
}

.bar-month-chip {
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 10px;
  line-height: 1;
  color: #475569;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(203, 213, 225, 0.95);
  border-radius: 999px;
  padding: 3px 7px;
}

.bar-icon-badge {
  width: 22px;
  height: 22px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  color: #0284c7;
  background: linear-gradient(160deg, #dbeafe, #eff6ff);
  border: 1px solid rgba(147, 197, 253, 0.58);
}

.bar-icon-high {
  color: #0f766e;
  background: linear-gradient(160deg, #ccfbf1, #f0fdfa);
  border-color: rgba(45, 212, 191, 0.55);
}

.bar-icon-mid {
  color: #0369a1;
  background: linear-gradient(160deg, #dbeafe, #eff6ff);
  border-color: rgba(56, 189, 248, 0.52);
}

.bar-icon-low {
  color: #b45309;
  background: linear-gradient(160deg, #fef3c7, #fffbeb);
  border-color: rgba(245, 158, 11, 0.5);
}

.bar-slot {
  width: 28px;
  height: 112px;
  padding: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(191, 219, 254, 0.78), rgba(226, 232, 240, 0.9));
  display: flex;
  align-items: flex-end;
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(148, 163, 184, 0.28);
}

.bar-fill {
  width: 100%;
  border-radius: 999px;
  background: linear-gradient(180deg, #38bdf8, #0284c7);
  transition: height 0.45s ease;
  position: relative;
}

.bar-fill::after {
  content: '';
  position: absolute;
  left: 18%;
  top: 8%;
  width: 64%;
  height: 18%;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.32);
}

.bar-fill-high {
  background: linear-gradient(180deg, #2dd4bf, #0f766e);
}

.bar-fill-mid {
  background: linear-gradient(180deg, #38bdf8, #0284c7);
}

.bar-fill-low {
  background: linear-gradient(180deg, #f59e0b, #d97706);
}

.bar-item strong {
  font-size: 14px;
}

.bar-order {
  font-size: 34px;
  line-height: 1;
  letter-spacing: -0.02em;
  margin-top: 1px;
}

.bar-order-label {
  font-size: 11px;
  color: #64748b;
  line-height: 1;
}

.pass-rate {
  font-size: 11px;
  width: 100%;
  text-align: center;
  color: #166534;
  background: #dcfce7;
  padding: 5px 6px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-weight: 600;
}

.pass-rate-high {
  color: #166534;
  background: #dcfce7;
  border-color: #86efac;
}

.pass-rate-mid {
  color: #0c4a6e;
  background: #dbeafe;
  border-color: #93c5fd;
}

.pass-rate-low {
  color: #92400e;
  background: #fef3c7;
  border-color: #fcd34d;
}

.pass-rate-track {
  width: 100%;
  height: 4px;
  border-radius: 999px;
  background: rgba(203, 213, 225, 0.62);
  overflow: hidden;
}

.pass-rate-track-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.pass-rate-track-fill-high {
  background: linear-gradient(90deg, #34d399, #10b981);
}

.pass-rate-track-fill-mid {
  background: linear-gradient(90deg, #60a5fa, #0ea5e9);
}

.pass-rate-track-fill-low {
  background: linear-gradient(90deg, #fbbf24, #f59e0b);
}

.chart-title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.title-icon {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: grid;
  place-items: center;
}

.title-icon-sky {
  color: #0369a1;
  background: linear-gradient(160deg, #dbeafe, #eef6ff);
  border: 1px solid #bfdbfe;
}

.title-icon-emerald {
  color: #0f766e;
  background: linear-gradient(160deg, #d1fae5, #ecfdf5);
  border: 1px solid #a7f3d0;
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

  .line-chart-wrap {
    grid-template-columns: 1fr;
  }

  .y-axis-row {
    display: none;
  }

  .x-axis-row {
    grid-column: 1;
  }

  .trend-snapshot {
    grid-template-columns: 1fr;
  }
}
</style>
