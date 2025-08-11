package com.zhentao.dto;

import lombok.Data;

@Data
public class MatchmakingRequestListDTO {
    // 当前页（小程序端传，默认 1）
    private Integer pageNum = 1; 
    // 每页条数（小程序端传，默认 10）
    private Integer pageSize = 10; 
    // 筛选条件：申请用户 ID（可选）
    private Long userId; 
    // 筛选条件：申请状态（可选，如 0-待处理、1-已接受 等）
    private Integer requestStatus; 
}