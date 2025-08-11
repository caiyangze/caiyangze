package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_chat_message
 */
@TableName(value ="tb_chat_message")
@Data
public class TbChatMessage {
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
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

    /**
     * 是否撤回：0-否，1-是
     */
    private Integer isRecalled;

    /**
     * 撤回时间
     */
    private Date recallTime;

    /**
     * 是否删除：0-否，1-是
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TbChatMessage other = (TbChatMessage) that;
        return (this.getMessageId() == null ? other.getMessageId() == null : this.getMessageId().equals(other.getMessageId()))
            && (this.getConversationId() == null ? other.getConversationId() == null : this.getConversationId().equals(other.getConversationId()))
            && (this.getSenderId() == null ? other.getSenderId() == null : this.getSenderId().equals(other.getSenderId()))
            && (this.getReceiverId() == null ? other.getReceiverId() == null : this.getReceiverId().equals(other.getReceiverId()))
            && (this.getMessageType() == null ? other.getMessageType() == null : this.getMessageType().equals(other.getMessageType()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getMediaUrl() == null ? other.getMediaUrl() == null : this.getMediaUrl().equals(other.getMediaUrl()))
            && (this.getMediaSize() == null ? other.getMediaSize() == null : this.getMediaSize().equals(other.getMediaSize()))
            && (this.getMediaDuration() == null ? other.getMediaDuration() == null : this.getMediaDuration().equals(other.getMediaDuration()))
            && (this.getReplyToMessageId() == null ? other.getReplyToMessageId() == null : this.getReplyToMessageId().equals(other.getReplyToMessageId()))
            && (this.getIsRecalled() == null ? other.getIsRecalled() == null : this.getIsRecalled().equals(other.getIsRecalled()))
            && (this.getRecallTime() == null ? other.getRecallTime() == null : this.getRecallTime().equals(other.getRecallTime()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMessageId() == null) ? 0 : getMessageId().hashCode());
        result = prime * result + ((getConversationId() == null) ? 0 : getConversationId().hashCode());
        result = prime * result + ((getSenderId() == null) ? 0 : getSenderId().hashCode());
        result = prime * result + ((getReceiverId() == null) ? 0 : getReceiverId().hashCode());
        result = prime * result + ((getMessageType() == null) ? 0 : getMessageType().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getMediaUrl() == null) ? 0 : getMediaUrl().hashCode());
        result = prime * result + ((getMediaSize() == null) ? 0 : getMediaSize().hashCode());
        result = prime * result + ((getMediaDuration() == null) ? 0 : getMediaDuration().hashCode());
        result = prime * result + ((getReplyToMessageId() == null) ? 0 : getReplyToMessageId().hashCode());
        result = prime * result + ((getIsRecalled() == null) ? 0 : getIsRecalled().hashCode());
        result = prime * result + ((getRecallTime() == null) ? 0 : getRecallTime().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", messageId=").append(messageId);
        sb.append(", conversationId=").append(conversationId);
        sb.append(", senderId=").append(senderId);
        sb.append(", receiverId=").append(receiverId);
        sb.append(", messageType=").append(messageType);
        sb.append(", content=").append(content);
        sb.append(", mediaUrl=").append(mediaUrl);
        sb.append(", mediaSize=").append(mediaSize);
        sb.append(", mediaDuration=").append(mediaDuration);
        sb.append(", replyToMessageId=").append(replyToMessageId);
        sb.append(", isRecalled=").append(isRecalled);
        sb.append(", recallTime=").append(recallTime);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}