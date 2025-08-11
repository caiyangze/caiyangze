package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_feedback
 */
@TableName(value ="tb_feedback")
@Data
public class TbFeedback {
    /**
     * 反馈ID
     */
    @TableId(type = IdType.AUTO)
    private Long feedbackId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 反馈类型：1-功能建议，2-体验问题，3-技术故障，4-其他
     */
    private Integer feedbackType;

    /**
     * 反馈内容
     */
    private String feedbackContent;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 图片URL（JSON数组）
     */
    private String imageUrls;

    /**
     * 反馈状态：0-待处理，1-处理中，2-已解决，3-已关闭
     */
    private Integer feedbackStatus;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private Date handleTime;

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
        TbFeedback other = (TbFeedback) that;
        return (this.getFeedbackId() == null ? other.getFeedbackId() == null : this.getFeedbackId().equals(other.getFeedbackId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getFeedbackType() == null ? other.getFeedbackType() == null : this.getFeedbackType().equals(other.getFeedbackType()))
            && (this.getFeedbackContent() == null ? other.getFeedbackContent() == null : this.getFeedbackContent().equals(other.getFeedbackContent()))
            && (this.getContactInfo() == null ? other.getContactInfo() == null : this.getContactInfo().equals(other.getContactInfo()))
            && (this.getImageUrls() == null ? other.getImageUrls() == null : this.getImageUrls().equals(other.getImageUrls()))
            && (this.getFeedbackStatus() == null ? other.getFeedbackStatus() == null : this.getFeedbackStatus().equals(other.getFeedbackStatus()))
            && (this.getReplyContent() == null ? other.getReplyContent() == null : this.getReplyContent().equals(other.getReplyContent()))
            && (this.getHandlerId() == null ? other.getHandlerId() == null : this.getHandlerId().equals(other.getHandlerId()))
            && (this.getHandleTime() == null ? other.getHandleTime() == null : this.getHandleTime().equals(other.getHandleTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFeedbackId() == null) ? 0 : getFeedbackId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getFeedbackType() == null) ? 0 : getFeedbackType().hashCode());
        result = prime * result + ((getFeedbackContent() == null) ? 0 : getFeedbackContent().hashCode());
        result = prime * result + ((getContactInfo() == null) ? 0 : getContactInfo().hashCode());
        result = prime * result + ((getImageUrls() == null) ? 0 : getImageUrls().hashCode());
        result = prime * result + ((getFeedbackStatus() == null) ? 0 : getFeedbackStatus().hashCode());
        result = prime * result + ((getReplyContent() == null) ? 0 : getReplyContent().hashCode());
        result = prime * result + ((getHandlerId() == null) ? 0 : getHandlerId().hashCode());
        result = prime * result + ((getHandleTime() == null) ? 0 : getHandleTime().hashCode());
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
        sb.append(", feedbackId=").append(feedbackId);
        sb.append(", userId=").append(userId);
        sb.append(", feedbackType=").append(feedbackType);
        sb.append(", feedbackContent=").append(feedbackContent);
        sb.append(", contactInfo=").append(contactInfo);
        sb.append(", imageUrls=").append(imageUrls);
        sb.append(", feedbackStatus=").append(feedbackStatus);
        sb.append(", replyContent=").append(replyContent);
        sb.append(", handlerId=").append(handlerId);
        sb.append(", handleTime=").append(handleTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}