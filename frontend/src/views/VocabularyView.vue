<script setup lang="ts">
import { BookOpen, GraduationCap, MoreVertical, Plus, Search, TrendingUp, X } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { collections } from '@/data/demo'
import type { VocabularyCollection } from '@/data/demo'

const router = useRouter()
const query = ref('')
const showCreate = ref(false)
const newName = ref('')
const newLanguage = ref<'en' | 'ja'>('en')
const newDescription = ref('')
const selectedCollection = ref<VocabularyCollection | null>(null)
const createdCollections = ref<VocabularyCollection[]>([])
const allCollections = computed(() => [...collections, ...createdCollections.value])
const filtered = computed(() => {
  const keyword = query.value.trim().toLowerCase()
  if (!keyword) return allCollections.value
  return allCollections.value.filter((item) => `${item.name}${item.languageCode}${item.description}`.toLowerCase().includes(keyword))
})
const statisticsItems = computed(() => {
  const statistics = selectedCollection.value?.statistics
  if (!statistics) return []
  return [
    { key: 'total', label: '单词总数', value: statistics.totalCount },
    { key: 'mastered', label: '已掌握', value: statistics.masteredCount },
    { key: 'new', label: '未学习', value: statistics.newCount },
    { key: 'learning', label: '学习中', value: statistics.learningCount },
    { key: 'due', label: '待复习', value: statistics.dueCount },
  ]
})
const chartSegments = computed(() => {
  const total = statisticsItems.value.reduce((sum, item) => sum + item.value, 0)
  return statisticsItems.value.map((item) => ({
    ...item,
    percentage: total ? item.value / total * 100 : 0,
  }))
})

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
})

function formatDate(value: string) {
  return dateFormatter.format(new Date(value))
}

function openCreate() {
  showCreate.value = true
}

function closeCreate() {
  showCreate.value = false
}

function createCollection() {
  const name = newName.value.trim()
  if (!name) return

  const now = new Date().toISOString()
  createdCollections.value.push({
    libraryId: `custom-${Date.now()}`,
    name,
    languageCode: newLanguage.value,
    description: newDescription.value.trim(),
    wordCount: 0,
    createdAt: now,
    updatedAt: now,
    tone: newLanguage.value === 'ja' ? 'sage' : 'blue',
    statistics: { totalCount: 0, newCount: 0, learningCount: 0, masteredCount: 0, dueCount: 0 },
  })
  newName.value = ''
  newLanguage.value = 'en'
  newDescription.value = ''
  closeCreate()
}

function openStatistics(collection: VocabularyCollection) {
  selectedCollection.value = collection
}
</script>

