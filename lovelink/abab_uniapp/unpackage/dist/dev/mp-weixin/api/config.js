"use strict";
const ENV = {
  development: {
    BASE_URL: process.env.VUE_APP_API_URL || "http://localhost:9001",
    WS_URL: process.env.VUE_APP_WS_URL || "ws://localhost:9001",
    API_VERSION: process.env.VUE_APP_API_VERSION || "/api/v1"
  },
  production: {
    BASE_URL: process.env.VUE_APP_API_URL || "https://api.example.com",
    WS_URL: process.env.VUE_APP_WS_URL || "wss://api.example.com",
    API_VERSION: process.env.VUE_APP_API_VERSION || "/api/v1"
  },
  test: {
    BASE_URL: process.env.VUE_APP_TEST_API_URL || "https://test-api.example.com",
    WS_URL: process.env.VUE_APP_TEST_WS_URL || "wss://test-api.example.com",
    API_VERSION: process.env.VUE_APP_API_VERSION || "/api/v1"
  }
};
const CURRENT_ENV = "development";
const CONFIG = {
  // 请求超时时间（毫秒）
  TIME_OUT: 1e4,
  // 最大重试次数
  MAX_RETRY: 2,
  // 是否开启调试日志
  DEBUG: CURRENT_ENV === "development",
  // 是否使用加密传输
  USE_ENCRYPTION: CURRENT_ENV === "production",
  // 是否自动刷新token
  AUTO_REFRESH_TOKEN: true,
  // Token相关
  TOKEN: {
    // 存储键名
    KEY: "token",
    // 刷新Token键名
    REFRESH_KEY: "refreshToken",
    // 过期时间（毫秒）
    EXPIRES_IN: 7 * 24 * 60 * 60 * 1e3,
    // 7天
    // 刷新阈值（毫秒），提前多久刷新token
    REFRESH_THRESHOLD: 30 * 60 * 1e3
    // 30分钟
  },
  // 错误码映射
  ERROR_CODES: {
    SUCCESS: 200,
    UNAUTHORIZED: 401,
    NOT_LOGGED_IN: 402,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    SERVER_ERROR: 500
  }
};
const getBaseUrl = () => {
  return ENV[CURRENT_ENV].BASE_URL;
};
const getWebSocketUrl = () => {
  return ENV[CURRENT_ENV].WS_URL;
};
const getApiPath = (path) => {
  if (/^(http|https):\/\//.test(path)) {
    return path;
  }
  const formattedPath = path.startsWith("/") ? path : `/${path}`;
  if (formattedPath.startsWith(ENV[CURRENT_ENV].API_VERSION)) {
    const actualPath = formattedPath.substring(ENV[CURRENT_ENV].API_VERSION.length);
    return getBaseUrl() + actualPath;
  }
  return getBaseUrl() + formattedPath;
};
const config = {
  ENV,
  CURRENT_ENV,
  CONFIG,
  getBaseUrl,
  getWebSocketUrl,
  getApiPath
};
exports.config = config;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/config.js.map
