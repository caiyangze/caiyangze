package com.zhentao.vo;

import com.zhentao.pojo.TbMomentMedia;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 动态展示VO
 * @author zhentao
 * @date 2025/7/21
 */
@Data
public class MomentVO {
    
    /**
     * 动态ID
     */
    private Long momentId;
    
    /**
     * 用户ID
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
     * 动态内容
     */
    private String content;
    
    /**
     * 位置信息
     */
    private String location;
    
    /**
     * 可见范围：1-公开，2-仅关注者可见，3-仅自己可见
     */
    private Integer visibility;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 浏览数
     */
    private Integer viewCount;
    
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
     * 媒体文件列表
     */
    private List<TbMomentMedia> mediaList;
    
    /**
     * 是否已点赞
     */
    private Boolean isLiked = false;
    
    /**
     * 是否是自己的动态
     */
    private Boolean isMine = false;
}
