package com.zhentao.vo;

import lombok.Data;

import java.util.Date;

@Data
public class MatchmakingRequestVO {
    // 申请 ID
    private Long requestId; 
    // 申请用户 ID
    private Long userId; 
    // 目标用户 ID
    private Long targetUserId; 
    // 申请状态（小程序端可根据此做状态展示）
    private Integer requestStatus; 
    // 申请留言
    private String requestMessage; 
    // 创建时间（格式化后给小程序，如 "2025-07-21 14:00"）
    private Date createdAt;
    // 其他需要给小程序的字段...
}