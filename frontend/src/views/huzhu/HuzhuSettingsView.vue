<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { FRONTEND_FONT_OPTIONS, normalizeFrontendFontKey, resolveFrontendFontFamily, type FrontendFontKey } from '@/lib/frontendFonts'
import {
  FRONTEND_ADMIN_AVATAR_OPTIONS,
  normalizeFrontendAdminAvatarKey,
  resolveFrontendAdminAvatarUrl,
  type FrontendAdminAvatarKey,
} from '@/lib/frontendAvatars'

const HOVER_PREVIEW_KEY = 'huzhu-sidebar-hover-preview'

const fontKey = ref<FrontendFontKey>(normalizeFrontendFontKey(localStorage.getItem('huzhu-frontend-font-key')))
const avatarKey = ref<FrontendAdminAvatarKey>(normalizeFrontendAdminAvatarKey(localStorage.getItem('huzhu-frontend-avatar-key')))
const hoverPreviewEnabled = ref(localStorage.getItem(HOVER_PREVIEW_KEY) !== '0')

const avatarUrl = computed(() => resolveFrontendAdminAvatarUrl(avatarKey.value))

watch(fontKey, (value) => {
  localStorage.setItem('huzhu-frontend-font-key', value)
  if (typeof document !== 'undefined') {
    document.documentElement.style.setProperty('--app-font-family', resolveFrontendFontFamily(value))
  }
})

watch(avatarKey, (value) => {
  localStorage.setItem('huzhu-frontend-avatar-key', value)
  window.dispatchEvent(new Event('huzhu-ui-config-updated'))
})

watch(hoverPreviewEnabled, (value) => {
  localStorage.setItem(HOVER_PREVIEW_KEY, value ? '1' : '0')
  window.dispatchEvent(new Event('huzhu-ui-config-updated'))
})
</script>

<template>
  <div class="settings-page">
    <section class="card">
      <h3>系统配置</h3>
      <p>设置会实时生效，并保存在当前浏览器。</p>

      <div class="form-grid">
        <label>
          <span>系统字体</span>
          <select v-model="fontKey">
            <option v-for="item in FRONTEND_FONT_OPTIONS" :key="item.key" :value="item.key">{{ item.label }}</option>
          </select>
        </label>

        <label>
          <span>管理员头像</span>
          <select v-model="avatarKey">
            <option v-for="item in FRONTEND_ADMIN_AVATAR_OPTIONS" :key="item.key" :value="item.key">{{ item.label }}</option>
          </select>
        </label>

        <div class="switch-row">
          <div class="switch-copy">
            <span>侧栏悬停预览</span>
            <p>开启后，折叠侧栏可悬停预览分组菜单；关闭后仅支持点击展开。</p>
          </div>
          <button
            type="button"
            class="switch"
            :class="{ on: hoverPreviewEnabled }"
            role="switch"
            :aria-checked="hoverPreviewEnabled"
            @click="hoverPreviewEnabled = !hoverPreviewEnabled"
          >
            <span class="switch-track">
              <span class="switch-thumb"></span>
            </span>
            <span class="switch-text">{{ hoverPreviewEnabled ? '开启' : '关闭' }}</span>
          </button>
        </div>
      </div>

      <div class="preview">
        <div class="avatar">
          <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" />
          <span v-else>A</span>
        </div>
        <div>
          <h4>预览</h4>
          <p>Admin</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.settings-page {
  display: grid;
  gap: 12px;
}

.card {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

h3 {
  margin: 0;
}

p {
  margin: 8px 0 14px;
  color: #64748b;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

label span {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #334155;
}

select {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 8px 10px;
}

.switch-row {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid #d9e6f5;
  border-radius: 12px;
  background: linear-gradient(120deg, #f8fbff, #f1f7ff);
  padding: 12px 14px;
}

.switch-copy span {
  display: block;
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #1f3b64;
}

.switch-copy p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #5b7190;
}

.switch {
  border: 1px solid #c6daef;
  border-radius: 999px;
  padding: 3px 10px 3px 3px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #ffffff;
  color: #475569;
  cursor: pointer;
  min-width: 100px;
  transition: all 0.2s ease, transform 0.18s ease;
}

.switch:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 176, 0.12);
}

.switch.on {
  background: #eaf6ff;
  border-color: #76b8f3;
  color: #0f4778;
}

.switch-track {
  width: 38px;
  height: 24px;
  border-radius: 999px;
  background: #dce8f6;
  display: inline-flex;
  align-items: center;
  padding: 2px;
  transition: background 0.2s ease;
}

.switch.on .switch-track {
  background: linear-gradient(135deg, #56b8ff, #1d8ae6);
}

.switch-thumb {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  background: #ffffff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.16);
  transform: translateX(0);
  transition: transform 0.2s ease;
}

.switch.on .switch-thumb {
  transform: translateX(14px);
}

.switch-text {
  font-size: 12px;
  font-weight: 600;
  min-width: 26px;
  text-align: center;
}

@media (max-width: 640px) {
  .switch-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

.preview {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  background: #f8fafc;
}

.avatar {
  width: 44px;
  height: 44px;
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

h4 {
  margin: 0;
}
</style>
