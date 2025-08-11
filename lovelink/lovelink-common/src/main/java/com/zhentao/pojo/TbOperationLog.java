package com.zhentao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName tb_operation_log
 */
@TableName(value ="tb_operation_log")
@Data
public class TbOperationLog {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人类型：1-管理员，2-用户，3-系统
     */
    private Integer operatorType;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 执行时长(毫秒)
     */
    private Integer executionTime;

    /**
     * 创建时间
     */
    private Date createdAt;

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
        TbOperationLog other = (TbOperationLog) that;
        return (this.getLogId() == null ? other.getLogId() == null : this.getLogId().equals(other.getLogId()))
            && (this.getOperatorId() == null ? other.getOperatorId() == null : this.getOperatorId().equals(other.getOperatorId()))
            && (this.getOperatorType() == null ? other.getOperatorType() == null : this.getOperatorType().equals(other.getOperatorType()))
            && (this.getOperatorName() == null ? other.getOperatorName() == null : this.getOperatorName().equals(other.getOperatorName()))
            && (this.getModule() == null ? other.getModule() == null : this.getModule().equals(other.getModule()))
            && (this.getOperation() == null ? other.getOperation() == null : this.getOperation().equals(other.getOperation()))
            && (this.getMethod() == null ? other.getMethod() == null : this.getMethod().equals(other.getMethod()))
            && (this.getParams() == null ? other.getParams() == null : this.getParams().equals(other.getParams()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getUserAgent() == null ? other.getUserAgent() == null : this.getUserAgent().equals(other.getUserAgent()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getErrorMsg() == null ? other.getErrorMsg() == null : this.getErrorMsg().equals(other.getErrorMsg()))
            && (this.getExecutionTime() == null ? other.getExecutionTime() == null : this.getExecutionTime().equals(other.getExecutionTime()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLogId() == null) ? 0 : getLogId().hashCode());
        result = prime * result + ((getOperatorId() == null) ? 0 : getOperatorId().hashCode());
        result = prime * result + ((getOperatorType() == null) ? 0 : getOperatorType().hashCode());
        result = prime * result + ((getOperatorName() == null) ? 0 : getOperatorName().hashCode());
        result = prime * result + ((getModule() == null) ? 0 : getModule().hashCode());
        result = prime * result + ((getOperation() == null) ? 0 : getOperation().hashCode());
        result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
        result = prime * result + ((getParams() == null) ? 0 : getParams().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getUserAgent() == null) ? 0 : getUserAgent().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
        result = prime * result + ((getExecutionTime() == null) ? 0 : getExecutionTime().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logId=").append(logId);
        sb.append(", operatorId=").append(operatorId);
        sb.append(", operatorType=").append(operatorType);
        sb.append(", operatorName=").append(operatorName);
        sb.append(", module=").append(module);
        sb.append(", operation=").append(operation);
        sb.append(", method=").append(method);
        sb.append(", params=").append(params);
        sb.append(", ip=").append(ip);
        sb.append(", userAgent=").append(userAgent);
        sb.append(", status=").append(status);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}