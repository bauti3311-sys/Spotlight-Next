// Install before Vue mounts so Android's page-finished handshake cannot race onMounted.
export default defineNuxtPlugin(() => {
  window.addEventListener('message', event => {
    if (location.origin !== 'https://spotlight.local' || event.data !== 'spotlight-native-v1' || !event.ports[0]) return
    ;(window as any).__spotlightPendingPort = event.ports[0]
    window.dispatchEvent(new Event('spotlight-port-ready'))
  })
})
