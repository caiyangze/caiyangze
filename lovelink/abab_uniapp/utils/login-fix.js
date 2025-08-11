/**
 * 登录问题修复工具
 */

/**
 * 完全清除登录状态
 */
export function clearAllLoginData() {
  console.log('🧹 清除所有登录数据...');
  
  // 清除所有可能的存储key
  const keysToRemove = [
    'token',
    'refreshToken', 
    'userInfo',
    'loginTime',
    'userId',
    'user',
    'authToken',
    'accessToken'
  ];
  
  keysToRemove.forEach(key => {
    uni.removeStorageSync(key);
    console.log(`✅ 已清除: ${key}`);
  });
  
  // 清除所有存储（谨慎使用）
  // uni.clearStorageSync();
  
  console.log('✅ 登录数据清除完成');
}

/**
 * 强制跳转到登录页
 */
export function forceToLogin() {
  console.log('🔄 强制跳转到登录页...');
  
  clearAllLoginData();
  
  uni.reLaunch({
    url: '/pages/login/login',
    success: () => {
      console.log('✅ 已跳转到登录页');
    },
    fail: (error) => {
      console.error('❌ 跳转失败:', error);
      // 备用方案
      uni.navigateTo({
        url: '/pages/login/login'
      });
    }
  });
}

/**
 * 检查登录页面是否存在
 */
export function checkLoginPage() {
  console.log('🔍 检查登录页面...');
  
  // 尝试获取页面栈
  const pages = getCurrentPages();
  console.log('当前页面栈:', pages.map(p => p.route));
  
  // 检查是否有登录页
  const hasLoginPage = pages.some(page => 
    page.route.includes('login') || 
    page.route.includes('auth')
  );
  
  console.log('是否有登录页:', hasLoginPage);
  return hasLoginPage;
}

/**
 * 模拟登录（用于测试）
 */
export function mockLogin() {
  console.log('🧪 模拟登录...');
  
  const mockToken = 'mock_token_' + Date.now();
  const mockUserInfo = {
    userId: 1001,
    nickname: '测试用户',
    avatar: '/static/message/default-avatar.png',
    phone: '13800138000'
  };
  
  uni.setStorageSync('token', mockToken);
  uni.setStorageSync('userInfo', mockUserInfo);
  
  console.log('✅ 模拟登录完成');
  console.log('Token:', mockToken);
  console.log('UserInfo:', mockUserInfo);
  
  return { token: mockToken, userInfo: mockUserInfo };
}

/**
 * 验证token格式
 */
export function validateToken(token) {
  if (!token) {
    return { valid: false, reason: 'Token为空' };
  }
  
  if (typeof token !== 'string') {
    return { valid: false, reason: 'Token不是字符串' };
  }
  
  if (token.length < 10) {
    return { valid: false, reason: 'Token长度太短' };
  }
  
  // 检查是否是JWT格式
  const parts = token.split('.');
  if (parts.length === 3) {
    try {
      const payload = JSON.parse(atob(parts[1]));
      const now = Date.now() / 1000;
      
      if (payload.exp && payload.exp < now) {
        return { valid: false, reason: 'JWT Token已过期' };
      }
      
      return { valid: true, reason: 'JWT Token有效', payload };
    } catch (error) {
      return { valid: false, reason: 'JWT Token格式错误' };
    }
  }
  
  return { valid: true, reason: '普通Token格式' };
}

/**
 * 完整的登录修复流程
 */
export const fixLoginIssue = async function() {
  console.log('🔧 开始登录修复流程...');
  
  // 1. 检查当前token
  const currentToken = uni.getStorageSync('token');
  console.log('当前Token:', currentToken ? currentToken.substring(0, 20) + '...' : 'null');
  
  if (currentToken) {
    const validation = validateToken(currentToken);
    console.log('Token验证结果:', validation);
    
    if (!validation.valid) {
      console.log('❌ Token无效，清除并重新登录');
      forceToLogin();
      return;
    }
  }
  
  // 2. 测试API连接
  try {
    console.log('🧪 测试API连接...');
    const response = await uni.request({
      url: 'http://localhost:9001/follow/mutual',
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'token': currentToken,
        'Authorization': `Bearer ${currentToken}`
      }
    });
    
    console.log('API测试响应:', response);
    
    if (response.statusCode === 200 && response.data.code === 200) {
      console.log('✅ API连接正常，登录状态有效');
      return true;
    } else {
      console.log('❌ API返回错误，需要重新登录');
      forceToLogin();
      return false;
    }
    
  } catch (error) {
    console.error('❌ API测试失败:', error);
    forceToLogin();
    return false;
  }
}

/**
 * 紧急修复按钮（在页面上显示）
 */
export function createEmergencyFixButton() {
  // 创建一个浮动按钮用于紧急修复
  const button = document.createElement('button');
  button.innerHTML = '🔧 修复登录';
  button.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 9999;
    background: #ff4757;
    color: white;
    border: none;
    padding: 10px 15px;
    border-radius: 5px;
    cursor: pointer;
    font-size: 14px;
  `;
  
  button.onclick = () => {
    fixLoginIssue();
  };
  
  document.body.appendChild(button);
  
  // 5秒后自动移除
  setTimeout(() => {
    if (button.parentNode) {
      button.parentNode.removeChild(button);
    }
  }, 5000);
}
