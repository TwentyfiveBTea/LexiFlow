export interface Article {
  id: string
  language: string
  articleId: string
  languageCode: 'en' | 'ja'
  source: string
  title: string
  excerpt: string
  date: string
  words: number
  wordCount: number
  createdAt: string
  progress: number
  readTime: string
  tone: 'blue' | 'clay' | 'charcoal' | 'sage'
  processingDetail: ArticleProcessingDetail
}

export interface ArticleProcessingDetail {
  wordCount: number
  parseStatus: number
  translationStatus: number
  analysisStatus: number
  parsedAt: string | null
  translatedAt: string | null
  analyzedAt: string | null
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
  { id: 'algorithmic-sentience', articleId: 'algorithmic-sentience', language: '英语', languageCode: 'en', source: 'Lexis Review', title: 'The Jurisprudence of Algorithmic Sentience', excerpt: 'As artificial intelligence models approach theoretical autonomy, legal frameworks struggle to define liability and synthetic rights.', date: '今天', words: 1380, wordCount: 1380, createdAt: '2026-07-19T08:30:00+08:00', progress: 100, readTime: '14 分钟', tone: 'blue', processingDetail: { wordCount: 1380, parseStatus: 2, translationStatus: 2, analysisStatus: 2, parsedAt: '2026-07-19T08:31:00+08:00', translatedAt: '2026-07-19T08:34:00+08:00', analyzedAt: '2026-07-19T08:36:00+08:00' } },
  { id: 'language-acquisition', articleId: 'language-acquisition', language: '英语', languageCode: 'en', source: 'Language Review', title: 'How Deep Reading Shapes Language Acquisition', excerpt: 'Sustained attention to complex texts strengthens vocabulary, inference, and long-term recall.', date: '2 天前', words: 965, wordCount: 965, createdAt: '2026-07-17T15:20:00+08:00', progress: 100, readTime: '11 分钟', tone: 'clay', processingDetail: { wordCount: 965, parseStatus: 2, translationStatus: 2, analysisStatus: 1, parsedAt: '2026-07-17T15:21:00+08:00', translatedAt: '2026-07-17T15:24:00+08:00', analyzedAt: null } },
  { id: 'academic-memory', articleId: 'academic-memory', language: '英语', languageCode: 'en', source: 'Academic Notes', title: 'Memory, Context, and Academic Vocabulary', excerpt: 'Academic vocabulary becomes durable when learners encounter it across meaningful contexts.', date: '4 天前', words: 1124, wordCount: 1124, createdAt: '2026-07-15T10:05:00+08:00', progress: 100, readTime: '9 分钟', tone: 'charcoal', processingDetail: { wordCount: 1124, parseStatus: 2, translationStatus: 2, analysisStatus: 2, parsedAt: '2026-07-15T10:06:00+08:00', translatedAt: '2026-07-15T10:09:00+08:00', analyzedAt: '2026-07-15T10:12:00+08:00' } },
  { id: 'memory-language', articleId: 'memory-language', language: '日语', languageCode: 'ja', source: '思想', title: '記憶と言語のあいだにあるもの', excerpt: '言葉を学ぶことは、世界を見るための新しい輪郭を手に入れることでもある。', date: '7 月 14 日', words: 860, wordCount: 860, createdAt: '2026-07-14T18:40:00+08:00', progress: 100, readTime: '18 分钟', tone: 'sage', processingDetail: { wordCount: 860, parseStatus: 2, translationStatus: 2, analysisStatus: 2, parsedAt: '2026-07-14T18:41:00+08:00', translatedAt: '2026-07-14T18:45:00+08:00', analyzedAt: '2026-07-14T18:47:00+08:00' } },
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
