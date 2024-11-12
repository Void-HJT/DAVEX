import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import pinia from '@/stores/index'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
// import VueMeta from 'vue-meta'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(pinia)
app.use(router)
// app.use(VueMeta)
app.use(ElementPlus)

app.mount('#app')
