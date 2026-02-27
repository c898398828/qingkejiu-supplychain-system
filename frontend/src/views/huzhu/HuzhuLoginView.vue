<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { huzhuAuthService } from '@/services/api'
import { isHuzhuAuthenticated, setHuzhuAuth } from '@/lib/huzhuAuth'

const REMEMBERED_USERNAME_KEY = 'huzhu-login-remembered-username'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)
const rememberUsername = ref(true)
const capsLockOn = ref(false)
const shake = ref(false)
const form = ref({ username: '', password: '' })
const panelStyle = ref<Record<string, string>>({ '--tilt-x': '0deg', '--tilt-y': '0deg' })

const canSubmit = computed(() => form.value.username.trim().length > 0 && form.value.password.trim().length > 0)

const triggerShake = () => {
  shake.value = false
  requestAnimationFrame(() => {
    shake.value = true
    setTimeout(() => {
      shake.value = false
    }, 480)
  })
}

const handlePanelMove = (event: MouseEvent) => {
  const target = event.currentTarget as HTMLElement | null
  if (!target) return
  const rect = target.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width - 0.5) * 2
  const y = ((event.clientY - rect.top) / rect.height - 0.5) * 2
  panelStyle.value = {
    '--tilt-x': `${(-y * 2.8).toFixed(2)}deg`,
    '--tilt-y': `${(x * 2.8).toFixed(2)}deg`,
  }
}

const resetPanelTilt = () => {
  panelStyle.value = { '--tilt-x': '0deg', '--tilt-y': '0deg' }
}

const updateCapsLockState = (event: KeyboardEvent) => {
  capsLockOn.value = event.getModifierState('CapsLock')
}

const submit = async () => {
  error.value = ''

  if (!canSubmit.value) {
    error.value = '请输入完整的用户名和密码'
    triggerShake()
    return
  }

  loading.value = true

  try {
    const result = await huzhuAuthService.login(form.value.username.trim(), form.value.password)

    if (result.code !== 200 || !result.data?.token) {
      error.value = result.message || '登录失败，请检查账号或密码'
      triggerShake()
      return
    }

    if (rememberUsername.value) {
      localStorage.setItem(REMEMBERED_USERNAME_KEY, form.value.username.trim())
    } else {
      localStorage.removeItem(REMEMBERED_USERNAME_KEY)
    }

    setHuzhuAuth(result.data.token, result.data.user || {})
    router.replace('/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '登录失败，请稍后再试'
    triggerShake()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isHuzhuAuthenticated()) {
    router.replace('/dashboard')
    return
  }

  const rememberedUsername = localStorage.getItem(REMEMBERED_USERNAME_KEY)
  if (rememberedUsername) {
    form.value.username = rememberedUsername
  }
})
</script>

