/**
 * 相亲广场相关API
 */
import http from './http';

// API基础路径
const BASE_PATH = '/loveSquare';

/**
 * 获取相亲广场用户列表
 * @param {Number} pageNum 当前页数
 * @param {Number} pageSize 每页条数
 * @returns {Promise} 返回包含用户列表的Promise
 */
export const getUserList = (pageNum = 1, pageSize = 10) => {
  return http.post(`${BASE_PATH}/list`, { pageNum, pageSize });
};

/**
 * 获取首页推荐用户列表（少量数据）
 * @param {Number} pageSize 获取条数
 * @returns {Promise} 返回包含用户列表的Promise
 */
export const getRecommendUsers = (pageSize = 4) => {
  return http.post(`${BASE_PATH}/list`, { pageNum: 1, pageSize });
};

export default {
  getUserList,
  getRecommendUsers
};