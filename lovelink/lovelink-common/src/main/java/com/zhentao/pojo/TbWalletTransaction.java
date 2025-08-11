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
 * @TableName tb_wallet_transaction
 */
@TableName(value ="tb_wallet_transaction")
@Data
public class TbWalletTransaction {
    /**
     * 交易ID
     */
    @TableId(type = IdType.AUTO)
    private Long transactionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易类型：1-充值，2-消费，3-收入，4-提现，5-退款
     */
    private Integer transactionType;

    /**
     * 交易金额
     */
    private BigDecimal transactionAmount;

    /**
     * 虚拟币数量
     */
    private Integer coinAmount;

    /**
     * 交易后余额
     */
    private BigDecimal balance;

    /**
     * 交易后虚拟币余额
     */
    private Integer coinBalance;

    /**
     * 交易描述
     */
    private String transactionDesc;

    /**
     * 关联ID（订单号/提现单号等）
     */
    private String relatedId;

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
        TbWalletTransaction other = (TbWalletTransaction) that;
        return (this.getTransactionId() == null ? other.getTransactionId() == null : this.getTransactionId().equals(other.getTransactionId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTransactionType() == null ? other.getTransactionType() == null : this.getTransactionType().equals(other.getTransactionType()))
            && (this.getTransactionAmount() == null ? other.getTransactionAmount() == null : this.getTransactionAmount().equals(other.getTransactionAmount()))
            && (this.getCoinAmount() == null ? other.getCoinAmount() == null : this.getCoinAmount().equals(other.getCoinAmount()))
            && (this.getBalance() == null ? other.getBalance() == null : this.getBalance().equals(other.getBalance()))
            && (this.getCoinBalance() == null ? other.getCoinBalance() == null : this.getCoinBalance().equals(other.getCoinBalance()))
            && (this.getTransactionDesc() == null ? other.getTransactionDesc() == null : this.getTransactionDesc().equals(other.getTransactionDesc()))
            && (this.getRelatedId() == null ? other.getRelatedId() == null : this.getRelatedId().equals(other.getRelatedId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTransactionId() == null) ? 0 : getTransactionId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTransactionType() == null) ? 0 : getTransactionType().hashCode());
        result = prime * result + ((getTransactionAmount() == null) ? 0 : getTransactionAmount().hashCode());
        result = prime * result + ((getCoinAmount() == null) ? 0 : getCoinAmount().hashCode());
        result = prime * result + ((getBalance() == null) ? 0 : getBalance().hashCode());
        result = prime * result + ((getCoinBalance() == null) ? 0 : getCoinBalance().hashCode());
        result = prime * result + ((getTransactionDesc() == null) ? 0 : getTransactionDesc().hashCode());
        result = prime * result + ((getRelatedId() == null) ? 0 : getRelatedId().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", transactionId=").append(transactionId);
        sb.append(", userId=").append(userId);
        sb.append(", transactionType=").append(transactionType);
        sb.append(", transactionAmount=").append(transactionAmount);
        sb.append(", coinAmount=").append(coinAmount);
        sb.append(", balance=").append(balance);
        sb.append(", coinBalance=").append(coinBalance);
        sb.append(", transactionDesc=").append(transactionDesc);
        sb.append(", relatedId=").append(relatedId);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}