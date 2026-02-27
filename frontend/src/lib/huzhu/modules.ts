import {
  LayoutDashboard,
  Sprout,
  ShoppingCart,
  Boxes,
  Factory,
  ShieldCheck,
  ReceiptText,
  Users,
  UserCog,
  KeyRound,
  ScrollText,
  Truck,
  ListTodo,
  FolderOpen,
  BellRing,
  ScanLine,
} from 'lucide-vue-next'

export interface HuzhuModuleField {
  key: string
  label: string
  type: 'text' | 'number' | 'date' | 'textarea' | 'select'
  required?: boolean
  options?: Array<{ label: string; value: string | number }>
}

export interface HuzhuModuleConfig {
  key: string
  group: '系统管理' | '供应链管理' | '生产管理' | '销售管理' | '协同办公' | '质量追溯'
  title: string
  endpoint: string
  icon: any
  teamOwner: string
  keywordPlaceholder: string
  statusField?: string
  statusOptions?: Array<{ label: string; value: string | number }>
  columns: Array<{ key: string; label: string }>
  formFields: HuzhuModuleField[]
}

const yesNoStatus = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

const processStatus = [
  { label: '待处理', value: '待处理' },
  { label: '处理中', value: '处理中' },
  { label: '已完成', value: '已完成' },
  { label: 'pending', value: 'pending' },
  { label: 'processing', value: 'processing' },
  { label: 'done', value: 'done' },
]

const qualityStatus = [
  { label: '合格', value: '合格' },
  { label: '不合格', value: '不合格' },
  { label: 'qualified', value: 'qualified' },
  { label: 'unqualified', value: 'unqualified' },
]

const orderStatus = [
  { label: '待付款', value: '待付款' },
  { label: '待出库', value: '待出库' },
  { label: '已发货', value: '已发货' },
  { label: '已完成', value: '已完成' },
  { label: '待支付', value: '待支付' },
  { label: '处理中', value: '处理中' },
  { label: 'pending_payment', value: 'pending_payment' },
  { label: 'processing', value: 'processing' },
  { label: 'done', value: 'done' },
]

