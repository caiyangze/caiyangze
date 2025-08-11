package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_community_member
 */
@TableName(value ="tb_community_member")
@Data
public class TbCommunityMember {
    /**
     * 成员ID
     */
    @TableId(type = IdType.AUTO)
    private Long memberId;

    /**
     * 社群ID
     */
    private Long communityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员角色：0-普通成员，1-管理员，2-群主
     */
    private Integer memberRole;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 状态：0-已退出，1-正常，2-已禁言
     */
    private Integer status;

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
        TbCommunityMember other = (TbCommunityMember) that;
        return (this.getMemberId() == null ? other.getMemberId() == null : this.getMemberId().equals(other.getMemberId()))
            && (this.getCommunityId() == null ? other.getCommunityId() == null : this.getCommunityId().equals(other.getCommunityId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getMemberRole() == null ? other.getMemberRole() == null : this.getMemberRole().equals(other.getMemberRole()))
            && (this.getJoinTime() == null ? other.getJoinTime() == null : this.getJoinTime().equals(other.getJoinTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMemberId() == null) ? 0 : getMemberId().hashCode());
        result = prime * result + ((getCommunityId() == null) ? 0 : getCommunityId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getMemberRole() == null) ? 0 : getMemberRole().hashCode());
        result = prime * result + ((getJoinTime() == null) ? 0 : getJoinTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", memberId=").append(memberId);
        sb.append(", communityId=").append(communityId);
        sb.append(", userId=").append(userId);
        sb.append(", memberRole=").append(memberRole);
        sb.append(", joinTime=").append(joinTime);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }
}