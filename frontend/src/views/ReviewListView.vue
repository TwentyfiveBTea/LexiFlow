<script setup lang="ts">
import { ArrowRight, BookOpenCheck, RefreshCw } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { words } from '@/data/demo'
import { getDueWords } from '@/lib/api'
import type { DueWordResponse } from '@/lib/api'

const router = useRouter()
const dueWords = ref<DueWordResponse[]>([])
const loading = ref(true)
const error = ref('')
const usingDemoData = ref(false)

interface TranslationItem {
  type: string
  translation: string
}

const demoWords: DueWordResponse[] = words.map((word) => ({
  libraryWordId: word.libraryWordId,
  wordId: word.wordId,
  languageCode: word.languageCode,
  level: word.level,
  word: word.word,
  kana: word.kana || null,
  us: word.us || null,
  uk: word.uk || null,
  translations: word.translations,
}))

const countLabel = computed(() => `${dueWords.value.length} 个词条`)

function parseTranslations(value: string | null): TranslationItem[] {
  if (!value) return []
  try {
    const items = JSON.parse(value) as Array<Record<string, string>>
    return items
      .map((item) => ({
        type: item.type?.trim() ?? '',
        translation: (item.translation || item.meaning || '').trim(),
      }))
      .filter((item) => item.translation)
  } catch {
    return [{ type: '', translation: value }]
  }
}

function formatPhonetic(value: string | null) {
  const normalized = value?.trim() ?? ''
  if (!normalized) return ''
  return normalized.startsWith('[') && normalized.endsWith(']') ? normalized : `[${normalized}]`
}

async function loadDueWords() {
  loading.value = true
  error.value = ''
  usingDemoData.value = false
  try {
    dueWords.value = await getDueWords()
  } catch {
    if (import.meta.env.DEV) {
      usingDemoData.value = true
      dueWords.value = demoWords
    } else {
      error.value = '待复习单词加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => { void loadDueWords() })
</script>

<template>
  <main class="page review-page">
    <header class="page-header fade-in">
      <div>
        <p class="eyebrow">Memory deck · Today</p>
        <h1 class="page-title">单词复习</h1>
        <p class="page-description">把今天需要复习的单词逐个复习一遍，保证记忆的连续性</p>
      </div>
      <button class="btn btn-primary" type="button" :disabled="loading || !dueWords.length" @click="router.push('/review/session')">
        <BookOpenCheck :size="17" />开始复习<ArrowRight :size="16" />
      </button>
    </header>

    <section class="review-summary surface fade-in">
      <div><span class="summary-icon"><BookOpenCheck :size="20" /></span><div><strong class="serif">{{ countLabel }}</strong><p>今日待复习</p></div></div>
      <span v-if="usingDemoData" class="demo-badge">示例数据</span>
    </section>

    <section v-if="loading" class="review-state surface fade-in"><RefreshCw :size="20" class="spin" /><p>正在加载待复习单词...</p></section>
    <section v-else-if="error" class="review-state surface fade-in"><p>{{ error }}</p><button class="btn btn-secondary" type="button" @click="loadDueWords">重新加载</button></section>
    <section v-else-if="!dueWords.length" class="review-state surface fade-in"><BookOpenCheck :size="24" /><strong>今天没有待复习单词</strong><p>新的到期词条会在这里出现</p></section>
    <section v-else class="review-table surface fade-in">
      <div class="review-table-head"><span>词条</span><span>释义</span><span>发音</span><span>等级</span><span>语言</span></div>
      <article v-for="word in dueWords" :key="word.libraryWordId ?? `${word.languageCode}-${word.wordId}`" class="review-row">
        <div class="term"><strong class="serif">{{ word.word }}</strong></div>
        <div class="meaning">
          <template v-if="parseTranslations(word.translations).length">
            <div v-for="(item, index) in parseTranslations(word.translations)" :key="`${item.type}-${index}`" class="meaning-item">
              <span v-if="item.type" class="part-of-speech">{{ item.type }}</span>
              <span>{{ item.translation }}</span>
            </div>
          </template>
          <span v-else class="empty-value">暂无释义</span>
        </div>
        <div class="pronunciation"><span v-if="word.kana">{{ word.kana }}</span><span v-if="word.us">US {{ formatPhonetic(word.us) }}</span><span v-if="word.uk">UK {{ formatPhonetic(word.uk) }}</span><span v-if="!word.kana && !word.us && !word.uk">-</span></div>
        <span class="badge level-badge">{{ word.level || '-' }}</span>
        <span class="badge">{{ word.languageCode }}</span>
      </article>
    </section>
  </main>
</template>

<style scoped>
.review-page .page-header { align-items: center; }
.review-page .page-header > .btn { flex: 0 0 auto; }
.review-summary { min-height: 82px; display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 18px 22px; margin-bottom: 18px; }
.review-summary > div { display: flex; align-items: center; gap: 12px; }.summary-icon { width: 40px; height: 40px; display: grid; place-items: center; border-radius: 7px; color: var(--primary); background: var(--primary-soft); }.review-summary strong { display: block; color: var(--primary); font-size: 22px; }.review-summary p { margin: 2px 0 0; color: var(--ink-muted); font-size: 12px; }.demo-badge { padding: 4px 8px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; }
.review-table { overflow: hidden; }.review-table-head, .review-row { display: grid; grid-template-columns: 1.2fr 2fr 1.35fr .7fr .45fr; gap: 18px; align-items: center; padding: 0 22px; }.review-table-head { min-height: 44px; color: var(--ink-muted); background: var(--surface-low); font-size: 10px; font-weight: 750; text-transform: uppercase; }.review-row { min-height: 88px; padding-top: 14px; padding-bottom: 14px; border-top: 1px solid rgba(199,196,192,.7); }.term { min-width: 0; }.term strong { overflow: hidden; display: block; color: var(--primary); font-size: 19px; text-overflow: ellipsis; white-space: nowrap; }.meaning { min-width: 0; display: grid; gap: 5px; color: var(--ink); font-size: 13px; line-height: 1.45; }.meaning-item { min-width: 0; display: flex; align-items: baseline; gap: 7px; }.meaning-item > span:last-child { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.part-of-speech { flex: 0 0 auto; min-width: 28px; color: var(--secondary); font-size: 11px; font-weight: 750; }.empty-value { color: var(--ink-muted); }.pronunciation { display: grid; gap: 3px; color: var(--ink-muted); font-size: 12px; line-height: 1.35; }.review-row .badge { width: fit-content; color: var(--secondary); background: var(--secondary-soft); text-transform: lowercase; }.review-row .level-badge { text-transform: none; }.review-state { min-height: 280px; display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 8px; color: var(--ink-muted); text-align: center; }.review-state strong { color: var(--primary); font-size: 16px; }.review-state p { margin: 0; font-size: 13px; }.review-state .btn { margin-top: 8px; }.spin { animation: spin 1s linear infinite; }@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 760px) { .review-page .page-header { align-items: stretch; }.review-page .page-header > .btn { width: 100%; }.review-table-head { display: none; }.review-row { grid-template-columns: 1fr auto; gap: 10px 16px; padding: 16px 18px; }.meaning, .pronunciation { grid-column: 1 / -1; }.meaning { grid-row: 2; }.pronunciation { grid-row: 3; }.review-row .badge { grid-column: 2; grid-row: 1; }.review-row .level-badge { grid-column: 1; grid-row: 4; } }
</style>
