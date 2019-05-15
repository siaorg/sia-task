'use strict'
import axios from 'axios'
import ES6Promise from 'es6-promise'
import appRouters from '../routers/router.index.js'
ES6Promise.polyfill() // [ext] fix ie9 promise bug

axios.defaults.withCredentials = true

// [ext] axios request interceptors
axios.interceptors.request.use(
  config => {
    // config.headers['X-Requested-With'] = 'XMLHttpRequest'
    return config
  }
)

axios.interceptors.response.use(
  res => {
    return res
  },
  err => {
    if (err.message && err.message.indexOf('Network Error') > -1) {
      appRouters.replace({
        path: '/401'
      })
      return Promise.reject(err)
    }
  })

const http = {}
http.timeout = 60000 // 60s 1min

http.get = function (url, params) {
  let config = {
    method: 'GET',
    url: url,
    params: params, // params : { key : value}
    timeout: this.timeout,
    headers: {
      'Content-Type': 'application/xxxx-form; charset=UTF-8'
    },
    responseType: 'json'
  }
  return axios(config)
}

http.post = function (url, obj) {
  obj.jsessionid = JSON.parse(sessionStorage.getItem('jsessionid'))['jsessionid']
  let config = {
    method: 'POST',
    url: url,
    data: JSON.stringify(obj), // change obj to json obj
    timeout: this.timeout,
    headers: {
      'Content-Type': 'application/json; charset=UTF-8'
    },
    responseType: 'json'
  }
  return axios(config)
}

http.postNoObj = function (url, obj) {
  let config = {
    method: 'POST',
    url: url,
    data: obj, // change obj to string
    timeout: this.timeout,
    headers: {
      'Content-Type': 'application/json; charset=UTF-8'
    },
    responseType: 'json'
  }
  return axios(config)
}

export default http
