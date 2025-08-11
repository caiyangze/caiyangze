"use strict";
const common_vendor = require("../common/vendor.js");
const utils_errorHandler = require("./error-handler.js");
function safeSetScrollTop(scrollTopRef, value) {
  try {
    if (!scrollTopRef) {
      common_vendor.index.__f__("warn", "at utils/scroll.js:17", "scrollTopRef为null或undefined，跳过设置");
      return;
    }
    if (typeof scrollTopRef !== "object" || !("value" in scrollTopRef)) {
      common_vendor.index.__f__("warn", "at utils/scroll.js:23", "scrollTopRef不是有效的响应式引用，跳过设置");
      return;
    }
    const numValue = typeof value === "number" ? value : 0;
    scrollTopRef.value = numValue;
  } catch (error) {
    common_vendor.index.__f__("error", "at utils/scroll.js:34", "设置scrollTop失败:", error);
    common_vendor.index.__f__("error", "at utils/scroll.js:35", "错误详情:", {
      scrollTopRef,
      value,
      error: error.message
    });
  }
}
function safeSetScrollToView(scrollToViewRef, elementId, clearDelay = 100) {
  try {
    if (scrollToViewRef && scrollToViewRef.value !== null && typeof scrollToViewRef.value !== "undefined") {
      const validElementId = typeof elementId === "string" ? elementId : "";
      scrollToViewRef.value = validElementId;
      if (clearDelay > 0) {
        setTimeout(() => {
          try {
            if (scrollToViewRef && scrollToViewRef.value !== null) {
              scrollToViewRef.value = "";
            }
          } catch (error) {
            common_vendor.index.__f__("warn", "at utils/scroll.js:64", "清空scrollToView失败:", error);
          }
        }, clearDelay);
      }
    }
  } catch (error) {
    common_vendor.index.__f__("warn", "at utils/scroll.js:70", "设置scrollToView失败:", error);
  }
}
function safeScrollToBottom(scrollToViewRef, scrollTopRef, bottomAnchorId = "bottom-anchor") {
  try {
    if (!scrollToViewRef || !scrollTopRef) {
      common_vendor.index.__f__("warn", "at utils/scroll.js:84", "滚动引用无效");
      return;
    }
    safeSetScrollToView(scrollToViewRef, bottomAnchorId);
    setTimeout(() => {
      try {
        safeSetScrollTop(scrollTopRef, 999999);
      } catch (scrollError) {
        common_vendor.index.__f__("warn", "at utils/scroll.js:96", "备用scrollTop滚动失败:", scrollError);
      }
    }, 50);
  } catch (error) {
    common_vendor.index.__f__("warn", "at utils/scroll.js:100", "滚动到底部失败:", error);
    try {
      safeSetScrollTop(scrollTopRef, 999999);
    } catch (scrollError) {
      common_vendor.index.__f__("warn", "at utils/scroll.js:105", "最终备用滚动也失败:", scrollError);
    }
  }
}
function safeScrollToTop(scrollTopRef) {
  safeSetScrollTop(scrollTopRef, 0);
}
function isPageActive() {
  try {
    const pages = getCurrentPages();
    return pages && pages.length > 0;
  } catch (error) {
    common_vendor.index.__f__("warn", "at utils/scroll.js:127", "检查页面状态失败:", error);
    return false;
  }
}
function createSafeScrollRefs(initialScrollTop = 0, initialScrollToView = "") {
  return {
    scrollTop: utils_errorHandler.createSafeScrollTopRef(initialScrollTop),
    scrollToView: utils_errorHandler.createSafeScrollTopRef(initialScrollToView)
    // 复用同样的安全机制
  };
}
function createSafeScrollHandler(scrollToViewRef, scrollTopRef) {
  return {
    scrollToBottom: (bottomAnchorId) => {
      if (isPageActive()) {
        safeScrollToBottom(scrollToViewRef, scrollTopRef, bottomAnchorId);
      } else {
        common_vendor.index.__f__("warn", "at utils/scroll.js:157", "页面不活跃，跳过滚动操作");
      }
    },
    scrollToTop: () => {
      if (isPageActive()) {
        safeScrollToTop(scrollTopRef);
      } else {
        common_vendor.index.__f__("warn", "at utils/scroll.js:164", "页面不活跃，跳过滚动操作");
      }
    },
    scrollToView: (elementId, clearDelay) => {
      if (isPageActive()) {
        safeSetScrollToView(scrollToViewRef, elementId, clearDelay);
      } else {
        common_vendor.index.__f__("warn", "at utils/scroll.js:171", "页面不活跃，跳过滚动操作");
      }
    },
    setScrollTop: (value) => {
      if (isPageActive()) {
        safeSetScrollTop(scrollTopRef, value);
      } else {
        common_vendor.index.__f__("warn", "at utils/scroll.js:178", "页面不活跃，跳过滚动操作");
      }
    }
  };
}
exports.createSafeScrollHandler = createSafeScrollHandler;
exports.createSafeScrollRefs = createSafeScrollRefs;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/scroll.js.map
