package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_matchmaking_request
 */
@TableName(value ="tb_matchmaking_request")
@Data
public class TbMatchmakingRequest {
    /**
     * 申请ID
     */
    @TableId(type = IdType.AUTO)
    private Long requestId;

    /**
     * 申请用户ID
     */
    private Long userId;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 红娘ID（为空表示系统分配）
     */
    private Long matchmakerId;

    /**
     * 申请留言
     */
    private String requestMessage;

    /**
     * 申请状态：
     * 0-待红娘处理
     * 1-红娘已接受，待目标用户确认
     * 2-红娘已拒绝
     * 3-目标用户已拒绝
     * 4-双方都同意，可以安排约会
     * 5-已完成
     */
    private Integer requestStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

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
        TbMatchmakingRequest other = (TbMatchmakingRequest) that;
        return (this.getRequestId() == null ? other.getRequestId() == null : this.getRequestId().equals(other.getRequestId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTargetUserId() == null ? other.getTargetUserId() == null : this.getTargetUserId().equals(other.getTargetUserId()))
            && (this.getMatchmakerId() == null ? other.getMatchmakerId() == null : this.getMatchmakerId().equals(other.getMatchmakerId()))
            && (this.getRequestMessage() == null ? other.getRequestMessage() == null : this.getRequestMessage().equals(other.getRequestMessage()))
            && (this.getRequestStatus() == null ? other.getRequestStatus() == null : this.getRequestStatus().equals(other.getRequestStatus()))
            && (this.getRejectReason() == null ? other.getRejectReason() == null : this.getRejectReason().equals(other.getRejectReason()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRequestId() == null) ? 0 : getRequestId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTargetUserId() == null) ? 0 : getTargetUserId().hashCode());
        result = prime * result + ((getMatchmakerId() == null) ? 0 : getMatchmakerId().hashCode());
        result = prime * result + ((getRequestMessage() == null) ? 0 : getRequestMessage().hashCode());
        result = prime * result + ((getRequestStatus() == null) ? 0 : getRequestStatus().hashCode());
        result = prime * result + ((getRejectReason() == null) ? 0 : getRejectReason().hashCode());
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
        sb.append(", requestId=").append(requestId);
        sb.append(", userId=").append(userId);
        sb.append(", targetUserId=").append(targetUserId);
        sb.append(", matchmakerId=").append(matchmakerId);
        sb.append(", requestMessage=").append(requestMessage);
        sb.append(", requestStatus=").append(requestStatus);
        sb.append(", rejectReason=").append(rejectReason);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}