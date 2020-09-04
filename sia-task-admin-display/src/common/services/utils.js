'use strict'
import moment from 'moment'
const formatDates = {}
formatDates.dateFormat = function (time) {
  if (time === null || time === 'null' || time === '') {
    return ''
  } else {
    return moment(new Date(time)).format('YYYY-MM-DD HH:mm:ss')
  }
}

/**
 * getDay(0) 获取当前日期
 * getDay(-3) 获取最近三天
 * @param day（当前日期最近几天的参数值）
 * @return 返回所求天数的日期
 */

formatDates.getDay = function (day) {
  let today = new Date()
  let targetdayMilliseconds = today.getTime() + 1000 * 60 * 60 * 24 * day
  today.setTime(targetdayMilliseconds)
  let tYear = today.getFullYear()
  let tMonth = today.getMonth()
  let tDate = today.getDate()
  tMonth = doHandleMonth(tMonth + 1)
  tDate = doHandleMonth(tDate)
  return tYear + '-' + tMonth + '-' + tDate
}

function doHandleMonth (month) {
  let m = month
  if (month.toString().length === 1) {
    m = '0' + month
  }
  return m
}

/**
 * 获取url参数
 * @param urlParams（所求参数的url）
 * @return 以对象的形式返回所有参数
 */

formatDates.getUrlParams = function (urlParams) {
  let theRequest = {}
  if (urlParams.indexOf('?') !== -1) {
    let str = urlParams.substr(urlParams.indexOf('?') + 1)
    let strs = str.split('&')
    for (let i = 0; i < strs.length; i++) {
      theRequest[strs[i].split('=')[0]] = unescape(strs[i].split('=')[1])
    }
  }
  return theRequest
}

export default formatDates
