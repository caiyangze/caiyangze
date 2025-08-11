package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_user_profile
 */
@TableName(value ="tb_user_profile")
@Data
public class TbUserProfile {
    /**
     * 资料ID
     */
    @TableId(type = IdType.AUTO)
    private Long profileId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户年龄
     */
    private Integer age;

    /**
     * 身高(cm)
     */
    private Integer height;

    /**
     * 体重(kg)
     */
    private Integer weight;

    /**
     * 学历：1-高中及以下，2-大专，3-本科，4-硕士，5-博士
     */
    private Integer education;

    /**
     * 学历认证图片URL
     */
    private String educationCert;

    /**
     * 公司
     */
    private String company;

    /**
     * 职位
     */
    private String position;

    /**
     * 收入水平：1-5k以下，2-5k-10k，3-10k-20k，4-20k-50k，5-50k以上
     */
    private Integer incomeLevel;

    /**
     * 工作城市
     */
    private String workCity;

    /**
     * 家乡
     */
    private String hometown;

    /**
     * 婚姻状况：1-未婚，2-离异，3-丧偶
     */
    private Integer maritalStatus;

    /**
     * 是否有子女：0-无，1-有
     */
    private Integer hasChildren;

    /**
     * 住房情况：1-租房，2-有房贷款，3-有房无贷款，4-和家人同住
     */
    private Integer houseStatus;

    /**
     * 车辆情况：1-无车，2-有车有贷款，3-有车无贷款
     */
    private Integer carStatus;

    /**
     * 自我介绍
     */
    private String selfIntroduction;

    /**
     * 兴趣爱好
     */
    private String hobby;

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
        TbUserProfile other = (TbUserProfile) that;
        return (this.getProfileId() == null ? other.getProfileId() == null : this.getProfileId().equals(other.getProfileId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRealName() == null ? other.getRealName() == null : this.getRealName().equals(other.getRealName()))
            && (this.getAge() == null ? other.getAge() == null : this.getAge().equals(other.getAge()))
            && (this.getHeight() == null ? other.getHeight() == null : this.getHeight().equals(other.getHeight()))
            && (this.getWeight() == null ? other.getWeight() == null : this.getWeight().equals(other.getWeight()))
            && (this.getEducation() == null ? other.getEducation() == null : this.getEducation().equals(other.getEducation()))
            && (this.getEducationCert() == null ? other.getEducationCert() == null : this.getEducationCert().equals(other.getEducationCert()))
            && (this.getCompany() == null ? other.getCompany() == null : this.getCompany().equals(other.getCompany()))
            && (this.getPosition() == null ? other.getPosition() == null : this.getPosition().equals(other.getPosition()))
            && (this.getIncomeLevel() == null ? other.getIncomeLevel() == null : this.getIncomeLevel().equals(other.getIncomeLevel()))
            && (this.getWorkCity() == null ? other.getWorkCity() == null : this.getWorkCity().equals(other.getWorkCity()))
            && (this.getHometown() == null ? other.getHometown() == null : this.getHometown().equals(other.getHometown()))
            && (this.getMaritalStatus() == null ? other.getMaritalStatus() == null : this.getMaritalStatus().equals(other.getMaritalStatus()))
            && (this.getHasChildren() == null ? other.getHasChildren() == null : this.getHasChildren().equals(other.getHasChildren()))
            && (this.getHouseStatus() == null ? other.getHouseStatus() == null : this.getHouseStatus().equals(other.getHouseStatus()))
            && (this.getCarStatus() == null ? other.getCarStatus() == null : this.getCarStatus().equals(other.getCarStatus()))
            && (this.getSelfIntroduction() == null ? other.getSelfIntroduction() == null : this.getSelfIntroduction().equals(other.getSelfIntroduction()))
            && (this.getHobby() == null ? other.getHobby() == null : this.getHobby().equals(other.getHobby()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getProfileId() == null) ? 0 : getProfileId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRealName() == null) ? 0 : getRealName().hashCode());
        result = prime * result + ((getAge() == null) ? 0 : getAge().hashCode());
        result = prime * result + ((getHeight() == null) ? 0 : getHeight().hashCode());
        result = prime * result + ((getWeight() == null) ? 0 : getWeight().hashCode());
        result = prime * result + ((getEducation() == null) ? 0 : getEducation().hashCode());
        result = prime * result + ((getEducationCert() == null) ? 0 : getEducationCert().hashCode());
        result = prime * result + ((getCompany() == null) ? 0 : getCompany().hashCode());
        result = prime * result + ((getPosition() == null) ? 0 : getPosition().hashCode());
        result = prime * result + ((getIncomeLevel() == null) ? 0 : getIncomeLevel().hashCode());
        result = prime * result + ((getWorkCity() == null) ? 0 : getWorkCity().hashCode());
        result = prime * result + ((getHometown() == null) ? 0 : getHometown().hashCode());
        result = prime * result + ((getMaritalStatus() == null) ? 0 : getMaritalStatus().hashCode());
        result = prime * result + ((getHasChildren() == null) ? 0 : getHasChildren().hashCode());
        result = prime * result + ((getHouseStatus() == null) ? 0 : getHouseStatus().hashCode());
        result = prime * result + ((getCarStatus() == null) ? 0 : getCarStatus().hashCode());
        result = prime * result + ((getSelfIntroduction() == null) ? 0 : getSelfIntroduction().hashCode());
        result = prime * result + ((getHobby() == null) ? 0 : getHobby().hashCode());
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
        sb.append(", profileId=").append(profileId);
        sb.append(", userId=").append(userId);
        sb.append(", realName=").append(realName);
        sb.append(", age=").append(age);
        sb.append(", height=").append(height);
        sb.append(", weight=").append(weight);
        sb.append(", education=").append(education);
        sb.append(", educationCert=").append(educationCert);
        sb.append(", company=").append(company);
        sb.append(", position=").append(position);
        sb.append(", incomeLevel=").append(incomeLevel);
        sb.append(", workCity=").append(workCity);
        sb.append(", hometown=").append(hometown);
        sb.append(", maritalStatus=").append(maritalStatus);
        sb.append(", hasChildren=").append(hasChildren);
        sb.append(", houseStatus=").append(houseStatus);
        sb.append(", carStatus=").append(carStatus);
        sb.append(", selfIntroduction=").append(selfIntroduction);
        sb.append(", hobby=").append(hobby);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}