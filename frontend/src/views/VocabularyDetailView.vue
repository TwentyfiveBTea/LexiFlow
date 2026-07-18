<script setup lang="ts">
import { ArrowLeft, ChevronDown, Filter, Search, Volume2 } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { collections, words } from '@/data/demo'

const route = useRoute()
const query = ref('')
const level = ref('全部等级')
const collection = computed(() => collections.find((item) => item.libraryId === route.params.id) ?? collections[0]!)
const filteredWords = computed(() => words.filter((word) => {
  const match = `${word.term}${word.translation}${word.definition}`.toLowerCase().includes(query.value.toLowerCase())
  return match && (level.value === '全部等级' || word.level === level.value)
}))

function pronounce(word: string) {
  if ('speechSynthesis' in window) {
    speechSynthesis.cancel()
    speechSynthesis.speak(new SpeechSynthesisUtterance(word))
  }
}
</script>

<template>
  <main class="page">
    <RouterLink class="back-link" to="/vocabulary"><ArrowLeft :size="16" />返回词汇库</RouterLink>
    <header class="page-header detail-header fade-in">
      <div><p class="eyebrow">Collection · {{ collection.wordCount.toLocaleString() }} words</p><h1 class="page-title">{{ collection.name }}</h1><p class="page-description">按掌握度安排下一次记忆练习，持续巩固阅读中真正遇到的词。</p></div>
      <RouterLink class="btn btn-primary" to="/reader/algorithmic-sentience">开始复习</RouterLink>
    </header>

    <section class="word-toolbar surface fade-in">
      <label class="word-search"><Search :size="18" /><input v-model="query" placeholder="搜索单词、释义" /></label>
      <label class="level-filter"><Filter :size="16" /><select v-model="level"><option>全部等级</option><option>GRE</option><option>IELTS</option></select><ChevronDown :size="14" /></label>
    </section>

    <section class="word-table surface fade-in">
      <div class="table-head"><span>单词</span><span>释义</span><span>等级</span><span>掌握度</span></div>
      <article v-for="word in filteredWords" :key="word.id" class="word-row">
        <div class="term"><button class="sound" :aria-label="`朗读 ${word.term}`" @click="pronounce(word.term)"><Volume2 :size="16" /></button><div><strong class="serif">{{ word.term }}</strong><small>{{ word.phonetic }}</small></div></div>
        <div class="meaning"><strong>{{ word.partOfSpeech }} {{ word.translation }}</strong><span>{{ word.definition }}</span></div>
        <div><span class="badge">{{ word.level }}</span></div>
        <div class="mastery-cell"><div class="progress"><span :style="{ width: `${word.mastery}%` }"></span></div><small>{{ word.mastery }}%</small></div>
      </article>
      <div v-if="!filteredWords.length" class="table-empty">没有符合条件的单词。</div>
    </section>
  </main>
</template>

<style scoped>
.back-link { display: inline-flex; align-items: center; gap: 6px; margin-bottom: 26px; color: var(--ink-muted); font-size: 13px; font-weight: 650; }.back-link:hover { color: var(--primary); }
.detail-header { margin-bottom: 28px; }
.word-toolbar { min-height: 66px; display: flex; align-items: center; gap: 12px; padding: 10px 12px; margin-bottom: 18px; }
.word-search { height: 44px; flex: 1; display: flex; align-items: center; gap: 9px; padding: 0 12px; border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }.word-search input { width: 100%; border: 0; outline: 0; background: transparent; }
.level-filter { height: 44px; display: flex; align-items: center; gap: 7px; padding: 0 12px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); }.level-filter select { border: 0; outline: 0; appearance: none; background: transparent; }
.word-table { overflow: hidden; }.table-head, .word-row { display: grid; grid-template-columns: 1.2fr 2.4fr .6fr 1fr; gap: 20px; align-items: center; padding: 0 22px; }
.table-head { min-height: 46px; color: var(--ink-muted); background: var(--surface-low); font-size: 11px; font-weight: 700; text-transform: uppercase; }
.word-row { min-height: 98px; border-top: 1px solid rgba(199,196,192,.7); }
.term { display: flex; align-items: center; gap: 11px; }.sound { width: 32px; height: 32px; display: grid; place-items: center; border: 0; border-radius: 50%; color: var(--primary); background: var(--primary-soft); }.term div { display: grid; gap: 3px; }.term strong { font-size: 19px; color: var(--primary); }.term small { color: var(--ink-muted); }
.meaning { display: grid; gap: 4px; min-width: 0; }.meaning strong { font-size: 13px; }.meaning span { overflow: hidden; color: var(--ink-muted); font-size: 12px; white-space: nowrap; text-overflow: ellipsis; }
.mastery-cell { display: grid; grid-template-columns: 1fr 30px; align-items: center; gap: 8px; }.mastery-cell small { color: var(--ink-muted); font-size: 11px; }
.table-empty { padding: 50px; text-align: center; color: var(--ink-muted); }
@media (max-width: 760px) { .table-head { display: none; }.word-row { grid-template-columns: 1fr auto; gap: 12px; padding-block: 18px; }.meaning { grid-column: 1 / -1; grid-row: 2; }.mastery-cell { grid-column: 1 / -1; }.word-toolbar { align-items: stretch; flex-direction: column; }.detail-header .btn { align-self: flex-start; } }
</style>
