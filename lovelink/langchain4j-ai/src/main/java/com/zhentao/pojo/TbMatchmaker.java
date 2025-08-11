package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_matchmaker
 */
@TableName(value ="tb_matchmaker")
@Data
public class TbMatchmaker {
    /**
     * 红娘ID
     */
    @TableId(type = IdType.AUTO)
    private Long matchmakerId;

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
     * 联系电话
     */
    private String phone;

    /**
     * 红娘等级：1-预备红娘，2-正式红娘，3-金牌红娘
     */
    private Integer matchmakerLevel;

    /**
     * 服务区域
     */
    private String serviceArea;

    /**
     * 从业年限
     */
    private Integer serviceYears;

    /**
     * 成功牵线次数
     */
    private Integer successCount;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 资质证书URL
     */
    private String certification;

    /**
     * 红娘状态：0-冻结，1-正常
     */
    private Integer matchmakerStatus;

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
        TbMatchmaker other = (TbMatchmaker) that;
        return (this.getMatchmakerId() == null ? other.getMatchmakerId() == null : this.getMatchmakerId().equals(other.getMatchmakerId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRealName() == null ? other.getRealName() == null : this.getRealName().equals(other.getRealName()))
            && (this.getIdCardNo() == null ? other.getIdCardNo() == null : this.getIdCardNo().equals(other.getIdCardNo()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getMatchmakerLevel() == null ? other.getMatchmakerLevel() == null : this.getMatchmakerLevel().equals(other.getMatchmakerLevel()))
            && (this.getServiceArea() == null ? other.getServiceArea() == null : this.getServiceArea().equals(other.getServiceArea()))
            && (this.getServiceYears() == null ? other.getServiceYears() == null : this.getServiceYears().equals(other.getServiceYears()))
            && (this.getSuccessCount() == null ? other.getSuccessCount() == null : this.getSuccessCount().equals(other.getSuccessCount()))
            && (this.getIntroduction() == null ? other.getIntroduction() == null : this.getIntroduction().equals(other.getIntroduction()))
            && (this.getCertification() == null ? other.getCertification() == null : this.getCertification().equals(other.getCertification()))
            && (this.getMatchmakerStatus() == null ? other.getMatchmakerStatus() == null : this.getMatchmakerStatus().equals(other.getMatchmakerStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMatchmakerId() == null) ? 0 : getMatchmakerId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRealName() == null) ? 0 : getRealName().hashCode());
        result = prime * result + ((getIdCardNo() == null) ? 0 : getIdCardNo().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getMatchmakerLevel() == null) ? 0 : getMatchmakerLevel().hashCode());
        result = prime * result + ((getServiceArea() == null) ? 0 : getServiceArea().hashCode());
        result = prime * result + ((getServiceYears() == null) ? 0 : getServiceYears().hashCode());
        result = prime * result + ((getSuccessCount() == null) ? 0 : getSuccessCount().hashCode());
        result = prime * result + ((getIntroduction() == null) ? 0 : getIntroduction().hashCode());
        result = prime * result + ((getCertification() == null) ? 0 : getCertification().hashCode());
        result = prime * result + ((getMatchmakerStatus() == null) ? 0 : getMatchmakerStatus().hashCode());
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
        sb.append(", matchmakerId=").append(matchmakerId);
        sb.append(", userId=").append(userId);
        sb.append(", realName=").append(realName);
        sb.append(", idCardNo=").append(idCardNo);
        sb.append(", phone=").append(phone);
        sb.append(", matchmakerLevel=").append(matchmakerLevel);
        sb.append(", serviceArea=").append(serviceArea);
        sb.append(", serviceYears=").append(serviceYears);
        sb.append(", successCount=").append(successCount);
        sb.append(", introduction=").append(introduction);
        sb.append(", certification=").append(certification);
        sb.append(", matchmakerStatus=").append(matchmakerStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}