<template>
  <div class="login-scene">
    <div class="scene-layer" aria-hidden="true">
      <div class="noise"></div>
      <div class="beam beam-left"></div>
      <div class="beam beam-right"></div>
      <div class="blueprint-grid"></div>
    </div>

    <main class="login-shell">
      <section class="brand-zone">
        <div class="brand-top">
          <p class="chip">CHAIN COMMAND CENTER</p>
          <h1>互助县青稞酒产业链<br>协同管理系统</h1>
          <p class="brand-intro">从种植基地到终端订单，用统一视图管理全链路业务和角色权限。</p>

          <div class="signal-board">
            <div class="signal-item"><span class="dot"></span> 种植与采购联动</div>
            <div class="signal-item"><span class="dot"></span> 生产与质检闭环</div>
            <div class="signal-item"><span class="dot"></span> 库存与销售追踪</div>
          </div>
        </div>

        <div class="brand-lower">
          <div class="brand-metrics">
            <article>
              <h3>16</h3>
              <p>业务模块</p>
            </article>
            <article>
              <h3>7x24</h3>
              <p>链路监控</p>
            </article>
            <article>
              <h3>Token</h3>
              <p>鉴权保护</p>
            </article>
          </div>

          <div class="brand-ops">
            <article class="ops-card">
              <h4>运行看板</h4>
              <ul>
                <li><span class="status-dot online"></span> API 网关：正常</li>
                <li><span class="status-dot online"></span> 鉴权服务：正常</li>
                <li><span class="status-dot warn"></span> 数据同步：巡检中</li>
              </ul>
            </article>
            <article class="ops-card">
              <h4>今日重点</h4>
              <div class="ops-tags">
                <span>库存盘点</span>
                <span>采购审批</span>
                <span>批次追溯</span>
                <span>订单复核</span>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section
        class="panel-zone"
        :class="{ shake }"
        :style="panelStyle"
        @mousemove="handlePanelMove"
        @mouseleave="resetPanelTilt"
      >
        <div class="panel-head">
          <h2>控制台登录</h2>
          <p>仅授权用户可访问后台功能模块</p>
        </div>

        <form class="login-form" @submit.prevent="submit">
          <div class="field field-delay-1">
            <label for="username">用户名</label>
            <input id="username" v-model="form.username" autocomplete="username" placeholder="请输入用户名" />
          </div>

          <div class="field field-delay-2">
            <label for="password">密码</label>
            <div class="password-wrap">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="请输入密码"
                @keydown="updateCapsLockState"
                @keyup="updateCapsLockState"
                @blur="capsLockOn = false"
              />
              <button
                type="button"
                class="password-toggle"
                @click="showPassword = !showPassword"
                :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              >
                {{ showPassword ? '隐藏' : '显示' }}
              </button>
            </div>
            <p v-if="capsLockOn" class="caps-tip">检测到 Caps Lock 已开启</p>
          </div>

          <div class="form-meta field-delay-3">
            <label class="remember-check">
              <input v-model="rememberUsername" type="checkbox" />
              <span>记住用户名</span>
            </label>
            <span class="security-note">已启用 Token 鉴权</span>
          </div>

          <p v-if="error" class="error">{{ error }}</p>

          <button class="submit-btn field-delay-4" type="submit" :disabled="loading || !canSubmit">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? '身份校验中...' : '进入系统' }}
          </button>
        </form>

        <div class="panel-lower">
          <div class="panel-footer">
            <div class="footer-item">
              <strong>登录安全</strong>
              <span>异常登录自动失效并回到登录页</span>
            </div>
            <div class="footer-item">
              <strong>会话策略</strong>
              <span>支持记住用户名与权限隔离访问</span>
            </div>
          </div>

          <div class="panel-notice">
            <h4>登录提醒</h4>
            <ul>
              <li>首次登录后请及时修改默认密码。</li>
              <li>操作日志已接入审计，敏感操作可追踪。</li>
              <li>若提示会话失效，请重新登录。</li>
            </ul>
          </div>
        </div>
      </section>
    </main>

    <footer class="footer-note">Graduation Project · Huzhu Barley Wine System · 2026</footer>
  </div>
</template>

