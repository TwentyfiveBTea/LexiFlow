<script setup lang="ts">
import { ArrowLeft, BookmarkPlus, Check, ChevronRight, Filter, PanelRightClose, PanelRightOpen, Plus, X } from 'lucide-vue-next'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppSelect from '@/components/AppSelect.vue'
import BrandMark from '@/components/BrandMark.vue'
import { articles, collections, words } from '@/data/demo'
import { usePreferencesStore } from '@/stores/preferences'
import {
  addArticleVocabToLibrary,
  createVocabLibrary,
  getArticleDetail,
  getArticleVocabLevels,
  getArticleVocabLevelOccurrences,
  getArticleVocabs,
  getVocabLibraries,
} from '@/lib/api'
import type { ArticleDetailResponse, ArticleVocabOccurrenceResponse, ArticleVocabResponse, VocabLibraryResponse } from '@/lib/api'

interface TranslationItem {
  type: string
  translation: string
}

interface TextSegment {
  text: string
  vocab: ArticleVocabResponse | null
  occurrenceId: string | null
}

interface ContentBlock {
  source: string
  translation: string | null
  sourceStartOffset: number
}

const route = useRoute()
const router = useRouter()
const preferences = usePreferencesStore()
const progress = ref(0)
const panelOpen = ref(true)
const loading = ref(true)
const vocabLoading = ref(false)
const error = ref('')
const usingDemoData = ref(false)
const article = ref<ArticleDetailResponse | null>(null)
const articleVocabs = ref<ArticleVocabResponse[]>([])
const availableLevels = ref<string[]>([])
const selectedLevel = ref('')
const libraries = ref<VocabLibraryResponse[]>([])
const expandedVocabId = ref<string | null>(null)
const occurrences = ref<ArticleVocabOccurrenceResponse[]>([])
const articleOccurrences = ref<ArticleVocabOccurrenceResponse[]>([])
const libraryPickerFor = ref<string | null>(null)
const addedLibraryNames = ref<Record<string, string>>({})
const addMessages = ref<Record<string, string>>({})
const createLibraryOpen = ref(false)
const createLibraryName = ref('')
const createLibraryDescription = ref('')
const createLibraryError = ref('')
const createLibrarySubmitting = ref(false)
let panelScrollTimer: ReturnType<typeof window.setTimeout> | null = null

const articleId = computed(() => String(route.params.id ?? ''))
const levelOptions = computed(() => availableLevels.value.map((level) => ({ value: level, label: level })))
const contentBlocks = computed(() => article.value
  ? splitContentBlocks(article.value.parsedContent, article.value.translationStatus === 2)
  : [])
const languageLabel = computed(() => article.value?.languageCode === 'ja' ? '日语 · JA' : '英语 · EN')
const compatibleLibraries = computed(() => libraries.value.filter((library) => library.languageCode === article.value?.languageCode))
const articleLanguageCode = computed(() => article.value?.languageCode === 'ja' ? 'ja' : 'en')
const articleLanguageLabel = computed(() => articleLanguageCode.value === 'ja' ? '日语' : '英语')
const readingMinutes = computed(() => Math.max(1, Math.ceil((article.value?.wordCount ?? 0) / 250)))
const readerStyle = computed<Record<string, string>>(() => ({
  '--reader-font-family': preferences.readingFont === 'Georgia' ? 'Georgia, serif' : preferences.readingFont === '宋体' ? '"Songti SC", SimSun, serif' : "'Literata', Georgia, serif",
  '--reader-font-size': `${preferences.fontSize}px`,
  '--reader-line-height': preferences.lineHeight,
}))

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false,
})

function formatDate(value: string) {
  return dateFormatter.format(new Date(value))
}

function parseTranslations(value: string | null): TranslationItem[] {
  if (!value) return []
  try {
    const items = JSON.parse(value) as Array<Record<string, string>>
    return items
      .map((item) => ({ type: item.type?.trim() ?? '', translation: (item.translation || item.meaning || '').trim() }))
      .filter((item) => item.translation)
  } catch {
    return [{ type: '', translation: value }]
  }
}

function translationPreview(value: string | null) {
  return parseTranslations(value).map((item) => item.translation).join('；') || '暂无释义'
}

function formatPhonetic(value: string | null) {
  const normalized = value?.trim() ?? ''
  if (!normalized) return ''
  return normalized.startsWith('[') && normalized.endsWith(']') ? normalized : `[${normalized}]`
}

