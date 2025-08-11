"use strict";
const common_vendor = require("../../common/vendor.js");
const api_chat = require("../../api/chat.js");
const utils_websocket = require("../../utils/websocket.js");
const _sfc_main = {
  __name: "chat-test",
  setup(__props) {
    const testResult = common_vendor.ref("等待测试...\n");
    const targetUserId = common_vendor.ref("");
    function addResult(text) {
      testResult.value += (/* @__PURE__ */ new Date()).toLocaleTimeString() + ": " + text + "\n";
    }
    async function testMutualFriends() {
      try {
        addResult("开始测试获取好友列表...");
        const response = await api_chat.getMutualFollowList();
        addResult("API响应: " + JSON.stringify(response, null, 2));
      } catch (error) {
        addResult("测试失败: " + error.message);
      }
    }
    async function testCanChat() {
      try {
        addResult("开始测试canChat接口（默认用户ID: 1）...");
        const response = await api_chat.canChat(1);
        addResult("canChat响应: " + JSON.stringify(response, null, 2));
        if (response.code === 200) {
          if (response.data === true) {
            addResult("✅ 可以与用户1聊天（互相关注）");
          } else {
            addResult("❌ 不能与用户1聊天（未互相关注）");
          }
        } else {
          addResult("❌ 检查失败: " + response.message);
        }
      } catch (error) {
        addResult("测试失败: " + error.message);
      }
    }
    async function testCanChatWithId() {
      if (!targetUserId.value) {
        addResult("❌ 请输入目标用户ID");
        return;
      }
      try {
        addResult(`开始测试canChat接口（用户ID: ${targetUserId.value}）...`);
        const response = await api_chat.canChat(targetUserId.value);
        addResult("canChat响应: " + JSON.stringify(response, null, 2));
        if (response.code === 200) {
          if (response.data === true) {
            addResult(`✅ 可以与用户${targetUserId.value}聊天（互相关注）`);
          } else {
            addResult(`❌ 不能与用户${targetUserId.value}聊天（未互相关注）`);
          }
        } else {
          addResult("❌ 检查失败: " + response.message);
        }
      } catch (error) {
        addResult("测试失败: " + error.message);
      }
    }
    async function testWebSocket() {
      try {
        addResult("开始测试WebSocket连接...");
        const token = common_vendor.index.getStorageSync("token");
        if (!token) {
          addResult("错误: 未找到token，请先登录");
          return;
        }
        await utils_websocket.wsManager.connect(token);
        addResult("WebSocket连接成功");
        utils_websocket.wsManager.onMessage("CONNECTED", (message) => {
          addResult("收到连接确认: " + JSON.stringify(message));
        });
        utils_websocket.wsManager.onMessage("HEARTBEAT", (message) => {
          addResult("收到心跳响应: " + JSON.stringify(message));
        });
      } catch (error) {
        addResult("WebSocket连接失败: " + error.message);
      }
    }
    function testSendMessage() {
      try {
        addResult("开始测试发送消息...");
        if (!utils_websocket.wsManager.getConnectionStatus().isConnected) {
          addResult("错误: WebSocket未连接");
          return;
        }
        const success = utils_websocket.wsManager.sendChatMessage(1, "这是一条测试消息", 1);
        if (success) {
          addResult("消息发送成功");
        } else {
          addResult("消息发送失败");
        }
      } catch (error) {
        addResult("发送消息失败: " + error.message);
      }
    }
    function goToMessage() {
      common_vendor.index.navigateTo({
        url: "/pages/message/message"
      });
    }
    function goToChat() {
      common_vendor.index.navigateTo({
        url: "/pages/message/chat?conversationId=new&userId=1&name=测试用户&avatar=/static/message/default-avatar.png"
      });
    }
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(testMutualFriends),
        b: common_vendor.o(testCanChat),
        c: common_vendor.o(testWebSocket),
        d: common_vendor.o(testSendMessage),
        e: targetUserId.value,
        f: common_vendor.o(($event) => targetUserId.value = $event.detail.value),
        g: common_vendor.o(testCanChatWithId),
        h: common_vendor.t(testResult.value),
        i: common_vendor.o(goToMessage),
        j: common_vendor.o(goToChat)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d6d77a4c"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/chat-test.js.map
