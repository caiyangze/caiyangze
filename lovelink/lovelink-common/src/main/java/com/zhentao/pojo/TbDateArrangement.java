package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_date_arrangement
 */
@TableName(value ="tb_date_arrangement")
@Data
public class TbDateArrangement {
    /**
     * 安排ID
     */
    @TableId(type = IdType.AUTO)
    private Long arrangementId;

    /**
     * 牵线申请ID
     */
    private Long requestId;

    /**
     * 红娘ID
     */
    private Long matchmakerId;

    /**
     * 用户A的ID
     */
    private Long userId;

    /**
     * 用户B的ID
     */
    private Long targetUserId;

    /**
     * 约会时间
     */
    private Date dateTime;

    /**
     * 约会地点
     */
    private String dateLocation;

    /**
     * 约会类型：1-咖啡厅，2-餐厅，3-电影院，4-其他
     */
    private Integer dateType;

    /**
     * 约会计划详情
     */
    private String datePlan;

    /**
     * 用户A确认状态：0-待确认，1-已确认，2-已拒绝
     */
    private Integer userAConfirm;

    /**
     * 用户B确认状态：0-待确认，1-已确认，2-已拒绝
     */
    private Integer userBConfirm;

    /**
     * 安排状态：0-待确认，1-已确认，2-已取消，3-已完成
     */
    private Integer arrangementStatus;

    /**
     * 取消原因
     */
    private String cancelReason;

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
        TbDateArrangement other = (TbDateArrangement) that;
        return (this.getArrangementId() == null ? other.getArrangementId() == null : this.getArrangementId().equals(other.getArrangementId()))
            && (this.getRequestId() == null ? other.getRequestId() == null : this.getRequestId().equals(other.getRequestId()))
            && (this.getMatchmakerId() == null ? other.getMatchmakerId() == null : this.getMatchmakerId().equals(other.getMatchmakerId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTargetUserId() == null ? other.getTargetUserId() == null : this.getTargetUserId().equals(other.getTargetUserId()))
            && (this.getDateTime() == null ? other.getDateTime() == null : this.getDateTime().equals(other.getDateTime()))
            && (this.getDateLocation() == null ? other.getDateLocation() == null : this.getDateLocation().equals(other.getDateLocation()))
            && (this.getDateType() == null ? other.getDateType() == null : this.getDateType().equals(other.getDateType()))
            && (this.getDatePlan() == null ? other.getDatePlan() == null : this.getDatePlan().equals(other.getDatePlan()))
            && (this.getUserAConfirm() == null ? other.getUserAConfirm() == null : this.getUserAConfirm().equals(other.getUserAConfirm()))
            && (this.getUserBConfirm() == null ? other.getUserBConfirm() == null : this.getUserBConfirm().equals(other.getUserBConfirm()))
            && (this.getArrangementStatus() == null ? other.getArrangementStatus() == null : this.getArrangementStatus().equals(other.getArrangementStatus()))
            && (this.getCancelReason() == null ? other.getCancelReason() == null : this.getCancelReason().equals(other.getCancelReason()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getArrangementId() == null) ? 0 : getArrangementId().hashCode());
        result = prime * result + ((getRequestId() == null) ? 0 : getRequestId().hashCode());
        result = prime * result + ((getMatchmakerId() == null) ? 0 : getMatchmakerId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTargetUserId() == null) ? 0 : getTargetUserId().hashCode());
        result = prime * result + ((getDateTime() == null) ? 0 : getDateTime().hashCode());
        result = prime * result + ((getDateLocation() == null) ? 0 : getDateLocation().hashCode());
        result = prime * result + ((getDateType() == null) ? 0 : getDateType().hashCode());
        result = prime * result + ((getDatePlan() == null) ? 0 : getDatePlan().hashCode());
        result = prime * result + ((getUserAConfirm() == null) ? 0 : getUserAConfirm().hashCode());
        result = prime * result + ((getUserBConfirm() == null) ? 0 : getUserBConfirm().hashCode());
        result = prime * result + ((getArrangementStatus() == null) ? 0 : getArrangementStatus().hashCode());
        result = prime * result + ((getCancelReason() == null) ? 0 : getCancelReason().hashCode());
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
        sb.append(", arrangementId=").append(arrangementId);
        sb.append(", requestId=").append(requestId);
        sb.append(", matchmakerId=").append(matchmakerId);
        sb.append(", userId=").append(userId);
        sb.append(", targetUserId=").append(targetUserId);
        sb.append(", dateTime=").append(dateTime);
        sb.append(", dateLocation=").append(dateLocation);
        sb.append(", dateType=").append(dateType);
        sb.append(", datePlan=").append(datePlan);
        sb.append(", userAConfirm=").append(userAConfirm);
        sb.append(", userBConfirm=").append(userBConfirm);
        sb.append(", arrangementStatus=").append(arrangementStatus);
        sb.append(", cancelReason=").append(cancelReason);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}