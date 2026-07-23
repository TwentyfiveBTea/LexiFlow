<script setup lang="ts">
import { ArrowLeft, Filter, RefreshCw, Search, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppSelect from '@/components/AppSelect.vue'
import { collections, words } from '@/data/demo'
import type { Word } from '@/data/demo'
import { deleteVocabLibraryWord, getVocabLibraries, getVocabLibraryStatistics, getVocabLibraryWords } from '@/lib/api'
import type { VocabLibraryResponse, VocabLibraryStatisticsResponse, VocabLibraryWordResponse } from '@/lib/api'

const route = useRoute()
const query = ref('')
const level = ref('')
const localWords = ref<Word[]>([...words])
const collectionOverride = ref<ReturnType<typeof mapLibrary> | null>(null)
const collection = computed(() => collectionOverride.value ?? collections.find((item) => item.libraryId === route.params.id) ?? collections[0]!)
const loading = ref(true)
const loadError = ref('')
const usingDemoData = ref(false)
const deleteError = ref('')
const englishLevels = [
  { value: 'BEC', label: 'BEC' }, { value: 'CET4', label: 'CET4' }, { value: 'CET6', label: 'CET6' },
  { value: 'GMAT', label: 'GMAT' }, { value: 'GRE', label: 'GRE' }, { value: 'IELTS', label: 'IELTS' },
  { value: 'LEVEL4', label: 'Level4' }, { value: 'LEVEL8', label: 'Level8' }, { value: 'SAT', label: 'SAT' },
  { value: 'TOEFL', label: 'TOEFL' }, { value: 'CHUZHONG', label: '初中' }, { value: 'GAOZHONG', label: '高中' },
  { value: 'KAOYAN', label: '考研' },
]
const japaneseLevels = ['N5', 'N4', 'N3', 'N2', 'N1'].map((value) => ({ value, label: value }))
const availableLevels = computed(() => collection.value.languageCode === 'ja' ? japaneseLevels : englishLevels)

function mapLibrary(library: VocabLibraryResponse, statistics: VocabLibraryStatisticsResponse | null) {
  return {
    libraryId: library.libraryId,
    name: library.name,
    languageCode: library.languageCode === 'ja' ? 'ja' as const : 'en' as const,
    description: library.description ?? '',
    wordCount: statistics?.totalCount ?? library.wordCount,
    createdAt: library.createdAt,
    updatedAt: library.updatedAt,
    tone: 'blue' as const,
    statistics: statistics ? {
      totalCount: statistics.totalCount,
      newCount: statistics.newCount,
      learningCount: statistics.learningCount,
      masteredCount: statistics.masteredCount,
      dueCount: statistics.dueCount,
    } : { totalCount: library.wordCount, newCount: 0, learningCount: 0, masteredCount: 0, dueCount: 0 },
  }
}

function mapWord(word: VocabLibraryWordResponse): Word {
  return {
    id: word.wordId,
    term: word.word,
    libraryWordId: word.libraryWordId,
    wordId: word.wordId,
    languageCode: word.languageCode,
    word: word.word,
    kana: word.kana ?? '',
    us: word.us ?? '',
    uk: word.uk ?? '',
    translations: word.translations ?? '[]',
    phrases: word.phrases ?? '[]',
    addedAt: word.addedAt,
    phonetic: word.us ?? word.uk ?? word.kana ?? '',
    partOfSpeech: '',
    definition: '',
    translation: '',
    level: '',
    mastery: 0,
  }
}

async function loadDetail() {
  loading.value = true
  loadError.value = ''
  usingDemoData.value = false
  try {
    const libraryId = String(route.params.id)
    const [libraries, wordsResult] = await Promise.all([
      getVocabLibraries(),
      getVocabLibraryWords(libraryId),
    ])
    const library = libraries.find((item) => item.libraryId === libraryId)
    if (!library) throw new Error('词汇库不存在')
    let statistics: VocabLibraryStatisticsResponse | null = null
    try {
      statistics = await getVocabLibraryStatistics(libraryId)
    } catch {
      // The word list remains usable without the optional statistics request.
    }
    collectionOverride.value = mapLibrary(library, statistics)
    localWords.value = wordsResult.map(mapWord)
  } catch (error) {
    if (import.meta.env.DEV) {
      usingDemoData.value = true
      collectionOverride.value = null
      localWords.value = [...words]
    } else {
      loadError.value = error instanceof Error ? error.message : '词汇库详情加载失败'
    }
  } finally {
    loading.value = false
  }
}

interface TranslationItem {
  type: string
  translation: string
}

const filteredWords = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  return localWords.value.filter((word) => {
    const searchable = [word.word, word.kana, word.us, word.uk, word.translations, word.phrases, word.translation, word.definition].join(' ').toLowerCase()
    return word.languageCode === collection.value.languageCode
      && (!keyword || searchable.includes(keyword))
      && (!level.value || word.level === level.value)
  })
})

