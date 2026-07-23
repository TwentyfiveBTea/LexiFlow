import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

export type InterfaceLanguage = 'zh-CN' | 'en' | 'ja'
export type ReadingFont = 'Literata' | 'Georgia' | '宋体'

export interface PreferencesSnapshot {
  readingFont: ReadingFont
  fontSize: string
  lineHeight: string
  highlight: boolean
  interfaceLanguage: InterfaceLanguage
}

const STORAGE_KEY = 'lexiflow.preferences'
const defaults: PreferencesSnapshot = {
  readingFont: 'Literata',
  fontSize: '19',
  lineHeight: '1.7',
  highlight: true,
  interfaceLanguage: 'zh-CN',
}

function readPreferences(): PreferencesSnapshot {
  try {
    const parsed = JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '{}') as Partial<PreferencesSnapshot>
    return {
      readingFont: parsed.readingFont === 'Georgia' || parsed.readingFont === '宋体' ? parsed.readingFont : defaults.readingFont,
      fontSize: parsed.fontSize === '17' || parsed.fontSize === '21' ? parsed.fontSize : defaults.fontSize,
      lineHeight: parsed.lineHeight === '1.6' || parsed.lineHeight === '1.85' ? parsed.lineHeight : defaults.lineHeight,
      highlight: parsed.highlight !== false,
      interfaceLanguage: parsed.interfaceLanguage === 'en' || parsed.interfaceLanguage === 'ja' ? parsed.interfaceLanguage : defaults.interfaceLanguage,
    }
  } catch {
    return { ...defaults }
  }
}

export const usePreferencesStore = defineStore('preferences', () => {
  const initial = readPreferences()
  const readingFont = ref<ReadingFont>(initial.readingFont)
  const fontSize = ref(initial.fontSize)
  const lineHeight = ref(initial.lineHeight)
  const highlight = ref(initial.highlight)
  const interfaceLanguage = ref<InterfaceLanguage>(initial.interfaceLanguage)
  const locale = computed(() => interfaceLanguage.value === 'zh-CN' ? 'zh-CN' : interfaceLanguage.value)

  function snapshot(): PreferencesSnapshot {
    return {
      readingFont: readingFont.value,
      fontSize: fontSize.value,
      lineHeight: lineHeight.value,
      highlight: highlight.value,
      interfaceLanguage: interfaceLanguage.value,
    }
  }

  function save(next: Partial<PreferencesSnapshot>) {
    if (next.readingFont) readingFont.value = next.readingFont
    if (next.fontSize) fontSize.value = next.fontSize
    if (next.lineHeight) lineHeight.value = next.lineHeight
    if (next.highlight !== undefined) highlight.value = next.highlight
    if (next.interfaceLanguage) interfaceLanguage.value = next.interfaceLanguage
    localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot()))
    document.documentElement.lang = locale.value
  }

  watch(interfaceLanguage, (value) => {
    document.documentElement.lang = value
  }, { immediate: true })

  return { readingFont, fontSize, lineHeight, highlight, interfaceLanguage, locale, snapshot, save }
})
