import axios from 'axios'

export interface ApiResult<T> {
  code: string
  message: string
  data: T
}

export interface LoginResponse {
  userId: string
  email: string
  avatar: string | null
  token: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  username: string
  password: string
  confirmPassword: string
}

export interface UserProfileResponse {
  avatar: string | null
  email: string
  registeredDays: number
}

export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

export interface ArticleUploadResponse {
  articleId: string
  title: string
  languageCode: string
  parseStatus: number
  translationStatus: number
}

export interface ArticleProcessingDetailResponse {
  wordCount: number
  parseStatus: number
  translationStatus: number
  analysisStatus: number
  parsedAt: string | null
  translatedAt: string | null
  analyzedAt: string | null
}

export interface RechargeRecordResponse {
  orderNo: string
  amountYuan: number
  credits: number
  creditedAt: string
}

export interface CreditLedgerResponse {
  articleId: string
  totalCredits: number
  ocrCredits: number
  translationCredits: number
  completedAt: string
}

export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface DueWordResponse {
  libraryWordId: string | null
  wordId: number
  languageCode: 'en' | 'ja'
  level: string | null
  word: string
  kana: string | null
  us: string | null
  uk: string | null
  translations: string | null
}

export interface ArticleDetailResponse {
  articleId: string
  title: string
  parsedContent: string
  languageCode: 'en' | 'ja'
  wordCount: number
  charCount: number
  createdAt: string
}

export interface ArticleListResponse {
  articleId: string
  title: string
  languageCode: 'en' | 'ja'
  wordCount: number
  createdAt: string
}

export interface ArticleVocabResponse {
  articleVocabId: string
  wordId: number
  languageCode: 'en' | 'ja'
  baseWord: string
  matchedForms: string | null
  occurrenceCount: number
  firstMatchedText: string | null
  firstSentence: string | null
  translations: string | null
  us: string | null
  uk: string | null
  kana: string | null
}

export interface ArticleVocabOccurrenceResponse {
  occurrenceId: string
  articleVocabId: string
  wordId: number
  normalizedText: string
  posTag: string | null
  posType: string | null
  sentence: string
  startOffset: number
  endOffset: number
}

export interface ArticleAnalyzeResponse {
  articleId: string
  analysisLevel: string
  analysisStatus: number
  reused: boolean
  matchedWordCount: number
  vocabs: ArticleVocabResponse[]
}

export interface VocabLibraryResponse {
  libraryId: string
  name: string
  languageCode: 'en' | 'ja'
  description: string | null
  wordCount: number
  createdAt: string
  updatedAt: string
}

export interface VocabLibraryWordResponse {
  libraryWordId: string
  wordId: number
  languageCode: 'en' | 'ja'
  word: string
  kana: string | null
  us: string | null
  uk: string | null
  translations: string | null
  phrases: string | null
  addedAt: string
}

export interface VocabLibraryStatisticsResponse {
  libraryId: string
  totalCount: number
  newCount: number
  learningCount: number
  masteredCount: number
  dueCount: number
}

export interface CreateVocabLibraryRequest {
  name: string
  languageCode: 'en' | 'ja'
  description?: string
}

export interface CreditAccountResponse {
  availableCredits: number
  frozenCredits: number
  status: number
  updatedAt: string
}

export interface CreatePaymentOrderRequest {
  amountYuan: number
  clientRequestNo: string
  deviceType?: string
}

export interface CreatePaymentOrderResponse {
  orderNo: string
  amountMinor: number
  totalCredits: number
  orderStatus: number
  expiresAt: string
  submitUrl: string
  method: string
  parameters: Record<string, string>
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api/v1',
  timeout: 20_000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('lexiflow.token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use((response) => {
  const payload = response.data as ApiResult<unknown>
  if (payload?.code && payload.code !== '0000000') {
    return Promise.reject(new Error(payload.message || '请求失败'))
  }
  return response
})

export async function getUserProfile() {
  const response = await api.get<ApiResult<UserProfileResponse>>('/user/profile')
  return response.data.data
}

export async function login(payload: LoginRequest) {
  const response = await api.post<ApiResult<LoginResponse>>('/auth/login', payload)
  return response.data.data
}

export async function register(payload: RegisterRequest) {
  await api.post<ApiResult<void>>('/auth/register', payload)
}

export async function logout() {
  await api.post<ApiResult<void>>('/auth/logout')
}

export async function changeUserAvatar(file: File) {
  const formData = new FormData()
  formData.append('avatar', file)
  const response = await api.post<ApiResult<string>>('/user/profile/change-avatar', formData)
  return response.data.data
}

export async function changeUsername(username: string) {
  await api.post<ApiResult<void>>('/user/profile/change-username', { username })
}

export async function changePassword(payload: ChangePasswordRequest) {
  await api.post<ApiResult<void>>('/user/profile/change-password', payload)
}

