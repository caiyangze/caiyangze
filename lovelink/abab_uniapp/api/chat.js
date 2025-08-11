/**
 * 聊天相关API
 */

import http from './http';

/**
 * 检查是否可以与指定用户聊天
 * @param {Number} targetUserId 目标用户ID
 */
export const canChat = (targetUserId) => {
  return http.get(`/chat/canChat/${targetUserId}`);
};

/**
 * 获取会话列表
 */
export const getConversationList = () => {
  return http.get('/chat/conversations');
};

/**
 * 获取聊天记录
 * @param {Number} conversationId 会话ID
 * @param {Number} page 页码
 * @param {Number} size 每页大小
 */
export const getChatHistory = (conversationId, page = 1, size = 20) => {
  return http.get(`/chat/history/${conversationId}`, {
    page,
    size
  });
};

/**
 * 发送消息（HTTP接口，用于文件上传等场景）
 * @param {Object} messageData 消息数据
 */
export const sendMessage = (messageData) => {
  return http.post('/chat/send', messageData);
};

/**
 * 标记消息为已读
 * @param {Number} conversationId 会话ID
 * @param {Number} messageId 消息ID
 */
export const markMessageAsRead = (conversationId, messageId) => {
  return http.post('/chat/read', {
    conversationId,
    messageId
  });
};

/**
 * 撤回消息
 * @param {Number} messageId 消息ID
 */
export const recallMessage = (messageId) => {
  return http.post(`/chat/recall/${messageId}`);
};

/**
 * 删除会话
 * @param {Number} conversationId 会话ID
 */
export const deleteConversation = (conversationId) => {
  return http.del(`/chat/conversation/${conversationId}`);
};

/**
 * 获取用户在线状态
 * @param {Number} userId 用户ID
 */
export const getOnlineStatus = (userId) => {
  return http.get(`/chat/online/${userId}`);
};

/**
 * 获取在线用户统计
 */
export const getOnlineStats = () => {
  return http.get('/chat/stats');
};

/**
 * 获取互相关注的好友列表
 */
export const getMutualFollowList = () => {
  return http.get('/follow/mutual');
};

/**
 * 批量获取用户在线状态
 * @param {Array} userIds 用户ID数组
 */
export const getBatchOnlineStatus = (userIds) => {
  return http.post('/chat/batchOnlineStatus', { userIds });
};

/**
 * 获取未读消息数量
 * @param {Array} userIds 用户ID数组
 */
export const getUnreadCounts = (userIds) => {
  return http.post('/chat/unreadCounts', { userIds });
};

/**
 * 测试接口
 */
export const testChatApi = (data) => {
  return http.post('/chat/test', data);
};
