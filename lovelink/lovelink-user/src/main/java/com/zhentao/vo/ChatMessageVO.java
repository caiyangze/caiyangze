package com.zhentao.vo;

import lombok.Data;

import java.util.Date;

/**
 * 聊天消息VO
 * 
 * @author zhentao
 */
@Data
public class ChatMessageVO {
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 会话ID
     */
    private Long conversationId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者昵称
     */
    private String senderNickname;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息类型：1-文本，2-图片，3-语音，4-视频，5-文件，6-系统消息
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 媒体文件URL
     */
    private String mediaUrl;
    
    /**
     * 媒体文件大小
     */
    private Long mediaSize;
    
    /**
     * 媒体时长
     */
    private Integer mediaDuration;
    
    /**
     * 回复的消息ID
     */
    private Long replyToMessageId;
    
    /**
     * 是否撤回
     */
    private Integer isRecalled;
    
    /**
     * 撤回时间
     */
    private Date recallTime;
    
    /**
     * 是否已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private Date createdAt;
}
