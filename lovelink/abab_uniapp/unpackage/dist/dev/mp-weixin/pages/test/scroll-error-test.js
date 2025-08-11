"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_scroll = require("../../utils/scroll.js");
const _sfc_main = {
  __name: "scroll-error-test",
  setup(__props) {
    const messages = common_vendor.ref([]);
    const logs = common_vendor.ref([]);
    const isDestroyed = common_vendor.ref(false);
    const scrollRefs = utils_scroll.createSafeScrollRefs(0, "");
    const scrollTop = scrollRefs.scrollTop;
    const scrollToView = scrollRefs.scrollToView;
    const scrollHandler = utils_scroll.createSafeScrollHandler(scrollToView, scrollTop);
    function addLog(message) {
      const timestamp = (/* @__PURE__ */ new Date()).toLocaleTimeString();
      logs.value.push(`[${timestamp}] ${message}`);
      if (logs.value.length > 50) {
        logs.value.shift();
      }
    }
    function addMessage(content) {
      messages.value.push(content);
      addLog(`添加消息: ${content}`);
    }
    function testNormalScroll() {
      addLog("开始正常滚动测试");
      try {
        addMessage(`正常滚动测试消息 - ${(/* @__PURE__ */ new Date()).toLocaleTimeString()}`);
        setTimeout(() => {
          try {
            scrollHandler.scrollToBottom("bottom-anchor");
            addLog("正常滚动测试成功");
          } catch (error) {
            addLog(`正常滚动测试错误: ${error.message}`);
          }
        }, 100);
      } catch (error) {
        addLog(`正常滚动测试异常: ${error.message}`);
      }
    }
    function testRapidScroll() {
      addLog("开始快速滚动测试");
      try {
        for (let i = 0; i < 5; i++) {
          addMessage(`快速滚动测试消息 ${i + 1}`);
          setTimeout(() => {
            try {
              scrollHandler.scrollToBottom("bottom-anchor");
              addLog(`快速滚动 ${i + 1} 成功`);
            } catch (error) {
              addLog(`快速滚动 ${i + 1} 错误: ${error.message}`);
            }
          }, i * 50);
        }
      } catch (error) {
        addLog(`快速滚动测试异常: ${error.message}`);
      }
    }
    function testScrollAfterDestroy() {
      addLog("开始销毁后滚动测试");
      try {
        isDestroyed.value = true;
        addLog("模拟页面销毁状态");
        setTimeout(() => {
          try {
            scrollHandler.scrollToBottom("bottom-anchor");
            addLog("销毁后滚动测试完成（应该被跳过）");
          } catch (error) {
            addLog(`销毁后滚动测试错误: ${error.message}`);
          }
        }, 100);
        setTimeout(() => {
          isDestroyed.value = false;
          addLog("恢复正常状态");
        }, 1e3);
      } catch (error) {
        addLog(`销毁后滚动测试异常: ${error.message}`);
      }
    }
    function testNavigateToChat() {
      addLog("开始跳转聊天页面测试");
      try {
        const testUser = {
          userId: "123",
          nickname: "测试用户",
          avatarUrl: "/static/message/default-avatar.png"
        };
        common_vendor.index.navigateTo({
          url: `/pages/message/chat?userId=${testUser.userId}&name=${encodeURIComponent(testUser.nickname)}&avatar=${encodeURIComponent(testUser.avatarUrl)}`,
          success: () => {
            addLog("跳转聊天页面成功");
          },
          fail: (error) => {
            addLog(`跳转聊天页面失败: ${JSON.stringify(error)}`);
          }
        });
      } catch (error) {
        addLog(`跳转聊天页面异常: ${error.message}`);
      }
    }
    function clearLogs() {
      logs.value = [];
      addLog("日志已清空");
    }
    common_vendor.onMounted(() => {
      addLog("滚动错误修复测试页面已加载");
      for (let i = 1; i <= 10; i++) {
        messages.value.push(`初始测试消息 ${i}`);
      }
      addLog(`添加了 ${messages.value.length} 条初始消息`);
    });
    common_vendor.onUnmounted(() => {
      isDestroyed.value = true;
      addLog("测试页面已销毁");
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(testNormalScroll),
        b: common_vendor.o(testRapidScroll),
        c: common_vendor.o(testScrollAfterDestroy),
        d: common_vendor.o(testNavigateToChat),
        e: common_vendor.o(clearLogs),
        f: common_vendor.f(messages.value, (message, index, i0) => {
          return {
            a: common_vendor.t(message),
            b: index,
            c: `message-${index}`
          };
        }),
        g: common_vendor.unref(scrollTop),
        h: common_vendor.unref(scrollToView),
        i: common_vendor.f(logs.value, (log, index, i0) => {
          return {
            a: common_vendor.t(log),
            b: index,
            c: log.includes("错误") || log.includes("失败") ? 1 : ""
          };
        })
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-972e72a2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/scroll-error-test.js.map
