"use strict";
const api_http = require("./http.js");
const BASE_PATH = "/VIP";
const getVipPackages = () => {
  return api_http.http.get(`${BASE_PATH}/packages`);
};
const getPayMethods = () => {
  return api_http.http.get(`${BASE_PATH}/payMethods`);
};
const createVipOrder = (data) => {
  return api_http.http.post(`${BASE_PATH}/createOrder`, data);
};
const processVipPayment = (data) => {
  return api_http.http.post(`${BASE_PATH}/pay`, data);
};
const getVipOrderDetail = (orderId) => {
  return api_http.http.get(`${BASE_PATH}/order/${orderId}`);
};
const getUserVipStatus = () => {
  return api_http.http.get(`${BASE_PATH}/status`);
};
exports.createVipOrder = createVipOrder;
exports.getPayMethods = getPayMethods;
exports.getUserVipStatus = getUserVipStatus;
exports.getVipOrderDetail = getVipOrderDetail;
exports.getVipPackages = getVipPackages;
exports.processVipPayment = processVipPayment;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/vip.js.map
