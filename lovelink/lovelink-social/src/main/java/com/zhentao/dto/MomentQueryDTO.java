package com.zhentao.dto;

import lombok.Data;

/**
 * 动态查询DTO
 * @author zhentao
 * @date 2025/7/21
 */
@Data
public class MomentQueryDTO {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 用户ID（查询指定用户的动态）
     */
    private Long userId;
    
    /**
     * 可见范围：1-公开，2-仅关注者可见，3-仅自己可见
     */
    private Integer visibility;
    
    /**
     * 状态：0-已删除，1-正常，2-已屏蔽
     */
    private Integer status = 1;
    
    /**
     * 是否只查询自己的动态
     */
    private Boolean onlyMine = false;
}
