package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_report
 */
@TableName(value ="tb_report")
@Data
public class TbReport {
    /**
     * 举报ID
     */
    @TableId(type = IdType.AUTO)
    private Long reportId;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 被举报人/内容ID
     */
    private Long reportedId;

    /**
     * 举报类型：1-用户，2-动态，3-评论，4-社群
     */
    private Integer reportType;

    /**
     * 举报原因类型：1-色情低俗，2-广告骚扰，3-政治敏感，4-诈骗，5-其他
     */
    private Integer reasonType;

    /**
     * 举报原因描述
     */
    private String reasonDesc;

    /**
     * 证据图片URL（JSON数组）
     */
    private String evidenceUrls;

    /**
     * 举报状态：0-待处理，1-已处理，2-已驳回
     */
    private Integer reportStatus;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理时间
     */
    private Date handleTime;

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
        TbReport other = (TbReport) that;
        return (this.getReportId() == null ? other.getReportId() == null : this.getReportId().equals(other.getReportId()))
            && (this.getReporterId() == null ? other.getReporterId() == null : this.getReporterId().equals(other.getReporterId()))
            && (this.getReportedId() == null ? other.getReportedId() == null : this.getReportedId().equals(other.getReportedId()))
            && (this.getReportType() == null ? other.getReportType() == null : this.getReportType().equals(other.getReportType()))
            && (this.getReasonType() == null ? other.getReasonType() == null : this.getReasonType().equals(other.getReasonType()))
            && (this.getReasonDesc() == null ? other.getReasonDesc() == null : this.getReasonDesc().equals(other.getReasonDesc()))
            && (this.getEvidenceUrls() == null ? other.getEvidenceUrls() == null : this.getEvidenceUrls().equals(other.getEvidenceUrls()))
            && (this.getReportStatus() == null ? other.getReportStatus() == null : this.getReportStatus().equals(other.getReportStatus()))
            && (this.getHandleResult() == null ? other.getHandleResult() == null : this.getHandleResult().equals(other.getHandleResult()))
            && (this.getHandlerId() == null ? other.getHandlerId() == null : this.getHandlerId().equals(other.getHandlerId()))
            && (this.getHandleTime() == null ? other.getHandleTime() == null : this.getHandleTime().equals(other.getHandleTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getReportId() == null) ? 0 : getReportId().hashCode());
        result = prime * result + ((getReporterId() == null) ? 0 : getReporterId().hashCode());
        result = prime * result + ((getReportedId() == null) ? 0 : getReportedId().hashCode());
        result = prime * result + ((getReportType() == null) ? 0 : getReportType().hashCode());
        result = prime * result + ((getReasonType() == null) ? 0 : getReasonType().hashCode());
        result = prime * result + ((getReasonDesc() == null) ? 0 : getReasonDesc().hashCode());
        result = prime * result + ((getEvidenceUrls() == null) ? 0 : getEvidenceUrls().hashCode());
        result = prime * result + ((getReportStatus() == null) ? 0 : getReportStatus().hashCode());
        result = prime * result + ((getHandleResult() == null) ? 0 : getHandleResult().hashCode());
        result = prime * result + ((getHandlerId() == null) ? 0 : getHandlerId().hashCode());
        result = prime * result + ((getHandleTime() == null) ? 0 : getHandleTime().hashCode());
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
        sb.append(", reportId=").append(reportId);
        sb.append(", reporterId=").append(reporterId);
        sb.append(", reportedId=").append(reportedId);
        sb.append(", reportType=").append(reportType);
        sb.append(", reasonType=").append(reasonType);
        sb.append(", reasonDesc=").append(reasonDesc);
        sb.append(", evidenceUrls=").append(evidenceUrls);
        sb.append(", reportStatus=").append(reportStatus);
        sb.append(", handleResult=").append(handleResult);
        sb.append(", handlerId=").append(handlerId);
        sb.append(", handleTime=").append(handleTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}