/**
 * HTTP请求封装
 */

import config from './config';

// 基础配置
const CONFIG = config.CONFIG;

// 请求计数器（用于防止重复请求）
const pendingRequests = new Map();

/**
 * 生成请求Key
 * @param {Object} config 请求配置
 */
const generateRequestKey = (config) => {
  const { url, method, data } = config;
  return `${method}:${url}:${JSON.stringify(data || {})}`;
};

/**
 * 取消重复请求
 * @param {Object} config 请求配置
 */
const removePendingRequest = (config) => {
  const requestKey = generateRequestKey(config);
  if (pendingRequests.has(requestKey)) {
    const abortController = pendingRequests.get(requestKey);
    abortController.abort();
    pendingRequests.delete(requestKey);
  }
};

/**
 * 添加请求到待处理列表
 * @param {Object} config 请求配置
 */
const addPendingRequest = (config) => {
  const requestKey = generateRequestKey(config);
  if (config.cancelDuplicated && !pendingRequests.has(requestKey)) {
    const abortController = new AbortController();
    config.signal = abortController.signal;
    pendingRequests.set(requestKey, abortController);
  }
};

/**
 * 请求完成后从待处理列表移除
 * @param {Object} config 请求配置
 */
const removePendingRequestAfterCompletion = (config) => {
  const requestKey = generateRequestKey(config);
  pendingRequests.delete(requestKey);
};

/**
 * 刷新Token
 */
