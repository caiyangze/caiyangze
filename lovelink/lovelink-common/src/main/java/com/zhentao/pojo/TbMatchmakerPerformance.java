package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_matchmaker_performance
 */
@TableName(value ="tb_matchmaker_performance")
@Data
public class TbMatchmakerPerformance {
    /**
     * 业绩ID
     */
    @TableId(type = IdType.AUTO)
    private Long performanceId;

    /**
     * 红娘ID
     */
    private Long matchmakerId;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 接单数量
     */
    private Integer requestCount;

    /**
     * 安排约会数量
     */
    private Integer arrangementCount;

    /**
     * 成功牵线数量
     */
    private Integer successCount;

    /**
     * 满意度平均分
     */
    private BigDecimal satisfactionAvg;

    /**
     * 佣金金额
     */
    private BigDecimal commissionAmount;

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
        TbMatchmakerPerformance other = (TbMatchmakerPerformance) that;
        return (this.getPerformanceId() == null ? other.getPerformanceId() == null : this.getPerformanceId().equals(other.getPerformanceId()))
            && (this.getMatchmakerId() == null ? other.getMatchmakerId() == null : this.getMatchmakerId().equals(other.getMatchmakerId()))
            && (this.getYear() == null ? other.getYear() == null : this.getYear().equals(other.getYear()))
            && (this.getMonth() == null ? other.getMonth() == null : this.getMonth().equals(other.getMonth()))
            && (this.getRequestCount() == null ? other.getRequestCount() == null : this.getRequestCount().equals(other.getRequestCount()))
            && (this.getArrangementCount() == null ? other.getArrangementCount() == null : this.getArrangementCount().equals(other.getArrangementCount()))
            && (this.getSuccessCount() == null ? other.getSuccessCount() == null : this.getSuccessCount().equals(other.getSuccessCount()))
            && (this.getSatisfactionAvg() == null ? other.getSatisfactionAvg() == null : this.getSatisfactionAvg().equals(other.getSatisfactionAvg()))
            && (this.getCommissionAmount() == null ? other.getCommissionAmount() == null : this.getCommissionAmount().equals(other.getCommissionAmount()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPerformanceId() == null) ? 0 : getPerformanceId().hashCode());
        result = prime * result + ((getMatchmakerId() == null) ? 0 : getMatchmakerId().hashCode());
        result = prime * result + ((getYear() == null) ? 0 : getYear().hashCode());
        result = prime * result + ((getMonth() == null) ? 0 : getMonth().hashCode());
        result = prime * result + ((getRequestCount() == null) ? 0 : getRequestCount().hashCode());
        result = prime * result + ((getArrangementCount() == null) ? 0 : getArrangementCount().hashCode());
        result = prime * result + ((getSuccessCount() == null) ? 0 : getSuccessCount().hashCode());
        result = prime * result + ((getSatisfactionAvg() == null) ? 0 : getSatisfactionAvg().hashCode());
        result = prime * result + ((getCommissionAmount() == null) ? 0 : getCommissionAmount().hashCode());
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
        sb.append(", performanceId=").append(performanceId);
        sb.append(", matchmakerId=").append(matchmakerId);
        sb.append(", year=").append(year);
        sb.append(", month=").append(month);
        sb.append(", requestCount=").append(requestCount);
        sb.append(", arrangementCount=").append(arrangementCount);
        sb.append(", successCount=").append(successCount);
        sb.append(", satisfactionAvg=").append(satisfactionAvg);
        sb.append(", commissionAmount=").append(commissionAmount);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}