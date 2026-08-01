<script setup lang="ts">
import { Check, Eye, EyeOff } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import BrandMark from '@/components/BrandMark.vue'
import { login, register } from '@/lib/api'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const email = ref('')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const agreed = ref(false)
const showPassword = ref(false)
const message = ref('')
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z]).{6,}$/
const isValid = computed(() => email.value && username.value && passwordPattern.test(password.value) && password.value === confirmPassword.value && agreed.value)

async function submit() {
  if (!isValid.value) {
    if (!passwordPattern.test(password.value)) message.value = '密码长度不少于 6 位，且必须包含大小写字母'
    else if (password.value !== confirmPassword.value) message.value = '两次输入的密码不一致'
    else message.value = '请完整填写信息并同意服务条款'
    return
  }
  try {
    await register({
      email: email.value,
      username: username.value,
      password: password.value,
      confirmPassword: confirmPassword.value,
    })
    const response = await login({ email: email.value, password: password.value })
    session.signIn(response)
    await router.push('/dashboard')
  } catch (error) {
    message.value = error instanceof Error ? error.message : '注册失败，请稍后重试'
  }
}
</script>

<template>
  <main class="register-page">
    <section class="register-card surface fade-in">
      <header>
        <BrandMark bilingual />
        <h1 class="serif">创建账号</h1>
        <p>加入 LexiFlow，开启精读学习之旅</p>
      </header>

      <form @submit.prevent="submit">
        <div class="form-fields">
          <div class="field-group"><label class="field-label" for="register-email">电子邮件</label><input id="register-email" v-model="email" class="field" type="email" placeholder="name@example.com" required /></div>
          <div class="field-group"><label class="field-label" for="username">用户名</label><input id="username" v-model="username" class="field" placeholder="您所展示的昵称" required /></div>
          <div class="field-group">
            <label class="field-label" for="register-password">密码</label>
            <div class="password-field"><input id="register-password" v-model="password" class="field" :type="showPassword ? 'text' : 'password'" placeholder="密码长度不少于6位，且必须包含大小写字母" required /><button type="button" aria-label="显示或隐藏密码" @click="showPassword = !showPassword"><EyeOff v-if="showPassword" :size="17" /><Eye v-else :size="17" /></button></div>
          </div>
          <div class="field-group"><label class="field-label" for="confirm-password">确认密码</label><input id="confirm-password" v-model="confirmPassword" class="field" type="password" placeholder="再次输入密码" required /></div>
        </div>

        <div class="agreement">
          <label class="agreement-control">
            <input v-model="agreed" type="checkbox" aria-label="同意服务条款与隐私政策" />
            <span class="check"><Check :size="13" /></span>
          </label>
          <span class="agreement-copy">我已阅读并同意<RouterLink to="/terms" target="_blank">服务条款</RouterLink>与<RouterLink to="/privacy" target="_blank">隐私政策</RouterLink></span>
        </div>
        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="btn btn-primary submit" type="submit">创建账号</button>
      </form>
      <p class="login-link">已有账号？<RouterLink to="/login">直接登录</RouterLink></p>
    </section>

    <footer class="auth-footer">
      <a
        href="https://beian.miit.gov.cn/"
        target="_blank"
        rel="noopener noreferrer"
      >
        粤ICP备2025405156号-2
      </a>
    </footer>
  </main>
</template>

<style scoped>
.register-page { min-height: 100vh; display: grid; grid-template-rows: minmax(0, 1fr) auto; padding: 48px 24px 24px; background: var(--surface); }
.register-card { align-self: center; justify-self: center; width: min(100%, 600px); padding: 44px; }
header { margin-bottom: 28px; text-align: center; }
header :deep(.brand) { justify-content: center; margin-bottom: 18px; }
header h1 { margin: 0; color: var(--primary); font-size: 36px; }
header > p:last-child { margin: 6px 0 0; color: var(--ink-muted); }
.form-fields { display: grid; gap: 16px; margin-bottom: 18px; }
.field-group { min-width: 0; }
.register-card .field::placeholder { font-size: 12px; }
.password-field { position: relative; }
.password-field .field { padding-right: 44px; }
.password-field button { position: absolute; right: 6px; top: 3px; width: 38px; height: 38px; display: grid; place-items: center; border: 0; color: var(--ink-muted); background: transparent; }
.agreement { display: flex; align-items: center; gap: 10px; color: var(--ink-muted); font-size: 13px; line-height: 20px; }
.agreement-control { position: relative; display: grid; flex: 0 0 auto; cursor: pointer; }
.agreement-control input { position: absolute; opacity: 0; }
.check { width: 18px; height: 18px; display: grid; place-items: center; border: 1px solid var(--outline); border-radius: 4px; color: transparent; background: white; }
.agreement-control input:focus-visible + .check { outline: 2px solid var(--primary); outline-offset: 2px; }
.agreement-control input:checked + .check { color: white; border-color: var(--primary); background: var(--primary); }
.agreement-copy a { margin-inline: 3px; color: var(--secondary); font-weight: 700; text-decoration: underline; text-decoration-color: rgba(124, 87, 48, .35); text-underline-offset: 3px; }
.agreement-copy a:hover, .agreement-copy a:focus-visible { color: var(--primary); text-decoration-color: currentColor; }
.form-message { margin: 14px 0 0; color: var(--error); font-size: 13px; }
.submit { width: 100%; margin-top: 22px; }
.login-link { margin: 22px 0 0; text-align: center; color: var(--ink-muted); font-size: 13px; }
.login-link a { margin-left: 5px; color: var(--secondary); font-weight: 700; }
.auth-footer { margin-top: 28px; color: var(--ink-muted); font-size: 11px; line-height: 18px; text-align: center; }
.auth-footer a { text-underline-offset: 3px; }
.auth-footer a:hover, .auth-footer a:focus-visible { color: var(--primary); text-decoration: underline; }
@media (max-width: 640px) {
  .register-page { padding: 28px 18px 18px; }
  .register-card { padding: 28px 22px; }
}
</style>
