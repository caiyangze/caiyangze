package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 牵线申请创建DTO
 * @author zhentao
 */
@Data
public class MatchmakingRequestCreateDTO {

    /**
     * 目标用户ID
     */
    @NotNull(message = "目标用户ID不能为空")
    private Long targetUserId;

    /**
     * 红娘ID（可选，如果不指定则根据等级和分配方式分配）
     */
    private Long matchmakerId;

    /**
     * 红娘等级：1-预备红娘，2-正式红娘，3-金牌红娘
     * 如果指定了红娘ID，此字段无效
     * 如果未指定红娘ID，系统会从此等级的红娘中智能分配
     */
    private Integer matchmakerLevel;

    /**
     * 申请留言
     */
    @NotNull(message = "申请留言不能为空")
    @Size(min = 10, max = 500, message = "申请留言长度应在10-500字之间")
    private String requestMessage;
}
