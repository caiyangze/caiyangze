package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_date_feedback
 */
@TableName(value ="tb_date_feedback")
@Data
public class TbDateFeedback {
    /**
     * 反馈ID
     */
    @TableId(type = IdType.AUTO)
    private Long feedbackId;

    /**
     * 约会安排ID
     */
    private Long arrangementId;

    /**
     * 反馈用户ID
     */
    private Long userId;

    /**
     * 满意度评分：1-5分
     */
    private Integer satisfactionScore;

    /**
     * 反馈内容
     */
    private String feedbackContent;

    /**
     * 红娘服务评分：1-5分
     */
    private Integer matchmakerScore;

    /**
     * 是否愿意再次约会：0-否，1-是
     */
    private Integer isWillingNext;

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
        TbDateFeedback other = (TbDateFeedback) that;
        return (this.getFeedbackId() == null ? other.getFeedbackId() == null : this.getFeedbackId().equals(other.getFeedbackId()))
            && (this.getArrangementId() == null ? other.getArrangementId() == null : this.getArrangementId().equals(other.getArrangementId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getSatisfactionScore() == null ? other.getSatisfactionScore() == null : this.getSatisfactionScore().equals(other.getSatisfactionScore()))
            && (this.getFeedbackContent() == null ? other.getFeedbackContent() == null : this.getFeedbackContent().equals(other.getFeedbackContent()))
            && (this.getMatchmakerScore() == null ? other.getMatchmakerScore() == null : this.getMatchmakerScore().equals(other.getMatchmakerScore()))
            && (this.getIsWillingNext() == null ? other.getIsWillingNext() == null : this.getIsWillingNext().equals(other.getIsWillingNext()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFeedbackId() == null) ? 0 : getFeedbackId().hashCode());
        result = prime * result + ((getArrangementId() == null) ? 0 : getArrangementId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getSatisfactionScore() == null) ? 0 : getSatisfactionScore().hashCode());
        result = prime * result + ((getFeedbackContent() == null) ? 0 : getFeedbackContent().hashCode());
        result = prime * result + ((getMatchmakerScore() == null) ? 0 : getMatchmakerScore().hashCode());
        result = prime * result + ((getIsWillingNext() == null) ? 0 : getIsWillingNext().hashCode());
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
        sb.append(", arrangementId=").append(arrangementId);
        sb.append(", userId=").append(userId);
        sb.append(", satisfactionScore=").append(satisfactionScore);
        sb.append(", feedbackContent=").append(feedbackContent);
        sb.append(", matchmakerScore=").append(matchmakerScore);
        sb.append(", isWillingNext=").append(isWillingNext);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}