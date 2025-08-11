"use strict";
const common_vendor = require("../../common/vendor.js");
const api_chat = require("../../api/chat.js");
const utils_websocket = require("../../utils/websocket.js");
const _sfc_main = {
  __name: "chat",
  props: {
    conversationId: String,
    userId: String,
    name: String,
    avatar: String
  },
  setup(__props) {
    const props = __props;
    const chatUser = common_vendor.reactive({
      id: props.userId,
      name: decodeURIComponent(props.name || ""),
      avatar: decodeURIComponent(props.avatar || "/static/message/default-avatar.png")
    });
    const currentUser = common_vendor.reactive({
      id: null,
      avatar: "/static/message/default-avatar.png"
    });
    const messageList = common_vendor.ref([]);
    const inputMessage = common_vendor.ref("");
    const scrollTop = common_vendor.ref(0);
    const scrollToView = common_vendor.ref("");
    const isOnline = common_vendor.ref(false);
    const loadingMore = common_vendor.ref(false);
    const avatarError = common_vendor.ref(false);
    const showAvatarPlaceholder = common_vendor.ref(false);
    const currentPage = common_vendor.ref(1);
    const pageSize = common_vendor.ref(20);
    const hasMoreMessages = common_vendor.ref(true);
    const isInputFocused = common_vendor.ref(false);
    const typingTimer = common_vendor.ref(null);
    common_vendor.onMounted(() => {
      const pages = getCurrentPages();
      const currentPage2 = pages[pages.length - 1];
      const options = currentPage2.options;
      if (options.userId) {
        chatUser.id = options.userId;
        chatUser.name = decodeURIComponent(options.name || "");
        chatUser.avatar = decodeURIComponent(options.avatar || "/static/message/default-avatar.png");
        if (options.conversationId && options.conversationId !== "new") {
          loadChatHistory();
        }
        getCurrentUserInfo();
        initWebSocket();
        connectWebSocket();
        setTimeout(() => {
          scrollToBottom();
        }, 500);
      }
    });
    common_vendor.onShow(() => {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:252", "聊天页面显示");
      setTimeout(() => {
        scrollToBottom();
      }, 300);
    });
    async function getCurrentUserInfo() {
      const token = common_vendor.index.getStorageSync("token");
      common_vendor.index.__f__("log", "at pages/message/chat.vue:263", "=== 获取用户信息调试 ===");
      common_vendor.index.__f__("log", "at pages/message/chat.vue:264", "token:", token);
      if (!token) {
        common_vendor.index.__f__("warn", "at pages/message/chat.vue:267", "未找到token，使用默认用户信息");
        setDefaultUserInfo();
        return;
      }
      try {
        const { getByUserInfo } = await "../../api/user/auth.js";
        const result = await getByUserInfo(token);
        common_vendor.index.__f__("log", "at pages/message/chat.vue:277", "API返回的用户信息:", result);
        if (result && result.code === 200 && result.data) {
          const userInfo = result.data;
          currentUser.id = userInfo.userId || userInfo.id;
          currentUser.nickname = userInfo.nickname || userInfo.name || userInfo.username || "我";
          let avatarUrl = userInfo.avatarUrl || userInfo.avatar;
          common_vendor.index.__f__("log", "at pages/message/chat.vue:287", "API返回的头像路径:", avatarUrl);
          if (!avatarUrl || avatarUrl === "null" || avatarUrl === "undefined" || avatarUrl.trim() === "") {
            currentUser.avatar = "/static/message/default-avatar.png";
            showAvatarPlaceholder.value = true;
            common_vendor.index.__f__("log", "at pages/message/chat.vue:292", "使用默认头像和占位符");
          } else {
            currentUser.avatar = avatarUrl;
            showAvatarPlaceholder.value = false;
            common_vendor.index.__f__("log", "at pages/message/chat.vue:296", "使用API获取的真实头像:", currentUser.avatar);
          }
          common_vendor.index.setStorageSync("userInfo", userInfo);
        } else {
          common_vendor.index.__f__("warn", "at pages/message/chat.vue:302", "API返回数据格式错误:", result);
          setDefaultUserInfo();
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/chat.vue:306", "获取用户信息失败:", error);
        setDefaultUserInfo();
      }
      common_vendor.index.__f__("log", "at pages/message/chat.vue:310", "=== 最终用户信息 ===");
      common_vendor.index.__f__("log", "at pages/message/chat.vue:311", "currentUser:", currentUser);
      common_vendor.index.__f__("log", "at pages/message/chat.vue:312", "currentUser.avatar:", currentUser.avatar);
    }
    function setDefaultUserInfo() {
      currentUser.id = 1001;
      currentUser.avatar = "/static/message/default-avatar.png";
      currentUser.nickname = "我";
      showAvatarPlaceholder.value = true;
      common_vendor.index.__f__("log", "at pages/message/chat.vue:321", "已设置默认用户信息");
    }
    async function loadChatHistory(page = 1) {
      try {
        const pages = getCurrentPages();
        const currentPage2 = pages[pages.length - 1];
        const options = currentPage2.options;
        const conversationId = options.conversationId;
        if (!conversationId || conversationId === "new") {
          common_vendor.index.__f__("log", "at pages/message/chat.vue:334", "新会话，无历史记录");
          return;
        }
        if (page === 1) {
          messageList.value = [];
        } else {
          loadingMore.value = true;
        }
        const response = await api_chat.getChatHistory(conversationId, page, pageSize.value);
        if (response.code === 200) {
          const messages = response.data.map((msg) => ({
            ...msg,
            isSelf: msg.senderId == currentUser.id,
            status: msg.senderId == currentUser.id ? "sent" : "received"
          }));
          if (page === 1) {
            messageList.value = messages.reverse();
            common_vendor.nextTick$1(() => {
              scrollToBottom();
            });
            setTimeout(() => {
              scrollToBottom();
            }, 200);
            setTimeout(() => {
              scrollToBottom();
            }, 500);
          } else {
            messageList.value = [...messages.reverse(), ...messageList.value];
          }
          hasMoreMessages.value = messages.length === pageSize.value;
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/chat.vue:371", "加载聊天记录失败:", error);
        common_vendor.index.showToast({
          title: "加载失败",
          icon: "none"
        });
      } finally {
        loadingMore.value = false;
      }
    }
    function loadMoreMessages() {
      if (loadingMore.value || !hasMoreMessages.value)
        return;
      currentPage.value++;
      loadChatHistory(currentPage.value);
    }
    function initWebSocket() {
      utils_websocket.wsManager.onMessage("CHAT", (message) => {
        if (message.senderId == chatUser.id || message.receiverId == chatUser.id) {
          const newMessage = {
            ...message.data,
            messageId: message.messageId,
            senderId: message.senderId,
            isSelf: message.senderId == currentUser.id,
            status: "received",
            createdAt: (/* @__PURE__ */ new Date()).toISOString()
          };
          messageList.value.push(newMessage);
          common_vendor.nextTick$1(() => {
            scrollToBottom();
          });
          setTimeout(() => {
            scrollToBottom();
          }, 100);
          setTimeout(() => {
            scrollToBottom();
          }, 300);
          if (!newMessage.isSelf) {
            markAsRead(newMessage.messageId);
          }
        }
      });
      utils_websocket.wsManager.onMessage("SENT", (message) => {
        common_vendor.index.__f__("log", "at pages/message/chat.vue:429", "收到消息发送确认:", message);
        const index = messageList.value.findIndex((msg) => msg.tempId === message.tempId || msg.tempId === message.messageId);
        if (index > -1) {
          messageList.value[index].status = "sent";
          messageList.value[index].messageId = message.messageId;
          common_vendor.index.__f__("log", "at pages/message/chat.vue:434", "消息状态更新为已发送");
        }
      });
      utils_websocket.wsManager.onMessage("SEND_FAILED", (message) => {
        common_vendor.index.__f__("log", "at pages/message/chat.vue:440", "收到消息发送失败通知:", message);
        const index = messageList.value.findIndex((msg) => msg.tempId === message.tempId);
        if (index > -1) {
          messageList.value[index].status = "failed";
        }
      });
      utils_websocket.wsManager.onMessage("USER_STATUS", (data) => {
        common_vendor.index.__f__("log", "at pages/message/chat.vue:449", "收到用户状态更新:", data);
        if (data.userId == chatUser.id) {
          isOnline.value = data.isOnline;
        }
      });
      setTimeout(() => {
        if (utils_websocket.wsManager.isConnected) {
          utils_websocket.wsManager.send({
            type: "GET_USER_STATUS",
            data: { userId: chatUser.id }
          });
        }
      }, 1e3);
    }
    async function connectWebSocket() {
      const token = common_vendor.index.getStorageSync("token");
      if (!token) {
        common_vendor.index.__f__("error", "at pages/message/chat.vue:470", "没有token，无法连接WebSocket");
        common_vendor.index.showToast({
          title: "请先登录",
          icon: "none"
        });
        return;
      }
      try {
        common_vendor.index.__f__("log", "at pages/message/chat.vue:479", "正在连接WebSocket...");
        await utils_websocket.wsManager.connect(token);
        common_vendor.index.__f__("log", "at pages/message/chat.vue:481", "WebSocket连接成功");
        isOnline.value = true;
        setTimeout(() => {
          if (utils_websocket.wsManager.isConnected) {
            utils_websocket.wsManager.send({
              type: "GET_USER_STATUS",
              data: { userId: chatUser.id }
            });
          }
        }, 1e3);
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/chat.vue:497", "WebSocket连接失败:", error);
        isOnline.value = false;
        common_vendor.index.showToast({
          title: "WebSocket连接失败",
          icon: "none"
        });
      }
    }
    function sendMessage() {
      const content = inputMessage.value.trim();
      if (!content)
        return;
      const tempId = Date.now();
      const newMessage = {
        tempId,
        content,
        messageType: 1,
        isSelf: true,
        status: "sending",
        createdAt: (/* @__PURE__ */ new Date()).toISOString(),
        senderId: currentUser.id,
        senderAvatar: currentUser.avatar
      };
      common_vendor.index.__f__("log", "at pages/message/chat.vue:523", "发送消息:", newMessage);
      common_vendor.index.__f__("log", "at pages/message/chat.vue:524", "当前用户头像:", currentUser.avatar);
      messageList.value.push(newMessage);
      inputMessage.value = "";
      common_vendor.nextTick$1(() => {
        scrollToBottom();
      });
      setTimeout(() => {
        scrollToBottom();
      }, 100);
      setTimeout(() => {
        scrollToBottom();
      }, 300);
      if (!utils_websocket.wsManager.isConnected) {
        common_vendor.index.__f__("warn", "at pages/message/chat.vue:546", "WebSocket未连接，尝试重新连接...");
        const token = common_vendor.index.getStorageSync("token");
        if (token) {
          utils_websocket.wsManager.connect(token).then(() => {
            common_vendor.index.__f__("log", "at pages/message/chat.vue:550", "重连成功，重新发送消息");
            sendMessageWithWebSocket();
          }).catch((error) => {
            common_vendor.index.__f__("error", "at pages/message/chat.vue:553", "重连失败:", error);
            markMessageFailed();
          });
        } else {
          common_vendor.index.__f__("error", "at pages/message/chat.vue:557", "没有token，无法连接WebSocket");
          markMessageFailed();
        }
        return;
      }
      sendMessageWithWebSocket();
      async function sendMessageWithWebSocket() {
        try {
          const messageData = {
            receiverId: chatUser.id,
            content,
            messageType: 1
          };
          common_vendor.index.__f__("log", "at pages/message/chat.vue:575", "保存消息到数据库:", messageData);
          const response = await api_chat.sendMessage(messageData);
          if (response.code === 200) {
            common_vendor.index.__f__("log", "at pages/message/chat.vue:579", "消息保存成功:", response.data);
            const index = messageList.value.findIndex((msg) => msg.tempId === tempId);
            if (index > -1) {
              messageList.value[index].messageId = response.data.messageId;
              messageList.value[index].status = "sent";
            }
            const wsSuccess = utils_websocket.wsManager.sendChatMessage(chatUser.id, content, 1, tempId);
            if (wsSuccess) {
              common_vendor.index.__f__("log", "at pages/message/chat.vue:591", "WebSocket实时通知发送成功");
            } else {
              common_vendor.index.__f__("warn", "at pages/message/chat.vue:593", "WebSocket实时通知发送失败，但消息已保存");
            }
          } else {
            common_vendor.index.__f__("error", "at pages/message/chat.vue:597", "消息保存失败:", response);
            markMessageFailed();
          }
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/message/chat.vue:602", "发送消息API调用失败:", error);
          common_vendor.index.__f__("log", "at pages/message/chat.vue:605", "尝试仅通过WebSocket发送...");
          const wsSuccess = utils_websocket.wsManager.sendChatMessage(chatUser.id, content, 1, tempId);
          if (wsSuccess) {
            common_vendor.index.__f__("log", "at pages/message/chat.vue:608", "WebSocket发送成功，但未保存到数据库");
            setTimeout(() => {
              const index = messageList.value.findIndex((msg) => msg.tempId === tempId);
              if (index > -1 && messageList.value[index].status === "sending") {
                messageList.value[index].status = "sent";
                common_vendor.index.__f__("log", "at pages/message/chat.vue:615", "消息超时自动标记为已发送（可能未保存到数据库）");
              }
            }, 3e3);
          } else {
            markMessageFailed();
          }
        }
      }
      function markMessageFailed() {
        common_vendor.index.__f__("error", "at pages/message/chat.vue:627", "WebSocket发送失败");
        const index = messageList.value.findIndex((msg) => msg.tempId === tempId);
        if (index > -1) {
          messageList.value[index].status = "failed";
        }
        common_vendor.index.showToast({
          title: "发送失败，请重试",
          icon: "none"
        });
      }
    }
    function resendMessage(message) {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:642", "重发消息:", message);
      message.status = "sending";
      if (!utils_websocket.wsManager.isConnected) {
        common_vendor.index.__f__("warn", "at pages/message/chat.vue:647", "WebSocket未连接，尝试重新连接...");
        const token = common_vendor.index.getStorageSync("token");
        if (token) {
          utils_websocket.wsManager.connect(token).then(() => {
            common_vendor.index.__f__("log", "at pages/message/chat.vue:651", "重连成功，重新发送消息");
            doResendMessage(message);
          }).catch((error) => {
            common_vendor.index.__f__("error", "at pages/message/chat.vue:654", "重连失败:", error);
            message.status = "failed";
            common_vendor.index.showToast({
              title: "重发失败，请检查网络",
              icon: "none"
            });
          });
        } else {
          message.status = "failed";
          common_vendor.index.showToast({
            title: "重发失败，请重新登录",
            icon: "none"
          });
        }
        return;
      }
      doResendMessage(message);
    }
    function doResendMessage(message) {
      const success = utils_websocket.wsManager.sendChatMessage(chatUser.id, message.content, message.messageType, message.tempId);
      if (success) {
        common_vendor.index.__f__("log", "at pages/message/chat.vue:678", "重发成功，等待服务器确认...");
        setTimeout(() => {
          if (message.status === "sending") {
            message.status = "sent";
            common_vendor.index.__f__("log", "at pages/message/chat.vue:684", "重发消息超时自动标记为已发送");
          }
        }, 5e3);
      } else {
        message.status = "failed";
        common_vendor.index.showToast({
          title: "重发失败",
          icon: "none"
        });
      }
    }
    function onAvatarLoad(e) {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:699", "头像加载成功:", e.target.src);
      showAvatarPlaceholder.value = false;
    }
    function onSelfAvatarError(e) {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:705", "自己头像加载失败:", e.target.src);
      common_vendor.index.__f__("log", "at pages/message/chat.vue:706", "错误详情:", e);
      showAvatarPlaceholder.value = true;
    }
    function onAvatarError(e) {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:712", "对方头像加载失败:", e.target.src);
      common_vendor.index.__f__("log", "at pages/message/chat.vue:713", "错误详情:", e);
      e.target.src = "/static/message/default-avatar.png";
    }
    async function markAsRead(messageId) {
      try {
        const pages = getCurrentPages();
        const currentPage2 = pages[pages.length - 1];
        const options = currentPage2.options;
        const conversationId = options.conversationId;
        if (conversationId && conversationId !== "new") {
          await api_chat.markMessageAsRead(conversationId, messageId);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/chat.vue:730", "标记已读失败:", error);
      }
    }
    function onInputFocus() {
      isInputFocused.value = true;
    }
    function onInputBlur() {
      isInputFocused.value = false;
    }
    function onInputChange() {
      if (typingTimer.value) {
        clearTimeout(typingTimer.value);
      }
      utils_websocket.wsManager.sendTyping(chatUser.id);
      typingTimer.value = setTimeout(() => {
      }, 1e3);
    }
    function chooseImage() {
      common_vendor.index.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success: (res) => {
          common_vendor.index.__f__("log", "at pages/message/chat.vue:764", "选择图片:", res);
        }
      });
    }
    function previewImage(url) {
      common_vendor.index.previewImage({
        urls: [url]
      });
    }
    function showEmojiPanel() {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:779", "显示表情面板");
    }
    function shouldShowTime(message, index) {
      if (index === 0)
        return true;
      const prevMessage = messageList.value[index - 1];
      const currentTime = new Date(message.createdAt).getTime();
      const prevTime = new Date(prevMessage.createdAt).getTime();
      return currentTime - prevTime > 5 * 60 * 1e3;
    }
    function formatMessageTime(timeStr) {
      const time = new Date(timeStr);
      const now = /* @__PURE__ */ new Date();
      if (now.toDateString() === time.toDateString()) {
        return time.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" });
      } else {
        return time.toLocaleString("zh-CN", {
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit"
        });
      }
    }
    function getMessageTypeText(type) {
      const typeMap = {
        2: "[图片]",
        3: "[语音]",
        4: "[视频]",
        5: "[文件]",
        6: "[系统消息]"
      };
      return typeMap[type] || "[未知消息]";
    }
    function scrollToBottom() {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:822", "执行滚动到底部");
      scrollToView.value = "bottom-anchor";
      common_vendor.nextTick$1(() => {
        const query = common_vendor.index.createSelectorQuery();
        query.select(".message-list").boundingClientRect();
        query.exec((res) => {
          if (res[0]) {
            scrollTop.value = res[0].height + 1e3;
          }
        });
      });
      setTimeout(() => {
        scrollToView.value = "";
      }, 100);
    }
    function handleAvatarError() {
      avatarError.value = true;
    }
    function goBack() {
      common_vendor.index.__f__("log", "at pages/message/chat.vue:849", "尝试返回上一页...");
      const pages = getCurrentPages();
      common_vendor.index.__f__("log", "at pages/message/chat.vue:853", "当前页面栈长度:", pages.length);
      if (pages.length > 1) {
        common_vendor.index.navigateBack({
          success: () => {
            common_vendor.index.__f__("log", "at pages/message/chat.vue:859", "返回成功");
          },
          fail: (error) => {
            common_vendor.index.__f__("error", "at pages/message/chat.vue:862", "返回失败:", error);
            goToMessageList();
          }
        });
      } else {
        common_vendor.index.__f__("log", "at pages/message/chat.vue:869", "页面栈只有一页，直接跳转到消息列表");
        goToMessageList();
      }
    }
    function goToMessageList() {
      common_vendor.index.reLaunch({
        url: "/pages/message/message",
        success: () => {
          common_vendor.index.__f__("log", "at pages/message/chat.vue:879", "跳转到消息页面成功");
        },
        fail: (err) => {
          common_vendor.index.__f__("error", "at pages/message/chat.vue:882", "跳转到消息页面失败:", err);
          common_vendor.index.switchTab({
            url: "/pages/index/index"
          });
        }
      });
    }
    common_vendor.onUnmounted(() => {
      if (typingTimer.value) {
        clearTimeout(typingTimer.value);
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goBack),
        b: avatarError.value
      }, avatarError.value ? {} : {}, {
        c: chatUser.avatar,
        d: common_vendor.o(handleAvatarError),
        e: common_vendor.t(chatUser.name),
        f: common_vendor.t(isOnline.value ? "在线" : "离线"),
        g: isOnline.value ? 1 : "",
        h: loadingMore.value
      }, loadingMore.value ? {} : {}, {
        i: common_vendor.f(messageList.value, (message, index, i0) => {
          return common_vendor.e({
            a: shouldShowTime(message, index)
          }, shouldShowTime(message, index) ? {
            b: common_vendor.t(formatMessageTime(message.createdAt))
          } : {}, {
            c: !message.isSelf
          }, !message.isSelf ? common_vendor.e({
            d: chatUser.avatar || "/static/message/default-avatar.png",
            e: common_vendor.o(onAvatarError, message.messageId || index),
            f: message.messageType === 1
          }, message.messageType === 1 ? {
            g: common_vendor.t(message.content)
          } : message.messageType === 2 ? {
            i: message.mediaUrl,
            j: common_vendor.o(($event) => previewImage(message.mediaUrl), message.messageId || index)
          } : {
            k: common_vendor.t(getMessageTypeText(message.messageType))
          }, {
            h: message.messageType === 2
          }) : common_vendor.e({
            l: message.status === "sending"
          }, message.status === "sending" ? {} : message.status === "sent" ? {} : message.status === "read" ? {} : message.status === "failed" ? {
            p: common_vendor.o(($event) => resendMessage(message), message.messageId || index)
          } : {}, {
            m: message.status === "sent",
            n: message.status === "read",
            o: message.status === "failed",
            q: message.messageType === 1
          }, message.messageType === 1 ? {
            r: common_vendor.t(message.content)
          } : message.messageType === 2 ? {
            t: message.mediaUrl,
            v: common_vendor.o(($event) => previewImage(message.mediaUrl), message.messageId || index)
          } : {
            w: common_vendor.t(getMessageTypeText(message.messageType))
          }, {
            s: message.messageType === 2,
            x: currentUser.avatar || "/static/message/default-avatar.png",
            y: common_vendor.o(onSelfAvatarError, message.messageId || index),
            z: common_vendor.o(onAvatarLoad, message.messageId || index),
            A: showAvatarPlaceholder.value
          }, showAvatarPlaceholder.value ? {
            B: common_vendor.t(currentUser.nickname ? currentUser.nickname.charAt(0) : "我")
          } : {}), {
            C: message.messageId || index,
            D: `message-${message.messageId || message.tempId || index}`,
            E: message.isSelf ? 1 : ""
          });
        }),
        j: scrollToView.value,
        k: scrollTop.value,
        l: common_vendor.o(loadMoreMessages),
        m: common_vendor.o(showEmojiPanel),
        n: common_vendor.o(chooseImage),
        o: common_vendor.o(onInputFocus),
        p: common_vendor.o(onInputBlur),
        q: common_vendor.o([($event) => inputMessage.value = $event.detail.value, onInputChange]),
        r: common_vendor.o(sendMessage),
        s: inputMessage.value,
        t: inputMessage.value.trim() ? 1 : "",
        v: common_vendor.o(sendMessage)
      });
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-013fa921"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/message/chat.js.map
