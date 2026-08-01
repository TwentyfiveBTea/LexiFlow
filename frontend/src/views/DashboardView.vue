<script setup lang="ts">
import { ArrowRight, BookOpen, CircleAlert, CircleCheck, FileText, LoaderCircle, Upload, X } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articles } from '@/data/demo'
import { getRecentArticles, getTodayDueWordCount, uploadArticle } from '@/lib/api'
import type { ArticleListResponse } from '@/lib/api'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const fileInput = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadFeedback = ref<'closed' | 'uploading' | 'success' | 'error'>('closed')
const uploadMessage = ref('')
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
const uploadDialogTitle = computed(() => {
  if (uploadFeedback.value === 'uploading') return '正在提交文章'
  if (uploadFeedback.value === 'success') return '已进入解析队列'
  return '文章提交失败'
})
const maxUploadFileSize = 50 * 1024 * 1024

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

function closeUploadFeedback() {
  if (uploading.value) return
  uploadFeedback.value = 'closed'
  uploadMessage.value = ''
  uploadProgress.value = 0
}

function showUploadError(message: string) {
  uploadMessage.value = message
  uploadFeedback.value = 'error'
}

async function upload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file || uploading.value) return
  if (file.size > maxUploadFileSize) {
    showUploadError('文件大小不能超过50MB')
    target.value = ''
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  uploadMessage.value = `正在上传“${file.name}”，完成后将自动进入后台解析`
  uploadFeedback.value = 'uploading'
  try {
    const result = await uploadArticle(file, (percent) => { uploadProgress.value = percent })
    uploadMessage.value = `“${result.title}”已上传，系统正在后台解析，可稍后前往图书馆查看进度`
    uploadFeedback.value = 'success'
    recentArticles.value = [{
      articleId: result.articleId,
      title: result.title,
      languageCode: result.languageCode === 'ja' ? 'ja' as const : 'en' as const,
      createdAt: new Date().toISOString(),
    }, ...recentArticles.value].slice(0, 2)
  } catch (error) {
    showUploadError(error instanceof Error ? error.message : '文章上传失败，请稍后重试')
  } finally {
    uploading.value = false
    target.value = ''
  }
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
            <div class="start-copy">
              <h2 class="serif">开始阅读</h2>
              <p>上传外语文章，开启深度精读<small>内测阶段，仅支持英语和日语</small></p>
            </div>
          </div>
          <button class="file-drop" :disabled="uploading" :aria-busy="uploading" @click="fileInput?.click()">
            <Upload :size="20" />
            <span class="upload-copy"><strong>选择文章文件</strong><small>PDF · DOCX · TXT · Markdown · HTML</small></span>
          </button>
          <input ref="fileInput" class="hidden" type="file" accept=".pdf,.doc,.docx,.txt,.md,.html,.htm" @change="upload" />
        </article>

        <section class="recent-section fade-in">
          <div class="section-title"><div><p class="eyebrow">Continue reading</p><h2 class="serif">最近文章</h2></div><RouterLink to="/articles">查看图书馆<ArrowRight :size="15" /></RouterLink></div>
          <div class="article-list">
            <button v-for="article in recentArticles" :key="article.articleId" class="article-row surface" @click="router.push(`/reader/${article.articleId}`)">
              <div class="book-cover"><FileText :size="26" :stroke-width="1.35" /><span>{{ article.languageCode }}</span></div>
              <div class="article-main">
                <div class="article-meta"><span class="language-badge">{{ article.languageCode }}</span><span>创建时间 · {{ formatCreatedAt(article.createdAt) }}</span></div>
                <h3 class="serif" :title="article.title">{{ article.title }}</h3>
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

    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="uploadFeedback !== 'closed'" class="upload-modal-backdrop" @click.self="closeUploadFeedback">
          <section
            class="dialog-panel upload-modal surface"
            :role="uploadFeedback === 'error' ? 'alertdialog' : 'dialog'"
            aria-modal="true"
            aria-labelledby="upload-dialog-title"
            aria-describedby="upload-dialog-message"
          >
            <button v-if="!uploading" class="icon-btn upload-modal-close" type="button" aria-label="关闭上传提示" title="关闭" @click="closeUploadFeedback"><X :size="18" /></button>
            <span class="upload-state-icon" :class="uploadFeedback" aria-hidden="true">
              <LoaderCircle v-if="uploadFeedback === 'uploading'" :size="27" class="spin" />
              <CircleCheck v-else-if="uploadFeedback === 'success'" :size="28" />
              <CircleAlert v-else :size="28" />
            </span>
            <p class="eyebrow">Article processing</p>
            <h2 id="upload-dialog-title" class="serif">{{ uploadDialogTitle }}</h2>
            <p id="upload-dialog-message" class="upload-modal-message" aria-live="polite">{{ uploadMessage }}</p>
            <div v-if="uploading" class="upload-modal-progress" role="progressbar" :aria-valuenow="uploadProgress" aria-valuemin="0" aria-valuemax="100">
              <div><span :style="{ width: `${uploadProgress}%` }"></span></div>
              <strong>{{ uploadProgress }}%</strong>
            </div>
            <button v-else class="btn btn-primary upload-modal-action" type="button" @click="closeUploadFeedback">知道了</button>
          </section>
        </div>
      </Transition>
    </Teleport>
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
.start-copy { min-width: 0; }
.start-heading p { margin: 5px 0 0; color: var(--ink-muted); font-size: 14px; line-height: 1.55; }
.start-heading p small { display: block; margin-top: 2px; color: #747979; font-size: 11px; line-height: 1.5; }
.file-drop { width: 100%; min-height: 58px; margin-top: 22px; display: flex; align-items: center; justify-content: center; gap: 11px; padding: 10px 16px; border: 1px dashed var(--outline); border-radius: 7px; color: var(--ink-muted); background: transparent; }
.upload-copy { min-width: 0; display: flex; flex-direction: column; align-items: flex-start; gap: 2px; text-align: left; }
.upload-copy strong { color: var(--primary); font-size: 13px; font-weight: 700; }
.upload-copy small { font-size: 10px; line-height: 1.35; }
.file-drop:hover { color: var(--primary); border-color: var(--primary); background: var(--surface-low); }
.file-drop:disabled { cursor: wait; opacity: .7; }
.hidden { display: none; }
.upload-modal-backdrop { position: fixed; z-index: 90; inset: 0; display: grid; place-items: center; padding: 20px; background: rgba(27,28,28,.38); }
.upload-modal { position: relative; width: min(100%, 400px); padding: 32px; text-align: center; }
.upload-modal-close { position: absolute; top: 14px; right: 14px; }
.upload-state-icon { width: 54px; height: 54px; display: grid; place-items: center; margin: 0 auto 18px; border-radius: 50%; color: var(--primary); background: var(--primary-soft); }
.upload-state-icon.success { color: var(--success); background: #e5efe9; }
.upload-state-icon.error { color: var(--error); background: #f7e5e2; }
.upload-modal .eyebrow { margin-bottom: 6px; }
.upload-modal h2 { margin: 0; color: var(--primary); font-size: 25px; }
.upload-modal-message { margin: 12px auto 0; color: var(--ink-muted); font-size: 13px; line-height: 1.7; overflow-wrap: anywhere; }
.upload-modal-progress { display: grid; grid-template-columns: minmax(0, 1fr) 38px; align-items: center; gap: 12px; margin-top: 24px; }
.upload-modal-progress > div { height: 5px; overflow: hidden; border-radius: 5px; background: var(--surface-high); }
.upload-modal-progress span { display: block; height: 100%; border-radius: inherit; background: var(--primary); transition: width .18s ease; }
.upload-modal-progress strong { color: var(--primary); font-size: 12px; text-align: right; }
.upload-modal-action { width: 100%; margin-top: 24px; }
.spin { animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
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
.article-main h3 { min-width: 0; max-width: 100%; display: -webkit-box; overflow: hidden; margin: 0; color: var(--ink); font-size: 19px; line-height: 1.35; overflow-wrap: anywhere; word-break: break-word; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.language-badge { width: fit-content; min-height: 22px; display: inline-flex; align-items: center; padding: 0 7px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; text-transform: lowercase; }
.insights-column { position: sticky; top: 24px; display: grid; gap: 14px; }
.review-card { padding: 24px; border-radius: 8px; color: white; background: var(--primary); }
.review-card .eyebrow { color: #d3e1e4; }.review-card h2 { margin: 0; font-size: 24px; }
.review-card .btn { width: 100%; margin-top: 22px; color: var(--primary); background: white; }
@media (max-width: 1050px) { .dashboard-grid { grid-template-columns: 1fr; } .insights-column { position: static; grid-template-columns: 1fr; } }
@media (max-width: 720px) { .welcome { margin-bottom: 32px; }.primary-column { gap: 32px; }.start-card { padding: 20px; }.start-heading { gap: 12px; }.section-icon { width: 38px; height: 38px; }.start-heading h2 { font-size: 22px; }.start-heading p { margin-top: 3px; font-size: 13px; }.file-drop { min-height: 60px; margin-top: 18px; }.article-row { gap: 14px; min-height: 104px; }.book-cover { width: 58px; height: 72px; }.article-main h3 { font-size: 17px; }.article-meta { flex-wrap: wrap; gap: 6px; }.insights-column { grid-template-columns: 1fr; }.upload-modal { padding: 28px 22px 22px; }.upload-modal h2 { font-size: 22px; } }
</style>
