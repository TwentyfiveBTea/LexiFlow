<script setup lang="ts">
import { FileText, Grid2X2, List, MoreVertical, RefreshCw, ScanSearch, Search, Trash2, X } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articles } from '@/data/demo'
import type { Article, ArticleProcessingDetail } from '@/data/demo'
import AppSelect from '@/components/AppSelect.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { analyzeArticle, deleteArticle, getArticleProcessingDetail, getArticleVocabs, getArticles } from '@/lib/api'

interface ArticleCard {
  articleId: string
  languageCode: 'en' | 'ja'
  title: string
  wordCount: number
  createdAt: string
  tone: Article['tone']
  processingDetail: ArticleProcessingDetail
}

const router = useRouter()
const query = ref('')
const view = ref<'grid' | 'list'>('grid')
const languageFilter = ref<'all' | 'en' | 'ja'>('all')
const languageOptions = [
  { value: 'all', label: '全部语言' },
  { value: 'en', label: '英语' },
  { value: 'ja', label: '日语' },
]
const selectedArticle = ref<ArticleCard | null>(null)
const analysisArticle = ref<ArticleCard | null>(null)
const analysisLevel = ref('CET4')
const analysisDialogOpen = ref(false)
const analysisLoading = ref(false)
const analysisError = ref('')
const analysisResult = ref<{ matchedWordCount: number; reused: boolean; analysisLevel: string } | null>(null)
const deletedArticleIds = ref<string[]>([])
const deleteError = ref('')
const deleteDialogOpen = ref(false)
const loading = ref(true)
const loadError = ref('')
const usingDemoData = ref(false)
const articleItems = ref<ArticleCard[]>(articles.map((article) => ({
  articleId: article.articleId,
  languageCode: article.languageCode,
  title: article.title,
  wordCount: article.wordCount,
  createdAt: article.createdAt,
  tone: article.tone,
  processingDetail: article.processingDetail,
})))
const filteredArticles = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  return articleItems.value.filter((article) => {
    const matchesDeleted = !deletedArticleIds.value.includes(article.articleId)
    const matchesKeyword = !keyword || article.title.toLowerCase().includes(keyword)
    const matchesLanguage = languageFilter.value === 'all' || article.languageCode === languageFilter.value
    return matchesDeleted && matchesKeyword && matchesLanguage
  })
})

