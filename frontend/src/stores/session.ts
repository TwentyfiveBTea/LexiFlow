import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useSessionStore = defineStore('session', () => {
  const token = ref(localStorage.getItem('lexiflow.token'))
  const userName = ref(localStorage.getItem('lexiflow.userName') ?? '学者')
  const isAuthenticated = computed(() => Boolean(token.value))

  function signIn(name = '学者', nextToken = 'demo-session') {
    token.value = nextToken
    userName.value = name
    localStorage.setItem('lexiflow.token', nextToken)
    localStorage.setItem('lexiflow.userName', name)
  }

  function signOut() {
    token.value = null
    localStorage.removeItem('lexiflow.token')
  }

  return { token, userName, isAuthenticated, signIn, signOut }
})
