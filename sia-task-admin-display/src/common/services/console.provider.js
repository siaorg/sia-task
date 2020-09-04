'use strict'
const consoleProvider = {}
consoleProvider.devMode = process.env.NODE_ENV === 'production'
consoleProvider.log = function (key, params) {
  if (!this.devMode) {
    if (params) {
      console.log(key, params)
    } else {
      console.log(key)
    }
  }
}
export default consoleProvider
