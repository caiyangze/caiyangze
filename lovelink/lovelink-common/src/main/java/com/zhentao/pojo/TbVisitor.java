package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_visitor
 */
@TableName(value ="tb_visitor")
@Data
public class TbVisitor {
    /**
     * 访问ID
     */
    @TableId(type = IdType.AUTO)
    private Long visitId;

    /**
     * 访客ID
     */
    private Long visitorId;

    /**
     * 被访问用户ID
     */
    private Long visitedUserId;

    /**
     * 访问时间
     */
    private Date visitTime;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

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
        TbVisitor other = (TbVisitor) that;
        return (this.getVisitId() == null ? other.getVisitId() == null : this.getVisitId().equals(other.getVisitId()))
            && (this.getVisitorId() == null ? other.getVisitorId() == null : this.getVisitorId().equals(other.getVisitorId()))
            && (this.getVisitedUserId() == null ? other.getVisitedUserId() == null : this.getVisitedUserId().equals(other.getVisitedUserId()))
            && (this.getVisitTime() == null ? other.getVisitTime() == null : this.getVisitTime().equals(other.getVisitTime()))
            && (this.getIsRead() == null ? other.getIsRead() == null : this.getIsRead().equals(other.getIsRead()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getVisitId() == null) ? 0 : getVisitId().hashCode());
        result = prime * result + ((getVisitorId() == null) ? 0 : getVisitorId().hashCode());
        result = prime * result + ((getVisitedUserId() == null) ? 0 : getVisitedUserId().hashCode());
        result = prime * result + ((getVisitTime() == null) ? 0 : getVisitTime().hashCode());
        result = prime * result + ((getIsRead() == null) ? 0 : getIsRead().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", visitId=").append(visitId);
        sb.append(", visitorId=").append(visitorId);
        sb.append(", visitedUserId=").append(visitedUserId);
        sb.append(", visitTime=").append(visitTime);
        sb.append(", isRead=").append(isRead);
        sb.append("]");
        return sb.toString();
    }
}