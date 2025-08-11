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
 * @TableName tb_withdraw_application
 */
@TableName(value ="tb_withdraw_application")
@Data
public class TbWithdrawApplication {
    /**
     * 提现ID
     */
    @TableId(type = IdType.AUTO)
    private Long withdrawId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 提现单号
     */
    private String withdrawNo;

    /**
     * 提现金额
     */
    private BigDecimal withdrawAmount;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 实际到账金额
     */
    private BigDecimal actualAmount;

    /**
     * 提现方式：1-微信，2-支付宝，3-银行卡
     */
    private Integer withdrawType;

    /**
     * 提现账号
     */
    private String withdrawAccount;

    /**
     * 账户名
     */
    private String accountName;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 提现状态：0-审核中，1-已通过，2-已拒绝，3-已打款，4-已取消
     */
    private Integer withdrawStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 打款时间
     */
    private Date paymentTime;

    /**
     * 打款凭证
     */
    private String paymentProof;

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
        TbWithdrawApplication other = (TbWithdrawApplication) that;
        return (this.getWithdrawId() == null ? other.getWithdrawId() == null : this.getWithdrawId().equals(other.getWithdrawId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getWithdrawNo() == null ? other.getWithdrawNo() == null : this.getWithdrawNo().equals(other.getWithdrawNo()))
            && (this.getWithdrawAmount() == null ? other.getWithdrawAmount() == null : this.getWithdrawAmount().equals(other.getWithdrawAmount()))
            && (this.getFeeAmount() == null ? other.getFeeAmount() == null : this.getFeeAmount().equals(other.getFeeAmount()))
            && (this.getActualAmount() == null ? other.getActualAmount() == null : this.getActualAmount().equals(other.getActualAmount()))
            && (this.getWithdrawType() == null ? other.getWithdrawType() == null : this.getWithdrawType().equals(other.getWithdrawType()))
            && (this.getWithdrawAccount() == null ? other.getWithdrawAccount() == null : this.getWithdrawAccount().equals(other.getWithdrawAccount()))
            && (this.getAccountName() == null ? other.getAccountName() == null : this.getAccountName().equals(other.getAccountName()))
            && (this.getBankName() == null ? other.getBankName() == null : this.getBankName().equals(other.getBankName()))
            && (this.getWithdrawStatus() == null ? other.getWithdrawStatus() == null : this.getWithdrawStatus().equals(other.getWithdrawStatus()))
            && (this.getRejectReason() == null ? other.getRejectReason() == null : this.getRejectReason().equals(other.getRejectReason()))
            && (this.getReviewerId() == null ? other.getReviewerId() == null : this.getReviewerId().equals(other.getReviewerId()))
            && (this.getReviewTime() == null ? other.getReviewTime() == null : this.getReviewTime().equals(other.getReviewTime()))
            && (this.getPaymentTime() == null ? other.getPaymentTime() == null : this.getPaymentTime().equals(other.getPaymentTime()))
            && (this.getPaymentProof() == null ? other.getPaymentProof() == null : this.getPaymentProof().equals(other.getPaymentProof()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getWithdrawId() == null) ? 0 : getWithdrawId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getWithdrawNo() == null) ? 0 : getWithdrawNo().hashCode());
        result = prime * result + ((getWithdrawAmount() == null) ? 0 : getWithdrawAmount().hashCode());
        result = prime * result + ((getFeeAmount() == null) ? 0 : getFeeAmount().hashCode());
        result = prime * result + ((getActualAmount() == null) ? 0 : getActualAmount().hashCode());
        result = prime * result + ((getWithdrawType() == null) ? 0 : getWithdrawType().hashCode());
        result = prime * result + ((getWithdrawAccount() == null) ? 0 : getWithdrawAccount().hashCode());
        result = prime * result + ((getAccountName() == null) ? 0 : getAccountName().hashCode());
        result = prime * result + ((getBankName() == null) ? 0 : getBankName().hashCode());
        result = prime * result + ((getWithdrawStatus() == null) ? 0 : getWithdrawStatus().hashCode());
        result = prime * result + ((getRejectReason() == null) ? 0 : getRejectReason().hashCode());
        result = prime * result + ((getReviewerId() == null) ? 0 : getReviewerId().hashCode());
        result = prime * result + ((getReviewTime() == null) ? 0 : getReviewTime().hashCode());
        result = prime * result + ((getPaymentTime() == null) ? 0 : getPaymentTime().hashCode());
        result = prime * result + ((getPaymentProof() == null) ? 0 : getPaymentProof().hashCode());
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
        sb.append(", withdrawId=").append(withdrawId);
        sb.append(", userId=").append(userId);
        sb.append(", withdrawNo=").append(withdrawNo);
        sb.append(", withdrawAmount=").append(withdrawAmount);
        sb.append(", feeAmount=").append(feeAmount);
        sb.append(", actualAmount=").append(actualAmount);
        sb.append(", withdrawType=").append(withdrawType);
        sb.append(", withdrawAccount=").append(withdrawAccount);
        sb.append(", accountName=").append(accountName);
        sb.append(", bankName=").append(bankName);
        sb.append(", withdrawStatus=").append(withdrawStatus);
        sb.append(", rejectReason=").append(rejectReason);
        sb.append(", reviewerId=").append(reviewerId);
        sb.append(", reviewTime=").append(reviewTime);
        sb.append(", paymentTime=").append(paymentTime);
        sb.append(", paymentProof=").append(paymentProof);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}