package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_user_photo
 */
@TableName(value ="tb_user_photo")
@Data
public class TbUserPhoto {
    /**
     * 相片ID
     */
    @TableId(type = IdType.AUTO)
    private Long photoId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 照片描述
     */
    private String photoDesc;

    /**
     * 是否头像：0-否，1-是
     */
    private Integer isAvatar;

    /**
     * 是否公开：0-私密，1-公开
     */
    private Integer isPublic;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

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
        TbUserPhoto other = (TbUserPhoto) that;
        return (this.getPhotoId() == null ? other.getPhotoId() == null : this.getPhotoId().equals(other.getPhotoId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getPhotoUrl() == null ? other.getPhotoUrl() == null : this.getPhotoUrl().equals(other.getPhotoUrl()))
            && (this.getPhotoDesc() == null ? other.getPhotoDesc() == null : this.getPhotoDesc().equals(other.getPhotoDesc()))
            && (this.getIsAvatar() == null ? other.getIsAvatar() == null : this.getIsAvatar().equals(other.getIsAvatar()))
            && (this.getIsPublic() == null ? other.getIsPublic() == null : this.getIsPublic().equals(other.getIsPublic()))
            && (this.getSortOrder() == null ? other.getSortOrder() == null : this.getSortOrder().equals(other.getSortOrder()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPhotoId() == null) ? 0 : getPhotoId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getPhotoUrl() == null) ? 0 : getPhotoUrl().hashCode());
        result = prime * result + ((getPhotoDesc() == null) ? 0 : getPhotoDesc().hashCode());
        result = prime * result + ((getIsAvatar() == null) ? 0 : getIsAvatar().hashCode());
        result = prime * result + ((getIsPublic() == null) ? 0 : getIsPublic().hashCode());
        result = prime * result + ((getSortOrder() == null) ? 0 : getSortOrder().hashCode());
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
        sb.append(", photoId=").append(photoId);
        sb.append(", userId=").append(userId);
        sb.append(", photoUrl=").append(photoUrl);
        sb.append(", photoDesc=").append(photoDesc);
        sb.append(", isAvatar=").append(isAvatar);
        sb.append(", isPublic=").append(isPublic);
        sb.append(", sortOrder=").append(sortOrder);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}