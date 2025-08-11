/**
 * 全局错误处理器
 * 专门处理scrollTop相关的错误
 */

// 错误日志收集
const errorLogs = [];

/**
 * 添加错误日志
 * @param {string} type 错误类型
 * @param {Error} error 错误对象
 * @param {Object} context 上下文信息
 */
function addErrorLog(type, error, context = {}) {
  const log = {
    type,
    message: error.message,
    stack: error.stack,
    context,
    timestamp: new Date().toISOString(),
    url: window.location ? window.location.href : 'unknown'
  };
  
  errorLogs.push(log);
  
  // 限制日志数量
  if (errorLogs.length > 100) {
    errorLogs.shift();
  }
  
  console.error(`[${type}]`, error.message, context);
}

/**
 * 获取错误日志
 * @returns {Array} 错误日志数组
 */
export function getErrorLogs() {
  return [...errorLogs];
}

/**
 * 清空错误日志
 */
export function clearErrorLogs() {
  errorLogs.length = 0;
}

/**
 * 安全的scrollTop设置函数
 * 这是最后的防线，确保不会抛出未捕获的错误
 */
export function safeScrollTopSetter(element, value) {
  try {
    if (!element) {
      addErrorLog('SCROLL_ERROR', new Error('Element is null'), { value });
      return false;
    }
    
    if (typeof element.scrollTop === 'undefined') {
      addErrorLog('SCROLL_ERROR', new Error('Element does not have scrollTop property'), { 
        element: element.constructor.name,
        value 
      });
      return false;
    }
    
    element.scrollTop = value;
    return true;
  } catch (error) {
    addErrorLog('SCROLL_ERROR', error, { 
      element: element ? element.constructor.name : 'null',
      value 
    });
    return false;
  }
}

/**
 * 初始化全局错误处理
 */
export function initGlobalErrorHandler() {
  // 捕获未处理的Promise错误
  if (typeof window !== 'undefined') {
    window.addEventListener('unhandledrejection', (event) => {
      if (event.reason && event.reason.message && event.reason.message.includes('scrollTop')) {
        addErrorLog('UNHANDLED_PROMISE', event.reason, {
          type: 'scrollTop related'
        });
        
        // 阻止错误继续传播
        event.preventDefault();
        console.warn('ScrollTop错误已被全局处理器捕获并阻止');
      }
    });
    
    // 捕获全局错误
    window.addEventListener('error', (event) => {
      if (event.error && event.error.message && event.error.message.includes('scrollTop')) {
        addErrorLog('GLOBAL_ERROR', event.error, {
          filename: event.filename,
          lineno: event.lineno,
          colno: event.colno
        });
        
        // 阻止错误继续传播
        event.preventDefault();
        console.warn('ScrollTop错误已被全局处理器捕获并阻止');
      }
    });
  }
  
  console.log('全局scrollTop错误处理器已初始化');
}

/**
 * Vue组件错误处理器
 * 可以在Vue应用中使用
 */
export function createVueErrorHandler() {
  return (error, instance, info) => {
    if (error.message && error.message.includes('scrollTop')) {
      addErrorLog('VUE_ERROR', error, {
        component: instance ? instance.$options.name || 'Unknown' : 'Unknown',
        info
      });
      
      console.warn('Vue组件中的scrollTop错误已被处理:', error.message);
      return; // 阻止错误继续传播
    }
    
    // 其他错误继续抛出
    throw error;
  };
}

/**
 * 创建安全的响应式引用包装器
 * 确保scrollTop引用始终有效
 */
export function createSafeScrollTopRef(initialValue = 0) {
  const ref = {
    _value: initialValue,
    get value() {
      return this._value;
    },
    set value(newValue) {
      try {
        if (typeof newValue === 'number' && !isNaN(newValue)) {
          this._value = newValue;
        } else {
          console.warn('Invalid scrollTop value:', newValue, 'using 0 instead');
          this._value = 0;
        }
      } catch (error) {
        addErrorLog('REF_SETTER_ERROR', error, { newValue });
        this._value = 0;
      }
    }
  };
  
  return ref;
}

/**
 * 监控DOM元素的scrollTop设置
 * 在开发环境中使用
 */
export function monitorScrollTopSetting() {
  if (typeof window === 'undefined' || typeof Element === 'undefined') {
    return;
  }
  
  // 保存原始的scrollTop setter
  const originalScrollTopDescriptor = Object.getOwnPropertyDescriptor(Element.prototype, 'scrollTop');
  
  if (originalScrollTopDescriptor && originalScrollTopDescriptor.set) {
    const originalSetter = originalScrollTopDescriptor.set;
    
    // 重写scrollTop setter
    Object.defineProperty(Element.prototype, 'scrollTop', {
      get: originalScrollTopDescriptor.get,
      set: function(value) {
        try {
          if (this === null || this === undefined) {
            addErrorLog('DOM_SCROLL_ERROR', new Error('Trying to set scrollTop on null element'), { value });
            return;
          }
          
          originalSetter.call(this, value);
        } catch (error) {
          addErrorLog('DOM_SCROLL_ERROR', error, { 
            element: this ? this.tagName : 'null',
            value 
          });
          
          // 不重新抛出错误，避免中断应用
          console.warn('ScrollTop设置失败，已被安全处理:', error.message);
        }
      },
      enumerable: originalScrollTopDescriptor.enumerable,
      configurable: originalScrollTopDescriptor.configurable
    });
    
    console.log('DOM scrollTop监控已启用');
  }
}

export default {
  initGlobalErrorHandler,
  createVueErrorHandler,
  safeScrollTopSetter,
  createSafeScrollTopRef,
  monitorScrollTopSetting,
  getErrorLogs,
  clearErrorLogs
};
