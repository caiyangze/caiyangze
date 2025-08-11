package com.zhentao.dto;

import lombok.Data;

/**
 * 红娘服务订单查询参数DTO
 * @author zhentao
 */
@Data
public class MatchmakerOrderQueryDTO {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 用户ID（可选，管理员查询时使用）
     */
    private Long userId;
    
    /**
     * 红娘ID（可选）
     */
    private Long matchmakerId;
    
    /**
     * 订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成
     */
    private Integer orderStatus;
    
    /**
     * 服务类型：1-单次牵线，2-包月服务，3-高端定制
     */
    private Integer serviceType;
    
    /**
     * 支付方式：1-微信，2-支付宝，3-虚拟币，4-其他
     */
    private Integer payType;
    
    /**
     * 订单编号（模糊查询）
     */
    private String orderNo;
    
    /**
     * 开始时间（创建时间范围查询）
     */
    private String startTime;
    
    /**
     * 结束时间（创建时间范围查询）
     */
    private String endTime;
}
