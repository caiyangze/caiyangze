"use strict";
const api_http = require("./http.js");
function userSign() {
  return api_http.http.request({
    url: "/sign",
    method: "POST",
    hideErrorToast: true
    // 让页面自己处理错误提示
  });
}
function getSignCount() {
  return api_http.http.request({
    url: "/sign/count",
    method: "GET",
    hideErrorToast: true
    // 让页面自己处理错误提示
  });
}
function getMonthSignRecord() {
  return api_http.http.request({
    url: "/sign/month",
    method: "GET",
    hideErrorToast: true
  });
}
function checkTodaySign() {
  return api_http.http.request({
    url: "/sign/today",
    method: "GET",
    hideErrorToast: true
  });
}
exports.checkTodaySign = checkTodaySign;
exports.getMonthSignRecord = getMonthSignRecord;
exports.getSignCount = getSignCount;
exports.userSign = userSign;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/sign.js.map
