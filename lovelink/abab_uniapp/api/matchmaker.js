import http from './http'

// 红娘服务基础URL
const MATCHMAKER_BASE_URL = 'http://localhost:9004';
const USER_BASE_URL = 'http://localhost:9001';
const BASE_PATH = '/matchmaker/info'
const REQUEST_PATH = '/user/matchmaking'

/**
 * 获取红娘列表
 * @returns {Promise} 返回红娘列表的Promise
 */
export const getMatchmakerList = () => {
  return http.post(`${BASE_PATH}/list`)
}

/**
 * 获取红娘详情
 * @param {Number} matchmakerId 红娘ID
 * @returns {Promise} 返回红娘详情的Promise
 */
export const getMatchmakerDetail = (matchmakerId) => {
  return http.post(`${BASE_PATH}/detail`, { matchmakerId })
}

/**
 * 申请成为红娘
 * @param {Object} data 申请信息
 * @param {String} data.realName 真实姓名
 * @param {String} data.phone 联系电话
 * @param {String} data.idCardNo 身份证号
 * @param {String} data.idCardFront 身份证正面照片URL
 * @param {String} data.idCardBack 身份证背面照片URL
 * @param {String} data.serviceArea 期望服务区域
 * @param {String} data.introduction 个人介绍
 * @param {String} data.experience 相关经验
 * @returns {Promise} 返回申请结果的Promise
 */
export const applyMatchmaker = (data) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${MATCHMAKER_BASE_URL}${BASE_PATH}/apply`,
      method: 'POST',
      data: data,
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 检查用户是否已申请红娘
 * @returns {Promise} 返回申请状态的Promise
 */
export const checkApplicationStatus = () => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${MATCHMAKER_BASE_URL}${BASE_PATH}/checkApplicationStatus`,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 获取用户实名认证信息（用于表单自动填充）
 * @returns {Promise} 返回用户实名认证信息的Promise
 */
export const getUserVerification = () => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${MATCHMAKER_BASE_URL}${BASE_PATH}/getUserVerification`,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 获取申请记录
 * @returns {Promise} 返回申请记录的Promise
 */
export const getApplicationRecord = () => {
  return http.get(`${BASE_PATH}/applicationRecord`)
}

/**
 * 验证申请表单数据
 * @param {Object} formData 表单数据
 * @returns {Object} 验证结果 {valid: boolean, message: string}
 */
export const validateApplicationForm = (formData) => {
  const { realName, phone, idCardNo, serviceArea, idCardFront, idCardBack, introduction, experience } = formData
  
  // 检查必填字段
  if (!realName || !realName.trim()) {
    return { valid: false, message: '请输入真实姓名' }
  }
  
  if (!phone || !phone.trim()) {
    return { valid: false, message: '请输入联系电话' }
  }
  
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phone)) {
    return { valid: false, message: '请输入正确的手机号' }
  }
  
  if (!idCardNo || !idCardNo.trim()) {
    return { valid: false, message: '请输入身份证号' }
  }
  
  // 验证身份证号格式
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!idCardRegex.test(idCardNo)) {
    return { valid: false, message: '请输入正确的身份证号' }
  }
  
  if (!serviceArea || !serviceArea.trim()) {
    return { valid: false, message: '请输入服务区域' }
  }
  
  if (!idCardFront) {
    return { valid: false, message: '请上传身份证正面照片' }
  }
  
  if (!idCardBack) {
    return { valid: false, message: '请上传身份证背面照片' }
  }
  
  if (!introduction || !introduction.trim()) {
    return { valid: false, message: '请填写个人介绍' }
  }
  
  if (introduction.length < 100) {
    return { valid: false, message: '个人介绍不能少于100字' }
  }
  
  if (introduction.length > 500) {
    return { valid: false, message: '个人介绍不能超过500字' }
  }
  
  if (!experience || !experience.trim()) {
    return { valid: false, message: '请填写相关经验' }
  }
  
  if (experience.length < 100) {
    return { valid: false, message: '相关经验不能少于100字' }
  }
  
  if (experience.length > 500) {
    return { valid: false, message: '相关经验不能超过500字' }
  }
  
  return { valid: true, message: '验证通过' }
}

/**
 * 格式化申请状态
 * @param {Number} status 申请状态
 * @returns {String} 状态描述
 */
export const formatApplicationStatus = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 获取申请状态颜色
 * @param {Number} status 申请状态
 * @returns {String} 颜色值
 */
export const getStatusColor = (status) => {
  const colorMap = {
    0: '#f39c12', // 橙色 - 待审核
    1: '#27ae60', // 绿色 - 已通过
    2: '#e74c3c'  // 红色 - 已拒绝
  }
  return colorMap[status] || '#95a5a6'
}

/**
 * 获取红娘等级文本
 * @param {Number} level 红娘等级
 * @returns {String} 等级文本
 */
export const getMatchmakerLevelText = (level) => {
  const levelMap = {
    1: '预备红娘',
    2: '正式红娘',
    3: '金牌红娘'
  }
  return levelMap[level] || '预备红娘'
}

/**
 * 获取红娘等级价格
 * @param {Number} level 红娘等级
 * @returns {Number} 价格（元）
 */
export const getMatchmakerLevelPrice = (level) => {
  // 现在所有红娘都是统一价格69.9元
  return 69.9
}

/**
 * 获取红娘等级颜色
 * @param {Number} level 红娘等级
 * @returns {String} 颜色值
 */
export const getMatchmakerLevelColor = (level) => {
  const colorMap = {
    1: '#3498db', // 蓝色 - 预备红娘
    2: '#f39c12', // 橙色 - 正式红娘
    3: '#e74c3c'  // 红色 - 金牌红娘
  }
  return colorMap[level] || '#3498db'
}

/**
 * 计算申请费用
 * @returns {Number} 申请费用（虚拟币）
 */
export const getApplicationFee = () => {
  return 699
}

/**
 * 检查余额是否足够申请
 * @param {Number} balance 当前余额
 * @returns {Boolean} 是否足够
 */
export const checkBalanceSufficient = (balance) => {
  return balance >= getApplicationFee()
}

/**
 * 测试牵线申请接口
 * @returns {Promise} 返回测试结果的Promise
 */
export const testMatchmakingAPI = () => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${USER_BASE_URL}${REQUEST_PATH}/test`,
      method: 'GET',
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 获取可用红娘列表
 * @param {Number} level 红娘等级筛选（可选）：1-预备红娘，2-正式红娘，3-金牌红娘
 * @returns {Promise} 返回可用红娘列表的Promise
 */
