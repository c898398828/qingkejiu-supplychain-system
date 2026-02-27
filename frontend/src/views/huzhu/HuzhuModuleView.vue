<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, Plus, RotateCcw, Pencil, Trash2, Sparkles, Filter, ChevronLeft, ChevronRight, X, Save, Eye } from 'lucide-vue-next'
import { huzhuCrudService } from '@/services/api'
import { moduleMap, type HuzhuModuleConfig } from '@/lib/huzhu/modules'

const route = useRoute()

const moduleConfig = computed<HuzhuModuleConfig | null>(() => {
  const key = String(route.params.moduleKey || '')
  return moduleMap[key] || null
})
const isUserModule = computed(() => moduleConfig.value?.key === 'users')

const list = ref<any[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const status = ref<string | number | ''>('')

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formData = reactive<Record<string, any>>({})
const dialogError = ref('')
const detailVisibleId = ref<number | null>(null)

const displayColumns = computed(() => {
  if (!moduleConfig.value) return []
  const baseColumns = moduleConfig.value.columns || []
  const hiddenKeys = new Set(['address', 'productCode'])
  const visibleBaseColumns = baseColumns.filter((item) => !hiddenKeys.has(item.key))
  const keySet = new Set(visibleBaseColumns.map((item) => item.key))
  const extraColumns = (moduleConfig.value.formFields || [])
    .filter((field) => !hiddenKeys.has(field.key) && !keySet.has(field.key) && field.type !== 'textarea')
    .map((field) => ({ key: field.key, label: field.label }))

  const maxColumns = 8
  const remain = Math.max(0, maxColumns - visibleBaseColumns.length)
  return [...visibleBaseColumns, ...extraColumns.slice(0, remain)]
})

const roleLabelMap: Record<number, string> = {
  1: '管理员',
  2: '种植管理员',
  3: '生产管理员',
  4: '销售管理员',
}

const statusOptionMap = computed(() => {
  const map = new Map<string, string>()
  const options = moduleConfig.value?.statusOptions || []
  options.forEach((item) => map.set(String(item.value), item.label))
  return map
})

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const hasActiveFilters = computed(() => Boolean(keyword.value || status.value !== ''))

const moduleStats = computed(() => {
  const pageCount = list.value.length
  const enabled = list.value.filter((item) => String(item.status) === '1' || String(item.status).toLowerCase() === 'enabled').length
  return {
    pageCount,
    totalCount: total.value,
    enabledCount: enabled,
  }
})

const resetForm = () => {
  Object.keys(formData).forEach((k) => delete formData[k])
  moduleConfig.value?.formFields.forEach((field) => {
    formData[field.key] = ''
  })
}

const loadData = async () => {
  if (!moduleConfig.value) return
  loading.value = true
  try {
    const params: Record<string, any> = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
    }
    if (moduleConfig.value.statusField && status.value !== '') {
      params[moduleConfig.value.statusField] = status.value
    }

    const res = await huzhuCrudService.page<any>(moduleConfig.value.endpoint, params)
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleResetFilters = () => {
  keyword.value = ''
  status.value = ''
  pageNum.value = 1
  loadData()
}

const openCreate = () => {
  resetForm()
  dialogError.value = ''
  editingId.value = null
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  resetForm()
  dialogError.value = ''
  Object.assign(formData, row)
  editingId.value = row.id
  dialogVisible.value = true
}

const save = async () => {
  if (!moduleConfig.value) return
  dialogError.value = ''
  const requiredField = moduleConfig.value.formFields.find((field) => field.required && String(formData[field.key] ?? '').trim() === '')
  if (requiredField) {
    dialogError.value = `请先填写必填项：${requiredField.label}`
    return
  }
  const payload = { ...formData }
  if (editingId.value) {
    await huzhuCrudService.update(moduleConfig.value.endpoint, editingId.value, payload)
  } else {
    await huzhuCrudService.create(moduleConfig.value.endpoint, payload)
  }
  dialogVisible.value = false
  loadData()
}

const removeItem = async (row: any) => {
  if (!moduleConfig.value) return
  if (!window.confirm('确认删除该记录吗？')) return
  await huzhuCrudService.remove(moduleConfig.value.endpoint, row.id)
  if (detailVisibleId.value === row.id) {
    detailVisibleId.value = null
  }
  loadData()
}

const getFieldValue = (row: any, key: string) => {
  if (!row) return undefined
  const direct = row[key]
  if (direct !== undefined && direct !== null && direct !== '') return direct
  const fallbacks: Record<string, string[]> = {
    totalAmount: ['amount', 'total', 'total_amount'],
    orderDate: ['date', 'order_time', 'order_date', 'createTime'],
  }
  const keys = fallbacks[key] || []
  for (const k of keys) {
    const value = row[k]
    if (value !== undefined && value !== null && value !== '') return value
  }
  return direct
}

const formatCell = (row: any, key: string) => {
  const value = getFieldValue(row, key)
  if (value === null || value === undefined || value === '') return '-'
  if (key === 'totalAmount') {
    const num = Number(value)
    return Number.isFinite(num) ? `${num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })} 元` : String(value)
  }
  if (key.toLowerCase().includes('date') || key === 'createTime' || key === 'updateTime') {
    return String(value).replace('T', ' ').slice(0, 19)
  }
  return value
}

const resolveRoleLabel = (roleId: number | string) => {
  const id = Number(roleId)
  return roleLabelMap[id] || `角色${id || '-'}`
}

const resolveStatusLabel = (statusValue: number | string) => {
  const raw = String(statusValue)
  return statusOptionMap.value.get(raw) || raw || '-'
}

const statusTone = (statusValue: number | string) => {
  const value = String(statusValue).toLowerCase()
  if (
    value === '1' ||
    value.includes('启用') ||
    value.includes('已完成') ||
    value.includes('done') ||
    value.includes('signed') ||
    value.includes('合格') ||
    value.includes('qualified')
  ) {
    return 'status-ok'
  }
  if (
    value === '0' ||
    value.includes('停用') ||
    value.includes('待付款') ||
    value.includes('待支付') ||
    value.includes('pending') ||
    value.includes('异常') ||
    value.includes('不合格') ||
    value.includes('unqualified')
  ) {
    return 'status-warn'
  }
  return 'status-neutral'
}

const skeletonRows = computed(() => Array.from({ length: 5 }, (_, i) => i))
const isRequired = (field: HuzhuModuleConfig['formFields'][number]) => Boolean(field.required)
const placeholderText = (field: HuzhuModuleConfig['formFields'][number]) => {
  if (field.type === 'date') return '请选择日期'
  if (field.type === 'select') return '请选择'
  if (field.type === 'textarea') return `请输入${field.label}`
  return `请输入${field.label}`
}

const toggleDetail = (row: any) => {
  detailVisibleId.value = detailVisibleId.value === row.id ? null : row.id
}

const closeDetail = () => {
  detailVisibleId.value = null
}

const detailLabelMap = computed(() => {
  const map = new Map<string, string>()
  ;(moduleConfig.value?.columns || []).forEach((item) => map.set(item.key, item.label))
  ;(moduleConfig.value?.formFields || []).forEach((item) => map.set(item.key, item.label))
  map.set('id', 'ID')
  map.set('createTime', '创建时间')
  map.set('updateTime', '更新时间')
  return map
})

const getDetailEntries = (row: any) => {
  if (!row || typeof row !== 'object') return []
  const hiddenDetailKeys = new Set<string>()
  const rowKeys = Object.keys(row).filter((key) => {
    const value = row[key]
    return !hiddenDetailKeys.has(key) && typeof value !== 'object' && typeof value !== 'function'
  })
  const orderedKnownKeys = (moduleConfig.value?.formFields || [])
    .map((field) => field.key)
    .filter((key) => !hiddenDetailKeys.has(key))
  const priority = ['id', ...orderedKnownKeys, 'createTime', 'updateTime']
  const prioritySet = new Set(priority)
  const mergedKeys = Array.from(new Set([...orderedKnownKeys, ...rowKeys]))
  const ordered = [
    ...priority.filter((key) => mergedKeys.includes(key)),
    ...mergedKeys.filter((key) => !prioritySet.has(key)),
  ]

  return ordered.map((key) => ({
    key,
    label: detailLabelMap.value.get(key) || key,
    value: formatCell(row, key),
  }))
}

watch(
  () => route.params.moduleKey,
  () => {
    pageNum.value = 1
    keyword.value = ''
    status.value = ''
    resetForm()
    closeDetail()
    loadData()
  },
  { immediate: true },
)

onMounted(resetForm)
</script>

<template>
  <div v-if="moduleConfig" class="module-page">
    <section class="header-card card-float">
      <div>
        <h3>{{ moduleConfig.title }}</h3>
        <p>{{ moduleConfig.teamOwner }}</p>
      </div>
      <button class="btn btn-primary" @click="openCreate">
        <Plus :size="16" />
        <span>新增记录</span>
      </button>
    </section>

    <section class="tool-card card-float">
      <div class="input-wrap">
        <Search :size="16" class="prefix-icon" />
        <input v-model="keyword" :placeholder="`搜索：${moduleConfig.keywordPlaceholder}`" @keyup.enter="loadData" />
      </div>
      <div v-if="moduleConfig.statusOptions" class="input-wrap select-wrap">
        <Filter :size="16" class="prefix-icon" />
        <select v-model="status" @change="loadData">
          <option value="">全部状态</option>
          <option v-for="op in moduleConfig.statusOptions" :key="`${op.value}`" :value="op.value">{{ op.label }}</option>
        </select>
      </div>
      <button class="btn btn-light" :disabled="!hasActiveFilters" @click="handleResetFilters">
        <RotateCcw :size="15" />
        <span>重置</span>
      </button>
      <button class="btn btn-primary" @click="loadData">查询</button>
    </section>

    <section class="stat-strip card-float">
      <article class="stat-pill">
        <span>当前页</span>
        <strong>{{ moduleStats.pageCount }}</strong>
      </article>
      <article class="stat-pill">
        <span>总记录</span>
        <strong>{{ moduleStats.totalCount }}</strong>
      </article>
      <article class="stat-pill" v-if="isUserModule">
        <span>启用用户</span>
        <strong class="ok">{{ moduleStats.enabledCount }}</strong>
      </article>
      <article class="stat-pill hint">
        <Sparkles :size="14" />
        <span>支持分页、筛选、动态编辑</span>
      </article>
    </section>

    <section class="table-card card-float">
      <table>
        <thead>
          <tr>
            <th v-for="col in displayColumns" :key="col.key">{{ col.label }}</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody v-if="loading">
          <tr v-for="row in skeletonRows" :key="`skeleton-${row}`">
            <td v-for="col in displayColumns" :key="`s-${row}-${col.key}`">
              <div class="skeleton-line"></div>
            </td>
            <td>
              <div class="skeleton-line short"></div>
            </td>
          </tr>
        </tbody>
        <tbody v-else-if="!list.length">
          <tr>
            <td :colspan="displayColumns.length + 1">
              <div class="empty-state">
                <Sparkles :size="18" />
                <span>暂无数据，试试调整筛选或新增记录</span>
              </div>
            </td>
          </tr>
        </tbody>
        <tbody v-else>
          <tr
            v-for="(row, rowIndex) in list"
            :key="row.id"
            class="data-row"
            :class="{ 'data-row-detail-open': detailVisibleId === row.id }"
            :style="{ '--row-delay': `${rowIndex * 25}ms` }"
          >
            <td v-for="col in displayColumns" :key="col.key">
              <template v-if="isUserModule && col.key === 'username'">
                <div class="user-cell">
                  <span class="user-dot">{{ String(row.username || 'U').charAt(0).toUpperCase() }}</span>
                  <span>{{ formatCell(row, col.key) }}</span>
                </div>
              </template>
              <template v-else-if="isUserModule && col.key === 'roleId'">
                <span class="role-badge">{{ resolveRoleLabel(row.roleId) }}</span>
              </template>
              <template v-else-if="col.key === 'status'">
                <span class="status-badge" :class="statusTone(row.status)">{{ resolveStatusLabel(row.status) }}</span>
              </template>
              <template v-else>
                {{ formatCell(row, col.key) }}
              </template>
            </td>
            <td>
              <div class="row-actions">
                <button class="btn btn-info btn-sm" @click="toggleDetail(row)">
                  <Eye :size="14" />
                  <span>详情</span>
                </button>
                <button class="btn btn-light btn-sm" @click="openEdit(row)">
                  <Pencil :size="14" />
                  <span>编辑</span>
                </button>
                <button class="btn btn-danger btn-sm" @click="removeItem(row)">
                  <Trash2 :size="14" />
                  <span>删除</span>
                </button>
                <transition name="bubble-fade">
                  <div v-if="detailVisibleId === row.id" class="detail-bubble" @mouseleave="closeDetail">
                    <div class="bubble-head">
                      <strong>{{ moduleConfig.title }}详情</strong>
                      <button class="icon-btn mini" @click="closeDetail">
                        <X :size="12" />
                      </button>
                    </div>
                    <div class="bubble-grid">
                      <div v-for="field in getDetailEntries(row)" :key="field.key" class="bubble-item">
                        <span>{{ field.label }}</span>
                        <em>{{ field.value }}</em>
                      </div>
                    </div>
                  </div>
                </transition>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pager">
        <button class="btn btn-light" :disabled="pageNum <= 1" @click="pageNum -= 1; loadData()">
          <ChevronLeft :size="15" />
          <span>上一页</span>
        </button>
        <span>第 {{ pageNum }} 页 / 共 {{ totalPages }} 页</span>
        <button class="btn btn-light" :disabled="pageNum >= totalPages" @click="pageNum += 1; loadData()">
          <span>下一页</span>
          <ChevronRight :size="15" />
        </button>
      </div>
    </section>

    <transition name="dialog-fade">
      <div v-if="dialogVisible" class="dialog-mask" @click.self="dialogVisible = false">
        <div class="dialog">
          <div class="dialog-head">
            <div>
              <p class="dialog-kicker">{{ editingId ? 'Edit Mode' : 'Create Mode' }}</p>
              <h4>{{ editingId ? '编辑记录' : '新增记录' }}</h4>
            </div>
            <button class="icon-btn" @click="dialogVisible = false">
              <X :size="16" />
            </button>
          </div>

          <div class="dialog-tip">
            <span class="required-dot">*</span>
            <span>标记字段为必填项，请先完整填写再保存。</span>
          </div>

          <div v-if="dialogError" class="dialog-error">{{ dialogError }}</div>

          <div class="form-grid form-grid-enhanced">
            <label v-for="field in moduleConfig.formFields" :key="field.key" class="form-item" :class="{ required: isRequired(field) }">
              <span>{{ field.label }}</span>
              <textarea v-if="field.type === 'textarea'" v-model="formData[field.key]" rows="3" :placeholder="placeholderText(field)" />
              <select v-else-if="field.type === 'select'" v-model="formData[field.key]">
                <option value="">{{ placeholderText(field) }}</option>
                <option v-for="op in field.options || []" :key="`${op.value}`" :value="op.value">{{ op.label }}</option>
              </select>
              <input
                v-else
                :type="field.type === 'number' ? 'number' : field.type === 'date' ? 'date' : 'text'"
                v-model="formData[field.key]"
                :placeholder="placeholderText(field)"
              />
            </label>
          </div>

          <div class="dialog-actions dialog-actions-enhanced">
            <button class="btn btn-light" @click="dialogVisible = false">
              <X :size="14" />
              <span>取消</span>
            </button>
            <button class="btn btn-primary" @click="save">
              <Save :size="14" />
              <span>保存</span>
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>

  <div v-else class="table-card card-float">模块不存在</div>
</template>

<style scoped>
.module-page {
  display: grid;
  gap: 12px;
}

.card-float {
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.82));
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 14px;
  padding: 14px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card-float:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.1);
}

