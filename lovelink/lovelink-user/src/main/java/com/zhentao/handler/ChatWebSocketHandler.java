package com.zhentao.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhentao.dto.ChatMessageDTO;
import com.zhentao.dto.WebSocketMessageDTO;
import com.zhentao.pojo.TbChatMessage;
import com.zhentao.service.ChatService;
import com.zhentao.service.WebSocketSessionManager;
import com.zhentao.utils.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天WebSocket处理器
 * 
 * @author zhentao
 */
@Component
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    @Autowired
    private ChatService chatService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数中获取token
        String token = getTokenFromSession(session);
        if (token == null) {
            log.warn("WebSocket连接缺少token参数");
            session.close(CloseStatus.BAD_DATA.withReason("Missing token"));
            return;
        }
        
        try {
            // 验证token并获取用户ID
            Long userId = JwtService.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("无效的token: {}", token);
                session.close(CloseStatus.BAD_DATA.withReason("Invalid token"));
                return;
            }
            
            // 添加会话到管理器
            sessionManager.addSession(userId, session);
            
            // 发送连接成功消息
            WebSocketMessageDTO welcomeMessage = new WebSocketMessageDTO();
            welcomeMessage.setType("CONNECTED");
            welcomeMessage.setTimestamp(System.currentTimeMillis());
            welcomeMessage.setData("连接成功");
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
            
        } catch (Exception e) {
            log.error("WebSocket连接建立失败", e);
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection failed"));
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (!(message instanceof TextMessage)) {
            return;
        }
        
        String payload = ((TextMessage) message).getPayload();
        log.info("收到WebSocket消息: {}", payload);
        
        try {
            WebSocketMessageDTO wsMessage = objectMapper.readValue(payload, WebSocketMessageDTO.class);
            Long senderId = sessionManager.getUserIdBySessionId(session.getId());
            
            if (senderId == null) {
                log.warn("未找到会话对应的用户ID: {}", session.getId());
                return;
            }
            
            switch (wsMessage.getType()) {
                case "CHAT":
                    handleChatMessage(senderId, wsMessage);
                    break;
                case "TYPING":
                    handleTypingMessage(senderId, wsMessage);
                    break;
                case "READ":
                    handleReadMessage(senderId, wsMessage);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(session);
                    break;
                case "DATE_CONFIRM":
                    handleDateConfirmMessage(senderId, wsMessage);
                    break;
                default:
                    log.warn("未知的消息类型: {}", wsMessage.getType());
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误, 会话ID: {}", session.getId(), exception);
        sessionManager.removeSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket连接关闭, 会话ID: {}, 状态: {}", session.getId(), closeStatus);
        sessionManager.removeSession(session);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(Long senderId, WebSocketMessageDTO wsMessage) {
        try {
            ChatMessageDTO messageDTO = objectMapper.convertValue(wsMessage.getData(), ChatMessageDTO.class);

            // 发送消息
            TbChatMessage chatMessage = chatService.sendMessage(senderId, messageDTO);

            // 构建响应消息
            WebSocketMessageDTO responseMessage = new WebSocketMessageDTO();
            responseMessage.setType("CHAT");
            responseMessage.setSenderId(senderId);
            responseMessage.setReceiverId(messageDTO.getReceiverId());
            responseMessage.setMessageId(chatMessage.getMessageId());
            responseMessage.setTimestamp(System.currentTimeMillis());
            responseMessage.setData(chatMessage);

            // 发送给接收者
            sendMessageToUser(messageDTO.getReceiverId(), responseMessage);

            // 发送确认给发送者
            WebSocketMessageDTO confirmMessage = new WebSocketMessageDTO();
            confirmMessage.setType("SENT");
            confirmMessage.setMessageId(chatMessage.getMessageId());
            confirmMessage.setTimestamp(System.currentTimeMillis());
            confirmMessage.setData("消息发送成功");

            sendMessageToUser(senderId, confirmMessage);

        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
            // 发送错误消息给发送者
            WebSocketMessageDTO errorMessage = new WebSocketMessageDTO();
            errorMessage.setType("ERROR");
            errorMessage.setTimestamp(System.currentTimeMillis());
            errorMessage.setData(e.getMessage());

            sendMessageToUser(senderId, errorMessage);
        }
    }

    /**
     * 处理正在输入消息
     */
    private void handleTypingMessage(Long senderId, WebSocketMessageDTO wsMessage) {
        if (wsMessage.getReceiverId() != null) {
            WebSocketMessageDTO typingMessage = new WebSocketMessageDTO();
            typingMessage.setType("TYPING");
            typingMessage.setSenderId(senderId);
            typingMessage.setTimestamp(System.currentTimeMillis());
            typingMessage.setData(wsMessage.getData());

            sendMessageToUser(wsMessage.getReceiverId(), typingMessage);
        }
    }

    /**
     * 处理已读消息
     */
    private void handleReadMessage(Long userId, WebSocketMessageDTO wsMessage) {
        try {
            Long messageId = wsMessage.getMessageId();
            Long conversationId = (Long) wsMessage.getData();

            if (messageId != null && conversationId != null) {
                chatService.markMessageAsRead(conversationId, userId, messageId);

                // 通知发送者消息已读
                WebSocketMessageDTO readMessage = new WebSocketMessageDTO();
                readMessage.setType("READ");
                readMessage.setMessageId(messageId);
                readMessage.setTimestamp(System.currentTimeMillis());
                readMessage.setData("已读");

                // 这里需要根据消息ID找到发送者，然后发送已读回执
                // 为简化，暂时省略此逻辑
            }
        } catch (Exception e) {
            log.error("处理已读消息失败", e);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(WebSocketSession session) {
        try {
            WebSocketMessageDTO heartbeatResponse = new WebSocketMessageDTO();
            heartbeatResponse.setType("HEARTBEAT");
            heartbeatResponse.setTimestamp(System.currentTimeMillis());
            heartbeatResponse.setData("pong");

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(heartbeatResponse)));
        } catch (Exception e) {
            log.error("发送心跳响应失败", e);
        }
    }

    /**
     * 处理约会确认消息
     */
    private void handleDateConfirmMessage(Long userId, WebSocketMessageDTO wsMessage) {
        try {
            log.info("用户 {} 处理约会确认消息: {}", userId, wsMessage.getData());

            // 这里可以调用约会服务来处理确认逻辑
            // 暂时只是转发消息给相关用户

            // 发送确认响应给发送者
            WebSocketMessageDTO confirmResponse = new WebSocketMessageDTO();
            confirmResponse.setType("DATE_CONFIRM_RESPONSE");
            confirmResponse.setTimestamp(System.currentTimeMillis());
            confirmResponse.setData("约会确认处理成功");

            sendMessageToUser(userId, confirmResponse);

        } catch (Exception e) {
            log.error("处理约会确认消息失败", e);
        }
    }

    /**
     * 发送消息给指定用户
     */
    private void sendMessageToUser(Long userId, WebSocketMessageDTO message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = sessionManager.getUserSessions(userId);
        if (sessions != null && !sessions.isEmpty()) {
            String messageJson;
            try {
                messageJson = objectMapper.writeValueAsString(message);
            } catch (Exception e) {
                log.error("序列化消息失败", e);
                return;
            }

            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageJson));
                    }
                } catch (Exception e) {
                    log.error("发送WebSocket消息失败", e);
                    sessionManager.removeSession(session);
                }
            }
        }
    }

    /**
     * 发送约会通知给指定用户（公共方法，供其他服务调用）
     */
    public void sendDateNotification(Long userId, String notificationType, Object data) {
        WebSocketMessageDTO notification = new WebSocketMessageDTO();
        notification.setType(notificationType);
        notification.setTimestamp(System.currentTimeMillis());
        notification.setData(data);

        sendMessageToUser(userId, notification);
        log.info("发送约会通知给用户 {}: {}", userId, notificationType);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return sessionManager.isUserOnline(userId);
    }

    /**
     * 从WebSocket会话中获取token
     */
    private String getTokenFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri != null) {
            String query = uri.getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                        return keyValue[1];
                    }
                }
            }
        }
        return null;
    }
}
