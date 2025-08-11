package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_chat_setting
 */
@TableName(value ="tb_chat_setting")
@Data
public class TbChatSetting {
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
     * 允许陌生人消息：0-否，1-是
     */
    private Integer allowStrangerMessage;

    /**
     * 消息通知：0-关闭，1-开启
     */
    private Integer messageNotification;

    /**
     * 声音通知：0-关闭，1-开启
     */
    private Integer soundNotification;

    /**
     * 震动通知：0-关闭，1-开启
     */
    private Integer vibrationNotification;

    /**
     * 显示已读状态：0-否，1-是
     */
    private Integer showReadStatus;

    /**
     * 自动下载媒体：0-否，1-是
     */
    private Integer autoDownloadMedia;

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
        TbChatSetting other = (TbChatSetting) that;
        return (this.getSettingId() == null ? other.getSettingId() == null : this.getSettingId().equals(other.getSettingId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getAllowStrangerMessage() == null ? other.getAllowStrangerMessage() == null : this.getAllowStrangerMessage().equals(other.getAllowStrangerMessage()))
            && (this.getMessageNotification() == null ? other.getMessageNotification() == null : this.getMessageNotification().equals(other.getMessageNotification()))
            && (this.getSoundNotification() == null ? other.getSoundNotification() == null : this.getSoundNotification().equals(other.getSoundNotification()))
            && (this.getVibrationNotification() == null ? other.getVibrationNotification() == null : this.getVibrationNotification().equals(other.getVibrationNotification()))
            && (this.getShowReadStatus() == null ? other.getShowReadStatus() == null : this.getShowReadStatus().equals(other.getShowReadStatus()))
            && (this.getAutoDownloadMedia() == null ? other.getAutoDownloadMedia() == null : this.getAutoDownloadMedia().equals(other.getAutoDownloadMedia()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSettingId() == null) ? 0 : getSettingId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getAllowStrangerMessage() == null) ? 0 : getAllowStrangerMessage().hashCode());
        result = prime * result + ((getMessageNotification() == null) ? 0 : getMessageNotification().hashCode());
        result = prime * result + ((getSoundNotification() == null) ? 0 : getSoundNotification().hashCode());
        result = prime * result + ((getVibrationNotification() == null) ? 0 : getVibrationNotification().hashCode());
        result = prime * result + ((getShowReadStatus() == null) ? 0 : getShowReadStatus().hashCode());
        result = prime * result + ((getAutoDownloadMedia() == null) ? 0 : getAutoDownloadMedia().hashCode());
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
        sb.append(", allowStrangerMessage=").append(allowStrangerMessage);
        sb.append(", messageNotification=").append(messageNotification);
        sb.append(", soundNotification=").append(soundNotification);
        sb.append(", vibrationNotification=").append(vibrationNotification);
        sb.append(", showReadStatus=").append(showReadStatus);
        sb.append(", autoDownloadMedia=").append(autoDownloadMedia);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}