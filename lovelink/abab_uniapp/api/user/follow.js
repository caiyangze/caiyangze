/**
 * 关注相关API
 */
import http from '../http.js'
// 导入已有的关注功能，避免重复定义
import { toggleUserFollow, checkUserFollow } from './detail.js'

/**
 * 获取粉丝列表
 * @returns {Promise} 返回粉丝列表
 */
export const getFansList = () => {
  return http.post('/user/fensi')
}

/**
 * 获取关注列表
 * @returns {Promise} 返回关注列表
 */
export const getFollowingList = () => {
  return http.post('/user/guanzhu')
}

// 重新导出已有的关注功能
export { toggleUserFollow, checkUserFollow }

export default {
  getFansList,
  getFollowingList,
  toggleUserFollow,
  checkUserFollow
}
