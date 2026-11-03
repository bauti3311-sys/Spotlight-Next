import { decodeReply, type NativeReply, type NativeRow } from '../utils/native-protocol'

// Presentation only. All identities and effects are owned by the local Kotlin host.
export function useSpotlightNative() {
  const query = ref('')
  const rows = ref<NativeRow[]>([])
  const status = ref('Connecting to Android…')
  let port: MessagePort | undefined
  let sessionId = ''
  let serial = 0
  let revision = 0
  let executing = false
  let disposed = false
  let debounce: ReturnType<typeof setTimeout> | undefined
  const pending = new Map<string, { type: string; timer: ReturnType<typeof setTimeout>; resolve: (reply: NativeReply | undefined) => void }>()
  function clearPending() {
    for (const p of pending.values()) { clearTimeout(p.timer); p.resolve(undefined) }
    pending.clear()
  }
  function send(type: string, payload: Record<string, unknown> = {}): Promise<NativeReply | undefined> {
    if (!port || disposed || pending.size >= 64) return Promise.resolve(undefined)
    const requestId = String(++serial)
    return new Promise(resolve => {
      const timer = setTimeout(() => { pending.delete(requestId); resolve(undefined) }, 8000)
      pending.set(requestId, { type, timer, resolve })
      try { port!.postMessage(JSON.stringify({ v: 2, sessionId, requestId, type, payload })) }
      catch { clearTimeout(timer); pending.delete(requestId); resolve(undefined) }
    })
  }
  async function search() {
    const current = revision
    if (!port) { status.value = 'Android connection unavailable'; return }
    if (query.value.length > 256) { status.value = 'Search is limited to 256 characters'; return }
    status.value = 'Searching…'
    const reply = await send('search.query', { text: query.value, limit: 100 })
    if (disposed || current !== revision) return
    if (reply?.status === 'ok') {
      rows.value = reply.data!.items as NativeRow[]
      status.value = rows.value.length ? '' : 'No applications found'
    } else status.value = reply?.error?.message || 'Android did not respond. Search again.'
  }
  function receive() {
    const connection = (window as any).__spotlightPendingPort as { port: MessagePort; sessionId: string } | undefined
    if (location.origin !== 'https://spotlight.local' || !connection || disposed) return
    delete (window as any).__spotlightPendingPort
    clearTimeout(debounce); revision++; clearPending(); port?.close()
    port = connection.port; sessionId = connection.sessionId; serial = 0; executing = false; rows.value = []
    port.onmessage = event => {
      const reply = decodeReply(event.data, sessionId)
      if (!reply || disposed) return
      const request = pending.get(reply.requestId)
      if (!request || (reply.type !== request.type && reply.type !== 'bridge.error')) return
      if (reply.status === 'ok' && reply.type !== request.type) return
      clearTimeout(request.timer); pending.delete(reply.requestId); request.resolve(reply)
    }
    port.start(); void search()
  }
  function inspect(target: string) {
    return rows.value.some(row => row.id === target) ? send('result.inspect', { target }) : Promise.resolve(undefined)
  }
  async function execute(target: string, action: string) {
    if (executing || !['open', 'info'].includes(action) || !rows.value.some(row => row.id === target)) return
    const current = revision
    executing = true
    const inspected = await inspect(target)
    if (disposed || current !== revision) return
    if (inspected?.status !== 'ok' || inspected.data?.selected !== target) {
      executing = false; rows.value = []
      status.value = inspected?.error?.message || 'Result unavailable. Search again.'
      return
    }
    const reply = await send('action.execute', { target, action })
    if (disposed || current !== revision) return
    executing = false
    if (reply?.status !== 'ok') {
      rows.value = []
      status.value = reply?.error?.message || 'Action outcome unknown. Search again before retrying.'
    }
    // Never retry an effect after an error, timeout, disconnect or unknown outcome.
  }
  onMounted(() => {
    window.addEventListener('spotlight-port-ready', receive); receive()
    if (location.hostname !== 'spotlight.local') status.value = 'Android connection unavailable'
  })
  watch(query, () => {
    clearTimeout(debounce); revision++; rows.value = []; executing = false
    void send('search.cancel')
    debounce = setTimeout(() => { void search() }, 45)
  })
  onBeforeUnmount(() => {
    disposed = true; revision++; clearTimeout(debounce); clearPending()
    window.removeEventListener('spotlight-port-ready', receive); port?.close(); port = undefined
  })
  return { query, rows, status, inspect, execute, recover: () => send('overlay.recover'), close: () => send('overlay.close') }
}
