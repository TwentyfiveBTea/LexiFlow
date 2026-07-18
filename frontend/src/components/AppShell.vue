<script setup lang="ts">
import {
  BookOpen,
  Brain,
  CircleUserRound,
  Gauge,
  Library,
  LogOut,
  Menu,
  PanelLeftClose,
  PanelLeftOpen,
  Settings,
  WalletCards,
  X,
} from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { getUserProfile } from '@/lib/api'
import { useSessionStore } from '@/stores/session'
import BrandMark from './BrandMark.vue'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const collapsed = ref(false)
const mobileOpen = ref(false)
const profileOpen = ref(false)
const profileLoaded = ref(false)
const profileLoading = ref(false)
const avatarError = ref(false)
const profileArea = ref<HTMLElement | null>(null)

onClickOutside(profileArea, () => {
  profileOpen.value = false
})

async function logout() {
  profileOpen.value = false
  session.signOut()
  await router.push('/login')
}

async function toggleProfile() {
  profileOpen.value = !profileOpen.value
  if (!profileOpen.value || profileLoaded.value || profileLoading.value) return

  profileLoading.value = true
  try {
    session.updateProfile(await getUserProfile())
    avatarError.value = false
    profileLoaded.value = true
  } catch {
    // Keep the cached profile visible when the API is temporarily unavailable.
  } finally {
    profileLoading.value = false
  }
}

const navItems = [
  { label: '首页', to: '/dashboard', icon: Gauge },
  { label: '我的图书馆', to: '/articles', icon: Library },
  { label: '词汇库', to: '/vocabulary', icon: BookOpen },
  { label: '记忆卡牌', to: '/vocabulary/core', icon: Brain },
  { label: '钱包', to: '/wallet', icon: WalletCards },
  { label: '设置', to: '/settings', icon: Settings },
]

const currentLabel = computed(() => navItems.find((item) => route.path.startsWith(item.to))?.label ?? 'LexiFlow')
</script>

<template>
  <div class="app-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <button class="mobile-close icon-btn" aria-label="关闭导航" @click="mobileOpen = false"><X :size="19" /></button>
      <div class="sidebar-head">
        <button v-if="collapsed" class="brand-toggle" aria-label="打开边栏" @click="collapsed = false">
          <span class="brand-idle"><BrandMark compact /></span>
          <PanelLeftOpen class="brand-open-icon" :size="19" />
          <span class="brand-tooltip" role="tooltip">打开边栏</span>
        </button>
        <BrandMark v-else />
        <button v-if="!collapsed" class="collapse-btn icon-btn" aria-label="收起边栏" title="收起边栏" @click="collapsed = true">
          <PanelLeftClose :size="19" />
        </button>
      </div>

      <nav class="sidebar-nav" aria-label="主导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :title="collapsed ? item.label : undefined"
          @click="mobileOpen = false"
        >
          <component :is="item.icon" :size="19" :stroke-width="1.7" />
          <span v-if="!collapsed">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-foot" :class="{ centered: collapsed }">
        <div ref="profileArea" class="profile-area">
          <Transition name="profile-popover">
            <section v-if="profileOpen" id="account-popover" class="account-popover" aria-label="账户信息" :aria-busy="profileLoading">
              <div class="account-identity">
                <span class="account-avatar">
                  <img v-if="session.avatar && !avatarError" :src="session.avatar" alt="" @error="avatarError = true" />
                  <CircleUserRound v-else :size="27" :stroke-width="1.45" />
                </span>
                <span class="account-email" :title="session.email">{{ session.email }}</span>
                <small>这是你注册的第 {{ session.registeredDays }} 天</small>
              </div>
              <button class="logout-button" type="button" @click="logout">
                <LogOut :size="16" :stroke-width="1.8" />
                <span>退出登录</span>
              </button>
            </section>
          </Transition>

          <button
            class="profile"
            :class="{ centered: collapsed, active: profileOpen }"
            type="button"
            :aria-label="collapsed ? session.userName : undefined"
            aria-haspopup="dialog"
            aria-controls="account-popover"
            :aria-expanded="profileOpen"
            @click="toggleProfile"
            @keydown.esc="profileOpen = false"
          >
            <span class="avatar">
              <img v-if="session.avatar && !avatarError" :src="session.avatar" alt="" @error="avatarError = true" />
              <CircleUserRound v-else :size="20" :stroke-width="1.6" />
            </span>
            <span v-if="!collapsed" class="profile-copy"><strong>{{ session.userName }}</strong></span>
          </button>
        </div>
      </div>
    </aside>

    <div v-if="mobileOpen" class="mobile-scrim" @click="mobileOpen = false"></div>

    <section class="app-content">
      <header class="mobile-header">
        <button class="icon-btn" aria-label="打开导航" @click="mobileOpen = true"><Menu :size="20" /></button>
        <strong class="serif">{{ currentLabel }}</strong>
        <BrandMark compact />
      </header>
      <RouterView />
    </section>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; }
