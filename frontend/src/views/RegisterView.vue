<script setup lang="ts">
import { Check, Eye, EyeOff } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import BrandMark from '@/components/BrandMark.vue'
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
  session.signIn(username.value, 'demo-session', email.value)
  await router.push('/dashboard')
}
</script>

<template>
  <main class="register-page">
    <section class="register-card surface fade-in">
      <header>
        <BrandMark />
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

        <label class="agreement"><input v-model="agreed" type="checkbox" /><span class="check"><Check :size="13" /></span><span>我已阅读并同意服务条款与隐私政策</span></label>
        <p v-if="message" class="form-message">{{ message }}</p>
        <button class="btn btn-primary submit" type="submit">创建账号</button>
      </form>
      <p class="login-link">已有账号？<RouterLink to="/login">直接登录</RouterLink></p>
    </section>
  </main>
</template>

<style scoped>
.register-page { min-height: 100vh; display: grid; place-items: center; padding: 48px 24px; background: var(--surface); }
.register-card { width: min(100%, 600px); padding: 44px; }
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
.agreement { position: relative; display: flex; align-items: center; gap: 10px; color: var(--ink-muted); font-size: 13px; cursor: pointer; }
.agreement input { position: absolute; opacity: 0; }
.check { width: 18px; height: 18px; display: grid; place-items: center; border: 1px solid var(--outline); border-radius: 4px; color: transparent; background: white; }
.agreement input:checked + .check { color: white; border-color: var(--primary); background: var(--primary); }
.form-message { margin: 14px 0 0; color: var(--error); font-size: 13px; }
.submit { width: 100%; margin-top: 22px; }
.login-link { margin: 22px 0 0; text-align: center; color: var(--ink-muted); font-size: 13px; }
.login-link a { margin-left: 5px; color: var(--secondary); font-weight: 700; }
@media (max-width: 640px) { .register-card { padding: 28px 22px; } }
</style>
