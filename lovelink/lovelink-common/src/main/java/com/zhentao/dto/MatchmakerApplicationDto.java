package com.zhentao.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class MatchmakerApplicationDto {
    // 真实姓名
    private String realName;
    private String phone;
    // 身份证号
    private String idCardNo;
    // 身份证正面
    private String idCardFront;
    // 身份证反面
    private String idCardBack;
    // 服务区域
    private String serviceArea;
    // 简介
    private String introduction;
    // 经验
    private String experience;
    // 支付状态
    private Integer PaymentStatus;

    /**
     * 优惠券订单ID（使用优惠券时传递）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long voucherOrderId;

    /**
     * 优惠券抵扣金额（虚拟币数量）
     */
    private Integer voucherDiscountAmount;
}
