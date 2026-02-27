<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, PanelLeftClose, PanelLeftOpen, Settings, X } from 'lucide-vue-next'
import { canAccessModuleByRole, huzhuDashboardMenu, huzhuModules, huzhuModuleGroups } from '@/lib/huzhu/modules'
import { normalizeFrontendAdminAvatarKey, resolveFrontendAdminAvatarUrl } from '@/lib/frontendAvatars'
import { clearHuzhuAuth, getHuzhuUser } from '@/lib/huzhuAuth'

const SIDEBAR_COLLAPSED_KEY = 'huzhu-sidebar-collapsed'
const HOVER_PREVIEW_KEY = 'huzhu-sidebar-hover-preview'

const route = useRoute()
const router = useRouter()

const user = computed(() => getHuzhuUser())
const roleId = computed(() => Number(user.value?.roleId || 0))
const isSidebarCollapsed = ref(false)
const isMobileDrawerOpen = ref(false)
const activeDrawerKey = ref<string | null>(null)
const hoverDrawerKey = ref<string | null>(null)
const isDesktop = ref(true)
const railDrawerTop = ref(18)
const hoverPreviewEnabled = ref(true)
let hoverCloseTimer: ReturnType<typeof setTimeout> | null = null

const grouped = computed(() => {
  return huzhuModuleGroups.map((group) => ({
    group,
    items: huzhuModules.filter((item) => item.group === group && canAccessModuleByRole(roleId.value, item.key)),
  }))
})

const railTabs = computed(() => {
  const rootTabs = [
    { key: 'dashboard', title: huzhuDashboardMenu.title, icon: huzhuDashboardMenu.icon, path: '/dashboard' },
    { key: 'settings', title: '系统配置', icon: Settings, path: '/settings' },
  ]

  const groupTabs = grouped.value
    .filter((section) => section.items.length > 0)
    .map((section) => ({
      key: `group:${section.group}`,
      title: section.group,
      icon: section.items[0]?.icon || Settings,
      path: '',
    }))

  return [...rootTabs, ...groupTabs]
})

const currentRailKey = computed<string | null>(() => {
  if (route.path === '/dashboard') return 'dashboard'
  if (route.path === '/settings') return 'settings'
  if (route.path.startsWith('/modules/')) {
    const moduleKey = decodeURIComponent(route.path.replace('/modules/', '').split('/')[0] || '')
    const moduleItem = huzhuModules.find((item) => item.key === moduleKey)
    return moduleItem ? `group:${moduleItem.group}` : null
  }
  return null
})

const activeDrawerTitle = computed(() => {
  const key = activeDrawerKey.value || hoverDrawerKey.value
  if (!key) return ''
  if (key === 'dashboard') return '仪表盘'
  if (key === 'settings') return '系统配置'
  if (key.startsWith('group:')) return key.replace('group:', '')
  return ''
})

const activeDrawerItems = computed(() => {
  const key = activeDrawerKey.value || hoverDrawerKey.value
  if (!key) return [] as Array<{ title: string; path: string; icon: any }>

  if (key === 'dashboard') {
    return [{ title: huzhuDashboardMenu.title, path: '/dashboard', icon: huzhuDashboardMenu.icon }]
  }

  if (key === 'settings') {
    return [{ title: '系统配置', path: '/settings', icon: Settings }]
  }

  if (key.startsWith('group:')) {
    const group = key.replace('group:', '')
    const section = grouped.value.find((item) => item.group === group)
    return (section?.items || []).map((item) => ({
      title: item.title,
      path: `/modules/${item.key}`,
      icon: item.icon,
    }))
  }

  return []
})

const shouldShowRailDrawer = computed(() => {
  return isDesktop.value && isSidebarCollapsed.value && activeDrawerItems.value.length > 0
})

const isActive = (path: string) => route.path === path

const isRailTabActive = (tab: { key: string; path: string }) => {
  if (activeDrawerKey.value || hoverDrawerKey.value) {
    return (activeDrawerKey.value || hoverDrawerKey.value) === tab.key
  }
  if (tab.path) {
    return isActive(tab.path)
  }
  return currentRailKey.value === tab.key
}