watch(() => collection.value.languageCode, () => {
  level.value = ''
})

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false,
})

function formatDate(value: string) {
  return dateFormatter.format(new Date(value))
}

function formatPhonetic(value: string | null) {
  const normalized = value?.trim() ?? ''
  if (!normalized) return ''
  return normalized.startsWith('[') && normalized.endsWith(']') ? normalized : `[${normalized}]`
}

function parseTranslations(value: string): TranslationItem[] {
  try {
    const items = JSON.parse(value) as Array<Record<string, string>>
    return items
      .map((item) => ({
        type: item.type?.trim() ?? '',
        translation: (item.translation || item.meaning || '').trim(),
      }))
      .filter((item) => item.translation)
  } catch {
    return value ? [{ type: '', translation: value }] : []
  }
}

function formatPhrases(value: string) {
  try {
    const items = JSON.parse(value) as Array<Record<string, string>>
    return items.map((item) => item.translation ? `${item.phrase} · ${item.translation}` : item.phrase ?? '').filter(Boolean).join('；')
  } catch {
    return value
  }
}

async function removeWord(word: Word) {
  deleteError.value = ''
  try {
    if (!usingDemoData.value) {
      await deleteVocabLibraryWord(String(route.params.id), word.wordId, word.languageCode)
    }
    localWords.value = localWords.value.filter((item) => item.libraryWordId !== word.libraryWordId)
  } catch (error) {
    deleteError.value = error instanceof Error ? error.message : '删除词条失败'
  }
}

onMounted(() => { void loadDetail() })

</script>

<template>
  <main class="page">
    <RouterLink class="back-link" to="/vocabulary"><ArrowLeft :size="16" />返回词汇库</RouterLink>
    <header class="page-header detail-header fade-in">
      <div><p class="eyebrow">Collection · {{ collection.wordCount.toLocaleString() }} words<span v-if="usingDemoData"> · Demo</span></p><h1 class="page-title">{{ collection.name }}</h1><p class="page-description">按掌握度安排下一次记忆练习，持续巩固阅读中真正遇到的词</p></div>
    </header>

    <section class="word-toolbar surface fade-in">
      <label class="word-search"><Search :size="18" /><input v-model="query" placeholder="搜索单词" /></label>
      <AppSelect v-model="level" class="level-filter" :options="[{ value: '', label: '全部等级' }, ...availableLevels]" label="选择词汇等级" :icon="Filter" :columns="2" menu-width="286px" align="right" />
    </section>

    <section v-if="loading" class="detail-state surface"><RefreshCw :size="20" class="spin" /><p>正在加载词条...</p></section>
    <section v-else-if="loadError" class="detail-state surface"><p>{{ loadError }}</p><button class="btn btn-secondary" type="button" @click="loadDetail">重新加载</button></section>
    <section v-else class="word-table surface fade-in">
      <div class="table-head"><span>单词</span><span>释义与短语</span><span>发音</span><span>等级</span><span>加入时间</span><span aria-hidden="true"></span></div>
      <article v-for="word in filteredWords" :key="word.libraryWordId" class="word-row">
        <div class="term"><div class="term-text"><small v-if="word.kana">{{ word.kana }}</small><strong class="serif">{{ word.word }}</strong></div></div>
        <div class="meaning">
          <template v-if="parseTranslations(word.translations).length">
            <div v-for="(item, index) in parseTranslations(word.translations)" :key="`${item.type}-${index}`" class="translation-item">
              <span v-if="item.type" class="part-of-speech">{{ item.type }}</span>
              <strong>{{ item.translation }}</strong>
            </div>
          </template>
          <strong v-else class="empty-value">暂无释义</strong>
          <span v-if="word.phrases && word.phrases !== '[]'" class="phrases">{{ formatPhrases(word.phrases) }}</span>
        </div>
        <div class="pronunciation"><span v-if="word.us">US {{ formatPhonetic(word.us) }}</span><span v-if="word.uk">UK {{ formatPhonetic(word.uk) }}</span></div>
        <div><span class="badge level-badge">{{ word.level }}</span></div>
        <div class="added-at">{{ formatDate(word.addedAt) }}</div>
        <button class="delete-word icon-btn" type="button" :aria-label="`从词汇库删除 ${word.word}`" title="从词汇库删除" @click="removeWord(word)"><Trash2 :size="16" /></button>
      </article>
      <div v-if="!filteredWords.length" class="table-empty">没有符合条件的单词</div>
    </section>
    <p v-if="deleteError" class="delete-error" role="alert">{{ deleteError }}</p>
  </main>
