<script setup lang="ts">
import { Camera, Check, Eye, EyeOff, ImageUp, Languages, LockKeyhole, LogOut, MonitorCog, Save, ShieldAlert, UserRound, X } from 'lucide-vue-next'
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppSelect from '@/components/AppSelect.vue'
import { changePassword as requestPasswordChange, changeUserAvatar, changeUsername as requestUsernameChange } from '@/lib/api'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()
const saved = ref(false)
const usernameSaving = ref(false)
const usernameMessage = ref('')
const showAvatarDialog = ref(false)
const avatarFileInput = ref<HTMLInputElement | null>(null)
const avatarFile = ref<File | null>(null)
const avatarPreview = ref<string | null>(session.avatar)
const avatarPreviewUrl = ref('')
const avatarError = ref(false)
const avatarSaving = ref(false)
const avatarMessage = ref('')
const showPasswordDialog = ref(false)
const passwordSaving = ref(false)
const passwordMessage = ref('')
const passwordNotice = ref('')
const passwordVisibility = reactive({ old: false, next: false, confirm: false })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z]).{6,}$/
const settings = reactive({
  username: session.userName,
  readingFont: 'Literata',
  fontSize: '19',
  lineHeight: '1.7',
  highlight: true,
  interfaceLanguage: 'zh-CN',
})

const readingFontOptions = [
  { value: 'Literata', label: 'Literata' },
  { value: 'Georgia', label: 'Georgia' },
  { value: '宋体', label: '宋体' },
]
const fontSizeOptions = [
  { value: '17', label: '17px' },
  { value: '19', label: '19px' },
  { value: '21', label: '21px' },
]
const lineHeightOptions = [
  { value: '1.6', label: '舒适' },
  { value: '1.7', label: '宽松' },
  { value: '1.85', label: '非常宽松' },
]
const interfaceLanguageOptions = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en', label: 'English' },
  { value: 'ja', label: '日本語' },
]
const canSubmitPassword = computed(() => Boolean(
  passwordForm.oldPassword
  && passwordPattern.test(passwordForm.newPassword)
  && passwordForm.newPassword === passwordForm.confirmPassword,
))

onBeforeUnmount(clearAvatarPreviewUrl)

function save() {
  saved.value = true
  setTimeout(() => { saved.value = false }, 2200)
}

async function saveUsername() {
  const username = settings.username.trim()
  if (!username || usernameSaving.value) return

  usernameSaving.value = true
  usernameMessage.value = ''
  try {
    await requestUsernameChange(username)
    session.setUserName(username)
    usernameMessage.value = '用户名已更新。'
  } catch (error) {
    usernameMessage.value = error instanceof Error ? error.message : '用户名更新失败。'
  } finally {
    usernameSaving.value = false
  }
}

function clearAvatarPreviewUrl() {
  if (!avatarPreviewUrl.value) return
  URL.revokeObjectURL(avatarPreviewUrl.value)
  avatarPreviewUrl.value = ''
}

function openAvatarPicker() {
  clearAvatarPreviewUrl()
  if (avatarFileInput.value) avatarFileInput.value.value = ''
  avatarFile.value = null
  avatarPreview.value = session.avatar
  avatarError.value = false
  avatarMessage.value = ''
  showAvatarDialog.value = true
}

function closeAvatarPicker() {
  showAvatarDialog.value = false
  clearAvatarPreviewUrl()
}

function chooseAvatarFile() {
  avatarFileInput.value?.click()
}

function handleAvatarFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return

  clearAvatarPreviewUrl()
  avatarFile.value = file
  avatarPreviewUrl.value = URL.createObjectURL(file)
  avatarPreview.value = avatarPreviewUrl.value
  avatarError.value = false
  avatarMessage.value = ''
}

async function saveAvatar() {
  if (!avatarFile.value || avatarSaving.value) return

  avatarSaving.value = true
  avatarMessage.value = ''
  try {
    const avatar = await changeUserAvatar(avatarFile.value)
    session.setAvatar(avatar)
    avatarError.value = false
    closeAvatarPicker()
  } catch (error) {
    avatarMessage.value = error instanceof Error ? error.message : '头像更新失败。'
  } finally {
    avatarSaving.value = false
  }
}

