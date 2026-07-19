<script setup lang="ts">
import { ArrowLeft, Filter, Search, Trash2 } from 'lucide-vue-next'
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppSelect from '@/components/AppSelect.vue'
import { collections, words } from '@/data/demo'
import type { Word } from '@/data/demo'

const route = useRoute()
const query = ref('')
const level = ref('')
const localWords = ref<Word[]>([...words])
const collection = computed(() => collections.find((item) => item.libraryId === route.params.id) ?? collections[0]!)
const englishLevels = [
  { value: 'BEC', label: 'BEC' }, { value: 'CET4', label: 'CET4' }, { value: 'CET6', label: 'CET6' },
  { value: 'GMAT', label: 'GMAT' }, { value: 'GRE', label: 'GRE' }, { value: 'IELTS', label: 'IELTS' },
  { value: 'LEVEL4', label: 'Level4' }, { value: 'LEVEL8', label: 'Level8' }, { value: 'SAT', label: 'SAT' },
  { value: 'TOEFL', label: 'TOEFL' }, { value: 'CHUZHONG', label: '初中' }, { value: 'GAOZHONG', label: '高中' },
  { value: 'KAOYAN', label: '考研' },
]
const japaneseLevels = ['N5', 'N4', 'N3', 'N2', 'N1'].map((value) => ({ value, label: value }))
const availableLevels = computed(() => collection.value.languageCode === 'ja' ? japaneseLevels : englishLevels)
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

function formatJsonList(value: string, field: 'translation' | 'phrase') {
  try {
    const items = JSON.parse(value) as Array<Record<string, string>>
    return items.map((item) => field === 'phrase' && item.translation ? `${item.phrase} · ${item.translation}` : item[field] ?? '').filter(Boolean).join('；')
  } catch {
    return value
  }
}

function removeWord(word: Word) {
  localWords.value = localWords.value.filter((item) => item.libraryWordId !== word.libraryWordId)
}

</script>

<template>
  <main class="page">
    <RouterLink class="back-link" to="/vocabulary"><ArrowLeft :size="16" />返回词汇库</RouterLink>
    <header class="page-header detail-header fade-in">
      <div><p class="eyebrow">Collection · {{ collection.wordCount.toLocaleString() }} words</p><h1 class="page-title">{{ collection.name }}</h1><p class="page-description">按掌握度安排下一次记忆练习，持续巩固阅读中真正遇到的词。</p></div>
    </header>

    <section class="word-toolbar surface fade-in">
      <label class="word-search"><Search :size="18" /><input v-model="query" placeholder="搜索单词" /></label>
      <AppSelect v-model="level" class="level-filter" :options="[{ value: '', label: '全部等级' }, ...availableLevels]" label="选择词汇等级" :icon="Filter" :columns="2" menu-width="286px" align="right" />
    </section>

    <section class="word-table surface fade-in">
      <div class="table-head"><span>单词</span><span>释义与短语</span><span>发音</span><span>等级</span><span>加入时间</span><span aria-hidden="true"></span></div>
      <article v-for="word in filteredWords" :key="word.libraryWordId" class="word-row">
        <div class="term"><div class="term-text"><small v-if="word.kana">{{ word.kana }}</small><strong class="serif">{{ word.word }}</strong></div></div>
        <div class="meaning"><strong>{{ formatJsonList(word.translations, 'translation') || '暂无释义' }}</strong><span v-if="word.phrases && word.phrases !== '[]'">{{ formatJsonList(word.phrases, 'phrase') }}</span></div>
        <div class="pronunciation"><span v-if="word.us">US {{ word.us }}</span><span v-if="word.uk">UK {{ word.uk }}</span></div>
        <div><span class="badge level-badge">{{ word.level }}</span></div>
        <div class="added-at">{{ formatDate(word.addedAt) }}</div>
        <button class="delete-word icon-btn" type="button" :aria-label="`从词汇库删除 ${word.word}`" title="从词汇库删除" @click="removeWord(word)"><Trash2 :size="16" /></button>
      </article>
      <div v-if="!filteredWords.length" class="table-empty">没有符合条件的单词。</div>
    </section>
  </main>
</template>

<style scoped>
.back-link { display: inline-flex; align-items: center; gap: 6px; margin-bottom: 26px; color: var(--ink-muted); font-size: 13px; font-weight: 650; }.back-link:hover { color: var(--primary); }
.detail-header { margin-bottom: 28px; }
.word-toolbar { position: relative; z-index: 10; min-height: 66px; display: flex; align-items: center; gap: 12px; padding: 10px 12px; margin-bottom: 18px; overflow: visible; }
.word-search { height: 44px; min-width: 0; flex: 1 1 auto; display: flex; align-items: center; gap: 9px; padding: 0 12px; border: 1px solid transparent; border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }.word-search:focus-within { border-color: var(--primary); }.word-search input { width: 100%; border: 0; outline: 0; background: transparent; }
.level-filter { width: 132px; flex: 0 0 auto; }
.word-table { position: relative; z-index: 1; overflow: hidden; }.table-head, .word-row { display: grid; grid-template-columns: 1.15fr 1.9fr 1.05fr .55fr 1.15fr 42px; gap: 17px; align-items: center; padding: 0 22px; }
.table-head { min-height: 46px; color: var(--ink-muted); background: var(--surface-low); font-size: 11px; font-weight: 700; text-transform: uppercase; }.word-row { min-height: 104px; border-top: 1px solid rgba(199,196,192,.7); }
.term { min-width: 0; }.term-text { display: flex; flex-direction: column; gap: 2px; min-width: 0; }.term strong { overflow: hidden; color: var(--primary); font-size: 19px; text-overflow: ellipsis; white-space: nowrap; }.term small { color: var(--ink-muted); font-size: 12px; }
.meaning { display: grid; gap: 5px; min-width: 0; }.meaning strong { overflow: hidden; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }.meaning span { overflow: hidden; color: var(--ink-muted); font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.pronunciation { display: grid; gap: 4px; color: var(--ink-muted); font-size: 12.5px; line-height: 1.45; }.level-badge { color: var(--secondary); background: var(--secondary-soft); }.added-at { color: var(--ink-muted); font-size: 12.5px; line-height: 1.45; white-space: nowrap; }.delete-word { color: var(--error); }.delete-word:hover { color: white; background: var(--error); }
.table-empty { padding: 50px; text-align: center; color: var(--ink-muted); }
@media (max-width: 1000px) { .table-head, .word-row { grid-template-columns: 1.15fr 1.8fr .55fr 1fr 42px; }.table-head span:nth-child(3), .pronunciation { display: none; }.word-row { gap: 12px; } }
@media (max-width: 760px) { .table-head { display: none; }.word-row { grid-template-columns: 1fr auto; gap: 12px; padding-block: 18px; }.meaning, .pronunciation { display: grid; grid-column: 1 / -1; }.meaning { grid-row: 2; }.pronunciation { grid-row: 3; }.added-at { grid-column: 1; }.word-toolbar { align-items: stretch; flex-direction: column; }.level-filter { width: 100%; }.level-menu { left: 0; right: auto; width: min(100%, 320px); } }
</style>
