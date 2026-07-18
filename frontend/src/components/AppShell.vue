<script setup lang="ts">
import {
  BookOpen,
  Brain,
  ChevronLeft,
  ChevronRight,
  CircleUserRound,
  Gauge,
  Library,
  Menu,
  Settings,
  Upload,
  WalletCards,
  X,
} from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import BrandMark from './BrandMark.vue'

const route = useRoute()
const collapsed = ref(false)
const mobileOpen = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const uploadedFile = ref('')

const navItems = [
  { label: '首页', to: '/dashboard', icon: Gauge },
  { label: '我的图书馆', to: '/articles', icon: Library },
  { label: '词汇库', to: '/vocabulary', icon: BookOpen },
  { label: '记忆卡牌', to: '/vocabulary/core', icon: Brain },
  { label: '钱包', to: '/wallet', icon: WalletCards },
  { label: '设置', to: '/settings', icon: Settings },
]

const currentLabel = computed(() => navItems.find((item) => route.path.startsWith(item.to))?.label ?? 'LexiFlow')

function handleUpload(event: Event) {
  const target = event.target as HTMLInputElement
  uploadedFile.value = target.files?.[0]?.name ?? ''
}
</script>

<template>
  <div class="app-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="sidebar" :class="{ open: mobileOpen }">
      <button class="mobile-close icon-btn" aria-label="关闭导航" @click="mobileOpen = false"><X :size="19" /></button>
      <div class="sidebar-head">
        <BrandMark :compact="collapsed" />
        <button class="collapse-btn icon-btn" :aria-label="collapsed ? '展开导航' : '收起导航'" @click="collapsed = !collapsed">
          <ChevronRight v-if="collapsed" :size="18" />
          <ChevronLeft v-else :size="18" />
        </button>
      </div>

      <div class="profile" :class="{ centered: collapsed }">
        <span class="avatar"><CircleUserRound :size="22" :stroke-width="1.6" /></span>
        <span v-if="!collapsed" class="profile-copy"><strong>Reading Room</strong><small>深度学习模式</small></span>
      </div>

      <button class="upload-btn" @click="fileInput?.click()">
        <Upload :size="17" /><span v-if="!collapsed">上传文章</span>
      </button>
      <input ref="fileInput" class="sr-only" type="file" accept=".pdf,.doc,.docx,.txt,.md" @change="handleUpload" />
      <p v-if="uploadedFile && !collapsed" class="upload-status" :title="uploadedFile">已选择：{{ uploadedFile }}</p>

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
        <span class="status-dot"></span><span v-if="!collapsed">今日已专注 42 分钟</span>
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
.profile { display: flex; align-items: center; gap: 11px; margin: 18px 16px 12px; min-height: 42px; }
.profile.centered { justify-content: center; }
.avatar { width: 40px; height: 40px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 50%; background: var(--surface-high); color: var(--secondary); }
.profile-copy { display: grid; min-width: 0; }
.profile-copy strong { color: var(--secondary); font-family: 'Literata', Georgia, serif; font-size: 14px; white-space: nowrap; }
.profile-copy small { color: var(--ink-muted); font-size: 11px; margin-top: 2px; }
.upload-btn { min-height: 42px; margin: 0 16px 6px; display: flex; align-items: center; justify-content: center; gap: 8px; border: 0; border-radius: 7px; color: white; background: var(--primary); font-weight: 650; }
.upload-btn:hover { background: var(--primary-hover); }
.upload-status { margin: 3px 18px 4px; overflow: hidden; color: var(--ink-muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.sidebar-nav { padding: 12px 8px; display: grid; gap: 3px; overflow-y: auto; }
.nav-link { min-height: 44px; display: flex; align-items: center; gap: 12px; padding: 0 13px; border-radius: 7px; color: var(--ink-muted); font-size: 14px; font-weight: 550; white-space: nowrap; }
.nav-link:hover { color: var(--primary); background: var(--surface-low); }
.nav-link.router-link-active { color: #5d3b1e; background: var(--secondary-soft); }
.sidebar-foot { margin-top: auto; min-height: 58px; display: flex; align-items: center; gap: 8px; padding: 0 20px; border-top: 1px solid rgba(199,196,192,.7); color: var(--ink-muted); font-size: 11px; }
.sidebar-foot.centered { justify-content: center; padding-inline: 0; }
.status-dot { width: 7px; height: 7px; flex: 0 0 auto; border-radius: 50%; background: var(--success); }
.app-content { min-height: 100vh; margin-left: var(--sidebar-width); transition: margin-left .22s ease; }
.is-collapsed .sidebar { width: 78px; }
.is-collapsed .sidebar-head { justify-content: center; padding-inline: 10px; flex-direction: column; }
.is-collapsed .collapse-btn { position: absolute; top: 76px; right: -18px; border: 1px solid var(--outline); background: white; }
.is-collapsed .upload-btn { margin-inline: 13px; padding: 0; }
.is-collapsed .nav-link { justify-content: center; padding: 0; }
.is-collapsed .app-content { margin-left: 78px; }
.mobile-header, .mobile-close, .mobile-scrim { display: none; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); white-space: nowrap; border: 0; }

@media (max-width: 800px) {
  .sidebar, .is-collapsed .sidebar { width: min(86vw, 292px); transform: translateX(-100%); transition: transform .22s ease; }
  .sidebar.open { transform: translateX(0); }
  .is-collapsed .sidebar-head { flex-direction: row; justify-content: space-between; padding-inline: 18px; }
  .is-collapsed .collapse-btn, .collapse-btn { display: none; }
  .is-collapsed .nav-link { justify-content: flex-start; padding-inline: 13px; }
  .is-collapsed .upload-btn { margin-inline: 16px; padding-inline: 12px; }
  .mobile-close { display: grid; position: absolute; right: 10px; top: 10px; z-index: 2; }
  .mobile-scrim { display: block; position: fixed; z-index: 35; inset: 0; background: rgba(27,28,28,.25); }
  .app-content, .is-collapsed .app-content { margin-left: 0; }
  .mobile-header { height: 58px; display: flex; align-items: center; justify-content: space-between; padding: 0 14px; border-bottom: 1px solid var(--outline); background: rgba(252,249,248,.96); }
}
</style>