<style scoped>
.login-scene {
  --ink-900: #10223a;
  --ink-700: #1f3f65;
  --ink-500: #3f5f87;
  --line-100: rgba(255, 255, 255, 0.86);
  --line-200: rgba(184, 206, 228, 0.7);
  --panel-bg: rgba(255, 255, 255, 0.84);
  --panel-shadow: rgba(15, 31, 56, 0.18);
  --accent-1: #105d8b;
  --accent-2: #ca6b27;
  --danger: #bc2f34;
  position: relative;
  min-height: 100vh;
  padding: 1rem;
  overflow: hidden;
  background: linear-gradient(152deg, #eef8ff 0%, #f8fbfd 52%, #fff4e7 100%);
}

.scene-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.noise {
  position: absolute;
  inset: 0;
  opacity: 0.08;
  background-image: radial-gradient(circle at 1px 1px, #203450 1px, transparent 0);
  background-size: 20px 20px;
}

.blueprint-grid {
  position: absolute;
  inset: 0;
  opacity: 0.36;
  background-image: linear-gradient(rgba(43, 74, 110, 0.11) 1px, transparent 1px),
    linear-gradient(90deg, rgba(43, 74, 110, 0.11) 1px, transparent 1px);
  background-size: 34px 34px;
  mask-image: radial-gradient(circle at center, black 54%, transparent 84%);
}

.beam {
  position: absolute;
  width: min(58vw, 700px);
  height: min(58vw, 700px);
  border-radius: 999px;
  filter: blur(12px);
  opacity: 0.52;
  animation: beamFloat 14s ease-in-out infinite;
}

.beam-left {
  left: -18%;
  top: -24%;
  background: radial-gradient(circle at 40% 40%, rgba(58, 176, 237, 0.66), rgba(40, 88, 186, 0.26));
}

.beam-right {
  right: -16%;
  bottom: -26%;
  animation-delay: 5s;
  background: radial-gradient(circle at 44% 38%, rgba(245, 160, 88, 0.66), rgba(196, 90, 51, 0.24));
}

.login-shell {
  position: relative;
  z-index: 2;
  width: min(1100px, 96vw);
  min-height: min(700px, 92vh);
  margin: 0 auto;
  border: 1px solid var(--line-100);
  border-radius: 28px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1.08fr 0.92fr;
  box-shadow: 0 30px 60px var(--panel-shadow);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.66), rgba(255, 255, 255, 0.46));
  backdrop-filter: blur(24px);
  perspective: 900px;
}

.brand-zone {
  padding: clamp(2rem, 4vw, 3.5rem);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: var(--ink-900);
}

.brand-top {
  display: grid;
}

.chip {
  margin: 0;
  align-self: flex-start;
  padding: 0.4rem 0.7rem;
  border-radius: 999px;
  font-size: 0.72rem;
  letter-spacing: 0.15em;
  color: #fff;
  background: linear-gradient(125deg, #1f5a85, #22739b);
  box-shadow: 0 10px 24px rgba(25, 91, 140, 0.28);
  animation: revealUp 0.6s both;
}

.brand-zone h1 {
  margin: 0.9rem 0 0;
  font-family: 'XindiXueshan', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: clamp(2rem, 4.4vw, 3rem);
  line-height: 1.16;
  animation: revealUp 0.7s both;
  animation-delay: 0.08s;
}

.brand-intro {
  margin: 1rem 0 0;
  max-width: 32rem;
  font-size: 1rem;
  line-height: 1.8;
  color: var(--ink-700);
  animation: revealUp 0.75s both;
  animation-delay: 0.16s;
}

.signal-board {
  margin-top: 1.25rem;
  display: grid;
  gap: 0.6rem;
  animation: revealUp 0.8s both;
  animation-delay: 0.24s;
}

.signal-item {
  display: inline-flex;
  align-items: center;
  gap: 0.65rem;
  color: #214567;
  font-size: 0.95rem;
}

.dot {
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
  box-shadow: 0 0 0 0 rgba(202, 107, 39, 0.58);
  animation: pulseDot 2.6s infinite;
}

.brand-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.65rem;
}

.brand-lower {
  margin-top: 1.1rem;
  display: grid;
  gap: 0.75rem;
}

.brand-metrics article {
  border-radius: 0.9rem;
  padding: 0.7rem 0.8rem;
  background: rgba(255, 255, 255, 0.46);
  border: 1px solid rgba(166, 194, 221, 0.34);
}

.brand-metrics h3 {
  margin: 0;
  font-size: 1rem;
  color: #1a4974;
}

.brand-metrics p {
  margin: 0.2rem 0 0;
  font-size: 0.76rem;
  color: #486a8f;
}

.brand-ops {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.65rem;
}

