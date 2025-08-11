"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_scroll = require("../../utils/scroll.js");
const _sfc_main = {
  __name: "scroll-test",
  setup(__props) {
    const messages = common_vendor.ref([]);
    const scrollRefs = utils_scroll.createSafeScrollRefs(0, "");
    const scrollTop = scrollRefs.scrollTop;
    const scrollToView = scrollRefs.scrollToView;
    const scrollHandler = utils_scroll.createSafeScrollHandler(scrollToView, scrollTop);
    function addMessage() {
      const messageCount = messages.value.length + 1;
      messages.value.push(`这是第 ${messageCount} 条测试消息 - ${(/* @__PURE__ */ new Date()).toLocaleTimeString()}`);
      setTimeout(() => {
        scrollHandler.scrollToBottom("bottom-anchor");
      }, 100);
    }
    function testScrollToBottom() {
      scrollHandler.scrollToBottom("bottom-anchor");
    }
    function testScrollToTop() {
      scrollHandler.scrollToTop();
    }
    function clearMessages() {
      messages.value = [];
    }
    for (let i = 1; i <= 20; i++) {
      messages.value.push(`初始测试消息 ${i}`);
    }
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(testScrollToBottom),
        b: common_vendor.o(testScrollToTop),
        c: common_vendor.o(addMessage),
        d: common_vendor.o(clearMessages),
        e: common_vendor.f(messages.value, (message, index, i0) => {
          return {
            a: common_vendor.t(message),
            b: index,
            c: `message-${index}`
          };
        }),
        f: common_vendor.unref(scrollTop),
        g: common_vendor.unref(scrollToView),
        h: common_vendor.t(messages.value.length),
        i: common_vendor.t(common_vendor.unref(scrollTop)),
        j: common_vendor.t(common_vendor.unref(scrollToView))
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2d610b3d"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/scroll-test.js.map
