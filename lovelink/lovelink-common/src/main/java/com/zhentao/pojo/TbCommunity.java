package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_community
 */
@TableName(value ="tb_community")
@Data
public class TbCommunity {
    /**
     * 社群ID
     */
    @TableId(type = IdType.AUTO)
    private Long communityId;

    /**
     * 社群名称
     */
    private String communityName;

    /**
     * 社群描述
     */
    private String communityDesc;

    /**
     * 社群图标URL
     */
    private String communityIcon;

    /**
     * 社群封面URL
     */
    private String communityCover;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 社群类型：1-兴趣，2-地域，3-情感，4-其他
     */
    private Integer communityType;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 帖子数量
     */
    private Integer postCount;

    /**
     * 加入方式：1-自由加入，2-审核加入，3-邀请加入
     */
    private Integer joinType;

    /**
     * 状态：0-已解散，1-正常，2-已屏蔽
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
        TbCommunity other = (TbCommunity) that;
        return (this.getCommunityId() == null ? other.getCommunityId() == null : this.getCommunityId().equals(other.getCommunityId()))
            && (this.getCommunityName() == null ? other.getCommunityName() == null : this.getCommunityName().equals(other.getCommunityName()))
            && (this.getCommunityDesc() == null ? other.getCommunityDesc() == null : this.getCommunityDesc().equals(other.getCommunityDesc()))
            && (this.getCommunityIcon() == null ? other.getCommunityIcon() == null : this.getCommunityIcon().equals(other.getCommunityIcon()))
            && (this.getCommunityCover() == null ? other.getCommunityCover() == null : this.getCommunityCover().equals(other.getCommunityCover()))
            && (this.getCreatorId() == null ? other.getCreatorId() == null : this.getCreatorId().equals(other.getCreatorId()))
            && (this.getCommunityType() == null ? other.getCommunityType() == null : this.getCommunityType().equals(other.getCommunityType()))
            && (this.getMemberCount() == null ? other.getMemberCount() == null : this.getMemberCount().equals(other.getMemberCount()))
            && (this.getPostCount() == null ? other.getPostCount() == null : this.getPostCount().equals(other.getPostCount()))
            && (this.getJoinType() == null ? other.getJoinType() == null : this.getJoinType().equals(other.getJoinType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCommunityId() == null) ? 0 : getCommunityId().hashCode());
        result = prime * result + ((getCommunityName() == null) ? 0 : getCommunityName().hashCode());
        result = prime * result + ((getCommunityDesc() == null) ? 0 : getCommunityDesc().hashCode());
        result = prime * result + ((getCommunityIcon() == null) ? 0 : getCommunityIcon().hashCode());
        result = prime * result + ((getCommunityCover() == null) ? 0 : getCommunityCover().hashCode());
        result = prime * result + ((getCreatorId() == null) ? 0 : getCreatorId().hashCode());
        result = prime * result + ((getCommunityType() == null) ? 0 : getCommunityType().hashCode());
        result = prime * result + ((getMemberCount() == null) ? 0 : getMemberCount().hashCode());
        result = prime * result + ((getPostCount() == null) ? 0 : getPostCount().hashCode());
        result = prime * result + ((getJoinType() == null) ? 0 : getJoinType().hashCode());
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
        sb.append(", communityId=").append(communityId);
        sb.append(", communityName=").append(communityName);
        sb.append(", communityDesc=").append(communityDesc);
        sb.append(", communityIcon=").append(communityIcon);
        sb.append(", communityCover=").append(communityCover);
        sb.append(", creatorId=").append(creatorId);
        sb.append(", communityType=").append(communityType);
        sb.append(", memberCount=").append(memberCount);
        sb.append(", postCount=").append(postCount);
        sb.append(", joinType=").append(joinType);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}