function openPasswordDialog() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisibility.old = false
  passwordVisibility.next = false
  passwordVisibility.confirm = false
  passwordMessage.value = ''
  passwordNotice.value = ''
  showPasswordDialog.value = true
}

function closePasswordDialog() {
  showPasswordDialog.value = false
  passwordMessage.value = ''
}

async function savePassword() {
  if (passwordSaving.value) return
  if (!passwordForm.oldPassword) {
    passwordMessage.value = '请输入旧密码。'
    return
  }
  if (!passwordPattern.test(passwordForm.newPassword)) {
    passwordMessage.value = '新密码长度不少于 6 位，且必须包含大小写字母。'
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordMessage.value = '两次输入的新密码不一致。'
    return
  }

  passwordSaving.value = true
  passwordMessage.value = ''
  try {
    await requestPasswordChange({ ...passwordForm })
    closePasswordDialog()
    passwordNotice.value = '密码已更新。'
  } catch (error) {
    passwordMessage.value = error instanceof Error ? error.message : '密码更新失败。'
  } finally {
    passwordSaving.value = false
  }
}

async function logout() {
  session.signOut()
  await router.push('/login')
}
</script>

<template>
  <main class="page settings-page">
    <header class="page-header fade-in"><div><p class="eyebrow">Preferences</p><h1 class="page-title">应用设置</h1><p class="page-description">管理账号、阅读体验与学习语言。</p></div><button class="btn btn-primary" @click="save"><Check v-if="saved" :size="17" /><Save v-else :size="17" />{{ saved ? '已保存' : '保存更改' }}</button></header>

    <section class="settings-grid">
      <article class="settings-card account-card surface fade-in wide">
        <div class="card-title"><span><UserRound :size="20" /></span><div><h2 class="serif">账号信息</h2><p>用于登录和展示的个人资料。</p></div></div>

        <div class="account-profile">
          <button class="profile-avatar" type="button" aria-label="更换头像" title="更换头像" @click="openAvatarPicker">
            <img v-if="session.avatar && !avatarError" :src="session.avatar" alt="当前头像" @error="avatarError = true" />
            <UserRound v-else :size="30" />
            <span><Camera :size="14" /></span>
          </button>
          <div><strong class="serif">{{ session.userName }}</strong><button class="text-action avatar-action" type="button" @click="openAvatarPicker">更换头像</button></div>
        </div>

        <div class="account-fields">
          <div>
            <span class="field-label">电子邮件</span>
            <div class="readonly-field" :title="session.email">{{ session.email }}</div>
          </div>
          <div>
            <label class="field-label" for="settings-username">用户名</label>
            <div class="username-control"><input id="settings-username" v-model="settings.username" class="field" /><button class="btn btn-secondary" type="button" :disabled="usernameSaving || !settings.username.trim()" @click="saveUsername">{{ usernameSaving ? '更改中...' : '更改用户名' }}</button></div>
            <p v-if="usernameMessage" class="inline-message">{{ usernameMessage }}</p>
          </div>
        </div>
        <div class="password-action-row"><button class="text-action password-action" type="button" @click="openPasswordDialog"><LockKeyhole :size="15" />修改密码</button><span v-if="passwordNotice" class="success-message">{{ passwordNotice }}</span></div>
      </article>

      <article class="settings-card surface fade-in wide">
        <div class="card-title"><span><MonitorCog :size="20" /></span><div><h2 class="serif">阅读偏好</h2><p>调整长时间精读时的排版体验。</p></div></div>
        <div class="form-grid three">
          <div><span class="field-label">正文字体</span><AppSelect v-model="settings.readingFont" :options="readingFontOptions" label="选择正文字体" /></div>
          <div><span class="field-label">字号</span><AppSelect v-model="settings.fontSize" :options="fontSizeOptions" label="选择正文字号" /></div>
          <div><span class="field-label">行高</span><AppSelect v-model="settings.lineHeight" :options="lineHeightOptions" label="选择正文行高" /></div>
        </div>
        <label class="toggle-row"><span><strong>自动标记生词</strong><small>在精读正文中显示可点击的词汇下划线。</small></span><input v-model="settings.highlight" type="checkbox" /><i></i></label>
      </article>

      <article class="settings-card language-card surface fade-in wide">
        <div class="card-title"><span><Languages :size="20" /></span><div><h2 class="serif">语言</h2><p>界面显示语言</p></div></div>
        <div class="language-field">
          <div><span class="field-label">界面语言</span><AppSelect v-model="settings.interfaceLanguage" :options="interfaceLanguageOptions" label="选择界面语言" /></div>
        </div>
      </article>

      <article class="settings-card danger surface fade-in wide">
        <div class="card-title danger-title"><span><ShieldAlert :size="20" /></span><div><h2 class="serif">账号操作</h2><p>退出当前设备的登录状态。</p></div><button class="btn btn-secondary" type="button" @click="logout"><LogOut :size="16" />退出登录</button></div>
      </article>
    </section>

    <Transition name="dialog">
      <div v-if="showAvatarDialog" class="modal-backdrop" @click.self="closeAvatarPicker">
        <form class="dialog-panel avatar-modal surface" role="dialog" aria-modal="true" aria-labelledby="avatar-dialog-title" @submit.prevent="saveAvatar">
          <div class="modal-heading"><div><p class="eyebrow">Profile image</p><h2 id="avatar-dialog-title" class="serif">更换头像</h2></div><button class="icon-btn" type="button" aria-label="关闭头像弹窗" title="关闭" @click="closeAvatarPicker"><X :size="18" /></button></div>
          <button class="avatar-picker" type="button" @click="chooseAvatarFile">
            <span class="avatar-preview">
              <img v-if="avatarPreview && !avatarError" :src="avatarPreview" alt="头像预览" @error="avatarError = true" />
              <UserRound v-else :size="36" />
            </span>
            <span class="avatar-picker-copy"><strong>{{ avatarFile?.name ?? '选择图片' }}</strong><small>PNG、JPG 或 WebP</small></span>
            <ImageUp :size="19" />
          </button>
          <input ref="avatarFileInput" hidden type="file" accept="image/png,image/jpeg,image/webp" @change="handleAvatarFile" />
          <p v-if="avatarMessage" class="form-message error-message">{{ avatarMessage }}</p>
          <div class="modal-actions"><button class="btn btn-secondary" type="button" @click="closeAvatarPicker">取消</button><button class="btn btn-primary" type="submit" :disabled="!avatarFile || avatarSaving">{{ avatarSaving ? '上传中...' : '确认更换' }}</button></div>
        </form>
      </div>
    </Transition>

    <Transition name="dialog">
      <div v-if="showPasswordDialog" class="modal-backdrop" @click.self="closePasswordDialog">
        <form class="dialog-panel password-modal surface" role="dialog" aria-modal="true" aria-labelledby="password-dialog-title" @submit.prevent="savePassword">
          <div class="modal-heading"><div><p class="eyebrow">Account security</p><h2 id="password-dialog-title" class="serif">修改密码</h2></div><button class="icon-btn" type="button" aria-label="关闭修改密码弹窗" title="关闭" @click="closePasswordDialog"><X :size="18" /></button></div>
          <div class="password-fields">
            <div>
              <label class="field-label" for="old-password">旧密码</label>
              <div class="password-input"><input id="old-password" v-model="passwordForm.oldPassword" :type="passwordVisibility.old ? 'text' : 'password'" autocomplete="current-password" required /><button type="button" :aria-label="passwordVisibility.old ? '隐藏旧密码' : '显示旧密码'" :title="passwordVisibility.old ? '隐藏密码' : '显示密码'" @click="passwordVisibility.old = !passwordVisibility.old"><EyeOff v-if="passwordVisibility.old" :size="17" /><Eye v-else :size="17" /></button></div>
            </div>
            <div>
              <label class="field-label" for="new-password">新密码</label>
              <div class="password-input"><input id="new-password" v-model="passwordForm.newPassword" :type="passwordVisibility.next ? 'text' : 'password'" autocomplete="new-password" required /><button type="button" :aria-label="passwordVisibility.next ? '隐藏新密码' : '显示新密码'" :title="passwordVisibility.next ? '隐藏密码' : '显示密码'" @click="passwordVisibility.next = !passwordVisibility.next"><EyeOff v-if="passwordVisibility.next" :size="17" /><Eye v-else :size="17" /></button></div>
              <small class="password-hint">不少于 6 位，且必须包含大小写字母。</small>
            </div>
            <div>
              <label class="field-label" for="confirm-password">确认新密码</label>
              <div class="password-input"><input id="confirm-password" v-model="passwordForm.confirmPassword" :type="passwordVisibility.confirm ? 'text' : 'password'" autocomplete="new-password" required /><button type="button" :aria-label="passwordVisibility.confirm ? '隐藏确认密码' : '显示确认密码'" :title="passwordVisibility.confirm ? '隐藏密码' : '显示密码'" @click="passwordVisibility.confirm = !passwordVisibility.confirm"><EyeOff v-if="passwordVisibility.confirm" :size="17" /><Eye v-else :size="17" /></button></div>
            </div>
          </div>
          <p v-if="passwordMessage" class="form-message error-message">{{ passwordMessage }}</p>
          <div class="modal-actions"><button class="btn btn-secondary" type="button" @click="closePasswordDialog">取消</button><button class="btn btn-primary" type="submit" :disabled="!canSubmitPassword || passwordSaving">{{ passwordSaving ? '提交中...' : '确认修改' }}</button></div>
        </form>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.settings-grid { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 18px; }.settings-card { position: relative; padding: 26px; }.settings-card:focus-within { z-index: 20; }.settings-card.wide { grid-column: 1 / -1; }