async function loadArticles() {
  loading.value = true
  loadError.value = ''
  usingDemoData.value = false
  try {
    const result = await getArticles()
    const mapped: ArticleCard[] = await Promise.all(result.map(async (article, index) => {
      let processingDetail: ArticleProcessingDetail = {
        wordCount: article.wordCount,
        parseStatus: 0,
        translationStatus: 0,
        analysisStatus: 0,
        parsedAt: null,
        translatedAt: null,
        analyzedAt: null,
      }
      try {
        const detail = await getArticleProcessingDetail(article.articleId)
        processingDetail = { ...processingDetail, ...detail }
      } catch {
        // The article itself remains usable when processing metadata is unavailable.
      }
      return {
        ...article,
        languageCode: article.languageCode === 'ja' ? 'ja' as const : 'en' as const,
        tone: (['blue', 'clay', 'charcoal', 'sage'] as const)[index % 4],
        processingDetail,
      }
    }))
    articleItems.value = mapped
  } catch {
    if (import.meta.env.DEV) {
      usingDemoData.value = true
    } else {
      articleItems.value = []
      loadError.value = '文章列表加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

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

const englishAnalysisLevels = [
  { value: 'BEC', label: 'BEC', description: '商务英语' },
  { value: 'CET4', label: 'CET-4', description: '大学英语四级' },
  { value: 'CET6', label: 'CET-6', description: '大学英语六级' },
  { value: 'GMAT', label: 'GMAT', description: '商学院入学考试' },
  { value: 'GRE', label: 'GRE', description: '研究生入学考试' },
  { value: 'IELTS', label: 'IELTS', description: '雅思' },
  { value: 'LEVEL4', label: 'Level 4', description: '英语四级词表' },
  { value: 'LEVEL8', label: 'Level 8', description: '英语八级词表' },
  { value: 'SAT', label: 'SAT', description: '美国大学入学考试' },
  { value: 'TOEFL', label: 'TOEFL', description: '托福' },
  { value: 'CHUZHONG', label: '初中', description: '初中英语词表' },
  { value: 'GAOZHONG', label: '高中', description: '高中英语词表' },
  { value: 'KAOYAN', label: '考研', description: '考研英语词表' },
]
const japaneseAnalysisLevels = [
  { value: 'N5', label: 'N5', description: '日语入门' },
  { value: 'N4', label: 'N4', description: '基础日语' },
  { value: 'N3', label: 'N3', description: '日常阅读' },
  { value: 'N2', label: 'N2', description: '中高级阅读' },
  { value: 'N1', label: 'N1', description: '高级阅读' },
]
const analysisLevels = computed(() => analysisArticle.value?.languageCode === 'ja' ? japaneseAnalysisLevels : englishAnalysisLevels)

function openAnalysisDialog(article: ArticleCard) {
  analysisArticle.value = article
  analysisLevel.value = article.languageCode === 'ja' ? 'N3' : 'CET4'
  analysisError.value = ''
  analysisResult.value = null
  analysisDialogOpen.value = true
}

function closeAnalysisDialog() {
  if (analysisLoading.value) return
  analysisDialogOpen.value = false
  analysisArticle.value = null
}

async function confirmAnalysis() {
  if (!analysisArticle.value || analysisLoading.value) return

  analysisLoading.value = true
  analysisError.value = ''
  analysisResult.value = null
  try {
    const result = await analyzeArticle(analysisArticle.value.articleId, analysisLevel.value)
    if (result.reused || result.analysisStatus === 2) {
      analysisResult.value = {
        matchedWordCount: result.matchedWordCount,
        reused: result.reused,
        analysisLevel: result.analysisLevel,
      }
      return
    }

    await waitForAnalysis(result.articleId, result.analysisLevel)
  } catch (error) {
    analysisError.value = error instanceof Error ? error.message : '词汇解析失败，请稍后重试'
  } finally {
    analysisLoading.value = false
  }
}

async function waitForAnalysis(articleId: string, level: string) {
  for (let attempt = 0; attempt < 80; attempt += 1) {
    await new Promise((resolve) => window.setTimeout(resolve, 1500))
    const detail = await getArticleProcessingDetail(articleId)
    if (detail.analysisStatus === 3) {
      throw new Error('词汇解析失败，请稍后重试')
    }
    if (detail.analysisStatus === 2) {
      const vocabs = await getArticleVocabs(articleId, level)
      analysisResult.value = {
        matchedWordCount: vocabs.length,
        reused: false,
        analysisLevel: level,
      }
      return
    }
  }
  throw new Error('词汇解析仍在后台进行，请稍后查看文章详情')
}

function requestDeleteArticle() {
  if (!selectedArticle.value) return
  deleteError.value = ''
  deleteDialogOpen.value = true
}

async function confirmDeleteArticle() {
  const article = selectedArticle.value
  if (!article) return

  deleteDialogOpen.value = false
  deleteError.value = ''
  try {
    if (!usingDemoData.value) {
      await deleteArticle(article.articleId)
    }
    articleItems.value = articleItems.value.filter((item) => item.articleId !== article.articleId)
    selectedArticle.value = null
  } catch (error) {
    deleteError.value = error instanceof Error ? error.message : '删除文章失败'
  }
}

onMounted(() => { void loadArticles() })
</script>

<template>
  <main class="page">
    <header class="page-header fade-in">
      <div><p class="eyebrow">Repository · {{ articleItems.length }} texts<span v-if="usingDemoData"> · Demo</span></p><h1 class="page-title">文章库</h1><p class="page-description">整理所有阅读材料，回到任何一段仍值得推敲的文本</p></div>
    </header>

    <section class="library-toolbar surface fade-in">
      <label class="search-field"><Search :size="18" /><input v-model="query" placeholder="搜索标题" /></label>
      <AppSelect v-model="languageFilter" class="language-select" :options="languageOptions" label="按语言筛选文章" menu-width="132px" align="right" />
      <div class="view-switch" aria-label="视图切换">
        <button type="button" :class="{ active: view === 'grid' }" aria-label="网格视图" @click="view = 'grid'"><Grid2X2 :size="17" /></button>
        <button type="button" :class="{ active: view === 'list' }" aria-label="列表视图" @click="view = 'list'"><List :size="18" /></button>
      </div>
    </section>

    <section v-if="loading" class="library-state surface"><RefreshCw :size="20" class="spin" /><p>正在加载文章库...</p></section>
    <section v-else-if="loadError" class="library-state surface"><p>{{ loadError }}</p><button class="btn btn-secondary" type="button" @click="loadArticles">重新加载</button></section>
    <section v-else class="article-grid" :class="{ list: view === 'list' }">
      <article v-for="(article, index) in filteredArticles" :key="article.articleId" class="article-card surface fade-in" :style="{ animationDelay: `${index * 70}ms` }">
        <div class="card-actions">
          <button class="parse-button" type="button" :aria-label="`解析 ${article.title} 的词汇`" title="解析文章词汇" @click="openAnalysisDialog(article)"><ScanSearch :size="15" />解析</button>
          <button class="more icon-btn" type="button" :aria-label="`查看 ${article.title} 详细信息`" title="查看文章详细信息" @click="selectedArticle = article; deleteError = ''"><MoreVertical :size="18" /></button>
        </div>
        <button class="cover" :class="article.tone" @click="router.push(`/reader/${article.articleId}`)"><FileText :size="38" :stroke-width="1.2" /></button>
        <div class="article-copy">
          <span class="language-badge">{{ article.languageCode }}</span>
          <button class="title serif" :title="article.title" @click="router.push(`/reader/${article.articleId}`)">{{ article.title }}</button>
          <dl class="article-meta">
            <div><dt>文章主语言</dt><dd>{{ article.languageCode }}</dd></div>
            <div><dt>词数统计</dt><dd>{{ article.wordCount.toLocaleString() }}</dd></div>
            <div><dt>创建时间</dt><dd>{{ formatDate(article.createdAt) }}</dd></div>
          </dl>
        </div>
      </article>
    </section>

    <div v-if="!loading && !loadError && !filteredArticles.length" class="empty surface"><Search :size="26" /><h2 class="serif">没有找到文章</h2><p>换一个关键词或语言筛选试试</p></div>

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
          <p v-if="deleteError" class="delete-error">{{ deleteError }}</p>
          <div class="modal-actions"><button class="icon-btn danger-action" type="button" aria-label="删除文章" title="删除文章" @click="requestDeleteArticle"><Trash2 :size="17" /></button><button class="btn btn-primary" type="button" @click="selectedArticle = null">完成</button></div>
        </section>
      </div>
    </Transition>

    <Transition name="dialog">
      <div v-if="analysisDialogOpen && analysisArticle" class="modal-backdrop analysis-backdrop" @click.self="closeAnalysisDialog">
        <section class="dialog-panel analysis-modal surface" role="dialog" aria-modal="true" aria-labelledby="analysis-dialog-title">
          <div class="modal-heading">
            <div><p class="eyebrow">Vocabulary analysis</p><h2 id="analysis-dialog-title" class="serif">选择解析等级</h2><p>{{ analysisArticle.title }}</p></div>
            <button class="icon-btn" type="button" aria-label="关闭词汇解析弹窗" title="关闭" :disabled="analysisLoading" @click="closeAnalysisDialog"><X :size="18" /></button>
          </div>

          <div class="analysis-context"><ScanSearch :size="18" /><span>将按所选等级匹配文章中的目标词汇</span></div>
          <div class="level-grid" role="radiogroup" aria-label="词汇解析等级">
            <button v-for="level in analysisLevels" :key="level.value" class="level-option" :class="{ selected: analysisLevel === level.value }" type="button" role="radio" :aria-checked="analysisLevel === level.value" :disabled="analysisLoading" @click="analysisLevel = level.value">
              <span class="level-check" aria-hidden="true"></span><strong>{{ level.label }}</strong><small>{{ level.description }}</small>
            </button>
          </div>

          <p v-if="analysisError" class="analysis-message error" role="alert">{{ analysisError }}</p>
          <div v-if="analysisResult" class="analysis-result" :class="{ reused: analysisResult.reused }"><div class="analysis-result-copy"><strong>{{ analysisResult.reused ? '已经解析过该等级' : '解析完成' }}</strong><span>{{ analysisResult.matchedWordCount.toLocaleString() }} 个词汇已{{ analysisResult.reused ? '复用' : '完成解析' }}</span></div><small>{{ analysisResult.analysisLevel }} 词表</small></div>
          <div class="modal-actions analysis-actions">
            <button class="btn btn-secondary" type="button" :disabled="analysisLoading" @click="closeAnalysisDialog">{{ analysisResult ? '完成' : '取消' }}</button>
            <button v-if="!analysisResult" class="btn btn-primary" type="button" :disabled="analysisLoading" @click="confirmAnalysis"><ScanSearch :size="16" />{{ analysisLoading ? '解析中…' : '确认解析' }}</button>
          </div>
        </section>
      </div>
    </Transition>

    <ConfirmDialog
      :visible="deleteDialogOpen"
      title="删除文章"
      message="删除之后该文章不能恢复，确定要继续吗？"
      @close="deleteDialogOpen = false"
      @confirm="confirmDeleteArticle"
    />
  </main>
</template>

<style scoped>
.library-toolbar { position: relative; z-index: 20; min-height: 66px; display: flex; align-items: center; gap: 12px; padding: 10px 12px; margin-bottom: 28px; }
.search-field { height: 44px; flex: 1; display: flex; align-items: center; gap: 10px; padding: 0 13px; border: 1px solid transparent; border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); }
.search-field:focus-within { border-color: var(--primary); }
.search-field input { width: 100%; border: 0; outline: 0; background: transparent; }
.language-select { width: 132px; flex: 0 0 auto; }
.view-switch { height: 44px; display: flex; padding: 3px; border: 1px solid var(--outline); border-radius: 7px; }
.view-switch button { width: 36px; display: grid; place-items: center; border: 0; border-radius: 5px; color: var(--ink-muted); background: transparent; }
.view-switch button.active { color: var(--primary); background: var(--surface-high); }
.article-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 18px; }
.article-card { position: relative; display: flex; gap: 20px; min-height: 238px; padding: 22px; }
.card-actions { position: absolute; top: 10px; right: 9px; display: flex; align-items: center; gap: 2px; }
.more { position: static; }
.parse-button { min-height: 32px; display: inline-flex; align-items: center; gap: 5px; padding: 0 8px; border: 1px solid transparent; border-radius: 6px; color: var(--primary); background: transparent; font-size: 11px; font-weight: 700; }
.parse-button:hover, .parse-button:focus-visible { border-color: var(--primary); background: var(--primary-soft); outline: none; }
.cover { width: 108px; flex: 0 0 108px; display: grid; place-items: center; border: 0; border-radius: 5px; color: white; background: var(--primary); }
.cover.clay { background: var(--secondary); }.cover.charcoal { background: #454848; }.cover.sage { background: #597166; }
.article-copy { min-width: 0; flex: 1; display: flex; flex-direction: column; }
.language-badge { width: fit-content; min-height: 22px; display: inline-flex; align-items: center; padding: 0 7px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; text-transform: lowercase; }
.title { min-width: 0; max-width: 100%; display: -webkit-box; overflow: hidden; margin: 13px 30px 12px 0; padding: 0; border: 0; color: var(--ink); background: transparent; text-align: left; font-size: 20px; line-height: 1.35; font-weight: 600; overflow-wrap: anywhere; word-break: break-word; -webkit-box-orient: vertical; -webkit-line-clamp: 3; }
.title:hover { color: var(--primary); }
.article-meta { display: grid; gap: 7px; margin: auto 0 0; padding-top: 13px; border-top: 1px solid rgba(199,196,192,.65); }
.article-meta div { display: flex; align-items: center; justify-content: space-between; gap: 12px; }.article-meta dt { color: var(--ink-muted); font-size: 11px; }.article-meta dd { margin: 0; color: var(--ink); font-size: 11px; font-weight: 650; white-space: nowrap; }
.article-grid.list { grid-template-columns: 1fr; }
.article-grid.list .article-card { min-height: 210px; }
.article-grid.list .cover { width: 100px; flex-basis: 100px; }
.empty { padding: 60px 24px; text-align: center; color: var(--ink-muted); }
.empty h2 { margin: 14px 0 4px; color: var(--primary); }
.empty p { margin: 0; }
.library-state { min-height: 280px; display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 10px; color: var(--ink-muted); text-align: center; }.library-state p { margin: 0; font-size: 13px; }.library-state .btn { margin-top: 8px; }.spin { animation: spin 1s linear infinite; }@keyframes spin { to { transform: rotate(360deg); } }
.modal-backdrop { position: fixed; z-index: 60; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }
.article-detail-modal { width: min(100%, 540px); padding: 28px 30px; }
.modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 22px; }.modal-heading h2 { margin: 0; color: var(--primary); font-size: 26px; }.modal-heading > div > p:last-child { max-width: 410px; margin: 6px 0 0; overflow: hidden; color: var(--ink-muted); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.detail-list { margin: 0; border-top: 1px solid var(--outline); }.detail-list div { min-height: 43px; display: flex; align-items: center; justify-content: space-between; gap: 18px; border-bottom: 1px solid rgba(199,196,192,.7); }.detail-list div.count { min-height: 49px; }.detail-list dt { color: var(--ink-muted); font-size: 12px; }.detail-list dd { margin: 0; color: var(--ink); font-size: 12px; font-weight: 650; white-space: nowrap; }.detail-list .count dd { color: var(--primary); font-family: 'Literata', Georgia, serif; font-size: 21px; }.detail-list .status dd { color: var(--success); }.detail-list dd.pending { color: var(--ink-muted); font-weight: 500; }
.delete-error { margin: 14px 0 0; color: var(--error); font-size: 12px; }.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }.danger-action { margin-right: auto; color: var(--error); border-color: #d7b8b8; }.danger-action:hover { color: #8f3028; border-color: var(--error); background: #f8eaea; }
.analysis-backdrop { z-index: 65; }
.analysis-modal { width: min(100%, 620px); padding: 28px 30px; }
.analysis-context { display: flex; align-items: center; gap: 9px; padding: 12px 14px; margin-bottom: 18px; border: 1px solid var(--outline); border-radius: 7px; color: var(--primary); background: var(--primary-soft); font-size: 12px; }
.level-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; max-height: 300px; padding: 2px; overflow-y: auto; }
.level-option { min-height: 68px; display: grid; grid-template-columns: 14px 1fr; grid-template-rows: auto auto; align-content: center; column-gap: 8px; padding: 10px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink); background: var(--surface-lowest); text-align: left; transition: border-color .18s ease, background-color .18s ease, transform .18s ease; }
.level-option:hover:not(:disabled) { border-color: var(--primary); background: var(--surface-low); transform: translateY(-1px); }
.level-option.selected { border-color: var(--primary); background: var(--primary-soft); box-shadow: inset 0 0 0 1px var(--primary); }
.level-option:disabled { cursor: wait; opacity: .65; }
.level-check { grid-row: 1 / span 2; width: 12px; height: 12px; align-self: center; border: 1px solid var(--outline); border-radius: 50%; background: white; }
.level-option.selected .level-check { border: 3px solid var(--primary); }
.level-option strong { font-size: 12px; line-height: 16px; }.level-option small { color: var(--ink-muted); font-size: 10px; line-height: 15px; }
.analysis-message { margin: 14px 0 0; font-size: 12px; }.analysis-message.error { color: var(--error); }
.analysis-result { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 16px; margin-top: 16px; border: 1px solid #bcd4c2; border-radius: 7px; color: var(--success); background: #f2f8f3; }.analysis-result-copy { min-width: 0; display: grid; gap: 3px; }.analysis-result strong { font-size: 15px; }.analysis-result span { font-size: 13px; font-weight: 650; }.analysis-result small { flex: 0 0 auto; color: var(--ink-muted); font-size: 11px; }.analysis-result.reused { border-color: #d9c59c; color: var(--secondary); background: #fff9ed; }
.analysis-actions { margin-top: 20px; }
@media (max-width: 1100px) { .article-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) { .library-toolbar { align-items: stretch; flex-wrap: wrap; }.search-field { flex-basis: 100%; }.language-select { flex: 1; width: auto; }.article-card { min-height: 218px; gap: 14px; padding: 16px; }.cover { width: 72px; flex-basis: 72px; }.article-meta div { align-items: flex-start; flex-direction: column; gap: 2px; }.article-meta dd { white-space: normal; }.article-detail-modal, .analysis-modal { padding: 24px; }.detail-list div { align-items: flex-start; flex-direction: column; gap: 3px; padding-block: 10px; }.detail-list dd { white-space: normal; }.level-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }.card-actions { right: 7px; }.parse-button { padding-inline: 6px; } }
</style>
