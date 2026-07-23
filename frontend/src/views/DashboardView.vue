<script setup lang="ts">
import { ArrowRight, BookOpen, FileText, Upload } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articles } from '@/data/demo'
import { getRecentArticles, getTodayDueWordCount } from '@/lib/api'
import type { ArticleListResponse } from '@/lib/api'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const fileInput = ref<HTMLInputElement | null>(null)
const notice = ref('')
const dueWordCount = ref(24)
const recentArticles = ref<Array<Pick<ArticleListResponse, 'articleId' | 'title' | 'languageCode' | 'createdAt'>>>(
  articles.slice(0, 2).map((article) => ({
    articleId: article.articleId,
    title: article.title,
    languageCode: article.languageCode,
    createdAt: article.createdAt,
  })),
)
const weekday = computed(() => new Intl.DateTimeFormat('en-US', { weekday: 'long' }).format(new Date()).toUpperCase())

onMounted(async () => {
  const [recentResult, dueCountResult] = await Promise.allSettled([getRecentArticles(), getTodayDueWordCount()])
  if (recentResult.status === 'fulfilled') {
    recentArticles.value = recentResult.value.map((article) => ({
      articleId: article.articleId,
      title: article.title,
      languageCode: article.languageCode,
      createdAt: article.createdAt,
    }))
  } else if (!import.meta.env.DEV) {
    recentArticles.value = []
  }
  if (dueCountResult.status === 'fulfilled') {
    dueWordCount.value = dueCountResult.value
  } else if (!import.meta.env.DEV) {
    dueWordCount.value = 0
  }
})

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

function formatCreatedAt(value: string) {
  return dateFormatter.format(new Date(value))
}

function upload(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files?.[0]) notice.value = `已选择 ${target.files[0].name}`
}
</script>

<template>
  <main class="page dashboard-page">
    <header class="welcome fade-in">
      <p class="eyebrow">{{ weekday }} · DEEP WORK</p>
      <h1 class="page-title">欢迎回来，{{ session.userName }}</h1>
      <p class="page-description">点击下方开始今天的文章精读和外语学习</p>
    </header>

    <section class="dashboard-grid">
      <div class="primary-column">
        <article class="start-card surface fade-in">
          <div class="start-heading">
            <span class="section-icon"><BookOpen :size="21" /></span>
            <div><h2 class="serif">开始阅读</h2><p>上传外语文章，开启深度精读</p></div>
          </div>
          <button class="file-drop" @click="fileInput?.click()"><Upload :size="19" /><span>上传 PDF、DOCX、TXT 或 Markdown 文件</span></button>
          <input ref="fileInput" class="hidden" type="file" accept=".pdf,.doc,.docx,.txt,.md" @change="upload" />
          <p v-if="notice" class="notice">{{ notice }}</p>
        </article>

        <section class="recent-section fade-in">
          <div class="section-title"><div><p class="eyebrow">Continue reading</p><h2 class="serif">最近文章</h2></div><RouterLink to="/articles">查看图书馆<ArrowRight :size="15" /></RouterLink></div>
          <div class="article-list">
            <button v-for="article in recentArticles" :key="article.articleId" class="article-row surface" @click="router.push(`/reader/${article.articleId}`)">
              <div class="book-cover"><FileText :size="26" :stroke-width="1.35" /><span>{{ article.languageCode }}</span></div>
              <div class="article-main">
                <div class="article-meta"><span class="language-badge">{{ article.languageCode }}</span><span>创建时间 · {{ formatCreatedAt(article.createdAt) }}</span></div>
                <h3 class="serif">{{ article.title }}</h3>
              </div>
            </button>
          </div>
        </section>
      </div>

      <aside class="insights-column">
        <article class="review-card fade-in">
          <p class="eyebrow">Memory deck</p><h2 class="serif">{{ dueWordCount }} 个词待复习</h2>
          <RouterLink class="btn btn-primary" to="/review">开始复习<ArrowRight :size="16" /></RouterLink>
        </article>
      </aside>
    </section>
  </main>
</template>

<style scoped>
.welcome { margin-bottom: 42px; }
.dashboard-grid { display: grid; grid-template-columns: minmax(0, 1fr) 300px; gap: 30px; align-items: start; }
.primary-column { display: grid; gap: 38px; }
.start-card { padding: 28px; }
.start-heading { display: flex; gap: 14px; align-items: flex-start; }
.section-icon { width: 42px; height: 42px; display: grid; place-items: center; flex: 0 0 auto; color: var(--primary); background: var(--primary-soft); border-radius: 7px; }
.start-heading h2, .section-title h2 { margin: 0; color: var(--primary); font-size: 25px; }
.start-heading p { margin: 5px 0 0; color: var(--ink-muted); font-size: 14px; }
.file-drop { width: 100%; min-height: 48px; margin-top: 24px; display: flex; align-items: center; justify-content: center; gap: 8px; border: 1px dashed var(--outline); border-radius: 7px; color: var(--ink-muted); background: transparent; }
.file-drop span { font-size: 13px; }
.file-drop:hover { color: var(--primary); border-color: var(--primary); background: var(--surface-low); }
.hidden { display: none; }
.notice { margin: 12px 0 0; color: var(--success); font-size: 13px; }
.section-title { display: flex; align-items: end; justify-content: space-between; margin-bottom: 16px; }
.section-title .eyebrow { margin-bottom: 4px; }
.section-title a { display: flex; align-items: center; gap: 5px; color: var(--secondary); font-size: 13px; font-weight: 650; }
.article-list { display: grid; gap: 12px; }
.article-row { width: 100%; display: flex; gap: 16px; min-height: 116px; padding: 16px; text-align: left; transition: border-color .18s ease, transform .18s ease; }
.article-row:hover { border-color: var(--primary); transform: translateY(-1px); }
.book-cover { width: 64px; height: 84px; flex: 0 0 auto; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 7px; border-radius: 4px; color: white; background: var(--primary); }
.book-cover span { font-size: 10px; font-weight: 700; }
.book-cover.clay { background: var(--secondary); }.book-cover.charcoal { background: #454848; }.book-cover.sage { background: #597166; }
.article-main { min-width: 0; flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 8px; }
.article-meta { display: flex; align-items: center; gap: 8px; color: var(--ink-muted); font-size: 11px; }
.article-main h3 { margin: 0; color: var(--ink); font-size: 19px; line-height: 1.35; }
.language-badge { width: fit-content; min-height: 22px; display: inline-flex; align-items: center; padding: 0 7px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; text-transform: lowercase; }
.insights-column { position: sticky; top: 24px; display: grid; gap: 14px; }
.review-card { padding: 24px; border-radius: 8px; color: white; background: var(--primary); }
.review-card .eyebrow { color: #d3e1e4; }.review-card h2 { margin: 0; font-size: 24px; }
.review-card .btn { width: 100%; margin-top: 22px; color: var(--primary); background: white; }
@media (max-width: 1050px) { .dashboard-grid { grid-template-columns: 1fr; } .insights-column { position: static; grid-template-columns: 1fr; } }
@media (max-width: 720px) { .article-row { gap: 14px; min-height: 104px; }.book-cover { width: 58px; height: 72px; }.article-main h3 { font-size: 17px; }.article-meta { flex-wrap: wrap; gap: 6px; }.insights-column { grid-template-columns: 1fr; } }
</style>