const avatarKey = ref(normalizeFrontendAdminAvatarKey(localStorage.getItem('huzhu-frontend-avatar-key')))
const avatarUrl = computed(() => resolveFrontendAdminAvatarUrl(avatarKey.value))
const userInitial = computed(() => String(user.value?.username || 'A').charAt(0).toUpperCase())

const syncUiConfig = () => {
  avatarKey.value = normalizeFrontendAdminAvatarKey(localStorage.getItem('huzhu-frontend-avatar-key'))
  hoverPreviewEnabled.value = localStorage.getItem(HOVER_PREVIEW_KEY) !== '0'
}

const syncViewport = () => {
  isDesktop.value = window.innerWidth > 980
  if (!isDesktop.value) {
    isSidebarCollapsed.value = false
    activeDrawerKey.value = null
    hoverDrawerKey.value = null
  }
}

const toggleSidebarCollapse = () => {
  if (!isDesktop.value) return
  isSidebarCollapsed.value = !isSidebarCollapsed.value
  localStorage.setItem(SIDEBAR_COLLAPSED_KEY, isSidebarCollapsed.value ? '1' : '0')
  if (!isSidebarCollapsed.value) {
    activeDrawerKey.value = null
    hoverDrawerKey.value = null
  }
}

const toggleMobileDrawer = () => {
  isMobileDrawerOpen.value = !isMobileDrawerOpen.value
}

const closeMobileDrawer = () => {
  isMobileDrawerOpen.value = false
}

const toggleRailTab = (key: string, event?: MouseEvent) => {
  if (key === 'dashboard' || key === 'settings') {
    router.push(key === 'dashboard' ? '/dashboard' : '/settings')
    activeDrawerKey.value = null
    hoverDrawerKey.value = null
    return
  }
  if (!isGroupTabKey(key)) return
  updateDrawerAnchorFromEvent(event)
  clearHoverCloseTimer()
  hoverDrawerKey.value = null
  activeDrawerKey.value = activeDrawerKey.value === key ? null : key
}

const closeRailDrawer = () => {
  activeDrawerKey.value = null
  hoverDrawerKey.value = null
}

const isGroupTabKey = (key: string) => key.startsWith('group:')

const clearHoverCloseTimer = () => {
  if (hoverCloseTimer) {
    clearTimeout(hoverCloseTimer)
    hoverCloseTimer = null
  }
}

const scheduleHoverClose = () => {
  if (activeDrawerKey.value) return
  clearHoverCloseTimer()
  hoverCloseTimer = setTimeout(() => {
    hoverDrawerKey.value = null
  }, 120)
}

const updateDrawerAnchorFromEvent = (event?: MouseEvent) => {
  const target = event?.currentTarget as HTMLElement | null
  if (!target) return
  const top = target.getBoundingClientRect().top
  railDrawerTop.value = Math.max(16, Math.min(window.innerHeight - 260, top - 10))
}

const handleRailTabEnter = (key: string, event?: MouseEvent) => {
  if (!hoverPreviewEnabled.value) return
  if (!isDesktop.value || !isSidebarCollapsed.value || activeDrawerKey.value) return
  if (!isGroupTabKey(key)) return
  clearHoverCloseTimer()
  updateDrawerAnchorFromEvent(event)
  hoverDrawerKey.value = key
}

const handleRailTabLeave = () => {
  scheduleHoverClose()
}

const handleRailDrawerEnter = () => {
  clearHoverCloseTimer()
}

const handleRailDrawerLeave = () => {
  scheduleHoverClose()
}

const handleSidebarMaskClick = () => {
  closeMobileDrawer()
}

const logout = () => {
  clearHuzhuAuth()
  router.push('/login')
}

const onWindowKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    closeMobileDrawer()
    closeRailDrawer()
  }
}

const onWindowPointerDown = (event: MouseEvent) => {
  if (!shouldShowRailDrawer.value) return
  const target = event.target as HTMLElement | null
  if (!target) return
  if (target.closest('.rail-drawer') || target.closest('.rail-tab')) return
  closeRailDrawer()
}

watch(
  () => route.path,
  () => {
    closeMobileDrawer()
    if (!isSidebarCollapsed.value || !isDesktop.value) {
      closeRailDrawer()
    }
  }
)

