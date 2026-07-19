<script setup lang="ts">
import { CheckCircle2, ChevronLeft, ChevronRight, Coins, CreditCard, History, LockKeyhole, WalletCards, X } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { creditLedgerRecordsDemo, rechargeRecordsDemo } from '@/data/demo'
import { getCreditLedger, getRechargeRecords } from '@/lib/api'
import type { CreditLedgerResponse, PageResponse, RechargeRecordResponse } from '@/lib/api'

type RecordDialog = 'recharge' | 'credits'

const amounts = [10, 20, 35, 50, 75, 100]
const CREDITS_PER_CNY = 10_000
const availableCredits = 1_250
const selectedAmount = ref(35)
const customAmount = ref<number | null>(null)
const paymentNotice = ref('')
const activeRecordDialog = ref<RecordDialog | null>(null)
const recordsLoading = ref(false)
const recordsError = ref('')
const usingDemoData = ref(false)
const recordPageSize = 6
const rechargeRecords = ref<PageResponse<RechargeRecordResponse>>({ records: [], total: 0, page: 1, pageSize: recordPageSize, totalPages: 0 })
const creditRecords = ref<PageResponse<CreditLedgerResponse>>({ records: [], total: 0, page: 1, pageSize: recordPageSize, totalPages: 0 })
const finalAmount = computed(() => customAmount.value && customAmount.value > 0 ? customAmount.value : selectedAmount.value)
const approximateBalanceCny = computed(() => (availableCredits / CREDITS_PER_CNY).toFixed(2))
const activePage = computed(() => activeRecordDialog.value === 'recharge' ? rechargeRecords.value : creditRecords.value)

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

function creditsFor(amount: number) {
  return (amount * CREDITS_PER_CNY).toLocaleString('en-US')
}

function formatDate(value: string) {
  return dateFormatter.format(new Date(value))
}

function formatCredits(value: number) {
  return value.toLocaleString('en-US')
}

function formatCny(value: number) {
  return `¥${Number(value).toFixed(2)}`
}

function paginateDemo<T>(records: T[], page: number): PageResponse<T> {
  const start = (page - 1) * recordPageSize
  return {
    records: records.slice(start, start + recordPageSize),
    total: records.length,
    page,
    pageSize: recordPageSize,
    totalPages: Math.ceil(records.length / recordPageSize),
  }
}

async function loadRecordPage(page: number) {
  if (!activeRecordDialog.value) return
  recordsLoading.value = true
  recordsError.value = ''
  usingDemoData.value = false
  try {
    if (activeRecordDialog.value === 'recharge') {
      rechargeRecords.value = await getRechargeRecords(page, recordPageSize)
    } else {
      creditRecords.value = await getCreditLedger(page, recordPageSize)
    }
  } catch {
    if (import.meta.env.DEV) {
      usingDemoData.value = true
      if (activeRecordDialog.value === 'recharge') {
        rechargeRecords.value = paginateDemo<RechargeRecordResponse>(rechargeRecordsDemo, page)
      } else {
        creditRecords.value = paginateDemo<CreditLedgerResponse>(creditLedgerRecordsDemo, page)
      }
    } else {
      recordsError.value = '记录加载失败，请确认服务状态后重试。'
    }
  } finally {
    recordsLoading.value = false
  }
}

function openRecordDialog(dialog: RecordDialog) {
  activeRecordDialog.value = dialog
  void loadRecordPage(1)
}

function closeRecordDialog() {
  activeRecordDialog.value = null
  recordsError.value = ''
}

function changeRecordPage(page: number) {
  if (recordsLoading.value || page < 1 || page > activePage.value.totalPages) return
  void loadRecordPage(page)
}

function recharge() {
  if (finalAmount.value < 1 || finalAmount.value > 100) {
    paymentNotice.value = '充值金额需要在 1 至 100 元之间。'
    return
  }
  paymentNotice.value = `已创建 ¥${finalAmount.value} 充值订单，等待支付。`
}
</script>

