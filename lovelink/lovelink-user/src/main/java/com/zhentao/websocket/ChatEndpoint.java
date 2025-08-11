package com.zhentao.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天端点
 * 使用JSR-356标准WebSocket API
 */
@ServerEndpoint(value = "/ws/chat")
@Component
@Slf4j
public class ChatEndpoint {
    
    // 存储所有连接的会话
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    // JSON处理器
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @OnOpen
    public void onOpen(Session session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        log.error("=== WebSocket连接建立 ===");
        log.error("会话ID: {}", sessionId);
        log.error("当前连接数: {}", sessions.size());
        
        try {
            // 发送欢迎消息
            Map<String, Object> welcomeData = new HashMap<>();
            welcomeData.put("type", "CONNECTED");
            welcomeData.put("message", "连接成功");
            welcomeData.put("timestamp", System.currentTimeMillis());

            String welcomeMessage = objectMapper.writeValueAsString(welcomeData);
            session.getBasicRemote().sendText(welcomeMessage);
            log.error("已发送欢迎消息: {}", welcomeMessage);
        } catch (Exception e) {
            log.error("发送欢迎消息失败", e);
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        log.error("收到消息: {}", message);
        
        try {
            // 回显消息
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", "ECHO");
            responseData.put("message", "收到: " + message);
            responseData.put("timestamp", System.currentTimeMillis());

            String response = objectMapper.writeValueAsString(responseData);
            session.getBasicRemote().sendText(response);
            log.error("已回复消息: {}", response);
        } catch (Exception e) {
            log.error("回复消息失败", e);
        }
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        log.error("WebSocket连接关闭");
        log.error("会话ID: {}", sessionId);
        log.error("关闭原因: {}", closeReason);
        log.error("剩余连接数: {}", sessions.size());
    }
    
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，会话ID: {}", session.getId(), error);
    }
    
    /**
     * 广播消息给所有连接
     */
    public static void broadcast(String message) {
        log.error("广播消息给 {} 个连接: {}", sessions.size(), message);
        
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                log.error("广播消息失败", e);
            }
        });
    }
}
