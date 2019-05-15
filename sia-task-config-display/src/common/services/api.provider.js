'use strict'
const apiProvider = {}

apiProvider.apiConfig = {}

// [ext] if NODE_ENV is production , load the static site-map.js
apiProvider.load = function () {
  if (process.env.NODE_ENV === 'production') {
    apiProvider.apiConfig['CESHI_API_HOST'] = window.API.CESHI_API_HOST// [ext] reference from /static/site-map.js
    apiProvider.apiConfig['CESHI_API_HOST_LOG'] = window.API.CESHI_API_HOST_LOG// [ext] reference from /static/site-map.js
  } else {
    apiProvider.apiConfig['CESHI_API_HOST'] = process.env.CESHI_API_HOST
    apiProvider.apiConfig['CESHI_API_HOST_LOG'] = process.env.CESHI_API_HOST_LOG
  }
}
apiProvider.load()
// apiProvider.apiConfig['CESHI_API_HOST'] = process.env.CESHI_API_HOST
apiProvider.mapModuleRoute = function (module = 'CESHI_API_HOST') {
  if (module) {
    if (module === 'CESHI_API_HOST') {
      return location.protocol + '//' + apiProvider.apiConfig['CESHI_API_HOST']
    }
    if (module === 'CESHI_API_HOST_LOG') {
      return location.protocol + '//' + apiProvider.apiConfig['CESHI_API_HOST_LOG']
    }
  }
}

apiProvider.getApiAddress = function (url, module) {
  let apiAddress = ''
  if (url) {
    if (url.indexOf('/') === 0) {
      apiAddress = this.mapModuleRoute(module) + url
    } else {
      apiAddress = this.mapModuleRoute(module) + '/' + url
    }
  }
  return apiAddress
}

export default apiProvider
