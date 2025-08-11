package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_chat_message_status
 */
@TableName(value ="tb_chat_message_status")
@Data
public class TbChatMessageStatus {
    /**
     * 状态ID
     */
    @TableId(type = IdType.AUTO)
    private Long statusId;

    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 读取时间
     */
    private Date readTime;

    /**
     * 是否送达：0-未送达，1-已送达
     */
    private Integer isDelivered;

    /**
     * 送达时间
     */
    private Date deliveredTime;

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
        TbChatMessageStatus other = (TbChatMessageStatus) that;
        return (this.getStatusId() == null ? other.getStatusId() == null : this.getStatusId().equals(other.getStatusId()))
            && (this.getMessageId() == null ? other.getMessageId() == null : this.getMessageId().equals(other.getMessageId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getIsRead() == null ? other.getIsRead() == null : this.getIsRead().equals(other.getIsRead()))
            && (this.getReadTime() == null ? other.getReadTime() == null : this.getReadTime().equals(other.getReadTime()))
            && (this.getIsDelivered() == null ? other.getIsDelivered() == null : this.getIsDelivered().equals(other.getIsDelivered()))
            && (this.getDeliveredTime() == null ? other.getDeliveredTime() == null : this.getDeliveredTime().equals(other.getDeliveredTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getStatusId() == null) ? 0 : getStatusId().hashCode());
        result = prime * result + ((getMessageId() == null) ? 0 : getMessageId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getIsRead() == null) ? 0 : getIsRead().hashCode());
        result = prime * result + ((getReadTime() == null) ? 0 : getReadTime().hashCode());
        result = prime * result + ((getIsDelivered() == null) ? 0 : getIsDelivered().hashCode());
        result = prime * result + ((getDeliveredTime() == null) ? 0 : getDeliveredTime().hashCode());
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
        sb.append(", statusId=").append(statusId);
        sb.append(", messageId=").append(messageId);
        sb.append(", userId=").append(userId);
        sb.append(", isRead=").append(isRead);
        sb.append(", readTime=").append(readTime);
        sb.append(", isDelivered=").append(isDelivered);
        sb.append(", deliveredTime=").append(deliveredTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}