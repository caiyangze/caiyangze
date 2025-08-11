package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评论创建DTO
 * @author zhentao
 * @date 2025/7/23
 */
@Data
public class CommentCreateDTO {
    
    /**
     * 动态ID
     */
    @NotNull(message = "动态ID不能为空")
    private Long momentId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字符")
    private String content;
    
    /**
     * 父评论ID（回复评论时使用）
     */
    private Long parentId;
    
    /**
     * 回复用户ID
     */
    private Long replyUserId;
}
