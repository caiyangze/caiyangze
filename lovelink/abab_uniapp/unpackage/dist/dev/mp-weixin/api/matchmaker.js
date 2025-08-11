"use strict";
const common_vendor = require("../common/vendor.js");
require("./http.js");
const MATCHMAKER_BASE_URL = "http://localhost:9004";
const BASE_PATH = "/matchmaker/info";
const applyMatchmaker = (data) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.request({
      url: `${MATCHMAKER_BASE_URL}${BASE_PATH}/apply`,
      method: "POST",
      data,
      header: {
        "Content-Type": "application/json",
        "token": token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};
const checkApplicationStatus = () => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.request({
      url: `${MATCHMAKER_BASE_URL}${BASE_PATH}/checkApplicationStatus`,
      method: "GET",
      header: {
        "Content-Type": "application/json",
        "token": token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};
const getUserVerification = () => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    common_vendor.index.request({
      url: `${MATCHMAKER_BASE_URL}${BASE_PATH}/getUserVerification`,
      method: "GET",
      header: {
        "Content-Type": "application/json",
        "token": token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};
exports.applyMatchmaker = applyMatchmaker;
exports.checkApplicationStatus = checkApplicationStatus;
exports.getUserVerification = getUserVerification;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/matchmaker.js.map
