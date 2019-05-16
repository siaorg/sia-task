
import Vue from 'vue'
import CommonRouter from './common.router.js'
import FrameRouter from '../../frame/routers/frame.router.js'
// import HttpPlugIn from '../../common/plugins/http.js'
// import ApiProvider from '../../common/services/api.provider.js'
// import getUrlParamsTag from '../../common/services/utils.js'
import LoginRouter from '../../login/routers/login.router.js'

import VueRouter from 'vue-router'
Vue.use(VueRouter)

let routerArray = []

routerArray = routerArray.concat(CommonRouter.routers)
routerArray = routerArray.concat(FrameRouter.routers)
routerArray = routerArray.concat(LoginRouter.routers)

const appRouter = new VueRouter({
  mode: 'history',
  saveScrollPosition: true,
  routes: routerArray
})

appRouter.beforeEach((to, from, next) => {
  if (to.meta.auth) {
    if (sessionStorage.getItem('login') === 'show') {
      if (sessionStorage.getItem('isAdmin') !== 'admin' && to.path === '/dispatch-manage') {
        next({
          path: '/404'
        })
      } else {
        next()
      }
    } else {
      next({
        path: '/login',
        query: {redirect: to.fullPath}
      })
    }
  } else {
    next()
  }
  // next()
  document.title = to.meta.title
})

export default appRouter