.ops-card {
  border-radius: 0.9rem;
  padding: 0.74rem 0.82rem;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(166, 194, 221, 0.38);
}

.ops-card h4 {
  margin: 0;
  font-size: 0.82rem;
  color: #1d4674;
}

.ops-card ul {
  margin: 0.46rem 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 0.34rem;
  color: #4c6888;
  font-size: 0.74rem;
}

.status-dot {
  display: inline-block;
  width: 0.46rem;
  height: 0.46rem;
  border-radius: 999px;
  margin-right: 0.38rem;
}

.status-dot.online {
  background: #10b981;
}

.status-dot.warn {
  background: #f59e0b;
}

.ops-tags {
  margin-top: 0.5rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.38rem;
}

.ops-tags span {
  font-size: 0.72rem;
  color: #2f5a87;
  padding: 0.24rem 0.45rem;
  border-radius: 999px;
  background: rgba(215, 233, 250, 0.72);
}

.panel-zone {
  padding: clamp(1.5rem, 3vw, 2.3rem);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  border-left: 1px solid var(--line-200);
  background: var(--panel-bg);
  transform: rotateX(var(--tilt-x)) rotateY(var(--tilt-y));
  transition: transform 0.18s ease;
}

.panel-head h2 {
  margin: 0;
  font-family: 'XindiJinzhong', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: clamp(1.7rem, 2.8vw, 2.1rem);
  color: var(--ink-900);
  animation: revealUp 0.5s both;
}

.panel-head p {
  margin: 0.5rem 0 1.2rem;
  color: var(--ink-500);
  font-size: 0.9rem;
  animation: revealUp 0.56s both;
  animation-delay: 0.06s;
}

.login-form {
  display: grid;
  gap: 0.88rem;
}

.field {
  animation: revealUp 0.56s both;
}

.field-delay-1 { animation-delay: 0.1s; }
.field-delay-2 { animation-delay: 0.16s; }
.field-delay-3 { animation-delay: 0.22s; }
.field-delay-4 { animation-delay: 0.28s; }

.field label {
  display: block;
  margin-left: 0.14rem;
  margin-bottom: 0.34rem;
  font-size: 0.76rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #2f557d;
}

.field input {
  width: 100%;
  min-height: 2.95rem;
  border: 1px solid #c6d8eb;
  border-radius: 0.9rem;
  background: rgba(248, 252, 255, 0.92);
  color: #17365c;
  padding: 0.66rem 0.84rem;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.field input:hover {
  border-color: #8eb2d8;
}

.field input:focus {
  outline: none;
  border-color: #2e75c6;
  box-shadow: 0 0 0 4px rgba(40, 106, 186, 0.2);
  transform: translateY(-1px);
}

.password-wrap {
  position: relative;
}

.password-wrap input {
  padding-right: 3.7rem;
}

.password-toggle {
  position: absolute;
  right: 0.45rem;
  top: 50%;
  transform: translateY(-50%);
  border: 0;
  border-radius: 0.66rem;
  padding: 0.35rem 0.55rem;
  background: rgba(37, 90, 154, 0.12);
  color: #245895;
  font-size: 0.72rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s ease;
}

.password-toggle:hover {
  background: rgba(37, 90, 154, 0.2);
}

.caps-tip {
  margin: 0.36rem 0 0;
  font-size: 0.78rem;
  color: #be4d0c;
}

.form-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 1.5rem;
}

.remember-check {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.84rem;
  color: #35597e;
  cursor: pointer;
}

.remember-check input {
  width: 1rem;
  height: 1rem;
  accent-color: #2f6ab1;
}

.security-note {
  font-size: 0.78rem;
  color: #5c7897;
}

.error {
  margin: 0;
  border-radius: 0.9rem;
  border: 1px solid rgba(188, 47, 52, 0.26);
  background: rgba(255, 236, 237, 0.86);
  color: var(--danger);
  font-size: 0.82rem;
  padding: 0.62rem 0.75rem;
}

