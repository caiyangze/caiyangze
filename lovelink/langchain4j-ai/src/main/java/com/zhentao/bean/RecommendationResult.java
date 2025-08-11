package com.zhentao.bean;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 推荐结果数据传输对象
 */
@Data
public class RecommendationResult {
    
    /**
     * 推荐类型：user-用户推荐, matchmaker-红娘推荐
     */
    private String recommendationType;
    
    /**
     * 推荐的用户列表
     */
    private List<UserRecommendation> userRecommendations;
    
    /**
     * 推荐的红娘列表
     */
    private List<MatchmakerRecommendation> matchmakerRecommendations;
    
    /**
     * 推荐理由
     */
    private String recommendationReason;
    
    /**
     * 推荐总数
     */
    private Integer totalCount;
    
    /**
     * 用户推荐信息
     */
    @Data
    public static class UserRecommendation {
        private Long userId;
        private String nickname;
        private Integer gender;
        private Integer age;
        private String avatarUrl;
        private String location;
        private String introduction;
        private Integer matchScore; // 匹配度评分 0-100
        private String matchReason; // 匹配理由
        private Date lastLoginTime;
        private Boolean isVip;
        private Boolean isVerified;
        private Integer countLike;
        private Integer countFollow;
    }
    
    /**
     * 红娘推荐信息
     */
    @Data
    public static class MatchmakerRecommendation {
        private Long matchmakerId;
        private Long userId;
        private String nickname;
        private String realName;
        private String avatarUrl;
        private Integer matchmakerLevel;
        private String serviceArea;
        private Integer serviceYears;
        private Integer successCount;
        private String introduction;
        private String certification;
        private Integer rating; // 评分 0-5星
        private String specialties; // 专长领域
        private Boolean isOnline; // 是否在线
    }
}
