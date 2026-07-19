<script setup lang="ts">
import { Check, ChevronDown } from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'
import { computed, ref } from 'vue'
import type { Component } from 'vue'

interface SelectOption {
  value: string
  label: string
}

const props = withDefaults(defineProps<{
  modelValue: string
  options: SelectOption[]
  label: string
  placeholder?: string
  icon?: Component
  columns?: number
  menuWidth?: string
  align?: 'left' | 'right'
}>(), {
  placeholder: '请选择',
  icon: undefined,
  columns: 1,
  menuWidth: '100%',
  align: 'left',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const open = ref(false)
const selectArea = ref<HTMLElement | null>(null)
const selectedLabel = computed(() => props.options.find((item) => item.value === props.modelValue)?.label ?? props.placeholder)
const menuStyle = computed(() => ({
  width: props.menuWidth,
  gridTemplateColumns: `repeat(${props.columns}, minmax(0, 1fr))`,
  left: props.align === 'left' ? '0' : 'auto',
  right: props.align === 'right' ? '0' : 'auto',
}))

onClickOutside(selectArea, () => {
  open.value = false
})

function select(value: string) {
  emit('update:modelValue', value)
  open.value = false
}
</script>

<template>
  <div ref="selectArea" class="app-select" :class="{ open }">
    <button class="select-trigger" type="button" aria-haspopup="listbox" :aria-label="label" :aria-expanded="open" @click="open = !open" @keydown.esc="open = false">
      <component :is="icon" v-if="icon" :size="16" />
      <span>{{ selectedLabel }}</span>
      <ChevronDown :size="14" :class="{ rotated: open }" />
    </button>
    <Transition name="select-menu">
      <div v-if="open" class="select-menu surface" role="listbox" :aria-label="label" :style="menuStyle">
        <button v-for="item in options" :key="item.value" type="button" role="option" :aria-selected="modelValue === item.value" :class="{ active: modelValue === item.value }" @click.stop="select(item.value)">
          <span>{{ item.label }}</span>
          <Check v-if="modelValue === item.value" :size="14" />
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.app-select { position: relative; z-index: 0; width: 100%; height: 44px; }.app-select.open { z-index: 80; }
.select-trigger { width: 100%; height: 100%; display: flex; align-items: center; gap: 7px; padding: 0 11px; border: 1px solid var(--outline); border-radius: 7px; color: var(--ink-muted); background: var(--surface-lowest); font-size: 12.5px; }
.select-trigger:hover, .select-trigger:focus-visible, .app-select:has(.select-menu) .select-trigger { color: var(--primary); border-color: var(--primary); outline: none; }
.select-trigger span { min-width: 0; flex: 1; overflow: hidden; color: var(--ink); text-align: left; text-overflow: ellipsis; white-space: nowrap; }
.select-trigger svg:last-child { flex: 0 0 auto; transition: transform .18s ease; }.select-trigger svg.rotated { transform: rotate(180deg); }
.select-menu { position: absolute; z-index: 50; top: calc(100% + 8px); display: grid; gap: 3px; padding: 7px; box-shadow: 0 14px 34px rgba(45,45,45,.13), 0 2px 8px rgba(45,45,45,.05); }
.select-menu button { min-height: 34px; display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 0 10px; border: 0; border-radius: 5px; color: var(--ink-muted); background: transparent; font-size: 12px; text-align: left; }
.select-menu button:hover, .select-menu button:focus-visible { color: var(--primary); background: var(--surface-low); outline: none; }.select-menu button.active { color: #5d3b1e; background: var(--secondary-soft); font-weight: 700; }
.select-menu-enter-active { transition: opacity .16s ease-out, transform .2s cubic-bezier(.22, 1, .36, 1); transform-origin: top; }.select-menu-leave-active { transition: opacity .12s ease-in, transform .15s ease-in; transform-origin: top; }.select-menu-enter-from, .select-menu-leave-to { opacity: 0; transform: translateY(-4px) scale(.985); }
</style>
