"use strict";
const api_http = require("./http.js");
const BASE_PATH = "/wallet";
const getWalletInfo = () => {
  return api_http.http.get(`${BASE_PATH}/info`);
};
const getTransactions = (pageNum = 1, pageSize = 20, type = null) => {
  const params = { pageNum, pageSize };
  if (type !== null) {
    params.type = type;
  }
  return api_http.http.get(`${BASE_PATH}/transactions`, params);
};
const recharge = (data) => {
  return api_http.http.post(`${BASE_PATH}/recharge`, data);
};
const consume = (data) => {
  return api_http.http.post(`${BASE_PATH}/consume`, data);
};
const withdraw = (data) => {
  return api_http.http.post(`${BASE_PATH}/withdraw`, data);
};
const getRechargePackages = () => {
  return [
    { id: 1, coinAmount: 100, price: 10, bonus: 0, desc: "100虚拟币" },
    { id: 2, coinAmount: 500, price: 50, bonus: 50, desc: "500虚拟币+50赠送" },
    { id: 3, coinAmount: 1e3, price: 100, bonus: 150, desc: "1000虚拟币+150赠送" },
    { id: 4, coinAmount: 2e3, price: 200, bonus: 400, desc: "2000虚拟币+400赠送" },
    { id: 5, coinAmount: 5e3, price: 500, bonus: 1200, desc: "5000虚拟币+1200赠送" },
    { id: 6, coinAmount: 1e4, price: 1e3, bonus: 3e3, desc: "10000虚拟币+3000赠送" }
  ];
};
const formatAmount = (amount, unit = "元") => {
  if (amount === null || amount === void 0) {
    return `0${unit}`;
  }
  if (unit === "币") {
    return `${amount}${unit}`;
  }
  return `${parseFloat(amount).toFixed(2)}${unit}`;
};
exports.consume = consume;
exports.formatAmount = formatAmount;
exports.getRechargePackages = getRechargePackages;
exports.getTransactions = getTransactions;
exports.getWalletInfo = getWalletInfo;
exports.recharge = recharge;
exports.withdraw = withdraw;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/wallet.js.map