<template>
  <main class="page">
    <header class="page-header fade-in"><div><p class="eyebrow">Credits & billing</p><h1 class="page-title">我的钱包</h1><p class="page-description">管理账户额度与充值记录。</p></div><div class="header-actions"><button class="btn btn-secondary" type="button" @click="openRecordDialog('recharge')"><History :size="17" />交易记录</button><button class="btn btn-secondary" type="button" @click="openRecordDialog('credits')"><Coins :size="17" />Credits 使用记录</button></div></header>

    <section class="balance-panel surface fade-in">
      <div><span class="balance-icon"><WalletCards :size="22" /></span><p>可用额度</p><strong class="serif balance-value">{{ availableCredits.toLocaleString('en-US') }} <small>Credits</small><span class="balance-equivalent">≈ ¥{{ approximateBalanceCny }} CNY</span></strong></div>
      <div><span class="balance-icon muted"><LockKeyhole :size="21" /></span><p>冻结额度</p><strong class="serif">0 <small>Credits</small></strong></div>
      <div class="account-summary"><span class="status"><i></i>账户正常</span><div class="update-block"><p>最后更新</p><strong class="update-time">2026-07-18 14:30</strong></div></div>
    </section>

    <section class="recharge-section fade-in">
      <div class="section-heading"><p class="eyebrow">Add credits</p><h2 class="serif">添加资金</h2><p>选择金额和付款方式。</p></div>
      <div class="amount-grid">
        <button v-for="amount in amounts" :key="amount" class="amount-card surface" :class="{ selected: selectedAmount === amount && !customAmount }" @click="selectedAmount = amount; customAmount = null">
          <span class="amount-main"><strong class="serif">{{ amount }}</strong><small>CNY</small></span>
          <span class="credit-amount">{{ creditsFor(amount) }} Credits</span>
          <CheckCircle2 v-if="selectedAmount === amount && !customAmount" :size="17" />
        </button>
      </div>

      <div class="custom-row surface"><div><label class="field-label" for="custom-amount">自定义金额（1–100 元）</label><div class="amount-input"><span>¥</span><input id="custom-amount" v-model.number="customAmount" min="1" max="100" type="number" placeholder="输入金额" /></div></div><button class="btn btn-primary" @click="recharge">确认充值</button></div>

      <div class="payment-method"><h3 class="serif">付款方式</h3><button class="method surface"><span class="method-icon"><CreditCard :size="19" /></span><strong>支付宝 Alipay</strong><CheckCircle2 :size="19" /></button></div>
      <p v-if="paymentNotice" class="payment-notice">{{ paymentNotice }}</p>
    </section>

    <Transition name="dialog">
      <div v-if="activeRecordDialog" class="modal-backdrop" @click.self="closeRecordDialog">
        <section class="dialog-panel records-modal surface" role="dialog" aria-modal="true" aria-labelledby="records-dialog-title">
          <header class="modal-heading">
            <div>
              <p class="eyebrow">{{ activeRecordDialog === 'recharge' ? 'Recharge history' : 'Credits ledger' }}</p>
              <div class="records-title-row"><h2 id="records-dialog-title" class="serif">{{ activeRecordDialog === 'recharge' ? '交易记录' : 'Credits 使用记录' }}</h2><span v-if="usingDemoData" class="demo-badge">示例数据</span></div>
              <p>{{ activeRecordDialog === 'recharge' ? '查看已完成支付并到账的充值订单。' : '查看文章处理产生的 Credits 消耗。' }}</p>
            </div>
            <button class="icon-btn" type="button" aria-label="关闭记录弹窗" title="关闭" @click="closeRecordDialog"><X :size="18" /></button>
          </header>

          <div class="records-content" :aria-busy="recordsLoading">
            <div v-if="recordsLoading" class="records-state"><span class="loading-line"></span><p>正在加载记录...</p></div>
            <div v-else-if="recordsError" class="records-state error-state"><p>{{ recordsError }}</p><button class="btn btn-secondary" type="button" @click="loadRecordPage(activePage.page)">重新加载</button></div>
            <div v-else-if="activePage.records.length === 0" class="records-state"><span class="empty-icon"><History v-if="activeRecordDialog === 'recharge'" :size="21" /><Coins v-else :size="21" /></span><strong>暂无记录</strong><p>{{ activeRecordDialog === 'recharge' ? '完成充值后，交易会显示在这里。' : '完成文章处理后，Credits 消耗会显示在这里。' }}</p></div>

            <div v-else class="records-table-wrap">
              <table v-if="activeRecordDialog === 'recharge'" class="records-table">
                <thead><tr><th>到账时间</th><th>订单号</th><th>充值金额</th><th class="numeric">到账 Credits</th></tr></thead>
                <tbody><tr v-for="record in rechargeRecords.records" :key="record.orderNo"><td>{{ formatDate(record.creditedAt) }}</td><td class="identifier">{{ record.orderNo }}</td><td>{{ formatCny(record.amountYuan) }}</td><td class="numeric credits-cell">+{{ formatCredits(record.credits) }}</td></tr></tbody>
              </table>

              <table v-else class="records-table credits-table">
                <thead><tr><th>完成时间</th><th>文章 ID</th><th class="numeric">总消耗</th><th class="numeric">OCR</th><th class="numeric">翻译</th></tr></thead>
                <tbody><tr v-for="record in creditRecords.records" :key="`${record.articleId}-${record.completedAt}`"><td>{{ formatDate(record.completedAt) }}</td><td class="identifier">{{ record.articleId }}</td><td class="numeric credits-cell">{{ formatCredits(record.totalCredits) }}</td><td class="numeric">{{ formatCredits(record.ocrCredits) }}</td><td class="numeric">{{ formatCredits(record.translationCredits) }}</td></tr></tbody>
              </table>
            </div>
          </div>

          <footer v-if="!recordsLoading && !recordsError && activePage.total > 0" class="pagination-footer">
            <span>共 {{ activePage.total.toLocaleString('zh-CN') }} 条</span>
            <div class="pagination-controls">
              <button class="icon-btn" type="button" aria-label="上一页" title="上一页" :disabled="activePage.page <= 1" @click="changeRecordPage(activePage.page - 1)"><ChevronLeft :size="18" /></button>
              <strong>{{ activePage.page }} / {{ activePage.totalPages }}</strong>
              <button class="icon-btn" type="button" aria-label="下一页" title="下一页" :disabled="activePage.page >= activePage.totalPages" @click="changeRecordPage(activePage.page + 1)"><ChevronRight :size="18" /></button>
            </div>
          </footer>
        </section>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.header-actions { display: flex; align-items: center; gap: 10px; }