function splitContentBlocks(content: string, translated: boolean): ContentBlock[] {
  const result: ContentBlock[] = []
  const separator = /\n[ \t]*\n/g
  let blockStart = 0
  let match: RegExpExecArray | null
  while ((match = separator.exec(content)) !== null) {
    addContentBlock(result, content, blockStart, match.index, translated)
    blockStart = separator.lastIndex
  }
  addContentBlock(result, content, blockStart, content.length, translated)
  return result
}

function addContentBlock(result: ContentBlock[], content: string, blockStart: number, blockEnd: number, translated: boolean) {
  let sourceStart = blockStart
  let sourceEnd = blockEnd
  let translation: string | null = null
  if (translated) {
    const lineBreak = content.indexOf('\n', blockStart)
    if (lineBreak >= blockStart && lineBreak < blockEnd) {
      sourceEnd = lineBreak
      translation = content.slice(lineBreak + 1, blockEnd).trim() || null
    }
  }
  while (sourceStart < sourceEnd && /\s/.test(content[sourceStart]!)) sourceStart += 1
  while (sourceEnd > sourceStart && /\s/.test(content[sourceEnd - 1]!)) sourceEnd -= 1
  if (sourceStart < sourceEnd) {
    result.push({ source: content.slice(sourceStart, sourceEnd), translation, sourceStartOffset: sourceStart })
  }
}

function segmentText(text: string, absoluteStart: number): TextSegment[] {
  if (!text || !articleVocabs.value.length || !articleOccurrences.value.length) {
    return [{ text, vocab: null, occurrenceId: null }]
  }
  const vocabularyById = new Map(articleVocabs.value.map((vocab) => [vocab.articleVocabId, vocab]))
  const absoluteEnd = absoluteStart + text.length
  const relevantOccurrences = articleOccurrences.value
    .filter((occurrence) => occurrence.startOffset >= absoluteStart && occurrence.endOffset <= absoluteEnd)
    .sort((left, right) => left.startOffset - right.startOffset || right.endOffset - left.endOffset)
  const segments: TextSegment[] = []
  let cursor = 0
  for (const occurrence of relevantOccurrences) {
    const vocab = vocabularyById.get(occurrence.articleVocabId)
    const start = occurrence.startOffset - absoluteStart
    const end = occurrence.endOffset - absoluteStart
    if (!vocab || start < cursor || start < 0 || end > text.length || end <= start) continue
    if (start > cursor) segments.push({ text: text.slice(cursor, start), vocab: null, occurrenceId: null })
    segments.push({ text: text.slice(start, end), vocab, occurrenceId: occurrence.occurrenceId })
    cursor = end
  }
  if (cursor < text.length) segments.push({ text: text.slice(cursor), vocab: null, occurrenceId: null })
  return segments.length ? segments : [{ text, vocab: null, occurrenceId: null }]
}

function updateProgress() {
  const height = document.documentElement.scrollHeight - window.innerHeight
  progress.value = height > 0 ? Math.min(100, window.scrollY / height * 100) : 0
}

async function selectArticleWord(vocab: ArticleVocabResponse) {
  expandedVocabId.value = vocab.articleVocabId
  libraryPickerFor.value = null
  panelOpen.value = true
  occurrences.value = articleOccurrences.value.filter((occurrence) => occurrence.articleVocabId === vocab.articleVocabId)
  await nextTick()
  if (panelScrollTimer) window.clearTimeout(panelScrollTimer)
  requestAnimationFrame(() => {
    scrollPanelToWord(vocab.articleVocabId)
    panelScrollTimer = window.setTimeout(() => {
      if (expandedVocabId.value === vocab.articleVocabId) scrollPanelToWord(vocab.articleVocabId)
    }, 220)
  })
}

function scrollPanelToWord(articleVocabId: string) {
  const panel = document.querySelector<HTMLElement>('.vocab-panel')
  const panelItem = Array.from(document.querySelectorAll<HTMLElement>('.vocab-panel .word-item[data-vocab-id]'))
    .find((element) => element.dataset.vocabId === articleVocabId)
  if (!panel || !panelItem) return

  const panelRect = panel.getBoundingClientRect()
  const panelHead = panel.querySelector<HTMLElement>('.panel-head')
  const panelItemRect = panelItem.getBoundingClientRect()
  const visibleTop = (panelHead?.getBoundingClientRect().bottom ?? panelRect.top) - panelRect.top
  const targetTop = panel.scrollTop + panelItemRect.top - panelRect.top - visibleTop
  panel.scrollTo({ top: Math.max(targetTop, 0), behavior: 'auto' })
}

