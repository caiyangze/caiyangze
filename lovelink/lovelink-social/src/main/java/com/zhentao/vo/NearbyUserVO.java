package com.zhentao.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 附近用户信息VO
 * @author zhentao
 * @date 2025/8/3
 */
@Data
public class NearbyUserVO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 自我介绍
     */
    private String selfIntroduction;
    
    /**
     * 用户标签
     */
    private List<String> tags;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 距离（单位：公里）
     */
    private Double distance;
    
    /**
     * 位置描述
     */
    private String locationName;
    
    /**
     * 是否在线
     */
    private Boolean isOnline = false;
    
    /**
     * 最后活跃时间
     */
    private Date lastActiveTime;
    
    /**
     * 是否VIP
     */
    private Integer isVip;
    
    /**
     * 是否已关注
     */
    private Boolean isFollowed = false;

    /**
     * 匹配度（0-100）
     */
    private Double matchScore = 50.0;

    /**
     * 是否已点赞
     */
    private Boolean isLiked = false;

    /**
     * 共同兴趣标签数量
     */
    private Integer commonInterests = 0;


}
