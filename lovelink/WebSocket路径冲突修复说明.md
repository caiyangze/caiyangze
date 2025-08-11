# WebSocket路径冲突修复说明

## 🚨 问题根本原因

WebSocket连接405错误的根本原因是**路径冲突**：

1. **WebSocket配置**：`/chat` 路径用于WebSocket连接
2. **HTTP控制器**：`ChatController` 使用 `@RequestMapping("/chat")`

当前端尝试建立WebSocket连接到 `/chat` 时，Spring Boot将其路由到HTTP控制器而不是WebSocket处理器，导致405错误。

## 🔧 修复方案

### 1. 修改WebSocket路径

**文件**: `lovelink-user/src/main/java/com/zhentao/config/WebSocketConfig.java`

```java
@Override
public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    // 原生WebSocket - 新路径
    registry.addHandler(chatWebSocketHandler, "/ws/chat")
            .setAllowedOrigins("*");
    
    // SockJS降级支持 - 新路径
    registry.addHandler(chatWebSocketHandler, "/ws/chat-sockjs")
            .setAllowedOrigins("*")
            .withSockJS();
}
```

### 2. 更新前端连接地址

**文件**: `abab_uniapp/utils/websocket.js`

```javascript
// 修改前
const wsUrl = `${config.getWebSocketUrl()}/chat?token=${encodeURIComponent(token)}`;

// 修改后
const wsUrl = `${config.getWebSocketUrl()}/ws/chat?token=${encodeURIComponent(token)}`;
```

### 3. 路径规划

现在的路径分配：
- **HTTP API**: `/chat/*` - 用于REST API调用
- **WebSocket**: `/ws/chat` - 用于WebSocket连接
- **SockJS**: `/ws/chat-sockjs` - 用于SockJS降级连接

## 📋 修改文件清单

1. `lovelink-user/src/main/java/com/zhentao/config/WebSocketConfig.java`
   - 修改WebSocket路径为 `/ws/chat`
   - 添加SockJS支持路径 `/ws/chat-sockjs`
   - 添加详细日志

2. `abab_uniapp/utils/websocket.js`
   - 更新WebSocket连接地址

3. `lovelink-user/src/main/java/com/zhentao/controller/MatchmakingConfirmController.java`
   - 添加WebSocket测试端点

## 🧪 测试步骤

### 1. 重启后端服务

```bash
cd lovelink-user
mvn spring-boot:run
```

观察启动日志，应该看到：
```
正在注册WebSocket处理器...
已注册WebSocket处理器: /ws/chat
已注册SockJS WebSocket处理器: /ws/chat-sockjs
```

### 2. 测试WebSocket端点

访问测试接口：
```
GET http://localhost:9001/user/matchmaking/confirm/test/websocket
```

应该返回：
```json
{
  "code": 200,
  "data": {
    "message": "WebSocket测试端点",
    "wsUrl": "ws://localhost:9001/ws/chat",
    "sockjsUrl": "ws://localhost:9001/ws/chat-sockjs",
    "timestamp": 1691234567890
  }
}
```

### 3. 测试WebSocket连接

在浏览器控制台中测试：
```javascript
const token = uni.getStorageSync('token');
const ws = new WebSocket(`ws://localhost:9001/ws/chat?token=${encodeURIComponent(token)}`);
ws.onopen = () => console.log('WebSocket连接成功');
ws.onerror = (error) => console.error('WebSocket连接失败:', error);
```

## 🎯 预期结果

修复后，WebSocket连接应该：
1. ✅ 成功建立连接（状态码101 Switching Protocols）
2. ✅ 正确解析token参数
3. ✅ 验证用户身份
4. ✅ 发送连接成功消息
5. ✅ 支持实时聊天功能

## 📝 注意事项

1. **路径一致性**：确保前后端使用相同的WebSocket路径
2. **CORS配置**：生产环境建议配置具体的允许域名
3. **Token格式**：确保token正确URL编码
4. **降级支持**：SockJS提供WebSocket的降级方案

## 🔄 后续优化

1. **环境配置**：将WebSocket路径配置化，支持不同环境
2. **监控告警**：添加WebSocket连接状态监控
3. **性能优化**：考虑WebSocket连接池和负载均衡
4. **安全加固**：添加更严格的token验证和CORS配置

## 🐛 如果问题仍然存在

1. 检查是否有其他服务占用9001端口
2. 确认防火墙设置允许WebSocket连接
3. 查看浏览器开发者工具的Network标签页
4. 检查后端日志中的详细错误信息