async function togglePanelWord(vocab: ArticleVocabResponse) {
  expandedVocabId.value = expandedVocabId.value === vocab.articleVocabId ? null : vocab.articleVocabId
  libraryPickerFor.value = null
  panelOpen.value = true
  occurrences.value = []
  if (expandedVocabId.value) {
    occurrences.value = articleOccurrences.value.filter((occurrence) => occurrence.articleVocabId === vocab.articleVocabId)
    await nextTick()
    const articleBody = document.querySelector<HTMLElement>('.article-body')
    const firstOccurrence = Array.from(articleBody?.querySelectorAll<HTMLElement>('[data-vocab-id]') ?? [])
      .find((element) => element.dataset.vocabId === vocab.articleVocabId)
    firstOccurrence?.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' })
  }
}

function openLibraryPicker(vocab: ArticleVocabResponse) {
  libraryPickerFor.value = libraryPickerFor.value === vocab.articleVocabId ? null : vocab.articleVocabId
  addMessages.value[vocab.articleVocabId] = ''
}

function openCreateLibrary() {
  createLibraryError.value = ''
  createLibraryName.value = ''
  createLibraryDescription.value = ''
  libraryPickerFor.value = null
  createLibraryOpen.value = true
}

function closeCreateLibrary() {
  if (createLibrarySubmitting.value) return
  createLibraryOpen.value = false
}

async function createLibrary() {
  const name = createLibraryName.value.trim()
  if (!name || !article.value) {
    createLibraryError.value = '请输入词汇库名称'
    return
  }

  createLibrarySubmitting.value = true
  createLibraryError.value = ''
  const languageCode = articleLanguageCode.value
  try {
    let created: VocabLibraryResponse
    if (usingDemoData.value) {
      const now = new Date().toISOString()
      created = {
        libraryId: `demo-library-${Date.now()}`,
        name,
        languageCode,
        description: createLibraryDescription.value.trim() || null,
        wordCount: 0,
        createdAt: now,
        updatedAt: now,
      }
    } else {
      created = await createVocabLibrary({
        name,
        languageCode,
        description: createLibraryDescription.value.trim(),
      })
    }
    libraries.value = [...libraries.value, created]
    createLibraryOpen.value = false
  } catch (error) {
    createLibraryError.value = error instanceof Error ? error.message : '创建词汇库失败，请稍后重试'
  } finally {
    createLibrarySubmitting.value = false
  }
}

async function addToLibrary(vocab: ArticleVocabResponse, library: VocabLibraryResponse) {
  try {
    if (!usingDemoData.value) await addArticleVocabToLibrary(library.libraryId, articleId.value, vocab.articleVocabId)
    libraries.value = libraries.value.map((each) => each.libraryId === library.libraryId
      ? { ...each, wordCount: each.wordCount + 1, updatedAt: new Date().toISOString() }
      : each)
    addedLibraryNames.value[vocab.articleVocabId] = library.name
    addMessages.value[vocab.articleVocabId] = `已加入“${library.name}”`
    libraryPickerFor.value = null
  } catch {
    addMessages.value[vocab.articleVocabId] = '加入失败，请稍后重试'
  }
}

function buildDemoDetail(): ArticleDetailResponse {
  const demoArticle = articles.find((item) => item.articleId === articleId.value) ?? articles[0]!
  const parsedContent = [
    demoArticle.excerpt,
    'The rapid evolution of generative models has precipitated a profound legal crisis. We are no longer dealing with mere automation; we are navigating the murky waters of algorithmic sentience, or at least the convincing simulacrum thereof. The traditional paradigms of intellectual property and tort law are proving grossly inadequate when confronted with systems capable of novel creation and autonomous decision-making.',
    'Consider the case of a decentralized autonomous organization governed by a neural network. If this entity executes a smart contract that results in catastrophic financial loss for a third party, establishing culpability becomes an exercise in futile obfuscation. Is the original programmer liable? The nodes executing the code? Or does the algorithm itself possess a nascent form of legal personhood?',
    'Scholars argue that to impute liability to a non-biological entity requires a radical restructuring of jurisprudence. Yet the societal demand for accountability cannot be ignored, while an overly broad response may stifle innovation.',
  ].join('\n\n')
  return {
    articleId: demoArticle.articleId,
    title: demoArticle.title,
    parsedContent,
    languageCode: demoArticle.languageCode,
    translationStatus: 0,
    wordCount: demoArticle.wordCount,
    charCount: parsedContent.length,
    createdAt: demoArticle.createdAt,
  }
}

