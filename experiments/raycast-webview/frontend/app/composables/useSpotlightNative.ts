interface NativeRow { id: string; label: string; suffix: string; type: string; icon: string }
interface Reply { v: number; id: number; rows?: NativeRow[]; error?: string }

// Only an origin-checked MessagePort transferred by the Android host can service requests.
// Browser-only use is intentionally empty: installed apps are never simulated here.
export function useSpotlightNative() {
  const query = ref('')
  const rows = ref<NativeRow[]>([])
  const status = ref('Connecting to Android…')
  let port: MessagePort | undefined
  let serial = 0
  let latestSearch = 0
  let debounce: ReturnType<typeof setTimeout> | undefined
  function send(op: string, fields: Record<string, unknown> = {}) {
    const id = ++serial
    port?.postMessage(JSON.stringify({ v: 1, id, op, ...fields }))
    return id
  }
  function search() { latestSearch = send('search', { query: query.value }); status.value = 'Searching…' }
  function receive() {
    const pending = (window as any).__spotlightPendingPort as MessagePort | undefined
    if (location.origin !== 'https://spotlight.local' || !pending || port) return
    port = pending
    delete (window as any).__spotlightPendingPort
    port.onmessage = event => {
      try {
        const reply: Reply = JSON.parse(event.data)
        if (reply.v !== 1) return
        if (reply.id === latestSearch && Array.isArray(reply.rows)) {
          rows.value = reply.rows
          status.value = rows.value.length ? '' : 'No applications found'
        } else if (reply.error) status.value = reply.error
      } catch { status.value = 'Invalid response from Android' }
    }
    port.start()
    search()
  }
  onMounted(() => {
    window.addEventListener('spotlight-port-ready', receive)
    receive()
    if (location.hostname !== 'spotlight.local') status.value = 'Android connection unavailable'
  })
  watch(query, () => {
    clearTimeout(debounce)
    rows.value = []
    debounce = setTimeout(search, 45)
  })
  onBeforeUnmount(() => { clearTimeout(debounce); window.removeEventListener('spotlight-port-ready', receive); port?.close() })
  return { query, rows, status, execute: (target: string, action: string) => send('execute', { target, action }), recover: () => send('recover'), close: () => send('close') }
}
