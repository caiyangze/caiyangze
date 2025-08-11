package com.zhentao.dto;


import lombok.Data;

@Data
public class MatchmakerRequestDto {
    //申请用户id
    private Long userId;
    //目标用户ID
    private Long targetUserId;
    //红娘ID(可为空)
    private Long matchmakerId;
    //申请留言
    private String requestMessage;
    //申请状态（前端一般不填，默认0-待处理，可在Service层赋值）
    private Integer requestStatus;
    //拒绝原因（前端默认不填，默认为空）
    private String rejectReason;
}