function demoVocabs(level: string): ArticleVocabResponse[] {
  return words.filter((word) => word.languageCode === article.value?.languageCode && word.level === level).map((word) => ({
    articleVocabId: `demo-${level}-${word.wordId}`,
    wordId: word.wordId,
    languageCode: word.languageCode,
    baseWord: word.word,
    matchedForms: JSON.stringify([word.word]),
    occurrenceCount: 1,
    firstMatchedText: word.word,
    firstSentence: null,
    translations: word.translations,
    us: word.us || null,
    uk: word.uk || null,
    kana: word.kana || null,
  }))
}

function useDemoReader() {
  usingDemoData.value = true
  article.value = buildDemoDetail()
  availableLevels.value = [...new Set(words.filter((word) => word.languageCode === article.value?.languageCode).map((word) => word.level))]
  libraries.value = collections.filter((library) => library.languageCode === article.value?.languageCode).map((library) => ({
    libraryId: library.libraryId,
    name: library.name,
    languageCode: library.languageCode,
    description: library.description,
    wordCount: library.wordCount,
    createdAt: library.createdAt,
    updatedAt: library.updatedAt,
  }))
  selectedLevel.value = availableLevels.value[0] ?? ''
}

async function loadVocabs(level: string) {
  expandedVocabId.value = null
  libraryPickerFor.value = null
  if (!level) {
    articleVocabs.value = []
    articleOccurrences.value = []
    return
  }
  vocabLoading.value = true
  try {
    if (usingDemoData.value) {
      articleVocabs.value = demoVocabs(level)
      articleOccurrences.value = []
    } else {
      const [vocabs, levelOccurrences] = await Promise.all([
        getArticleVocabs(articleId.value, level),
        getArticleVocabLevelOccurrences(articleId.value, level),
      ])
      articleVocabs.value = vocabs
      articleOccurrences.value = levelOccurrences
    }
  } catch {
    articleVocabs.value = []
    articleOccurrences.value = []
  } finally {
    vocabLoading.value = false
  }
}