.card-title { display: flex; align-items: flex-start; gap: 12px; padding-bottom: 20px; margin-bottom: 20px; border-bottom: 1px solid rgba(199,196,192,.7); }.card-title > span { width: 38px; height: 38px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 7px; color: var(--primary); background: var(--primary-soft); }.card-title h2 { margin: 0; color: var(--primary); font-size: 21px; }.card-title p { margin: 4px 0 0; color: var(--ink-muted); font-size: 12px; }
.account-profile { display: flex; align-items: center; gap: 16px; padding-bottom: 20px; margin-bottom: 20px; border-bottom: 1px solid rgba(199,196,192,.55); }.profile-avatar { position: relative; width: 70px; height: 70px; flex: 0 0 auto; display: grid; place-items: center; padding: 0; border: 1px solid var(--outline); border-radius: 50%; color: var(--secondary); background: var(--surface-high); }.profile-avatar img, .avatar-preview img { width: 100%; height: 100%; display: block; border-radius: inherit; object-fit: cover; }.profile-avatar > span { position: absolute; right: -2px; bottom: 1px; width: 25px; height: 25px; display: grid; place-items: center; border: 2px solid white; border-radius: 50%; color: white; background: var(--primary); }.account-profile > div { display: grid; align-items: start; gap: 5px; }.account-profile strong { color: var(--primary); font-size: 18px; }.avatar-action { margin: 0; }
.account-fields { display: grid; gap: 17px; }.readonly-field { width: 100%; height: 44px; display: flex; align-items: center; overflow: hidden; padding: 0 13px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); background: var(--surface-container); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }.username-control { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 10px; }.username-control .field { font-size: 13px; }.username-control .btn:disabled { opacity: .5; cursor: not-allowed; }.inline-message { margin: 7px 0 0; color: var(--success); font-size: 11px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }.form-grid.three { grid-template-columns: repeat(3, minmax(0, 1fr)); }.language-field { width: 100%; }.text-action { display: inline-flex; align-items: center; gap: 6px; padding: 0; border: 0; color: var(--secondary); background: transparent; font-size: 13px; font-weight: 700; }.password-action-row { display: flex; align-items: center; gap: 12px; margin-top: 18px; }.success-message { color: var(--success); font-size: 11px; }
.toggle-row { position: relative; display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 58px; padding: 16px 0 0; margin-top: 18px; border-top: 1px solid rgba(199,196,192,.65); cursor: pointer; }.toggle-row span { display: grid; gap: 4px; }.toggle-row strong { font-size: 13px; }.toggle-row small { color: var(--ink-muted); font-size: 11px; }.toggle-row input { position: absolute; opacity: 0; }.toggle-row i { position: relative; width: 42px; height: 23px; flex: 0 0 auto; border-radius: 20px; background: var(--surface-high); transition: background .18s ease; }.toggle-row i::after { content: ''; position: absolute; width: 17px; height: 17px; left: 3px; top: 3px; border-radius: 50%; background: white; box-shadow: 0 1px 3px rgba(0,0,0,.2); transition: transform .18s ease; }.toggle-row input:checked + i { background: var(--primary); }.toggle-row input:checked + i::after { transform: translateX(19px); }
.danger { border-color: #d7b8b8; }.danger .card-title > span { color: var(--error); background: #f3e3e3; }.danger-title { align-items: center; padding: 0; margin: 0; border-bottom: 0; }.danger-title > div { min-width: 0; flex: 1; }.danger-title .btn { flex: 0 0 auto; }
.modal-backdrop { position: fixed; z-index: 60; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.3); }.avatar-modal, .password-modal { width: min(100%, 440px); padding: 28px 30px; }.modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }.modal-heading h2 { margin: 0; color: var(--primary); font-size: 26px; }.avatar-picker { width: 100%; min-height: 104px; display: flex; align-items: center; gap: 14px; padding: 16px; margin-top: 24px; border: 1px dashed var(--outline); border-radius: 7px; color: var(--ink-muted); background: var(--surface-low); text-align: left; }.avatar-picker:hover, .avatar-picker:focus-visible { color: var(--primary); border-color: var(--primary); outline: none; }.avatar-preview { width: 64px; height: 64px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 50%; color: var(--secondary); background: var(--surface-high); }.avatar-picker-copy { min-width: 0; flex: 1; display: grid; gap: 5px; }.avatar-picker-copy strong { overflow: hidden; color: var(--ink); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }.avatar-picker-copy small { font-size: 11px; }.password-fields { display: grid; gap: 17px; margin-top: 24px; }.password-input { height: 44px; display: flex; align-items: center; border: 1px solid var(--outline); border-radius: 7px; background: var(--surface-low); transition: border-color .18s ease, box-shadow .18s ease; }.password-input:focus-within { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(50,78,88,.1); }.password-input input { min-width: 0; flex: 1; height: 100%; padding: 0 13px; border: 0; outline: 0; color: var(--ink); background: transparent; font-size: 13px; }.password-input button { width: 42px; height: 42px; display: grid; place-items: center; flex: 0 0 auto; border: 0; color: var(--ink-muted); background: transparent; }.password-input button:hover { color: var(--primary); }.password-hint { display: block; margin-top: 6px; color: var(--ink-muted); font-size: 10.5px; }.form-message { margin: 12px 0 0; font-size: 12px; }.error-message { color: var(--error); }.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }.modal-actions .btn:disabled { opacity: .45; cursor: not-allowed; }
@media (max-width: 760px) { .settings-grid { grid-template-columns: 1fr; }.settings-card.wide { grid-column: auto; }.form-grid, .form-grid.three { grid-template-columns: 1fr; }.username-control { grid-template-columns: 1fr; }.danger-title { align-items: stretch; flex-wrap: wrap; }.danger-title .btn { width: 100%; }.avatar-modal { padding: 24px 20px; } }
</style>