export async function getDueWords(libraryId?: string) {
  const response = await api.get<ApiResult<DueWordResponse[]>>('/learning/due', {
    params: libraryId ? { libraryId } : undefined,
  })
  return response.data.data
}

export async function getTodayDueWordCount() {
  const response = await api.get<ApiResult<number>>('/vocab/due')
  return response.data.data
}

export async function reviewWord(wordId: number, languageCode: 'en' | 'ja', rating: 'UNKNOWN' | 'VAGUE' | 'KNOWN') {
  await api.post<ApiResult<void>>(`/learning/words/${wordId}/review`, { languageCode, rating })
}

export async function getArticleDetail(articleId: string) {
  const response = await api.get<ApiResult<ArticleDetailResponse>>(`/article/${articleId}`)
  return response.data.data
}

export async function getRecentArticles() {
  const response = await api.get<ApiResult<ArticleListResponse[]>>('/article/recent')
  return response.data.data
}

export async function getArticles(params?: { keyword?: string; languageCode?: 'en' | 'ja' }) {
  const response = await api.get<ApiResult<ArticleListResponse[]>>('/article/list', { params })
  return response.data.data
}

export async function getArticleProcessingDetail(articleId: string) {
  const response = await api.get<ApiResult<ArticleProcessingDetailResponse>>(`/article/${articleId}/processing-detail`)
  return response.data.data
}

export async function uploadArticle(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await api.post<ApiResult<ArticleUploadResponse>>('/article/upload', formData)
  return response.data.data
}

export async function analyzeArticle(articleId: string, analysisLevel: string) {
  const response = await api.post<ApiResult<ArticleAnalyzeResponse>>(`/article/${articleId}/analyze`, { analysisLevel })
  return response.data.data
}

export async function getArticleVocabLevels(articleId: string) {
  const response = await api.get<ApiResult<string[]>>(`/article/${articleId}/vocab-levels`)
  return response.data.data
}

export async function getArticleVocabs(articleId: string, analysisLevel: string) {
  const response = await api.get<ApiResult<ArticleVocabResponse[]>>(`/article/${articleId}/vocabs`, {
    params: { analysisLevel },
  })
  return response.data.data
}

export async function getArticleVocabOccurrences(articleId: string, articleVocabId: string) {
  const response = await api.get<ApiResult<ArticleVocabOccurrenceResponse[]>>(`/article/${articleId}/vocabs/${articleVocabId}/occurrences`)
  return response.data.data
}

export async function getVocabLibraries(params?: { keyword?: string; languageCode?: 'en' | 'ja' }) {
  const response = await api.get<ApiResult<VocabLibraryResponse[]>>('/vocab/libraries', {
    params,
  })
  return response.data.data
}

export async function createVocabLibrary(payload: CreateVocabLibraryRequest) {
  const response = await api.post<ApiResult<VocabLibraryResponse>>('/vocab/libraries', payload)
  return response.data.data
}

export async function getVocabLibraryWords(libraryId: string, params?: { keyword?: string; level?: string }) {
  const response = await api.get<ApiResult<VocabLibraryWordResponse[]>>(`/vocab/libraries/${libraryId}/words`, { params })
  return response.data.data
}

export async function getVocabLibraryStatistics(libraryId: string) {
  const response = await api.get<ApiResult<VocabLibraryStatisticsResponse>>(`/vocab/libraries/${libraryId}/statistics`)
  return response.data.data
}

export async function deleteVocabLibraryWord(libraryId: string, wordId: number, languageCode: 'en' | 'ja') {
  await api.delete<ApiResult<void>>(`/vocab/libraries/${libraryId}/words/${wordId}`, { params: { languageCode } })
}

export async function deleteVocabLibrary(libraryId: string) {
  await api.delete<ApiResult<void>>(`/vocab/libraries/${libraryId}`)
}

export async function addArticleVocabToLibrary(libraryId: string, articleId: string, articleVocabId: string) {
  await api.post<ApiResult<void>>(`/vocab/libraries/${libraryId}/words`, { articleId, articleVocabId })
}

export async function deleteArticle(articleId: string) {
  await api.delete<ApiResult<void>>(`/article/${articleId}`)
}

export async function getRechargeRecords(page: number, pageSize: number) {
  const response = await api.get<ApiResult<PageResponse<RechargeRecordResponse>>>('/pay/recharges', {
    params: { page, pageSize },
  })
  return response.data.data
}

export async function getCreditLedger(page: number, pageSize: number) {
  const response = await api.get<ApiResult<PageResponse<CreditLedgerResponse>>>('/pay/credits/ledger', {
    params: { page, pageSize },
  })
  return response.data.data
}

export async function getCreditAccount() {
  const response = await api.get<ApiResult<CreditAccountResponse>>('/pay/credits/account')
  return response.data.data
}

export async function createPaymentOrder(payload: CreatePaymentOrderRequest) {
  const response = await api.post<ApiResult<CreatePaymentOrderResponse>>('/pay/orders', payload)
  return response.data.data
}
