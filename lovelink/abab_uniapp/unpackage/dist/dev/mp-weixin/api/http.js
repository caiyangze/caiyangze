"use strict";
const common_vendor = require("../common/vendor.js");
const api_config = require("./config.js");
const CONFIG = api_config.config.CONFIG;
const pendingRequests = /* @__PURE__ */ new Map();
const generateRequestKey = (config) => {
  const { url, method, data } = config;
  return `${method}:${url}:${JSON.stringify(data || {})}`;
};
const removePendingRequest = (config) => {
  const requestKey = generateRequestKey(config);
  if (pendingRequests.has(requestKey)) {
    const abortController = pendingRequests.get(requestKey);
    abortController.abort();
    pendingRequests.delete(requestKey);
  }
};
const addPendingRequest = (config) => {
  const requestKey = generateRequestKey(config);
  if (config.cancelDuplicated && !pendingRequests.has(requestKey)) {
    const abortController = new AbortController();
    config.signal = abortController.signal;
    pendingRequests.set(requestKey, abortController);
  }
};
const removePendingRequestAfterCompletion = (config) => {
  const requestKey = generateRequestKey(config);
  pendingRequests.delete(requestKey);
};
const refreshToken = async function() {
  try {
    const refreshToken2 = common_vendor.index.getStorageSync(CONFIG.TOKEN.REFRESH_KEY);
    if (!refreshToken2) {
      throw new Error("No refresh token available");
    }
    const response = await common_vendor.index.request({
      url: api_config.config.getApiPath("/api/v1/auth/refresh"),
      method: "POST",
      header: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${refreshToken2}`
      },
      data: {}
    });
    if (response.statusCode === 200 && response.data.code === 200) {
      common_vendor.index.setStorageSync(CONFIG.TOKEN.KEY, response.data.data.token);
      common_vendor.index.setStorageSync(CONFIG.TOKEN.REFRESH_KEY, response.data.data.refreshToken);
      return response.data.data.token;
    } else {
      throw new Error("Failed to refresh token");
    }
  } catch (error) {
    common_vendor.index.__f__("error", "at api/http.js:85", "Token refresh failed:", error);
    common_vendor.index.removeStorageSync(CONFIG.TOKEN.KEY);
    common_vendor.index.removeStorageSync(CONFIG.TOKEN.REFRESH_KEY);
    common_vendor.index.reLaunch({
      url: "/pages/login/login"
    });
    return null;
  }
};
const requestInterceptor = (config) => {
  const token = common_vendor.index.getStorageSync(CONFIG.TOKEN.KEY);
  if (!config.header) {
    config.header = {};
  }
  if (!config.header["Content-Type"]) {
    config.header["Content-Type"] = "application/json";
  }
  if (token) {
    config.header["token"] = token;
    config.header["Authorization"] = `Bearer ${token}`;
    common_vendor.index.__f__("log", "at api/http.js:121", "设置请求头token:", token.substring(0, 20) + "...");
  } else {
    common_vendor.index.__f__("warn", "at api/http.js:123", "请求时没有token");
  }
  if (config.preventReplay) {
    const timestamp = Date.now();
    const nonce = Math.random().toString(36).substring(2, 15);
    config.header["X-Timestamp"] = timestamp;
    config.header["X-Nonce"] = nonce;
  }
  if (config.cancelDuplicated) {
    removePendingRequest(config);
    addPendingRequest(config);
  }
  if (CONFIG.USE_ENCRYPTION && config.data && typeof config.data === "object")
    ;
  return config;
};
const responseInterceptor = async (response, config) => {
  if (config.cancelDuplicated) {
    removePendingRequestAfterCompletion(config);
  }
  if (response.statusCode === 200) {
    const { code, message, data } = response.data;
    if (code === CONFIG.ERROR_CODES.SUCCESS) {
      return Promise.resolve(response.data);
    } else if (code === CONFIG.ERROR_CODES.UNAUTHORIZED) {
      if (CONFIG.AUTO_REFRESH_TOKEN && !config.isRefreshingToken) {
        const newToken = await refreshToken();
        if (newToken) {
          config.header["Authorization"] = `Bearer ${newToken}`;
          return request(config);
        }
      } else {
        common_vendor.index.removeStorageSync(CONFIG.TOKEN.KEY);
        common_vendor.index.removeStorageSync(CONFIG.TOKEN.REFRESH_KEY);
        common_vendor.index.reLaunch({
          url: "/pages/login/login"
        });
        return Promise.reject(response.data);
      }
    } else if (code === CONFIG.ERROR_CODES.NOT_LOGGED_IN) {
      common_vendor.index.removeStorageSync(CONFIG.TOKEN.KEY);
      common_vendor.index.removeStorageSync(CONFIG.TOKEN.REFRESH_KEY);
      common_vendor.index.reLaunch({
        url: "/pages/login/login"
      });
      return Promise.reject(response.data);
    } else if (code === CONFIG.ERROR_CODES.FORBIDDEN) {
      common_vendor.index.showToast({
        title: message || "权限不足",
        icon: "none"
      });
      return Promise.reject(response.data);
    } else {
      if (!config.hideErrorToast) {
        common_vendor.index.showToast({
          title: message || "请求失败",
          icon: "none"
        });
      }
      return Promise.reject(response.data);
    }
  } else {
    if (!config.hideErrorToast) {
      common_vendor.index.showToast({
        title: "网络请求失败",
        icon: "none"
      });
    }
    return Promise.reject(response);
  }
};
const request = (requestConfig) => {
  const requestWithConfig = requestInterceptor(requestConfig);
  let url;
  if (/^(http|https):\/\//.test(requestWithConfig.url)) {
    url = requestWithConfig.url;
  } else {
    if (requestWithConfig.url.startsWith("/api/v1/")) {
      const path = requestWithConfig.url.substring(7);
      url = `${api_config.config.getBaseUrl()}${path.startsWith("/") ? path : `/${path}`}`;
    } else {
      url = api_config.config.getApiPath(requestWithConfig.url);
    }
  }
  common_vendor.index.__f__("log", "at api/http.js:269", `[HTTP] ${requestWithConfig.method || "GET"} ${requestWithConfig.url} -> ${url}`, requestWithConfig.data || {});
  common_vendor.index.__f__("log", "at api/http.js:270", "请求配置:", requestWithConfig);
  common_vendor.index.__f__("log", "at api/http.js:271", "BASE_URL:", api_config.config.getBaseUrl());
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      url,
      data: requestWithConfig.data,
      method: requestWithConfig.method || "GET",
      header: requestWithConfig.header,
      timeout: requestWithConfig.timeout || CONFIG.TIME_OUT,
      success: async (res) => {
        try {
          common_vendor.index.__f__("log", "at api/http.js:284", `[HTTP Response] ${requestWithConfig.method || "GET"} ${url}`, res);
          const result = await responseInterceptor(res, requestWithConfig);
          resolve(result);
        } catch (error) {
          common_vendor.index.__f__("error", "at api/http.js:289", `[HTTP Error] ${requestWithConfig.method || "GET"} ${url}`, error);
          reject(error);
        }
      },
      fail: (err) => {
        if (requestWithConfig.cancelDuplicated) {
          removePendingRequestAfterCompletion(requestWithConfig);
        }
        common_vendor.index.__f__("error", "at api/http.js:299", `[HTTP Fail] ${requestWithConfig.method || "GET"} ${url}`, err);
        if (err.errMsg && err.errMsg.includes("timeout") && requestWithConfig.retryCount < CONFIG.MAX_RETRY) {
          requestWithConfig.retryCount = (requestWithConfig.retryCount || 0) + 1;
          common_vendor.index.__f__("log", "at api/http.js:305", `[HTTP Retry] ${requestWithConfig.method || "GET"} ${url} - Attempt ${requestWithConfig.retryCount}`);
          setTimeout(() => {
            resolve(request(requestWithConfig));
          }, 1e3 * requestWithConfig.retryCount);
          return;
        }
        if (!requestWithConfig.hideErrorToast) {
          common_vendor.index.showToast({
            title: "网络请求失败",
            icon: "none"
          });
        }
        reject(err);
      }
    });
  });
};
const get = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: "GET",
    retryCount: 0,
    ...config
  });
};
const post = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: "POST",
    retryCount: 0,
    ...config
  });
};
const put = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: "PUT",
    retryCount: 0,
    ...config
  });
};
const del = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: "DELETE",
    retryCount: 0,
    ...config
  });
};
const http = {
  request,
  get,
  post,
  put,
  del,
  delete: del,
  // 添加delete别名
  // 提供getApiPath方法，方便直接使用
  getApiPath: api_config.config.getApiPath,
  // 提供getBaseUrl方法，方便直接使用
  getBaseUrl: api_config.config.getBaseUrl,
  // 提供配置对象
  config: api_config.config
};
exports.http = http;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/http.js.map
