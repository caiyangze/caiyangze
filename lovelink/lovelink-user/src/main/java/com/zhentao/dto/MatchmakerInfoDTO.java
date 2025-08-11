package com.zhentao.dto;

import lombok.Data;

/**
 * 红娘信息DTO（用于前端展示）
 * @author zhentao
 */
@Data
public class MatchmakerInfoDTO {
    
    /**
     * 红娘ID
     */
    private Long matchmakerId;
    
    /**
     * 红娘昵称（显示名称）
     */
    private String nickname;
    
    /**
     * 红娘等级：1-预备红娘，2-正式红娘，3-金牌红娘
     */
    private Integer matchmakerLevel;
    
    /**
     * 红娘等级文本
     */
    private String matchmakerLevelText;
    
    /**
     * 服务区域
     */
    private String serviceArea;
    
    /**
     * 从业年限
     */
    private Integer serviceYears;
    
    /**
     * 成功牵线次数
     */
    private Integer successCount;
    
    /**
     * 个人介绍
     */
    private String introduction;
    
    /**
     * 当前处理的申请数量
     */
    private Long currentRequests;
    
    /**
     * 综合评分
     */
    private Double score;
    
    /**
     * 是否推荐（基于评分排序）
     */
    private Boolean recommended;
}
