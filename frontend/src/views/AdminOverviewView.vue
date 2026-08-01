/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员总览页面
 */
<script setup lang="ts">
import { ChevronLeft, ChevronRight, RefreshCw, UsersRound } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { getAdminUsers } from '@/lib/api'
import type { AdminUserResponse, PageResponse } from '@/lib/api'

const page = ref(1)
const pageSize = 10
const data = ref<PageResponse<AdminUserResponse> | null>(null)
const loading = ref(false)
const error = ref('')
const totalLabel = computed(() => data.value?.total.toLocaleString() ?? '-')
const canPrevious = computed(() => page.value > 1 && !loading.value)
const canNext = computed(() => Boolean(data.value && page.value < data.value.totalPages && !loading.value))

function formatDate(value: string) {
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short', hour12: false }).format(new Date(value))
}

async function load(nextPage = page.value) {
  page.value = nextPage
  loading.value = true
  error.value = ''
  try {
    data.value = await getAdminUsers(page.value, pageSize)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '用户数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(() => { void load() })
</script>

<template>
  <section class="admin-page">
    <header class="admin-header"><div><p>Administration · Users</p><h1 class="serif">用户总览</h1><span>注册数量与最新注册用户</span></div><button class="icon-button" type="button" aria-label="刷新用户数据" title="刷新" :disabled="loading" @click="load()"><RefreshCw :size="18" :class="{ spin: loading }" /></button></header>
    <section class="metric"><span><UsersRound :size="21" /></span><div><small>注册用户</small><strong class="serif">{{ totalLabel }}</strong></div></section>
    <section class="table-panel"><header><div><h2 class="serif">注册列表</h2><p>按注册时间倒序</p></div><small v-if="data">第 {{ data.page }} / {{ data.totalPages || 1 }} 页</small></header><div v-if="loading" class="table-state">正在加载用户数据...</div><div v-else-if="error" class="table-state error">{{ error }}</div><div v-else-if="!data?.records.length" class="table-state">暂无用户数据</div><div v-else class="table-wrap"><table><thead><tr><th>用户 ID</th><th>用户名</th><th>注册时间</th></tr></thead><tbody><tr v-for="user in data.records" :key="user.userId"><td class="identifier">{{ user.userId }}</td><td><strong>{{ user.username || '-' }}</strong></td><td>{{ formatDate(user.registeredAt) }}</td></tr></tbody></table></div><footer><span>共 {{ data?.total ?? 0 }} 位用户</span><div><button type="button" aria-label="上一页" title="上一页" :disabled="!canPrevious" @click="load(page - 1)"><ChevronLeft :size="16" /></button><button type="button" aria-label="下一页" title="下一页" :disabled="!canNext" @click="load(page + 1)"><ChevronRight :size="16" /></button></div></footer></section>
  </section>
</template>

<style scoped>
.admin-page { max-width: 1280px; margin: 0 auto; }.admin-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 30px; }.admin-header p { margin: 0 0 8px; color: #628074; font-size: 11px; font-weight: 750; letter-spacing: .07em; text-transform: uppercase; }.admin-header h1 { margin: 0; color: #1f3b33; font-size: 34px; }.admin-header span { display: block; margin-top: 7px; color: #72827b; font-size: 13px; }.icon-button { width: 38px; height: 38px; display: grid; place-items: center; border: 1px solid #c9d6d0; border-radius: 6px; color: #315d4f; background: #fff; }.icon-button:hover:not(:disabled) { background: #e9f0ec; }.icon-button:disabled { opacity: .55; cursor: wait; }.metric { width: min(100%, 280px); display: flex; align-items: center; gap: 13px; padding: 20px; margin-bottom: 22px; border: 1px solid #cbd8d2; border-radius: 7px; background: #fff; }.metric > span { width: 42px; height: 42px; display: grid; place-items: center; border-radius: 6px; color: #315d4f; background: #e3eee8; }.metric small { display: block; color: #72827b; font-size: 11px; }.metric strong { display: block; margin-top: 3px; color: #1f3b33; font-size: 28px; }.table-panel { overflow: hidden; border: 1px solid #cbd8d2; border-radius: 7px; background: #fff; }.table-panel > header, .table-panel footer { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 17px 20px; }.table-panel > header { border-bottom: 1px solid #dce4df; }.table-panel h2 { margin: 0; color: #1f3b33; font-size: 20px; }.table-panel header p, .table-panel header small, .table-panel footer { color: #72827b; font-size: 11px; }.table-panel header p { margin: 3px 0 0; }.table-panel footer { border-top: 1px solid #dce4df; }.table-panel footer div { display: flex; gap: 6px; }.table-panel footer button { width: 31px; height: 31px; display: grid; place-items: center; border: 1px solid #cbd8d2; border-radius: 5px; color: #315d4f; background: #fff; }.table-panel footer button:disabled { opacity: .4; cursor: not-allowed; }.table-wrap { overflow-x: auto; }table { width: 100%; border-collapse: collapse; text-align: left; }th { padding: 12px 20px; color: #71817a; background: #f4f7f5; font-size: 10px; font-weight: 750; letter-spacing: .05em; }td { padding: 15px 20px; border-top: 1px solid #e4e9e6; color: #53645c; font-size: 12px; }td strong { color: #263e36; }.identifier { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; color: #527467; }.table-state { padding: 54px 20px; color: #72827b; font-size: 13px; text-align: center; }.table-state.error { color: var(--error); }.spin { animation: spin 1s linear infinite; }@keyframes spin { to { transform: rotate(360deg); } }
</style>
