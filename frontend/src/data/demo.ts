export interface Article {
  id: string
  language: string
  source: string
  title: string
  excerpt: string
  date: string
  words: number
  progress: number
  readTime: string
  tone: 'blue' | 'clay' | 'charcoal' | 'sage'
}

export interface VocabularyCollection {
  libraryId: string
  name: string
  languageCode: 'en' | 'ja'
  description: string
  wordCount: number
  createdAt: string
  updatedAt: string
  tone: 'blue' | 'clay' | 'charcoal' | 'sage'
  statistics: VocabularyStatistics
}

export interface VocabularyStatistics {
  totalCount: number
  newCount: number
  learningCount: number
  masteredCount: number
  dueCount: number
}

export interface Word {
  id: number
  term: string
  phonetic: string
  partOfSpeech: string
  definition: string
  translation: string
  level: string
  mastery: number
}

export const articles: Article[] = [
  { id: 'algorithmic-sentience', language: '英语', source: 'Lexis Review', title: 'The Jurisprudence of Algorithmic Sentience', excerpt: 'As artificial intelligence models approach theoretical autonomy, legal frameworks struggle to define liability and synthetic rights.', date: '今天', words: 38, progress: 62, readTime: '14 分钟', tone: 'blue' },
  { id: 'urban-space', language: '法语', source: 'Le Monde', title: "L'Architecture de Demain: Repenser l'Espace Urbain", excerpt: 'Une exploration des tendances architecturales qui redéfinissent nos villes face aux défis climatiques.', date: '2 天前', words: 42, progress: 65, readTime: '11 分钟', tone: 'clay' },
  { id: 'philosophy-silence', language: '德语', source: 'Der Spiegel', title: 'Die Philosophie der Stille in einer lauten Welt', excerpt: 'Warum wir das Schweigen wieder lernen müssen, um klarer zu denken und besser zu verstehen.', date: '4 天前', words: 27, progress: 31, readTime: '9 分钟', tone: 'charcoal' },
  { id: 'memory-language', language: '日语', source: '思想', title: '記憶と言語のあいだにあるもの', excerpt: '言葉を学ぶことは、世界を見るための新しい輪郭を手に入れることでもある。', date: '7 月 14 日', words: 51, progress: 100, readTime: '18 分钟', tone: 'sage' },
]

export const collections: VocabularyCollection[] = [
  { libraryId: 'core', name: '考研核心词汇', languageCode: 'en', description: '覆盖考研英语阅读与写作中的核心高频词汇。', wordCount: 1280, createdAt: '2026-06-18T09:30:00+08:00', updatedAt: '2026-07-19T08:15:00+08:00', tone: 'blue', statistics: { totalCount: 1280, newCount: 105, learningCount: 100, masteredCount: 1075, dueCount: 64 } },
  { libraryId: 'economist', name: '经济学人高频词', languageCode: 'en', description: '整理经济学人文章中的商业、政治与社会议题词汇。', wordCount: 450, createdAt: '2026-06-26T14:20:00+08:00', updatedAt: '2026-07-18T21:40:00+08:00', tone: 'clay', statistics: { totalCount: 450, newCount: 140, learningCount: 108, masteredCount: 202, dueCount: 48 } },
  { libraryId: 'academic', name: '学术写作必备', languageCode: 'en', description: '收录论文写作中常用的论证、衔接与研究方法词汇。', wordCount: 320, createdAt: '2026-07-02T11:05:00+08:00', updatedAt: '2026-07-19T06:10:00+08:00', tone: 'charcoal', statistics: { totalCount: 320, newCount: 200, learningCount: 82, masteredCount: 38, dueCount: 22 } },
  { libraryId: 'literature', name: '现代文学鉴赏', languageCode: 'ja', description: '用于日本现代文学阅读的表达、意象与评论词汇。', wordCount: 615, createdAt: '2026-05-12T16:45:00+08:00', updatedAt: '2026-07-16T19:25:00+08:00', tone: 'sage', statistics: { totalCount: 615, newCount: 21, learningCount: 28, masteredCount: 566, dueCount: 75 } },
]

export const words: Word[] = [
  { id: 1, term: 'sentience', phonetic: '/ˈsen.ti.əns/', partOfSpeech: 'n.', definition: 'The capacity to feel, perceive, or experience subjectively.', translation: '知觉；感觉能力', level: 'GRE', mastery: 78 },
  { id: 2, term: 'futile', phonetic: '/ˈfjuː.taɪl/', partOfSpeech: 'adj.', definition: 'Incapable of producing any useful result; pointless.', translation: '无用的；徒劳的', level: 'IELTS', mastery: 42 },
  { id: 3, term: 'impute', phonetic: '/ɪmˈpjuːt/', partOfSpeech: 'v.', definition: 'Represent something undesirable as being caused by someone.', translation: '归咎于；归功于', level: 'GRE', mastery: 31 },
  { id: 4, term: 'stifle', phonetic: '/ˈstaɪ.fəl/', partOfSpeech: 'v.', definition: 'Prevent or constrain an activity or idea.', translation: '扼杀；抑制', level: 'IELTS', mastery: 65 },
  { id: 5, term: 'culpability', phonetic: '/ˌkʌl.pəˈbɪl.ə.ti/', partOfSpeech: 'n.', definition: 'Responsibility for a fault or wrong.', translation: '罪责；可责性', level: 'GRE', mastery: 23 },
  { id: 6, term: 'nascent', phonetic: '/ˈnæs.ənt/', partOfSpeech: 'adj.', definition: 'Just coming into existence and beginning to display signs of potential.', translation: '初生的；萌芽的', level: 'GRE', mastery: 54 },
]
