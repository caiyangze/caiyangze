/**
 * 红娘端管理API
 */
import http from './http'

// 红娘端基础URL
const MATCHMAKER_BASE_URL = 'http://localhost:9004'

/**
 * 获取红娘的申请列表
 * @param {Number} status 申请状态 (可选)
 * @returns {Promise} 返回申请列表的Promise
 */
export const getMatchmakerRequests = (status = null) => {
  const url = `${MATCHMAKER_BASE_URL}/matchmaker/requests/list`
  const params = status !== null ? { status } : {}
  
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    
    if (!token) {
      reject(new Error('未登录'))
      return
    }
    
    uni.request({
      url: url,
      method: 'GET',
      data: params,
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        console.error('获取申请列表失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 处理牵线申请（接受或拒绝）
 * @param {Number} requestId 申请ID
 * @param {Number} action 操作类型 1-接受，2-拒绝
 * @param {String} rejectReason 拒绝原因（拒绝时必填）
 * @returns {Promise} 返回处理结果的Promise
 */
export const handleMatchmakingRequest = (requestId, action, rejectReason = null) => {
  const url = `${MATCHMAKER_BASE_URL}/matchmaker/requests/handle`
  
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    
    if (!token) {
      reject(new Error('未登录'))
      return
    }
    
    const data = {
      requestId: requestId,
      action: action
    }
    
    if (action === 2 && rejectReason) {
      data.rejectReason = rejectReason
    }
    
    console.log('处理申请请求 - URL:', url)
    console.log('处理申请请求 - Data:', data)
    console.log('处理申请请求 - Token:', token)
    
    uni.request({
      url: url,
      method: 'POST',
      data: data,
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        console.log('处理申请响应:', res)
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(new Error(`请求失败: ${res.statusCode} - ${res.data?.message || '未知错误'}`))
        }
      },
      fail: (err) => {
        console.error('处理申请请求失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 获取申请详情
 * @param {Number} requestId 申请ID
 * @returns {Promise} 返回申请详情的Promise
 */
export const getRequestDetail = (requestId) => {
  const url = `${MATCHMAKER_BASE_URL}/matchmaker/requests/detail/${requestId}`
  
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    
    if (!token) {
      reject(new Error('未登录'))
      return
    }
    
    uni.request({
      url: url,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        console.error('获取申请详情失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 获取待处理申请数量
 * @returns {Promise} 返回待处理申请数量的Promise
 */
export const getPendingRequestCount = () => {
  const url = `${MATCHMAKER_BASE_URL}/matchmaker/requests/pending-count`
  
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    
    if (!token) {
      reject(new Error('未登录'))
      return
    }
    
    uni.request({
      url: url,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        console.error('获取待处理数量失败:', err)
        reject(err)
      }
    })
  })
}
