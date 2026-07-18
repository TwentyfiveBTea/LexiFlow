<script setup lang="ts">
import { ArrowLeft, BookmarkPlus, ChevronRight, Clock3, PanelRightClose, PanelRightOpen, Volume2 } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BrandMark from '@/components/BrandMark.vue'
import { words } from '@/data/demo'

const router = useRouter()
const progress = ref(0)
const panelOpen = ref(true)
const selectedTerm = ref('sentience')
const saved = ref<number[]>([])
const selectedWord = computed(() => words.find((word) => word.term === selectedTerm.value) ?? words[0]!)

function updateProgress() {
  const height = document.documentElement.scrollHeight - window.innerHeight
  progress.value = height > 0 ? Math.min(100, window.scrollY / height * 100) : 0
}

function selectWord(term: string) {
  selectedTerm.value = term
  panelOpen.value = true
}

function pronounce(term: string) {
  if ('speechSynthesis' in window) {
    speechSynthesis.cancel()
    speechSynthesis.speak(new SpeechSynthesisUtterance(term))
  }
}

function saveWord(id: number) {
  saved.value = saved.value.includes(id) ? saved.value.filter((item) => item !== id) : [...saved.value, id]
}

onMounted(() => { updateProgress(); window.addEventListener('scroll', updateProgress, { passive: true }) })
onBeforeUnmount(() => window.removeEventListener('scroll', updateProgress))
</script>

