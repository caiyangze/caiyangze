package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 牵线申请确认DTO
 * @author zhentao
 */
@Data
public class MatchmakingConfirmDTO {
    
    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long requestId;
    
    /**
     * 操作类型：1-同意，2-拒绝
     */
    @NotNull(message = "操作类型不能为空")
    private Integer action;
    
    /**
     * 拒绝原因（拒绝时可填写）
     */
    @Size(max = 200, message = "拒绝原因不能超过200字")
    private String rejectReason;
}
