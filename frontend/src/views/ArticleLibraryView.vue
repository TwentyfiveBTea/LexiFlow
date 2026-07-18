<script setup lang="ts">
import { Clock3, FileText, Grid2X2, List, MoreVertical, Search, SlidersHorizontal } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articles } from '@/data/demo'

const router = useRouter()
const query = ref('')
const view = ref<'grid' | 'list'>('grid')
const language = ref('全部语言')
const filteredArticles = computed(() => articles.filter((article) => {
  const matchesQuery = `${article.title}${article.source}${article.excerpt}`.toLowerCase().includes(query.value.toLowerCase())
  return matchesQuery && (language.value === '全部语言' || article.language === language.value)
}))
</script>

<template>
  <main class="page">
    <header class="page-header fade-in">
      <div><p class="eyebrow">Repository · {{ articles.length }} texts</p><h1 class="page-title">文章库</h1><p class="page-description">整理所有阅读材料，回到任何一段仍值得推敲的文本。</p></div>
    </header>

    <section class="library-toolbar surface fade-in">
      <label class="search-field"><Search :size="18" /><input v-model="query" placeholder="搜索标题、来源或正文摘要" /></label>
      <label class="filter-field"><SlidersHorizontal :size="17" /><select v-model="language"><option>全部语言</option><option>英语</option><option>法语</option><option>德语</option><option>日语</option></select></label>
      <div class="view-switch" aria-label="视图切换">
        <button :class="{ active: view === 'grid' }" aria-label="网格视图" @click="view = 'grid'"><Grid2X2 :size="17" /></button>
        <button :class="{ active: view === 'list' }" aria-label="列表视图" @click="view = 'list'"><List :size="18" /></button>
      </div>
    </section>

    <section class="article-grid" :class="{ list: view === 'list' }">
      <article v-for="(article, index) in filteredArticles" :key="article.id" class="article-card surface fade-in" :style="{ animationDelay: `${index * 70}ms` }">
        <button class="more icon-btn" aria-label="文章操作"><MoreVertical :size="18" /></button>
        <button class="cover" :class="article.tone" @click="router.push(`/reader/${article.id}`)"><FileText :size="38" :stroke-width="1.2" /><span>{{ article.language }}</span></button>
        <div class="article-copy">
          <div class="meta"><span class="badge">{{ article.language }}</span><span>{{ article.source }}</span><span>·</span><span>{{ article.date }}</span></div>
          <button class="title serif" @click="router.push(`/reader/${article.id}`)">{{ article.title }}</button>
          <p>{{ article.excerpt }}</p>
          <div class="card-footer">
            <div class="read-time"><Clock3 :size="14" />{{ article.readTime }}</div>
            <span>采摘 {{ article.words }} 词</span>
          </div>
          <div class="progress"><span :style="{ width: `${article.progress}%` }"></span></div>
        </div>
      </article>
    </section>

    <div v-if="!filteredArticles.length" class="empty surface"><Search :size="26" /><h2 class="serif">没有找到文章</h2><p>换一个关键词或语言筛选试试。</p></div>
  </main>
</template>

<style scoped>
.library-toolbar { min-height: 66px; display: flex; align-items: center; gap: 12px; padding: 10px 12px; margin-bottom: 28px; }
.search-field { height: 44px; flex: 1; display: flex; align-items: center; gap: 10px; padding: 0 13px; border: 1px solid transparent; border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }
.search-field:focus-within { border-color: var(--primary); }
.search-field input { width: 100%; border: 0; outline: 0; background: transparent; }
.filter-field { height: 44px; display: flex; align-items: center; gap: 8px; padding: 0 12px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); background: white; }
.filter-field select { min-width: 112px; border: 0; outline: 0; color: var(--ink); background: transparent; }
.view-switch { height: 44px; display: flex; padding: 3px; border: 1px solid var(--outline); border-radius: 7px; }
.view-switch button { width: 36px; display: grid; place-items: center; border: 0; border-radius: 5px; color: var(--ink-muted); background: transparent; }
.view-switch button.active { color: var(--primary); background: var(--surface-high); }
.article-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.article-card { position: relative; display: flex; gap: 20px; min-height: 252px; padding: 22px; }
.more { position: absolute; top: 10px; right: 9px; }
.cover { width: 118px; flex: 0 0 118px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 15px; border: 0; border-radius: 5px; color: white; background: var(--primary); }
.cover span { font-size: 11px; font-weight: 700; text-transform: uppercase; }
.cover.clay { background: var(--secondary); }.cover.charcoal { background: #454848; }.cover.sage { background: #597166; }
.article-copy { min-width: 0; flex: 1; display: flex; flex-direction: column; }
.meta { padding-right: 26px; display: flex; align-items: center; gap: 6px; color: var(--ink-muted); font-size: 11px; }
.title { margin: 13px 0 8px; padding: 0; border: 0; color: var(--ink); background: transparent; text-align: left; font-size: 20px; line-height: 1.35; font-weight: 600; }
.title:hover { color: var(--primary); }
.article-copy > p { margin: 0; overflow: hidden; color: var(--ink-muted); font-size: 13px; line-height: 1.55; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; }
.card-footer { margin-top: auto; display: flex; align-items: center; justify-content: space-between; color: var(--ink-muted); font-size: 11px; }
.read-time { display: flex; align-items: center; gap: 5px; }
.article-card .progress { margin-top: 12px; }
.article-grid.list { grid-template-columns: 1fr; }
.article-grid.list .article-card { min-height: 190px; }
.article-grid.list .cover { width: 100px; flex-basis: 100px; }
.empty { padding: 60px 24px; text-align: center; color: var(--ink-muted); }
.empty h2 { margin: 14px 0 4px; color: var(--primary); }
.empty p { margin: 0; }
@media (max-width: 1100px) { .article-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .library-toolbar { align-items: stretch; flex-wrap: wrap; }.search-field { flex-basis: 100%; }.filter-field { flex: 1; }.article-card { min-height: 210px; gap: 14px; padding: 16px; }.cover { width: 72px; flex-basis: 72px; }.article-copy > p { -webkit-line-clamp: 2; }.card-footer { flex-direction: column; align-items: flex-start; gap: 4px; } }
</style>
