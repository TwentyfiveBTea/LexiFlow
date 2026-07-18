<script setup lang="ts">
import { BookOpenText } from 'lucide-vue-next'

withDefaults(defineProps<{ compact?: boolean }>(), { compact: false })
</script>

<template>
  <div class="brand" :class="{ compact }">
    <span class="brand-icon"><BookOpenText :size="compact ? 18 : 22" :stroke-width="1.8" /></span>
    <span v-if="!compact" class="brand-copy">
      <strong>LexiFlow</strong>
      <small>Intensive Study</small>
    </span>
  </div>
</template>

<style scoped>
.brand { display: flex; align-items: center; gap: 11px; min-width: 0; }
.brand-icon { width: 38px; height: 38px; display: grid; place-items: center; border: 1px solid var(--outline); border-radius: 50%; color: var(--primary); background: var(--surface-low); }
.brand-copy { min-width: 0; display: grid; gap: 1px; }
.brand-copy strong { font-family: 'Literata', Georgia, serif; color: var(--primary); font-size: 17px; line-height: 20px; }
.brand-copy small { color: var(--ink-muted); font-size: 10px; line-height: 14px; text-transform: uppercase; }
.compact .brand-icon { width: 34px; height: 34px; }
</style>
