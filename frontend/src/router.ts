import { createRouter, createWebHistory } from 'vue-router'
import AppShell from '@/components/AppShell.vue'
import { translateText } from '@/lib/i18n'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true, title: '登录' } },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { public: true, title: '注册' } },
    {
      path: '/',
      component: AppShell,
      children: [
        { path: 'dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '控制台' } },
        { path: 'articles', name: 'articles', component: () => import('@/views/ArticleLibraryView.vue'), meta: { title: '文章库' } },
        { path: 'vocabulary', name: 'vocabulary', component: () => import('@/views/VocabularyView.vue'), meta: { title: '词汇库' } },
        { path: 'vocabulary/:id', name: 'vocabulary-detail', component: () => import('@/views/VocabularyDetailView.vue'), meta: { title: '词汇明细' } },
        { path: 'review', name: 'review-list', component: () => import('@/views/ReviewListView.vue'), meta: { title: '单词复习' } },
        { path: 'review/session', name: 'review-session', component: () => import('@/views/ReviewSessionView.vue'), meta: { title: '开始复习' } },
        { path: 'wallet', name: 'wallet', component: () => import('@/views/WalletView.vue'), meta: { title: '钱包' } },
        { path: 'settings', name: 'settings', component: () => import('@/views/SettingsView.vue'), meta: { title: '设置' } },
      ],
    },
    { path: '/reader/:id', name: 'reader', component: () => import('@/views/ReaderView.vue'), meta: { title: '精读' } },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const isAuthenticated = Boolean(localStorage.getItem('lexiflow.token'))
  if (!to.meta.public && !isAuthenticated) return { name: 'login', query: { redirect: to.fullPath } }
  if (to.meta.public && isAuthenticated) return { name: 'dashboard' }
  return true
})

router.afterEach((to) => {
  const raw = localStorage.getItem('lexiflow.preferences')
  let language: 'zh-CN' | 'en' | 'ja' = 'zh-CN'
  try {
    const parsed = JSON.parse(raw ?? '{}') as { interfaceLanguage?: string }
    if (parsed.interfaceLanguage === 'en' || parsed.interfaceLanguage === 'ja') language = parsed.interfaceLanguage
  } catch {
    // Use Chinese as the default when preferences are unavailable.
  }
  const title = typeof to.meta.title === 'string' ? translateText(to.meta.title, language) : ''
  document.title = title ? `${title} · LexiFlow` : 'LexiFlow'
})

export default router
