package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_matchmaker_application
 */
@TableName(value ="tb_matchmaker_application")
@Data
public class TbMatchmakerApplication {
    /**
     * 申请ID
     */
    @TableId(type = IdType.AUTO)
    private Long applicationId;

    /**
     * 申请用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 身份证号（加密存储）
     */
    private String idCardNo;

    /**
     * 身份证正面照片URL
     */
    private String idCardFront;

    /**
     * 身份证背面照片URL
     */
    private String idCardBack;

    /**
     * 期望服务区域
     */
    private String serviceArea;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 相关经验
     */
    private String experience;

    /**
     * 申请状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer applicationStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

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
        TbMatchmakerApplication other = (TbMatchmakerApplication) that;
        return (this.getApplicationId() == null ? other.getApplicationId() == null : this.getApplicationId().equals(other.getApplicationId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRealName() == null ? other.getRealName() == null : this.getRealName().equals(other.getRealName()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getIdCardNo() == null ? other.getIdCardNo() == null : this.getIdCardNo().equals(other.getIdCardNo()))
            && (this.getIdCardFront() == null ? other.getIdCardFront() == null : this.getIdCardFront().equals(other.getIdCardFront()))
            && (this.getIdCardBack() == null ? other.getIdCardBack() == null : this.getIdCardBack().equals(other.getIdCardBack()))
            && (this.getServiceArea() == null ? other.getServiceArea() == null : this.getServiceArea().equals(other.getServiceArea()))
            && (this.getIntroduction() == null ? other.getIntroduction() == null : this.getIntroduction().equals(other.getIntroduction()))
            && (this.getExperience() == null ? other.getExperience() == null : this.getExperience().equals(other.getExperience()))
            && (this.getApplicationStatus() == null ? other.getApplicationStatus() == null : this.getApplicationStatus().equals(other.getApplicationStatus()))
            && (this.getRejectReason() == null ? other.getRejectReason() == null : this.getRejectReason().equals(other.getRejectReason()))
            && (this.getReviewerId() == null ? other.getReviewerId() == null : this.getReviewerId().equals(other.getReviewerId()))
            && (this.getReviewTime() == null ? other.getReviewTime() == null : this.getReviewTime().equals(other.getReviewTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getApplicationId() == null) ? 0 : getApplicationId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRealName() == null) ? 0 : getRealName().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getIdCardNo() == null) ? 0 : getIdCardNo().hashCode());
        result = prime * result + ((getIdCardFront() == null) ? 0 : getIdCardFront().hashCode());
        result = prime * result + ((getIdCardBack() == null) ? 0 : getIdCardBack().hashCode());
        result = prime * result + ((getServiceArea() == null) ? 0 : getServiceArea().hashCode());
        result = prime * result + ((getIntroduction() == null) ? 0 : getIntroduction().hashCode());
        result = prime * result + ((getExperience() == null) ? 0 : getExperience().hashCode());
        result = prime * result + ((getApplicationStatus() == null) ? 0 : getApplicationStatus().hashCode());
        result = prime * result + ((getRejectReason() == null) ? 0 : getRejectReason().hashCode());
        result = prime * result + ((getReviewerId() == null) ? 0 : getReviewerId().hashCode());
        result = prime * result + ((getReviewTime() == null) ? 0 : getReviewTime().hashCode());
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
        sb.append(", applicationId=").append(applicationId);
        sb.append(", userId=").append(userId);
        sb.append(", realName=").append(realName);
        sb.append(", phone=").append(phone);
        sb.append(", idCardNo=").append(idCardNo);
        sb.append(", idCardFront=").append(idCardFront);
        sb.append(", idCardBack=").append(idCardBack);
        sb.append(", serviceArea=").append(serviceArea);
        sb.append(", introduction=").append(introduction);
        sb.append(", experience=").append(experience);
        sb.append(", applicationStatus=").append(applicationStatus);
        sb.append(", rejectReason=").append(rejectReason);
        sb.append(", reviewerId=").append(reviewerId);
        sb.append(", reviewTime=").append(reviewTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}