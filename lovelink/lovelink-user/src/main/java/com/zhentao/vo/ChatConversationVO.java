package com.zhentao.vo;

import lombok.Data;

import java.util.Date;

/**
 * 聊天会话VO
 * 
 * @author zhentao
 */
@Data
public class ChatConversationVO {
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 对方用户ID
     */
    private Long otherUserId;
    
    /**
     * 对方用户昵称
     */
    private String otherUserNickname;
    
    /**
     * 对方用户头像
     */
    private String otherUserAvatar;
    
    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;
    
    /**
     * 最后消息时间
     */
    private Date lastMessageTime;
    
    /**
     * 最后消息内容预览
     */
    private String lastMessageContent;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 会话状态：0-禁用，1-正常
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createdAt;
}
