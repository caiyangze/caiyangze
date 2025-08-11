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
 * @TableName tb_matchmaker_order
 */
@TableName(value ="tb_matchmaker_order")
@Data
public class TbMatchmakerOrder {
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 红娘ID
     */
    private Long matchmakerId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 服务类型：1-单次牵线，2-包月服务，3-高端定制
     */
    private Integer serviceType;

    /**
     * 服务描述
     */
    private String serviceDesc;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 支付方式：1-微信，2-支付宝，3-虚拟币，4-其他
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 支付平台交易号
     */
    private String transactionId;

    /**
     * 服务开始时间
     */
    private Date serviceStartTime;

    /**
     * 服务结束时间
     */
    private Date serviceEndTime;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成
     */
    private Integer orderStatus;

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
        TbMatchmakerOrder other = (TbMatchmakerOrder) that;
        return (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getMatchmakerId() == null ? other.getMatchmakerId() == null : this.getMatchmakerId().equals(other.getMatchmakerId()))
            && (this.getOrderNo() == null ? other.getOrderNo() == null : this.getOrderNo().equals(other.getOrderNo()))
            && (this.getServiceType() == null ? other.getServiceType() == null : this.getServiceType().equals(other.getServiceType()))
            && (this.getServiceDesc() == null ? other.getServiceDesc() == null : this.getServiceDesc().equals(other.getServiceDesc()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getPayAmount() == null ? other.getPayAmount() == null : this.getPayAmount().equals(other.getPayAmount()))
            && (this.getDiscountAmount() == null ? other.getDiscountAmount() == null : this.getDiscountAmount().equals(other.getDiscountAmount()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getPayTime() == null ? other.getPayTime() == null : this.getPayTime().equals(other.getPayTime()))
            && (this.getTransactionId() == null ? other.getTransactionId() == null : this.getTransactionId().equals(other.getTransactionId()))
            && (this.getServiceStartTime() == null ? other.getServiceStartTime() == null : this.getServiceStartTime().equals(other.getServiceStartTime()))
            && (this.getServiceEndTime() == null ? other.getServiceEndTime() == null : this.getServiceEndTime().equals(other.getServiceEndTime()))
            && (this.getOrderStatus() == null ? other.getOrderStatus() == null : this.getOrderStatus().equals(other.getOrderStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getMatchmakerId() == null) ? 0 : getMatchmakerId().hashCode());
        result = prime * result + ((getOrderNo() == null) ? 0 : getOrderNo().hashCode());
        result = prime * result + ((getServiceType() == null) ? 0 : getServiceType().hashCode());
        result = prime * result + ((getServiceDesc() == null) ? 0 : getServiceDesc().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getPayAmount() == null) ? 0 : getPayAmount().hashCode());
        result = prime * result + ((getDiscountAmount() == null) ? 0 : getDiscountAmount().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getPayTime() == null) ? 0 : getPayTime().hashCode());
        result = prime * result + ((getTransactionId() == null) ? 0 : getTransactionId().hashCode());
        result = prime * result + ((getServiceStartTime() == null) ? 0 : getServiceStartTime().hashCode());
        result = prime * result + ((getServiceEndTime() == null) ? 0 : getServiceEndTime().hashCode());
        result = prime * result + ((getOrderStatus() == null) ? 0 : getOrderStatus().hashCode());
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
        sb.append(", orderId=").append(orderId);
        sb.append(", userId=").append(userId);
        sb.append(", matchmakerId=").append(matchmakerId);
        sb.append(", orderNo=").append(orderNo);
        sb.append(", serviceType=").append(serviceType);
        sb.append(", serviceDesc=").append(serviceDesc);
        sb.append(", amount=").append(amount);
        sb.append(", payAmount=").append(payAmount);
        sb.append(", discountAmount=").append(discountAmount);
        sb.append(", payType=").append(payType);
        sb.append(", payTime=").append(payTime);
        sb.append(", transactionId=").append(transactionId);
        sb.append(", serviceStartTime=").append(serviceStartTime);
        sb.append(", serviceEndTime=").append(serviceEndTime);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}