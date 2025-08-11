package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 聊天消息DTO
 * 
 * @author zhentao
 */
@Data
public class ChatMessageDTO {
    
    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    /**
     * 消息类型：1-文本，2-图片，3-语音，4-视频，5-文件
     */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 媒体文件URL（图片、语音、视频等）
     */
    private String mediaUrl;
    
    /**
     * 媒体文件大小（字节）
     */
    private Long mediaSize;
    
    /**
     * 媒体时长（秒，用于语音、视频）
     */
    private Integer mediaDuration;
    
    /**
     * 回复的消息ID
     */
    private Long replyToMessageId;
}
