import { createRouter, createWebHistory } from 'vue-router'
import AppShell from '@/components/AppShell.vue'

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
      ],
    },
    { path: '/reader/:id', name: 'reader', component: () => import('@/views/ReaderView.vue'), meta: { title: '精读' } },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${String(to.meta.title)} · LexiFlow` : 'LexiFlow'
})

export default router
