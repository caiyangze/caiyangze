package com.zhentao.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket会话管理器
 * 
 * @author zhentao
 */
@Component
@Slf4j
public class WebSocketSessionManager {
    
    /**
     * 用户ID -> WebSocket会话的映射
     */
    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    /**
     * WebSocket会话 -> 用户ID的映射
     */
    private final ConcurrentHashMap<String, Long> sessionUsers = new ConcurrentHashMap<>();
    
    /**
     * 添加用户会话
     * 
     * @param userId 用户ID
     * @param session WebSocket会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionUsers.put(session.getId(), userId);
        log.info("用户 {} 建立WebSocket连接，会话ID: {}", userId, session.getId());
    }
    
    /**
     * 移除用户会话
     * 
     * @param session WebSocket会话
     */
    public void removeSession(WebSocketSession session) {
        Long userId = sessionUsers.remove(session.getId());
        if (userId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("用户 {} 断开WebSocket连接，会话ID: {}", userId, session.getId());
        }
    }
    
    /**
     * 获取用户的所有会话
     * 
     * @param userId 用户ID
     * @return 用户的所有WebSocket会话
     */
    public CopyOnWriteArraySet<WebSocketSession> getUserSessions(Long userId) {
        return userSessions.get(userId);
    }
    
    /**
     * 根据会话ID获取用户ID
     * 
     * @param sessionId 会话ID
     * @return 用户ID
     */
    public Long getUserIdBySessionId(String sessionId) {
        return sessionUsers.get(sessionId);
    }
    
    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    /**
     * 获取在线用户数量
     * 
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }
    
    /**
     * 获取总连接数
     * 
     * @return 总连接数
     */
    public int getTotalConnectionCount() {
        return sessionUsers.size();
    }
}
