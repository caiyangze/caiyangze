package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_announcement
 */
@TableName(value ="tb_announcement")
@Data
public class TbAnnouncement {
    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
    private Long announcementId;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型：1-系统公告，2-活动公告，3-维护公告
     */
    private Integer announcementType;

    /**
     * 目标类型：1-全部用户，2-特定用户组，3-管理员
     */
    private Integer targetType;

    /**
     * 目标ID列表（JSON格式）
     */
    private String targetIds;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop;

    /**
     * 状态：0-下线，1-上线
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long creatorId;

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
        TbAnnouncement other = (TbAnnouncement) that;
        return (this.getAnnouncementId() == null ? other.getAnnouncementId() == null : this.getAnnouncementId().equals(other.getAnnouncementId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getAnnouncementType() == null ? other.getAnnouncementType() == null : this.getAnnouncementType().equals(other.getAnnouncementType()))
            && (this.getTargetType() == null ? other.getTargetType() == null : this.getTargetType().equals(other.getTargetType()))
            && (this.getTargetIds() == null ? other.getTargetIds() == null : this.getTargetIds().equals(other.getTargetIds()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getIsTop() == null ? other.getIsTop() == null : this.getIsTop().equals(other.getIsTop()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAnnouncementId() == null) ? 0 : getAnnouncementId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getAnnouncementType() == null) ? 0 : getAnnouncementType().hashCode());
        result = prime * result + ((getTargetType() == null) ? 0 : getTargetType().hashCode());
        result = prime * result + ((getTargetIds() == null) ? 0 : getTargetIds().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getIsTop() == null) ? 0 : getIsTop().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreatorId() == null) ? 0 : getCreatorId().hashCode());
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
        sb.append(", announcementId=").append(announcementId);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", announcementType=").append(announcementType);
        sb.append(", targetType=").append(targetType);
        sb.append(", targetIds=").append(targetIds);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", isTop=").append(isTop);
        sb.append(", status=").append(status);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}