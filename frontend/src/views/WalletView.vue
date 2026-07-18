<script setup lang="ts">
import { CheckCircle2, CreditCard, History, LockKeyhole, WalletCards } from 'lucide-vue-next'
import { computed, ref } from 'vue'

const amounts = [10, 20, 35, 50, 75, 100]
const selectedAmount = ref(35)
const customAmount = ref<number | null>(null)
const paymentNotice = ref('')
const finalAmount = computed(() => customAmount.value && customAmount.value > 0 ? customAmount.value : selectedAmount.value)

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
    <header class="page-header fade-in"><div><p class="eyebrow">Credits & billing</p><h1 class="page-title">我的钱包</h1><p class="page-description">管理账户额度与充值记录。</p></div><button class="btn btn-secondary"><History :size="17" />交易记录</button></header>

    <section class="balance-panel surface fade-in">
      <div><span class="balance-icon"><WalletCards :size="22" /></span><p>可用额度</p><strong class="serif">1,250 <small>Credits</small></strong></div>
      <div><span class="balance-icon muted"><LockKeyhole :size="21" /></span><p>冻结额度</p><strong class="serif">0 <small>Credits</small></strong></div>
      <div><span class="status"><i></i>账户正常</span><p>最后更新</p><strong class="update-time">2026-07-18 14:30</strong></div>
    </section>

    <section class="recharge-section fade-in">
      <div class="section-heading"><p class="eyebrow">Add credits</p><h2 class="serif">添加资金</h2><p>选择金额和付款方式。</p></div>
      <div class="amount-grid">
        <button v-for="amount in amounts" :key="amount" class="amount-card surface" :class="{ selected: selectedAmount === amount && !customAmount }" @click="selectedAmount = amount; customAmount = null">
          <strong class="serif">{{ amount }}</strong><span>CNY</span><CheckCircle2 v-if="selectedAmount === amount && !customAmount" :size="17" />
        </button>
      </div>

      <div class="custom-row surface"><div><label class="field-label" for="custom-amount">自定义金额（1–100 元）</label><div class="amount-input"><span>¥</span><input id="custom-amount" v-model.number="customAmount" min="1" max="100" type="number" placeholder="输入金额" /></div></div><button class="btn btn-primary" @click="recharge">确认充值 ¥{{ finalAmount }}</button></div>

      <div class="payment-method"><h3 class="serif">付款方式</h3><button class="method surface"><span class="method-icon"><CreditCard :size="19" /></span><strong>支付宝 Alipay</strong><CheckCircle2 :size="19" /></button></div>
      <p v-if="paymentNotice" class="payment-notice">{{ paymentNotice }}</p>
    </section>
  </main>
</template>

<style scoped>
.balance-panel { display: grid; grid-template-columns: 1.2fr 1fr 1fr; margin-bottom: 48px; overflow: hidden; }
.balance-panel > div { min-height: 150px; display: grid; align-content: center; padding: 28px; border-right: 1px solid var(--outline); }.balance-panel > div:last-child { border-right: 0; }
.balance-icon { width: 38px; height: 38px; display: grid; place-items: center; margin-bottom: 15px; border-radius: 7px; color: var(--primary); background: var(--primary-soft); }.balance-icon.muted { color: var(--ink-muted); background: var(--surface-high); }
.balance-panel p { margin: 0 0 6px; color: var(--ink-muted); font-size: 11px; font-weight: 700; text-transform: uppercase; }
.balance-panel strong { color: var(--primary); font-size: 31px; }.balance-panel strong small { font-family: 'Inter', sans-serif; color: var(--ink-muted); font-size: 12px; }
.status { display: flex; align-items: center; gap: 8px; margin-bottom: 19px; color: var(--success); font-size: 13px; font-weight: 700; }.status i { width: 8px; height: 8px; border-radius: 50%; background: var(--success); }
.balance-panel .update-time { color: var(--ink); font-family: 'Inter', sans-serif; font-size: 14px; }
.section-heading h2 { margin: 0; color: var(--primary); font-size: 28px; }.section-heading > p:last-child { margin: 6px 0 0; color: var(--ink-muted); }
.amount-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 22px; }
.amount-card { position: relative; min-height: 102px; display: flex; align-items: baseline; justify-content: center; gap: 5px; color: var(--primary); }.amount-card:hover, .amount-card.selected { border-color: var(--primary); background: var(--primary-soft); }.amount-card strong { font-size: 33px; }.amount-card span { color: var(--ink-muted); font-size: 11px; }.amount-card svg { position: absolute; right: 10px; top: 10px; }
.custom-row { display: flex; align-items: end; gap: 18px; padding: 22px; margin-top: 14px; }.custom-row > div { flex: 1; }.amount-input { height: 44px; display: flex; align-items: center; gap: 8px; padding: 0 13px; border: 1px solid var(--outline); border-radius: 7px; background: var(--surface-low); }.amount-input:focus-within { border-color: var(--primary); }.amount-input span { color: var(--ink-muted); }.amount-input input { width: 100%; border: 0; outline: 0; background: transparent; }
.payment-method { margin-top: 30px; }.payment-method h3 { margin: 0 0 12px; color: var(--primary); font-size: 19px; }.method { width: 100%; min-height: 62px; display: flex; align-items: center; gap: 12px; padding: 0 16px; color: var(--primary); }.method strong { flex: 1; text-align: left; color: var(--ink); font-size: 14px; }.method-icon { width: 36px; height: 36px; display: grid; place-items: center; border-radius: 7px; background: var(--primary-soft); }
.payment-notice { padding: 12px 14px; margin: 14px 0 0; border-radius: 7px; color: var(--success); background: #e5eee8; font-size: 13px; }
@media (max-width: 760px) { .balance-panel { grid-template-columns: 1fr; }.balance-panel > div { min-height: 120px; border-right: 0; border-bottom: 1px solid var(--outline); }.amount-grid { grid-template-columns: repeat(2, 1fr); }.custom-row { align-items: stretch; flex-direction: column; } }
</style>
