"use strict";
const api_http = require("./http.js");
function getVoucherList(params = {}) {
  return api_http.http.request({
    url: "/voucher/list",
    method: "POST",
    data: Object.assign({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }, params),
    hideErrorToast: true
    // 让页面自己处理错误提示
  });
}
function seckillVoucher(voucherId) {
  return api_http.http.request({
    url: `/voucher/seckill?voucherId=${voucherId}`,
    method: "POST",
    hideErrorToast: true
    // 禁用自动错误提示，让页面自己处理
  });
}
function getMyVoucherList(params = {}) {
  return api_http.http.request({
    url: "/voucher/getMyVoucherList",
    method: "POST",
    data: {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10,
      status: params.status,
      // 1-未使用，2-已使用，3-已过期
      ...params
    },
    hideErrorToast: true
    // 让页面自己处理错误提示
  });
}
function getMyAvailableVouchers(params = {}) {
  return api_http.http.request({
    url: "/voucher/getMyVoucherList",
    method: "POST",
    data: {
      pageNum: 1,
      pageSize: 100,
      status: 2,
      // 2-已支付（未使用）
      ...params
    },
    hideErrorToast: true
  });
}
exports.getMyAvailableVouchers = getMyAvailableVouchers;
exports.getMyVoucherList = getMyVoucherList;
exports.getVoucherList = getVoucherList;
exports.seckillVoucher = seckillVoucher;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/voucher.js.map
