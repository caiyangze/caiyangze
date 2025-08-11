package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 动态更新DTO
 * @author zhentao
 * @date 2025/7/21
 */
@Data
public class MomentUpdateDTO {
    
    /**
     * 动态ID
     */
    @NotNull(message = "动态ID不能为空")
    private Long momentId;
    
    /**
     * 动态内容
     */
    @Size(max = 1000, message = "动态内容不能超过1000字符")
    private String content;
    
    /**
     * 位置信息
     */
    @Size(max = 100, message = "位置信息不能超过100字符")
    private String location;
    
    /**
     * 可见范围：1-公开，2-仅关注者可见，3-仅自己可见
     */
    private Integer visibility;
}
