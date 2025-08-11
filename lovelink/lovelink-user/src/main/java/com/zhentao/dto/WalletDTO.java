package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 钱包数据传输对象
 *
 * @author zhentao
 */
@Data
public class WalletDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 虚拟币数量（用于充值、消费）
     */
    @Min(value = 1, message = "虚拟币数量必须大于0")
    private Integer coinAmount;

    /**
     * 交易金额（用于现金相关操作）
     */
    @DecimalMin(value = "0.01", message = "交易金额必须大于0.01")
    private BigDecimal transactionAmount;

    /**
     * 交易描述
     */
    private String transactionDesc;

    /**
     * 关联ID（订单号、提现单号等）
     */
    private String relatedId;

    /**
     * 支付方式（充值时使用）
     * 1-支付宝，2-微信，3-银行卡
     */
    private Integer paymentMethod;

    /**
     * 提现账户信息（提现时使用）
     */
    private String withdrawAccount;

    /**
     * 提现账户类型（提现时使用）
     * 1-支付宝，2-微信，3-银行卡
     */
    private Integer withdrawAccountType;

    /**
     * 提现账户姓名（提现时使用）
     */
    private String withdrawAccountName;

    /**
     * 交易类型
     * 1-充值，2-消费，3-收入，4-提现，5-退款
     */
    private Integer transactionType;

    /**
     * 页码（查询时使用）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小（查询时使用）
     */
    private Integer pageSize = 20;

    /**
     * 开始时间（查询时使用）
     */
    private String startTime;

    /**
     * 结束时间（查询时使用）
     */
    private String endTime;

    /**
     * 优惠券订单ID（消费时使用优惠券）
     */
    private String voucherOrderId;

    /**
     * 优惠券抵扣金额（虚拟币数量）
     */
    private Integer voucherDiscountAmount;
}
