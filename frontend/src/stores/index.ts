import type { App } from 'vue'
import { createPinia } from 'pinia'

const pinia = createPinia()

export function setupStores(app: App) {
  app.use(pinia)
}

export { pinia }
