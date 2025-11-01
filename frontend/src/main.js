import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 引入Element UI
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
Vue.use(ElementUI)

// 引入全局样式
import './styles/index.css'

// 引入Mock数据（仅在开发环境且启用Mock时）
if (process.env.NODE_ENV === 'development' && process.env.VUE_APP_USE_MOCK === 'true') {
  require('./mock')
}

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')

