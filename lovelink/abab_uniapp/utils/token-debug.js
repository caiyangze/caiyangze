/**
 * Token调试工具
 */

/**
 * 检查token状态
 */
export function checkTokenStatus() {
  const token = uni.getStorageSync('token');
  const refreshToken = uni.getStorageSync('refreshToken');
  const userInfo = uni.getStorageSync('userInfo');
  
  console.log('=== Token状态检查 ===');
  console.log('Token:', token ? token.substring(0, 30) + '...' : 'null');
  console.log('RefreshToken:', refreshToken ? refreshToken.substring(0, 30) + '...' : 'null');
  console.log('UserInfo:', userInfo);
  console.log('Token长度:', token ? token.length : 0);
  
  // 检查token格式
  if (token) {
    try {
      // 如果是JWT token，尝试解析
      const parts = token.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        console.log('JWT Payload:', payload);
        console.log('Token过期时间:', new Date(payload.exp * 1000));
        console.log('当前时间:', new Date());
        console.log('Token是否过期:', payload.exp * 1000 < Date.now());
      }
    } catch (error) {
      console.log('Token不是JWT格式或解析失败:', error);
    }
  }
  
  return {
    hasToken: !!token,
    hasRefreshToken: !!refreshToken,
    hasUserInfo: !!userInfo,
    token: token,
    tokenLength: token ? token.length : 0
  };
}

/**
 * 清除所有登录信息
 */
export function clearLoginInfo() {
  console.log('清除登录信息...');
  uni.removeStorageSync('token');
  uni.removeStorageSync('refreshToken');
  uni.removeStorageSync('userInfo');
  console.log('登录信息已清除');
}

/**
 * 设置测试token
 */
export function setTestToken() {
  const testToken = 'test_token_' + Date.now();
  uni.setStorageSync('token', testToken);
  console.log('设置测试token:', testToken);
}

/**
 * 测试API请求
 */
export const testApiRequest = async function() {
  console.log('=== 测试API请求 ===');
  
  const status = checkTokenStatus();
  if (!status.hasToken) {
    console.error('没有token，无法测试');
    return;
  }
  
  try {
    // 测试获取好友列表接口
    const response = await uni.request({
      url: 'http://localhost:9001/follow/mutual',
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': status.token,
        'Authorization': `Bearer ${status.token}`
      }
    });
    
    console.log('API测试响应:', response);
    
    if (response.statusCode === 200) {
      console.log('✅ API请求成功');
      return response.data;
    } else {
      console.error('❌ API请求失败:', response);
      return null;
    }
    
  } catch (error) {
    console.error('❌ API请求异常:', error);
    return null;
  }
}

/**
 * 检查网络连接
 */
export function checkNetworkStatus() {
  uni.getNetworkType({
    success: (res) => {
      console.log('=== 网络状态 ===');
      console.log('网络类型:', res.networkType);
      console.log('是否连接:', res.networkType !== 'none');
    },
    fail: (error) => {
      console.error('获取网络状态失败:', error);
    }
  });
}

/**
 * 完整的登录状态诊断
 */
export const diagnoseLoginStatus = async function() {
  console.log('🔍 开始登录状态诊断...');
  
  // 1. 检查网络
  checkNetworkStatus();
  
  // 2. 检查token
  const tokenStatus = checkTokenStatus();
  
  // 3. 测试API
  if (tokenStatus.hasToken) {
    await testApiRequest();
  }
  
  // 4. 给出建议
  console.log('=== 诊断建议 ===');
  if (!tokenStatus.hasToken) {
    console.log('❌ 没有token，请重新登录');
  } else if (!tokenStatus.hasUserInfo) {
    console.log('⚠️ 有token但没有用户信息，可能需要重新获取用户信息');
  } else {
    console.log('✅ 登录状态看起来正常，可能是服务器端问题');
  }
  
  return tokenStatus;
}
