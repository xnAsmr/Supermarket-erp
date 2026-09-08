import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './assets/styles/index.css'
import permission from './directives/permission'

const app = createApp(App)
const pinia = createPinia()

window.onerror = function (msg, source, lineno, colno, error) {
  if (msg && msg.toString().includes('startTime')) return false
}

window.addEventListener('unhandledrejection', function (e) {
  if (e.reason && e.reason.message && e.reason.message.includes('startTime')) {
    e.preventDefault()
  }
})

app.directive('permission', permission)
app.use(pinia)
app.use(router)
app.mount('#app')
