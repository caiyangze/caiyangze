package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 打招呼DTO
 * @author zhentao
 * @date 2025/8/3
 */
@Data
public class GreetingDTO {
    
    /**
     * 目标用户ID
     */
    @NotNull(message = "目标用户ID不能为空")
    private Long toUserId;
    
    /**
     * 招呼消息
     */
    @Size(max = 200, message = "招呼消息不能超过200个字符")
    private String message = "你好，很高兴认识你！";
    
    /**
     * 招呼类型：1-文字，2-表情，3-语音
     */
    private Integer type = 1;
}
