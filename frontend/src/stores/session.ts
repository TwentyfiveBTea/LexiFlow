import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { UserProfileResponse } from '@/lib/api'

export const useSessionStore = defineStore('session', () => {
  const token = ref(localStorage.getItem('lexiflow.token'))
  const userName = ref(localStorage.getItem('lexiflow.userName') ?? '学者')
  const email = ref(localStorage.getItem('lexiflow.email') ?? 'scholar@example.com')
  const avatar = ref(localStorage.getItem('lexiflow.avatar'))
  const registeredDays = ref(Number(localStorage.getItem('lexiflow.registeredDays')) || 1)
  const isAuthenticated = computed(() => Boolean(token.value))

  function signIn(name = '学者', nextToken = 'demo-session', accountEmail = 'scholar@example.com') {
    token.value = nextToken
    userName.value = name
    email.value = accountEmail
    localStorage.setItem('lexiflow.token', nextToken)
    localStorage.setItem('lexiflow.userName', name)
    localStorage.setItem('lexiflow.email', accountEmail)
  }

  function updateProfile(profile: UserProfileResponse) {
    email.value = profile.email
    avatar.value = profile.avatar
    registeredDays.value = profile.registeredDays
    localStorage.setItem('lexiflow.email', profile.email)
    localStorage.setItem('lexiflow.registeredDays', String(profile.registeredDays))
    if (profile.avatar) localStorage.setItem('lexiflow.avatar', profile.avatar)
    else localStorage.removeItem('lexiflow.avatar')
  }

  function signOut() {
    token.value = null
    localStorage.removeItem('lexiflow.token')
  }

  return { token, userName, email, avatar, registeredDays, isAuthenticated, signIn, updateProfile, signOut }
})
