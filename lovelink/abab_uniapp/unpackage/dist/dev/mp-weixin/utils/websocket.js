"use strict";
const common_vendor = require("../common/vendor.js");
const api_config = require("../api/config.js");
class WebSocketManager {
  constructor() {
    this.socket = null;
    this.isConnected = false;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.reconnectCount = 0;
    this.maxReconnectCount = 5;
    this.heartbeatInterval = 3e4;
    this.reconnectInterval = 5e3;
    this.messageHandlers = /* @__PURE__ */ new Map();
    this.connectionPromise = null;
  }
  /**
   * 连接WebSocket
   * @param {String} token JWT token
   */
  connect(token) {
    if (this.isConnected || this.connectionPromise) {
      return this.connectionPromise || Promise.resolve();
    }
    this.connectionPromise = new Promise((resolve, reject) => {
      try {
        const wsUrl = `${api_config.config.getWebSocketUrl()}/chat?token=${encodeURIComponent(token)}`;
        common_vendor.index.__f__("log", "at utils/websocket.js:33", "WebSocket连接地址:", wsUrl);
        this.socket = common_vendor.index.connectSocket({
          url: wsUrl,
          success: () => {
            common_vendor.index.__f__("log", "at utils/websocket.js:38", "WebSocket连接请求发送成功");
          },
          fail: (error) => {
            common_vendor.index.__f__("error", "at utils/websocket.js:41", "WebSocket连接失败:", error);
            this.connectionPromise = null;
            reject(error);
          }
        });
        this.socket.onOpen(() => {
          common_vendor.index.__f__("log", "at utils/websocket.js:49", "WebSocket连接已建立");
          this.isConnected = true;
          this.reconnectCount = 0;
          this.startHeartbeat();
          this.connectionPromise = null;
          resolve();
        });
        this.socket.onMessage((res) => {
          try {
            const message = JSON.parse(res.data);
            common_vendor.index.__f__("log", "at utils/websocket.js:61", "收到WebSocket消息:", message);
            this.handleMessage(message);
          } catch (error) {
            common_vendor.index.__f__("error", "at utils/websocket.js:64", "解析WebSocket消息失败:", error);
          }
        });
        this.socket.onClose((res) => {
          common_vendor.index.__f__("log", "at utils/websocket.js:70", "WebSocket连接已关闭:", res);
          this.isConnected = false;
          this.stopHeartbeat();
          this.connectionPromise = null;
          if (res.code !== 1e3 && this.reconnectCount < this.maxReconnectCount) {
            this.scheduleReconnect(token);
          }
        });
        this.socket.onError((error) => {
          common_vendor.index.__f__("error", "at utils/websocket.js:83", "WebSocket连接错误:", error);
          this.isConnected = false;
          this.connectionPromise = null;
          reject(error);
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at utils/websocket.js:90", "创建WebSocket连接失败:", error);
        this.connectionPromise = null;
        reject(error);
      }
    });
    return this.connectionPromise;
  }
  /**
   * 断开连接
   */
  disconnect() {
    if (this.socket) {
      this.socket.close({
        code: 1e3,
        reason: "主动断开连接"
      });
    }
    this.isConnected = false;
    this.stopHeartbeat();
    this.clearReconnectTimer();
    this.connectionPromise = null;
  }
  /**
   * 发送消息
   * @param {Object} message 消息对象
   */
  send(message) {
    if (!this.isConnected || !this.socket) {
      common_vendor.index.__f__("warn", "at utils/websocket.js:121", "WebSocket未连接，无法发送消息");
      return false;
    }
    try {
      const messageStr = JSON.stringify(message);
      this.socket.send({
        data: messageStr,
        success: () => {
          common_vendor.index.__f__("log", "at utils/websocket.js:130", "WebSocket消息发送成功:", message);
        },
        fail: (error) => {
          common_vendor.index.__f__("error", "at utils/websocket.js:133", "WebSocket消息发送失败:", error);
        }
      });
      return true;
    } catch (error) {
      common_vendor.index.__f__("error", "at utils/websocket.js:138", "发送WebSocket消息失败:", error);
      return false;
    }
  }
  /**
   * 发送聊天消息
   * @param {Number} receiverId 接收者ID
   * @param {String} content 消息内容
   * @param {Number} messageType 消息类型
   * @param {Number} tempId 临时消息ID
   */
  sendChatMessage(receiverId, content, messageType = 1, tempId = null) {
    const messageData = {
      type: "CHAT",
      timestamp: Date.now(),
      data: {
        receiverId,
        messageType,
        content
      }
    };
    if (tempId) {
      messageData.tempId = tempId;
    }
    common_vendor.index.__f__("log", "at utils/websocket.js:166", "发送WebSocket消息:", messageData);
    return this.send(messageData);
  }
  /**
   * 发送正在输入状态
   * @param {Number} receiverId 接收者ID
   */
  sendTyping(receiverId) {
    return this.send({
      type: "TYPING",
      receiverId,
      timestamp: Date.now(),
      data: true
    });
  }
  /**
   * 发送已读回执
   * @param {Number} messageId 消息ID
   * @param {Number} conversationId 会话ID
   */
  sendReadReceipt(messageId, conversationId) {
    return this.send({
      type: "READ",
      messageId,
      timestamp: Date.now(),
      data: conversationId
    });
  }
  /**
   * 注册消息处理器
   * @param {String} type 消息类型
   * @param {Function} handler 处理函数
   */
  onMessage(type, handler) {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, []);
    }
    this.messageHandlers.get(type).push(handler);
  }
  /**
   * 移除消息处理器
   * @param {String} type 消息类型
   * @param {Function} handler 处理函数
   */
  offMessage(type, handler) {
    if (this.messageHandlers.has(type)) {
      const handlers = this.messageHandlers.get(type);
      const index = handlers.indexOf(handler);
      if (index > -1) {
        handlers.splice(index, 1);
      }
    }
  }
  /**
   * 处理接收到的消息
   * @param {Object} message 消息对象
   */
  handleMessage(message) {
    const { type } = message;
    if (this.messageHandlers.has(type)) {
      const handlers = this.messageHandlers.get(type);
      handlers.forEach((handler) => {
        try {
          handler(message);
        } catch (error) {
          common_vendor.index.__f__("error", "at utils/websocket.js:236", "消息处理器执行失败:", error);
        }
      });
    }
  }
  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      this.send({
        type: "HEARTBEAT",
        timestamp: Date.now(),
        data: "ping"
      });
    }, this.heartbeatInterval);
  }
  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }
  /**
   * 安排重连
   * @param {String} token JWT token
   */
  scheduleReconnect(token) {
    this.clearReconnectTimer();
    this.reconnectCount++;
    common_vendor.index.__f__("log", "at utils/websocket.js:274", `准备第${this.reconnectCount}次重连...`);
    this.reconnectTimer = setTimeout(() => {
      this.connect(token).catch((error) => {
        common_vendor.index.__f__("error", "at utils/websocket.js:278", "重连失败:", error);
      });
    }, this.reconnectInterval * this.reconnectCount);
  }
  /**
   * 清除重连定时器
   */
  clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }
  /**
   * 获取连接状态
   */
  getConnectionStatus() {
    return {
      isConnected: this.isConnected,
      reconnectCount: this.reconnectCount
    };
  }
}
const wsManager = new WebSocketManager();
exports.wsManager = wsManager;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/websocket.js.map
