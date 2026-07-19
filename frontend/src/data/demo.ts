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
  libraryWordId: string
  wordId: number
  languageCode: 'en' | 'ja'
  word: string
  kana: string
  us: string
  uk: string
  translations: string
  phrases: string
  addedAt: string
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
  { id: 1, libraryWordId: 'core-word-1', wordId: 1, languageCode: 'en', word: 'sentience', kana: '', us: '/ˈsen.ti.əns/', uk: '/ˈsen.ti.əns/', translations: '[{"translation":"知觉；感觉能力","type":"n."}]', phrases: '[]', addedAt: '2026-07-19T08:30:00+08:00', term: 'sentience', phonetic: '/ˈsen.ti.əns/', partOfSpeech: 'n.', definition: 'The capacity to feel, perceive, or experience subjectively.', translation: '知觉；感觉能力', level: 'GRE', mastery: 78 },
  { id: 2, libraryWordId: 'core-word-2', wordId: 2, languageCode: 'en', word: 'futile', kana: '', us: '/ˈfjuː.taɪl/', uk: '/ˈfjuː.taɪl/', translations: '[{"translation":"无用的；徒劳的","type":"adj."}]', phrases: '[]', addedAt: '2026-07-19T08:32:00+08:00', term: 'futile', phonetic: '/ˈfjuː.taɪl/', partOfSpeech: 'adj.', definition: 'Incapable of producing any useful result; pointless.', translation: '无用的；徒劳的', level: 'IELTS', mastery: 42 },
  { id: 3, libraryWordId: 'core-word-3', wordId: 3, languageCode: 'en', word: 'impute', kana: '', us: '/ɪmˈpjuːt/', uk: '/ɪmˈpjuːt/', translations: '[{"translation":"归咎于；归功于","type":"v."}]', phrases: '[]', addedAt: '2026-07-19T08:35:00+08:00', term: 'impute', phonetic: '/ɪmˈpjuːt/', partOfSpeech: 'v.', definition: 'Represent something undesirable as being caused by someone.', translation: '归咎于；归功于', level: 'GRE', mastery: 31 },
  { id: 4, libraryWordId: 'core-word-4', wordId: 4, languageCode: 'en', word: 'stifle', kana: '', us: '/ˈstaɪ.fəl/', uk: '/ˈstaɪ.fəl/', translations: '[{"translation":"扼杀；抑制","type":"v."}]', phrases: '[]', addedAt: '2026-07-19T08:38:00+08:00', term: 'stifle', phonetic: '/ˈstaɪ.fəl/', partOfSpeech: 'v.', definition: 'Prevent or constrain an activity or idea.', translation: '扼杀；抑制', level: 'IELTS', mastery: 65 },
  { id: 5, libraryWordId: 'core-word-5', wordId: 5, languageCode: 'en', word: 'culpability', kana: '', us: '/ˌkʌl.pəˈbɪl.ə.ti/', uk: '/ˌkʌl.pəˈbɪl.ə.ti/', translations: '[{"translation":"罪责；可责性","type":"n."}]', phrases: '[]', addedAt: '2026-07-19T08:40:00+08:00', term: 'culpability', phonetic: '/ˌkʌl.pəˈbɪl.ə.ti/', partOfSpeech: 'n.', definition: 'Responsibility for a fault or wrong.', translation: '罪责；可责性', level: 'GRE', mastery: 23 },
  { id: 6, libraryWordId: 'core-word-6', wordId: 6, languageCode: 'en', word: 'nascent', kana: '', us: '/ˈnæs.ənt/', uk: '/ˈnæs.ənt/', translations: '[{"translation":"初生的；萌芽的","type":"adj."}]', phrases: '[]', addedAt: '2026-07-19T08:42:00+08:00', term: 'nascent', phonetic: '/ˈnæs.ənt/', partOfSpeech: 'adj.', definition: 'Just coming into existence and beginning to display signs of potential.', translation: '初生的；萌芽的', level: 'GRE', mastery: 54 },
  { id: 7, libraryWordId: 'literature-word-1', wordId: 7, languageCode: 'ja', word: '記憶', kana: 'きおく', us: '', uk: '', translations: '[{"translation":"记忆","type":"名词"}]', phrases: '[]', addedAt: '2026-07-18T15:10:00+08:00', term: '記憶', phonetic: 'きおく', partOfSpeech: '名词', definition: '过去的经验或知识留存在头脑中的状态。', translation: '记忆', level: 'N3', mastery: 62 },
  { id: 8, libraryWordId: 'literature-word-2', wordId: 8, languageCode: 'ja', word: '言語', kana: 'げんご', us: '', uk: '', translations: '[{"translation":"语言","type":"名词"}]', phrases: '[]', addedAt: '2026-07-18T15:14:00+08:00', term: '言語', phonetic: 'げんご', partOfSpeech: '名词', definition: '人类用来表达和交流思想的符号体系。', translation: '语言', level: 'N4', mastery: 48 },
  { id: 9, libraryWordId: 'literature-word-3', wordId: 9, languageCode: 'ja', word: '学ぶ', kana: 'まなぶ', us: '', uk: '', translations: '[{"translation":"学习；钻研","type":"动词"}]', phrases: '[]', addedAt: '2026-07-18T15:18:00+08:00', term: '学ぶ', phonetic: 'まなぶ', partOfSpeech: '动词', definition: '通过学习或经验掌握知识和技能。', translation: '学习；钻研', level: 'N5', mastery: 71 },
]
