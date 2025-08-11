/**
 * 用户详情相关API
 */
import http from '../http';

// API基础路径
const BASE_PATH = '/loveSquare';
const SOCIAL_BASE_URL = 'http://localhost:9002'; // 社交服务基础URL

/**
 * 获取用户详情（从相亲广场获取）
 * @param {String|Number} userId 用户ID
 * @returns {Promise} 返回包含用户详情的Promise
 */
export const getUserDetail = (userId) => {
  return http.post(`${BASE_PATH}/list`, { 
    pageNum: 1, 
    pageSize: 100,
    userId: userId // 如果后端支持按用户ID筛选
  });
};

/**
 * 获取单个用户详情
 * @param {String|Number} userId 用户ID
 * @returns {Promise} 返回包含用户详情的Promise
 */
export const getUserDetailById = (userId) => {
  return http.get(`/user/${userId}/detail`);
};

/**
 * 获取用户相册
 * @param {String|Number} userId 用户ID
 * @param {Number} pageNum 页码
 * @param {Number} pageSize 每页数量
 * @returns {Promise} 返回包含用户相册的Promise
 */
export const getUserPhotos = (userId, pageNum = 1, pageSize = 10) => {
  return http.post(`${BASE_PATH}/findUserPhoto`, {
    userId: userId,
    pageNum: pageNum,
    pageSize: pageSize
  });
};

/**
 * 获取用户动态
 * @param {String|Number} userId 用户ID
 * @param {Number} pageNum 页码
 * @param {Number} pageSize 每页数量
 * @returns {Promise} 返回包含用户动态的Promise
 */
export const getUserMoments = (userId, pageNum = 1, pageSize = 10) => {
  return http.request({
    url: `${SOCIAL_BASE_URL}/api/social/moment/user/${userId}?pageNum=${pageNum}&pageSize=${pageSize}`,
    method: 'GET'
  });
};

/**
 * 点赞/取消点赞用户
 * @param {String|Number} userId 用户ID
 * @returns {Promise} 返回操作结果
 */
export const toggleUserLike = (userId) => {
  return http.post(`/user/${userId}/like`);
};

/**
 * 关注/取消关注用户
 * @param {String|Number} targetUserId 目标用户ID
 * @returns {Promise} 返回操作结果
 */
export const toggleUserFollow = (targetUserId) => {
  return http.post(`/follow/addAndCancel?followedUserId=${targetUserId}`);
};

/**
 * 检查是否已关注用户
 * @param {String|Number} targetUserId 目标用户ID
 * @returns {Promise} 返回关注状态
 */
export const checkUserFollow = (targetUserId) => {
  return http.post(`/follow/isFollow?followedUserId=${targetUserId}`);
};

export default {
  getUserDetail,
  getUserDetailById,
  getUserPhotos,
  getUserMoments,
  toggleUserLike,
  toggleUserFollow,
  checkUserFollow
};