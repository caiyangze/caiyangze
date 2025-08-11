/**
 * 导航工具函数
 */

// 主要页面路径
export const MAIN_PAGES = [
  '/pages/index/index',
  '/pages/square/square', 
  '/pages/message/message',
  '/pages/mine/mine'
];

/**
 * 跳转到主要页面（原 tabBar 页面）
 * @param {string} url 页面路径
 */
export function navigateToMainPage(url) {
  if (!MAIN_PAGES.includes(url)) {
    console.warn(`${url} 不是主要页面，建议使用 uni.navigateTo`);
  }
  
  return new Promise((resolve, reject) => {
    uni.reLaunch({
      url,
      success: (res) => {
        console.log(`跳转到 ${url} 成功`);
        resolve(res);
      },
      fail: (err) => {
        console.error(`跳转到 ${url} 失败:`, err);
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

/**
 * 跳转到普通页面
 * @param {string} url 页面路径
 * @param {object} options 跳转选项
 */
export function navigateToPage(url, options = {}) {
  return new Promise((resolve, reject) => {
    const config = Object.assign({
      url: url,
      success: (res) => {
        console.log(`跳转到 ${url} 成功`);
        resolve(res);
      },
      fail: (err) => {
        console.error(`跳转到 ${url} 失败:`, err);
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
        reject(err);
      }
    }, options);

    uni.navigateTo(config);
  });
}

/**
 * 返回上一页
 * @param {number} delta 返回的页面数
 */
export function navigateBack(delta = 1) {
  return new Promise((resolve, reject) => {
    uni.navigateBack({
      delta,
      success: (res) => {
        console.log(`返回 ${delta} 页成功`);
        resolve(res);
      },
      fail: (err) => {
        console.error(`返回页面失败:`, err);
        // 如果返回失败，尝试跳转到首页
        navigateToMainPage('/pages/index/index').catch(() => {
          uni.showToast({
            title: '页面跳转失败',
            icon: 'none'
          });
        });
        reject(err);
      }
    });
  });
}

/**
 * 重定向到页面
 * @param {string} url 页面路径
 */
export function redirectToPage(url) {
  return new Promise((resolve, reject) => {
    uni.redirectTo({
      url,
      success: (res) => {
        console.log(`重定向到 ${url} 成功`);
        resolve(res);
      },
      fail: (err) => {
        console.error(`重定向到 ${url} 失败:`, err);
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

/**
 * 重新启动到页面（清空页面栈）
 * @param {string} url 页面路径
 */
export function reLaunchToPage(url) {
  return new Promise((resolve, reject) => {
    uni.reLaunch({
      url,
      success: (res) => {
        console.log(`重新启动到 ${url} 成功`);
        resolve(res);
      },
      fail: (err) => {
        console.error(`重新启动到 ${url} 失败:`, err);
        uni.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

/**
 * 获取当前页面路径
 */
export function getCurrentPagePath() {
  try {
    const pages = getCurrentPages();
    if (pages && pages.length > 0) {
      const currentPage = pages[pages.length - 1];
      return '/' + currentPage.route;
    }
  } catch (error) {
    console.error('获取当前页面路径失败:', error);
  }
  return null;
}

/**
 * 检查是否为主要页面
 * @param {string} url 页面路径
 */
export function isMainPage(url) {
  return MAIN_PAGES.includes(url);
}

/**
 * 智能跳转（自动判断跳转方式）
 * @param {string} url 页面路径
 * @param {object} options 跳转选项
 */
export function smartNavigate(url, options = {}) {
  if (isMainPage(url)) {
    return navigateToMainPage(url);
  } else {
    return navigateToPage(url, options);
  }
}
