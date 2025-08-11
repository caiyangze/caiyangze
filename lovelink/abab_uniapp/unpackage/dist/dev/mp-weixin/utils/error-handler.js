"use strict";
const common_vendor = require("../common/vendor.js");
function addErrorLog(type, error, context = {}) {
  ({
    type,
    message: error.message,
    stack: error.stack,
    context,
    timestamp: (/* @__PURE__ */ new Date()).toISOString(),
    url: window.location ? window.location.href : "unknown"
  });
  common_vendor.index.__f__("error", "at utils/error-handler.js:32", `[${type}]`, error.message, context);
}
function initGlobalErrorHandler() {
  if (typeof window !== "undefined") {
    window.addEventListener("unhandledrejection", (event) => {
      if (event.reason && event.reason.message && event.reason.message.includes("scrollTop")) {
        addErrorLog("UNHANDLED_PROMISE", event.reason, {
          type: "scrollTop related"
        });
        event.preventDefault();
        common_vendor.index.__f__("warn", "at utils/error-handler.js:94", "ScrollTop错误已被全局处理器捕获并阻止");
      }
    });
    window.addEventListener("error", (event) => {
      if (event.error && event.error.message && event.error.message.includes("scrollTop")) {
        addErrorLog("GLOBAL_ERROR", event.error, {
          filename: event.filename,
          lineno: event.lineno,
          colno: event.colno
        });
        event.preventDefault();
        common_vendor.index.__f__("warn", "at utils/error-handler.js:109", "ScrollTop错误已被全局处理器捕获并阻止");
      }
    });
  }
  common_vendor.index.__f__("log", "at utils/error-handler.js:114", "全局scrollTop错误处理器已初始化");
}
function createVueErrorHandler() {
  return (error, instance, info) => {
    if (error.message && error.message.includes("scrollTop")) {
      addErrorLog("VUE_ERROR", error, {
        component: instance ? instance.$options.name || "Unknown" : "Unknown",
        info
      });
      common_vendor.index.__f__("warn", "at utils/error-handler.js:129", "Vue组件中的scrollTop错误已被处理:", error.message);
      return;
    }
    throw error;
  };
}
function createSafeScrollTopRef(initialValue = 0) {
  const ref = {
    _value: initialValue,
    get value() {
      return this._value;
    },
    set value(newValue) {
      try {
        if (typeof newValue === "number" && !isNaN(newValue)) {
          this._value = newValue;
        } else {
          common_vendor.index.__f__("warn", "at utils/error-handler.js:153", "Invalid scrollTop value:", newValue, "using 0 instead");
          this._value = 0;
        }
      } catch (error) {
        addErrorLog("REF_SETTER_ERROR", error, { newValue });
        this._value = 0;
      }
    }
  };
  return ref;
}
function monitorScrollTopSetting() {
  if (typeof window === "undefined" || typeof Element === "undefined") {
    return;
  }
  const originalScrollTopDescriptor = Object.getOwnPropertyDescriptor(Element.prototype, "scrollTop");
  if (originalScrollTopDescriptor && originalScrollTopDescriptor.set) {
    const originalSetter = originalScrollTopDescriptor.set;
    Object.defineProperty(Element.prototype, "scrollTop", {
      get: originalScrollTopDescriptor.get,
      set: function(value) {
        try {
          if (this === null || this === void 0) {
            addErrorLog("DOM_SCROLL_ERROR", new Error("Trying to set scrollTop on null element"), { value });
            return;
          }
          originalSetter.call(this, value);
        } catch (error) {
          addErrorLog("DOM_SCROLL_ERROR", error, {
            element: this ? this.tagName : "null",
            value
          });
          common_vendor.index.__f__("warn", "at utils/error-handler.js:199", "ScrollTop设置失败，已被安全处理:", error.message);
        }
      },
      enumerable: originalScrollTopDescriptor.enumerable,
      configurable: originalScrollTopDescriptor.configurable
    });
    common_vendor.index.__f__("log", "at utils/error-handler.js:206", "DOM scrollTop监控已启用");
  }
}
exports.createSafeScrollTopRef = createSafeScrollTopRef;
exports.createVueErrorHandler = createVueErrorHandler;
exports.initGlobalErrorHandler = initGlobalErrorHandler;
exports.monitorScrollTopSetting = monitorScrollTopSetting;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/error-handler.js.map
