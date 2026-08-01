/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员登录页面
 */
<script setup lang="ts">
import { ArrowRight, Eye, EyeOff, ShieldCheck } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '@/lib/api'

const router = useRouter()
const account = ref('')
const password = ref('')
const showPassword = ref(false)
const submitting = ref(false)
const message = ref('')

async function submit() {
  if (submitting.value) return
  submitting.value = true
  message.value = ''
  try {
    const response = await adminLogin({ account: account.value.trim(), password: password.value })
    localStorage.setItem('lexiflow.admin.token', response.token)
    localStorage.setItem('lexiflow.admin.username', response.username)
    await router.replace('/admin/overview')
  } catch (error) {
    message.value = error instanceof Error ? error.message : '管理员登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="admin-login-page">
    <section class="admin-login-panel fade-in">
      <div class="admin-login-mark"><ShieldCheck :size="28" /></div>
      <p class="eyebrow">LexiFlow · Administration</p>
      <h1 class="serif">管理员登录</h1>
      <p class="intro">仅限授权账号访问运营与账务数据</p>
      <form @submit.prevent="submit">
        <label class="field-label" for="admin-account">账号</label>
        <input id="admin-account" v-model="account" class="field" autocomplete="username" required />
        <label class="field-label" for="admin-password">密码</label>
        <div class="password-field">
          <input id="admin-password" v-model="password" class="field" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" required />
          <button type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword"><EyeOff v-if="showPassword" :size="17" /><Eye v-else :size="17" /></button>
        </div>
        <p v-if="message" class="form-message" role="alert">{{ message }}</p>
        <button class="btn btn-primary submit" type="submit" :disabled="submitting">{{ submitting ? '验证中...' : '进入后台' }}<ArrowRight v-if="!submitting" :size="17" /></button>
      </form>
    </section>
  </main>
</template>

<style scoped>
.admin-login-page { min-height: 100vh; display: grid; place-items: center; padding: 28px; color: var(--ink); background: #edf1ef; }.admin-login-panel { width: min(100%, 420px); padding: 42px; border: 1px solid #ccd6d1; border-radius: 8px; background: #fff; box-shadow: 0 22px 50px rgba(35, 55, 48, .11); }.admin-login-mark { width: 48px; height: 48px; display: grid; place-items: center; margin-bottom: 28px; border-radius: 8px; color: #fff; background: #365d51; }.eyebrow { margin-bottom: 10px; color: #577166; }.admin-login-panel h1 { margin: 0; color: #23463d; font-size: 32px; }.intro { margin: 8px 0 30px; color: var(--ink-muted); font-size: 13px; }.admin-login-panel form { display: grid; }.field { margin-bottom: 20px; background: #fff; }.password-field { position: relative; }.password-field .field { padding-right: 44px; }.password-field button { position: absolute; top: 3px; right: 7px; width: 38px; height: 38px; display: grid; place-items: center; border: 0; color: var(--ink-muted); background: transparent; }.submit { width: 100%; margin-top: 8px; background: #365d51; }.submit:hover { background: #284a40; }.submit:disabled { opacity: .65; cursor: wait; }.form-message { margin: -6px 0 14px; color: var(--error); font-size: 12px; }
</style>
