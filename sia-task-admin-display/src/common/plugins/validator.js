'use strict'
const validator = {}

validator.required = function (tips) {
  return {required: true, message: tips, trigger: 'blur'}
}

export default validator
