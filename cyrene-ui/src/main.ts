import {createApp} from 'vue'
import 'element-plus/dist/index.css'
import ElementPlus from 'element-plus'
import App from './App.vue'
import {router} from './router'
import * as ElIcons from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import {createPinia} from "pinia";

const pinia = createPinia()
const app = createApp(App)

// 统一注册Icon图标
for (const name in ElIcons){
    app.component(name,(ElIcons as any)[name])
}
app.use(router)
app.use(pinia)
app.use(ElementPlus, {locale: zhCn})
app.mount('#app')
