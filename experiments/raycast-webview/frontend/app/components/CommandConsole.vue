<script setup lang="ts">
const open = ref(true)
const native = useSpotlightNative()
const highlightedItem = ref<any>()
const actionsDropdownOpen = ref(false)
const selected = computed(() => highlightedItem.value?.value ?? native.rows.value[0])
const activate = () => { if (selected.value) native.execute(selected.value.id, 'open') }
const groups = computed(() => [{ id: 'apps', label: 'Applications', ignoreFilter: true,
  items: native.rows.value.map(row => ({ ...row, onSelect: () => {
    highlightedItem.value = { value: row }
    void native.inspect(row.id)
  } })) }])
const settingsMenu = [{ id: 'settings', items: [
  { label: 'Use Compose recovery', icon: 'i-lucide-rotate-ccw', onSelect: () => native.recover() },
  { label: 'Close Spotlight', icon: 'i-lucide-x', onSelect: () => native.close() }
]}]
const actionsDropdownItems = computed(() => selected.value ? [
  { label: 'Open Application', icon: 'i-lucide-arrow-up-right', onSelect: activate },
  { label: 'Application Info', icon: 'i-lucide-info', onSelect: () => native.execute(selected.value.id, 'info') }
] : [])
const handleHighlight = (payload: any) => {
  highlightedItem.value = payload
  if (payload?.value?.id) void native.inspect(payload.value.id)
}
const handleRightClick = (event: MouseEvent) => {
  event.preventDefault()
  if (selected.value) actionsDropdownOpen.value = true
}
function keyboard(event: KeyboardEvent) {
  const fromResults = (event.target as HTMLElement)?.closest?.('input[placeholder="Search for apps..."],[role="option"]')
  if (event.key === 'Escape') {
    event.preventDefault(); event.stopImmediatePropagation()
    if (actionsDropdownOpen.value) actionsDropdownOpen.value = false
    else native.close()
  } else if (event.key.toLowerCase() === 'k' && (event.ctrlKey || event.metaKey)) {
    event.preventDefault(); actionsDropdownOpen.value = !actionsDropdownOpen.value
  } else if (event.key === 'Tab' && !event.isComposing && actionsDropdownOpen.value) {
    event.preventDefault(); event.stopImmediatePropagation()
    document.activeElement?.dispatchEvent(new KeyboardEvent('keydown', { key: event.shiftKey ? 'ArrowUp' : 'ArrowDown', bubbles: true, cancelable: true }))
  } else if (event.key === 'Tab' && !event.shiftKey && !event.isComposing && !actionsDropdownOpen.value && selected.value && fromResults) {
    event.preventDefault(); event.stopImmediatePropagation(); actionsDropdownOpen.value = true
  } else if (event.key === 'Enter' && !event.isComposing && !actionsDropdownOpen.value && fromResults) {
    event.preventDefault(); event.stopImmediatePropagation(); activate()
  }
}
onMounted(() => document.addEventListener('keydown', keyboard, true))
onBeforeUnmount(() => document.removeEventListener('keydown', keyboard, true))
watch(native.rows, () => { highlightedItem.value = undefined })

const modalUi = {
  content: 'bg-default/70 backdrop-blur-xl border border-accented/70 max-w-2xl h-[min(25rem,calc(100dvh-16px))] flex flex-col shadow-raycast'
}

const commandPaletteUi = {
  root: 'flex flex-col h-full',
  label: 'text-muted font-medium',
  item: 'data-highlighted:not-data-disabled:before:bg-muted',
  content: 'flex-1 overflow-y-auto'
}

</script>

<template>
  <UModal v-model:open="open" :overlay="false" :dismissible="false" :ui="modalUi">
    <template #content>
      <UCommandPalette 
        :groups
        v-model:search-term="native.query.value"
        placeholder="Search for apps..."
        :ui="commandPaletteUi" 
        :fuse="{ resultLimit: 100, fuseOptions: { includeMatches: true } }"
        @highlight="handleHighlight"
      >
        <template #item="{ item }">
          <div class="relative flex items-center justify-between w-full p-1">
            <div class="absolute inset-0 -m-1" @contextmenu="handleRightClick" />
            <div class="flex items-center gap-2.5">
              <div class="flex items-center gap-2.5">
                <img :src="item.icon" class="size-5" alt="" />
                <span>{{ item.label }}</span>
              </div>
              <span class="text-muted font-medium">
                {{ item.suffix }}
              </span>
            </div>
            <span class="text-muted text-xs font-medium">
              {{ item.type }}
            </span>
          </div>
        </template>

        <template #empty><span role="status">{{ native.status.value }}</span></template>

        <template #footer>
          <CommandFooter
            v-model:actions-open="actionsDropdownOpen"
            :settings-menu
            :actions-items="actionsDropdownItems"
            :actions-disabled="!selected"
            @activate="activate"
          />
        </template>
      </UCommandPalette>
    </template>
  </UModal>
</template>

<style>
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: var(--ui-bg-accented);
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--ui-bg-accented);
}

::-webkit-scrollbar-thumb:active {
  background: var(--ui-bg-accented);
}

::-webkit-scrollbar-corner {
  background: transparent;
}

html {
  scrollbar-width: thin;
  scrollbar-color: var(--ui-bg-accented) transparent;
}
</style>