<template>
  <main class="page">
    <header class="page-header fade-in">
      <div><p class="eyebrow">Repository · Collections</p><h1 class="page-title">词汇库</h1><p class="page-description">分类管理词汇资源，建立深度阅读与学术研究的知识枢纽。</p></div>
      <div class="header-actions"><label class="compact-search"><Search :size="17" /><input v-model="query" placeholder="搜索词库" /></label><button class="btn btn-primary" @click="openCreate"><Plus :size="17" />新建词库</button></div>
    </header>

    <section class="collection-grid">
      <article
        v-for="(collection, index) in filtered"
        :key="collection.libraryId"
        class="collection-card surface fade-in"
        :style="{ animationDelay: `${index * 75}ms` }"
        @click="router.push(`/vocabulary/${collection.libraryId}`)"
      >
        <div class="collection-top">
          <span class="collection-cover" :class="collection.tone"><GraduationCap v-if="collection.tone === 'blue'" :size="28" /><TrendingUp v-else-if="collection.tone === 'clay'" :size="28" /><BookOpen v-else :size="28" /></span>
          <div class="collection-actions"><span class="language-badge">{{ collection.languageCode }}</span><button class="icon-btn" :aria-label="`查看 ${collection.name} 统计`" title="查看学习统计" @click.stop="openStatistics(collection)"><MoreVertical :size="19" /></button></div>
        </div>
        <div class="collection-heading"><h2 class="serif">{{ collection.name }}</h2></div>
        <p class="collection-description">{{ collection.description || '暂无词汇库描述。' }}</p>
        <dl class="collection-meta">
          <div><dt>正常词条</dt><dd>{{ collection.wordCount.toLocaleString() }}</dd></div>
          <div><dt>创建时间</dt><dd>{{ formatDate(collection.createdAt) }}</dd></div>
          <div><dt>更新时间</dt><dd>{{ formatDate(collection.updatedAt) }}</dd></div>
        </dl>
      </article>

      <button class="create-card fade-in" @click="openCreate"><span><Plus :size="25" /></span><strong class="serif">创建新词库</strong><small>建立英语或日语词汇库，整理专属学习资料。</small></button>
    </section>

    <div v-if="showCreate" class="modal-backdrop" @click.self="closeCreate">
      <form class="create-modal surface" role="dialog" aria-modal="true" aria-labelledby="create-library-title" @submit.prevent="createCollection">
        <div class="modal-heading"><div><p class="eyebrow">New collection</p><h2 id="create-library-title" class="serif">创建词汇库</h2></div><button class="icon-btn" type="button" aria-label="关闭创建词库弹窗" @click="closeCreate"><X :size="18" /></button></div>

        <label class="field-label" for="collection-name">词汇库名称</label>
        <input id="collection-name" v-model="newName" class="field" autofocus maxlength="128" required />

        <span id="collection-language-label" class="field-label">语言标识</span>
        <div class="language-segmented" role="radiogroup" aria-labelledby="collection-language-label">
          <button type="button" role="radio" :aria-checked="newLanguage === 'en'" :class="{ active: newLanguage === 'en' }" @click="newLanguage = 'en'"><strong>en</strong><span>英语</span></button>
          <button type="button" role="radio" :aria-checked="newLanguage === 'ja'" :class="{ active: newLanguage === 'ja' }" @click="newLanguage = 'ja'"><strong>ja</strong><span>日语</span></button>
        </div>

        <div class="description-label"><label class="field-label" for="collection-description">词汇库描述</label><span>{{ newDescription.length }} / 500</span></div>
        <textarea id="collection-description" v-model="newDescription" class="field description-field" maxlength="500"></textarea>

        <div class="modal-actions"><button class="btn btn-secondary" type="button" @click="closeCreate">取消</button><button class="btn btn-primary" type="submit">创建</button></div>
      </form>
    </div>

    <div v-if="selectedCollection" class="modal-backdrop" @click.self="selectedCollection = null">
      <section class="statistics-modal surface" role="dialog" aria-modal="true" aria-labelledby="statistics-title">
        <div class="modal-heading"><div><p class="eyebrow">Learning statistics</p><h2 id="statistics-title" class="serif">词库学习统计</h2><p>{{ selectedCollection.name }}</p></div><button class="icon-btn" type="button" aria-label="关闭学习统计弹窗" @click="selectedCollection = null"><X :size="18" /></button></div>
        <dl class="statistics-list">
          <div v-for="item in statisticsItems" :key="item.key" :class="{ total: item.key === 'total' }"><dt>{{ item.label }}</dt><dd class="serif">{{ item.value.toLocaleString() }}</dd></div>
        </dl>
        <div class="statistics-chart">
          <p>学习数据占比</p>
          <div class="distribution-bar" role="img" :aria-label="chartSegments.map((item) => `${item.label} ${item.value}`).join('，')"><span v-for="item in chartSegments" :key="item.key" :class="`segment-${item.key}`" :style="{ width: `${item.percentage}%` }"></span></div>
          <div class="distribution-legend">
            <div v-for="item in chartSegments" :key="item.key"><span><i :class="`legend-${item.key}`"></i>{{ item.label }}</span><strong>{{ item.value.toLocaleString() }} · {{ item.percentage.toFixed(1) }}%</strong></div>
          </div>
        </div>
        <div class="modal-actions"><button class="btn btn-primary" type="button" @click="selectedCollection = null">完成</button></div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.header-actions { display: flex; gap: 10px; }
