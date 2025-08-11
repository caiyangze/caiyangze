package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_follow
 */
@TableName(value ="tb_follow")
@Data
public class TbFollow {
    //0未关注1已关注2互相关注
    @TableField(exist = false)
    private Integer isFollow;
    @TableField(exist = false)
    private TbUser user;
    /**
     * 关注ID
     */
    @TableId(type = IdType.AUTO)
    private Long followId;

    /**
     * 关注人ID
     */
    private Long userId;

    /**
     * 被关注人ID
     */
    private Long followedUserId;

    /**
     * 关注状态：0-取消关注，1-已关注
     */
    private Integer followStatus;

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
        TbFollow other = (TbFollow) that;
        return (this.getFollowId() == null ? other.getFollowId() == null : this.getFollowId().equals(other.getFollowId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getFollowedUserId() == null ? other.getFollowedUserId() == null : this.getFollowedUserId().equals(other.getFollowedUserId()))
            && (this.getFollowStatus() == null ? other.getFollowStatus() == null : this.getFollowStatus().equals(other.getFollowStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getFollowId() == null) ? 0 : getFollowId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getFollowedUserId() == null) ? 0 : getFollowedUserId().hashCode());
        result = prime * result + ((getFollowStatus() == null) ? 0 : getFollowStatus().hashCode());
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
        sb.append(", followId=").append(followId);
        sb.append(", userId=").append(userId);
        sb.append(", followedUserId=").append(followedUserId);
        sb.append(", followStatus=").append(followStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}