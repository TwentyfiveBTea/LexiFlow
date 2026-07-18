<script setup lang="ts">
import { ArrowRight, Eye, EyeOff } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BrandMark from '@/components/BrandMark.vue'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const email = ref('scholar@example.com')
const password = ref('lexiflow')
const showPassword = ref(false)
const loading = ref(false)

async function submit() {
  loading.value = true
  await new Promise((resolve) => setTimeout(resolve, 450))
  session.signIn(email.value.split('@')[0] || '学者', 'demo-session', email.value)
  await router.push('/dashboard')
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-intro fade-in">
      <BrandMark />
      <div class="intro-copy">
        <h1 class="serif">Turn what you read into what you remember.</h1>
        <p class="intro-english">A smart language learning platform based on news, literature, and academic research.</p>
        <p class="intro-chinese">读有所得，记有所长。<br />一个基于新闻、文献及学术研究的智能语言学习平台。</p>
      </div>
    </section>

    <section class="auth-panel">
      <div class="auth-card fade-in">
        <div class="mobile-brand"><BrandMark /></div>
        <p class="eyebrow">欢迎回来</p>
        <h2 class="serif">登录 LexiFlow</h2>
        <p class="auth-copy">继续您的外刊精读和外语学习</p>

        <form @submit.prevent="submit">
          <label class="field-label" for="email">电子邮件</label>
          <input id="email" v-model="email" class="field" autocomplete="email" type="email" required />

          <label class="field-label" for="password">密码</label>
          <div class="password-field">
            <input id="password" v-model="password" class="field" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" required />
            <button type="button" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
              <EyeOff v-if="showPassword" :size="17" /><Eye v-else :size="17" />
            </button>
          </div>

          <button class="btn btn-primary submit" type="submit" :disabled="loading">
            {{ loading ? '正在进入…' : '登录到工作区' }}<ArrowRight v-if="!loading" :size="17" />
          </button>
        </form>

        <p class="auth-switch">还没有账号？<RouterLink to="/register">创建账号</RouterLink></p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.auth-page { min-height: 100vh; display: grid; grid-template-columns: minmax(380px, 1fr) minmax(540px, 1.15fr); background: var(--surface); }
.auth-intro { position: relative; display: flex; flex-direction: column; justify-content: center; padding: 64px clamp(44px, 7vw, 112px); overflow: hidden; color: white; background: var(--primary); }
.auth-intro :deep(.brand-icon) { color: var(--primary); background: white; border-color: white; }
.auth-intro :deep(.brand-copy strong), .auth-intro :deep(.brand-copy small) { color: white; }
.intro-copy { margin-block: auto; padding-block: 56px; }
.auth-intro h1 { max-width: 650px; margin: 0; font-size: clamp(42px, 4vw, 60px); line-height: 1.18; font-weight: 600; }
.intro-english { max-width: 610px; margin: 26px 0 0; color: #dce9ec; font-family: 'Literata', Georgia, serif; font-size: 19px; line-height: 1.65; }
.intro-chinese { max-width: 590px; margin: 18px 0 0; color: #bdd0d5; font-size: 15px; line-height: 1.75; }
.auth-panel { display: grid; place-items: center; padding: 48px; }
.auth-card { width: min(100%, 430px); animation-delay: .12s; }
.auth-card > .eyebrow { margin-bottom: 12px; font-size: 14px; line-height: 18px; }
.auth-card h2 { margin: 0; color: var(--primary); font-size: 36px; font-weight: 600; }
.auth-copy { margin: 8px 0 30px; color: var(--ink-muted); }
.mobile-brand { display: none; margin-bottom: 36px; }
form { display: grid; }
.field { margin-bottom: 20px; background: white; }
.password-field { position: relative; }
.password-field .field { padding-right: 44px; }
.password-field button { position: absolute; right: 8px; top: 3px; width: 38px; height: 38px; display: grid; place-items: center; border: 0; color: var(--ink-muted); background: transparent; }
.submit { width: 100%; margin-top: 8px; }
.submit:disabled { opacity: .65; cursor: wait; }
.auth-switch { margin: 24px 0 0; text-align: center; color: var(--ink-muted); font-size: 13px; }
.auth-switch a { margin-left: 5px; color: var(--secondary); font-weight: 700; }

@media (max-width: 900px) {
  .auth-page { grid-template-columns: 1fr; }
  .auth-intro { display: none; }
  .auth-panel { padding: 40px 22px; }
  .mobile-brand { display: block; }
}
</style>