onMounted(() => {
  isSidebarCollapsed.value = localStorage.getItem(SIDEBAR_COLLAPSED_KEY) === '1'
  hoverPreviewEnabled.value = localStorage.getItem(HOVER_PREVIEW_KEY) !== '0'
  syncViewport()
  window.addEventListener('huzhu-ui-config-updated', syncUiConfig)
  window.addEventListener('keydown', onWindowKeydown)
  window.addEventListener('resize', syncViewport)
  window.addEventListener('mousedown', onWindowPointerDown)
})

onUnmounted(() => {
  window.removeEventListener('huzhu-ui-config-updated', syncUiConfig)
  window.removeEventListener('keydown', onWindowKeydown)
  window.removeEventListener('resize', syncViewport)
  window.removeEventListener('mousedown', onWindowPointerDown)
  clearHoverCloseTimer()
})
</script>

<template>
  <div class="layout" :style="{ '--sidebar-width': isSidebarCollapsed ? '92px' : '286px' }">
    <transition name="mask-fade">
      <div v-if="isMobileDrawerOpen" class="sidebar-mask" @click="handleSidebarMaskClick"></div>
    </transition>

    <aside class="sidebar" :class="{ collapsed: isSidebarCollapsed, 'mobile-open': isMobileDrawerOpen }">
      <div class="sidebar-head">
        <div class="brand" v-if="!isSidebarCollapsed">
          <h1>互助青稞酒系统</h1>
          <p>产业链协同管理平台</p>
        </div>
        <div class="brand-mini" v-else>互助</div>

        <button
          v-if="isDesktop"
          class="collapse-btn"
          type="button"
          @click="toggleSidebarCollapse"
          :aria-label="isSidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
        >
          <PanelLeftOpen v-if="isSidebarCollapsed" class="icon" />
          <PanelLeftClose v-else class="icon" />
        </button>
      </div>

      <template v-if="!isSidebarCollapsed">
        <router-link class="menu-item" :class="{ active: isActive('/dashboard') }" to="/dashboard">
          <component :is="huzhuDashboardMenu.icon" class="icon" />
          <span>{{ huzhuDashboardMenu.title }}</span>
        </router-link>

        <router-link class="menu-item" :class="{ active: isActive('/settings') }" to="/settings">
          <Settings class="icon" />
          <span>系统配置</span>
        </router-link>

        <div v-for="section in grouped" :key="section.group" class="menu-group">
          <h3>{{ section.group }}</h3>
          <router-link
            v-for="item in section.items"
            :key="item.key"
            class="menu-item"
            :class="{ active: isActive(`/modules/${item.key}`) }"
            :to="`/modules/${item.key}`"
          >
            <component :is="item.icon" class="icon" />
            <span>{{ item.title }}</span>
          </router-link>
        </div>
      </template>

      <template v-else>
        <div class="tab-rail">
          <button
            v-for="tab in railTabs"
            :key="tab.key"
            type="button"
            class="rail-tab"
            :class="{ active: isRailTabActive(tab) }"
            @click="toggleRailTab(tab.key, $event)"
            @mouseenter="handleRailTabEnter(tab.key, $event)"
            @mouseleave="handleRailTabLeave"
            :title="tab.title"
          >
            <component :is="tab.icon" class="icon" />
            <span class="rail-tooltip">{{ tab.title }}</span>
          </button>
        </div>
      </template>
    </aside>

    <transition name="drawer-fade">
      <div
        v-if="shouldShowRailDrawer"
        class="rail-drawer rail-drawer-floating"
        :style="{ top: `${railDrawerTop}px` }"
        @mouseenter="handleRailDrawerEnter"
        @mouseleave="handleRailDrawerLeave"
      >
        <div class="rail-drawer-head">
          <p>{{ activeDrawerTitle }}</p>
          <button type="button" class="rail-close" @click="closeRailDrawer" aria-label="关闭抽屉">
            <X class="icon" />
          </button>
        </div>

        <router-link
          v-for="item in activeDrawerItems"
          :key="item.path"
          class="drawer-item"
          :class="{ active: isActive(item.path) }"
          :to="item.path"
          @click="closeRailDrawer"
        >
          <component :is="item.icon" class="icon" />
          <span>{{ item.title }}</span>
        </router-link>
      </div>
    </transition>

    <div class="content-wrap">
      <header class="topbar">
        <div class="topbar-fx" aria-hidden="true">
          <span class="fx-orb orb-a"></span>
          <span class="fx-orb orb-b"></span>
          <span class="fx-sheen"></span>
        </div>
        <div class="topbar-title-wrap">
          <button class="mobile-menu-btn" type="button" @click="toggleMobileDrawer" aria-label="打开导航">
            <Menu class="icon" />
          </button>
          <h2 class="topbar-title">{{ (route.meta.title as string) || '管理中心' }}</h2>
        </div>

        <div class="user-box">
          <div class="avatar">
            <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" />
            <span v-else>{{ userInitial }}</span>
          </div>
          <div>
            <p class="name">{{ user?.realName || user?.username || '管理员' }}</p>
            <p class="role">{{ user?.username || 'admin' }}</p>
          </div>
          <button @click="logout">退出登录</button>
        </div>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  position: relative;
  display: grid;
  grid-template-columns: var(--sidebar-width) 1fr;
  min-height: 100vh;
  background: linear-gradient(180deg, #ddf2ff 0%, #f5f8fb 45%, #f8f8f8 100%);
}

