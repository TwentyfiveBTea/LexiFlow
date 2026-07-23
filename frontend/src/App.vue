<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import { RouterView } from 'vue-router'
import { useRoute } from 'vue-router'
import { installDocumentLocalization, translateDocument, translateText } from '@/lib/i18n'
import { usePreferencesStore } from '@/stores/preferences'

const preferences = usePreferencesStore()
const route = useRoute()
let stopLocalization: (() => void) | undefined

function updateDocumentTitle() {
  const title = typeof route.meta.title === 'string' ? route.meta.title : ''
  document.title = title ? `${translateText(title, preferences.interfaceLanguage)} · LexiFlow` : 'LexiFlow'
}

onMounted(() => {
  stopLocalization = installDocumentLocalization(() => preferences.interfaceLanguage)
})

watch(() => preferences.interfaceLanguage, () => {
  translateDocument(preferences.interfaceLanguage)
  updateDocumentTitle()
})
watch(() => route.fullPath, updateDocumentTitle, { immediate: true })
onUnmounted(() => stopLocalization?.())
</script>

<template>
  <RouterView />
</template>
