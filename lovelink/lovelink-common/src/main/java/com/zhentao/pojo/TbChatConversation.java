package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_chat_conversation
 */
@TableName(value ="tb_chat_conversation")
@Data
public class TbChatConversation {
    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long conversationId;

    /**
     * 用户1ID（较小的用户ID）
     */
    private Long user1Id;

    /**
     * 用户2ID（较大的用户ID）
     */
    private Long user2Id;

    /**
     * 会话类型：1-私聊，2-群聊
     */
    private Integer conversationType;

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
     * 用户1未读消息数
     */
    private Integer user1UnreadCount;

    /**
     * 用户2未读消息数
     */
    private Integer user2UnreadCount;

    /**
     * 用户1是否删除会话：0-否，1-是
     */
    private Integer user1Deleted;

    /**
     * 用户2是否删除会话：0-否，1-是
     */
    private Integer user2Deleted;

    /**
     * 会话状态：0-禁用，1-正常
     */
    private Integer status;

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
        TbChatConversation other = (TbChatConversation) that;
        return (this.getConversationId() == null ? other.getConversationId() == null : this.getConversationId().equals(other.getConversationId()))
            && (this.getUser1Id() == null ? other.getUser1Id() == null : this.getUser1Id().equals(other.getUser1Id()))
            && (this.getUser2Id() == null ? other.getUser2Id() == null : this.getUser2Id().equals(other.getUser2Id()))
            && (this.getConversationType() == null ? other.getConversationType() == null : this.getConversationType().equals(other.getConversationType()))
            && (this.getLastMessageId() == null ? other.getLastMessageId() == null : this.getLastMessageId().equals(other.getLastMessageId()))
            && (this.getLastMessageTime() == null ? other.getLastMessageTime() == null : this.getLastMessageTime().equals(other.getLastMessageTime()))
            && (this.getLastMessageContent() == null ? other.getLastMessageContent() == null : this.getLastMessageContent().equals(other.getLastMessageContent()))
            && (this.getUser1UnreadCount() == null ? other.getUser1UnreadCount() == null : this.getUser1UnreadCount().equals(other.getUser1UnreadCount()))
            && (this.getUser2UnreadCount() == null ? other.getUser2UnreadCount() == null : this.getUser2UnreadCount().equals(other.getUser2UnreadCount()))
            && (this.getUser1Deleted() == null ? other.getUser1Deleted() == null : this.getUser1Deleted().equals(other.getUser1Deleted()))
            && (this.getUser2Deleted() == null ? other.getUser2Deleted() == null : this.getUser2Deleted().equals(other.getUser2Deleted()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getConversationId() == null) ? 0 : getConversationId().hashCode());
        result = prime * result + ((getUser1Id() == null) ? 0 : getUser1Id().hashCode());
        result = prime * result + ((getUser2Id() == null) ? 0 : getUser2Id().hashCode());
        result = prime * result + ((getConversationType() == null) ? 0 : getConversationType().hashCode());
        result = prime * result + ((getLastMessageId() == null) ? 0 : getLastMessageId().hashCode());
        result = prime * result + ((getLastMessageTime() == null) ? 0 : getLastMessageTime().hashCode());
        result = prime * result + ((getLastMessageContent() == null) ? 0 : getLastMessageContent().hashCode());
        result = prime * result + ((getUser1UnreadCount() == null) ? 0 : getUser1UnreadCount().hashCode());
        result = prime * result + ((getUser2UnreadCount() == null) ? 0 : getUser2UnreadCount().hashCode());
        result = prime * result + ((getUser1Deleted() == null) ? 0 : getUser1Deleted().hashCode());
        result = prime * result + ((getUser2Deleted() == null) ? 0 : getUser2Deleted().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
        sb.append(", conversationId=").append(conversationId);
        sb.append(", user1Id=").append(user1Id);
        sb.append(", user2Id=").append(user2Id);
        sb.append(", conversationType=").append(conversationType);
        sb.append(", lastMessageId=").append(lastMessageId);
        sb.append(", lastMessageTime=").append(lastMessageTime);
        sb.append(", lastMessageContent=").append(lastMessageContent);
        sb.append(", user1UnreadCount=").append(user1UnreadCount);
        sb.append(", user2UnreadCount=").append(user2UnreadCount);
        sb.append(", user1Deleted=").append(user1Deleted);
        sb.append(", user2Deleted=").append(user2Deleted);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}