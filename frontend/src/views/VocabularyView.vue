<script setup lang="ts">
import { BookOpen, GraduationCap, MoreVertical, Plus, Search, TrendingUp } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { collections } from '@/data/demo'

const router = useRouter()
const query = ref('')
const showCreate = ref(false)
const newName = ref('')
const createdCollections = ref<typeof collections>([])
const allCollections = computed(() => [...collections, ...createdCollections.value])
const filtered = computed(() => allCollections.value.filter((item) => item.name.includes(query.value)))

function createCollection() {
  if (!newName.value.trim()) return
  createdCollections.value.push({ id: `custom-${Date.now()}`, name: newName.value.trim(), words: 0, mastered: 0, lastOpened: '刚刚', tone: 'blue' })
  newName.value = ''
  showCreate.value = false
}
</script>

<template>
  <main class="page">
    <header class="page-header fade-in">
      <div><p class="eyebrow">Repository · Collections</p><h1 class="page-title">词汇库</h1><p class="page-description">分类管理词汇资源，建立深度阅读与学术研究的知识枢纽。</p></div>
      <div class="header-actions"><label class="compact-search"><Search :size="17" /><input v-model="query" placeholder="搜索词库" /></label><button class="btn btn-primary" @click="showCreate = true"><Plus :size="17" />新建词库</button></div>
    </header>

    <section class="collection-grid">
      <article v-for="(collection, index) in filtered" :key="collection.id" class="collection-card surface fade-in" :style="{ animationDelay: `${index * 75}ms` }" @click="router.push(`/vocabulary/${collection.id}`)">
        <div class="collection-top">
          <span class="collection-cover" :class="collection.tone"><GraduationCap v-if="collection.tone === 'blue'" :size="28" /><TrendingUp v-else-if="collection.tone === 'clay'" :size="28" /><BookOpen v-else :size="28" /></span>
          <button class="icon-btn" aria-label="词库操作" @click.stop><MoreVertical :size="19" /></button>
        </div>
        <h2 class="serif">{{ collection.name }}</h2>
        <p>{{ collection.words.toLocaleString() }} 词 · {{ collection.lastOpened }}访问</p>
        <div class="mastery"><div><strong>掌握度 {{ collection.words ? Math.round(collection.mastered / collection.words * 100) : 0 }}%</strong><span>{{ collection.mastered.toLocaleString() }} / {{ collection.words.toLocaleString() }}</span></div><div class="progress"><span :style="{ width: `${collection.words ? collection.mastered / collection.words * 100 : 0}%` }"></span></div></div>
      </article>

      <button class="create-card fade-in" @click="showCreate = true"><span><Plus :size="25" /></span><strong class="serif">创建新词库</strong><small>导入文章或手动加入单词，建立专属语料。</small></button>
    </section>

    <div v-if="showCreate" class="modal-backdrop" @click.self="showCreate = false">
      <form class="create-modal surface" @submit.prevent="createCollection"><p class="eyebrow">New collection</p><h2 class="serif">创建词汇库</h2><label class="field-label" for="collection-name">词库名称</label><input id="collection-name" v-model="newName" class="field" autofocus placeholder="例如：科技新闻高频词" /><div class="modal-actions"><button class="btn btn-secondary" type="button" @click="showCreate = false">取消</button><button class="btn btn-primary" type="submit">创建</button></div></form>
    </div>
  </main>
</template>

<style scoped>
.header-actions { display: flex; gap: 10px; }
.compact-search { width: 230px; height: 42px; display: flex; align-items: center; gap: 8px; padding: 0 12px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); background: white; }
.compact-search:focus-within { border-color: var(--primary); }.compact-search input { min-width: 0; flex: 1; border: 0; outline: 0; background: transparent; }
.collection-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; }
.collection-card { min-height: 285px; padding: 24px; cursor: pointer; transition: border-color .2s ease, transform .2s ease; }
.collection-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.collection-top { display: flex; justify-content: space-between; align-items: flex-start; }
.collection-cover { width: 60px; height: 76px; display: grid; place-items: center; border-radius: 4px; color: white; background: var(--primary); box-shadow: inset -7px 0 rgba(255,255,255,.09); }
.collection-cover.clay { background: var(--secondary); }.collection-cover.charcoal { background: #454848; }.collection-cover.sage { background: #597166; }
.collection-card h2 { margin: 22px 0 7px; color: var(--ink); font-size: 22px; }
.collection-card > p { margin: 0; color: var(--ink-muted); font-size: 12px; }
.mastery { margin-top: 31px; }
.mastery > div:first-child { display: flex; align-items: center; justify-content: space-between; margin-bottom: 9px; color: var(--ink-muted); font-size: 11px; }
.mastery strong { color: var(--primary); }
.create-card { min-height: 285px; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; border: 1px dashed var(--outline); border-radius: 8px; color: var(--ink-muted); background: transparent; }
.create-card:hover { color: var(--primary); border-color: var(--primary); background: var(--surface-low); }
.create-card > span { width: 54px; height: 54px; display: grid; place-items: center; border-radius: 50%; background: var(--surface-high); }
.create-card strong { margin-top: 16px; font-size: 19px; }.create-card small { max-width: 210px; margin-top: 7px; line-height: 1.5; }
.modal-backdrop { position: fixed; z-index: 60; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }
.create-modal { width: min(100%, 430px); padding: 30px; }.create-modal h2 { margin: 0 0 24px; color: var(--primary); font-size: 26px; }.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }
@media (max-width: 1100px) { .collection-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 700px) { .header-actions { flex-direction: column; }.compact-search { width: 100%; }.collection-grid { grid-template-columns: 1fr; } }
</style>
