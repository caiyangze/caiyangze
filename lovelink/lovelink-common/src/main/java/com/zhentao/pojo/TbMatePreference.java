package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_mate_preference
 */
@TableName(value ="tb_mate_preference")
@Data
public class TbMatePreference {
    /**
     * 偏好ID
     */
    @TableId(type = IdType.AUTO)
    private Long preferenceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 最小年龄
     */
    private Integer ageMin;

    /**
     * 最大年龄
     */
    private Integer ageMax;

    /**
     * 最小身高(cm)
     */
    private Integer heightMin;

    /**
     * 最大身高(cm)
     */
    private Integer heightMax;

    /**
     * 最低学历
     */
    private Integer educationMin;

    /**
     * 最低收入水平
     */
    private Integer incomeMin;

    /**
     * 可接受的婚姻状况，多选逗号分隔
     */
    private String maritalStatus;

    /**
     * 期望工作城市，多选逗号分隔
     */
    private String workCity;

    /**
     * 是否要求有房：0-不要求，1-要求
     */
    private Integer hasHouse;

    /**
     * 是否要求有车：0-不要求，1-要求
     */
    private Integer hasCar;

    /**
     * 其他要求
     */
    private String otherRequirements;

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
        TbMatePreference other = (TbMatePreference) that;
        return (this.getPreferenceId() == null ? other.getPreferenceId() == null : this.getPreferenceId().equals(other.getPreferenceId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAgeMin() == null ? other.getAgeMin() == null : this.getAgeMin().equals(other.getAgeMin()))
            && (this.getAgeMax() == null ? other.getAgeMax() == null : this.getAgeMax().equals(other.getAgeMax()))
            && (this.getHeightMin() == null ? other.getHeightMin() == null : this.getHeightMin().equals(other.getHeightMin()))
            && (this.getHeightMax() == null ? other.getHeightMax() == null : this.getHeightMax().equals(other.getHeightMax()))
            && (this.getEducationMin() == null ? other.getEducationMin() == null : this.getEducationMin().equals(other.getEducationMin()))
            && (this.getIncomeMin() == null ? other.getIncomeMin() == null : this.getIncomeMin().equals(other.getIncomeMin()))
            && (this.getMaritalStatus() == null ? other.getMaritalStatus() == null : this.getMaritalStatus().equals(other.getMaritalStatus()))
            && (this.getWorkCity() == null ? other.getWorkCity() == null : this.getWorkCity().equals(other.getWorkCity()))
            && (this.getHasHouse() == null ? other.getHasHouse() == null : this.getHasHouse().equals(other.getHasHouse()))
            && (this.getHasCar() == null ? other.getHasCar() == null : this.getHasCar().equals(other.getHasCar()))
            && (this.getOtherRequirements() == null ? other.getOtherRequirements() == null : this.getOtherRequirements().equals(other.getOtherRequirements()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPreferenceId() == null) ? 0 : getPreferenceId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAgeMin() == null) ? 0 : getAgeMin().hashCode());
        result = prime * result + ((getAgeMax() == null) ? 0 : getAgeMax().hashCode());
        result = prime * result + ((getHeightMin() == null) ? 0 : getHeightMin().hashCode());
        result = prime * result + ((getHeightMax() == null) ? 0 : getHeightMax().hashCode());
        result = prime * result + ((getEducationMin() == null) ? 0 : getEducationMin().hashCode());
        result = prime * result + ((getIncomeMin() == null) ? 0 : getIncomeMin().hashCode());
        result = prime * result + ((getMaritalStatus() == null) ? 0 : getMaritalStatus().hashCode());
        result = prime * result + ((getWorkCity() == null) ? 0 : getWorkCity().hashCode());
        result = prime * result + ((getHasHouse() == null) ? 0 : getHasHouse().hashCode());
        result = prime * result + ((getHasCar() == null) ? 0 : getHasCar().hashCode());
        result = prime * result + ((getOtherRequirements() == null) ? 0 : getOtherRequirements().hashCode());
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
        sb.append(", preferenceId=").append(preferenceId);
        sb.append(", userId=").append(userId);
        sb.append(", ageMin=").append(ageMin);
        sb.append(", ageMax=").append(ageMax);
        sb.append(", heightMin=").append(heightMin);
        sb.append(", heightMax=").append(heightMax);
        sb.append(", educationMin=").append(educationMin);
        sb.append(", incomeMin=").append(incomeMin);
        sb.append(", maritalStatus=").append(maritalStatus);
        sb.append(", workCity=").append(workCity);
        sb.append(", hasHouse=").append(hasHouse);
        sb.append(", hasCar=").append(hasCar);
        sb.append(", otherRequirements=").append(otherRequirements);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}