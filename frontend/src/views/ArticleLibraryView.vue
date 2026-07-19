<script setup lang="ts">
import { FileText, Grid2X2, List, MoreVertical, Search, X } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articles } from '@/data/demo'
import type { Article } from '@/data/demo'

const router = useRouter()
const query = ref('')
const view = ref<'grid' | 'list'>('grid')
const languageFilter = ref<'all' | 'en' | 'ja'>('all')
const selectedArticle = ref<Article | null>(null)
const filteredArticles = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  return articles.filter((article) => {
    const matchesKeyword = !keyword || article.title.toLowerCase().includes(keyword)
    const matchesLanguage = languageFilter.value === 'all' || article.languageCode === languageFilter.value
    return matchesKeyword && matchesLanguage
  })
})

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

const detailItems = computed(() => {
  const detail = selectedArticle.value?.processingDetail
  if (!detail) return []
  return [
    { label: '词数统计', value: detail.wordCount.toLocaleString(), type: 'count' },
    { label: '解析状态', value: formatStatus(detail.parseStatus), type: 'status' },
    { label: '翻译状态', value: formatStatus(detail.translationStatus), type: 'status' },
    { label: '词汇分析状态', value: formatStatus(detail.analysisStatus), type: 'status' },
    { label: '解析完成时间', value: formatDate(detail.parsedAt), type: 'date' },
    { label: '翻译完成时间', value: formatDate(detail.translatedAt), type: 'date' },
    { label: '最近分析完成时间', value: formatDate(detail.analyzedAt), type: 'date' },
  ]
})

function formatDate(value: string | null) {
  return value ? dateFormatter.format(new Date(value)) : '尚未完成'
}

function formatStatus(value: number) {
  return `${value} · ${['待处理', '处理中', '已完成', '处理失败'][value] ?? '未知状态'}`
}
</script>

<template>
  <main class="page">
    <header class="page-header fade-in">
      <div><p class="eyebrow">Repository · {{ articles.length }} texts</p><h1 class="page-title">文章库</h1><p class="page-description">整理所有阅读材料，回到任何一段仍值得推敲的文本。</p></div>
    </header>

    <section class="library-toolbar surface fade-in">
      <label class="search-field"><Search :size="18" /><input v-model="query" placeholder="搜索标题" /></label>
      <div class="language-filter" role="radiogroup" aria-label="按语言筛选文章">
        <button type="button" role="radio" :aria-checked="languageFilter === 'all'" :class="{ active: languageFilter === 'all' }" @click="languageFilter = 'all'">全部</button>
        <button type="button" role="radio" :aria-checked="languageFilter === 'en'" :class="{ active: languageFilter === 'en' }" @click="languageFilter = 'en'">en</button>
        <button type="button" role="radio" :aria-checked="languageFilter === 'ja'" :class="{ active: languageFilter === 'ja' }" @click="languageFilter = 'ja'">ja</button>
      </div>
      <div class="view-switch" aria-label="视图切换">
        <button type="button" :class="{ active: view === 'grid' }" aria-label="网格视图" @click="view = 'grid'"><Grid2X2 :size="17" /></button>
        <button type="button" :class="{ active: view === 'list' }" aria-label="列表视图" @click="view = 'list'"><List :size="18" /></button>
      </div>
    </section>

    <section class="article-grid" :class="{ list: view === 'list' }">
      <article v-for="(article, index) in filteredArticles" :key="article.articleId" class="article-card surface fade-in" :style="{ animationDelay: `${index * 70}ms` }">
        <button class="more icon-btn" type="button" :aria-label="`查看 ${article.title} 详细信息`" title="查看文章详细信息" @click="selectedArticle = article"><MoreVertical :size="18" /></button>
        <button class="cover" :class="article.tone" @click="router.push(`/reader/${article.articleId}`)"><FileText :size="38" :stroke-width="1.2" /></button>
        <div class="article-copy">
          <span class="language-badge">{{ article.languageCode }}</span>
          <button class="title serif" @click="router.push(`/reader/${article.articleId}`)">{{ article.title }}</button>
          <dl class="article-meta">
            <div><dt>文章主语言</dt><dd>{{ article.languageCode }}</dd></div>
            <div><dt>词数统计</dt><dd>{{ article.wordCount.toLocaleString() }}</dd></div>
            <div><dt>创建时间</dt><dd>{{ formatDate(article.createdAt) }}</dd></div>
          </dl>
        </div>
      </article>
    </section>

    <div v-if="!filteredArticles.length" class="empty surface"><Search :size="26" /><h2 class="serif">没有找到文章</h2><p>换一个关键词或语言筛选试试。</p></div>

    <Transition name="dialog">
      <div v-if="selectedArticle" class="modal-backdrop" @click.self="selectedArticle = null">
        <section class="dialog-panel article-detail-modal surface" role="dialog" aria-modal="true" aria-labelledby="article-detail-title">
          <div class="modal-heading">
            <div><p class="eyebrow">Processing detail</p><h2 id="article-detail-title" class="serif">文章详细信息</h2><p>{{ selectedArticle.title }}</p></div>
            <button class="icon-btn" type="button" aria-label="关闭文章详细信息弹窗" @click="selectedArticle = null"><X :size="18" /></button>
          </div>
          <dl class="detail-list">
            <div v-for="item in detailItems" :key="item.label" :class="item.type"><dt>{{ item.label }}</dt><dd :class="{ pending: item.value === '尚未完成' }">{{ item.value }}</dd></div>
          </dl>
          <div class="modal-actions"><button class="btn btn-primary" type="button" @click="selectedArticle = null">完成</button></div>
        </section>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.library-toolbar { min-height: 66px; display: flex; align-items: center; gap: 12px; padding: 10px 12px; margin-bottom: 28px; }
