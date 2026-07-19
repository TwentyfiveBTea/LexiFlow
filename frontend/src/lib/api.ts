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

export async function reviewWord(wordId: number, languageCode: 'en' | 'ja', rating: 'UNKNOWN' | 'VAGUE' | 'KNOWN') {
  await api.post<ApiResult<void>>(`/learning/words/${wordId}/review`, { languageCode, rating })
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