export const huzhuModules: HuzhuModuleConfig[] = [
  {
    key: 'users',
    group: '系统管理',
    title: '用户管理',
    endpoint: '/users',
    icon: UserCog,
    teamOwner: '队友A（系统与权限）',
    keywordPlaceholder: '用户名/姓名/电话/邮箱',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'username', label: '用户名' },
      { key: 'realName', label: '姓名' },
      { key: 'roleId', label: '角色' },
      { key: 'phone', label: '电话' },
      { key: 'email', label: '邮箱' },
      { key: 'status', label: '状态' },
      { key: 'createTime', label: '创建时间' },
    ],
    formFields: [
      { key: 'username', label: '用户名', type: 'text', required: true },
      { key: 'password', label: '密码', type: 'text' },
      { key: 'realName', label: '姓名', type: 'text' },
      { key: 'phone', label: '电话', type: 'text' },
      { key: 'email', label: '邮箱', type: 'text' },
      { key: 'roleId', label: '角色ID', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
    ],
  },
  {
    key: 'roles',
    group: '系统管理',
    title: '角色管理',
    endpoint: '/roles',
    icon: Users,
    teamOwner: '队友A（系统与权限）',
    keywordPlaceholder: '角色名称/描述',
    columns: [
      { key: 'roleName', label: '角色名称' },
      { key: 'description', label: '角色描述' },
    ],
    formFields: [
      { key: 'roleName', label: '角色名称', type: 'text', required: true },
      { key: 'description', label: '角色描述', type: 'textarea' },
    ],
  },
  {
    key: 'permissions',
    group: '系统管理',
    title: '权限管理',
    endpoint: '/permissions',
    icon: KeyRound,
    teamOwner: '队友A（系统与权限）',
    keywordPlaceholder: '权限名称/编码/URL',
    columns: [
      { key: 'permissionName', label: '权限名称' },
      { key: 'permissionCode', label: '权限编码' },
      { key: 'url', label: 'URL' },
    ],
    formFields: [
      { key: 'permissionName', label: '权限名称', type: 'text', required: true },
      { key: 'permissionCode', label: '权限编码', type: 'text', required: true },
      { key: 'url', label: 'URL', type: 'text' },
      { key: 'parentId', label: '父级ID', type: 'number' },
      { key: 'type', label: '类型', type: 'number' },
      { key: 'sort', label: '排序', type: 'number' },
    ],
  },
  {
    key: 'planting-bases',
    group: '供应链管理',
    title: '青稞种植管理',
    endpoint: '/planting-bases',
    icon: Sprout,
    teamOwner: '队友B（种植与采购）',
    keywordPlaceholder: '基地名称/位置/负责人',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'baseName', label: '基地名称' },
      { key: 'location', label: '位置' },
      { key: 'area', label: '面积' },
      { key: 'manager', label: '负责人' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'baseName', label: '基地名称', type: 'text', required: true },
      { key: 'location', label: '位置', type: 'text' },
      { key: 'area', label: '面积', type: 'number' },
      { key: 'soilType', label: '土壤', type: 'text' },
      { key: 'climate', label: '气候', type: 'text' },
      { key: 'manager', label: '负责人', type: 'text' },
      { key: 'contactPhone', label: '电话', type: 'text' },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
    ],
  },
  {
    key: 'purchases',
    group: '供应链管理',
    title: '原料采购管理',
    endpoint: '/purchases',
    icon: ShoppingCart,
    teamOwner: '队友B（种植与采购）',
    keywordPlaceholder: '采购单号/供应商/原料',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'purchaseNo', label: '采购编号' },
      { key: 'supplierName', label: '供应商' },
      { key: 'materialName', label: '原料' },
      { key: 'totalAmount', label: '金额' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'purchaseNo', label: '采购编号', type: 'text', required: true },
      { key: 'supplierName', label: '供应商', type: 'text', required: true },
      { key: 'materialName', label: '原料', type: 'text', required: true },
      { key: 'quantity', label: '数量', type: 'number' },
      { key: 'unit', label: '单位', type: 'text' },
      { key: 'price', label: '单价', type: 'number' },
      { key: 'totalAmount', label: '总额', type: 'number' },
      { key: 'purchaseDate', label: '采购日期', type: 'date' },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
      { key: 'remark', label: '备注', type: 'textarea' },
    ],
  },
  {
    key: 'inventories',
    group: '供应链管理',
    title: '库存管理',
    endpoint: '/inventories',
    icon: Boxes,
    teamOwner: '队友B（种植与采购）',
    keywordPlaceholder: '产品名称/编码/仓库',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'productName', label: '产品名称' },
      { key: 'productCode', label: '产品编码' },
      { key: 'category', label: '类别' },
      { key: 'quantity', label: '数量' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'productName', label: '产品名称', type: 'text', required: true },
      { key: 'productCode', label: '产品编码', type: 'text', required: true },
      { key: 'category', label: '类别', type: 'text' },
      { key: 'quantity', label: '数量', type: 'number' },
      { key: 'unit', label: '单位', type: 'text' },
      { key: 'price', label: '单价', type: 'number' },
      { key: 'totalValue', label: '总值', type: 'number' },
      { key: 'warehouse', label: '仓库', type: 'text' },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
    ],
  },
  {
    key: 'production-processes',
    group: '生产管理',
    title: '生产流程管理',
    endpoint: '/production-processes',
    icon: Factory,
    teamOwner: '队友C（生产与质检）',
    keywordPlaceholder: '流程号/批次号/产品',
    statusField: 'status',
    statusOptions: processStatus,
    columns: [
      { key: 'processNo', label: '流程编号' },
      { key: 'productName', label: '产品' },
      { key: 'batchNo', label: '批次' },
      { key: 'quantity', label: '数量' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'processNo', label: '流程编号', type: 'text', required: true },
      { key: 'productName', label: '产品名称', type: 'text', required: true },
      { key: 'batchNo', label: '批次号', type: 'text', required: true },
      { key: 'quantity', label: '数量', type: 'number' },
      { key: 'unit', label: '单位', type: 'text' },
      { key: 'startDate', label: '开始日期', type: 'date' },
      { key: 'endDate', label: '结束日期', type: 'date' },
      { key: 'status', label: '状态', type: 'select', options: processStatus },
      { key: 'operator', label: '操作员', type: 'text' },
      { key: 'remark', label: '备注', type: 'textarea' },
    ],
  },
  {
    key: 'quality-controls',
    group: '生产管理',
    title: '质量管理',
    endpoint: '/quality-controls',
    icon: ShieldCheck,
    teamOwner: '队友C（生产与质检）',
    keywordPlaceholder: '质检号/批次/检测人',
    statusField: 'status',
    statusOptions: qualityStatus,
    columns: [
      { key: 'qualityNo', label: '质检编号' },
      { key: 'productName', label: '产品' },
      { key: 'batchNo', label: '批次' },
      { key: 'tester', label: '检测员' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'qualityNo', label: '质检编号', type: 'text', required: true },
      { key: 'productName', label: '产品名称', type: 'text', required: true },
      { key: 'batchNo', label: '批次号', type: 'text', required: true },
      { key: 'testItem', label: '检测项目', type: 'text' },
      { key: 'testResult', label: '检测结果', type: 'textarea' },
      { key: 'testStandard', label: '检测标准', type: 'textarea' },
      { key: 'tester', label: '检测人', type: 'text' },
      { key: 'testDate', label: '检测日期', type: 'date' },
      { key: 'status', label: '状态', type: 'select', options: qualityStatus },
      { key: 'remark', label: '备注', type: 'textarea' },
    ],
  },
  {
    key: 'orders',
    group: '销售管理',
    title: '订单管理',
    endpoint: '/orders',
    icon: ScrollText,
    teamOwner: '队友D（销售与客户）',
    keywordPlaceholder: '订单号/客户/电话',
    statusField: 'status',
    statusOptions: orderStatus,
    columns: [
      { key: 'orderNo', label: '订单号' },
      { key: 'customerName', label: '客户名称' },
      { key: 'totalAmount', label: '金额' },
      { key: 'orderDate', label: '日期' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'orderNo', label: '订单号', type: 'text', required: true },
      { key: 'customerName', label: '客户名称', type: 'text', required: true },
      { key: 'customerPhone', label: '电话', type: 'text' },
      { key: 'address', label: '地址', type: 'text' },
      { key: 'totalAmount', label: '金额', type: 'number' },
      { key: 'orderDate', label: '订单日期', type: 'date' },
      { key: 'status', label: '状态', type: 'select', options: orderStatus },
      { key: 'paymentMethod', label: '支付方式', type: 'text' },
      { key: 'remark', label: '备注', type: 'textarea' },
    ],
  },
  {
    key: 'deliveries',
    group: '销售管理',
    title: '配送管理',
    endpoint: '/deliveries',
    icon: Truck,
    teamOwner: '队友D（销售与客户）',
    keywordPlaceholder: '配送单号/订单号/客户/物流单号',
    statusField: 'status',
    statusOptions: [
      { label: '运输中', value: 'in_transit' },
      { label: '已签收', value: 'signed' },
      { label: '异常', value: 'exception' },
    ],
    columns: [
      { key: 'deliveryNo', label: '配送单号' },
      { key: 'orderNo', label: '订单号' },
      { key: 'customerName', label: '客户' },
      { key: 'trackingNo', label: '物流单号' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'deliveryNo', label: '配送单号', type: 'text', required: true },
      { key: 'orderNo', label: '订单号', type: 'text', required: true },
      { key: 'customerName', label: '客户名称', type: 'text', required: true },
      { key: 'deliveryCompany', label: '物流公司', type: 'text' },
      { key: 'trackingNo', label: '物流单号', type: 'text' },
      { key: 'deliveryDate', label: '配送日期', type: 'date' },
      {
        key: 'status',
        label: '状态',
        type: 'select',
        options: [
          { label: '运输中', value: 'in_transit' },
          { label: '已签收', value: 'signed' },
          { label: '异常', value: 'exception' },
        ],
      },
      { key: 'remark', label: '备注', type: 'textarea' },
    ],
  },
  {
    key: 'customers',
    group: '销售管理',
    title: '客户管理',
    endpoint: '/customers',
    icon: ReceiptText,
    teamOwner: '队友D（销售与客户）',
    keywordPlaceholder: '客户名称/联系人/电话/邮箱',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'customerName', label: '客户名称' },
      { key: 'contactPerson', label: '联系人' },
      { key: 'phone', label: '电话' },
      { key: 'email', label: '邮箱' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'customerName', label: '客户名称', type: 'text', required: true },
      { key: 'contactPerson', label: '联系人', type: 'text' },
      { key: 'phone', label: '电话', type: 'text' },
      { key: 'email', label: '邮箱', type: 'text' },
      { key: 'address', label: '地址', type: 'text' },
      { key: 'businessScope', label: '经营范围', type: 'textarea' },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
      { key: 'remark', label: '备注', type: 'textarea' },
    ],
  },
  {
    key: 'tasks',
    group: '协同办公',
    title: '任务管理',
    endpoint: '/tasks',
    icon: ListTodo,
    teamOwner: '队友E（协同办公）',
    keywordPlaceholder: '任务标题/内容/负责人',
    statusField: 'status',
    statusOptions: [
      { label: '待处理', value: 'pending' },
      { label: '处理中', value: 'processing' },
      { label: '已完成', value: 'done' },
    ],
    columns: [
      { key: 'taskTitle', label: '任务标题' },
      { key: 'assignee', label: '负责人' },
      { key: 'dueDate', label: '截止日期' },
      { key: 'priority', label: '优先级' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'taskTitle', label: '任务标题', type: 'text', required: true },
      { key: 'taskContent', label: '任务内容', type: 'textarea' },
      { key: 'assignee', label: '负责人', type: 'text' },
      { key: 'dueDate', label: '截止日期', type: 'date' },
      {
        key: 'priority',
        label: '优先级',
        type: 'select',
        options: [
          { label: '高', value: 'high' },
          { label: '中', value: 'medium' },
          { label: '低', value: 'low' },
        ],
      },
      {
        key: 'status',
        label: '状态',
        type: 'select',
        options: [
          { label: '待处理', value: 'pending' },
          { label: '处理中', value: 'processing' },
          { label: '已完成', value: 'done' },
        ],
      },
    ],
  },
  {
    key: 'documents',
    group: '协同办公',
    title: '文档管理',
    endpoint: '/documents',
    icon: FolderOpen,
    teamOwner: '队友E（协同办公）',
    keywordPlaceholder: '标题/文件名/上传人/版本',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'title', label: '标题' },
      { key: 'fileName', label: '文件名' },
      { key: 'uploader', label: '上传人' },
      { key: 'version', label: '版本' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'title', label: '标题', type: 'text', required: true },
      { key: 'fileName', label: '文件名', type: 'text', required: true },
      { key: 'fileUrl', label: '文件URL', type: 'text' },
      { key: 'uploader', label: '上传人', type: 'text' },
      { key: 'version', label: '版本', type: 'text' },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
    ],
  },
  {
    key: 'notices',
    group: '协同办公',
    title: '通知公告',
    endpoint: '/notices',
    icon: BellRing,
    teamOwner: '队友E（协同办公）',
    keywordPlaceholder: '标题/内容/发布人',
    statusField: 'status',
    statusOptions: yesNoStatus,
    columns: [
      { key: 'noticeTitle', label: '标题' },
      { key: 'publisher', label: '发布人' },
      { key: 'isTop', label: '置顶' },
      { key: 'status', label: '状态' },
    ],
    formFields: [
      { key: 'noticeTitle', label: '标题', type: 'text', required: true },
      { key: 'noticeContent', label: '内容', type: 'textarea' },
      { key: 'publisher', label: '发布人', type: 'text' },
      { key: 'isTop', label: '置顶', type: 'select', options: [{ label: '否', value: 0 }, { label: '是', value: 1 }] },
      { key: 'status', label: '状态', type: 'select', options: yesNoStatus },
    ],
  },
  {
    key: 'trace-records',
    group: '质量追溯',
    title: '追溯记录',
    endpoint: '/trace-records',
    icon: ScanLine,
    teamOwner: '队友C（生产与质检）',
    keywordPlaceholder: '追溯码/产品/批次/阶段',
    columns: [
      { key: 'traceCode', label: '追溯码' },
      { key: 'productName', label: '产品' },
      { key: 'batchNo', label: '批次' },
      { key: 'stage', label: '阶段' },
      { key: 'recordTime', label: '记录时间' },
    ],
    formFields: [
      { key: 'traceCode', label: '追溯码', type: 'text', required: true },
      { key: 'productName', label: '产品名称', type: 'text', required: true },
      { key: 'productCode', label: '产品编码', type: 'text', required: true },
      { key: 'batchNo', label: '批次号', type: 'text', required: true },
      { key: 'stage', label: '阶段', type: 'text' },
      { key: 'stageDetail', label: '阶段详情', type: 'textarea' },
      { key: 'operator', label: '操作员', type: 'text' },
      { key: 'recordTime', label: '记录时间', type: 'text' },
    ],
  },
]

