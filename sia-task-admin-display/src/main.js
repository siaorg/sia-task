import Vue from 'vue'
import App from './App'
import AppRouters from './common/routers/router.index.js'

// [ext] console provider inject
import ConsoleProvider from './common/services/console.provider.js'

// [ext] http/axios inject
import HttpPlugIn from './common/plugins/http.js'

// [ext] api inject
import ApiProvider from './common/services/api.provider.js'

// [ext] store inject
import StoreProvider from './common/services/store.provider.js'

// [ext] element-ui inject
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import 'element-ui/lib/theme-chalk/display.css'

// [ext] extent element-ui validate inject
import Validator from './common/plugins/validator.js'

// [ext] utils
import formatDate from './common/services/utils.js'

// [ext] echarts
import Echart from 'echarts'

Vue.prototype.$console = ConsoleProvider
Vue.prototype.$http = HttpPlugIn
Vue.prototype.$api = ApiProvider
Vue.use(ElementUI)
Vue.prototype.$validator = Validator
Vue.prototype.$echarts = Echart
Vue.prototype.$formatDate = formatDate
Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store: StoreProvider,
  router: AppRouters,
  template: '<App/>',
  components: { App }
})
