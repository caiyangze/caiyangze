package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 红娘处理牵线申请DTO
 * @author zhentao
 */
@Data
public class MatchmakingRequestHandleDTO {
    
    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long requestId;
    
    /**
     * 处理动作：1-接受，2-拒绝
     */
    @NotNull(message = "处理动作不能为空")
    private Integer action;
    
    /**
     * 拒绝原因（当action=2时必填）
     */
    @Size(max = 200, message = "拒绝原因不能超过200字")
    private String rejectReason;
}
