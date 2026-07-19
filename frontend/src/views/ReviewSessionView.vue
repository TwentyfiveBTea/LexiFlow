<script setup lang="ts">
import { ArrowLeft, Check, CircleAlert, Eye, EyeOff } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { words } from '@/data/demo'
import { getDueWords, reviewWord } from '@/lib/api'
import type { DueWordResponse } from '@/lib/api'

const router = useRouter()
const sessionWords = ref<DueWordResponse[]>([])
const currentIndex = ref(0)
const loading = ref(true)
const submitting = ref(false)
const error = ref('')
const showMeaning = ref(false)
const usingDemoData = ref(false)

const demoWords: DueWordResponse[] = words.map((word) => ({
  libraryWordId: word.libraryWordId,
  wordId: word.wordId,
  languageCode: word.languageCode,
  word: word.word,
  kana: word.kana || null,
  us: word.us || null,
  uk: word.uk || null,
  translations: word.translations,
  phrases: word.phrases,
}))

const currentWord = computed(() => sessionWords.value[currentIndex.value])
const progressLabel = computed(() => sessionWords.value.length ? `${currentIndex.value + 1} / ${sessionWords.value.length}` : '0 / 0')
const progressPercent = computed(() => sessionWords.value.length ? (currentIndex.value / sessionWords.value.length) * 100 : 0)

function formatJsonList(value: string | null, field: 'translation' | 'phrase') {
  if (!value) return ''
  try {
    const items = JSON.parse(value) as Array<Record<string, string>>
    return items.map((item) => field === 'phrase' && item.translation ? `${item.phrase} · ${item.translation}` : item[field] ?? '').filter(Boolean).join('；')
  } catch {
    return value
  }
}

async function loadSession() {
  loading.value = true
  error.value = ''
  usingDemoData.value = false
  try {
    sessionWords.value = await getDueWords()
  } catch {
    if (import.meta.env.DEV) {
      usingDemoData.value = true
      sessionWords.value = demoWords
    } else {
      error.value = '复习单词加载失败，请稍后重试。'
    }
  } finally {
    loading.value = false
  }
}

async function submitRating(rating: 'UNKNOWN' | 'VAGUE' | 'KNOWN') {
  if (!currentWord.value || submitting.value) return
  submitting.value = true
  error.value = ''
  try {
    await reviewWord(currentWord.value.wordId, currentWord.value.languageCode, rating)
  } catch {
    if (!usingDemoData.value && !import.meta.env.DEV) {
      error.value = '提交复习结果失败，请重试。'
      submitting.value = false
      return
    }
  }

  const reviewedWord = currentWord.value
  if (rating !== 'KNOWN') {
    sessionWords.value.push(reviewedWord)
  }
  if (currentIndex.value >= sessionWords.value.length - 1) {
    await router.replace('/review')
  } else {
    currentIndex.value += 1
    showMeaning.value = false
  }
  submitting.value = false
}

onMounted(() => { void loadSession() })
</script>

<template>
  <main class="page session-page">
    <RouterLink class="back-link" to="/review"><ArrowLeft :size="16" />返回单词复习</RouterLink>
    <section v-if="loading" class="session-state surface fade-in"><span class="loading-line"></span><p>正在准备复习...</p></section>
    <section v-else-if="error" class="session-state surface fade-in"><CircleAlert :size="22" /><p>{{ error }}</p><button class="btn btn-secondary" type="button" @click="loadSession">重新加载</button></section>
    <section v-else-if="!currentWord" class="session-state surface fade-in"><Check :size="24" /><strong>今天的复习已经完成</strong><p>回到列表查看新的待复习词条。</p><RouterLink class="btn btn-primary" to="/review">返回列表</RouterLink></section>
    <section v-else class="session-shell fade-in">
      <header class="session-heading"><div><p class="eyebrow">Focused recall<span v-if="usingDemoData"> · Demo</span></p><h1 class="page-title">逐个复习</h1></div><strong>{{ progressLabel }}</strong></header>
      <div class="session-progress"><span :style="{ width: `${progressPercent}%` }"></span></div>
      <article class="flashcard surface">
        <div class="flash-term"><small v-if="currentWord.kana">{{ currentWord.kana }}</small><h2 class="serif">{{ currentWord.word }}</h2></div>
        <div class="pronunciation"><span v-if="currentWord.us">US {{ currentWord.us }}</span><span v-if="currentWord.uk">UK {{ currentWord.uk }}</span></div>
        <button class="meaning-toggle" type="button" :aria-expanded="showMeaning" @click="showMeaning = !showMeaning"><component :is="showMeaning ? EyeOff : Eye" :size="16" />{{ showMeaning ? '隐藏释义' : '显示释义' }}</button>
        <div v-if="showMeaning" class="flash-meaning"><strong>{{ formatJsonList(currentWord.translations, 'translation') || '暂无释义' }}</strong><span v-if="currentWord.phrases && currentWord.phrases !== '[]'">{{ formatJsonList(currentWord.phrases, 'phrase') }}</span></div>
      </article>
      <div class="rating-actions"><button class="rating-btn unknown" type="button" :disabled="submitting" @click="submitRating('UNKNOWN')"><span>不认识</span><small>UNKNOWN</small></button><button class="rating-btn vague" type="button" :disabled="submitting" @click="submitRating('VAGUE')"><span>有点模糊</span><small>VAGUE</small></button><button class="rating-btn known" type="button" :disabled="submitting" @click="submitRating('KNOWN')"><span>完全知道</span><small>KNOWN</small></button></div>
    </section>
  </main>
