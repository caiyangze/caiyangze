package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_user_verification
 */
@TableName(value ="tb_user_verification")
@Data
public class TbUserVerification {
    /**
     * 认证ID
     */
    @TableId(type = IdType.AUTO)
    private Long verificationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

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
     * 人脸照片URL
     */
    private String facePhoto;

    /**
     * 认证状态：0-待审核，1-已通过，2-已拒绝
     */
    private Integer verificationStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 认证通过时间
     */
    private Date verifiedTime;

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
        TbUserVerification other = (TbUserVerification) that;
        return (this.getVerificationId() == null ? other.getVerificationId() == null : this.getVerificationId().equals(other.getVerificationId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRealName() == null ? other.getRealName() == null : this.getRealName().equals(other.getRealName()))
            && (this.getIdCardNo() == null ? other.getIdCardNo() == null : this.getIdCardNo().equals(other.getIdCardNo()))
            && (this.getIdCardFront() == null ? other.getIdCardFront() == null : this.getIdCardFront().equals(other.getIdCardFront()))
            && (this.getIdCardBack() == null ? other.getIdCardBack() == null : this.getIdCardBack().equals(other.getIdCardBack()))
            && (this.getFacePhoto() == null ? other.getFacePhoto() == null : this.getFacePhoto().equals(other.getFacePhoto()))
            && (this.getVerificationStatus() == null ? other.getVerificationStatus() == null : this.getVerificationStatus().equals(other.getVerificationStatus()))
            && (this.getRejectReason() == null ? other.getRejectReason() == null : this.getRejectReason().equals(other.getRejectReason()))
            && (this.getVerifiedTime() == null ? other.getVerifiedTime() == null : this.getVerifiedTime().equals(other.getVerifiedTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getVerificationId() == null) ? 0 : getVerificationId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRealName() == null) ? 0 : getRealName().hashCode());
        result = prime * result + ((getIdCardNo() == null) ? 0 : getIdCardNo().hashCode());
        result = prime * result + ((getIdCardFront() == null) ? 0 : getIdCardFront().hashCode());
        result = prime * result + ((getIdCardBack() == null) ? 0 : getIdCardBack().hashCode());
        result = prime * result + ((getFacePhoto() == null) ? 0 : getFacePhoto().hashCode());
        result = prime * result + ((getVerificationStatus() == null) ? 0 : getVerificationStatus().hashCode());
        result = prime * result + ((getRejectReason() == null) ? 0 : getRejectReason().hashCode());
        result = prime * result + ((getVerifiedTime() == null) ? 0 : getVerifiedTime().hashCode());
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
        sb.append(", verificationId=").append(verificationId);
        sb.append(", userId=").append(userId);
        sb.append(", realName=").append(realName);
        sb.append(", idCardNo=").append(idCardNo);
        sb.append(", idCardFront=").append(idCardFront);
        sb.append(", idCardBack=").append(idCardBack);
        sb.append(", facePhoto=").append(facePhoto);
        sb.append(", verificationStatus=").append(verificationStatus);
        sb.append(", rejectReason=").append(rejectReason);
        sb.append(", verifiedTime=").append(verifiedTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}