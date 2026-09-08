import {createApp} from 'vue'
import 'element-plus/dist/index.css'
import ElementPlus from 'element-plus'
import App from './App.vue'
import {router} from './router'
import * as ElIcons from '@element-plus/icons-vue'
import {createPinia} from "pinia";
import i18n from "@/i18n";

const pinia = createPinia()
const app = createApp(App)

// 统一注册Icon图标
for (const name in ElIcons){
    app.component(name,(ElIcons as any)[name])
}
app.use(router)
app.use(pinia)
app.use(ElementPlus)
app.use(i18n)
app.mount('#app')