.search-field { height: 44px; flex: 1; display: flex; align-items: center; gap: 10px; padding: 0 13px; border: 1px solid transparent; border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }
.search-field:focus-within { border-color: var(--primary); }
.search-field input { width: 100%; border: 0; outline: 0; background: transparent; }
.language-filter { height: 44px; display: grid; grid-template-columns: repeat(3, minmax(38px, auto)); gap: 2px; padding: 3px; border: 1px solid var(--outline); border-radius: 7px; background: var(--surface-low); }
.language-filter button { min-width: 0; padding: 0 10px; border: 0; border-radius: 5px; color: var(--ink-muted); background: transparent; font-size: 11px; font-weight: 650; text-transform: lowercase; transition: color .16s ease, background-color .16s ease, box-shadow .16s ease; }
.language-filter button:hover { color: var(--primary); }.language-filter button:focus-visible { outline: 2px solid var(--primary); outline-offset: -2px; }.language-filter button.active { color: var(--primary); background: white; box-shadow: 0 1px 4px rgba(45,45,45,.11); }
.view-switch { height: 44px; display: flex; padding: 3px; border: 1px solid var(--outline); border-radius: 7px; }
.view-switch button { width: 36px; display: grid; place-items: center; border: 0; border-radius: 5px; color: var(--ink-muted); background: transparent; }
.view-switch button.active { color: var(--primary); background: var(--surface-high); }
.article-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.article-card { position: relative; display: flex; gap: 20px; min-height: 238px; padding: 22px; }
.more { position: absolute; top: 10px; right: 9px; }
.cover { width: 108px; flex: 0 0 108px; display: grid; place-items: center; border: 0; border-radius: 5px; color: white; background: var(--primary); }
.cover.clay { background: var(--secondary); }.cover.charcoal { background: #454848; }.cover.sage { background: #597166; }
.article-copy { min-width: 0; flex: 1; display: flex; flex-direction: column; }
.language-badge { width: fit-content; min-height: 22px; display: inline-flex; align-items: center; padding: 0 7px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; text-transform: lowercase; }
.title { margin: 13px 30px 12px 0; padding: 0; border: 0; color: var(--ink); background: transparent; text-align: left; font-size: 20px; line-height: 1.35; font-weight: 600; }
.title:hover { color: var(--primary); }
.article-meta { display: grid; gap: 7px; margin: auto 0 0; padding-top: 13px; border-top: 1px solid rgba(199,196,192,.65); }
.article-meta div { display: flex; align-items: center; justify-content: space-between; gap: 12px; }.article-meta dt { color: var(--ink-muted); font-size: 11px; }.article-meta dd { margin: 0; color: var(--ink); font-size: 11px; font-weight: 650; white-space: nowrap; }
.article-grid.list { grid-template-columns: 1fr; }
.article-grid.list .article-card { min-height: 210px; }
.article-grid.list .cover { width: 100px; flex-basis: 100px; }
.empty { padding: 60px 24px; text-align: center; color: var(--ink-muted); }
.empty h2 { margin: 14px 0 4px; color: var(--primary); }
.empty p { margin: 0; }
.modal-backdrop { position: fixed; z-index: 60; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }
.article-detail-modal { width: min(100%, 540px); padding: 28px 30px; }
.modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 22px; }.modal-heading h2 { margin: 0; color: var(--primary); font-size: 26px; }.modal-heading > div > p:last-child { max-width: 410px; margin: 6px 0 0; overflow: hidden; color: var(--ink-muted); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.detail-list { margin: 0; border-top: 1px solid var(--outline); }.detail-list div { min-height: 43px; display: flex; align-items: center; justify-content: space-between; gap: 18px; border-bottom: 1px solid rgba(199,196,192,.7); }.detail-list div.count { min-height: 49px; }.detail-list dt { color: var(--ink-muted); font-size: 12px; }.detail-list dd { margin: 0; color: var(--ink); font-size: 12px; font-weight: 650; white-space: nowrap; }.detail-list .count dd { color: var(--primary); font-family: 'Literata', Georgia, serif; font-size: 21px; }.detail-list .status dd { color: var(--success); }.detail-list dd.pending { color: var(--ink-muted); font-weight: 500; }
.modal-actions { display: flex; justify-content: flex-end; margin-top: 22px; }
@media (max-width: 1100px) { .article-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .library-toolbar { align-items: stretch; flex-wrap: wrap; }.search-field { flex-basis: 100%; }.language-filter { flex: 1; }.article-card { min-height: 218px; gap: 14px; padding: 16px; }.cover { width: 72px; flex-basis: 72px; }.article-meta div { align-items: flex-start; flex-direction: column; gap: 2px; }.article-meta dd { white-space: normal; }.article-detail-modal { padding: 24px; }.detail-list div { align-items: flex-start; flex-direction: column; gap: 3px; padding-block: 10px; }.detail-list dd { white-space: normal; } }
</style>
