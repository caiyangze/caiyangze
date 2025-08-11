"use strict";
const common_vendor = require("../../common/vendor.js");
const api_aiMatchmaker = require("../../api/ai-matchmaker.js");
const _sfc_main = {
  __name: "ai-matchmaker-test",
  setup(__props) {
    const aiInfoResult = common_vendor.ref(null);
    const chatResult = common_vendor.ref(null);
    const speechResult = common_vendor.ref(null);
    const testMessage = common_vendor.ref("你好，我是新用户，请介绍一下你自己");
    const speechText = common_vendor.ref("你好，我是小红娘，很高兴为您服务！");
    const logs = common_vendor.ref([]);
    function addLog(message) {
      const timestamp = (/* @__PURE__ */ new Date()).toLocaleTimeString();
      logs.value.unshift(`[${timestamp}] ${message}`);
      common_vendor.index.__f__("log", "at pages/test/ai-matchmaker-test.vue:86", message);
    }
    async function testGetAiInfo() {
      try {
        addLog("开始测试获取AI信息...");
        const result = await api_aiMatchmaker.getAiMatchmakerInfo();
        aiInfoResult.value = result;
        addLog("AI信息获取成功: " + JSON.stringify(result));
      } catch (error) {
        addLog("AI信息获取失败: " + error.message);
        common_vendor.index.__f__("error", "at pages/test/ai-matchmaker-test.vue:98", "测试失败:", error);
      }
    }
    async function testChat() {
      if (!testMessage.value.trim()) {
        addLog("请输入测试消息");
        return;
      }
      try {
        addLog("开始测试文字对话...");
        const userId = common_vendor.index.getStorageSync("userId") || 1;
        const result = await api_aiMatchmaker.chatWithAi(userId, testMessage.value);
        chatResult.value = result;
        addLog("对话测试成功: " + JSON.stringify(result));
      } catch (error) {
        addLog("对话测试失败: " + error.message);
        common_vendor.index.__f__("error", "at pages/test/ai-matchmaker-test.vue:117", "测试失败:", error);
      }
    }
    async function testTextToSpeech() {
      if (!speechText.value.trim()) {
        addLog("请输入要合成的文字");
        return;
      }
      try {
        addLog("开始测试语音合成...");
        const userId = common_vendor.index.getStorageSync("userId") || 1;
        const result = await api_aiMatchmaker.textToSpeech(userId, speechText.value, "LONGXIAOCHUN");
        speechResult.value = result;
        addLog("语音合成成功: 大小 " + result.audioSize + " 字节");
      } catch (error) {
        addLog("语音合成失败: " + error.message);
        common_vendor.index.__f__("error", "at pages/test/ai-matchmaker-test.vue:136", "测试失败:", error);
      }
    }
    async function playSpeech() {
      if (!speechResult.value || !speechResult.value.audioBase64) {
        addLog("没有可播放的语音");
        return;
      }
      try {
        addLog("开始播放语音...");
        await api_aiMatchmaker.playAudioFromBase64(speechResult.value.audioBase64);
        addLog("语音播放完成");
      } catch (error) {
        addLog("语音播放失败: " + error.message);
        common_vendor.index.__f__("error", "at pages/test/ai-matchmaker-test.vue:153", "播放失败:", error);
      }
    }
    function testNavigation() {
      try {
        addLog("测试导航到AI聊天页面...");
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
        addLog("导航测试失败: " + error.message);
      }
    }
    function goToMessagePage() {
      try {
        addLog("前往消息页面...");
        common_vendor.index.navigateTo({
          url: "/pages/message/message",
          success: () => {
            addLog("成功跳转到消息页面");
          },
          fail: (error) => {
            addLog("跳转失败: " + JSON.stringify(error));
          }
        });
      } catch (error) {
        addLog("跳转失败: " + error.message);
      }
    }
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(testGetAiInfo),
        b: aiInfoResult.value
      }, aiInfoResult.value ? {
        c: common_vendor.t(JSON.stringify(aiInfoResult.value, null, 2))
      } : {}, {
        d: testMessage.value,
        e: common_vendor.o(($event) => testMessage.value = $event.detail.value),
        f: common_vendor.o(testChat),
        g: chatResult.value
      }, chatResult.value ? {
        h: common_vendor.t(JSON.stringify(chatResult.value, null, 2))
      } : {}, {
        i: speechText.value,
        j: common_vendor.o(($event) => speechText.value = $event.detail.value),
        k: common_vendor.o(testTextToSpeech),
        l: speechResult.value
      }, speechResult.value ? {
        m: common_vendor.t(speechResult.value.audioSize),
        n: common_vendor.o(playSpeech)
      } : {}, {
        o: common_vendor.o(testNavigation),
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
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-1edb4245"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/test/ai-matchmaker-test.js.map
