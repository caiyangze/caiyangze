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
 * @TableName tb_vip_order
 */
@TableName(value ="tb_vip_order")
@Data
public class TbVipOrder {
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
     * 订单编号
     */
    private String orderNo;

    /**
     * VIP类型：1-月度，2-季度，3-年度
     */
    private Integer vipType;

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
     * 支付方式：1-微信，2-支付宝，3-苹果支付，4-其他
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
     * VIP开始时间
     */
    private Date startTime;

    /**
     * VIP结束时间
     */
    private Date endTime;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消，3-已退款
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
        TbVipOrder other = (TbVipOrder) that;
        return (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getOrderNo() == null ? other.getOrderNo() == null : this.getOrderNo().equals(other.getOrderNo()))
            && (this.getVipType() == null ? other.getVipType() == null : this.getVipType().equals(other.getVipType()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getPayAmount() == null ? other.getPayAmount() == null : this.getPayAmount().equals(other.getPayAmount()))
            && (this.getDiscountAmount() == null ? other.getDiscountAmount() == null : this.getDiscountAmount().equals(other.getDiscountAmount()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getPayTime() == null ? other.getPayTime() == null : this.getPayTime().equals(other.getPayTime()))
            && (this.getTransactionId() == null ? other.getTransactionId() == null : this.getTransactionId().equals(other.getTransactionId()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
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
        result = prime * result + ((getOrderNo() == null) ? 0 : getOrderNo().hashCode());
        result = prime * result + ((getVipType() == null) ? 0 : getVipType().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getPayAmount() == null) ? 0 : getPayAmount().hashCode());
        result = prime * result + ((getDiscountAmount() == null) ? 0 : getDiscountAmount().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getPayTime() == null) ? 0 : getPayTime().hashCode());
        result = prime * result + ((getTransactionId() == null) ? 0 : getTransactionId().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
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
        sb.append(", orderNo=").append(orderNo);
        sb.append(", vipType=").append(vipType);
        sb.append(", amount=").append(amount);
        sb.append(", payAmount=").append(payAmount);
        sb.append(", discountAmount=").append(discountAmount);
        sb.append(", payType=").append(payType);
        sb.append(", payTime=").append(payTime);
        sb.append(", transactionId=").append(transactionId);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}