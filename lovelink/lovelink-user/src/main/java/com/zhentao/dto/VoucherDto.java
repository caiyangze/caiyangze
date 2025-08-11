package com.zhentao.dto;

import lombok.Data;

/**
 * @author 王恒飞
 * @date 2025/7/21
 * @ClassName VoucherDto
 */
@Data
public class VoucherDto extends Page{

    /**
     * 优惠券状态筛选
     * 1-未支付，2-已支付（未使用），3-已核销，4-已取消，5-退款中，6-已退款
     */
    private Integer status;
}
