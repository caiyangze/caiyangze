"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "ai-simple-test",
  setup(__props) {
    const userInfo = common_vendor.ref({
      userId: null,
      username: "",
      nickname: "",
      avatar: ""
    });
    const connectionResult = common_vendor.ref(null);
    const chatResult = common_vendor.ref("");
    const testMessage = common_vendor.ref("你好，我是新用户，请介绍一下你自己");
    const logs = common_vendor.ref([]);
    function addLog(message) {
      const timestamp = (/* @__PURE__ */ new Date()).toLocaleTimeString();
      logs.value.unshift(`[${timestamp}] ${message}`);
      common_vendor.index.__f__("log", "at pages/test/ai-simple-test.vue:87", message);
    }
    function loadUserInfo() {
      try {
        const storedUserInfo = common_vendor.index.getStorageSync("userInfo");
        const userId = common_vendor.index.getStorageSync("userId");
        addLog("开始加载用户信息...");
        addLog("存储的用户信息: " + JSON.stringify(storedUserInfo));
        addLog("存储的用户ID: " + userId);
        if (storedUserInfo) {
          userInfo.value = {
            userId: userId || storedUserInfo.userId || storedUserInfo.id,
            username: storedUserInfo.username || storedUserInfo.name || "未知用户",
            nickname: storedUserInfo.nickname || storedUserInfo.username || "未知昵称",
            avatar: storedUserInfo.avatarUrl || storedUserInfo.avatar || "/static/message/default-avatar.png"
          };
          addLog("用户信息加载成功");
        } else {
          userInfo.value = {
            userId: userId || 1,
            username: "测试用户",
            nickname: "测试用户",
            avatar: "/static/message/default-avatar.png"
          };
          addLog("使用默认用户信息");
        }
        addLog("最终用户信息: " + JSON.stringify(userInfo.value));
      } catch (error) {
        addLog("加载用户信息失败: " + error.message);
      }
    }
    async function testConnection() {
      try {
        addLog("开始测试后端连接...");
        const response = await common_vendor.index.request({
          url: "http://localhost:8084/api/ai-matchmaker/info",
          method: "GET",
          timeout: 1e4
        });
        addLog("连接响应: " + JSON.stringify(response));
        if (response.statusCode === 200) {
          connectionResult.value = {
            success: true,
            message: "后端连接成功！"
          };
          addLog("后端连接测试成功");
        } else {
          connectionResult.value = {
            success: false,
            message: `连接失败，状态码: ${response.statusCode}`
          };
          addLog("后端连接测试失败: 状态码 " + response.statusCode);
        }
      } catch (error) {
        connectionResult.value = {
          success: false,
          message: "连接失败: " + error.message
        };
        addLog("后端连接测试失败: " + error.message);
      }
    }
    async function testChat() {
      if (!testMessage.value.trim()) {
        addLog("请输入测试消息");
        return;
      }
      try {
        addLog("开始测试AI对话...");
        addLog("发送消息: " + testMessage.value);
        const response = await common_vendor.index.request({
          url: "http://localhost:8084/api/ai-matchmaker/chat",
          method: "POST",
          header: {
            "Content-Type": "application/x-www-form-urlencoded"
          },
          data: {
            userId: userInfo.value.userId,
            message: testMessage.value
          },
          timeout: 3e4
        });
        addLog("对话响应: " + JSON.stringify(response));
        if (response.statusCode === 200 && response.data.success) {
          chatResult.value = response.data.aiResponse;
          addLog("AI对话测试成功");
        } else {
          chatResult.value = "对话失败: " + (response.data.message || "未知错误");
          addLog("AI对话测试失败: " + (response.data.message || "未知错误"));
        }
      } catch (error) {
        chatResult.value = "对话失败: " + error.message;
        addLog("AI对话测试失败: " + error.message);
      }
    }
    function goToAiChat() {
      try {
        addLog("导航到AI聊天页面...");
        common_vendor.index.navigateTo({
          url: "/pages/message/ai-chat?type=ai-matchmaker&name=小红娘&avatar=/static/ai-matchmaker/xiaohongniang-avatar.svg",
          success: () => {
            addLog("导航成功");
          },
          fail: (error) => {
            addLog("导航失败: " + JSON.stringify(error));
          }
        });
      } catch (error) {
        addLog("导航失败: " + error.message);
      }
    }
    function goToMessagePage() {
      try {
        addLog("返回消息页面...");
        common_vendor.index.navigateTo({
          url: "/pages/message/message",
          success: () => {
            addLog("返回成功");
          },
          fail: (error) => {
            addLog("返回失败: " + JSON.stringify(error));
          }
        });
      } catch (error) {
        addLog("返回失败: " + error.message);
      }
    }
    common_vendor.onMounted(() => {
      addLog("页面初始化开始");
      loadUserInfo();
      addLog("页面初始化完成");
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.t(userInfo.value.userId),
        b: common_vendor.t(userInfo.value.username),
        c: common_vendor.t(userInfo.value.nickname),
        d: userInfo.value.avatar,
        e: common_vendor.t(userInfo.value.avatar),
        f: common_vendor.o(testConnection),
        g: connectionResult.value
      }, connectionResult.value ? {
        h: common_vendor.t(connectionResult.value.message),
        i: common_vendor.n(connectionResult.value.success ? "success" : "error")
      } : {}, {
        j: testMessage.value,
        k: common_vendor.o(($event) => testMessage.value = $event.detail.value),
        l: common_vendor.o(testChat),
        m: chatResult.value
      }, chatResult.value ? {
        n: common_vendor.t(chatResult.value)
      } : {}, {
        o: common_vendor.o(goToAiChat),
        p: common_vendor.o(goToMessagePage),
        q: common_vendor.f(logs.value, (log, index, i0) => {
          return {
            a: common_vendor.t(log),
            b: index
          };
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-d97c6ded"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/ai-simple-test.js.map
