"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const api_http = require("./http.js");
const canChat = (targetUserId) => {
  return api_http.http.get(`/chat/canChat/${targetUserId}`);
};
const getConversationList = () => {
  return api_http.http.get("/chat/conversations");
};
const getChatHistory = (conversationId, page = 1, size = 20) => {
  return api_http.http.get(`/chat/history/${conversationId}`, {
    page,
    size
  });
};
const sendMessage = (messageData) => {
  return api_http.http.post("/chat/send", messageData);
};
const markMessageAsRead = (conversationId, messageId) => {
  return api_http.http.post("/chat/read", {
    conversationId,
    messageId
  });
};
const recallMessage = (messageId) => {
  return api_http.http.post(`/chat/recall/${messageId}`);
};
const deleteConversation = (conversationId) => {
  return api_http.http.del(`/chat/conversation/${conversationId}`);
};
const getOnlineStatus = (userId) => {
  return api_http.http.get(`/chat/online/${userId}`);
};
const getOnlineStats = () => {
  return api_http.http.get("/chat/stats");
};
const getMutualFollowList = () => {
  return api_http.http.get("/follow/mutual");
};
const getBatchOnlineStatus = (userIds) => {
  return api_http.http.post("/chat/batchOnlineStatus", { userIds });
};
const getUnreadCounts = (userIds) => {
  return api_http.http.post("/chat/unreadCounts", { userIds });
};
const testChatApi = (data) => {
  return api_http.http.post("/chat/test", data);
};
exports.canChat = canChat;
exports.deleteConversation = deleteConversation;
exports.getBatchOnlineStatus = getBatchOnlineStatus;
exports.getChatHistory = getChatHistory;
exports.getConversationList = getConversationList;
exports.getMutualFollowList = getMutualFollowList;
exports.getOnlineStats = getOnlineStats;
exports.getOnlineStatus = getOnlineStatus;
exports.getUnreadCounts = getUnreadCounts;
exports.markMessageAsRead = markMessageAsRead;
exports.recallMessage = recallMessage;
exports.sendMessage = sendMessage;
exports.testChatApi = testChatApi;
//# sourceMappingURL=../../.sourcemap/mp-weixin/api/chat.js.map
