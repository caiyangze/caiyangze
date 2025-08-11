"use strict";
const common_vendor = require("../../common/vendor.js");
const common_assets = require("../../common/assets.js");
const api_chat = require("../../api/chat.js");
const utils_websocket = require("../../utils/websocket.js");
const utils_tokenDebug = require("../../utils/token-debug.js");
if (!Math) {
  customTabBar();
}
const customTabBar = () => "../../components/custom-tab-bar.js";
const _sfc_main = {
  __name: "message",
  setup(__props) {
    const imageError = common_vendor.reactive({});
    const currentTabIndex = common_vendor.ref(0);
    const messageTabs = [
      { name: "全部", type: "all" },
      { name: "未读", type: "unread" },
      { name: "系统", type: "system" }
    ];
    const messages = common_vendor.ref([]);
    const loading = common_vendor.ref(false);
    common_vendor.ref(/* @__PURE__ */ new Set());
    common_vendor.ref(/* @__PURE__ */ new Map());
    let refreshTimer = null;
    function handleImageError(type) {
      imageError[type] = true;
    }
    function switchTab(index) {
      currentTabIndex.value = index;
    }
    async function loadMutualFriends() {
      try {
        loading.value = true;
        messages.value = [];
        common_vendor.index.__f__("log", "at pages/message/message.vue:131", "=== 开始加载好友列表 ===");
        const tokenStatus = utils_tokenDebug.checkTokenStatus();
        if (!tokenStatus.hasToken) {
          common_vendor.index.__f__("error", "at pages/message/message.vue:134", "❌ 没有token，无法加载好友列表");
          common_vendor.index.showToast({
            title: "请先登录",
            icon: "none"
          });
          return;
        }
        const response = await api_chat.getMutualFollowList();
        common_vendor.index.__f__("log", "at pages/message/message.vue:143", "好友列表API响应:", response);
        if (response.code === 200) {
          if (response.data && response.data.length > 0) {
            common_vendor.index.__f__("log", "at pages/message/message.vue:147", `找到 ${response.data.length} 个好友`);
            messages.value = response.data.map((friend) => ({
              id: null,
              // 暂时没有会话ID
              userId: friend.userId,
              name: friend.nickname || friend.name || `用户${friend.userId}`,
              avatar: friend.avatarUrl || friend.avatar || "/static/message/default-avatar.png",
              lastMessage: "点击开始聊天",
              time: "",
              unreadCount: 0,
              // 统一使用unreadCount
              isFriend: true,
              // 标记为好友
              isOnline: false,
              // 在线状态，默认离线
              lastSeen: friend.lastSeen || null
              // 最后在线时间
            }));
            await loadFriendsOnlineStatus();
            await loadUnreadCounts();
          } else {
            common_vendor.index.__f__("log", "at pages/message/message.vue:165", "没有好友数据，清空列表");
            messages.value = [];
          }
        } else {
          common_vendor.index.__f__("error", "at pages/message/message.vue:169", "好友列表API返回错误:", response);
          messages.value = [];
          common_vendor.index.showToast({
            title: response.message || "获取好友列表失败",
            icon: "none"
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/message.vue:177", "加载好友列表失败:", error);
        messages.value = [];
        if (error.code === 400 && error.message && error.message.includes("用户未登录")) {
          common_vendor.index.__f__("log", "at pages/message/message.vue:184", "检测到用户未登录错误，开始诊断...");
          await utils_tokenDebug.diagnoseLoginStatus();
          common_vendor.index.showModal({
            title: "登录状态异常",
            content: "检测到登录状态异常，请重新登录",
            showCancel: false,
            success: () => {
              common_vendor.index.reLaunch({
                url: "/pages/login/login"
              });
            }
          });
        } else {
          common_vendor.index.__f__("log", "at pages/message/message.vue:199", "好友列表加载失败，错误信息:", error);
          common_vendor.index.showToast({
            title: "加载失败: " + (error.message || "网络错误"),
            icon: "none"
          });
        }
      } finally {
        loading.value = false;
      }
    }
    async function loadConversations() {
      try {
        const friends = messages.value.filter((m) => m.isFriend);
        if (friends.length === 0) {
          common_vendor.index.__f__("log", "at pages/message/message.vue:216", "没有好友，跳过会话加载");
          return;
        }
        loading.value = true;
        const response = await api_chat.getConversationList();
        if (response.code === 200) {
          const conversations = response.data.map((conversation) => ({
            id: conversation.conversationId,
            userId: conversation.otherUserId,
            name: conversation.otherUserNickname,
            avatar: conversation.otherUserAvatar || "/static/message/default-avatar.png",
            lastMessage: conversation.lastMessageContent || "暂无消息",
            time: formatTime(conversation.lastMessageTime),
            unreadCount: conversation.unreadCount || 0,
            isFriend: false
            // 标记为会话
          }));
          messages.value.forEach((friend) => {
            if (friend.isFriend) {
              const conversation = conversations.find((c) => c.userId === friend.userId);
              if (conversation) {
                friend.id = conversation.id;
                friend.lastMessage = conversation.lastMessage;
                friend.time = conversation.time;
                friend.unreadCount = conversation.unreadCount;
              }
            }
          });
          common_vendor.index.__f__("log", "at pages/message/message.vue:247", "会话信息已更新到好友列表，不添加非好友会话");
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/message.vue:250", "加载会话列表失败:", error);
      } finally {
        loading.value = false;
      }
    }
    function formatTime(timeStr) {
      if (!timeStr)
        return "";
      const time = new Date(timeStr);
      const now = /* @__PURE__ */ new Date();
      const diff = now.getTime() - time.getTime();
      if (diff < 24 * 60 * 60 * 1e3 && now.getDate() === time.getDate()) {
        return time.toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" });
      } else if (diff < 48 * 60 * 60 * 1e3) {
        return "昨天";
      } else if (diff < 7 * 24 * 60 * 60 * 1e3) {
        const days = ["周日", "周一", "周二", "周三", "周四", "周五", "周六"];
        return days[time.getDay()];
      } else {
        return time.toLocaleDateString("zh-CN", { month: "2-digit", day: "2-digit" });
      }
    }
    async function loadFriendsOnlineStatus() {
      try {
        const friendIds = messages.value.filter((m) => m.isFriend).map((m) => m.userId);
        if (friendIds.length === 0) {
          common_vendor.index.__f__("log", "at pages/message/message.vue:290", "没有好友，跳过在线状态加载");
          return;
        }
        common_vendor.index.__f__("log", "at pages/message/message.vue:294", "批量加载好友在线状态:", friendIds);
        common_vendor.index.__f__("log", "at pages/message/message.vue:295", "发送请求参数:", { userIds: friendIds });
        const response = await api_chat.getBatchOnlineStatus(friendIds);
        common_vendor.index.__f__("log", "at pages/message/message.vue:299", "在线状态API响应:", response);
        if (response && response.code === 200) {
          const onlineStatusMap = response.data || {};
          common_vendor.index.__f__("log", "at pages/message/message.vue:303", "在线状态数据:", onlineStatusMap);
          messages.value.forEach((friend) => {
            if (friend.isFriend) {
              const status = onlineStatusMap[friend.userId];
              if (status) {
                friend.isOnline = status.isOnline || false;
                friend.lastSeen = status.lastSeen || null;
              } else {
                friend.isOnline = false;
                friend.lastSeen = null;
              }
            }
          });
          common_vendor.index.__f__(
            "log",
            "at pages/message/message.vue:320",
            "在线状态加载完成，更新后的好友列表:",
            messages.value.filter((m) => m.isFriend).map((m) => ({
              name: m.name,
              isOnline: m.isOnline,
              lastSeen: m.lastSeen
            }))
          );
        } else {
          common_vendor.index.__f__("error", "at pages/message/message.vue:328", "在线状态API返回错误:", response);
          await loadFriendsOnlineStatusFallback();
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/message.vue:333", "加载好友在线状态失败:", error);
        await loadFriendsOnlineStatusFallback();
      }
    }
    async function loadFriendsOnlineStatusFallback() {
      const friendIds = messages.value.filter((m) => m.isFriend).map((m) => m.userId);
      for (const friendId of friendIds) {
        try {
          const response = await api_chat.getOnlineStatus(friendId);
          if (response.code === 200) {
            const friend = messages.value.find((m) => m.userId === friendId);
            if (friend) {
              friend.isOnline = response.data.isOnline;
              friend.lastSeen = response.data.lastSeen;
            }
          }
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/message/message.vue:354", `获取用户${friendId}在线状态失败:`, error);
        }
      }
    }
    async function loadUnreadCounts() {
      try {
        const friendIds = messages.value.filter((m) => m.isFriend).map((m) => m.userId);
        if (friendIds.length === 0) {
          common_vendor.index.__f__("log", "at pages/message/message.vue:364", "没有好友，跳过未读消息数量加载");
          return;
        }
        common_vendor.index.__f__("log", "at pages/message/message.vue:368", "加载未读消息数量:", friendIds);
        common_vendor.index.__f__("log", "at pages/message/message.vue:369", "发送请求参数:", { userIds: friendIds });
        const response = await api_chat.getUnreadCounts(friendIds);
        common_vendor.index.__f__("log", "at pages/message/message.vue:372", "未读消息API响应:", response);
        if (response && response.code === 200) {
          const unreadCountsMap = response.data || {};
          common_vendor.index.__f__("log", "at pages/message/message.vue:376", "未读消息数据:", unreadCountsMap);
          messages.value.forEach((friend) => {
            if (friend.isFriend) {
              const unreadCount = unreadCountsMap[friend.userId];
              friend.unreadCount = typeof unreadCount === "number" ? unreadCount : 0;
            }
          });
          common_vendor.index.__f__(
            "log",
            "at pages/message/message.vue:386",
            "未读消息数量加载完成，更新后的好友列表:",
            messages.value.filter((m) => m.isFriend).map((m) => ({
              name: m.name,
              unreadCount: m.unreadCount
            }))
          );
        } else {
          common_vendor.index.__f__("error", "at pages/message/message.vue:393", "未读消息API返回错误:", response);
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/message.vue:396", "加载未读消息数量失败:", error);
      }
    }
    function getOfflineTime(lastSeen) {
      if (!lastSeen)
        return "离线";
      const now = /* @__PURE__ */ new Date();
      const lastSeenDate = new Date(lastSeen);
      const diffMs = now - lastSeenDate;
      const diffMinutes = Math.floor(diffMs / (1e3 * 60));
      const diffHours = Math.floor(diffMs / (1e3 * 60 * 60));
      const diffDays = Math.floor(diffMs / (1e3 * 60 * 60 * 24));
      if (diffMinutes < 1)
        return "刚刚离线";
      if (diffMinutes < 60)
        return `${diffMinutes}分钟前`;
      if (diffHours < 24)
        return `${diffHours}小时前`;
      if (diffDays < 7)
        return `${diffDays}天前`;
      return "离线";
    }
    function openChat(message) {
      try {
        const conversationId = message.id || "new";
        const userId = message.userId || "";
        const name = message.name || "未知用户";
        const avatar = message.avatar || "/static/message/default-avatar.png";
        common_vendor.index.__f__("log", "at pages/message/message.vue:429", "准备导航到聊天页面:", { conversationId, userId, name, avatar });
        common_vendor.index.navigateTo({
          url: `/pages/message/chat?conversationId=${conversationId}&userId=${userId}&name=${encodeURIComponent(name)}&avatar=${encodeURIComponent(avatar)}`,
          success: () => {
            common_vendor.index.__f__("log", "at pages/message/message.vue:434", "导航成功");
            const index = messages.value.findIndex((item) => item.userId === message.userId);
            if (index !== -1) {
              messages.value[index].unreadCount = 0;
            }
          },
          fail: (error) => {
            common_vendor.index.__f__("error", "at pages/message/message.vue:442", "导航失败:", error);
            common_vendor.index.showToast({
              title: "打开聊天页面失败",
              icon: "none"
            });
          }
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/message.vue:450", "openChat error:", error);
        common_vendor.index.showToast({
          title: "操作失败",
          icon: "none"
        });
      }
    }
    function goSearch() {
      common_vendor.index.navigateTo({
        url: "/pages/search/search"
      });
    }
    function goExplore() {
      common_vendor.index.reLaunch({
        url: "/pages/square/square"
      });
    }
    async function initWebSocket() {
      try {
        const token = common_vendor.index.getStorageSync("token");
        if (token) {
          await utils_websocket.wsManager.connect(token);
          utils_websocket.wsManager.onMessage("CHAT", (message) => {
            common_vendor.index.__f__("log", "at pages/message/message.vue:483", "收到新消息:", message);
            loadConversations();
          });
          utils_websocket.wsManager.onMessage("SENT", (message) => {
            common_vendor.index.__f__("log", "at pages/message/message.vue:490", "消息发送成功:", message);
          });
        }
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/message/message.vue:494", "WebSocket连接失败:", error);
      }
    }
    common_vendor.onMounted(() => {
      loadMutualFriends().then(() => {
        loadConversations();
      });
      initWebSocket();
      initWebSocketListeners();
      refreshTimer = setInterval(() => {
        if (messages.value.length > 0) {
          loadFriendsOnlineStatus();
          loadUnreadCounts();
        }
      }, 3e4);
    });
    function initWebSocketListeners() {
      utils_websocket.wsManager.onMessage("USER_STATUS", (data) => {
        common_vendor.index.__f__("log", "at pages/message/message.vue:520", "收到用户状态变化:", data);
        const friend = messages.value.find((m) => m.userId === data.userId);
        if (friend) {
          friend.isOnline = data.isOnline;
          friend.lastSeen = data.lastSeen || (/* @__PURE__ */ new Date()).toISOString();
          common_vendor.index.__f__("log", "at pages/message/message.vue:525", `用户${data.userId}状态更新为:`, data.isOnline ? "在线" : "离线");
        }
      });
      utils_websocket.wsManager.onMessage("USER_ONLINE", (data) => {
        common_vendor.index.__f__("log", "at pages/message/message.vue:531", "用户上线:", data);
        const friend = messages.value.find((m) => m.userId === data.userId);
        if (friend) {
          friend.isOnline = true;
          friend.lastSeen = (/* @__PURE__ */ new Date()).toISOString();
        }
      });
      utils_websocket.wsManager.onMessage("USER_OFFLINE", (data) => {
        common_vendor.index.__f__("log", "at pages/message/message.vue:541", "用户下线:", data);
        const friend = messages.value.find((m) => m.userId === data.userId);
        if (friend) {
          friend.isOnline = false;
          friend.lastSeen = data.lastSeen || (/* @__PURE__ */ new Date()).toISOString();
        }
      });
      utils_websocket.wsManager.onMessage("NEW_MESSAGE", (data) => {
        common_vendor.index.__f__("log", "at pages/message/message.vue:551", "收到新消息:", data);
        updateMessageWithNewMessage(data);
      });
      utils_websocket.wsManager.onMessage("UNREAD_COUNT_CHANGED", (data) => {
        common_vendor.index.__f__("log", "at pages/message/message.vue:557", "未读消息数量变化:", data);
        updateUnreadCount(data.userId, data.unreadCount);
      });
    }
    function updateMessageWithNewMessage(messageData) {
      const friend = messages.value.find((m) => m.userId === messageData.senderId);
      if (friend) {
        friend.lastMessage = messageData.content || "新消息";
        friend.time = formatTime(new Date(messageData.timestamp));
        friend.unreadCount = (friend.unreadCount || 0) + 1;
        const index = messages.value.findIndex((m) => m.userId === messageData.senderId);
        if (index > 0) {
          const friendData = messages.value.splice(index, 1)[0];
          messages.value.unshift(friendData);
        }
        common_vendor.index.__f__("log", "at pages/message/message.vue:579", `收到来自用户${messageData.senderId}的新消息，未读数量: ${friend.unreadCount}`);
      }
    }
    function updateUnreadCount(userId, unreadCount) {
      const friend = messages.value.find((m) => m.userId === userId);
      if (friend) {
        friend.unreadCount = unreadCount;
      }
    }
    common_vendor.onUnmounted(() => {
      utils_websocket.wsManager.disconnect();
      if (refreshTimer) {
        clearInterval(refreshTimer);
        refreshTimer = null;
      }
    });
    return (_ctx, _cache) => {
      return common_vendor.e({
        a: common_vendor.o(goSearch),
        b: common_vendor.f(messageTabs, (tab, index, i0) => {
          return {
            a: common_vendor.t(tab.name),
            b: index,
            c: currentTabIndex.value === index ? 1 : "",
            d: common_vendor.o(($event) => switchTab(index), index)
          };
        }),
        c: common_vendor.f(messages.value, (message, index, i0) => {
          return common_vendor.e({
            a: imageError[`message${index}`] ? "/static/message/default-avatar.png" : message.avatar || "/static/message/default-avatar.png",
            b: common_vendor.o(($event) => handleImageError(`message${index}`), index),
            c: imageError[`message${index}`]
          }, imageError[`message${index}`] ? {
            d: common_vendor.t(message.name ? message.name.charAt(0) : "?")
          } : {}, {
            e: message.isFriend
          }, message.isFriend ? {
            f: message.isOnline ? 1 : ""
          } : {}, {
            g: message.unreadCount > 0
          }, message.unreadCount > 0 ? {
            h: common_vendor.t(message.unreadCount > 99 ? "99+" : message.unreadCount)
          } : {}, {
            i: common_vendor.t(message.name),
            j: message.isFriend
          }, message.isFriend ? {
            k: common_vendor.t(message.isOnline ? "在线" : getOfflineTime(message.lastSeen)),
            l: message.isOnline ? 1 : ""
          } : {}, {
            m: common_vendor.t(message.time),
            n: common_vendor.t(message.lastMessage),
            o: index,
            p: common_vendor.o(($event) => openChat(message), index)
          });
        }),
        d: messages.value.length === 0
      }, messages.value.length === 0 ? {
        e: common_assets._imports_0$2,
        f: common_vendor.o(goExplore)
      } : {});
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-4c1b26cf"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/message/message.js.map