export const getAvailableMatchmakers = (level = null) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    let url = `${USER_BASE_URL}${REQUEST_PATH}/available-matchmakers`;
    if (level !== null && level >= 1 && level <= 3) {
      url += `?level=${level}`;
    }

    uni.request({
      url: url,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 发起牵线申请（创建订单）
 * @param {Object} data 申请数据
 * @param {Number} data.targetUserId 目标用户ID
 * @param {Number} data.matchmakerId 红娘ID（可选，指定红娘时传递）
 * @param {Number} data.matchmakerLevel 红娘等级（可选，智能分配时传递）：1-预备红娘，2-正式红娘，3-金牌红娘
 * @param {String} data.requestMessage 申请留言
 * @returns {Promise} 返回订单创建结果的Promise
 */
export const submitMatchmakingRequest = (data) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${USER_BASE_URL}${REQUEST_PATH}/submit`,
      method: 'POST',
      data: data,
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 获取我发起的牵线申请列表
 * @returns {Promise} 返回申请列表的Promise
 */
export const getMyMatchmakingRequests = () => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${USER_BASE_URL}${REQUEST_PATH}/my-requests`,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 取消牵线申请
 * @param {Number} requestId 申请ID
 * @returns {Promise} 返回取消结果的Promise
 */
export const cancelMatchmakingRequest = (requestId) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${USER_BASE_URL}${REQUEST_PATH}/cancel/${requestId}`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 验证牵线申请表单数据
 * @param {Object} formData 表单数据
 * @returns {Object} 验证结果 {valid: boolean, message: string}
 */
export const validateMatchmakingRequestForm = (formData) => {
  const { targetUserId, requestMessage } = formData

  if (!targetUserId) {
    return { valid: false, message: '请选择目标用户' }
  }

  if (!requestMessage || !requestMessage.trim()) {
    return { valid: false, message: '请填写申请留言' }
  }

  if (requestMessage.length < 10) {
    return { valid: false, message: '申请留言不能少于10字' }
  }

  if (requestMessage.length > 500) {
    return { valid: false, message: '申请留言不能超过500字' }
  }

  return { valid: true, message: '验证通过' }
}

/**
 * 格式化申请状态（用户视角）
 * @param {Number} status 申请状态
 * @returns {String} 状态描述
 */
export const formatRequestStatus = (status) => {
  const statusMap = {
    0: '待红娘处理',
    1: '等待对方确认',  // 红娘已接受，但还需要目标用户确认
    2: '红娘已拒绝',
    3: '对方已拒绝',
    4: '双方已同意',
    5: '已完成'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 获取申请状态颜色
 * @param {Number} status 申请状态
 * @returns {String} 颜色值
 */
export const getRequestStatusColor = (status) => {
  const colorMap = {
    0: '#f39c12', // 橙色 - 待红娘处理
    1: '#3498db', // 蓝色 - 等待对方确认
    2: '#e74c3c', // 红色 - 红娘已拒绝
    3: '#e67e22', // 深橙色 - 对方已拒绝
    4: '#27ae60', // 绿色 - 双方已同意
    5: '#9b59b6'  // 紫色 - 已完成
  }
  return colorMap[status] || '#95a5a6'
}

// ==================== 红娘服务订单相关接口 ====================

/**
 * 获取我的红娘服务订单列表
 * @param {Object} queryParams 查询参数
 * @returns {Promise} 返回订单列表的Promise
 */
export const getMyMatchmakerOrders = (queryParams = {}) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    const defaultParams = {
      pageNum: 1,
      pageSize: 10
    };

    const params = { ...defaultParams, ...queryParams };

    uni.request({
      url: `${USER_BASE_URL}/user/matchmaker-order/my-orders`,
      method: 'POST',
      data: params,
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 获取红娘服务订单详情
 * @param {Number} orderId 订单ID
 * @returns {Promise} 返回订单详情的Promise
 */
export const getMatchmakerOrderDetail = (orderId) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    console.log('获取订单详情 - OrderId:', orderId)
    console.log('获取订单详情 - Token:', token)

    if (!token) {
      console.error('获取订单详情失败: Token为空')
      reject(new Error('未登录，请先登录'))
      return
    }

    uni.request({
      url: `${USER_BASE_URL}/user/matchmaker-order/detail/${orderId}`,
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        console.log('获取订单详情响应:', res)
        resolve(res.data);
      },
      fail: (err) => {
        console.error('获取订单详情请求失败:', err)
        reject(err);
      }
    });
  });
}

/**
 * 取消红娘服务订单
 * @param {Number} orderId 订单ID
 * @returns {Promise} 返回取消结果的Promise
 */
export const cancelMatchmakerOrder = (orderId) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    uni.request({
      url: `${USER_BASE_URL}/user/matchmaker-order/cancel/${orderId}`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        resolve(res.data);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

/**
 * 支付红娘服务订单
 * @param {Number} orderId 订单ID
 * @param {Number} payType 支付方式：1-微信，2-支付宝，3-虚拟币
 * @returns {Promise} 返回支付结果的Promise
 */
export const payMatchmakerOrder = (orderId, payType) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || '';

    console.log('支付订单 - OrderId:', orderId, 'PayType:', payType)
    console.log('支付订单 - Token:', token)

    if (!token) {
      console.error('支付订单失败: Token为空')
      reject(new Error('未登录，请先登录'))
      return
    }

    uni.request({
      url: `${USER_BASE_URL}/user/matchmaker-order/pay/${orderId}?payType=${payType}`,
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'token': token
      },
      success: (res) => {
        console.log('支付订单响应:', res)
        resolve(res.data);
      },
      fail: (err) => {
        console.error('支付订单请求失败:', err)
        reject(err);
      }
    });
  });
}

/**
 * 格式化订单状态
 * @param {Number} status 订单状态
 * @returns {String} 状态描述
 */
export const formatOrderStatus = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已取消',
    3: '已退款',
    4: '已完成'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 获取订单状态颜色
 * @param {Number} status 订单状态
 * @returns {String} 颜色值
 */
export const getOrderStatusColor = (status) => {
  const colorMap = {
    0: '#f39c12', // 橙色 - 待支付
    1: '#27ae60', // 绿色 - 已支付
    2: '#95a5a6', // 灰色 - 已取消
    3: '#e74c3c', // 红色 - 已退款
    4: '#9b59b6'  // 紫色 - 已完成
  }
  return colorMap[status] || '#95a5a6'
}

export default {
  getMatchmakerList,
  getMatchmakerDetail,
  applyMatchmaker,
  checkApplicationStatus,
  getApplicationRecord,
  validateApplicationForm,
  formatApplicationStatus,
  getStatusColor,
  getApplicationFee,
  checkBalanceSufficient,
  testMatchmakingAPI,
  getAvailableMatchmakers,
  submitMatchmakingRequest,
  getMyMatchmakingRequests,
  cancelMatchmakingRequest,
  validateMatchmakingRequestForm,
  formatRequestStatus,
  getRequestStatusColor,
  // 订单相关
  getMyMatchmakerOrders,
  getMatchmakerOrderDetail,
  cancelMatchmakerOrder,
  payMatchmakerOrder,
  formatOrderStatus,
  getOrderStatusColor
}