.balance-panel { display: grid; grid-template-columns: 1.2fr 1fr 1fr; margin-bottom: 48px; overflow: hidden; }
.balance-panel > div { min-height: 150px; display: flex; flex-direction: column; justify-content: flex-start; padding: 28px; border-right: 1px solid var(--outline); }.balance-panel > div:last-child { border-right: 0; }
.balance-icon { width: 38px; height: 38px; display: grid; place-items: center; margin-bottom: 15px; border-radius: 7px; color: var(--primary); background: var(--primary-soft); }.balance-icon.muted { color: var(--ink-muted); background: var(--surface-high); }
.balance-panel p { margin: 0 0 6px; color: var(--ink-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
.balance-panel strong { color: var(--primary); font-size: 31px; }.balance-panel strong small { font-family: 'Inter', sans-serif; color: var(--ink-muted); font-size: 12px; }
.balance-value { display: flex; flex-wrap: nowrap; align-items: baseline; gap: 0 5px; white-space: nowrap; }.balance-equivalent { margin-left: 5px; color: var(--ink-muted); font-family: 'Inter', sans-serif; font-size: 13px; font-weight: 600; }
.status { width: fit-content; height: 38px; display: flex; flex: 0 0 38px; align-items: center; gap: 8px; margin-bottom: 15px; color: var(--success); font-size: 13px; font-weight: 700; }.status i { width: 8px; height: 8px; border-radius: 50%; background: var(--success); }
.balance-panel .update-time { min-height: 46px; display: flex; align-items: center; color: var(--ink); font-family: 'Inter', sans-serif; font-size: 14px; }
.section-heading h2 { margin: 0; color: var(--primary); font-size: 28px; }.section-heading > p:last-child { margin: 6px 0 0; color: var(--ink-muted); }
.amount-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 22px; }
.amount-card { position: relative; min-height: 102px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5px; color: var(--primary); }.amount-card:hover, .amount-card.selected { border-color: var(--primary); background: var(--primary-soft); }.amount-main { display: flex; align-items: baseline; gap: 5px; }.amount-main strong { font-size: 33px; }.amount-main small { color: var(--ink-muted); font-size: 11px; }.amount-card .credit-amount { color: var(--ink-muted); font-size: 12px; font-weight: 600; }.amount-card svg { position: absolute; right: 10px; top: 10px; }
.custom-row { display: flex; align-items: end; gap: 18px; padding: 22px; margin-top: 14px; }.custom-row > div { flex: 1; }.amount-input { height: 44px; display: flex; align-items: center; gap: 8px; padding: 0 13px; border: 1px solid var(--outline); border-radius: 7px; background: var(--surface-low); }.amount-input:focus-within { border-color: var(--primary); }.amount-input span { color: var(--ink-muted); }.amount-input input { width: 100%; border: 0; outline: 0; background: transparent; font-size: 12px; }
.payment-method { margin-top: 30px; }.payment-method h3 { margin: 0 0 12px; color: var(--primary); font-size: 19px; }.method { width: 100%; min-height: 62px; display: flex; align-items: center; gap: 12px; padding: 0 16px; color: var(--primary); }.method strong { flex: 1; text-align: left; color: var(--ink); font-size: 14px; }.method-icon { width: 36px; height: 36px; display: grid; place-items: center; border-radius: 7px; background: var(--primary-soft); }
.payment-notice { padding: 12px 14px; margin: 14px 0 0; border-radius: 7px; color: var(--success); background: #e5eee8; font-size: 13px; }
.modal-backdrop { position: fixed; z-index: 60; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }
.records-modal { width: min(100%, 860px); max-height: min(720px, calc(100vh - 44px)); display: flex; flex-direction: column; overflow: hidden; padding: 28px 30px 22px; }
.modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; padding-bottom: 20px; border-bottom: 1px solid var(--outline); }.modal-heading h2 { margin: 0; color: var(--primary); font-size: 26px; }.modal-heading > div > p:last-child { margin: 6px 0 0; color: var(--ink-muted); font-size: 13px; }
.records-title-row { display: flex; align-items: center; gap: 10px; }.demo-badge { padding: 3px 7px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; }
.records-content { min-height: 310px; overflow: auto; }.records-table-wrap { overflow-x: auto; }.records-table { width: 100%; border-collapse: collapse; table-layout: fixed; }.records-table th { padding: 13px 12px; border-bottom: 1px solid var(--outline); color: var(--ink-muted); font-size: 10px; font-weight: 750; text-align: left; text-transform: uppercase; }.records-table td { height: 48px; padding: 8px 12px; border-bottom: 1px solid rgba(199,196,192,.55); color: var(--ink); font-size: 12px; vertical-align: middle; }.records-table tbody tr:last-child td { border-bottom: 0; }.records-table .numeric { text-align: right; }.records-table .identifier { overflow: hidden; font-family: 'Inter', sans-serif; text-overflow: ellipsis; white-space: nowrap; }.records-table .credits-cell { color: var(--primary); font-weight: 750; }.records-table th:nth-child(1) { width: 22%; }.records-table th:nth-child(2) { width: 38%; }.records-table th:nth-child(3) { width: 18%; }.records-table th:nth-child(4) { width: 22%; }.credits-table th:nth-child(1) { width: 22%; }.credits-table th:nth-child(2) { width: 30%; }.credits-table th:nth-child(3), .credits-table th:nth-child(4), .credits-table th:nth-child(5) { width: 16%; }
.records-state { min-height: 310px; display: flex; align-items: center; justify-content: center; flex-direction: column; color: var(--ink-muted); text-align: center; }.records-state p { margin: 8px 0 0; font-size: 12px; }.records-state strong { margin-top: 12px; color: var(--ink); font-size: 15px; }.empty-icon { width: 42px; height: 42px; display: grid; place-items: center; border-radius: 7px; color: var(--primary); background: var(--primary-soft); }.error-state .btn { margin-top: 14px; }.loading-line { width: 92px; height: 2px; overflow: hidden; position: relative; background: var(--surface-high); }.loading-line::after { position: absolute; inset: 0; content: ''; background: var(--primary); transform-origin: left; animation: record-loading 1s ease-in-out infinite; }
.pagination-footer { min-height: 52px; display: flex; align-items: end; justify-content: space-between; gap: 20px; padding-top: 14px; border-top: 1px solid var(--outline); color: var(--ink-muted); font-size: 11px; }.pagination-controls { display: flex; align-items: center; gap: 10px; }.pagination-controls strong { min-width: 48px; color: var(--ink); font-size: 12px; text-align: center; }.pagination-controls .icon-btn:disabled { opacity: .35; cursor: not-allowed; }
@keyframes record-loading { 0% { transform: translateX(-100%) scaleX(.35); } 55% { transform: translateX(35%) scaleX(.65); } 100% { transform: translateX(110%) scaleX(.3); } }
@media (max-width: 760px) { .header-actions { width: 100%; flex-wrap: wrap; }.balance-panel { grid-template-columns: 1fr; }.balance-panel > div { min-height: 120px; border-right: 0; border-bottom: 1px solid var(--outline); }.amount-grid { grid-template-columns: repeat(2, 1fr); }.custom-row { align-items: stretch; flex-direction: column; }.records-modal { padding: 23px 20px 18px; }.records-table { min-width: 700px; } }
</style>
