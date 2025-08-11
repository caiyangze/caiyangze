package com.zhentao.dto;

import lombok.Data;

/**
 * WebSocket消息DTO
 * 
 * @author zhentao
 */
@Data
public class WebSocketMessageDTO {
    
    /**
     * 消息类型：
     * CHAT - 聊天消息
     * TYPING - 正在输入
     * READ - 已读回执
     * ONLINE - 上线通知
     * OFFLINE - 下线通知
     * HEARTBEAT - 心跳
     */
    private String type;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容（JSON格式）
     */
    private Object data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 消息ID（用于回执）
     */
    private Long messageId;
}
