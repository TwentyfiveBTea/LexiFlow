<script setup lang="ts">
import logoUrl from '@/assets/lexiflow-logo.png'

withDefaults(defineProps<{ compact?: boolean }>(), { compact: false })
</script>

<template>
  <div class="brand" :class="{ compact }">
    <span class="brand-icon"><img class="brand-logo" :src="logoUrl" alt="" /></span>
    <span v-if="!compact" class="brand-copy">
      <strong><span class="brand-cn">译流</span><span class="brand-en">LexiFlow</span></strong>
    </span>
  </div>
</template>

<style scoped>
.brand { display: flex; align-items: center; gap: 9px; min-width: 0; }
.brand-icon { width: 34px; height: 34px; flex: 0 0 auto; display: grid; place-items: center; }
.brand-logo { width: 27px; height: 27px; display: block; object-fit: contain; }
.brand-copy { min-width: 0; }
.brand-copy strong { display: inline-flex; align-items: baseline; gap: 6px; color: var(--primary); line-height: 22px; white-space: nowrap; }
.brand-cn { font-family: 'Songti SC', STSong, SimSun, serif; font-size: 17px; font-weight: 700; line-height: 1; transform: translateY(-1px); }
.brand-en { font-family: 'Literata', Georgia, serif; font-size: 18px; font-weight: 650; line-height: 1; }
.compact .brand-icon { width: 32px; height: 32px; }
.compact .brand-logo { width: 28px; height: 28px; }
</style>
