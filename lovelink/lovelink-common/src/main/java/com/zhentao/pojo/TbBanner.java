package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_banner
 */
@TableName(value ="tb_banner")
@Data
public class TbBanner {
    /**
     * 轮播图ID
     */
    @TableId(type = IdType.AUTO)
    private Long bannerId;

    /**
     * 轮播图标题
     */
    private String bannerTitle;

    /**
     * 轮播图片URL
     */
    private String bannerImage;

    /**
     * 跳转链接
     */
    private String bannerUrl;

    /**
     * 跳转类型：1-内部页面，2-外部链接，3-活动页面
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 展示位置：1-首页，2-相亲广场，3-游戏大厅
     */
    private Integer position;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

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
        TbBanner other = (TbBanner) that;
        return (this.getBannerId() == null ? other.getBannerId() == null : this.getBannerId().equals(other.getBannerId()))
            && (this.getBannerTitle() == null ? other.getBannerTitle() == null : this.getBannerTitle().equals(other.getBannerTitle()))
            && (this.getBannerImage() == null ? other.getBannerImage() == null : this.getBannerImage().equals(other.getBannerImage()))
            && (this.getBannerUrl() == null ? other.getBannerUrl() == null : this.getBannerUrl().equals(other.getBannerUrl()))
            && (this.getTargetType() == null ? other.getTargetType() == null : this.getTargetType().equals(other.getTargetType()))
            && (this.getTargetId() == null ? other.getTargetId() == null : this.getTargetId().equals(other.getTargetId()))
            && (this.getPosition() == null ? other.getPosition() == null : this.getPosition().equals(other.getPosition()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getBannerId() == null) ? 0 : getBannerId().hashCode());
        result = prime * result + ((getBannerTitle() == null) ? 0 : getBannerTitle().hashCode());
        result = prime * result + ((getBannerImage() == null) ? 0 : getBannerImage().hashCode());
        result = prime * result + ((getBannerUrl() == null) ? 0 : getBannerUrl().hashCode());
        result = prime * result + ((getTargetType() == null) ? 0 : getTargetType().hashCode());
        result = prime * result + ((getTargetId() == null) ? 0 : getTargetId().hashCode());
        result = prime * result + ((getPosition() == null) ? 0 : getPosition().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
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
        sb.append(", bannerId=").append(bannerId);
        sb.append(", bannerTitle=").append(bannerTitle);
        sb.append(", bannerImage=").append(bannerImage);
        sb.append(", bannerUrl=").append(bannerUrl);
        sb.append(", targetType=").append(targetType);
        sb.append(", targetId=").append(targetId);
        sb.append(", position=").append(position);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", status=").append(status);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}