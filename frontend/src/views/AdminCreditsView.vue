/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员Credits使用页面
 */
<script setup lang="ts">
import { ChevronLeft, ChevronRight, RefreshCw } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { getAdminCreditsSummary, getAdminCreditUsage } from '@/lib/api'
import type { AdminCreditUsageResponse, AdminCreditsSummaryResponse, PageResponse } from '@/lib/api'

const summary = ref<AdminCreditsSummaryResponse | null>(null)
const usage = ref<PageResponse<AdminCreditUsageResponse> | null>(null)
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = 10
const periods = computed(() => summary.value ? [
  { label: '近 1 天', credits: summary.value.lastDayCredits }, { label: '近 3 天', credits: summary.value.lastThreeDaysCredits }, { label: '近 7 天', credits: summary.value.lastSevenDaysCredits }, { label: '近 30 天', credits: summary.value.lastThirtyDaysCredits },
] : [])

function formatDate(value: string) { return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short', hour12: false }).format(new Date(value)) }
function number(value: number) { return value.toLocaleString() }
async function load(nextPage = page.value) { page.value = nextPage; loading.value = true; error.value = ''; try { const [nextSummary, nextUsage] = await Promise.all([getAdminCreditsSummary(), getAdminCreditUsage(page.value, pageSize)]); summary.value = nextSummary; usage.value = nextUsage } catch (cause) { error.value = cause instanceof Error ? cause.message : 'Credits数据加载失败' } finally { loading.value = false } }
onMounted(() => { void load() })
</script>

<template>
  <section class="admin-page"><header class="admin-header"><div><p>Administration · Credits</p><h1 class="serif">Credits 使用</h1><span>全部用户的文章处理消耗</span></div><button class="icon-button" type="button" aria-label="刷新Credits数据" title="刷新" :disabled="loading" @click="load()"><RefreshCw :size="18" :class="{ spin: loading }" /></button></header><section class="metrics"><article v-for="item in periods" :key="item.label"><small>{{ item.label }}</small><strong class="serif">{{ number(item.credits) }}</strong><span>Credits 已使用</span></article></section><section class="table-panel"><header><div><h2 class="serif">使用记录</h2><p>已结算的文章处理消耗</p></div><small v-if="usage">第 {{ usage.page }} / {{ usage.totalPages || 1 }} 页</small></header><div v-if="loading" class="table-state">正在加载Credits数据...</div><div v-else-if="error" class="table-state error">{{ error }}</div><div v-else-if="!usage?.records.length" class="table-state">暂无使用记录</div><div v-else class="table-wrap"><table><thead><tr><th>用户</th><th>文章</th><th>总消耗</th><th>OCR</th><th>翻译</th><th>完成时间</th></tr></thead><tbody><tr v-for="record in usage.records" :key="`${record.userId}-${record.completedAt}-${record.articleTitle}`"><td><strong>{{ record.username || '-' }}</strong><small class="identifier">{{ record.userId }}</small></td><td>{{ record.articleTitle || '-' }}</td><td class="number">{{ number(record.totalCredits) }}</td><td class="number">{{ number(record.ocrCredits) }}</td><td class="number">{{ number(record.translationCredits) }}</td><td>{{ formatDate(record.completedAt) }}</td></tr></tbody></table></div><footer><span>共 {{ usage?.total ?? 0 }} 条记录</span><div><button type="button" aria-label="上一页" title="上一页" :disabled="loading || page <= 1" @click="load(page - 1)"><ChevronLeft :size="16" /></button><button type="button" aria-label="下一页" title="下一页" :disabled="loading || !usage || page >= usage.totalPages" @click="load(page + 1)"><ChevronRight :size="16" /></button></div></footer></section></section>
</template>

<style scoped>
.admin-page { max-width: 1320px; margin: 0 auto; }.admin-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 30px; }.admin-header p { margin: 0 0 8px; color: #628074; font-size: 11px; font-weight: 750; letter-spacing: .07em; text-transform: uppercase; }.admin-header h1 { margin: 0; color: #1f3b33; font-size: 34px; }.admin-header span { display: block; margin-top: 7px; color: #72827b; font-size: 13px; }.icon-button { width: 38px; height: 38px; display: grid; place-items: center; border: 1px solid #c9d6d0; border-radius: 6px; color: #315d4f; background: #fff; }.icon-button:disabled { opacity: .55; cursor: wait; }.metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-bottom: 22px; }.metrics article { padding: 19px; border: 1px solid #cbd8d2; border-radius: 7px; background: #fff; }.metrics small, .metrics span { display: block; color: #72827b; font-size: 11px; }.metrics strong { display: block; margin: 9px 0 4px; color: #1f3b33; font-size: 27px; }.table-panel { overflow: hidden; border: 1px solid #cbd8d2; border-radius: 7px; background: #fff; }.table-panel > header, .table-panel footer { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 17px 20px; }.table-panel > header { border-bottom: 1px solid #dce4df; }.table-panel h2 { margin: 0; color: #1f3b33; font-size: 20px; }.table-panel header p, .table-panel header small, .table-panel footer { color: #72827b; font-size: 11px; }.table-panel header p { margin: 3px 0 0; }.table-panel footer { border-top: 1px solid #dce4df; }.table-panel footer div { display: flex; gap: 6px; }.table-panel footer button { width: 31px; height: 31px; display: grid; place-items: center; border: 1px solid #cbd8d2; border-radius: 5px; color: #315d4f; background: #fff; }.table-panel footer button:disabled { opacity: .4; cursor: not-allowed; }.table-wrap { overflow-x: auto; }table { min-width: 920px; width: 100%; border-collapse: collapse; text-align: left; }th { padding: 12px 16px; color: #71817a; background: #f4f7f5; font-size: 10px; font-weight: 750; letter-spacing: .05em; }td { padding: 14px 16px; border-top: 1px solid #e4e9e6; color: #53645c; font-size: 12px; }td strong, td small { display: block; }td strong { color: #263e36; }.identifier { margin-top: 3px; color: #72827b; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: 10px; }.number { color: #285a49; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }.table-state { padding: 54px 20px; color: #72827b; font-size: 13px; text-align: center; }.table-state.error { color: var(--error); }.spin { animation: spin 1s linear infinite; }@keyframes spin { to { transform: rotate(360deg); } }@media (max-width: 980px) { .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
