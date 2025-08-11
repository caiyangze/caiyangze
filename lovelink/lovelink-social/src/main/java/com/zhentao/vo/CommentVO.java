package com.zhentao.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论展示VO
 * @author zhentao
 * @date 2025/7/23
 */
@Data
public class CommentVO {
    
    /**
     * 评论ID
     */
    private Long commentId;
    
    /**
     * 评论目标ID
     */
    private Long targetId;
    
    /**
     * 评论类型：1-动态，2-语音房，3-游戏
     */
    private Integer targetType;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatarUrl;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 父评论ID（回复评论时使用）
     */
    private Long parentId;
    
    /**
     * 回复用户ID
     */
    private Long replyUserId;
    
    /**
     * 回复用户昵称
     */
    private String replyUserNickname;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态：0-已删除，1-正常，2-已屏蔽
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 是否已点赞
     */
    private Boolean isLiked = false;
    
    /**
     * 子评论列表（回复）
     */
    private List<CommentVO> replies;
}
