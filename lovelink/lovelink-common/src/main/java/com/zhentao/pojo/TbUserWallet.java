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
 * @TableName tb_user_wallet
 */
@TableName(value ="tb_user_wallet")
@Data
public class TbUserWallet {
    /**
     * 钱包ID
     */
    @TableId(type = IdType.AUTO)
    private Long walletId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 虚拟币余额
     */
    private Integer coinBalance;

    /**
     * 累计充值虚拟币
     */
    private Integer totalRecharge;

    /**
     * 累计消费虚拟币
     */
    private Integer totalConsume;

    /**
     * 现金余额
     */
    private BigDecimal cashBalance;

    /**
     * 累计收入
     */
    private BigDecimal totalIncome;

    /**
     * 累计提现
     */
    private BigDecimal totalWithdraw;

    /**
     * 钱包状态：0-冻结，1-正常
     */
    private Integer walletStatus;

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
        TbUserWallet other = (TbUserWallet) that;
        return (this.getWalletId() == null ? other.getWalletId() == null : this.getWalletId().equals(other.getWalletId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCoinBalance() == null ? other.getCoinBalance() == null : this.getCoinBalance().equals(other.getCoinBalance()))
            && (this.getTotalRecharge() == null ? other.getTotalRecharge() == null : this.getTotalRecharge().equals(other.getTotalRecharge()))
            && (this.getTotalConsume() == null ? other.getTotalConsume() == null : this.getTotalConsume().equals(other.getTotalConsume()))
            && (this.getCashBalance() == null ? other.getCashBalance() == null : this.getCashBalance().equals(other.getCashBalance()))
            && (this.getTotalIncome() == null ? other.getTotalIncome() == null : this.getTotalIncome().equals(other.getTotalIncome()))
            && (this.getTotalWithdraw() == null ? other.getTotalWithdraw() == null : this.getTotalWithdraw().equals(other.getTotalWithdraw()))
            && (this.getWalletStatus() == null ? other.getWalletStatus() == null : this.getWalletStatus().equals(other.getWalletStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getWalletId() == null) ? 0 : getWalletId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCoinBalance() == null) ? 0 : getCoinBalance().hashCode());
        result = prime * result + ((getTotalRecharge() == null) ? 0 : getTotalRecharge().hashCode());
        result = prime * result + ((getTotalConsume() == null) ? 0 : getTotalConsume().hashCode());
        result = prime * result + ((getCashBalance() == null) ? 0 : getCashBalance().hashCode());
        result = prime * result + ((getTotalIncome() == null) ? 0 : getTotalIncome().hashCode());
        result = prime * result + ((getTotalWithdraw() == null) ? 0 : getTotalWithdraw().hashCode());
        result = prime * result + ((getWalletStatus() == null) ? 0 : getWalletStatus().hashCode());
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
        sb.append(", walletId=").append(walletId);
        sb.append(", userId=").append(userId);
        sb.append(", coinBalance=").append(coinBalance);
        sb.append(", totalRecharge=").append(totalRecharge);
        sb.append(", totalConsume=").append(totalConsume);
        sb.append(", cashBalance=").append(cashBalance);
        sb.append(", totalIncome=").append(totalIncome);
        sb.append(", totalWithdraw=").append(totalWithdraw);
        sb.append(", walletStatus=").append(walletStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}