.sidebar-mask {
  position: fixed;
  inset: 0;
  z-index: 39;
  background: rgba(15, 23, 42, 0.34);
  backdrop-filter: blur(2px);
}

.sidebar {
  position: relative;
  z-index: 40;
  background: rgba(255, 255, 255, 0.82);
  border-right: 1px solid rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  padding: 16px 14px;
  overflow-y: auto;
  overflow-x: visible;
  transition: width 0.26s ease, padding 0.26s ease;
}

.sidebar.collapsed {
  padding: 14px 10px;
}

.sidebar-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
}

.brand p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.brand-mini {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 13px;
  font-weight: 700;
  color: #0c4a6e;
  background: linear-gradient(135deg, #dbeafe, #e0f2fe);
}

.collapse-btn {
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #355371;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.08);
  cursor: pointer;
}

.collapse-btn:hover {
  color: #0284c7;
  transform: translateY(-1px);
}

.menu-group h3 {
  margin: 16px 0 8px;
  color: #475569;
  font-size: 12px;
}

.menu-item {
  position: relative;
  display: flex;
  gap: 10px;
  align-items: center;
  text-decoration: none;
  color: #334155;
  padding: 10px 12px;
  border-radius: 12px;
  margin-bottom: 4px;
  transition: all 0.22s ease;
  overflow: hidden;
}

.menu-item:hover,
.menu-item.active {
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.1);
  color: #0284c7;
}

.menu-item::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(110deg, transparent 20%, rgba(2, 132, 199, 0.1), transparent 80%);
  transform: translateX(-115%);
  transition: transform 0.35s ease;
}

.menu-item:hover::after {
  transform: translateX(115%);
}

.menu-item:active {
  transform: scale(0.985);
}

.tab-rail {
  display: grid;
  gap: 8px;
  padding-top: 4px;
}

.rail-tab {
  position: relative;
  border: 0;
  width: 100%;
  min-height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #38516d;
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.2s ease;
}