.sidebar { position: fixed; z-index: 40; inset: 0 auto 0 0; width: var(--sidebar-width); display: flex; flex-direction: column; border-right: 1px solid var(--outline); background: var(--surface-lowest); transition: width .22s ease; }
.sidebar-head { min-height: 74px; display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 16px 18px; border-bottom: 1px solid rgba(199,196,192,.7); }
.brand-toggle { position: relative; width: 44px; height: 44px; display: grid; place-items: center; padding: 0; border: 0; border-radius: 8px; color: var(--ink); background: transparent; }
.brand-toggle:hover, .brand-toggle:focus-visible { background: var(--surface-low); outline: none; }
.brand-idle, .brand-open-icon { grid-area: 1 / 1; transition: opacity .15s ease; }
.brand-open-icon { opacity: 0; }
.brand-toggle:hover .brand-idle, .brand-toggle:focus-visible .brand-idle { opacity: 0; }
.brand-toggle:hover .brand-open-icon, .brand-toggle:focus-visible .brand-open-icon { opacity: 1; }
.brand-tooltip { position: absolute; z-index: 70; left: calc(100% + 10px); top: 50%; padding: 6px 9px; border-radius: 6px; color: white; background: #242424; font-size: 12px; font-weight: 600; line-height: 18px; white-space: nowrap; opacity: 0; pointer-events: none; transform: translate(4px, -50%); transition: opacity .15s ease, transform .15s ease; }
.brand-toggle:hover .brand-tooltip, .brand-toggle:focus-visible .brand-tooltip { opacity: 1; transform: translate(0, -50%); }
.profile-area { position: relative; width: 100%; }
.profile { width: 100%; min-height: 42px; display: flex; align-items: center; gap: 10px; padding: 4px 6px; border: 0; border-radius: 7px; color: inherit; background: transparent; text-align: left; transition: color .16s ease, background-color .16s ease; }
.profile:hover, .profile:focus-visible, .profile.active { color: var(--primary); background: var(--surface-low); outline: none; }
.profile.centered { justify-content: center; }
.avatar { width: 35px; height: 35px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 50%; background: var(--surface-high); color: var(--secondary); }
.avatar img, .account-avatar img { width: 100%; height: 100%; display: block; border-radius: inherit; object-fit: cover; }
.profile-copy { display: grid; min-width: 0; }
.profile-copy strong { overflow: hidden; color: var(--secondary); font-family: 'Literata', Georgia, serif; font-size: 13.5px; font-weight: 650; line-height: 18px; text-overflow: ellipsis; white-space: nowrap; }
.account-popover { position: absolute; z-index: 80; left: 0; bottom: calc(100% + 12px); width: 224px; border: 1px solid rgba(199,196,192,.82); border-radius: 8px; color: var(--ink); background: var(--surface-lowest); box-shadow: 0 14px 38px rgba(45,45,45,.13), 0 2px 8px rgba(45,45,45,.05); }
.account-popover::before, .account-popover::after { position: absolute; left: 17px; width: 0; height: 0; content: ''; border-style: solid; }
.account-popover::before { bottom: -8px; border-width: 8px 8px 0; border-color: rgba(199,196,192,.82) transparent transparent; }
.account-popover::after { bottom: -7px; border-width: 7px 7px 0; border-color: var(--surface-lowest) transparent transparent; transform: translateX(1px); }
.account-identity { display: flex; flex-direction: column; align-items: center; padding: 21px 18px 17px; text-align: center; }
.account-avatar { width: 48px; height: 48px; display: grid; place-items: center; margin-bottom: 11px; border-radius: 50%; color: var(--secondary); background: var(--surface-high); }
.account-email { max-width: 100%; overflow: hidden; margin-top: 3px; color: var(--ink-muted); font-size: 11.5px; line-height: 17px; text-overflow: ellipsis; white-space: nowrap; }
.account-identity small { margin-top: 3px; color: var(--ink-muted); font-size: 11px; line-height: 17px; }
.logout-button { width: 100%; min-height: 43px; display: flex; align-items: center; justify-content: center; gap: 7px; padding: 0 14px; border: 0; border-top: 1px solid rgba(199,196,192,.65); border-radius: 0 0 7px 7px; color: var(--error); background: transparent; font-size: 12px; font-weight: 650; }
.logout-button:hover, .logout-button:focus-visible { background: #fff7f5; outline: none; }
.profile-popover-enter-active, .profile-popover-leave-active { transition: opacity .16s ease, transform .16s ease; transform-origin: left bottom; }
.profile-popover-enter-from, .profile-popover-leave-to { opacity: 0; transform: translateY(5px) scale(.98); }
.sidebar-nav { padding: 12px 8px; display: grid; gap: 3px; overflow-y: auto; }
.nav-link { min-height: 44px; display: flex; align-items: center; gap: 12px; padding: 0 13px; border-radius: 7px; color: var(--ink-muted); font-size: 14px; font-weight: 550; white-space: nowrap; }
.nav-link:hover { color: var(--primary); background: var(--surface-low); }
.nav-link.router-link-active { color: #5d3b1e; background: var(--secondary-soft); }
.sidebar-foot { margin-top: auto; min-height: 66px; display: flex; align-items: center; padding: 13px 16px; border-top: 1px solid rgba(199,196,192,.7); color: var(--ink-muted); font-size: 11px; }
.sidebar-foot.centered { min-height: 66px; align-items: center; padding-inline: 0; }
.app-content { min-height: 100vh; margin-left: var(--sidebar-width); transition: margin-left .22s ease; }
.is-collapsed .sidebar { width: 78px; }
.is-collapsed .sidebar-head { justify-content: center; padding-inline: 10px; }
.is-collapsed .nav-link { justify-content: center; padding: 0; }
.is-collapsed .account-popover { left: calc(100% + 12px); bottom: -1px; }
.is-collapsed .account-popover::before, .is-collapsed .account-popover::after { top: auto; right: auto; bottom: 18px; left: -8px; border-width: 8px 8px 8px 0; border-color: transparent rgba(199,196,192,.82) transparent transparent; }
.is-collapsed .account-popover::after { left: -7px; border-width: 7px 7px 7px 0; border-color: transparent var(--surface-lowest) transparent transparent; transform: translateY(-1px); }
.is-collapsed .app-content { margin-left: 78px; }
.mobile-header, .mobile-close, .mobile-scrim { display: none; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }

@media (max-width: 800px) {
  .sidebar, .is-collapsed .sidebar { width: min(86vw, 292px); transform: translateX(-100%); transition: transform .22s ease; }
  .sidebar.open { transform: translateX(0); }
  .is-collapsed .sidebar-head { flex-direction: row; justify-content: space-between; padding-inline: 18px; }
  .is-collapsed .collapse-btn, .collapse-btn { display: none; }
  .is-collapsed .nav-link { justify-content: flex-start; padding-inline: 13px; }
  .mobile-close { display: grid; position: absolute; right: 10px; top: 10px; z-index: 2; }
  .mobile-scrim { display: block; position: fixed; z-index: 35; inset: 0; background: rgba(27,28,28,.25); }
  .app-content, .is-collapsed .app-content { margin-left: 0; }
  .mobile-header { height: 58px; display: flex; align-items: center; justify-content: space-between; padding: 0 14px; border-bottom: 1px solid var(--outline); background: rgba(252,249,248,.96); }
}
</style>
