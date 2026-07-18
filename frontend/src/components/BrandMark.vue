<script setup lang="ts">
import logoUrl from '@/assets/lexiflow-logo.png'

withDefaults(defineProps<{ compact?: boolean }>(), { compact: false })
</script>

<template>
  <div class="brand" :class="{ compact }">
    <span class="brand-icon"><img class="brand-logo" :src="logoUrl" alt="" /></span>
    <span v-if="!compact" class="brand-copy">
      <strong>LexiFlow</strong>
    </span>
  </div>
</template>

<style scoped>
.brand { display: flex; align-items: center; gap: 9px; min-width: 0; }
.brand-icon { width: 34px; height: 34px; flex: 0 0 auto; display: grid; place-items: center; }
.brand-logo { width: 27px; height: 27px; display: block; object-fit: contain; }
.brand-copy { min-width: 0; }
.brand-copy strong { font-family: 'Literata', Georgia, serif; color: var(--primary); font-size: 18px; font-weight: 650; line-height: 22px; }
.compact .brand-icon { width: 32px; height: 32px; }
.compact .brand-logo { width: 28px; height: 28px; }
</style>
