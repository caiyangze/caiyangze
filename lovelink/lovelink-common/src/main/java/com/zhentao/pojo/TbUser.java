package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 
 * @TableName tb_user
 */
@TableName(value ="tb_user")
@Data
public class TbUser {
    //详细资料
    @TableField(exist = false)
    private TbUserProfile userProfile;
    //标签关联
    @TableField(exist = false)
    private List<TbUserTag> userTags;
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private Date birthDate;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 粉丝数
     */
    private Integer fan;

    /**
     * 被关注总数
     */
    private Integer countFollow;

    /**
     * 被赞总数
     */
    private Integer countLike;

    /**
     * 用户角色：1-普通用户，2-红娘
     */
    private Integer userRole;

    /**
     * 注册来源：1-手机号，2-微信，3-QQ
     */
    private Integer registerSource;

    /**
     * 第三方平台ID
     */
    private String openId;

    /**
     * 注册时间
     */
    private Date registerTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 账号状态：0-冻结，1-正常，2-注销
     */
    private Integer accountStatus;

    /**
     * 是否实名认证：0-否，1-是
     */
    private Integer isVerified;

    /**
     * 是否VIP：0-否，1-是
     */
    private Integer isVip;

    /**
     * VIP过期时间
     */
    private Date vipExpireTime;

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
        TbUser other = (TbUser) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getPassword() == null ? other.getPassword() == null : this.getPassword().equals(other.getPassword()))
            && (this.getNickname() == null ? other.getNickname() == null : this.getNickname().equals(other.getNickname()))
            && (this.getGender() == null ? other.getGender() == null : this.getGender().equals(other.getGender()))
            && (this.getBirthDate() == null ? other.getBirthDate() == null : this.getBirthDate().equals(other.getBirthDate()))
            && (this.getAvatarUrl() == null ? other.getAvatarUrl() == null : this.getAvatarUrl().equals(other.getAvatarUrl()))
            && (this.getFan() == null ? other.getFan() == null : this.getFan().equals(other.getFan()))
            && (this.getCountFollow() == null ? other.getCountFollow() == null : this.getCountFollow().equals(other.getCountFollow()))
            && (this.getCountLike() == null ? other.getCountLike() == null : this.getCountLike().equals(other.getCountLike()))
            && (this.getUserRole() == null ? other.getUserRole() == null : this.getUserRole().equals(other.getUserRole()))
            && (this.getRegisterSource() == null ? other.getRegisterSource() == null : this.getRegisterSource().equals(other.getRegisterSource()))
            && (this.getOpenId() == null ? other.getOpenId() == null : this.getOpenId().equals(other.getOpenId()))
            && (this.getRegisterTime() == null ? other.getRegisterTime() == null : this.getRegisterTime().equals(other.getRegisterTime()))
            && (this.getLastLoginTime() == null ? other.getLastLoginTime() == null : this.getLastLoginTime().equals(other.getLastLoginTime()))
            && (this.getLastLoginIp() == null ? other.getLastLoginIp() == null : this.getLastLoginIp().equals(other.getLastLoginIp()))
            && (this.getAccountStatus() == null ? other.getAccountStatus() == null : this.getAccountStatus().equals(other.getAccountStatus()))
            && (this.getIsVerified() == null ? other.getIsVerified() == null : this.getIsVerified().equals(other.getIsVerified()))
            && (this.getIsVip() == null ? other.getIsVip() == null : this.getIsVip().equals(other.getIsVip()))
            && (this.getVipExpireTime() == null ? other.getVipExpireTime() == null : this.getVipExpireTime().equals(other.getVipExpireTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getPassword() == null) ? 0 : getPassword().hashCode());
        result = prime * result + ((getNickname() == null) ? 0 : getNickname().hashCode());
        result = prime * result + ((getGender() == null) ? 0 : getGender().hashCode());
        result = prime * result + ((getBirthDate() == null) ? 0 : getBirthDate().hashCode());
        result = prime * result + ((getAvatarUrl() == null) ? 0 : getAvatarUrl().hashCode());
        result = prime * result + ((getFan() == null) ? 0 : getFan().hashCode());
        result = prime * result + ((getCountFollow() == null) ? 0 : getCountFollow().hashCode());
        result = prime * result + ((getCountLike() == null) ? 0 : getCountLike().hashCode());
        result = prime * result + ((getUserRole() == null) ? 0 : getUserRole().hashCode());
        result = prime * result + ((getRegisterSource() == null) ? 0 : getRegisterSource().hashCode());
        result = prime * result + ((getOpenId() == null) ? 0 : getOpenId().hashCode());
        result = prime * result + ((getRegisterTime() == null) ? 0 : getRegisterTime().hashCode());
        result = prime * result + ((getLastLoginTime() == null) ? 0 : getLastLoginTime().hashCode());
        result = prime * result + ((getLastLoginIp() == null) ? 0 : getLastLoginIp().hashCode());
        result = prime * result + ((getAccountStatus() == null) ? 0 : getAccountStatus().hashCode());
        result = prime * result + ((getIsVerified() == null) ? 0 : getIsVerified().hashCode());
        result = prime * result + ((getIsVip() == null) ? 0 : getIsVip().hashCode());
        result = prime * result + ((getVipExpireTime() == null) ? 0 : getVipExpireTime().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", phone=").append(phone);
        sb.append(", password=").append(password);
        sb.append(", nickname=").append(nickname);
        sb.append(", gender=").append(gender);
        sb.append(", birthDate=").append(birthDate);
        sb.append(", avatarUrl=").append(avatarUrl);
        sb.append(", fan=").append(fan);
        sb.append(", countFollow=").append(countFollow);
        sb.append(", countLike=").append(countLike);
        sb.append(", userRole=").append(userRole);
        sb.append(", registerSource=").append(registerSource);
        sb.append(", openId=").append(openId);
        sb.append(", registerTime=").append(registerTime);
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", lastLoginIp=").append(lastLoginIp);
        sb.append(", accountStatus=").append(accountStatus);
        sb.append(", isVerified=").append(isVerified);
        sb.append(", isVip=").append(isVip);
        sb.append(", vipExpireTime=").append(vipExpireTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}