import http from './http.js'

/**
 * 优惠券相关API
 */

// 获取优惠券列表
export function getVoucherList(params = {}) {
  return http.request({
    url: '/voucher/list',
    method: 'POST',
    data: Object.assign({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }, params),
    hideErrorToast: true // 让页面自己处理错误提示
  })
}

// 秒杀优惠券
export function seckillVoucher(voucherId) {
  return http.request({
    url: `/voucher/seckill?voucherId=${voucherId}`,
    method: 'POST',
    hideErrorToast: true // 禁用自动错误提示，让页面自己处理
  })
}

// 获取我的优惠券列表
export function getMyVoucherList(params = {}) {
  return http.request({
    url: '/voucher/getMyVoucherList',
    method: 'POST',
    data: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10,
      status: params.status, // 1-未使用，2-已使用，3-已过期
      ...params
    },
    hideErrorToast: true // 让页面自己处理错误提示
  })
}

// 获取我的可用优惠券（用于消费时选择）
export function getMyAvailableVouchers(params = {}) {
  return http.request({
    url: '/voucher/getMyVoucherList',
    method: 'POST',
    data: {
      pageNum: 1,
      pageSize: 100,
      status: 2, // 2-已支付（未使用）
      ...params
    },
    hideErrorToast: true
  })
}
