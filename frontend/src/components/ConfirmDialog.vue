<script setup lang="ts">
import { AlertTriangle, Trash2, X } from 'lucide-vue-next'

withDefaults(defineProps<{
  visible: boolean
  title: string
  message: string
}>(), {
  visible: false,
})

const emit = defineEmits<{
  close: []
  confirm: []
}>()
</script>

<template>
  <Transition name="dialog">
    <div v-if="visible" class="modal-backdrop" @click.self="emit('close')">
      <section class="dialog-panel confirm-modal surface" role="alertdialog" aria-modal="true" aria-labelledby="confirm-dialog-title">
        <div class="modal-heading">
          <div>
            <p class="eyebrow">Confirm action</p>
            <h2 id="confirm-dialog-title" class="serif">{{ title }}</h2>
          </div>
          <button class="icon-btn" type="button" aria-label="关闭确认弹窗" title="关闭" @click="emit('close')"><X :size="18" /></button>
        </div>

        <div class="confirm-message">
          <span class="confirm-icon"><AlertTriangle :size="19" /></span>
          <p>{{ message }}</p>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="emit('close')">取消</button>
          <button class="btn confirm-button" type="button" @click="emit('confirm')"><Trash2 :size="16" />确认删除</button>
        </div>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.modal-backdrop { position: fixed; z-index: 70; inset: 0; display: grid; place-items: center; padding: 22px; background: rgba(27,28,28,.36); }
.confirm-modal { width: min(100%, 420px); padding: 28px 30px; }
.modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; }
.modal-heading h2 { margin: 0; color: var(--primary); font-size: 25px; }
.confirm-message { display: flex; align-items: center; gap: 12px; padding: 16px; margin-top: 22px; border: 1px solid #ead0cc; border-radius: 7px; color: #7e3d36; background: #fff7f5; }
.confirm-message p { margin: 0; font-size: 13px; line-height: 1.55; }
.confirm-icon { width: 30px; height: 30px; flex: 0 0 auto; display: grid; place-items: center; border-radius: 50%; color: var(--error); background: #f4deda; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }
.confirm-button { color: white; background: var(--error); }.confirm-button:hover { background: #8f3028; }
@media (max-width: 560px) { .confirm-modal { padding: 24px 20px; } }
</style>
