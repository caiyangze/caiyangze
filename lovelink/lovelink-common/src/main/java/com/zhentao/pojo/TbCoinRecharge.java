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
 * @TableName tb_coin_recharge
 */
@TableName(value ="tb_coin_recharge")
@Data
public class TbCoinRecharge {
    /**
     * 充值ID
     */
    @TableId(type = IdType.AUTO)
    private Long rechargeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 充值虚拟币数量
     */
    private Integer coinAmount;

    /**
     * 支付金额
     */
    private BigDecimal payAmount;

    /**
     * 赠送虚拟币数量
     */
    private Integer bonusAmount;

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
        TbCoinRecharge other = (TbCoinRecharge) that;
        return (this.getRechargeId() == null ? other.getRechargeId() == null : this.getRechargeId().equals(other.getRechargeId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getOrderNo() == null ? other.getOrderNo() == null : this.getOrderNo().equals(other.getOrderNo()))
            && (this.getCoinAmount() == null ? other.getCoinAmount() == null : this.getCoinAmount().equals(other.getCoinAmount()))
            && (this.getPayAmount() == null ? other.getPayAmount() == null : this.getPayAmount().equals(other.getPayAmount()))
            && (this.getBonusAmount() == null ? other.getBonusAmount() == null : this.getBonusAmount().equals(other.getBonusAmount()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getPayTime() == null ? other.getPayTime() == null : this.getPayTime().equals(other.getPayTime()))
            && (this.getTransactionId() == null ? other.getTransactionId() == null : this.getTransactionId().equals(other.getTransactionId()))
            && (this.getOrderStatus() == null ? other.getOrderStatus() == null : this.getOrderStatus().equals(other.getOrderStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRechargeId() == null) ? 0 : getRechargeId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getOrderNo() == null) ? 0 : getOrderNo().hashCode());
        result = prime * result + ((getCoinAmount() == null) ? 0 : getCoinAmount().hashCode());
        result = prime * result + ((getPayAmount() == null) ? 0 : getPayAmount().hashCode());
        result = prime * result + ((getBonusAmount() == null) ? 0 : getBonusAmount().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getPayTime() == null) ? 0 : getPayTime().hashCode());
        result = prime * result + ((getTransactionId() == null) ? 0 : getTransactionId().hashCode());
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
        sb.append(", rechargeId=").append(rechargeId);
        sb.append(", userId=").append(userId);
        sb.append(", orderNo=").append(orderNo);
        sb.append(", coinAmount=").append(coinAmount);
        sb.append(", payAmount=").append(payAmount);
        sb.append(", bonusAmount=").append(bonusAmount);
        sb.append(", payType=").append(payType);
        sb.append(", payTime=").append(payTime);
        sb.append(", transactionId=").append(transactionId);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}