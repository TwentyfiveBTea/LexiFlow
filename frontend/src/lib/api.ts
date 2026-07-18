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
