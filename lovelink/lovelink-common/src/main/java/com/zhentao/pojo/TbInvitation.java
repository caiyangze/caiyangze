package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_invitation
 */
@TableName(value ="tb_invitation")
@Data
public class TbInvitation {
    /**
     * 邀请ID
     */
    @TableId(type = IdType.AUTO)
    private Long invitationId;

    /**
     * 邀请人ID
     */
    private Long inviterId;

    /**
     * 被邀请人ID
     */
    private Long inviteeId;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 邀请链接
     */
    private String invitationLink;

    /**
     * 邀请类型：1-普通邀请，2-红娘邀请
     */
    private Integer invitationType;

    /**
     * 邀请状态：0-未使用，1-已使用，2-已过期
     */
    private Integer invitationStatus;

    /**
     * 注册时间
     */
    private Date registerTime;

    /**
     * 奖励状态：0-未发放，1-已发放
     */
    private Integer rewardStatus;

    /**
     * 奖励金额
     */
    private BigDecimal rewardAmount;

    /**
     * 奖励虚拟币
     */
    private Integer rewardCoin;

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
        TbInvitation other = (TbInvitation) that;
        return (this.getInvitationId() == null ? other.getInvitationId() == null : this.getInvitationId().equals(other.getInvitationId()))
            && (this.getInviterId() == null ? other.getInviterId() == null : this.getInviterId().equals(other.getInviterId()))
            && (this.getInviteeId() == null ? other.getInviteeId() == null : this.getInviteeId().equals(other.getInviteeId()))
            && (this.getInvitationCode() == null ? other.getInvitationCode() == null : this.getInvitationCode().equals(other.getInvitationCode()))
            && (this.getInvitationLink() == null ? other.getInvitationLink() == null : this.getInvitationLink().equals(other.getInvitationLink()))
            && (this.getInvitationType() == null ? other.getInvitationType() == null : this.getInvitationType().equals(other.getInvitationType()))
            && (this.getInvitationStatus() == null ? other.getInvitationStatus() == null : this.getInvitationStatus().equals(other.getInvitationStatus()))
            && (this.getRegisterTime() == null ? other.getRegisterTime() == null : this.getRegisterTime().equals(other.getRegisterTime()))
            && (this.getRewardStatus() == null ? other.getRewardStatus() == null : this.getRewardStatus().equals(other.getRewardStatus()))
            && (this.getRewardAmount() == null ? other.getRewardAmount() == null : this.getRewardAmount().equals(other.getRewardAmount()))
            && (this.getRewardCoin() == null ? other.getRewardCoin() == null : this.getRewardCoin().equals(other.getRewardCoin()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInvitationId() == null) ? 0 : getInvitationId().hashCode());
        result = prime * result + ((getInviterId() == null) ? 0 : getInviterId().hashCode());
        result = prime * result + ((getInviteeId() == null) ? 0 : getInviteeId().hashCode());
        result = prime * result + ((getInvitationCode() == null) ? 0 : getInvitationCode().hashCode());
        result = prime * result + ((getInvitationLink() == null) ? 0 : getInvitationLink().hashCode());
        result = prime * result + ((getInvitationType() == null) ? 0 : getInvitationType().hashCode());
        result = prime * result + ((getInvitationStatus() == null) ? 0 : getInvitationStatus().hashCode());
        result = prime * result + ((getRegisterTime() == null) ? 0 : getRegisterTime().hashCode());
        result = prime * result + ((getRewardStatus() == null) ? 0 : getRewardStatus().hashCode());
        result = prime * result + ((getRewardAmount() == null) ? 0 : getRewardAmount().hashCode());
        result = prime * result + ((getRewardCoin() == null) ? 0 : getRewardCoin().hashCode());
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
        sb.append(", invitationId=").append(invitationId);
        sb.append(", inviterId=").append(inviterId);
        sb.append(", inviteeId=").append(inviteeId);
        sb.append(", invitationCode=").append(invitationCode);
        sb.append(", invitationLink=").append(invitationLink);
        sb.append(", invitationType=").append(invitationType);
        sb.append(", invitationStatus=").append(invitationStatus);
        sb.append(", registerTime=").append(registerTime);
        sb.append(", rewardStatus=").append(rewardStatus);
        sb.append(", rewardAmount=").append(rewardAmount);
        sb.append(", rewardCoin=").append(rewardCoin);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}