/**
 * API配置文件
 * 集中管理API相关的配置，避免敏感信息泄露
 */

// 环境配置
export const ENV = {
  development: {
    BASE_URL: process.env.VUE_APP_API_URL || 'http://localhost:9001',
    SOCIAL_URL: process.env.VUE_APP_SOCIAL_URL || 'http://localhost:9002',
    WS_URL: process.env.VUE_APP_WS_URL || 'ws://localhost:9001',
    API_VERSION: process.env.VUE_APP_API_VERSION || '/api/v1'
  },
  production: {
    BASE_URL: process.env.VUE_APP_API_URL || 'https://api.example.com',
    SOCIAL_URL: process.env.VUE_APP_SOCIAL_URL || 'https://social.example.com',
    WS_URL: process.env.VUE_APP_WS_URL || 'wss://api.example.com',
    API_VERSION: process.env.VUE_APP_API_VERSION || '/api/v1'
  },
  test: {
    BASE_URL: process.env.VUE_APP_TEST_API_URL || 'https://test-api.example.com',
    SOCIAL_URL: process.env.VUE_APP_SOCIAL_URL || 'https://test-social.example.com',
    WS_URL: process.env.VUE_APP_TEST_WS_URL || 'wss://test-api.example.com',
    API_VERSION: process.env.VUE_APP_API_VERSION || '/api/v1'
  }
};

// 当前环境
export const CURRENT_ENV = process.env.NODE_ENV || 'development';

// 基础配置
export const CONFIG = {
  // 请求超时时间（毫秒）
  TIME_OUT: 10000,
  
  // 最大重试次数
  MAX_RETRY: 2,
  
  // 是否开启调试日志
  DEBUG: CURRENT_ENV === 'development',
  
  // 是否使用加密传输
  USE_ENCRYPTION: CURRENT_ENV === 'production',
  
  // 是否自动刷新token
  AUTO_REFRESH_TOKEN: true,
  
  // Token相关
  TOKEN: {
    // 存储键名
    KEY: 'token',
    // 刷新Token键名
    REFRESH_KEY: 'refreshToken',
    // 过期时间（毫秒）
    EXPIRES_IN: 7 * 24 * 60 * 60 * 1000, // 7天
    // 刷新阈值（毫秒），提前多久刷新token
    REFRESH_THRESHOLD: 30 * 60 * 1000 // 30分钟
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

/**
 * 获取API基础URL（不含API版本）
 * @returns {string} API基础URL
 */
export const getBaseUrl = () => {
  return ENV[CURRENT_ENV].BASE_URL;
};

/**
 * 获取WebSocket URL
 * @returns {string} WebSocket URL
 */
export const getWebSocketUrl = () => {
  return ENV[CURRENT_ENV].WS_URL;
};

/**
 * 获取社交服务URL
 * @returns {string} 社交服务基础URL
 */
export const getSocialUrl = () => {
  return ENV[CURRENT_ENV].SOCIAL_URL;
};

/**
 * 获取API路径
 * @param {string} path API路径
 * @returns {string} 完整API路径
 */
export const getApiPath = (path) => {
  // 如果是完整URL，直接返回
  if (/^(http|https):\/\//.test(path)) {
    return path;
  }

  // 确保path以/开头
  const formattedPath = path.startsWith('/') ? path : `/${path}`;

  // 判断是否为社交服务API
  if (formattedPath.startsWith('/api/social/')) {
    return getSocialUrl() + formattedPath;
  }

  // 如果路径已经包含API版本前缀，则直接使用基础URL
  if (formattedPath.startsWith(ENV[CURRENT_ENV].API_VERSION)) {
    // 从路径中移除API版本前缀
    const actualPath = formattedPath.substring(ENV[CURRENT_ENV].API_VERSION.length);
    return getBaseUrl() + actualPath;
  }

  // 否则，直接添加到基础URL
  return getBaseUrl() + formattedPath;
};

export default {
  ENV,
  CURRENT_ENV,
  CONFIG,
  getBaseUrl,
  getSocialUrl,
  getWebSocketUrl,
  getApiPath
};