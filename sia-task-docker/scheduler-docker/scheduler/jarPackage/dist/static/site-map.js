(function () {
  window.API = {
    'CESHI_API_HOST': 'your-config-address:10615'
  }
  Object.freeze(window.API)
  Object.defineProperty(window, 'API', {
    configurable: false,
    writable: false
  })
})()