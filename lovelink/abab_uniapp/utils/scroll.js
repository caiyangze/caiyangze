/**
 * 滚动相关工具函数
 * 解决H5环境下scrollTop设置null对象的问题
 */

import { createSafeScrollTopRef } from './error-handler'

/**
 * 安全设置scrollTop
 * @param {Ref} scrollTopRef - scrollTop的响应式引用
 * @param {number} value - 要设置的scrollTop值
 */
export function safeSetScrollTop(scrollTopRef, value) {
  try {
    // 检查scrollTopRef是否为有效的响应式引用
    if (!scrollTopRef) {
      console.warn('scrollTopRef为null或undefined，跳过设置');
      return;
    }

    // 检查是否为Vue的响应式引用
    if (typeof scrollTopRef !== 'object' || !('value' in scrollTopRef)) {
      console.warn('scrollTopRef不是有效的响应式引用，跳过设置');
      return;
    }

    // 确保值是有效的数字
    const numValue = typeof value === 'number' ? value : 0;

    // 安全设置值
    scrollTopRef.value = numValue;

  } catch (error) {
    console.error('设置scrollTop失败:', error);
    console.error('错误详情:', {
      scrollTopRef: scrollTopRef,
      value: value,
      error: error.message
    });
  }
}

/**
 * 安全设置scrollToView
 * @param {Ref} scrollToViewRef - scrollToView的响应式引用
 * @param {string} elementId - 要滚动到的元素ID
 * @param {number} clearDelay - 清空延迟时间（毫秒）
 */
export function safeSetScrollToView(scrollToViewRef, elementId, clearDelay = 100) {
  try {
    if (scrollToViewRef && scrollToViewRef.value !== null && typeof scrollToViewRef.value !== 'undefined') {
      // 确保elementId是有效的字符串
      const validElementId = typeof elementId === 'string' ? elementId : '';
      scrollToViewRef.value = validElementId;

      // 延迟清空以便下次滚动生效
      if (clearDelay > 0) {
        setTimeout(() => {
          try {
            if (scrollToViewRef && scrollToViewRef.value !== null) {
              scrollToViewRef.value = '';
            }
          } catch (error) {
            console.warn('清空scrollToView失败:', error);
          }
        }, clearDelay);
      }
    }
  } catch (error) {
    console.warn('设置scrollToView失败:', error);
  }
}

/**
 * 安全滚动到底部
 * @param {Ref} scrollToViewRef - scrollToView的响应式引用
 * @param {Ref} scrollTopRef - scrollTop的响应式引用（备用方案）
 * @param {string} bottomAnchorId - 底部锚点ID，默认为'bottom-anchor'
 */
export function safeScrollToBottom(scrollToViewRef, scrollTopRef, bottomAnchorId = 'bottom-anchor') {
  try {
    // 检查引用是否有效
    if (!scrollToViewRef || !scrollTopRef) {
      console.warn('滚动引用无效');
      return;
    }

    // 优先使用scrollToView
    safeSetScrollToView(scrollToViewRef, bottomAnchorId);

    // 备用方案：同时设置scrollTop，增加成功率
    setTimeout(() => {
      try {
        safeSetScrollTop(scrollTopRef, 999999);
      } catch (scrollError) {
        console.warn('备用scrollTop滚动失败:', scrollError);
      }
    }, 50);
  } catch (error) {
    console.warn('滚动到底部失败:', error);
    // 最后的备用方案
    try {
      safeSetScrollTop(scrollTopRef, 999999);
    } catch (scrollError) {
      console.warn('最终备用滚动也失败:', scrollError);
    }
  }
}

/**
 * 安全滚动到顶部
 * @param {Ref} scrollTopRef - scrollTop的响应式引用
 */
export function safeScrollToTop(scrollTopRef) {
  safeSetScrollTop(scrollTopRef, 0);
}

/**
 * 检查页面是否仍然活跃
 * @returns {boolean} 页面是否活跃
 */
export function isPageActive() {
  try {
    const pages = getCurrentPages();
    return pages && pages.length > 0;
  } catch (error) {
    console.warn('检查页面状态失败:', error);
    return false;
  }
}

/**
 * 创建安全的滚动引用
 * @param {number} initialScrollTop - 初始scrollTop值
 * @param {string} initialScrollToView - 初始scrollToView值
 * @returns {Object} 包含安全的滚动引用
 */
export function createSafeScrollRefs(initialScrollTop = 0, initialScrollToView = '') {
  return {
    scrollTop: createSafeScrollTopRef(initialScrollTop),
    scrollToView: createSafeScrollTopRef(initialScrollToView) // 复用同样的安全机制
  };
}

/**
 * 创建安全的滚动处理器
 * @param {Ref} scrollToViewRef - scrollToView的响应式引用
 * @param {Ref} scrollTopRef - scrollTop的响应式引用
 * @returns {Object} 包含各种滚动方法的对象
 */
export function createSafeScrollHandler(scrollToViewRef, scrollTopRef) {
  return {
    scrollToBottom: (bottomAnchorId) => {
      if (isPageActive()) {
        safeScrollToBottom(scrollToViewRef, scrollTopRef, bottomAnchorId);
      } else {
        console.warn('页面不活跃，跳过滚动操作');
      }
    },
    scrollToTop: () => {
      if (isPageActive()) {
        safeScrollToTop(scrollTopRef);
      } else {
        console.warn('页面不活跃，跳过滚动操作');
      }
    },
    scrollToView: (elementId, clearDelay) => {
      if (isPageActive()) {
        safeSetScrollToView(scrollToViewRef, elementId, clearDelay);
      } else {
        console.warn('页面不活跃，跳过滚动操作');
      }
    },
    setScrollTop: (value) => {
      if (isPageActive()) {
        safeSetScrollTop(scrollTopRef, value);
      } else {
        console.warn('页面不活跃，跳过滚动操作');
      }
    }
  };
}

/**
 * 防抖滚动函数
 * @param {Function} scrollFn - 滚动函数
 * @param {number} delay - 防抖延迟时间（毫秒）
 * @returns {Function} 防抖后的滚动函数
 */
export function debounceScroll(scrollFn, delay = 100) {
  let timeoutId = null;
  
  return function(...args) {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    
    timeoutId = setTimeout(() => {
      try {
        scrollFn.apply(this, args);
      } catch (error) {
        console.warn('防抖滚动执行失败:', error);
      }
    }, delay);
  };
}

/**
 * 节流滚动函数
 * @param {Function} scrollFn - 滚动函数
 * @param {number} delay - 节流延迟时间（毫秒）
 * @returns {Function} 节流后的滚动函数
 */
export function throttleScroll(scrollFn, delay = 100) {
  let lastTime = 0;
  
  return function(...args) {
    const now = Date.now();
    
    if (now - lastTime >= delay) {
      try {
        scrollFn.apply(this, args);
        lastTime = now;
      } catch (error) {
        console.warn('节流滚动执行失败:', error);
      }
    }
  };
}
