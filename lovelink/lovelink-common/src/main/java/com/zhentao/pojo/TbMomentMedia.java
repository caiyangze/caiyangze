package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName tb_moment_media
 */
@TableName(value ="tb_moment_media")
@Data
public class TbMomentMedia {
    /**
     * 媒体ID
     */
    @TableId(type = IdType.AUTO)
    private Long mediaId;

    /**
     * 动态ID
     */
    private Long momentId;

    /**
     * 媒体类型：1-图片，2-视频，3-音频
     */
    private Integer mediaType;

    /**
     * 媒体URL
     */
    private String mediaUrl;

    /**
     * 封面URL（视频用）
     */
    private String coverUrl;

    /**
     * 宽度
     */
    private Integer width;

    /**
     * 高度
     */
    private Integer height;

    /**
     * 时长（秒）
     */
    private Integer duration;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

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
        TbMomentMedia other = (TbMomentMedia) that;
        return (this.getMediaId() == null ? other.getMediaId() == null : this.getMediaId().equals(other.getMediaId()))
            && (this.getMomentId() == null ? other.getMomentId() == null : this.getMomentId().equals(other.getMomentId()))
            && (this.getMediaType() == null ? other.getMediaType() == null : this.getMediaType().equals(other.getMediaType()))
            && (this.getMediaUrl() == null ? other.getMediaUrl() == null : this.getMediaUrl().equals(other.getMediaUrl()))
            && (this.getCoverUrl() == null ? other.getCoverUrl() == null : this.getCoverUrl().equals(other.getCoverUrl()))
            && (this.getWidth() == null ? other.getWidth() == null : this.getWidth().equals(other.getWidth()))
            && (this.getHeight() == null ? other.getHeight() == null : this.getHeight().equals(other.getHeight()))
            && (this.getDuration() == null ? other.getDuration() == null : this.getDuration().equals(other.getDuration()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMediaId() == null) ? 0 : getMediaId().hashCode());
        result = prime * result + ((getMomentId() == null) ? 0 : getMomentId().hashCode());
        result = prime * result + ((getMediaType() == null) ? 0 : getMediaType().hashCode());
        result = prime * result + ((getMediaUrl() == null) ? 0 : getMediaUrl().hashCode());
        result = prime * result + ((getCoverUrl() == null) ? 0 : getCoverUrl().hashCode());
        result = prime * result + ((getWidth() == null) ? 0 : getWidth().hashCode());
        result = prime * result + ((getHeight() == null) ? 0 : getHeight().hashCode());
        result = prime * result + ((getDuration() == null) ? 0 : getDuration().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", mediaId=").append(mediaId);
        sb.append(", momentId=").append(momentId);
        sb.append(", mediaType=").append(mediaType);
        sb.append(", mediaUrl=").append(mediaUrl);
        sb.append(", coverUrl=").append(coverUrl);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", duration=").append(duration);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append("]");
        return sb.toString();
    }
}