<template>
  <div class="reader" :class="{ 'panel-closed': !panelOpen }">
    <div class="reading-progress" :style="{ width: `${progress}%` }"></div>
    <header class="reader-nav">
      <button class="icon-btn" aria-label="返回文章库" @click="router.push('/articles')"><ArrowLeft :size="19" /></button>
      <BrandMark />
      <button class="icon-btn" :aria-label="panelOpen ? '收起词汇侧栏' : '打开词汇侧栏'" @click="panelOpen = !panelOpen"><PanelRightClose v-if="panelOpen" :size="20" /><PanelRightOpen v-else :size="20" /></button>
    </header>

    <main class="reading-canvas">
      <article class="article-body">
        <header class="article-header fade-in">
          <p class="article-kicker">技术与法律 · 2026 年 7 月 18 日</p>
          <h1 class="serif">The Jurisprudence of Algorithmic <button class="word title-word" :class="{ active: selectedTerm === 'sentience' }" @click="selectWord('sentience')">Sentience</button></h1>
          <p class="dek serif">As artificial intelligence models approach theoretical autonomy, legal frameworks struggle to define the boundaries of liability and synthetic rights.</p>
          <div class="byline"><span>作者：Dr. Evelyn Sterling</span><span><Clock3 :size="15" />14 分钟阅读</span></div>
        </header>

        <div class="prose serif">
          <p>The rapid evolution of generative models has precipitated a profound legal crisis. We are no longer dealing with mere automation; we are navigating the murky waters of algorithmic <button class="word" :class="{ active: selectedTerm === 'sentience' }" @click="selectWord('sentience')">sentience</button>, or at least the convincing simulacrum thereof. The traditional paradigms of intellectual property and tort law are proving grossly inadequate when confronted with systems capable of novel creation and autonomous decision-making.</p>
          <p>Consider the case of a decentralized autonomous organization governed by a neural network. If this entity executes a smart contract that results in catastrophic financial loss for a third party, establishing <button class="word" :class="{ active: selectedTerm === 'culpability' }" @click="selectWord('culpability')">culpability</button> becomes an exercise in <button class="word" :class="{ active: selectedTerm === 'futile' }" @click="selectWord('futile')">futile</button> obfuscation. Is the original programmer liable? The nodes executing the code? Or does the algorithm itself possess a <button class="word" :class="{ active: selectedTerm === 'nascent' }" @click="selectWord('nascent')">nascent</button> form of legal personhood?</p>
          <figure>
            <img src="https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1400&q=85" alt="桌面上的计算机代码与研究资料" />
            <figcaption>图 1. 技术系统与制度框架的交叉研究。</figcaption>
          </figure>
          <p>Scholars argue that to <button class="word" :class="{ active: selectedTerm === 'impute' }" @click="selectWord('impute')">impute</button> liability to a non-biological entity requires a radical restructuring of jurisprudence. The concept of <em>mens rea</em> is incompatible with statistical weight matrices. Yet the societal demand for accountability cannot be ignored.</p>
          <p>This approach, while pragmatic, threatens to <button class="word" :class="{ active: selectedTerm === 'stifle' }" @click="selectWord('stifle')">stifle</button> innovation. It imposes an immense burden on creators to anticipate every conceivable permutation of their system's behavior in an infinitely complex real-world environment.</p>
          <h2>The burden of interpretation</h2>
          <p>The harder question is not whether a model can produce a persuasive answer, but how institutions should interpret the chain of human choices that made the answer possible. A useful legal framework must preserve that chain without pretending that technical complexity dissolves responsibility.</p>
        </div>
      </article>

      <aside v-if="panelOpen" class="vocab-panel">
        <div class="panel-head"><div><p class="eyebrow">GRE & IELTS</p><h2 class="serif">生词解析</h2></div><button class="icon-btn" aria-label="收起侧栏" @click="panelOpen = false"><PanelRightClose :size="19" /></button></div>
        <section class="selected-card">
          <div class="word-title"><h3 class="serif">{{ selectedWord.term }}</h3><span class="badge">{{ selectedWord.level }}</span></div>
          <button class="pronounce" @click="pronounce(selectedWord.term)"><Volume2 :size="15" />{{ selectedWord.phonetic }}</button>
          <p>{{ selectedWord.partOfSpeech }} {{ selectedWord.definition }}</p><strong>{{ selectedWord.translation }}</strong>
          <button class="save-word" :class="{ saved: saved.includes(selectedWord.id) }" @click="saveWord(selectedWord.id)"><BookmarkPlus :size="16" />{{ saved.includes(selectedWord.id) ? '已加入词库' : '加入词库' }}</button>
        </section>

        <div class="word-index">
          <button v-for="word in words" :key="word.id" :class="{ active: word.term === selectedTerm }" @click="selectWord(word.term)"><span><strong class="serif">{{ word.term }}</strong><small>{{ word.translation }}</small></span><ChevronRight :size="16" /></button>
        </div>
      </aside>
    </main>
  </div>
</template>

