import { createRouter, createWebHistory } from 'vue-router'
import HuzhuLoginView from '@/views/huzhu/HuzhuLoginView.vue'
import HuzhuLayout from '@/views/huzhu/HuzhuLayout.vue'
import HuzhuDashboardView from '@/views/huzhu/HuzhuDashboardView.vue'
import HuzhuModuleView from '@/views/huzhu/HuzhuModuleView.vue'
import HuzhuSettingsView from '@/views/huzhu/HuzhuSettingsView.vue'
import { canAccessModuleByRole, moduleMap } from '@/lib/huzhu/modules'
import { getHuzhuToken, getHuzhuUser } from '@/lib/huzhuAuth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: HuzhuLoginView, meta: { title: '登录' } },
    {
      path: '/',
      component: HuzhuLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: HuzhuDashboardView, meta: { requiresAuth: true, title: '仪表盘' } },
        { path: 'settings', name: 'settings', component: HuzhuSettingsView, meta: { requiresAuth: true, title: '系统配置' } },
        {
          path: 'modules/:moduleKey',
          name: 'module',
          component: HuzhuModuleView,
          meta: { requiresAuth: true, title: '业务模块' },
          beforeEnter: (to, from, next) => {
            const key = String(to.params.moduleKey || '')
            if (!moduleMap[key]) {
              next('/dashboard')
              return
            }
            const roleId = Number(getHuzhuUser()?.roleId || 0)
            if (!canAccessModuleByRole(roleId, key)) {
              next('/dashboard')
              return
            }
            to.meta.title = moduleMap[key].title
            next()
          }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ]
})

router.beforeEach((to, from, next) => {
  const token = getHuzhuToken()
  const needAuth = Boolean(to.meta.requiresAuth)

  if (needAuth && !token) {
    next('/login')
    return
  }

  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  next()
})

export default router