async function loadReader() {
  loading.value = true
  error.value = ''
  usingDemoData.value = false
  try {
    article.value = await getArticleDetail(articleId.value)
    const [levels, vocabLibraries] = await Promise.all([
      getArticleVocabLevels(articleId.value),
      getVocabLibraries({ languageCode: article.value.languageCode }),
    ])
    availableLevels.value = levels
    libraries.value = vocabLibraries.filter((library) => library.languageCode === article.value?.languageCode)
    selectedLevel.value = levels[0] ?? ''
  } catch {
    if (import.meta.env.DEV) useDemoReader()
    else error.value = '文章详情加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

watch(selectedLevel, (level) => { void loadVocabs(level) })
onMounted(() => {
  void loadReader()
  updateProgress()
  window.addEventListener('scroll', updateProgress, { passive: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateProgress)
  if (panelScrollTimer) window.clearTimeout(panelScrollTimer)
})
</script>

<template>
  <div class="reader" :class="{ 'panel-closed': !panelOpen, 'marking-disabled': !preferences.highlight }" :style="readerStyle">
    <div class="reading-progress" :style="{ width: `${progress}%` }"></div>
    <header class="reader-nav">
      <button class="icon-btn" aria-label="返回文章库" @click="router.push('/articles')"><ArrowLeft :size="19" /></button>
      <BrandMark />
      <button class="icon-btn" :aria-label="panelOpen ? '收起词汇侧栏' : '打开词汇侧栏'" @click="panelOpen = !panelOpen"><PanelRightClose v-if="panelOpen" :size="20" /><PanelRightOpen v-else :size="20" /></button>
    </header>

    <main class="reading-canvas">
      <section v-if="loading" class="reader-state"><span class="loading-line"></span><p>正在加载文章...</p></section>
      <section v-else-if="error" class="reader-state"><p>{{ error }}</p><button class="btn btn-secondary" type="button" @click="loadReader">重新加载</button></section>
      <article v-else-if="article" class="article-body">
        <header class="article-header fade-in">
          <p class="article-kicker">{{ languageLabel }}</p>
          <h1 class="serif">{{ article.title }}</h1>
          <dl class="article-meta">
            <div><dt>词数</dt><dd>{{ article.wordCount.toLocaleString() }}</dd></div>
            <div><dt>字符数</dt><dd>{{ article.charCount.toLocaleString() }}</dd></div>
            <div><dt>大致阅读时间</dt><dd>{{ readingMinutes }} 分钟</dd></div>
            <div><dt>创建时间</dt><dd>{{ formatDate(article.createdAt) }}</dd></div>
          </dl>
        </header>

        <div class="prose serif">
          <section v-for="(block, blockIndex) in contentBlocks" :key="blockIndex" class="content-block">
            <p class="source-paragraph">
              <template v-for="(segment, segmentIndex) in segmentText(block.source, block.sourceStartOffset)" :key="segment.occurrenceId ?? `plain-${segmentIndex}`">
                <button v-if="segment.vocab" class="word" :class="{ active: expandedVocabId === segment.vocab.articleVocabId }" :data-vocab-id="segment.vocab.articleVocabId" :data-occurrence-id="segment.occurrenceId" @click="selectArticleWord(segment.vocab)">{{ segment.text }}</button>
                <template v-else>{{ segment.text }}</template>
              </template>
            </p>
            <p v-if="block.translation" class="translation-paragraph">{{ block.translation }}</p>
          </section>
        </div>
      </article>

      <aside v-if="panelOpen" class="vocab-panel">
        <div class="panel-head">
          <div><p class="eyebrow">已解析词汇</p><h2 class="serif">生词解析</h2></div>
          <div class="panel-actions">
            <AppSelect v-if="levelOptions.length" v-model="selectedLevel" class="level-select" :options="levelOptions" label="筛选词汇等级" :icon="Filter" :columns="2" menu-width="210px" align="right" />
            <button class="icon-btn" aria-label="收起侧栏" @click="panelOpen = false"><PanelRightClose :size="19" /></button>
          </div>
        </div>

        <div v-if="vocabLoading" class="panel-state">正在加载词汇...</div>
        <div v-else-if="!levelOptions.length" class="panel-state">这篇文章还没有已解析的词汇等级</div>
        <div v-else-if="!articleVocabs.length" class="panel-state">当前等级没有命中词汇</div>
        <div v-else class="word-index">
          <article v-for="vocab in articleVocabs" :key="vocab.articleVocabId" class="word-item" :class="{ active: expandedVocabId === vocab.articleVocabId }" :data-vocab-id="vocab.articleVocabId">
            <button class="word-row" type="button" :aria-expanded="expandedVocabId === vocab.articleVocabId" @click="togglePanelWord(vocab)">
              <span><strong class="serif">{{ vocab.baseWord }}</strong><small>{{ translationPreview(vocab.translations) }}</small></span>
              <ChevronRight :size="16" />
            </button>
            <Transition name="word-detail">
              <div v-if="expandedVocabId === vocab.articleVocabId" class="word-detail">
                <h3 class="serif">{{ vocab.baseWord }}</h3>
                <div v-if="vocab.kana || vocab.us || vocab.uk" class="pronunciations">
                  <span v-if="vocab.kana">{{ vocab.kana }}</span>
                  <span v-if="vocab.us">US {{ formatPhonetic(vocab.us) }}</span>
                  <span v-if="vocab.uk">UK {{ formatPhonetic(vocab.uk) }}</span>
                </div>
                <div class="translations">
                  <div v-for="(item, index) in parseTranslations(vocab.translations)" :key="`${item.type}-${index}`"><span v-if="item.type">{{ item.type }}</span><strong>{{ item.translation }}</strong></div>
                  <p v-if="!parseTranslations(vocab.translations).length">暂无释义</p>
                </div>
                <div v-if="occurrences.length" class="occurrence-list"><small>原文出现位置</small><p v-for="occurrence in occurrences.slice(0, 3)" :key="occurrence.occurrenceId">{{ occurrence.sentence }}</p></div>
                <div class="add-area">
                  <button class="save-word" type="button" :class="{ saved: addedLibraryNames[vocab.articleVocabId] }" @click.stop="openLibraryPicker(vocab)">
                    <Check v-if="addedLibraryNames[vocab.articleVocabId]" :size="16" /><BookmarkPlus v-else :size="16" />
                    {{ addedLibraryNames[vocab.articleVocabId] ? `已加入 · ${addedLibraryNames[vocab.articleVocabId]}` : '加入词库' }}
                  </button>
                  <Transition name="library-picker">
                    <div v-if="libraryPickerFor === vocab.articleVocabId" class="library-picker surface">
                      <div class="library-picker-head">
                        <strong>选择{{ vocab.languageCode === 'ja' ? '日语' : '英语' }}词汇库</strong>
                        <button class="create-library-trigger" type="button" aria-haspopup="dialog" @click.stop.prevent="openCreateLibrary"><Plus :size="14" />新建</button>
                      </div>
                      <button v-for="library in compatibleLibraries" :key="library.libraryId" type="button" @click.stop="addToLibrary(vocab, library)"><span>{{ library.name }}</span><small>{{ library.wordCount.toLocaleString() }} 词</small></button>
                      <p v-if="!compatibleLibraries.length">暂无同语言词汇库</p>
                    </div>
                  </Transition>
                </div>
                <p v-if="addMessages[vocab.articleVocabId]" class="add-message">{{ addMessages[vocab.articleVocabId] }}</p>
              </div>
            </Transition>
          </article>
        </div>
      </aside>
    </main>

    <Teleport to="body">
      <Transition name="dialog">
        <div v-if="createLibraryOpen" class="modal-backdrop reader-create-backdrop" @click.self="closeCreateLibrary">
          <form class="dialog-panel reader-create-modal surface" role="dialog" aria-modal="true" aria-labelledby="reader-create-library-title" @submit.prevent="createLibrary">
            <div class="modal-heading"><div><p class="eyebrow">New collection</p><h2 id="reader-create-library-title" class="serif">创建词汇库</h2></div><button class="icon-btn" type="button" aria-label="关闭创建词汇库弹窗" @click="closeCreateLibrary"><X :size="18" /></button></div>
            <label class="field-label" for="reader-library-name">词汇库名称</label>
            <input id="reader-library-name" v-model="createLibraryName" class="field" maxlength="128" required autofocus />
            <span class="field-label">语言标识</span>
            <div class="fixed-language"><strong>{{ articleLanguageCode }}</strong><span>{{ articleLanguageLabel }}</span></div>
            <div class="description-label"><label class="field-label" for="reader-library-description">词汇库描述</label><span>{{ createLibraryDescription.length }} / 500</span></div>
            <textarea id="reader-library-description" v-model="createLibraryDescription" class="field description-field" maxlength="500"></textarea>
            <p v-if="createLibraryError" class="form-message" role="alert">{{ createLibraryError }}</p>
            <div class="modal-actions"><button class="btn btn-secondary" type="button" :disabled="createLibrarySubmitting" @click="closeCreateLibrary">取消</button><button class="btn btn-primary" type="submit" :disabled="createLibrarySubmitting">{{ createLibrarySubmitting ? '创建中…' : '创建' }}</button></div>
          </form>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.reader { --panel: 360px; min-height: 100vh; background: var(--surface); }.reader.panel-closed { --panel: 0px; }.reading-progress { position: fixed; z-index: 80; left: 0; top: 0; height: 3px; background: var(--secondary); }
.reader-nav { position: fixed; z-index: 45; left: 0; right: var(--panel); top: 0; height: 66px; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; border-bottom: 1px solid rgba(199,196,192,.75); background: rgba(252,249,248,.94); backdrop-filter: blur(12px); transition: right .22s ease; }.reader-nav :deep(.brand-copy small) { display: none; }
.reading-canvas { padding-right: var(--panel); transition: padding-right .22s ease; }.article-body { width: min(100%, 820px); margin-inline: auto; padding: 126px 42px 120px; }
.article-header { margin-bottom: 50px; }.article-kicker { margin: 0 0 16px; color: var(--secondary); font-size: 11px; font-weight: 700; text-transform: uppercase; }.article-header h1 { max-width: 100%; margin: 0; color: var(--ink); font-size: 49px; line-height: 1.18; font-weight: 650; overflow-wrap: anywhere; word-break: break-word; }.article-meta { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0; padding: 19px 0; margin: 30px 0 0; border-top: 1px solid var(--outline); border-bottom: 1px solid var(--outline); }.article-meta div { min-width: 0; padding: 0 16px; border-left: 1px solid var(--outline); }.article-meta div:first-child { padding-left: 0; border-left: 0; }.article-meta dt { color: var(--ink-muted); font-size: 10px; }.article-meta dd { overflow: hidden; margin: 4px 0 0; color: var(--ink); font-size: 12px; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.prose { color: #282a29; font-size: 18.5px; line-height: 1.78; }.reader .prose { font-family: var(--reader-font-family); font-size: var(--reader-font-size); line-height: var(--reader-line-height); }.content-block { margin: 0 0 30px; }.prose .source-paragraph { margin: 0; white-space: pre-wrap; }.prose .translation-paragraph { margin: 10px 0 0; padding-left: 16px; border-left: 2px solid var(--outline); color: var(--ink-muted); font-size: .9em; line-height: 1.72; white-space: pre-wrap; }.word { display: inline; padding: 0 1px; border: 0; border-bottom: 1px solid transparent; border-radius: 1px; color: inherit; background: transparent; font: inherit; line-height: inherit; cursor: pointer; }.word:hover, .word.active { border-bottom-color: var(--secondary); background: var(--secondary-soft); }.marking-disabled .word:not(.active), .marking-disabled .word:not(.active):hover { border-bottom-color: transparent; background: transparent; }
.vocab-panel { position: fixed; z-index: 50; inset: 0 0 0 auto; width: var(--panel); padding: 0 20px 24px; overflow-y: auto; border-left: 1px solid var(--outline); background: var(--surface-lowest); box-shadow: -8px 0 28px rgba(45,45,45,.04); }.panel-head { position: sticky; z-index: 8; top: 0; display: flex; align-items: center; justify-content: space-between; gap: 12px; min-height: 96px; padding: 20px 0 14px; border-bottom: 1px solid var(--outline); background: var(--surface-lowest); }.panel-head .eyebrow { margin-bottom: 2px; }.panel-head h2 { margin: 0; color: var(--primary); font-size: 23px; }.panel-actions { display: flex; align-items: center; gap: 6px; }.level-select { width: 134px; height: 40px; }.panel-state { padding: 38px 12px; color: var(--ink-muted); font-size: 12px; line-height: 1.6; text-align: center; }
.word-index { display: grid; }.word-item { border-bottom: 1px solid rgba(199,196,192,.7); }.word-row { width: 100%; min-height: 62px; display: flex; align-items: center; gap: 10px; padding: 9px 8px; border: 0; color: var(--ink-muted); background: transparent; text-align: left; }.word-row:hover, .word-item.active > .word-row { color: var(--primary); background: var(--surface-low); }.word-row > span { min-width: 0; flex: 1; display: grid; gap: 3px; }.word-row strong { overflow: hidden; color: var(--ink); font-size: 16px; text-overflow: ellipsis; white-space: nowrap; }.word-row small { overflow: hidden; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }.word-row > svg { flex: 0 0 auto; transition: transform .18s ease; }.word-item.active > .word-row > svg { transform: rotate(90deg); }
.occurrence-list { display: grid; gap: 6px; padding: 10px; margin-top: 14px; border-left: 2px solid var(--secondary-soft); background: var(--surface-low); }.occurrence-list small { color: var(--secondary); font-size: 10px; font-weight: 750; }.occurrence-list p { margin: 0; color: var(--ink-muted); font-family: 'Literata', Georgia, serif; font-size: 11px; line-height: 1.55; }.occurrence-state { margin-top: 14px; color: var(--ink-muted); font-size: 11px; }
.word-detail { padding: 8px 10px 16px; background: var(--surface-low); }.word-detail h3 { margin: 0; color: var(--primary); font-size: 22px; }.pronunciations { display: flex; flex-wrap: wrap; gap: 5px 12px; margin-top: 8px; color: var(--ink-muted); font-size: 11px; }.translations { display: grid; gap: 7px; padding-top: 13px; margin-top: 13px; border-top: 1px solid var(--outline); }.translations > div { display: grid; grid-template-columns: minmax(28px, auto) 1fr; align-items: baseline; gap: 8px; }.translations span { color: var(--secondary); font-size: 10.5px; font-weight: 750; }.translations strong { color: var(--ink); font-size: 12px; line-height: 1.5; font-weight: 600; }.translations p { margin: 0; color: var(--ink-muted); font-size: 12px; }
.add-area { position: relative; }.save-word { width: 100%; min-height: 38px; display: flex; align-items: center; justify-content: center; gap: 7px; margin-top: 16px; border: 1px solid var(--outline); border-radius: 6px; color: var(--primary); background: var(--surface-lowest); font-size: 12px; font-weight: 700; }.save-word:hover { border-color: var(--primary); }.save-word.saved { color: var(--success); border-color: var(--success); }.library-picker { position: absolute; z-index: 12; left: 0; right: 0; top: calc(100% + 7px); display: grid; gap: 3px; padding: 8px; box-shadow: 0 14px 34px rgba(45,45,45,.14), 0 2px 8px rgba(45,45,45,.06); }.library-picker > strong { padding: 6px 8px 7px; color: var(--ink-muted); font-size: 10px; }.library-picker button { min-height: 38px; display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 0 9px; border: 0; border-radius: 5px; color: var(--ink); background: transparent; text-align: left; }.library-picker button:hover { background: var(--surface-low); }.library-picker button span { overflow: hidden; font-size: 12px; font-weight: 650; text-overflow: ellipsis; white-space: nowrap; }.library-picker button small { flex: 0 0 auto; color: var(--ink-muted); font-size: 10px; }.library-picker p { margin: 8px; color: var(--ink-muted); font-size: 11px; }.add-message { margin: 9px 0 0; color: var(--success); font-size: 10.5px; text-align: center; }
.library-picker-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 3px 0 6px 8px; }.library-picker-head > strong { min-width: 0; overflow: hidden; padding: 0; color: var(--ink-muted); font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }.library-picker .create-library-trigger { min-height: 30px; flex: 0 0 auto; justify-content: center; gap: 5px; padding: 0 8px; border: 1px solid var(--outline); border-radius: 5px; color: var(--primary); font-size: 11px; font-weight: 650; }.library-picker .create-library-trigger:hover { border-color: var(--primary); background: var(--primary-soft); }
.reader-create-backdrop { position: fixed; z-index: 70; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }.reader-create-modal { width: min(100%, 480px); padding: 30px; }.reader-create-modal .modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; margin-bottom: 23px; }.reader-create-modal .modal-heading h2 { margin: 0; color: var(--primary); font-size: 26px; }.reader-create-modal > .field-label { display: block; margin-top: 16px; }.reader-create-modal .field { width: 100%; background: white; font-size: 13px; }.fixed-language { height: 46px; display: flex; align-items: center; justify-content: center; gap: 8px; padding: 3px; border: 1px solid var(--outline); border-radius: 7px; color: var(--primary); background: var(--surface-low); }.fixed-language strong { font-size: 13px; text-transform: lowercase; }.fixed-language span { font-size: 11px; }.reader-create-modal .description-label { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; }.reader-create-modal .description-label .field-label { margin: 0 0 7px; }.reader-create-modal .description-label span { color: var(--ink-muted); font-size: 10px; }.reader-create-modal .description-field { min-height: 96px; padding-block: 11px; resize: vertical; line-height: 1.55; }.reader-create-modal .form-message { margin: 14px 0 0; color: var(--error); font-size: 12px; }.reader-create-modal .modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }
.reader-state { min-height: 100vh; display: flex; align-items: center; justify-content: center; flex-direction: column; gap: 10px; color: var(--ink-muted); font-size: 13px; }.loading-line { width: 96px; height: 2px; position: relative; overflow: hidden; background: var(--surface-high); }.loading-line::after { position: absolute; inset: 0; content: ''; background: var(--primary); animation: loading 1s ease-in-out infinite; }@keyframes loading { 0% { transform: translateX(-100%) scaleX(.35); } 55% { transform: translateX(35%) scaleX(.65); } 100% { transform: translateX(110%) scaleX(.3); } }
.word-detail-enter-active { transition: opacity .18s ease-out, transform .2s cubic-bezier(.22,1,.36,1); }.word-detail-leave-active { transition: opacity .12s ease-in, transform .14s ease-in; }.word-detail-enter-from, .word-detail-leave-to { opacity: 0; transform: translateY(-4px); }.library-picker-enter-active { transition: opacity .15s ease-out, transform .18s ease-out; }.library-picker-leave-active { transition: opacity .1s ease-in; }.library-picker-enter-from, .library-picker-leave-to { opacity: 0; transform: translateY(-4px); }
@media (max-width: 1050px) { .reader { --panel: 326px; }.article-body { padding-inline: 30px; }.article-header h1 { font-size: 40px; }.article-meta { grid-template-columns: repeat(2, 1fr); }.article-meta div { padding: 10px 14px; }.article-meta div:nth-child(3) { border-left: 0; }.prose { font-size: 18px; } }
@media (max-width: 820px) { .reader { --panel: 0px; }.reading-canvas { padding-right: 0; }.reader-nav { right: 0; }.vocab-panel { width: min(92vw, 360px); box-shadow: -12px 0 40px rgba(0,0,0,.16); }.article-body { padding: 104px 22px 90px; }.article-header h1 { font-size: 35px; }.prose { font-size: 18px; }.reader-nav { padding-inline: 14px; }.reader-nav :deep(.brand-copy) { display: none; } }
@media (max-width: 520px) { .article-meta { grid-template-columns: 1fr 1fr; }.article-meta div { padding-inline: 10px; }.article-meta dd { font-size: 11px; }.panel-head { align-items: flex-start; }.panel-actions { align-items: flex-start; }.level-select { width: 124px; } }
</style>