export const huzhuModuleGroups: HuzhuModuleConfig['group'][] = [
  '系统管理',
  '供应链管理',
  '生产管理',
  '销售管理',
  '协同办公',
  '质量追溯',
]

export const huzhuDashboardMenu = {
  key: 'dashboard',
  title: '仪表盘',
  icon: LayoutDashboard,
}

export const moduleMap = huzhuModules.reduce<Record<string, HuzhuModuleConfig>>((acc, item) => {
  acc[item.key] = item
  return acc
}, {})

const commonModuleKeys = ['dashboard', 'settings', 'tasks', 'documents', 'notices']

const roleModuleMap: Record<number, string[]> = {
  1: ['*'],
  2: [...commonModuleKeys, 'planting-bases', 'purchases', 'inventories'],
  3: [...commonModuleKeys, 'purchases', 'inventories', 'production-processes'],
  4: [...commonModuleKeys, 'production-processes', 'orders', 'deliveries', 'customers'],
  5: [...commonModuleKeys, 'quality-controls', 'trace-records'],
  6: [...commonModuleKeys, 'orders', 'deliveries', 'customers'],
  7: [...commonModuleKeys, 'orders', 'deliveries'],
}

export const canAccessModuleByRole = (roleId: number | string | null | undefined, moduleKey: string) => {
  const id = Number(roleId || 0)
  if (!id || !moduleKey) return false
  const allowed = roleModuleMap[id]
  if (!allowed) return false
  if (allowed.includes('*')) return true
  return allowed.includes(moduleKey)
}