</template>

<style scoped>
.back-link { display: inline-flex; align-items: center; gap: 6px; margin-bottom: 26px; color: var(--ink-muted); font-size: 13px; font-weight: 650; }.back-link:hover { color: var(--primary); }
.session-shell { width: min(100%, 720px); margin: 12px auto 0; }.session-heading { display: flex; align-items: end; justify-content: space-between; gap: 20px; }.session-heading .eyebrow { margin-bottom: 8px; }.session-heading .page-title { font-size: 36px; }.session-heading > strong { color: var(--ink-muted); font-size: 13px; font-weight: 650; }.session-progress { height: 5px; overflow: hidden; margin: 18px 0; border-radius: 4px; background: var(--surface-high); }.session-progress span { display: block; height: 100%; border-radius: inherit; background: var(--primary); transition: width .25s ease; }.flashcard { min-height: 380px; display: flex; align-items: center; flex-direction: column; padding: 42px 34px 34px; text-align: center; }.flash-term { display: flex; align-items: center; flex-direction: column; gap: 4px; margin-top: 34px; }.flash-term small { color: var(--ink-muted); font-size: 15px; }.flash-term h2 { margin: 0; color: var(--primary); font-size: 48px; line-height: 1.2; }.pronunciation { display: flex; gap: 18px; margin-top: 14px; color: var(--ink-muted); font-size: 13px; }.meaning-toggle { display: inline-flex; align-items: center; gap: 7px; margin-top: 34px; padding: 8px 12px; border: 1px solid var(--outline); border-radius: 6px; color: var(--primary); background: var(--surface-lowest); font-size: 12px; }.meaning-toggle:hover { border-color: var(--primary); background: var(--surface-low); }.flash-meaning { width: min(100%, 520px); display: grid; gap: 8px; padding-top: 20px; margin-top: 18px; border-top: 1px solid var(--outline); color: var(--ink); font-size: 14px; line-height: 1.6; }.flash-meaning span { color: var(--ink-muted); font-size: 12px; }.rating-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 18px; }.rating-btn { min-height: 70px; display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 4px; border: 1px solid var(--outline); border-radius: 7px; background: var(--surface-lowest); transition: border-color .18s ease, background-color .18s ease, transform .18s ease; }.rating-btn:hover:not(:disabled) { transform: translateY(-1px); }.rating-btn:disabled { opacity: .55; cursor: wait; }.rating-btn span { font-size: 14px; font-weight: 700; }.rating-btn small { font-size: 10px; font-weight: 750; letter-spacing: .04em; }.rating-btn.unknown { color: var(--error); }.rating-btn.unknown:hover:not(:disabled) { border-color: var(--error); background: #f9e8e8; }.rating-btn.vague { color: var(--secondary); }.rating-btn.vague:hover:not(:disabled) { border-color: var(--secondary); background: var(--secondary-soft); }.rating-btn.known { color: var(--success); }.rating-btn.known:hover:not(:disabled) { border-color: var(--success); background: #e6efe8; }.session-state { min-height: 320px; display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 9px; color: var(--ink-muted); text-align: center; }.session-state strong { color: var(--primary); font-size: 17px; }.session-state p { margin: 0; font-size: 13px; }.session-state .btn { margin-top: 8px; }.loading-line { width: 92px; height: 2px; overflow: hidden; position: relative; background: var(--surface-high); }.loading-line::after { position: absolute; inset: 0; content: ''; background: var(--primary); transform-origin: left; animation: loading 1s ease-in-out infinite; }@keyframes loading { 0% { transform: translateX(-100%) scaleX(.35); } 55% { transform: translateX(35%) scaleX(.65); } 100% { transform: translateX(110%) scaleX(.3); } }
@media (max-width: 640px) { .session-shell { margin-top: 0; }.session-heading .page-title { font-size: 31px; }.flashcard { min-height: 350px; padding: 34px 22px 28px; }.flash-term h2 { font-size: 39px; }.rating-actions { grid-template-columns: 1fr; }.rating-btn { min-height: 58px; } }
</style>
