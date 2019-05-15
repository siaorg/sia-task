
import Vue from 'vue'
import CommonRouter from './common.router.js'
import FrameRouter from '../../frame/routers/frame.router.js'
import HttpPlugIn from '../../common/plugins/http.js'
import ApiProvider from '../../common/services/api.provider.js'
import getUrlParamsTag from '../../common/services/utils.js'

import VueRouter from 'vue-router'
Vue.use(VueRouter)

let routerArray = []

routerArray = routerArray.concat(CommonRouter.routers)
routerArray = routerArray.concat(FrameRouter.routers)

const appRouter = new VueRouter({
  mode: 'history',
  saveScrollPosition: true,
  routes: routerArray
})

// 获取url参数，设置cookie
let urlParams = {
  // jsessionid: '1d853c70-a9f9-4925-b61d-29d47785733b'
}

appRouter.getSessionId = function () {
  let getUrlParams = Object.keys(getUrlParamsTag.getUrlParams(window.location.href))
  if (getUrlParams.length !== 0 && getUrlParams.indexOf('jsessionid') !== -1) {
    urlParams = getUrlParamsTag.getUrlParams(window.location.href)
    sessionStorage.setItem('jsessionid', JSON.stringify(getUrlParamsTag.getUrlParams(window.location.href)))
  } else if (sessionStorage.getItem('jsessionid') !== '' && sessionStorage.getItem('jsessionid') !== null) {
    urlParams = JSON.parse(sessionStorage.getItem('jsessionid'))
  }
  let expires = new Date()
  expires.setTime(expires.getTime() + 10 * 24 * 3600 * 1000)
  document.cookie = 'jsessionid=' + urlParams['jsessionid'] + ';expires=' + expires.toGMTString() + ';path=/'
}

appRouter.getSessionId()

appRouter.getRoleInfo = function () {
  return new Promise((resolve, reject) => {
    HttpPlugIn.get(ApiProvider.getApiAddress('/ui/auth?jsessionid=' + urlParams.jsessionid, 'CESHI_API_HOST')).then((res) => {
      resolve(res.data.data)
    }).catch((err) => {
      reject(err)
    })
  })
}

appRouter.beforeEach((to, from, next) => {
  // if (to.meta.auth) {
  //   if (Object.keys(urlParams).indexOf('jsessionid') !== -1 && Object.keys(urlParams).length !== 0) {
  //     appRouter.getRoleInfo().then((params) => {
  //       if (params === null) {
  //         next({
  //           path: '/401'
  //         })
  //         return false
  //       }
  //       sessionStorage.setItem('selectAuth', JSON.stringify(params))
  //       next()
  //       if (params.indexOf('admin') === -1 && to.path === '/dispatch-manage') {
  //         next({
  //           path: '/404'
  //         })
  //       } else {
  //         next()
  //       }
  //     })
  //   } else {
  //     next({
  //       path: '/401'
  //     })
  //   }
  // } else {
  //   next()
  // }
  next()
  document.title = to.meta.title
})

export default appRouter
