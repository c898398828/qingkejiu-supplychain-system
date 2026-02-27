import './assets/main.css'

import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import { initInterfaceScale } from '@/lib/interfaceScale'
import { pinia } from '@/stores/pinia'
import { normalizeFrontendFontKey, resolveFrontendFontFamily } from '@/lib/frontendFonts'

const app = createApp(App)

app.use(pinia)
app.use(router)

initInterfaceScale()

if (typeof document !== 'undefined') {
  const savedFontKey = localStorage.getItem('huzhu-frontend-font-key')
  const normalized = normalizeFrontendFontKey(savedFontKey)
  document.documentElement.style.setProperty('--app-font-family', resolveFrontendFontFamily(normalized))
}
app.mount('#app')