</template>

<style scoped>
.back-link { display: inline-flex; align-items: center; gap: 6px; margin-bottom: 26px; color: var(--ink-muted); font-size: 13px; font-weight: 650; }.back-link:hover { color: var(--primary); }
.detail-header { margin-bottom: 28px; }.detail-header .page-title { max-width: 100%; overflow-wrap: anywhere; word-break: break-word; }
.word-toolbar { position: relative; z-index: 10; min-height: 66px; display: flex; align-items: center; gap: 12px; padding: 10px 12px; margin-bottom: 18px; overflow: visible; }
.word-search { height: 44px; min-width: 0; flex: 1 1 auto; display: flex; align-items: center; gap: 9px; padding: 0 12px; border: 1px solid transparent; border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }.word-search:focus-within { border-color: var(--primary); }.word-search input { width: 100%; border: 0; outline: 0; background: transparent; }
.level-filter { width: 132px; flex: 0 0 auto; }
.word-table { position: relative; z-index: 1; overflow: hidden; }.table-head, .word-row { display: grid; grid-template-columns: 1.15fr 1.9fr 1.05fr .55fr 1.15fr 42px; gap: 17px; align-items: center; padding: 0 22px; }
.table-head { min-height: 46px; color: var(--ink-muted); background: var(--surface-low); font-size: 11px; font-weight: 700; text-transform: uppercase; }.word-row { min-height: 104px; border-top: 1px solid rgba(199,196,192,.7); }
.term { min-width: 0; }.term-text { display: flex; flex-direction: column; gap: 2px; min-width: 0; }.term strong { overflow: hidden; color: var(--primary); font-size: 19px; text-overflow: ellipsis; white-space: nowrap; }.term small { color: var(--ink-muted); font-size: 12px; }
.meaning { display: grid; gap: 5px; min-width: 0; }.translation-item { min-width: 0; display: flex; align-items: baseline; gap: 7px; }.translation-item strong { overflow: hidden; font-size: 13px; font-weight: 650; text-overflow: ellipsis; white-space: nowrap; }.translation-item .part-of-speech { flex: 0 0 auto; min-width: 28px; color: var(--secondary); font-size: 11px; font-weight: 750; }.meaning .phrases { overflow: hidden; color: var(--ink-muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }.meaning .empty-value { color: var(--ink-muted); font-size: 13px; font-weight: 600; }
.pronunciation { display: grid; gap: 4px; color: var(--ink-muted); font-size: 12.5px; line-height: 1.45; }.level-badge { color: var(--secondary); background: var(--secondary-soft); }.added-at { color: var(--ink-muted); font-size: 12.5px; line-height: 1.45; white-space: nowrap; }.delete-word { color: var(--error); }.delete-word:hover { color: white; background: var(--error); }
.table-empty { padding: 50px; text-align: center; color: var(--ink-muted); }
.detail-state { min-height: 280px; display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 10px; color: var(--ink-muted); text-align: center; }.detail-state p { margin: 0; font-size: 13px; }.detail-state .btn { margin-top: 8px; }.spin { animation: spin 1s linear infinite; }@keyframes spin { to { transform: rotate(360deg); } }.delete-error { margin: 12px 0; color: var(--error); font-size: 12px; }
@media (max-width: 1000px) { .table-head, .word-row { grid-template-columns: 1.15fr 1.8fr .55fr 1fr 42px; }.table-head span:nth-child(3), .pronunciation { display: none; }.word-row { gap: 12px; } }
@media (max-width: 760px) { .table-head { display: none; }.word-row { grid-template-columns: 1fr auto; gap: 12px; padding-block: 18px; }.meaning, .pronunciation { display: grid; grid-column: 1 / -1; }.meaning { grid-row: 2; }.pronunciation { grid-row: 3; }.added-at { grid-column: 1; }.word-toolbar { align-items: stretch; flex-direction: column; }.level-filter { width: 100%; }.level-menu { left: 0; right: auto; width: min(100%, 320px); } }
</style>
