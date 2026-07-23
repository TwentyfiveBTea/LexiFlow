import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { LoginResponse, UserProfileResponse } from '@/lib/api'

export const useSessionStore = defineStore('session', () => {
  const token = ref(localStorage.getItem('lexiflow.token'))
  const userName = ref(localStorage.getItem('lexiflow.userName') ?? '学者')
  const email = ref(localStorage.getItem('lexiflow.email') ?? 'scholar@example.com')
  const avatar = ref(localStorage.getItem('lexiflow.avatar'))
  const registeredDays = ref(Number(localStorage.getItem('lexiflow.registeredDays')) || 1)
  const isAuthenticated = computed(() => Boolean(token.value))

  function signIn(response: LoginResponse, name = response.email.split('@')[0] || '学者') {
    token.value = response.token
    userName.value = name
    email.value = response.email
    setAvatar(response.avatar)
    localStorage.setItem('lexiflow.token', response.token)
    localStorage.setItem('lexiflow.userName', name)
    localStorage.setItem('lexiflow.email', response.email)
  }

  function updateProfile(profile: UserProfileResponse) {
    email.value = profile.email
    registeredDays.value = profile.registeredDays
    localStorage.setItem('lexiflow.email', profile.email)
    localStorage.setItem('lexiflow.registeredDays', String(profile.registeredDays))
    setAvatar(profile.avatar)
  }

  function setAvatar(nextAvatar: string | null) {
    avatar.value = nextAvatar
    if (nextAvatar) localStorage.setItem('lexiflow.avatar', nextAvatar)
    else localStorage.removeItem('lexiflow.avatar')
  }

  function setUserName(nextUserName: string) {
    userName.value = nextUserName
    localStorage.setItem('lexiflow.userName', nextUserName)
  }

  function signOut() {
    token.value = null
    localStorage.removeItem('lexiflow.token')
  }

  return { token, userName, email, avatar, registeredDays, isAuthenticated, signIn, updateProfile, setAvatar, setUserName, signOut }
})
