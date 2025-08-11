package com.zhentao.dto;

import lombok.Data;

/**
 * 心动速配请求DTO
 *
 * @author zhentao
 */
@Data
public class HeartMatchDTO {

    /**
     * 要匹配的性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 是否跳过钱包扣减（前端已处理优惠券扣减时使用）
     */
    private Boolean skipWalletDeduction = false;
}
