"use strict";
const common_vendor = require("../common/vendor.js");
function getBaseUrl() {
  {
    return "http://localhost:8084";
  }
}
function request(options) {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token");
    const baseUrl = getBaseUrl();
    const url = options.url.startsWith("http") ? options.url : baseUrl + options.url;
    const defaultHeaders = {
      "Content-Type": "application/json"
    };
    if (token) {
      defaultHeaders["Authorization"] = `Bearer ${token}`;
    }
    const headers = Object.assign(defaultHeaders, options.header || {});
    common_vendor.index.request({
      url,
      method: options.method || "GET",
      data: options.data || {},
      header: headers,
      timeout: options.timeout || 3e4,
      success: (res) => {
        common_vendor.index.__f__("log", "at utils/request.js:50", "请求成功:", {
          url,
          method: options.method || "GET",
          data: options.data,
          response: res.data
        });
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          const error = new Error(`HTTP ${res.statusCode}: ${res.data.message || "请求失败"}`);
          error.code = res.statusCode;
          error.data = res.data;
          reject(error);
        }
      },
      fail: (error) => {
        common_vendor.index.__f__("error", "at utils/request.js:68", "请求失败:", {
          url,
          method: options.method || "GET",
          data: options.data,
          error
        });
        let errorMessage = "网络请求失败";
        if (error.errMsg) {
          if (error.errMsg.includes("timeout")) {
            errorMessage = "请求超时，请检查网络连接";
          } else if (error.errMsg.includes("fail")) {
            errorMessage = "网络连接失败，请检查网络设置";
          }
        }
        const requestError = new Error(errorMessage);
        requestError.code = "NETWORK_ERROR";
        requestError.originalError = error;
        reject(requestError);
      }
    });
  });
}
exports.request = request;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/request.js.map
