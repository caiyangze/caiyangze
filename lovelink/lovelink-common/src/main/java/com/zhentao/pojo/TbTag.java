package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_tag
 */
@TableName(value ="tb_tag")
@Data
public class TbTag {
    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签类型：1-兴趣爱好，2-性格特点，3-生活方式，4-其他
     */
    private Integer tagType;

    /**
     * 标签状态：0-禁用，1-启用
     */
    private Integer tagStatus;

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
        TbTag other = (TbTag) that;
        return (this.getTagId() == null ? other.getTagId() == null : this.getTagId().equals(other.getTagId()))
            && (this.getTagName() == null ? other.getTagName() == null : this.getTagName().equals(other.getTagName()))
            && (this.getTagType() == null ? other.getTagType() == null : this.getTagType().equals(other.getTagType()))
            && (this.getTagStatus() == null ? other.getTagStatus() == null : this.getTagStatus().equals(other.getTagStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTagId() == null) ? 0 : getTagId().hashCode());
        result = prime * result + ((getTagName() == null) ? 0 : getTagName().hashCode());
        result = prime * result + ((getTagType() == null) ? 0 : getTagType().hashCode());
        result = prime * result + ((getTagStatus() == null) ? 0 : getTagStatus().hashCode());
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
        sb.append(", tagId=").append(tagId);
        sb.append(", tagName=").append(tagName);
        sb.append(", tagType=").append(tagType);
        sb.append(", tagStatus=").append(tagStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}