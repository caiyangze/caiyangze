"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const api_http = require("./http.js");
const BASE_PATH = "/loveSquare";
const getUserList = (pageNum = 1, pageSize = 10) => {
  return api_http.http.post(`${BASE_PATH}/list`, { pageNum, pageSize });
};
const getRecommendUsers = (pageSize = 4) => {
  return api_http.http.post(`${BASE_PATH}/list`, { pageNum: 1, pageSize });
};
const square = {
  getUserList,
  getRecommendUsers
};
exports.default = square;
exports.getRecommendUsers = getRecommendUsers;
exports.getUserList = getUserList;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/square.js.map
