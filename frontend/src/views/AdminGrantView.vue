/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员赠送Credits页面
 */
<script setup lang="ts">
import { Check, Gift, Send } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { grantAdminCredits } from '@/lib/api'

const userId = ref('')
const credits = ref<number | null>(null)
const submitting = ref(false)
const message = ref('')
const error = ref('')
const valid = computed(() => Boolean(userId.value.trim()) && Number.isInteger(credits.value) && (credits.value ?? 0) >= 1 && (credits.value ?? 0) <= 10_000_000)

async function submit() {
  if (!valid.value || submitting.value || credits.value === null) return
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    await grantAdminCredits({ userId: userId.value.trim(), credits: credits.value })
    message.value = `已向用户 ${userId.value.trim()} 赠送 ${credits.value.toLocaleString()} Credits`
    userId.value = ''
    credits.value = null
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '赠送Credits失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="admin-page"><header class="admin-header"><div><p>Administration · Grant</p><h1 class="serif">赠送 Credits</h1><span>为指定用户补充可用 Credits，并记录管理员调整流水</span></div></header><section class="grant-layout"><article class="grant-form"><div class="grant-mark"><Gift :size="22" /></div><h2 class="serif">创建赠送</h2><p>请输入目标用户的唯一 ID 和赠送数量。</p><form @submit.prevent="submit"><label class="field-label" for="grant-user-id">用户 ID</label><input id="grant-user-id" v-model="userId" class="field" autocomplete="off" inputmode="numeric" placeholder="输入用户 ID" required /><label class="field-label" for="grant-credits">赠送 Credits</label><input id="grant-credits" v-model.number="credits" class="field" type="number" min="1" max="10000000" step="1" placeholder="1 - 10,000,000" required /><small class="hint">赠送后会立即增加该用户的可用 Credits。</small><p v-if="error" class="notice error">{{ error }}</p><p v-else-if="message" class="notice success"><Check :size="15" />{{ message }}</p><button class="btn btn-primary submit" type="submit" :disabled="!valid || submitting"><Send :size="16" />{{ submitting ? '提交中...' : '确认赠送' }}</button></form></article><aside><h2 class="serif">操作说明</h2><ol><li>通过用户总览复制用户 ID。</li><li>确认赠送数量后提交。</li><li>赠送记录会写入该用户的 Credits 流水。</li></ol></aside></section></section>
</template>

<style scoped>
.admin-page { max-width: 1060px; margin: 0 auto; }.admin-header { margin-bottom: 30px; }.admin-header p { margin: 0 0 8px; color: #628074; font-size: 11px; font-weight: 750; letter-spacing: .07em; text-transform: uppercase; }.admin-header h1 { margin: 0; color: #1f3b33; font-size: 34px; }.admin-header span { display: block; margin-top: 7px; color: #72827b; font-size: 13px; }.grant-layout { display: grid; grid-template-columns: minmax(0, 1fr) 310px; gap: 20px; align-items: start; }.grant-form, aside { border: 1px solid #cbd8d2; border-radius: 7px; background: #fff; }.grant-form { padding: 30px; }.grant-mark { width: 42px; height: 42px; display: grid; place-items: center; margin-bottom: 20px; border-radius: 6px; color: #315d4f; background: #e3eee8; }.grant-form h2, aside h2 { margin: 0; color: #1f3b33; font-size: 22px; }.grant-form > p { margin: 7px 0 26px; color: #72827b; font-size: 13px; }.grant-form form { display: grid; }.field { margin-bottom: 19px; background: #fff; }.hint { margin: -10px 0 17px; color: #72827b; font-size: 11px; }.submit { width: fit-content; min-width: 132px; justify-content: center; margin-top: 10px; background: #365d51; }.submit:hover { background: #284a40; }.submit:disabled { opacity: .5; cursor: not-allowed; }.notice { display: flex; align-items: center; gap: 6px; margin: 0 0 14px; font-size: 12px; }.notice.error { color: var(--error); }.notice.success { color: #2f7657; }aside { padding: 25px; background: #eef4f0; }aside ol { display: grid; gap: 15px; padding-left: 20px; margin: 22px 0 0; color: #5c7167; font-size: 12px; line-height: 1.65; }@media (max-width: 820px) { .grant-layout { grid-template-columns: 1fr; }.grant-form { padding: 24px; } }
</style>
