import http from './http.js'

/**
 * 签到相关API
 */

// 用户签到
export function userSign() {
  return http.request({
    url: '/sign',
    method: 'POST',
    hideErrorToast: true // 让页面自己处理错误提示
  })
}

// 获取连续签到天数
export function getSignCount() {
  return http.request({
    url: '/sign/count',
    method: 'GET',
    hideErrorToast: true // 让页面自己处理错误提示
  })
}

// 获取本月签到记录（可选，用于显示签到日历）
export function getMonthSignRecord() {
  return http.request({
    url: '/sign/month',
    method: 'GET',
    hideErrorToast: true
  })
}

// 检查今天是否已签到
export function checkTodaySign() {
  return http.request({
    url: '/sign/today',
    method: 'GET',
    hideErrorToast: true
  })
}