<style scoped>
.reader { --panel: 344px; min-height: 100vh; background: var(--surface); }.reader.panel-closed { --panel: 0px; }.reading-progress { position: fixed; z-index: 80; left: 0; top: 0; height: 3px; background: var(--secondary); }
.reader-nav { position: fixed; z-index: 45; left: 0; right: var(--panel); top: 0; height: 66px; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; border-bottom: 1px solid rgba(199,196,192,.75); background: rgba(252,249,248,.94); backdrop-filter: blur(12px); transition: right .22s ease; }.reader-nav :deep(.brand-copy small) { display: none; }
.reading-canvas { padding-right: var(--panel); transition: padding-right .22s ease; }.article-body { width: min(100%, 820px); margin-inline: auto; padding: 126px 42px 120px; }
.article-header { margin-bottom: 50px; }.article-kicker { margin: 0 0 16px; color: var(--secondary); font-size: 11px; font-weight: 700; text-transform: uppercase; }.article-header h1 { margin: 0; color: var(--ink); font-size: 49px; line-height: 1.18; font-weight: 650; }.dek { margin: 22px 0 0; color: var(--ink-muted); font-size: 22px; line-height: 1.55; }.byline { display: flex; align-items: center; gap: 22px; padding-top: 20px; margin-top: 28px; border-top: 1px solid var(--outline); color: var(--ink-muted); font-size: 12px; }.byline span { display: flex; align-items: center; gap: 6px; }
.prose { color: #282a29; font-size: 18.5px; line-height: 1.78; }.prose p { margin: 0 0 30px; }.prose h2 { margin: 56px 0 20px; color: var(--primary); font-size: 28px; }.prose figure { margin: 42px 0; }.prose img { width: 100%; aspect-ratio: 16/9; display: block; object-fit: cover; border: 1px solid var(--outline); border-radius: 5px; filter: saturate(.68) contrast(.95); }.prose figcaption { margin-top: 9px; color: var(--ink-muted); text-align: center; font-family: 'Inter', sans-serif; font-size: 11px; }
.word { display: inline; padding: 0 1px; border: 0; border-bottom: 1px solid var(--secondary); border-radius: 1px; color: inherit; background: rgba(253,203,155,.23); font: inherit; line-height: inherit; }.word:hover, .word.active { background: var(--secondary-soft); }.title-word { text-transform: capitalize; }
.vocab-panel { position: fixed; z-index: 50; inset: 0 0 0 auto; width: var(--panel); padding: 24px 20px; overflow-y: auto; border-left: 1px solid var(--outline); background: var(--surface-lowest); box-shadow: -8px 0 28px rgba(45,45,45,.04); }.panel-head { position: sticky; z-index: 3; top: -24px; display: flex; align-items: center; justify-content: space-between; padding: 24px 0 15px; border-bottom: 1px solid var(--outline); background: white; }.panel-head .eyebrow { margin-bottom: 2px; }.panel-head h2 { margin: 0; color: var(--primary); font-size: 23px; }
.selected-card { padding: 20px; margin-top: 18px; border: 1px solid var(--primary); border-radius: 7px; background: var(--surface); }.word-title { display: flex; align-items: center; justify-content: space-between; }.word-title h3 { margin: 0; color: var(--primary); font-size: 28px; }.pronounce { display: flex; align-items: center; gap: 6px; margin: 7px 0 16px; padding: 0; border: 0; color: var(--ink-muted); background: transparent; font-size: 12px; }.selected-card > p { margin: 0 0 8px; color: var(--ink); font-size: 13px; line-height: 1.55; }.selected-card > strong { color: var(--secondary); font-size: 13px; }.save-word { width: 100%; min-height: 38px; display: flex; align-items: center; justify-content: center; gap: 7px; margin-top: 18px; border: 1px solid var(--outline); border-radius: 6px; color: var(--primary); background: white; font-size: 12px; font-weight: 700; }.save-word.saved { color: white; border-color: var(--success); background: var(--success); }
.word-index { display: grid; margin-top: 18px; }.word-index > button { min-height: 58px; display: flex; align-items: center; gap: 10px; padding: 0 9px; border: 0; border-bottom: 1px solid rgba(199,196,192,.7); color: var(--ink-muted); background: transparent; text-align: left; }.word-index > button:hover, .word-index > button.active { color: var(--primary); background: var(--surface-low); }.word-index span { flex: 1; display: grid; gap: 2px; }.word-index strong { color: var(--ink); font-size: 16px; }.word-index small { font-size: 11px; }
@media (max-width: 1050px) { .reader { --panel: 310px; }.article-body { padding-inline: 30px; }.article-header h1 { font-size: 40px; }.dek { font-size: 19px; } }
@media (max-width: 820px) { .reader { --panel: 0px; }.reading-canvas { padding-right: 0; }.reader-nav { right: 0; }.vocab-panel { width: min(88vw, 344px); box-shadow: -12px 0 40px rgba(0,0,0,.16); }.article-body { padding: 104px 22px 90px; }.article-header h1 { font-size: 35px; }.dek { font-size: 18px; }.prose { font-size: 18px; }.reader-nav { padding-inline: 14px; }.reader-nav :deep(.brand-copy) { display: none; } }
</style>
