package com.zhentao.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 红娘服务订单DTO
 * @author zhentao
 */
@Data
public class MatchmakerOrderDTO {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 红娘ID
     */
    private Long matchmakerId;
    
    /**
     * 红娘昵称
     */
    private String matchmakerNickname;
    
    /**
     * 服务类型：1-单次牵线，2-包月服务，3-高端定制
     */
    private Integer serviceType;
    
    /**
     * 服务类型文本
     */
    private String serviceTypeText;
    
    /**
     * 服务描述
     */
    private String serviceDesc;
    
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
     * 支付方式：1-微信，2-支付宝，3-虚拟币，4-其他
     */
    private Integer payType;
    
    /**
     * 支付方式文本
     */
    private String payTypeText;
    
    /**
     * 支付时间
     */
    private Date payTime;
    
    /**
     * 支付平台交易号
     */
    private String transactionId;
    
    /**
     * 服务开始时间
     */
    private Date serviceStartTime;
    
    /**
     * 服务结束时间
     */
    private Date serviceEndTime;
    
    /**
     * 订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成
     */
    private Integer orderStatus;
    
    /**
     * 订单状态文本
     */
    private String orderStatusText;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 是否可以取消
     */
    private Boolean canCancel;
    
    /**
     * 是否可以支付
     */
    private Boolean canPay;
    
    /**
     * 是否可以申请退款
     */
    private Boolean canRefund;
}
