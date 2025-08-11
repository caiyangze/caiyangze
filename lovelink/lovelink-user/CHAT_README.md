# WebSocket聊天功能使用说明

## 功能概述

本模块实现了基于WebSocket的实时聊天功能，支持用户间的实时消息传递。只有互相关注的用户才能进行聊天，确保了用户的隐私和安全。

## 主要特性

- ✅ 基于关注关系的聊天权限控制
- ✅ WebSocket实时消息传递
- ✅ 支持多种消息类型（文本、图片、语音、视频等）
- ✅ 消息撤回功能（2分钟内）
- ✅ 消息已读状态
- ✅ 在线状态管理
- ✅ 会话管理
- ✅ 聊天记录持久化

## API接口

### REST API

| 接口 | 方法 | 描述 | 需要登录 |
|------|------|------|----------|
| `/chat/canChat/{targetUserId}` | GET | 检查是否可以聊天 | ✅ |
| `/chat/conversations` | GET | 获取会话列表 | ✅ |
| `/chat/history/{conversationId}` | GET | 获取聊天记录 | ✅ |
| `/chat/send` | POST | 发送消息（HTTP） | ✅ |
| `/chat/read` | POST | 标记消息已读 | ✅ |
| `/chat/recall/{messageId}` | POST | 撤回消息 | ✅ |
| `/chat/conversation/{conversationId}` | DELETE | 删除会话 | ✅ |
| `/chat/online/{userId}` | GET | 获取在线状态 | ❌ |
| `/chat/stats` | GET | 获取在线统计 | ❌ |

### WebSocket连接

**连接地址：** `ws://localhost:9001/chat?token=YOUR_JWT_TOKEN`

**消息格式：**
```json
{
  "type": "CHAT|TYPING|READ|HEARTBEAT",
  "senderId": 123,
  "receiverId": 456,
  "messageId": 789,
  "timestamp": 1640995200000,
  "data": {}
}
```

## 使用示例

### 1. 检查是否可以聊天
```bash
curl -H "token: YOUR_JWT_TOKEN" \
     http://localhost:9001/chat/canChat/123
```

### 2. 建立WebSocket连接
```javascript
const ws = new WebSocket('ws://localhost:9001/chat?token=YOUR_JWT_TOKEN');

ws.onopen = function(event) {
    console.log('连接成功');
};

ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('收到消息:', message);
};
```

### 3. 发送聊天消息
```javascript
const message = {
    type: 'CHAT',
    timestamp: Date.now(),
    data: {
        receiverId: 123,
        messageType: 1,
        content: '你好！'
    }
};

ws.send(JSON.stringify(message));
```

### 4. 发送心跳
```javascript
const heartbeat = {
    type: 'HEARTBEAT',
    timestamp: Date.now(),
    data: 'ping'
};

ws.send(JSON.stringify(heartbeat));
```

## 测试

访问测试页面：`http://localhost:9001/chat-test.html`

1. 输入有效的JWT Token
2. 点击连接按钮
3. 输入接收者ID和消息内容
4. 点击发送消息进行测试

## 注意事项

1. **权限控制**：只有互相关注的用户才能聊天
2. **Token验证**：所有接口都需要有效的JWT Token
3. **消息撤回**：只能撤回2分钟内发送的消息
4. **连接管理**：WebSocket连接会自动管理用户在线状态
5. **错误处理**：所有异常都会返回相应的错误信息

## 数据库表

聊天功能使用以下数据库表：

- `tb_chat_conversation` - 聊天会话表
- `tb_chat_message` - 聊天消息表
- `tb_chat_message_status` - 消息状态表
- `tb_chat_participant` - 会话参与者表（群聊预留）
- `tb_chat_setting` - 聊天设置表
- `tb_follow` - 用户关注表（权限验证）

## 故障排除

1. **连接失败**：检查JWT Token是否有效
2. **无法发送消息**：确认两个用户是否互相关注
3. **消息未收到**：检查接收者是否在线
4. **撤回失败**：确认是否在2分钟时间限制内
