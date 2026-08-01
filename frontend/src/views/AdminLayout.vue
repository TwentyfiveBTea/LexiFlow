/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员后台布局
 */
<script setup lang="ts">
import { BarChart3, Gift, LayoutDashboard, LogOut, ShieldCheck } from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'

const router = useRouter()
const username = computed(() => localStorage.getItem('lexiflow.admin.username') ?? 'admin')

async function logout() {
  localStorage.removeItem('lexiflow.admin.token')
  localStorage.removeItem('lexiflow.admin.username')
  await router.replace('/admin/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand"><span><ShieldCheck :size="21" /></span><strong>LexiFlow</strong><small>ADMIN</small></div>
      <nav aria-label="管理员导航">
        <RouterLink to="/admin/overview"><LayoutDashboard :size="18" />总览</RouterLink>
        <RouterLink to="/admin/credits"><BarChart3 :size="18" />Credits 使用</RouterLink>
        <RouterLink to="/admin/grant"><Gift :size="18" />赠送 Credits</RouterLink>
      </nav>
      <div class="admin-session"><span>{{ username }}</span><button type="button" title="退出管理员后台" aria-label="退出管理员后台" @click="logout"><LogOut :size="17" /></button></div>
    </aside>
    <main class="admin-main"><RouterView /></main>
  </div>
</template>

<style scoped>
.admin-shell { min-height: 100vh; display: grid; grid-template-columns: 232px minmax(0, 1fr); color: #22362f; background: #f3f5f3; }.admin-sidebar { position: sticky; top: 0; height: 100vh; display: flex; flex-direction: column; padding: 24px 14px 18px; color: #dce8e1; background: #183b32; }.admin-brand { display: flex; align-items: center; gap: 9px; padding: 0 10px 28px; border-bottom: 1px solid rgba(220,232,225,.18); }.admin-brand > span { width: 31px; height: 31px; display: grid; place-items: center; border-radius: 6px; color: #183b32; background: #d9e9df; }.admin-brand strong { color: #fff; font-size: 17px; }.admin-brand small { margin-left: auto; color: #a5c3b4; font-size: 9px; font-weight: 750; letter-spacing: .08em; }.admin-sidebar nav { display: grid; gap: 4px; padding-top: 22px; }.admin-sidebar nav a { min-height: 42px; display: flex; align-items: center; gap: 10px; padding: 0 11px; border-radius: 6px; color: #c2d5cb; font-size: 13px; font-weight: 650; }.admin-sidebar nav a:hover { color: #fff; background: rgba(255,255,255,.08); }.admin-sidebar nav a.router-link-active { color: #183b32; background: #d9e9df; }.admin-session { display: flex; align-items: center; gap: 8px; margin-top: auto; padding: 14px 10px 0; border-top: 1px solid rgba(220,232,225,.18); color: #dce8e1; font-size: 12px; }.admin-session span { overflow: hidden; flex: 1; text-overflow: ellipsis; white-space: nowrap; }.admin-session button { width: 30px; height: 30px; display: grid; place-items: center; border: 0; border-radius: 5px; color: #c2d5cb; background: transparent; }.admin-session button:hover { color: #fff; background: rgba(255,255,255,.08); }.admin-main { min-width: 0; padding: 38px clamp(22px, 4vw, 58px); }
@media (max-width: 760px) { .admin-shell { grid-template-columns: 1fr; }.admin-sidebar { position: static; height: auto; display: grid; grid-template-columns: 1fr auto; gap: 16px; padding: 15px 18px; }.admin-brand { padding: 0; border: 0; }.admin-sidebar nav { grid-column: 1 / -1; grid-template-columns: repeat(3, minmax(0, 1fr)); padding: 0; }.admin-sidebar nav a { justify-content: center; padding: 0 6px; font-size: 11px; }.admin-sidebar nav a svg { display: none; }.admin-session { margin: 0; padding: 0; border: 0; }.admin-main { padding: 26px 18px; } }
</style>