const refreshToken = async function() {
  try {
    const refreshToken = uni.getStorageSync(CONFIG.TOKEN.REFRESH_KEY);
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    
    const response = await uni.request({
      url: config.getApiPath('/api/v1/auth/refresh'),
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${refreshToken}`
      },
      data: {}
    });
    
    if (response.statusCode === 200 && response.data.code === 200) {
      uni.setStorageSync(CONFIG.TOKEN.KEY, response.data.data.token);
      uni.setStorageSync(CONFIG.TOKEN.REFRESH_KEY, response.data.data.refreshToken);
      return response.data.data.token;
    } else {
      throw new Error('Failed to refresh token');
    }
  } catch (error) {
    console.error('Token refresh failed:', error);
    // 清除登录状态
    uni.removeStorageSync(CONFIG.TOKEN.KEY);
    uni.removeStorageSync(CONFIG.TOKEN.REFRESH_KEY);
    
    // 跳转到登录页
    uni.reLaunch({
      url: '/pages/login/login'
    });
    
    return null;
  }
};

/**
 * 请求拦截器
 * @param {Object} config 请求配置
 */
const requestInterceptor = (config) => {
  // 获取token
  const token = uni.getStorageSync(CONFIG.TOKEN.KEY);
  
  // 设置请求头
  if (!config.header) {
    config.header = {};
  }
  
  // 设置Content-Type
  if (!config.header['Content-Type']) {
    config.header['Content-Type'] = 'application/json';
  }
  
  // 添加token到请求头
  if (token) {
    config.header['token'] = token;
    config.header['Authorization'] = `Bearer ${token}`;
    console.log('设置请求头token:', token.substring(0, 20) + '...');
  } else {
    console.warn('请求时没有token，使用测试token');
    // 为了测试目的，添加一个默认的测试token
    const testToken = 'test-token-for-development';
    config.header['token'] = testToken;
    config.header['Authorization'] = `Bearer ${testToken}`;
  }
  
  // 添加防重放攻击参数
  if (config.preventReplay) {
    const timestamp = Date.now();
    const nonce = Math.random().toString(36).substring(2, 15);
    config.header['X-Timestamp'] = timestamp;
    config.header['X-Nonce'] = nonce;
    
    // 可以在这里添加签名逻辑
    // const signature = generateSignature(config.url, timestamp, nonce, token);
    // config.header['X-Signature'] = signature;
  }
  
  // 处理重复请求
  if (config.cancelDuplicated) {
    removePendingRequest(config);
    addPendingRequest(config);
  }
  
  // 数据加密 (生产环境)
  if (CONFIG.USE_ENCRYPTION && config.data && typeof config.data === 'object') {
    // 这里可以添加数据加密逻辑
    // config.data = encryptData(config.data);
    // config.header['X-Encrypted'] = '1';
  }
  
  return config;
};

/**
 * 响应拦截器
 * @param {Object} response 响应结果
 * @param {Object} config 请求配置
 */
const responseInterceptor = async (response, config) => {
  // 从待处理请求中移除
  if (config.cancelDuplicated) {
    removePendingRequestAfterCompletion(config);
  }
  
  // 请求成功
  if (response.statusCode === 200) {
    const { code, message, data } = response.data;
    
    // 业务成功
    if (code === CONFIG.ERROR_CODES.SUCCESS) {
      return Promise.resolve(response.data);
    } 
    // Token过期
    else if (code === CONFIG.ERROR_CODES.UNAUTHORIZED) {
      // 如果配置了自动刷新token
      if (CONFIG.AUTO_REFRESH_TOKEN && !config.isRefreshingToken) {
        const newToken = await refreshToken();
        if (newToken) {
          // 使用新token重试请求
          config.header['Authorization'] = `Bearer ${newToken}`;
          return request(config);
        }
      } else {
        // 清除本地token
        uni.removeStorageSync(CONFIG.TOKEN.KEY);
        uni.removeStorageSync(CONFIG.TOKEN.REFRESH_KEY);
        
        // 跳转到登录页
        uni.reLaunch({
          url: '/pages/login/login'
        });
        
        return Promise.reject(response.data);
      }
    }
    // 未登录
    else if (code === CONFIG.ERROR_CODES.NOT_LOGGED_IN) {
      // 清除本地token
      uni.removeStorageSync(CONFIG.TOKEN.KEY);
      uni.removeStorageSync(CONFIG.TOKEN.REFRESH_KEY);
      
      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/login/login'
      });
      
      return Promise.reject(response.data);
    } 
    // 权限不足
    else if (code === CONFIG.ERROR_CODES.FORBIDDEN) {
      uni.showToast({
        title: message || '权限不足',
        icon: 'none'
      });
      
      return Promise.reject(response.data);
    } 
    // 其他业务错误
    else {
      if (!config.hideErrorToast) {
        uni.showToast({
          title: message || '请求失败',
          icon: 'none'
        });
      }
      
      return Promise.reject(response.data);
    }
  } 
  // 请求失败
  else {
    if (!config.hideErrorToast) {
      uni.showToast({
        title: '网络请求失败',
        icon: 'none'
      });
    }
    
    return Promise.reject(response);
  }
};

/**
 * 发起请求
 * @param {Object} config 请求配置
 */
const request = (requestConfig) => {
  // 应用请求拦截器
  const requestWithConfig = requestInterceptor(requestConfig);
  
  // 完整URL (使用getApiPath处理路径)
  let url;
  if (/^(http|https):\/\//.test(requestWithConfig.url)) {
    // 如果是完整URL，直接使用
    url = requestWithConfig.url;
  } else {
    // 处理API路径
    if (requestWithConfig.url.startsWith('/api/v1/')) {
      // 如果路径以/api/v1/开头，去掉前缀
      const path = requestWithConfig.url.substring(7); // 去掉/api/v1前缀
      url = `${config.getBaseUrl()}${path.startsWith('/') ? path : `/${path}`}`;
    } else {
      // 使用导入的config.getApiPath方法处理其他路径
      url = config.getApiPath(requestWithConfig.url);
    }
  }
  
  // 调试日志
  console.log(`[HTTP] ${requestWithConfig.method || 'GET'} ${requestWithConfig.url} -> ${url}`, requestWithConfig.data || {});
  console.log('请求配置:', requestWithConfig);
  console.log('BASE_URL:', config.getBaseUrl());
  
  // 返回Promise
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      data: requestWithConfig.data,
      method: requestWithConfig.method || 'GET',
      header: requestWithConfig.header,
      timeout: requestWithConfig.timeout || CONFIG.TIME_OUT,
      success: async (res) => {
        try {
          // 调试日志
          console.log(`[HTTP Response] ${requestWithConfig.method || 'GET'} ${url}`, res);
          
          const result = await responseInterceptor(res, requestWithConfig);
          resolve(result);
        } catch (error) {
          console.error(`[HTTP Error] ${requestWithConfig.method || 'GET'} ${url}`, error);
          reject(error);
        }
      },
      fail: (err) => {
        // 从待处理请求中移除
        if (requestWithConfig.cancelDuplicated) {
          removePendingRequestAfterCompletion(requestWithConfig);
        }
        
        console.error(`[HTTP Fail] ${requestWithConfig.method || 'GET'} ${url}`, err);
        
        // 超时重试
        if (err.errMsg && err.errMsg.includes('timeout') && requestWithConfig.retryCount < CONFIG.MAX_RETRY) {
          requestWithConfig.retryCount = (requestWithConfig.retryCount || 0) + 1;
          
          console.log(`[HTTP Retry] ${requestWithConfig.method || 'GET'} ${url} - Attempt ${requestWithConfig.retryCount}`);
          
          setTimeout(() => {
            resolve(request(requestWithConfig));
          }, 1000 * requestWithConfig.retryCount); // 递增重试延迟
          return;
        }
        
        if (!requestWithConfig.hideErrorToast) {
          uni.showToast({
            title: '网络请求失败',
            icon: 'none'
          });
        }
        
        reject(err);
      }
    });
  });
};

/**
 * GET请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} config 请求配置
 */
const get = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: 'GET',
    retryCount: 0,
    ...config
  });
};

/**
 * POST请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} config 请求配置
 */
const post = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: 'POST',
    retryCount: 0,
    ...config
  });
};

/**
 * PUT请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} config 请求配置
 */
const put = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: 'PUT',
    retryCount: 0,
    ...config
  });
};

/**
 * DELETE请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} config 请求配置
 */
const del = (url, data = {}, config = {}) => {
  return request({
    url,
    data,
    method: 'DELETE',
    retryCount: 0,
    ...config
  });
};

// 导出HTTP请求方法，但不暴露BASE_URL
export default {
  request,
  get,
  post,
  put,
  del,
  delete: del, // 添加delete别名
  // 提供getApiPath方法，方便直接使用
  getApiPath: config.getApiPath,
  // 提供getBaseUrl方法，方便直接使用
  getBaseUrl: config.getBaseUrl,
  // 提供配置对象
  config
};