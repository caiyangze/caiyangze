/**
 * WebSocket管理器
 */

import config from '@/api/config';

class WebSocketManager {
  constructor() {
    this.socket = null;
    this.isConnected = false;
    this.reconnectTimer = null;
    this.heartbeatTimer = null;
    this.reconnectCount = 0;
    this.maxReconnectCount = 5;
    this.heartbeatInterval = 30000; // 30秒心跳
    this.reconnectInterval = 5000; // 5秒重连间隔
    this.messageHandlers = new Map();
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
        const wsUrl = `${config.getWebSocketUrl()}/ws/chat?token=${encodeURIComponent(token)}`;
        console.log('WebSocket连接地址:', wsUrl);

        this.socket = uni.connectSocket({
          url: wsUrl,
          success: () => {
            console.log('WebSocket连接请求发送成功');
          },
          fail: (error) => {
            console.error('WebSocket连接失败:', error);
            this.connectionPromise = null;
            reject(error);
          }
        });

        // 连接打开
        this.socket.onOpen(() => {
          console.log('WebSocket连接已建立');
          this.isConnected = true;
          this.reconnectCount = 0;
          this.startHeartbeat();
          this.connectionPromise = null;
          resolve();
        });

        // 接收消息
        this.socket.onMessage((res) => {
          try {
            const message = JSON.parse(res.data);
            console.log('收到WebSocket消息:', message);
            this.handleMessage(message);
          } catch (error) {
            console.error('解析WebSocket消息失败:', error);
          }
        });

        // 连接关闭
        this.socket.onClose((res) => {
          console.log('WebSocket连接已关闭:', res);
          this.isConnected = false;
          this.stopHeartbeat();
          this.connectionPromise = null;
          
          // 如果不是主动关闭，尝试重连
          if (res.code !== 1000 && this.reconnectCount < this.maxReconnectCount) {
            this.scheduleReconnect(token);
          }
        });

        // 连接错误
        this.socket.onError((error) => {
          console.error('WebSocket连接错误:', error);
          this.isConnected = false;
          this.connectionPromise = null;
          reject(error);
        });

      } catch (error) {
        console.error('创建WebSocket连接失败:', error);
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
        code: 1000,
        reason: '主动断开连接'
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
      console.warn('WebSocket未连接，无法发送消息');
      return false;
    }

    try {
      const messageStr = JSON.stringify(message);
      this.socket.send({
        data: messageStr,
        success: () => {
          console.log('WebSocket消息发送成功:', message);
        },
        fail: (error) => {
          console.error('WebSocket消息发送失败:', error);
        }
      });
      return true;
    } catch (error) {
      console.error('发送WebSocket消息失败:', error);
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
      type: 'CHAT',
      timestamp: Date.now(),
      data: {
        receiverId,
        messageType,
        content
      }
    };

    // 如果有临时ID，添加到消息中
    if (tempId) {
      messageData.tempId = tempId;
    }

    console.log('发送WebSocket消息:', messageData);
    return this.send(messageData);
  }

  /**
   * 发送正在输入状态
   * @param {Number} receiverId 接收者ID
   */
  sendTyping(receiverId) {
    return this.send({
      type: 'TYPING',
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
      type: 'READ',
      messageId,
      timestamp: Date.now(),
      data: conversationId
    });
  }

  /**
   * 发送约会确认消息
   * @param {Number} arrangementId 约会安排ID
   * @param {Number} confirm 确认状态：1-确认，2-拒绝
   */
  sendDateConfirm(arrangementId, confirm) {
    return this.send({
      type: 'DATE_CONFIRM',
      timestamp: Date.now(),
      data: {
        arrangementId,
        confirm
      }
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
      handlers.forEach(handler => {
        try {
          handler(message);
        } catch (error) {
          console.error('消息处理器执行失败:', error);
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
        type: 'HEARTBEAT',
        timestamp: Date.now(),
        data: 'ping'
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
    
    console.log(`准备第${this.reconnectCount}次重连...`);
    
    this.reconnectTimer = setTimeout(() => {
      this.connect(token).catch(error => {
        console.error('重连失败:', error);
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

// 创建全局WebSocket管理器实例
const wsManager = new WebSocketManager();

export default wsManager;
