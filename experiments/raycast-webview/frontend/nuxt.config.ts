export default defineNuxtConfig({
  compatibilityDate: '2025-06-01',
  ssr: false,
  experimental: { appManifest: false },
  devtools: { enabled: false },
  future: { compatibilityVersion: 4 },
  app: { baseURL: '/' },
  css: ['~/assets/css/index.css'],
  ui: { fonts: false },
  icon: { provider: 'none', clientBundle: { scan: true, icons: ['lucide:search', 'lucide:x', 'lucide:check', 'lucide:arrow-left', 'lucide:arrow-up-right', 'lucide:info', 'lucide:rotate-ccw', 'lucide:chevron-right', 'lucide:chevron-down', 'lucide:loader-circle', 'simple-icons:raycast'] }, serverBundle: false },
  colorMode: { preference: 'dark', fallback: 'dark' },
  nitro: { preset: 'static', prerender: { routes: ['/'] } },
  modules: ['@nuxt/ui', '@vueuse/nuxt']
})
