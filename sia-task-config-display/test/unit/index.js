import Vue from 'vue'
import 'babel-polyfill'
// [ext] http/axios inject
import HttpPlugIn from '../../src/common/plugins/http.js'

// [ext] api inject
import ApiProvider from '../../src/common/services/api.provider.js'

// [ext] element-ui inject
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// [ext] extent element-ui validate inject
import Validator from '../../src/common/plugins/validator.js'

// [ext] theme provider inject
import ThemeProvider from '../../src/common/services/theme.provider.js'

Vue.prototype.$theme = ThemeProvider
Vue.prototype.$http = HttpPlugIn
Vue.prototype.$api = ApiProvider
Vue.use(ElementUI)
Vue.prototype.$validator = Validator

Vue.config.productionTip = false

// require all test files (files that ends with .spec.js)
const testsContext = require.context('./specs', true, /\.spec$/)
testsContext.keys().forEach(testsContext)

// require all vue files in src for coverage.
// you can also change this to match only the subset of files that
// you want coverage for.
const srcContext = require.context('../../src', false, /^\.vue$/)
srcContext.keys().forEach(srcContext)