.submit-btn {
  width: 100%;
  min-height: 3rem;
  margin-top: 0.2rem;
  border: none;
  border-radius: 0.95rem;
  background: linear-gradient(135deg, var(--accent-1), #1f6d8c, var(--accent-2));
  background-size: 200% 100%;
  color: #fff;
  font-size: 0.94rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  cursor: pointer;
  box-shadow: 0 14px 24px rgba(27, 93, 137, 0.28);
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-position 0.35s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 30px rgba(27, 93, 137, 0.33);
  background-position: 100% 0;
}

.submit-btn:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.panel-footer {
  display: grid;
  gap: 0.55rem;
}

.panel-lower {
  margin-top: 0.85rem;
  display: grid;
  gap: 0.75rem;
}

.footer-item {
  border-radius: 0.78rem;
  border: 1px solid rgba(171, 196, 222, 0.42);
  background: rgba(255, 255, 255, 0.48);
  padding: 0.58rem 0.7rem;
  display: grid;
  gap: 0.16rem;
}

.footer-item strong {
  font-size: 0.78rem;
  color: #1d4674;
}

.footer-item span {
  font-size: 0.74rem;
  color: #5e7d9f;
}

.panel-notice {
  border-radius: 0.86rem;
  border: 1px solid rgba(171, 196, 222, 0.44);
  background: rgba(255, 255, 255, 0.52);
  padding: 0.62rem 0.72rem;
}

.panel-notice h4 {
  margin: 0;
  font-size: 0.8rem;
  color: #1d4674;
}

.panel-notice ul {
  margin: 0.5rem 0 0;
  padding-left: 1rem;
  display: grid;
  gap: 0.26rem;
  color: #5f7f9f;
  font-size: 0.73rem;
}

.spinner {
  width: 0.95rem;
  height: 0.95rem;
  border-radius: 999px;
  margin-right: 0.52rem;
  border: 2px solid rgba(255, 255, 255, 0.36);
  border-top-color: #fff;
  animation: spin 0.8s linear infinite;
}

.footer-note {
  position: relative;
  z-index: 2;
  margin-top: 0.52rem;
  text-align: center;
  color: rgba(19, 40, 67, 0.56);
  font-size: 0.75rem;
  letter-spacing: 0.05em;
}

.shake {
  animation: shakePanel 0.46s ease;
}

@keyframes revealUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes beamFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(1.03);
  }
}

@keyframes pulseDot {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(202, 107, 39, 0.52);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(202, 107, 39, 0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes shakePanel {
  0%, 100% { transform: rotateX(var(--tilt-x)) rotateY(var(--tilt-y)) translateX(0); }
  20% { transform: rotateX(var(--tilt-x)) rotateY(var(--tilt-y)) translateX(-6px); }
  40% { transform: rotateX(var(--tilt-x)) rotateY(var(--tilt-y)) translateX(5px); }
  60% { transform: rotateX(var(--tilt-x)) rotateY(var(--tilt-y)) translateX(-4px); }
  80% { transform: rotateX(var(--tilt-x)) rotateY(var(--tilt-y)) translateX(3px); }
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
    min-height: auto;
    max-width: 680px;
  }

  .panel-zone {
    border-left: 0;
    border-top: 1px solid var(--line-200);
    transform: none;
  }

  .brand-zone {
    padding-bottom: 1.25rem;
  }

  .brand-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .brand-ops {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .login-scene {
    padding: 0.75rem;
  }

  .login-shell {
    width: 100%;
    border-radius: 20px;
  }

  .brand-zone {
    padding: 1.2rem;
  }

  .panel-zone {
    padding: 1.2rem;
  }

  .form-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.4rem;
  }

  .brand-metrics {
    grid-template-columns: 1fr;
  }

  .panel-lower {
    margin-top: 1rem;
  }
}
</style>
