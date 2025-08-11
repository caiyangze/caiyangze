/**
 * VIP相关API接口
 */
import http from './http.js'

const BASE_PATH = '/VIP'

/**
 * 获取VIP套餐列表
 * @returns {Promise} VIP套餐列表
 */
export const getVipPackages = () => {
  return http.get(`${BASE_PATH}/packages`)
}

/**
 * 获取支付方式列表
 * @returns {Promise} 支付方式列表
 */
export const getPayMethods = () => {
  return http.get(`${BASE_PATH}/payMethods`)
}

/**
 * 创建VIP订单
 * @param {Object} data 订单数据
 * @param {Number} data.vipType VIP类型：1-月度，2-季度，3-年度
 * @returns {Promise} 订单信息
 */
export const createVipOrder = (data) => {
  return http.post(`${BASE_PATH}/createOrder`, data)
}

/**
 * 处理VIP支付
 * @param {Object} data 支付数据
 * @param {Number} data.orderId 订单ID
 * @param {Number} data.payType 支付方式：1-微信，2-支付宝，3-苹果支付，4-其他
 * @returns {Promise} 支付结果
 */
export const processVipPayment = (data) => {
  return http.post(`${BASE_PATH}/pay`, data)
}

/**
 * 查询VIP订单详情
 * @param {Number} orderId 订单ID
 * @returns {Promise} 订单详情
 */
export const getVipOrderDetail = (orderId) => {
  return http.get(`${BASE_PATH}/order/${orderId}`)
}

/**
 * 获取用户VIP状态
 * @returns {Promise} VIP状态信息
 */
export const getUserVipStatus = () => {
  return http.get(`${BASE_PATH}/status`)
}

/**
 * 获取用户VIP订单列表
 * @param {Object} params 查询参数
 * @param {Number} params.page 页码
 * @param {Number} params.size 每页数量
 * @param {Number} params.status 订单状态（可选）
 * @returns {Promise} 订单列表
 */
export const getUserVipOrders = (params = {}) => {
  return http.get(`${BASE_PATH}/orders`, params)
}

export default {
  getVipPackages,
  getPayMethods,
  createVipOrder,
  processVipPayment,
  getVipOrderDetail,
  getUserVipStatus,
  getUserVipOrders
}
