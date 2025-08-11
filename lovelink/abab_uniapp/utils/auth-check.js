/**
 * 统一的身份验证检查工具
 */

/**
 * 检查用户是否已登录
 * @returns {Object} 登录状态信息
 */
export function checkAuthStatus() {
  const token = uni.getStorageSync('token') || ''
  const userInfo = uni.getStorageSync('userInfo') || null
  
  console.log('=== 身份验证检查 ===')
  console.log('Token存在:', !!token)
  console.log('Token长度:', token.length)
  console.log('UserInfo存在:', !!userInfo)
  
  if (token) {
    console.log('Token前20位:', token.substring(0, 20) + '...')
  }
  
  return {
    isLoggedIn: !!token,
    token: token,
    userInfo: userInfo,
    hasValidToken: token && token.length > 10 // 简单的token有效性检查
  }
}

/**
 * 强制用户登录（如果未登录则跳转到登录页）
 * @param {String} redirectUrl 登录成功后的重定向URL
 * @returns {Boolean} 是否已登录
 */
export function requireAuth(redirectUrl = null) {
  const authStatus = checkAuthStatus()
  
  if (!authStatus.isLoggedIn) {
    console.log('用户未登录，跳转到登录页面')
    
    uni.showToast({
      title: '请先登录',
      icon: 'error'
    })
    
    setTimeout(() => {
      const loginUrl = redirectUrl 
        ? `/pages/login/login?redirect=${encodeURIComponent(redirectUrl)}`
        : '/pages/login/login'
      
      uni.redirectTo({
        url: loginUrl
      })
    }, 1500)
    
    return false
  }
  
  return true
}

/**
 * 创建带有token的请求头
 * @returns {Object} 请求头对象
 */
export function createAuthHeaders() {
  const token = uni.getStorageSync('token') || ''
  
  if (!token) {
    console.warn('创建请求头时token为空')
    return {
      'Content-Type': 'application/json'
    }
  }
  
  return {
    'Content-Type': 'application/json',
    'token': token
  }
}

/**
 * 安全的API请求封装
 * @param {Object} options 请求选项
 * @returns {Promise} 请求Promise
 */
export function secureRequest(options) {
  return new Promise((resolve, reject) => {
    // 检查登录状态
    const authStatus = checkAuthStatus()
    if (!authStatus.isLoggedIn) {
      reject(new Error('用户未登录'))
      return
    }
    
    // 自动添加token到请求头
    const headers = createAuthHeaders()
    if (options.header) {
      Object.assign(headers, options.header)
    }
    
    console.log('安全请求 - URL:', options.url)
    console.log('安全请求 - Headers:', headers)
    
    uni.request({
      ...options,
      header: headers,
      success: (res) => {
        console.log('安全请求响应:', res)
        
        // 检查是否是未登录错误
        if (res.data && res.data.code === 400 && res.data.message === '用户未登录') {
          console.error('服务器返回未登录错误，清除本地token')
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'error'
          })
          
          setTimeout(() => {
            uni.redirectTo({
              url: '/pages/login/login'
            })
          }, 1500)
          
          reject(new Error('登录已过期'))
          return
        }
        
        resolve(res.data)
      },
      fail: (err) => {
        console.error('安全请求失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 清除登录信息
 */
export function clearAuth() {
  console.log('清除登录信息')
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
  uni.removeStorageSync('userId')
}

/**
 * 页面级别的登录检查（在页面onLoad中使用）
 * @param {String} pageName 页面名称（用于日志）
 * @returns {Boolean} 是否通过验证
 */
export function pageAuthCheck(pageName = '当前页面') {
  console.log(`${pageName} - 执行登录检查`)
  
  const authStatus = checkAuthStatus()
  
  if (!authStatus.isLoggedIn) {
    console.error(`${pageName} - 用户未登录`)
    
    uni.showModal({
      title: '需要登录',
      content: '请先登录后再访问此页面',
      showCancel: false,
      confirmText: '去登录',
      success: () => {
        uni.redirectTo({
          url: '/pages/login/login'
        })
      }
    })
    
    return false
  }
  
  console.log(`${pageName} - 登录检查通过`)
  return true
}
