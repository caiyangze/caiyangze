package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_topic
 */
@TableName(value ="tb_topic")
@Data
public class TbTopic {
    /**
     * 话题ID
     */
    @TableId(type = IdType.AUTO)
    private Long topicId;

    /**
     * 话题名称
     */
    private String topicName;

    /**
     * 话题描述
     */
    private String topicDesc;

    /**
     * 话题封面URL
     */
    private String topicCover;

    /**
     * 话题类型：1-情感，2-兴趣，3-生活，4-其他
     */
    private Integer topicType;

    /**
     * 动态数
     */
    private Integer momentCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 是否热门：0-否，1-是
     */
    private Integer isHot;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0-已删除，1-正常，2-已屏蔽
     */
    private Integer status;

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
        TbTopic other = (TbTopic) that;
        return (this.getTopicId() == null ? other.getTopicId() == null : this.getTopicId().equals(other.getTopicId()))
            && (this.getTopicName() == null ? other.getTopicName() == null : this.getTopicName().equals(other.getTopicName()))
            && (this.getTopicDesc() == null ? other.getTopicDesc() == null : this.getTopicDesc().equals(other.getTopicDesc()))
            && (this.getTopicCover() == null ? other.getTopicCover() == null : this.getTopicCover().equals(other.getTopicCover()))
            && (this.getTopicType() == null ? other.getTopicType() == null : this.getTopicType().equals(other.getTopicType()))
            && (this.getMomentCount() == null ? other.getMomentCount() == null : this.getMomentCount().equals(other.getMomentCount()))
            && (this.getViewCount() == null ? other.getViewCount() == null : this.getViewCount().equals(other.getViewCount()))
            && (this.getIsHot() == null ? other.getIsHot() == null : this.getIsHot().equals(other.getIsHot()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTopicId() == null) ? 0 : getTopicId().hashCode());
        result = prime * result + ((getTopicName() == null) ? 0 : getTopicName().hashCode());
        result = prime * result + ((getTopicDesc() == null) ? 0 : getTopicDesc().hashCode());
        result = prime * result + ((getTopicCover() == null) ? 0 : getTopicCover().hashCode());
        result = prime * result + ((getTopicType() == null) ? 0 : getTopicType().hashCode());
        result = prime * result + ((getMomentCount() == null) ? 0 : getMomentCount().hashCode());
        result = prime * result + ((getViewCount() == null) ? 0 : getViewCount().hashCode());
        result = prime * result + ((getIsHot() == null) ? 0 : getIsHot().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
        sb.append(", topicId=").append(topicId);
        sb.append(", topicName=").append(topicName);
        sb.append(", topicDesc=").append(topicDesc);
        sb.append(", topicCover=").append(topicCover);
        sb.append(", topicType=").append(topicType);
        sb.append(", momentCount=").append(momentCount);
        sb.append(", viewCount=").append(viewCount);
        sb.append(", isHot=").append(isHot);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}