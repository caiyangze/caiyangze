"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "navigation-test",
  setup(__props) {
    const testResult = common_vendor.ref("等待测试...");
    function testChatNavigation() {
      try {
        testResult.value = "正在测试聊天页面导航...";
        common_vendor.index.navigateTo({
          url: "/pages/message/chat?conversationId=new&userId=1&name=测试用户&avatar=/static/message/default-avatar.png",
          success: () => {
            testResult.value = "聊天页面导航成功！";
          },
          fail: (error) => {
            testResult.value = "聊天页面导航失败：" + JSON.stringify(error);
          }
        });
      } catch (error) {
        testResult.value = "导航异常：" + error.message;
      }
    }
    function testMessageNavigation() {
      try {
        testResult.value = "正在测试消息页面导航...";
        common_vendor.index.navigateTo({
          url: "/pages/message/message",
          success: () => {
            testResult.value = "消息页面导航成功！";
          },
          fail: (error) => {
            testResult.value = "消息页面导航失败：" + JSON.stringify(error);
          }
        });
      } catch (error) {
        testResult.value = "导航异常：" + error.message;
      }
    }
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(testChatNavigation),
        b: common_vendor.o(testMessageNavigation),
        c: common_vendor.t(testResult.value)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-2de6417a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/navigation-test.js.map
