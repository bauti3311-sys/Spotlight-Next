export interface NativeRow { id: string; label: string; suffix: string; type: 'Application'; icon: string }
export interface NativeReply {
  v: 2; sessionId: string; requestId: string; type: string; status: 'ok' | 'error'
  data?: Record<string, unknown>; error?: { code: string; message: string }
}
const object = (value: unknown): value is Record<string, any> => !!value && typeof value === 'object' && !Array.isArray(value)
export function decodeReply(raw: unknown, sessionId: string): NativeReply | undefined {
  if (typeof raw !== 'string' || raw.length > 2 * 1024 * 1024 || new TextEncoder().encode(raw).length > 2 * 1024 * 1024) return
  try {
    const r = JSON.parse(raw)
    if (!object(r) || r.v !== 2 || r.sessionId !== sessionId || typeof r.requestId !== 'string'
      || !/^[1-9][0-9]{0,9}$/.test(r.requestId) || Number(r.requestId) > 2147483647 || typeof r.type !== 'string' || r.type.length > 40) return
    const allowed = ['v', 'sessionId', 'requestId', 'type', 'status', r.status === 'ok' ? 'data' : 'error']
    if (Object.keys(r).some(k => !allowed.includes(k)) || Object.keys(r).length !== 6) return
    if (r.status === 'error') {
      if (!object(r.error) || Object.keys(r.error).length !== 2 || typeof r.error.message !== 'string' || r.error.message.length > 256
        || !['INVALID_INPUT','UNSUPPORTED','PERMISSION_REQUIRED','STALE_RESULT','CANCELLED','TIMEOUT','UNAVAILABLE','INTERNAL'].includes(r.error.code)) return
    } else if (r.status !== 'ok' || !object(r.data)) return
    if (r.status === 'ok' && r.type === 'search.query') {
      const items = r.data.items
      if (!Array.isArray(items) || items.length > 100 || Object.keys(r.data).length !== 1) return
      const ids = new Set<string>()
      for (const row of items) {
        if (!object(row) || Object.keys(row).sort().join(',') !== 'icon,id,label,suffix,type'
          || typeof row.id !== 'string' || !/^[a-zA-Z0-9-]{1,64}$/.test(row.id) || ids.has(row.id)
          || typeof row.label !== 'string' || row.label.length > 256 || typeof row.suffix !== 'string' || row.suffix.length > 256
          || row.type !== 'Application' || typeof row.icon !== 'string' || row.icon.length > 16384
          || (row.icon !== '' && !/^data:image\/png;base64,[a-zA-Z0-9+/=]+$/.test(row.icon))) return
        ids.add(row.id)
      }
    } else if (r.status === 'ok') {
      if (r.type === 'result.inspect') {
        if (Object.keys(r.data).join(',') !== 'selected' || typeof r.data.selected !== 'string' || !/^[a-zA-Z0-9-]{1,64}$/.test(r.data.selected)) return
      } else if (r.type === 'action.execute') {
        if (Object.keys(r.data).join(',') !== 'state' || r.data.state !== 'requested') return
      } else if (!['search.cancel','overlay.close','overlay.recover'].includes(r.type) || Object.keys(r.data).length !== 0) return
    }
    return r as NativeReply
  } catch { return }
}
