// Android posts only to the exact local top-level origin; no privileged JS object.
export default defineNuxtPlugin(() => {
  window.addEventListener('message', event => {
    if (location.origin !== 'https://spotlight.local' || (event.source !== null && event.source !== window)
      || (event.origin && event.origin !== 'null' && event.origin !== location.origin) || event.ports.length !== 1) return
    try {
      const hello = JSON.parse(event.data)
      if (hello.v !== 2 || hello.type !== 'bridge.ready' || typeof hello.sessionId !== 'string'
        || !/^[a-zA-Z0-9-]{1,64}$/.test(hello.sessionId) || Object.keys(hello).length !== 3) return
      ;(window as any).__spotlightPendingPort?.port.close()
      ;(window as any).__spotlightPendingPort = { port: event.ports[0], sessionId: hello.sessionId }
      window.dispatchEvent(new Event('spotlight-port-ready'))
    } catch { /* Ignore foreign or malformed handshakes. */ }
  })
})
