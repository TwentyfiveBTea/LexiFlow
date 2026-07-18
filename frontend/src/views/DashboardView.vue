<script setup lang="ts">
import { ArrowRight, BookOpen, Clock3, FileText, Link2, Upload, Zap } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { articles } from '@/data/demo'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const articleUrl = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const notice = ref('')

function processUrl() {
  notice.value = articleUrl.value ? '链接已加入处理队列。' : '请先粘贴文章链接。'
}

function upload(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files?.[0]) notice.value = `已选择 ${target.files[0].name}`
}
</script>

<template>
  <main class="page dashboard-page">
    <header class="welcome fade-in">
      <p class="eyebrow">Saturday · Deep work</p>
      <h1 class="page-title">欢迎回来，{{ session.userName }}。</h1>
      <p class="page-description">记忆卡牌中还有 24 个基于昨日阅读内容的新单词等待复习。</p>
    </header>

    <section class="dashboard-grid">
      <div class="primary-column">
        <article class="start-card surface fade-in">
          <div class="start-heading">
            <span class="section-icon"><BookOpen :size="21" /></span>
            <div><h2 class="serif">开始阅读</h2><p>粘贴外语刊物链接或上传文档，开启深度精读。</p></div>
          </div>
          <div class="url-row">
            <label class="url-field"><Link2 :size="18" /><input v-model="articleUrl" type="url" placeholder="https://example.com/article" @keyup.enter="processUrl" /></label>
            <button class="btn btn-primary" @click="processUrl">处理文本<ArrowRight :size="17" /></button>
          </div>
          <button class="file-drop" @click="fileInput?.click()"><Upload :size="19" /><span>上传 PDF、DOCX、TXT 或 Markdown 文件</span></button>
          <input ref="fileInput" class="hidden" type="file" accept=".pdf,.doc,.docx,.txt,.md" @change="upload" />
          <p v-if="notice" class="notice">{{ notice }}</p>
        </article>

        <section class="recent-section fade-in">
          <div class="section-title"><div><p class="eyebrow">Continue reading</p><h2 class="serif">最近文章</h2></div><RouterLink to="/articles">查看图书馆<ArrowRight :size="15" /></RouterLink></div>
          <div class="article-list">
            <button v-for="article in articles.slice(0, 2)" :key="article.id" class="article-row surface" @click="router.push(`/reader/${article.id}`)">
              <div class="book-cover" :class="article.tone"><FileText :size="28" :stroke-width="1.35" /><span>{{ article.language }}</span></div>
              <div class="article-main">
                <div class="article-meta"><span class="badge">{{ article.language }}</span><span>{{ article.source }} · {{ article.date }}</span></div>
                <h3 class="serif">{{ article.title }}</h3>
                <p>{{ article.excerpt }}</p>
                <div class="article-progress"><span>采摘 {{ article.words }} 词</span><div class="progress"><span :style="{ width: `${article.progress}%` }"></span></div><small>{{ article.progress }}%</small></div>
              </div>
            </button>
          </div>
        </section>
      </div>

      <aside class="insights-column">
        <article class="metric surface fade-in"><span class="metric-icon"><Clock3 :size="21" /></span><div><small>本周阅读</small><strong class="serif">4h 32m</strong><p>较上周增加 18%</p></div></article>
        <article class="metric surface fade-in"><span class="metric-icon clay"><Zap :size="21" /></span><div><small>连续学习</small><strong class="serif">12 天</strong><p>保持稳定的阅读节奏</p></div></article>
        <article class="review-card fade-in">
          <p class="eyebrow">Memory deck</p><h2 class="serif">24 个词待复习</h2><p>今天的卡牌来自最近三篇阅读材料。</p>
          <RouterLink class="btn btn-primary" to="/vocabulary/core">开始复习<ArrowRight :size="16" /></RouterLink>
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
.url-row { display: flex; gap: 10px; margin-top: 24px; }
.url-field { min-height: 44px; flex: 1; display: flex; align-items: center; gap: 10px; padding: 0 13px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }
.url-field:focus-within { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(50,78,88,.1); }
.url-field input { width: 100%; border: 0; outline: none; background: transparent; }
.file-drop { width: 100%; min-height: 48px; margin-top: 12px; display: flex; align-items: center; justify-content: center; gap: 8px; border: 1px dashed var(--outline); border-radius: 7px; color: var(--ink-muted); background: transparent; }
.file-drop:hover { color: var(--primary); border-color: var(--primary); background: var(--surface-low); }
.hidden { display: none; }
.notice { margin: 12px 0 0; color: var(--success); font-size: 13px; }
.section-title { display: flex; align-items: end; justify-content: space-between; margin-bottom: 16px; }
.section-title .eyebrow { margin-bottom: 4px; }
.section-title a { display: flex; align-items: center; gap: 5px; color: var(--secondary); font-size: 13px; font-weight: 650; }
.article-list { display: grid; gap: 12px; }
.article-row { width: 100%; display: flex; gap: 20px; padding: 18px; text-align: left; transition: border-color .18s ease, transform .18s ease; }
.article-row:hover { border-color: var(--primary); transform: translateY(-1px); }
.book-cover { width: 76px; height: 104px; flex: 0 0 auto; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; border-radius: 4px; color: white; background: var(--primary); }
.book-cover span { font-size: 10px; font-weight: 700; }
.book-cover.clay { background: var(--secondary); }.book-cover.charcoal { background: #454848; }.book-cover.sage { background: #597166; }
.article-main { min-width: 0; flex: 1; }
.article-meta { display: flex; align-items: center; gap: 8px; color: var(--ink-muted); font-size: 11px; }
.article-main h3 { margin: 8px 0 5px; color: var(--ink); font-size: 19px; line-height: 1.35; }
.article-main > p { margin: 0; overflow: hidden; color: var(--ink-muted); font-size: 13px; line-height: 1.55; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.article-progress { margin-top: 13px; display: grid; grid-template-columns: auto 1fr auto; gap: 10px; align-items: center; color: var(--secondary); font-size: 11px; font-weight: 650; }
.article-progress small { color: var(--ink-muted); }
.insights-column { position: sticky; top: 24px; display: grid; gap: 14px; }
.metric { display: flex; gap: 14px; padding: 20px; }
.metric-icon { width: 40px; height: 40px; display: grid; place-items: center; color: var(--primary); background: var(--primary-soft); border-radius: 7px; }
.metric-icon.clay { color: var(--secondary); background: #f4e5d6; }
.metric small { display: block; color: var(--ink-muted); font-size: 11px; font-weight: 650; text-transform: uppercase; }
.metric strong { display: block; margin-top: 4px; color: var(--primary); font-size: 26px; }
.metric p { margin: 2px 0 0; color: var(--ink-muted); font-size: 11px; }
.review-card { padding: 24px; border-radius: 8px; color: white; background: var(--primary); }
.review-card .eyebrow { color: #d3e1e4; }.review-card h2 { margin: 0; font-size: 24px; }.review-card p:not(.eyebrow) { margin: 10px 0 22px; color: #d3e1e4; font-size: 13px; line-height: 1.55; }
.review-card .btn { width: 100%; color: var(--primary); background: white; }
@media (max-width: 1050px) { .dashboard-grid { grid-template-columns: 1fr; } .insights-column { position: static; grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 720px) { .url-row { flex-direction: column; }.article-row { gap: 14px; }.book-cover { width: 58px; height: 82px; }.article-main > p { display: none; }.article-progress { grid-template-columns: auto 1fr; }.article-progress small { display: none; }.insights-column { grid-template-columns: 1fr; } }
</style>