.header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h3 {
  margin: 0;
  font-size: 22px;
}

p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.tool-card {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.input-wrap {
  position: relative;
  min-width: 210px;
  flex: 1;
}

.prefix-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
}

.input-wrap input,
.input-wrap select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 9px 10px 9px 32px;
  background: rgba(255, 255, 255, 0.9);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input-wrap input:focus,
.input-wrap select:focus {
  outline: none;
  border-color: #38bdf8;
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.12);
}

.stat-strip {
  display: grid;
  gap: 8px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.stat-pill {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 10px;
}

.stat-pill span {
  color: #64748b;
  font-size: 12px;
}

.stat-pill strong {
  display: block;
  margin-top: 4px;
  font-size: 22px;
}

.stat-pill.hint {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-pill .ok {
  color: #16a34a;
}

.btn {
  border: 0;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  transition: transform 0.15s ease, filter 0.2s ease, opacity 0.2s ease;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn:disabled {
  opacity: 0.5;
  cursor: default;
}

.btn-primary {
  background: linear-gradient(135deg, #0284c7, #0ea5e9);
  color: #fff;
}

.btn-light {
  background: #e2e8f0;
  color: #0f172a;
}

.btn-info {
  background: #dbeafe;
  color: #1d4ed8;
}

.btn-danger {
  background: #ef4444;
  color: #fff;
}

.btn-sm {
  padding: 6px 10px;
  font-size: 12px;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
  overflow: visible;
}

thead th {
  position: sticky;
  top: 0;
  z-index: 1;
  background: #f1f5f9;
  border-bottom: 1px solid #dbe2ea;
  text-align: left;
  padding: 11px 8px;
}

tbody td {
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
  padding: 10px 8px;
}

.data-row {
  animation: rowIn 0.35s ease both;
  animation-delay: var(--row-delay);
  position: relative;
  z-index: 1;
}

.data-row-detail-open {
  z-index: 30;
}

@keyframes rowIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

tbody tr:hover td {
  background: rgba(248, 250, 252, 0.9);
}

.skeleton-line {
  height: 14px;
  border-radius: 8px;
  background: linear-gradient(90deg, #e2e8f0, #f1f5f9, #e2e8f0);
  background-size: 200% 100%;
  animation: shimmer 1.2s linear infinite;
}

.skeleton-line.short {
  width: 80px;
}

@keyframes shimmer {
  from {
    background-position: 200% 0;
  }
  to {
    background-position: -200% 0;
  }
}

.empty-state {
  min-height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-dot {
  width: 24px;
  height: 24px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #bfdbfe, #67e8f9);
  color: #0c4a6e;
  font-size: 12px;
  font-weight: 700;
}

.role-badge {
  display: inline-block;
  border-radius: 999px;
  background: #e0f2fe;
  color: #0c4a6e;
  padding: 2px 10px;
  font-size: 12px;
}

.status-badge {
  display: inline-block;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
}

.status-ok {
  background: #dcfce7;
  color: #166534;
}

.status-warn {
  background: #fee2e2;
  color: #991b1b;
}

.status-neutral {
  background: #e2e8f0;
  color: #334155;
}

.row-actions {
  display: flex;
  gap: 6px;
  position: relative;
  justify-content: flex-start;
  flex-wrap: wrap;
  overflow: visible;
}

.detail-bubble {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 100;
  width: min(430px, 76vw);
  border-radius: 14px;
  border: 1px solid #dbeafe;
  background: #ffffff;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.14);
  padding: 10px;
}

.bubble-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.icon-btn.mini {
  width: 24px;
  height: 24px;
}

.bubble-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.bubble-item {
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 7px 8px;
}

.bubble-item span {
  display: block;
  color: #64748b;
  font-size: 11px;
  margin-bottom: 3px;
}

.bubble-item em {
  font-style: normal;
  color: #0f172a;
  font-size: 12px;
  word-break: break-all;
}

.bubble-fade-enter-active,
.bubble-fade-leave-active {
  transition: all 0.2s ease;
}

.bubble-fade-enter-from,
.bubble-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.pager {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.pager span {
  color: #475569;
  font-size: 13px;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.35);
  display: grid;
  place-items: center;
  z-index: 30;
  padding: 16px;
}

.dialog {
  width: min(980px, 100%);
  max-height: 86vh;
  overflow: auto;
  background: linear-gradient(165deg, #ffffff, #f8fbff);
  border: 1px solid #dbe8f4;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.2);
}

.dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.dialog-kicker {
  margin: 0;
  color: #0ea5e9;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-weight: 700;
}

.dialog h4 {
  margin: 4px 0 0;
  font-size: 22px;
}

.icon-btn {
  border: 1px solid #dbe2ea;
  border-radius: 10px;
  background: #fff;
  width: 32px;
  height: 32px;
  display: grid;
  place-items: center;
  color: #334155;
}

.dialog-tip {
  margin-top: 10px;
  border-radius: 12px;
  background: #f0f9ff;
  border: 1px solid #dbeafe;
  padding: 8px 10px;
  color: #0c4a6e;
  font-size: 12px;
  display: flex;
  gap: 6px;
  align-items: center;
}

.required-dot {
  color: #dc2626;
  font-weight: 700;
}

.dialog-error {
  margin-top: 10px;
  border-radius: 10px;
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 8px 10px;
  font-size: 13px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.form-grid-enhanced {
  margin-top: 12px;
}

.form-item {
  padding: 10px;
  border-radius: 14px;
  border: 1px solid transparent;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.form-item:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.form-grid label span {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #334155;
}

.form-item.required span::after {
  content: ' *';
  color: #dc2626;
  font-weight: 700;
}

.form-grid input,
.form-grid textarea,
.form-grid select {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 9px 10px;
  background: #fff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.15s ease;
}

.form-grid input:focus,
.form-grid textarea:focus,
.form-grid select:focus {
  outline: none;
  border-color: #38bdf8;
  box-shadow: 0 0 0 4px rgba(14, 165, 233, 0.12);
  transform: translateY(-1px);
}

.dialog-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.dialog-actions-enhanced {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.22s ease;
}

.dialog-fade-enter-active .dialog,
.dialog-fade-leave-active .dialog {
  transition: transform 0.24s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

.dialog-fade-enter-from .dialog,
.dialog-fade-leave-to .dialog {
  transform: translateY(8px) scale(0.98);
}

@media (max-width: 760px) {
  .table-card {
    overflow: auto visible;
  }

  table {
    min-width: 780px;
  }

  .bubble-grid {
    grid-template-columns: 1fr;
  }
}
</style>