.compact-search { width: 230px; height: 42px; display: flex; align-items: center; gap: 8px; padding: 0 12px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); background: white; }
.compact-search:focus-within { border-color: var(--primary); }.compact-search input { min-width: 0; flex: 1; border: 0; outline: 0; background: transparent; }
.collection-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; }
.collection-card { min-height: 342px; display: flex; flex-direction: column; padding: 24px; cursor: pointer; transition: border-color .2s ease, transform .2s ease; }
.collection-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.collection-top { display: flex; justify-content: space-between; align-items: flex-start; }
.collection-actions { display: flex; align-items: center; gap: 4px; }
.collection-cover { width: 60px; height: 68px; display: grid; place-items: center; border-radius: 4px; color: white; background: var(--primary); box-shadow: inset -7px 0 rgba(255,255,255,.09); }
.collection-cover.clay { background: var(--secondary); }.collection-cover.charcoal { background: #454848; }.collection-cover.sage { background: #597166; }
.collection-heading { display: flex; align-items: center; gap: 9px; margin-top: 18px; }
.collection-heading h2 { min-width: 0; margin: 0; overflow: hidden; color: var(--ink); font-size: 21px; text-overflow: ellipsis; white-space: nowrap; }
.language-badge { flex: 0 0 auto; padding: 3px 6px; border-radius: 4px; color: var(--secondary); background: var(--secondary-soft); font-size: 10px; font-weight: 750; text-transform: lowercase; }
.collection-description { min-height: 40px; margin: 9px 0 0; display: -webkit-box; overflow: hidden; color: var(--ink-muted); font-size: 12px; line-height: 1.65; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.collection-meta { display: grid; gap: 8px; margin: auto 0 0; padding-top: 16px; border-top: 1px solid rgba(199,196,192,.65); }
.collection-meta div { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.collection-meta dt { color: var(--ink-muted); font-size: 11px; }.collection-meta dd { margin: 0; color: var(--ink); font-size: 11px; font-weight: 650; white-space: nowrap; }
.create-card { min-height: 342px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; border: 1px dashed var(--outline); border-radius: 8px; color: var(--ink-muted); background: transparent; }
.create-card:hover { color: var(--primary); border-color: var(--primary); background: var(--surface-low); }
.create-card > span { width: 54px; height: 54px; display: grid; place-items: center; border-radius: 50%; background: var(--surface-high); }
.create-card strong { margin-top: 16px; font-size: 19px; }.create-card small { max-width: 220px; margin-top: 7px; line-height: 1.5; }
.modal-backdrop { position: fixed; z-index: 60; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }
.create-modal { width: min(100%, 480px); padding: 30px; }.statistics-modal { width: min(100%, 520px); padding: 28px 30px; }
.modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }
.modal-heading h2 { margin: 0; color: var(--primary); font-size: 26px; }.modal-heading > div > p:last-child { margin: 6px 0 0; color: var(--ink-muted); font-size: 13px; }
.create-modal .modal-heading { margin-bottom: 23px; }.create-modal > .field-label { margin-top: 16px; }
.create-modal .field { background: white; }
.language-segmented { height: 46px; display: grid; grid-template-columns: 1fr 1fr; gap: 3px; padding: 3px; border: 1px solid var(--outline); border-radius: 7px; background: var(--surface-low); }
.language-segmented button { min-width: 0; display: flex; align-items: center; justify-content: center; gap: 7px; border: 0; border-radius: 5px; color: var(--ink-muted); background: transparent; }
.language-segmented button:hover { color: var(--primary); }.language-segmented button:focus-visible { outline: 2px solid var(--primary); outline-offset: -2px; }.language-segmented button.active { color: var(--primary); background: white; box-shadow: 0 1px 4px rgba(45,45,45,.11); }
.language-segmented strong { font-size: 13px; text-transform: lowercase; }.language-segmented span { font-size: 11px; }
.description-label { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; }.description-label .field-label { margin-bottom: 7px; }.description-label span { color: var(--ink-muted); font-size: 10px; }
.description-field { min-height: 96px; padding-block: 11px; resize: vertical; line-height: 1.55; }
.statistics-modal .modal-heading { margin-bottom: 22px; }
.statistics-list { margin: 0; border-top: 1px solid var(--outline); }
.statistics-list div { min-height: 43px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid rgba(199,196,192,.7); }
.statistics-list div.total { min-height: 49px; }.statistics-list dt { color: var(--ink-muted); font-size: 12px; }.statistics-list dd { margin: 0; color: var(--primary); font-size: 18px; font-weight: 650; }.statistics-list .total dd { font-size: 22px; }
.statistics-chart { margin-top: 18px; }.statistics-chart > p { margin: 0 0 9px; color: var(--ink-muted); font-size: 11px; font-weight: 700; }
.distribution-bar { width: 100%; height: 9px; display: flex; overflow: hidden; border-radius: 3px; background: var(--surface-high); }
.distribution-bar span { height: 100%; }.distribution-bar span + span { box-shadow: inset 1px 0 rgba(255,255,255,.9); }.segment-total, .legend-total { background: #203f4a; }.segment-mastered, .legend-mastered { background: #3f7758; }.segment-new, .legend-new { background: #ad4f3f; }.segment-learning, .legend-learning { background: #3676a3; }.segment-due, .legend-due { background: #d09a2f; }
.distribution-legend { display: grid; grid-template-columns: 1fr 1fr; gap: 7px 18px; margin-top: 12px; }
.distribution-legend div { min-width: 0; display: flex; align-items: center; justify-content: space-between; gap: 8px; color: var(--ink-muted); font-size: 10px; }.distribution-legend div > span { display: flex; align-items: center; gap: 6px; white-space: nowrap; }.distribution-legend i { width: 7px; height: 7px; flex: 0 0 auto; border-radius: 50%; }.distribution-legend strong { color: var(--ink); font-size: 10px; white-space: nowrap; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }
@media (max-width: 1100px) { .collection-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 700px) { .header-actions { flex-direction: column; }.compact-search { width: 100%; }.collection-grid { grid-template-columns: 1fr; } }
</style>