.rail-tab:hover,
.rail-tab.active {
  color: #0284c7;
  background: #fff;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.rail-tab::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 999px;
  background: linear-gradient(180deg, #0ea5e9, #0284c7);
  opacity: 0;
  transform: scaleY(0.4);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.rail-tab:hover::before,
.rail-tab.active::before {
  opacity: 1;
  transform: scaleY(1);
}

.rail-tab:active {
  transform: scale(0.96);
}

.rail-tooltip {
  position: absolute;
  left: calc(100% + 10px);
  top: 50%;
  transform: translateY(-50%);
  white-space: nowrap;
  padding: 6px 8px;
  border-radius: 8px;
  background: rgba(20, 38, 64, 0.92);
  color: #fff;
  font-size: 12px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.16s ease;
}

.rail-tab:hover .rail-tooltip {
  opacity: 1;
}

.rail-drawer {
  position: absolute;
  width: 230px;
  max-height: calc(100vh - 32px);
  overflow-y: auto;
  border-radius: 14px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(226, 232, 240, 0.92);
  box-shadow: 0 16px 32px rgba(15, 23, 42, 0.16);
  z-index: 120;
}

.rail-drawer-floating {
  left: calc(var(--sidebar-width) + 8px);
  top: 18px;
}

.rail-drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.rail-drawer-head p {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #1e3a5f;
}

.rail-close {
  border: 0;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  color: #475569;
  background: #eef4fb;
  cursor: pointer;
}

.drawer-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 10px;
  border-radius: 10px;
  color: #334155;
  text-decoration: none;
  margin-bottom: 4px;
  transition: all 0.18s ease;
}

.drawer-item:hover,
.drawer-item.active {
  color: #0284c7;
  background: #f8fbff;
}

.drawer-item:active {
  transform: scale(0.985);
}

.icon {
  width: 18px;
  height: 18px;
}

.content-wrap {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.topbar {
  position: relative;
  overflow: hidden;
  isolation: isolate;
  margin: 16px;
  border-radius: 18px;
  padding: 14px 18px;
  background: linear-gradient(110deg, rgba(255, 255, 255, 0.9), rgba(243, 251, 255, 0.88));
  border: 1px solid rgba(255, 255, 255, 0.75);
  box-shadow: 0 12px 26px rgba(15, 23, 42, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.75);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.topbar-fx {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: -1;
}

.fx-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  opacity: 0.55;
}

.orb-a {
  width: 220px;
  height: 220px;
  left: -72px;
  top: -122px;
  background: radial-gradient(circle at 40% 35%, rgba(74, 168, 255, 0.58), rgba(74, 168, 255, 0));
}

.orb-b {
  width: 240px;
  height: 240px;
  right: 24%;
  top: -148px;
  background: radial-gradient(circle at 45% 40%, rgba(67, 223, 255, 0.3), rgba(67, 223, 255, 0));
}

.fx-sheen {
  position: absolute;
  inset: 0;
  background: linear-gradient(115deg, transparent 20%, rgba(255, 255, 255, 0.45) 48%, transparent 76%);
  transform: translateX(-28%);
  animation: topbarSheen 5.8s ease-in-out infinite;
}

.topbar-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topbar-title {
  margin: 0;
  background: linear-gradient(120deg, #17355b, #245f9e);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: 0.02em;
}

.mobile-menu-btn {
  display: none;
  border: 0;
  border-radius: 10px;
  width: 36px;
  height: 36px;
  place-items: center;
  background: #eaf4ff;
  color: #1e4d87;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.mobile-menu-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(30, 77, 135, 0.18);
}

.mobile-menu-btn:active {
  transform: scale(0.96);
}

.topbar h2 {
  margin: 0;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  overflow: hidden;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #bfdbfe, #67e8f9);
  color: #0c4a6e;
  font-weight: 700;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.name {
  margin: 0;
  font-weight: 600;
  text-align: right;
}

.role {
  margin: 2px 0 0;
  color: #64748b;
  text-align: right;
  font-size: 12px;
}

.user-box button {
  border: 0;
  border-radius: 10px;
  background: #ef4444;
  color: #fff;
  padding: 8px 12px;
  cursor: pointer;
  transition: transform 0.18s ease, filter 0.18s ease;
}

.user-box button:hover {
  filter: saturate(1.08);
  transform: translateY(-1px);
}

.user-box button:active {
  transform: scale(0.97);
}

.main-content {
  padding: 0 16px 16px;
}

.mask-fade-enter-active,
.mask-fade-leave-active,
.drawer-fade-enter-active,
.drawer-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.mask-fade-enter-from,
.mask-fade-leave-to,
.drawer-fade-enter-from,
.drawer-fade-leave-to {
  opacity: 0;
}

.drawer-fade-enter-from,
.drawer-fade-leave-to {
  transform: translateX(-8px);
}

@keyframes topbarSheen {
  0%,
  100% {
    transform: translateX(-36%);
    opacity: 0.4;
  }
  50% {
    transform: translateX(22%);
    opacity: 0.8;
  }
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    width: min(300px, 80vw);
    transform: translateX(-102%);
    transition: transform 0.25s ease;
    border-right: 1px solid rgba(255, 255, 255, 0.9);
    overflow-x: hidden;
  }

  .rail-drawer-floating {
    display: none;
  }

  .sidebar.mobile-open {
    transform: translateX(0);
  }

  .mobile-menu-btn {
    display: grid;
  }

  .topbar {
    margin: 12px;
  }

  .main-content {
    padding: 0 12px 12px;
  }
}
</style>
