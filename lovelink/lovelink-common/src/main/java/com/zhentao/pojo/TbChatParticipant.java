package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_chat_participant
 */
@TableName(value ="tb_chat_participant")
@Data
public class TbChatParticipant {
    /**
     * 参与者ID
     */
    @TableId(type = IdType.AUTO)
    private Long participantId;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色：1-普通成员，2-管理员，3-群主
     */
    private Integer role;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 最后读取的消息ID
     */
    private Long lastReadMessageId;

    /**
     * 是否禁言：0-否，1-是
     */
    private Integer isMuted;

    /**
     * 禁言到期时间
     */
    private Date muteUntil;

    /**
     * 是否活跃：0-已退出，1-活跃
     */
    private Integer isActive;

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
        TbChatParticipant other = (TbChatParticipant) that;
        return (this.getParticipantId() == null ? other.getParticipantId() == null : this.getParticipantId().equals(other.getParticipantId()))
            && (this.getConversationId() == null ? other.getConversationId() == null : this.getConversationId().equals(other.getConversationId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRole() == null ? other.getRole() == null : this.getRole().equals(other.getRole()))
            && (this.getJoinTime() == null ? other.getJoinTime() == null : this.getJoinTime().equals(other.getJoinTime()))
            && (this.getLastReadMessageId() == null ? other.getLastReadMessageId() == null : this.getLastReadMessageId().equals(other.getLastReadMessageId()))
            && (this.getIsMuted() == null ? other.getIsMuted() == null : this.getIsMuted().equals(other.getIsMuted()))
            && (this.getMuteUntil() == null ? other.getMuteUntil() == null : this.getMuteUntil().equals(other.getMuteUntil()))
            && (this.getIsActive() == null ? other.getIsActive() == null : this.getIsActive().equals(other.getIsActive()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getParticipantId() == null) ? 0 : getParticipantId().hashCode());
        result = prime * result + ((getConversationId() == null) ? 0 : getConversationId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRole() == null) ? 0 : getRole().hashCode());
        result = prime * result + ((getJoinTime() == null) ? 0 : getJoinTime().hashCode());
        result = prime * result + ((getLastReadMessageId() == null) ? 0 : getLastReadMessageId().hashCode());
        result = prime * result + ((getIsMuted() == null) ? 0 : getIsMuted().hashCode());
        result = prime * result + ((getMuteUntil() == null) ? 0 : getMuteUntil().hashCode());
        result = prime * result + ((getIsActive() == null) ? 0 : getIsActive().hashCode());
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
        sb.append(", participantId=").append(participantId);
        sb.append(", conversationId=").append(conversationId);
        sb.append(", userId=").append(userId);
        sb.append(", role=").append(role);
        sb.append(", joinTime=").append(joinTime);
        sb.append(", lastReadMessageId=").append(lastReadMessageId);
        sb.append(", isMuted=").append(isMuted);
        sb.append(", muteUntil=").append(muteUntil);
        sb.append(", isActive=").append(isActive);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}