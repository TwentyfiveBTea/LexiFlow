<script setup lang="ts">
import { Bell, Check, Languages, LockKeyhole, MonitorCog, Save, ShieldAlert, UserRound } from 'lucide-vue-next'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const saved = ref(false)
const settings = reactive({
  email: 'scholar@example.com',
  username: session.userName,
  readingFont: 'Literata',
  fontSize: '19',
  lineHeight: '1.7',
  highlight: true,
  dailyReminder: true,
  weeklyReport: false,
  interfaceLanguage: '简体中文',
  learningLanguage: '英语',
})

function save() {
  saved.value = true
  setTimeout(() => { saved.value = false }, 2200)
}

async function logout() {
  session.signOut()
  await router.push('/login')
}
</script>

<template>
  <main class="page settings-page">
    <header class="page-header fade-in"><div><p class="eyebrow">Preferences</p><h1 class="page-title">应用设置</h1><p class="page-description">管理账号、阅读体验与学习提醒。</p></div><button class="btn btn-primary" @click="save"><Check v-if="saved" :size="17" /><Save v-else :size="17" />{{ saved ? '已保存' : '保存更改' }}</button></header>

    <section class="settings-grid">
      <article class="settings-card surface fade-in wide">
        <div class="card-title"><span><UserRound :size="20" /></span><div><h2 class="serif">账号信息</h2><p>用于登录和展示的个人资料。</p></div></div>
        <div class="form-grid"><div><label class="field-label" for="settings-email">电子邮件</label><input id="settings-email" v-model="settings.email" class="field" type="email" /></div><div><label class="field-label" for="settings-username">用户名</label><input id="settings-username" v-model="settings.username" class="field" /></div></div>
        <button class="text-action"><LockKeyhole :size="15" />修改密码</button>
      </article>

      <article class="settings-card surface fade-in wide">
        <div class="card-title"><span><MonitorCog :size="20" /></span><div><h2 class="serif">阅读偏好</h2><p>调整长时间精读时的排版体验。</p></div></div>
        <div class="form-grid three"><div><label class="field-label">正文字体</label><select v-model="settings.readingFont" class="field"><option>Literata</option><option>Georgia</option><option>宋体</option></select></div><div><label class="field-label">字号</label><select v-model="settings.fontSize" class="field"><option value="17">17px</option><option value="19">19px</option><option value="21">21px</option></select></div><div><label class="field-label">行高</label><select v-model="settings.lineHeight" class="field"><option value="1.6">舒适</option><option value="1.7">宽松</option><option value="1.85">非常宽松</option></select></div></div>
        <label class="toggle-row"><span><strong>自动标记生词</strong><small>在精读正文中显示可点击的词汇下划线。</small></span><input v-model="settings.highlight" type="checkbox" /><i></i></label>
      </article>

      <article class="settings-card surface fade-in">
        <div class="card-title"><span><Bell :size="20" /></span><div><h2 class="serif">学习提醒</h2><p>保持稳定但不过度打扰。</p></div></div>
        <label class="toggle-row"><span><strong>每日复习提醒</strong><small>有到期卡牌时发送通知。</small></span><input v-model="settings.dailyReminder" type="checkbox" /><i></i></label>
        <label class="toggle-row"><span><strong>每周学习报告</strong><small>每周日汇总阅读与掌握情况。</small></span><input v-model="settings.weeklyReport" type="checkbox" /><i></i></label>
      </article>

      <article class="settings-card surface fade-in">
        <div class="card-title"><span><Languages :size="20" /></span><div><h2 class="serif">语言</h2><p>界面与默认学习语言。</p></div></div>
        <label class="field-label">界面语言</label><select v-model="settings.interfaceLanguage" class="field"><option>简体中文</option><option>English</option><option>日本語</option></select>
        <label class="field-label second">默认学习语言</label><select v-model="settings.learningLanguage" class="field"><option>英语</option><option>日语</option><option>法语</option><option>德语</option></select>
      </article>

      <article class="settings-card danger surface fade-in wide">
        <div class="card-title"><span><ShieldAlert :size="20" /></span><div><h2 class="serif">账号操作</h2><p>退出当前设备，或永久删除账号数据。</p></div></div>
        <div class="danger-actions"><button class="btn btn-secondary" @click="logout">退出登录</button><button class="delete-btn">删除账号</button></div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.settings-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 18px; }.settings-card { padding: 26px; }.settings-card.wide { grid-column: 1 / -1; }
.card-title { display: flex; align-items: flex-start; gap: 12px; padding-bottom: 20px; margin-bottom: 20px; border-bottom: 1px solid rgba(199,196,192,.7); }.card-title > span { width: 38px; height: 38px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 7px; color: var(--primary); background: var(--primary-soft); }.card-title h2 { margin: 0; color: var(--primary); font-size: 21px; }.card-title p { margin: 4px 0 0; color: var(--ink-muted); font-size: 12px; }
.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }.form-grid.three { grid-template-columns: repeat(3, 1fr); }.text-action { display: inline-flex; align-items: center; gap: 6px; margin-top: 18px; padding: 0; border: 0; color: var(--secondary); background: transparent; font-size: 13px; font-weight: 700; }.field { background: var(--surface-low); }
.toggle-row { position: relative; display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 58px; padding: 10px 0; cursor: pointer; }.toggle-row + .toggle-row { border-top: 1px solid rgba(199,196,192,.65); }.toggle-row span { display: grid; gap: 4px; }.toggle-row strong { font-size: 13px; }.toggle-row small { color: var(--ink-muted); font-size: 11px; }.toggle-row input { position: absolute; opacity: 0; }.toggle-row i { position: relative; width: 42px; height: 23px; flex: 0 0 auto; border-radius: 20px; background: var(--surface-high); transition: background .18s ease; }.toggle-row i::after { content: ''; position: absolute; width: 17px; height: 17px; left: 3px; top: 3px; border-radius: 50%; background: white; box-shadow: 0 1px 3px rgba(0,0,0,.2); transition: transform .18s ease; }.toggle-row input:checked + i { background: var(--primary); }.toggle-row input:checked + i::after { transform: translateX(19px); }
.second { margin-top: 18px; }.danger { border-color: #d7b8b8; }.danger .card-title > span { color: var(--error); background: #f3e3e3; }.danger-actions { display: flex; align-items: center; justify-content: space-between; }.delete-btn { border: 0; color: var(--error); background: transparent; font-size: 13px; font-weight: 700; }
@media (max-width: 760px) { .settings-grid { grid-template-columns: 1fr; }.settings-card.wide { grid-column: auto; }.form-grid, .form-grid.three { grid-template-columns: 1fr; }.danger-actions { align-items: stretch; flex-direction: column; gap: 12px; }.delete-btn { min-height: 42px; } }
</style>
