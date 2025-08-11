"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_aiMatchmaker = require("../../api/ai-matchmaker.js");
if (!Array) {
  const _component_uni_popup = common_vendor.resolveComponent("uni-popup");
  _component_uni_popup();
}
const _sfc_main = {
  __name: "ai-chat",
  props: {
    type: String,
    name: String,
    avatar: String
  },
  setup(__props) {
    let audioRecorder = null;
    const aiInfo = common_vendor.reactive({
      name: "小红娘",
      avatar: "/static/ai-matchmaker/xiaohongniang-avatar.png",
      status: "在线",
      greeting: "您好！我是小红娘，很高兴为您服务！有什么可以帮助您的吗？ 💕"
    });
    const messages = common_vendor.ref([]);
    const inputMessage = common_vendor.ref("");
    const isAiTyping = common_vendor.ref(false);
    const scrollTop = common_vendor.ref(0);
    const avatarError = common_vendor.ref(false);
    const isVoiceMode = common_vendor.ref(false);
    const isRecording = common_vendor.ref(false);
    const showFunctionButtons = common_vendor.ref(false);
    function handleAvatarError() {
      avatarError.value = true;
    }
    function goBack() {
      common_vendor.index.navigateBack();
    }
    function getCurrentTime() {
      return (/* @__PURE__ */ new Date()).toLocaleTimeString("zh-CN", {
        hour: "2-digit",
        minute: "2-digit"
      });
    }
    function formatTime(timestamp) {
      if (!timestamp)
        return "";
      const date = new Date(timestamp);
      return date.toLocaleTimeString("zh-CN", {
        hour: "2-digit",
        minute: "2-digit"
      });
    }
    function formatMessage(content) {
      if (!content)
        return "";
      return content.replace(/\n/g, "<br>");
    }
    async function sendMessage() {
      if (!inputMessage.value.trim())
        return;
      const userMessage = {
        type: "user",
        content: inputMessage.value.trim(),
        timestamp: Date.now()
      };
      messages.value.push(userMessage);
      const messageContent = inputMessage.value.trim();
      inputMessage.value = "";
      scrollToBottom();
      isAiTyping.value = true;
      try {
        const response = await callAiApi(messageContent);
        const aiMessage = {
          type: "ai",
          content: response.aiResponse || "抱歉，我现在无法回复，请稍后再试。",
          timestamp: Date.now()
        };
        messages.value.push(aiMessage);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:297", "AI回复失败:", error);
        const errorMessage = {
          type: "ai",
          content: "抱歉，我现在遇到了一些问题，请稍后再试。",
          timestamp: Date.now()
        };
        messages.value.push(errorMessage);
      } finally {
        isAiTyping.value = false;
        scrollToBottom();
      }
    }
    async function callAiApi(message) {
      try {
        const userId = common_vendor.index.getStorageSync("userId") || 1;
        common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:316", "发送AI请求:", { userId, message });
        const response = await api_aiMatchmaker.chatWithAi(userId, message);
        common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:320", "AI响应:", response);
        if (response.success) {
          return response;
        } else {
          throw new Error(response.message || "请求失败");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:328", "AI API调用失败:", error);
        throw error;
      }
    }
    function scrollToBottom() {
      common_vendor.nextTick$1(() => {
        scrollTop.value = 99999;
      });
    }
    function toggleFunctionButtons() {
      showFunctionButtons.value = !showFunctionButtons.value;
    }
    function toggleVoiceInput() {
      isVoiceMode.value = !isVoiceMode.value;
      showFunctionButtons.value = false;
    }
    async function startRecording() {
      try {
        if (!audioRecorder) {
          audioRecorder = new api_aiMatchmaker.AudioRecorder();
        }
        isRecording.value = true;
        common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:359", "开始录音...");
        await audioRecorder.startRecording();
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:365", "开始录音失败:", error);
        isRecording.value = false;
        common_vendor.index.showToast({
          title: "录音失败: " + error.message,
          icon: "none"
        });
      }
    }
    async function stopRecording() {
      try {
        if (!audioRecorder || !isRecording.value) {
          return;
        }
        common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:381", "停止录音...");
        audioRecorder.stopRecording();
        const recordResult = await new Promise((resolve, reject) => {
          var _a, _b;
          const originalResolve = (_a = audioRecorder.recordingPromise) == null ? void 0 : _a.resolve;
          const originalReject = (_b = audioRecorder.recordingPromise) == null ? void 0 : _b.reject;
          audioRecorder.recordingPromise = {
            resolve: (result) => {
              if (originalResolve)
                originalResolve(result);
              resolve(result);
            },
            reject: (error) => {
              if (originalReject)
                originalReject(error);
              reject(error);
            }
          };
        });
        isRecording.value = false;
        if (recordResult && recordResult.tempFilePath) {
          common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:404", "录音完成:", recordResult);
          common_vendor.index.showLoading({
            title: "识别中..."
          });
          try {
            const userId = common_vendor.index.getStorageSync("userId") || 1;
            const audioBase64 = await api_aiMatchmaker.fileToBase64(recordResult.tempFilePath);
            const audioFile = {
              name: "audio.wav",
              type: "audio/wav",
              data: audioBase64
            };
            const recognitionResult = await api_aiMatchmaker.speechToText(userId, audioFile);
            common_vendor.index.hideLoading();
            if (recognitionResult.success && recognitionResult.recognizedText) {
              inputMessage.value = recognitionResult.recognizedText;
              await sendMessage();
            } else {
              common_vendor.index.showToast({
                title: "未识别到语音内容",
                icon: "none"
              });
            }
          } catch (error) {
            common_vendor.index.hideLoading();
            common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:440", "语音识别失败:", error);
            common_vendor.index.showToast({
              title: "语音识别失败",
              icon: "none"
            });
          }
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:449", "停止录音失败:", error);
        isRecording.value = false;
        common_vendor.index.showToast({
          title: "录音失败",
          icon: "none"
        });
      }
    }
    function cancelRecording() {
      if (audioRecorder) {
        audioRecorder.cancelRecording();
      }
      isRecording.value = false;
    }
    function selectImage() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: async (res) => {
          try {
            common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:474", "选择图片:", res);
            const tempFilePath = res.tempFilePaths[0];
            common_vendor.index.showLoading({
              title: "分析中..."
            });
            const userId = common_vendor.index.getStorageSync("userId") || 1;
            const imageBase64 = await api_aiMatchmaker.fileToBase64(tempFilePath);
            const imageFile = {
              name: "image.jpg",
              type: "image/jpeg",
              data: imageBase64
            };
            const analysisResult = await api_aiMatchmaker.analyzeImage(userId, imageFile, "请帮我分析这张图片");
            common_vendor.index.hideLoading();
            if (analysisResult.success) {
              const userMessage = {
                type: "user",
                content: "[图片]",
                image: tempFilePath,
                timestamp: Date.now()
              };
              messages.value.push(userMessage);
              const aiMessage = {
                type: "ai",
                content: analysisResult.aiResponse,
                timestamp: Date.now()
              };
              messages.value.push(aiMessage);
              scrollToBottom();
            } else {
              common_vendor.index.showToast({
                title: "图片分析失败",
                icon: "none"
              });
            }
          } catch (error) {
            common_vendor.index.hideLoading();
            common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:524", "图片分析失败:", error);
            common_vendor.index.showToast({
              title: "图片分析失败",
              icon: "none"
            });
          }
        },
        fail: (error) => {
          common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:532", "选择图片失败:", error);
        }
      });
      showFunctionButtons.value = false;
    }
    function generateImage() {
      const imagePrompt = "请帮我生成一张图片：";
      inputMessage.value = imagePrompt;
      showFunctionButtons.value = false;
      common_vendor.nextTick$1(() => {
      });
    }
    function getMatchAdvice() {
      const adviceMessage = "请为我提供一些交友和匹配的建议";
      inputMessage.value = adviceMessage;
      sendMessage();
      showFunctionButtons.value = false;
    }
    async function speakMessage(content) {
      try {
        if (!content || content.trim() === "") {
          common_vendor.index.showToast({
            title: "没有内容可以朗读",
            icon: "none"
          });
          return;
        }
        common_vendor.index.showLoading({
          title: "生成语音..."
        });
        const userId = common_vendor.index.getStorageSync("userId") || 1;
        const speechResult = await api_aiMatchmaker.textToSpeech(userId, content, "LONGXIAOCHUN");
        common_vendor.index.hideLoading();
        if (speechResult.success && speechResult.audioBase64) {
          await api_aiMatchmaker.playAudioFromBase64(speechResult.audioBase64);
        } else {
          common_vendor.index.showToast({
            title: "语音生成失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.hideLoading();
        common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:590", "语音合成失败:", error);
        common_vendor.index.showToast({
          title: "语音合成失败",
          icon: "none"
        });
      }
    }
    function copyMessage(content) {
      common_vendor.index.setClipboardData({
        data: content,
        success: () => {
          common_vendor.index.showToast({
            title: "已复制",
            icon: "success"
          });
        }
      });
    }
    function showMoreOptions() {
      this.$refs.morePopup.open();
    }
    function closeMoreOptions() {
      this.$refs.morePopup.close();
    }
    function clearChat() {
      common_vendor.index.showModal({
        title: "确认清空",
        content: "确定要清空所有聊天记录吗？",
        success: (res) => {
          if (res.confirm) {
            messages.value = [];
            this.$refs.morePopup.close();
          }
        }
      });
    }
    function exportChat() {
      common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:638", "导出聊天记录");
      this.$refs.morePopup.close();
    }
    function aiSettings() {
      common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:645", "AI设置");
      this.$refs.morePopup.close();
    }
    function onInputChange() {
    }
    function loadMoreMessages() {
      common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:657", "加载更多消息");
    }
    common_vendor.onMounted(async () => {
      try {
        const pages = getCurrentPages();
        const currentPage = pages[pages.length - 1];
        const options = currentPage.options;
        if (options.name) {
          aiInfo.name = decodeURIComponent(options.name);
        }
        if (options.avatar) {
          aiInfo.avatar = decodeURIComponent(options.avatar);
        }
        try {
          const aiInfoResponse = await api_aiMatchmaker.getAiMatchmakerInfo();
          if (aiInfoResponse.success) {
            Object.assign(aiInfo, aiInfoResponse);
          }
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:682", "获取AI信息失败:", error);
        }
        audioRecorder = new api_aiMatchmaker.AudioRecorder();
        common_vendor.index.__f__("log", "at pages/message/ai-chat.vue:688", "AI聊天页面初始化完成:", aiInfo);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/ai-chat.vue:691", "页面初始化失败:", error);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: aiInfo.avatar,
        c: common_vendor.o(handleAvatarError),
        d: avatarError.value
      }, avatarError.value ? {} : {}, {
        e: common_vendor.t(aiInfo.name),
        f: common_vendor.t(aiInfo.status),
        g: common_vendor.o(showMoreOptions),
        h: messages.value.length === 0
      }, messages.value.length === 0 ? {
        i: aiInfo.avatar,
        j: common_vendor.t(aiInfo.greeting),
        k: common_vendor.t(getCurrentTime())
      } : {}, {
        l: common_vendor.f(messages.value, (message, index, i0) => {
          return common_vendor.e({
            a: message.type === "ai"
          }, message.type === "ai" ? {
            b: aiInfo.avatar,
            c: formatMessage(message.content),
            d: common_vendor.o(($event) => speakMessage(message.content), index),
            e: common_vendor.o(($event) => copyMessage(message.content), index),
            f: common_vendor.t(formatTime(message.timestamp))
          } : {
            g: common_vendor.t(message.content),
            h: common_vendor.t(formatTime(message.timestamp)),
            i: common_assets._imports_0$4
          }, {
            j: index,
            k: message.type === "user" ? 1 : "",
            l: message.type === "ai" ? 1 : ""
          });
        }),
        m: isAiTyping.value
      }, isAiTyping.value ? {
        n: aiInfo.avatar
      } : {}, {
        o: scrollTop.value,
        p: common_vendor.o(loadMoreMessages),
        q: showFunctionButtons.value
      }, showFunctionButtons.value ? {
        r: common_vendor.o(selectImage),
        s: common_vendor.o(toggleVoiceInput),
        t: common_vendor.o(generateImage),
        v: common_vendor.o(getMatchAdvice)
      } : {}, {
        w: common_vendor.t(showFunctionButtons.value ? "−" : "+"),
        x: common_vendor.o(toggleFunctionButtons),
        y: !isVoiceMode.value
      }, !isVoiceMode.value ? {
        z: common_vendor.o([($event) => inputMessage.value = $event.detail.value, onInputChange]),
        A: inputMessage.value
      } : {
        B: common_vendor.t(isRecording.value ? "松开发送" : "按住说话"),
        C: isRecording.value ? 1 : "",
        D: common_vendor.o(startRecording),
        E: common_vendor.o(stopRecording),
        F: common_vendor.o(cancelRecording)
      }, {
        G: !isVoiceMode.value && inputMessage.value.trim()
      }, !isVoiceMode.value && inputMessage.value.trim() ? {
        H: common_vendor.o(sendMessage)
      } : {
        I: common_vendor.t(isVoiceMode.value ? "⌨️" : "🎤"),
        J: common_vendor.o(toggleVoiceInput)
      }, {
        K: common_vendor.o(clearChat),
        L: common_vendor.o(exportChat),
        M: common_vendor.o(aiSettings),
        N: common_vendor.o(closeMoreOptions),
        O: common_vendor.sr("morePopup", "0694ab06-0"),
        P: common_vendor.p({
          type: "bottom"
        })
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-0694ab06"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/message/ai-chat.js.map
