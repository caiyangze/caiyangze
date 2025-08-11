/**
 * 地理位置相关API
 */
import http from './http';

// API基础路径
const BASE_PATH = '/api/social/location';

/**
 * 更新用户位置信息
 * @param {Object} locationData 位置数据
 * @param {Number} locationData.longitude 经度
 * @param {Number} locationData.latitude 纬度
 * @param {String} locationData.locationName 位置名称（可选）
 * @param {String} locationData.address 地址信息（可选）
 * @returns {Promise} 返回更新结果
 */
export const updateUserLocation = (locationData) => {
  return http.request({
    url: `${BASE_PATH}/update`,
    method: 'POST',
    data: locationData
  });
};

/**
 * 查询附近的用户
 * @param {Object} queryData 查询条件
 * @param {Number} queryData.longitude 经度
 * @param {Number} queryData.latitude 纬度
 * @param {Number} queryData.radius 搜索半径（公里）
 * @param {Number} queryData.limit 返回数量限制
 * @param {Number} queryData.gender 性别筛选（可选）
 * @param {Number} queryData.minAge 最小年龄（可选）
 * @param {Number} queryData.maxAge 最大年龄（可选）
 * @returns {Promise} 返回附近用户列表
 */
export const getNearbyUsers = (queryData) => {
  return http.request({
    url: `${BASE_PATH}/nearby`,
    method: 'POST',
    data: queryData
  });
};

/**
 * 获取所有用户列表（用于附近的人功能）
 * @param {Object} queryData 查询条件
 * @param {Number} queryData.pageNum 页码
 * @param {Number} queryData.pageSize 每页数量
 * @param {Number} queryData.userId 用户ID（可选，用于筛选特定用户）
 * @returns {Promise} 返回用户列表
 */
export const getAllUsers = (queryData) => {
  return http.request({
    url: '/loveSquare/list',
    method: 'POST',
    data: queryData
  });
};

/**
 * 获取用户当前位置信息
 * @returns {Promise} 返回用户位置信息
 */
export const getCurrentLocation = () => {
  return http.request({
    url: `${BASE_PATH}/current`,
    method: 'GET'
  });
};

/**
 * 发送招呼
 * @param {Object} data 招呼数据
 * @param {Number} data.toUserId 目标用户ID
 * @param {String} data.message 招呼消息
 * @returns {Promise} 返回发送结果
 */
export const sendGreeting = (data) => {
  return http.request({
    url: `${BASE_PATH}/greeting`,
    method: 'POST',
    data
  });
};

/**
 * 收藏用户
 * @param {Object} data 收藏数据
 * @param {Number} data.targetUserId 目标用户ID
 * @returns {Promise} 返回收藏结果
 */
export const favoriteUser = (data) => {
  return http.request({
    url: `${BASE_PATH}/favorite`,
    method: 'POST',
    data
  });
};

/**
 * 取消收藏用户
 * @param {Object} data 取消收藏数据
 * @param {Number} data.targetUserId 目标用户ID
 * @returns {Promise} 返回取消收藏结果
 */
export const unfavoriteUser = (data) => {
  return http.request({
    url: `${BASE_PATH}/unfavorite`,
    method: 'POST',
    data
  });
};

/**
 * 获取收藏的用户列表
 * @returns {Promise} 返回收藏用户列表
 */
export const getFavoriteUsers = () => {
  return http.request({
    url: `${BASE_PATH}/favorites`,
    method: 'GET'
  });
};

/**
 * 删除用户位置信息
 * @returns {Promise} 返回删除结果
 */
export const removeUserLocation = () => {
  return http.request({
    url: `${BASE_PATH}/remove`,
    method: 'DELETE'
  });
};

/**
 * 计算两点之间的距离
 * @param {Number} lon1 经度1
 * @param {Number} lat1 纬度1
 * @param {Number} lon2 经度2
 * @param {Number} lat2 纬度2
 * @returns {Promise} 返回距离（公里）
 */
export const calculateDistance = (lon1, lat1, lon2, lat2) => {
  return http.request({
    url: `${BASE_PATH}/distance`,
    method: 'GET',
    data: { lon1, lat1, lon2, lat2 }
  });
};

/**
 * 获取用户地理位置（使用浏览器API）
 * @param {Object} options 配置选项
 * @returns {Promise} 返回位置信息
 */
export const getUserGeolocation = (options = {}) => {
  return new Promise((resolve, reject) => {
    // 检查是否支持地理位置API
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持地理位置API'));
      return;
    }

    const defaultOptions = {
      enableHighAccuracy: true, // 启用高精度
      timeout: 10000, // 超时时间10秒
      maximumAge: 300000 // 缓存时间5分钟
    };

    const finalOptions = { ...defaultOptions, ...options };

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude, accuracy } = position.coords;
        resolve({
          latitude,
          longitude,
          accuracy,
          timestamp: position.timestamp
        });
      },
      (error) => {
        let errorMessage = '获取位置失败';
        switch (error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = '用户拒绝了位置请求';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage = '位置信息不可用';
            break;
          case error.TIMEOUT:
            errorMessage = '获取位置超时';
            break;
        }
        reject(new Error(errorMessage));
      },
      finalOptions
    );
  });
};

/**
 * 监听用户位置变化
 * @param {Function} successCallback 成功回调
 * @param {Function} errorCallback 错误回调
 * @param {Object} options 配置选项
 * @returns {Number} 监听器ID，用于取消监听
 */
export const watchUserPosition = (successCallback, errorCallback, options = {}) => {
  if (!navigator.geolocation) {
    errorCallback(new Error('浏览器不支持地理位置API'));
    return null;
  }

  const defaultOptions = {
    enableHighAccuracy: true,
    timeout: 10000,
    maximumAge: 60000 // 位置监听缓存时间1分钟
  };

  const finalOptions = { ...defaultOptions, ...options };

  return navigator.geolocation.watchPosition(
    (position) => {
      const { latitude, longitude, accuracy } = position.coords;
      successCallback({
        latitude,
        longitude,
        accuracy,
        timestamp: position.timestamp
      });
    },
    (error) => {
      let errorMessage = '监听位置失败';
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMessage = '用户拒绝了位置请求';
          break;
        case error.POSITION_UNAVAILABLE:
          errorMessage = '位置信息不可用';
          break;
        case error.TIMEOUT:
          errorMessage = '获取位置超时';
          break;
      }
      errorCallback(new Error(errorMessage));
    },
    finalOptions
  );
};

/**
 * 取消位置监听
 * @param {Number} watchId 监听器ID
 */
export const clearWatch = (watchId) => {
  if (navigator.geolocation && watchId) {
    navigator.geolocation.clearWatch(watchId);
  }
};

/**
 * 请求位置权限（仅在支持的浏览器中）
 * @returns {Promise} 返回权限状态
 */
export const requestLocationPermission = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.permissions) {
      reject(new Error('浏览器不支持权限API'));
      return;
    }

    navigator.permissions.query({ name: 'geolocation' }).then((result) => {
      resolve(result.state); // 'granted', 'denied', 'prompt'
    }).catch((error) => {
      reject(error);
    });
  });
};

export default {
  updateUserLocation,
  getNearbyUsers,
  getAllUsers,
  getCurrentLocation,
  removeUserLocation,
  calculateDistance,
  getUserGeolocation,
  watchUserPosition,
  clearWatch,
  requestLocationPermission
};
