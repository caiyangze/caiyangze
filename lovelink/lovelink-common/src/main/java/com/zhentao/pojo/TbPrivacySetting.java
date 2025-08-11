package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_privacy_setting
 */
@TableName(value ="tb_privacy_setting")
@Data
public class TbPrivacySetting {
    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    private Long settingId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 显示在线状态：0-不显示，1-显示
     */
    private Integer showOnlineStatus;

    /**
     * 显示最后登录时间：0-不显示，1-显示
     */
    private Integer showLastLoginTime;

    /**
     * 显示访客记录：0-不显示，1-显示
     */
    private Integer showVisitor;

    /**
     * 允许陌生人留言：0-不允许，1-允许
     */
    private Integer allowStrangerMessage;

    /**
     * 资料可见级别：1-所有人，2-已关注的人，3-VIP用户
     */
    private Integer profileVisibleLevel;

    /**
     * 相册可见级别：1-所有人，2-已关注的人，3-VIP用户
     */
    private Integer photoVisibleLevel;

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
        TbPrivacySetting other = (TbPrivacySetting) that;
        return (this.getSettingId() == null ? other.getSettingId() == null : this.getSettingId().equals(other.getSettingId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getShowOnlineStatus() == null ? other.getShowOnlineStatus() == null : this.getShowOnlineStatus().equals(other.getShowOnlineStatus()))
            && (this.getShowLastLoginTime() == null ? other.getShowLastLoginTime() == null : this.getShowLastLoginTime().equals(other.getShowLastLoginTime()))
            && (this.getShowVisitor() == null ? other.getShowVisitor() == null : this.getShowVisitor().equals(other.getShowVisitor()))
            && (this.getAllowStrangerMessage() == null ? other.getAllowStrangerMessage() == null : this.getAllowStrangerMessage().equals(other.getAllowStrangerMessage()))
            && (this.getProfileVisibleLevel() == null ? other.getProfileVisibleLevel() == null : this.getProfileVisibleLevel().equals(other.getProfileVisibleLevel()))
            && (this.getPhotoVisibleLevel() == null ? other.getPhotoVisibleLevel() == null : this.getPhotoVisibleLevel().equals(other.getPhotoVisibleLevel()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSettingId() == null) ? 0 : getSettingId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getShowOnlineStatus() == null) ? 0 : getShowOnlineStatus().hashCode());
        result = prime * result + ((getShowLastLoginTime() == null) ? 0 : getShowLastLoginTime().hashCode());
        result = prime * result + ((getShowVisitor() == null) ? 0 : getShowVisitor().hashCode());
        result = prime * result + ((getAllowStrangerMessage() == null) ? 0 : getAllowStrangerMessage().hashCode());
        result = prime * result + ((getProfileVisibleLevel() == null) ? 0 : getProfileVisibleLevel().hashCode());
        result = prime * result + ((getPhotoVisibleLevel() == null) ? 0 : getPhotoVisibleLevel().hashCode());
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
        sb.append(", settingId=").append(settingId);
        sb.append(", userId=").append(userId);
        sb.append(", showOnlineStatus=").append(showOnlineStatus);
        sb.append(", showLastLoginTime=").append(showLastLoginTime);
        sb.append(", showVisitor=").append(showVisitor);
        sb.append(", allowStrangerMessage=").append(allowStrangerMessage);
        sb.append(", profileVisibleLevel=").append(profileVisibleLevel);
        sb.append(", photoVisibleLevel=").append(photoVisibleLevel);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}