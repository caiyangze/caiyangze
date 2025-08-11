package com.zhentao.tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import com.zhentao.service.TbUserService;
import com.zhentao.service.TbMatchmakerService;
import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbMatchmaker;
import com.zhentao.bean.RecommendationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * AI红娘工具类
 * 提供智能匹配、游戏推荐、红娘服务等功能
 */
@Component
public class AppointmentTools {

    @Autowired
    private TbUserService tbUserService;

    @Autowired
    private TbMatchmakerService tbMatchmakerService;

    /**
     * 文生图工具
     * 根据用户描述生成相应的图片
     */
    @Tool(name = "文生图", value = "根据用户的文字描述生成对应的图片，适用于头像生成、场景描绘、情感表达等需求")
    public String generateImage(
            @ToolMemoryId Long memoryId,
            @P(value = "图片描述", required = true) String description) {

        try {
            System.out.println("调用文生图功能 memoryId: " + memoryId + ", 描述: " + description);

            // 创建万象文生图模型
            WanxImageModel wanxImageModel = WanxImageModel.builder()
                    .modelName("wan2.2-t2i-flash")  // 使用万象v1模型
                    .apiKey("sk-c321eddd8a3549889fa460c1e695e0f2")
                    .build();

            // 优化提示词，添加良缘平台相关的风格指导
            String optimizedPrompt = optimizePromptForLoveApp(description);

            // 生成图片
            Response<Image> response = wanxImageModel.generate(optimizedPrompt);

            if (response != null && response.content() != null) {
                String imageUrl = String.valueOf(response.content().url());
                System.out.println("图片生成成功，URL: " + imageUrl);
                return "图片生成成功！\n图片链接：" + imageUrl + "\n\n温馨提示：\n" +
                       "1. 图片链接有效期为24小时，请及时保存\n" +
                       "2. 生成的图片可用作头像、聊天背景等\n" +
                       "3. 如需重新生成，请提供更详细的描述";
            } else {
                return "抱歉，图片生成失败，请稍后重试或尝试修改描述内容。";
            }

        } catch (Exception e) {
            System.err.println("文生图生成失败: " + e.getMessage());
            e.printStackTrace();
            return "图片生成遇到问题：" + e.getMessage() + "\n请检查网络连接或稍后重试。";
        }
    }
    /**
     * 为良缘平台优化提示词
     * 添加适合婚恋社交场景的风格指导
     */
    private String optimizePromptForLoveApp(String originalPrompt) {
        // 如果用户没有指定风格，添加温馨、浪漫的风格指导
        String styleGuide = "温馨浪漫风格，柔和色调，高质量，精美细节，";

        // 检查是否已经包含风格描述
        if (!originalPrompt.toLowerCase().contains("风格") &&
            !originalPrompt.toLowerCase().contains("style") &&
            !originalPrompt.toLowerCase().contains("色调")) {
            return styleGuide + originalPrompt;
        }

        return originalPrompt;
    }
    /**
     * 智能推荐异性用户工具
     * 根据用户的性别、年龄、地区等信息推荐合适的异性用户
     */
    @Tool(name = "推荐用户", value = "根据用户角色和基本信息，智能推荐合适的用户：普通用户推荐异性，红娘推荐所有用户")
    public String recommendUsers(
            @ToolMemoryId Long memoryId,
            @P(value = "用户ID", required = true) Long userId,
            @P(value = "用户性别：0-女，1-男", required = true) Integer userGender,
            @P(value = "用户角色：1-普通用户，2-红娘", required = false) Integer userRole,
            @P(value = "用户年龄", required = false) Integer userAge,
            @P(value = "用户所在地区", required = false) String userLocation,
            @P(value = "推荐数量，默认5个", required = false) Integer limit) {

        System.out.println("调用推荐用户功能 memoryId: " + memoryId + ", userId: " + userId +
                          ", gender: " + userGender + ", userRole: " + userRole + ", age: " + userAge + ", location: " + userLocation);

        try {
            // 设置默认推荐数量
            if (limit == null || limit <= 0) {
                limit = 5;
            }
            if (limit > 20) {
                limit = 20; // 最多推荐20个
            }

            // 根据用户角色获取推荐结果
            RecommendationResult result = performUserRecommendation(userId, userGender, userRole, userAge, userLocation, limit);

            if (result != null && result.getUserRecommendations() != null && !result.getUserRecommendations().isEmpty()) {
                StringBuilder response = new StringBuilder();

                // 根据用户角色显示不同的推荐标题
                if (userRole != null && userRole == 2) {
                    response.append("🌹 为您推荐了 ").append(result.getUserRecommendations().size()).append(" 位平台用户（红娘专用）：\n\n");
                } else {
                    response.append("💕 为您推荐了 ").append(result.getUserRecommendations().size()).append(" 位优质异性用户：\n\n");
                }

                for (int i = 0; i < result.getUserRecommendations().size(); i++) {
                    RecommendationResult.UserRecommendation user = result.getUserRecommendations().get(i);
                    response.append("👤 **推荐 ").append(i + 1).append("**\n");
                    response.append("昵称：").append(user.getNickname()).append("\n");
                    response.append("年龄：").append(user.getAge()).append("岁\n");
                    response.append("性别：").append(user.getGender() == 0 ? "女" : "男").append("\n");
                    if (user.getLocation() != null) {
                        response.append("地区：").append(user.getLocation()).append("\n");
                    }
                    response.append("匹配度：").append(user.getMatchScore()).append("%\n");
                    response.append("匹配理由：").append(user.getMatchReason()).append("\n");

                    if (user.getIsVip() != null && user.getIsVip()) {
                        response.append("👑 VIP用户\n");
                    }
                    if (user.getIsVerified() != null && user.getIsVerified()) {
                        response.append("✅ 已实名认证\n");
                    }

                    response.append("获赞数：").append(user.getCountLike() != null ? user.getCountLike() : 0).append("\n");
                    response.append("关注数：").append(user.getCountFollow() != null ? user.getCountFollow() : 0).append("\n");
                    response.append("\n");
                }

                response.append("💡 温馨提示：\n");
                response.append("1. 匹配度基于多维度算法计算\n");
                response.append("2. 建议主动发起友好的对话\n");
                response.append("3. 真诚交流是建立良好关系的基础\n");
                response.append("4. 如需查看更多推荐，请告诉我您的具体偏好");

                return response.toString();
            } else {
                return "抱歉，暂时没有找到合适的推荐用户。\n\n💡 建议：\n1. 完善您的个人资料\n2. 扩大地区范围\n3. 稍后再试，会有新用户加入";
            }

        } catch (Exception e) {
            System.err.println("推荐异性用户失败: " + e.getMessage());
            e.printStackTrace();
            return "推荐功能暂时遇到问题：" + e.getMessage() + "\n请稍后重试或联系客服。";
        }
    }

    /**
     * 智能推荐红娘工具
     * 根据用户需求推荐专业的红娘服务
     */
    @Tool(name = "推荐红娘", value = "推荐平台所有优质红娘，提供专业婚恋指导服务")
    public String recommendMatchmakers(
            @ToolMemoryId Long memoryId,
            @P(value = "用户ID", required = true) Long userId,
            @P(value = "推荐数量，默认8个", required = false) Integer limit) {

        System.out.println("调用推荐红娘功能 memoryId: " + memoryId + ", userId: " + userId + ", limit: " + limit);

        try {
            // 设置推荐数量 - 增加默认数量以展示更多红娘选择
            if (limit == null || limit <= 0) {
                limit = 8; // 默认推荐8个红娘
            }
            if (limit > 20) {
                limit = 20; // 最多推荐20个红娘
            }

            // 推荐所有优质红娘，不限制地区和等级
            RecommendationResult result = performMatchmakerRecommendation(userId, null, null, limit);

            if (result != null && result.getMatchmakerRecommendations() != null && !result.getMatchmakerRecommendations().isEmpty()) {
                StringBuilder response = new StringBuilder();
                response.append("🌹 为您推荐 ").append(result.getMatchmakerRecommendations().size()).append(" 位平台优质红娘：\n\n");

                for (int i = 0; i < result.getMatchmakerRecommendations().size(); i++) {
                    RecommendationResult.MatchmakerRecommendation matchmaker = result.getMatchmakerRecommendations().get(i);
                    response.append("👩‍💼 **红娘推荐 ").append(i + 1).append("**\n");
                    response.append("昵称：").append(matchmaker.getNickname()).append("\n");

                    String levelDesc = "";
                    switch (matchmaker.getMatchmakerLevel()) {
                        case 1: levelDesc = "预备红娘"; break;
                        case 2: levelDesc = "正式红娘"; break;
                        case 3: levelDesc = "金牌红娘⭐"; break;
                        default: levelDesc = "红娘"; break;
                    }
                    response.append("等级：").append(levelDesc).append("\n");

                    if (matchmaker.getServiceArea() != null) {
                        response.append("服务区域：").append(matchmaker.getServiceArea()).append("\n");
                    }
                    response.append("从业年限：").append(matchmaker.getServiceYears() != null ? matchmaker.getServiceYears() : 0).append("年\n");
                    response.append("成功牵线：").append(matchmaker.getSuccessCount() != null ? matchmaker.getSuccessCount() : 0).append("次\n");

                    if (matchmaker.getIntroduction() != null && !matchmaker.getIntroduction().trim().isEmpty()) {
                        response.append("个人介绍：").append(matchmaker.getIntroduction()).append("\n");
                    }

                    if (matchmaker.getIsOnline() != null && matchmaker.getIsOnline()) {
                        response.append("🟢 当前在线\n");
                    } else {
                        response.append("🔴 当前离线\n");
                    }

                    response.append("\n");
                }

                response.append("💡 服务说明：\n");
                response.append("1. 🌟 平台精选所有等级优质红娘，专业可靠\n");
                response.append("2. 📞 支持在线咨询、电话沟通、面谈服务\n");
                response.append("3. 🎯 各等级红娘提供差异化专业指导\n");
                response.append("4. 💝 如需了解特定红娘详情或预约服务，请告诉我");

                return response.toString();
            } else {
                return "🤔 暂时没有查询到红娘信息。\n\n💡 可能原因：\n1. 平台红娘信息正在更新中\n2. 系统暂时繁忙，请稍后重试\n3. 如有紧急需求，请联系客服获取帮助";
            }

        } catch (Exception e) {
            System.err.println("推荐专业红娘失败: " + e.getMessage());
            e.printStackTrace();
            return "红娘推荐功能暂时遇到问题：" + e.getMessage() + "\n请稍后重试或联系客服。";
        }
    }

    /**
     * 执行用户推荐算法
     */
    private RecommendationResult performUserRecommendation(Long userId, Integer userGender, Integer userRole, Integer userAge, String userLocation, Integer limit) {
        RecommendationResult result = new RecommendationResult();
        result.setRecommendationType("user");

        try {
            // 根据用户角色确定推荐策略
            Integer targetGender = null;
            if (userRole != null && userRole == 2) {
                // 红娘用户：推荐所有用户（不限性别）
                targetGender = null;
                System.out.println("红娘用户，推荐所有用户");
            } else {
                // 普通用户：推荐异性用户
                targetGender = (userGender == 0) ? 1 : 0;
                System.out.println("普通用户，推荐异性用户，目标性别: " + targetGender);
            }

            // 使用Service层的方法查询候选用户
            List<TbUser> candidateUsers = tbUserService.findRecommendedUsers(userId, targetGender, userLocation, limit);

            if (candidateUsers != null && !candidateUsers.isEmpty()) {
                // 应用推荐算法，计算匹配度
                List<RecommendationResult.UserRecommendation> recommendations = candidateUsers.stream()
                    .map(user -> {
                        RecommendationResult.UserRecommendation recommendation = new RecommendationResult.UserRecommendation();
                        recommendation.setUserId(user.getUserId());
                        recommendation.setNickname(user.getNickname() != null ? user.getNickname() : "用户" + user.getUserId());
                        recommendation.setGender(user.getGender());
                        recommendation.setAvatarUrl(user.getAvatarUrl());
                        recommendation.setLastLoginTime(user.getLastLoginTime());
                        recommendation.setIsVip(user.getIsVip() != null && user.getIsVip() == 1);
                        recommendation.setIsVerified(user.getIsVerified() != null && user.getIsVerified() == 1);
                        recommendation.setCountLike(user.getCountLike() != null ? user.getCountLike() : 0);
                        recommendation.setCountFollow(user.getCountFollow() != null ? user.getCountFollow() : 0);

                        // 计算年龄
                        if (user.getBirthDate() != null) {
                            Calendar cal = Calendar.getInstance();
                            int currentYear = cal.get(Calendar.YEAR);
                            cal.setTime(user.getBirthDate());
                            int birthYear = cal.get(Calendar.YEAR);
                            recommendation.setAge(currentYear - birthYear);
                        } else {
                            // 如果没有生日信息，设置一个默认年龄范围
                            recommendation.setAge(25); // 默认25岁
                        }

                        // 设置地区信息（如果有的话，这里需要根据实际数据库字段调整）
                        // recommendation.setLocation(user.getLocation()); // 假设有location字段

                        // 计算匹配度评分
                        int matchScore = calculateMatchScore(userAge, userLocation, user);
                        recommendation.setMatchScore(matchScore);

                        // 生成匹配理由
                        String matchReason = generateMatchReason(userAge, userLocation, user, matchScore);
                        recommendation.setMatchReason(matchReason);

                        return recommendation;
                    })
                    .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore())) // 按匹配度降序排序
                    .limit(limit) // 取前N个
                    .collect(Collectors.toList());

                result.setUserRecommendations(recommendations);
                result.setTotalCount(recommendations.size());
                result.setRecommendationReason("基于真实用户数据和多维度智能算法为您精心推荐");
            } else {
                result.setUserRecommendations(List.of());
                result.setTotalCount(0);
                result.setRecommendationReason("暂时没有找到合适的用户，建议完善个人资料或扩大筛选条件");
            }

        } catch (Exception e) {
            System.err.println("执行用户推荐算法失败: " + e.getMessage());
            e.printStackTrace();
            result.setUserRecommendations(List.of());
            result.setTotalCount(0);
            result.setRecommendationReason("推荐服务暂时不可用，请稍后重试");
        }

        return result;
    }

    /**
     * 执行红娘推荐算法
     */
    private RecommendationResult performMatchmakerRecommendation(Long userId, String userLocation, Integer preferredLevel, Integer limit) {
        RecommendationResult result = new RecommendationResult();
        result.setRecommendationType("matchmaker");

        try {
            // 使用Service层的方法查询候选红娘
            List<TbMatchmaker> candidateMatchmakers = tbMatchmakerService.findRecommendedMatchmakers(userLocation, preferredLevel, limit);

            if (candidateMatchmakers != null && !candidateMatchmakers.isEmpty()) {
                // 获取红娘对应的用户信息
                List<Long> userIds = candidateMatchmakers.stream()
                    .map(TbMatchmaker::getUserId)
                    .collect(Collectors.toList());

                Map<Long, TbUser> userMap = new HashMap<>();
                if (!userIds.isEmpty()) {
                    List<TbUser> users = tbUserService.getUsersByIds(userIds);
                    userMap = users.stream().collect(Collectors.toMap(TbUser::getUserId, user -> user));
                }

                // 构建推荐结果
                final Map<Long, TbUser> finalUserMap = userMap;
                List<RecommendationResult.MatchmakerRecommendation> recommendations = candidateMatchmakers.stream()
                    .map(matchmaker -> {
                        RecommendationResult.MatchmakerRecommendation recommendation = new RecommendationResult.MatchmakerRecommendation();
                        recommendation.setMatchmakerId(matchmaker.getMatchmakerId());
                        recommendation.setUserId(matchmaker.getUserId());
                        recommendation.setRealName(matchmaker.getRealName());
                        recommendation.setMatchmakerLevel(matchmaker.getMatchmakerLevel() != null ? matchmaker.getMatchmakerLevel() : 1);
                        recommendation.setServiceArea(matchmaker.getServiceArea());
                        recommendation.setServiceYears(matchmaker.getServiceYears() != null ? matchmaker.getServiceYears() : 0);
                        recommendation.setSuccessCount(matchmaker.getSuccessCount() != null ? matchmaker.getSuccessCount() : 0);
                        recommendation.setIntroduction(matchmaker.getIntroduction());
                        recommendation.setCertification(matchmaker.getCertification());

                        // 获取对应的用户信息
                        TbUser user = finalUserMap.get(matchmaker.getUserId());
                        if (user != null) {
                            recommendation.setNickname(user.getNickname() != null ? user.getNickname() : "红娘" + matchmaker.getMatchmakerId());
                            recommendation.setAvatarUrl(user.getAvatarUrl());

                            // 简单判断是否在线（最近24小时内登录过）
                            if (user.getLastLoginTime() != null) {
                                long timeDiff = System.currentTimeMillis() - user.getLastLoginTime().getTime();
                                recommendation.setIsOnline(timeDiff < 24 * 60 * 60 * 1000); // 24小时内算在线
                            } else {
                                recommendation.setIsOnline(false);
                            }
                        } else {
                            // 如果没有找到对应的用户信息，设置默认值
                            recommendation.setNickname("红娘" + matchmaker.getMatchmakerId());
                            recommendation.setAvatarUrl(null);
                            recommendation.setIsOnline(false);
                        }

                        // 计算评分（基于等级、成功次数、从业年限）
                        int rating = calculateMatchmakerRating(matchmaker);
                        recommendation.setRating(rating);

                        // 生成专长描述
                        String specialties = generateMatchmakerSpecialties(matchmaker);
                        recommendation.setSpecialties(specialties);

                        return recommendation;
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

                result.setMatchmakerRecommendations(recommendations);
                result.setTotalCount(recommendations.size());
                result.setRecommendationReason("基于真实红娘数据和专业能力为您推荐");
            } else {
                result.setMatchmakerRecommendations(List.of());
                result.setTotalCount(0);
                result.setRecommendationReason("暂时没有找到合适的红娘，建议扩大地区范围或降低等级要求");
            }

        } catch (Exception e) {
            System.err.println("执行红娘推荐算法失败: " + e.getMessage());
            e.printStackTrace();
            result.setMatchmakerRecommendations(List.of());
            result.setTotalCount(0);
            result.setRecommendationReason("红娘推荐服务暂时不可用，请稍后重试");
        }

        return result;
    }

    /**
     * 计算用户匹配度评分
     */
    private int calculateMatchScore(Integer userAge, String userLocation, TbUser targetUser) {
        int score = 60; // 基础分

        try {
            // 年龄匹配度（如果有年龄信息）
            if (userAge != null && targetUser.getBirthDate() != null) {
                Calendar cal = Calendar.getInstance();
                int currentYear = cal.get(Calendar.YEAR);
                cal.setTime(targetUser.getBirthDate());
                int targetAge = currentYear - cal.get(Calendar.YEAR);

                int ageDiff = Math.abs(userAge - targetAge);
                if (ageDiff <= 2) {
                    score += 20; // 年龄相近加分
                } else if (ageDiff <= 5) {
                    score += 10;
                } else if (ageDiff <= 10) {
                    score += 5;
                }
            }

            // VIP用户加分
            if (targetUser.getIsVip() != null && targetUser.getIsVip() == 1) {
                score += 10;
            }

            // 实名认证加分
            if (targetUser.getIsVerified() != null && targetUser.getIsVerified() == 1) {
                score += 10;
            }

            // 活跃度加分（最近登录时间）
            if (targetUser.getLastLoginTime() != null) {
                long timeDiff = System.currentTimeMillis() - targetUser.getLastLoginTime().getTime();
                if (timeDiff < 24 * 60 * 60 * 1000) { // 24小时内
                    score += 15;
                } else if (timeDiff < 7 * 24 * 60 * 60 * 1000) { // 7天内
                    score += 10;
                } else if (timeDiff < 30 * 24 * 60 * 60 * 1000) { // 30天内
                    score += 5;
                }
            }

            // 受欢迎程度加分
            if (targetUser.getCountLike() != null && targetUser.getCountLike() > 0) {
                if (targetUser.getCountLike() >= 100) {
                    score += 10;
                } else if (targetUser.getCountLike() >= 50) {
                    score += 5;
                }
            }

        } catch (Exception e) {
            System.err.println("计算匹配度评分失败: " + e.getMessage());
        }

        // 确保分数在合理范围内
        return Math.min(Math.max(score, 0), 100);
    }

    /**
     * 生成匹配理由
     */
    private String generateMatchReason(Integer userAge, String userLocation, TbUser targetUser, int matchScore) {
        List<String> reasons = new ArrayList<>();

        try {
            if (targetUser.getIsVip() != null && targetUser.getIsVip() == 1) {
                reasons.add("VIP优质用户");
            }

            if (targetUser.getIsVerified() != null && targetUser.getIsVerified() == 1) {
                reasons.add("已实名认证");
            }

            if (targetUser.getLastLoginTime() != null) {
                long timeDiff = System.currentTimeMillis() - targetUser.getLastLoginTime().getTime();
                if (timeDiff < 24 * 60 * 60 * 1000) {
                    reasons.add("活跃用户");
                }
            }

            if (userAge != null && targetUser.getBirthDate() != null) {
                Calendar cal = Calendar.getInstance();
                int currentYear = cal.get(Calendar.YEAR);
                cal.setTime(targetUser.getBirthDate());
                int targetAge = currentYear - cal.get(Calendar.YEAR);
                int ageDiff = Math.abs(userAge - targetAge);

                if (ageDiff <= 2) {
                    reasons.add("年龄相近");
                } else if (ageDiff <= 5) {
                    reasons.add("年龄合适");
                }
            }

            if (targetUser.getCountLike() != null && targetUser.getCountLike() >= 50) {
                reasons.add("人气较高");
            }

        } catch (Exception e) {
            System.err.println("生成匹配理由失败: " + e.getMessage());
        }

        if (reasons.isEmpty()) {
            return "系统智能推荐";
        }

        return String.join("、", reasons);
    }

    /**
     * 计算红娘评分
     */
    private int calculateMatchmakerRating(TbMatchmaker matchmaker) {
        int rating = 3; // 基础3星

        try {
            // 根据等级加分
            if (matchmaker.getMatchmakerLevel() != null) {
                switch (matchmaker.getMatchmakerLevel()) {
                    case 3: // 金牌红娘
                        rating = 5;
                        break;
                    case 2: // 正式红娘
                        rating = 4;
                        break;
                    case 1: // 预备红娘
                        rating = 3;
                        break;
                }
            }

            // 根据成功次数微调
            if (matchmaker.getSuccessCount() != null) {
                if (matchmaker.getSuccessCount() >= 100) {
                    rating = Math.min(rating + 1, 5);
                } else if (matchmaker.getSuccessCount() >= 50) {
                    // 保持当前评分
                } else if (matchmaker.getSuccessCount() < 10 && rating > 3) {
                    rating = Math.max(rating - 1, 3);
                }
            }

        } catch (Exception e) {
            System.err.println("计算红娘评分失败: " + e.getMessage());
        }

        return Math.min(Math.max(rating, 1), 5);
    }

    /**
     * 生成红娘专长描述
     */
    private String generateMatchmakerSpecialties(TbMatchmaker matchmaker) {
        List<String> specialties = new ArrayList<>();

        try {
            // 根据等级确定专长
            if (matchmaker.getMatchmakerLevel() != null) {
                switch (matchmaker.getMatchmakerLevel()) {
                    case 3: // 金牌红娘
                        specialties.add("高端婚恋定制");
                        specialties.add("深度情感分析");
                        break;
                    case 2: // 正式红娘
                        specialties.add("专业婚恋指导");
                        specialties.add("个性化匹配");
                        break;
                    case 1: // 预备红娘
                        specialties.add("基础婚恋咨询");
                        specialties.add("交友指导");
                        break;
                }
            }

            // 根据从业年限添加专长
            if (matchmaker.getServiceYears() != null) {
                if (matchmaker.getServiceYears() >= 5) {
                    specialties.add("资深经验");
                } else if (matchmaker.getServiceYears() >= 3) {
                    specialties.add("丰富经验");
                }
            }

            // 根据成功次数添加专长
            if (matchmaker.getSuccessCount() != null) {
                if (matchmaker.getSuccessCount() >= 50) {
                    specialties.add("高成功率");
                } else if (matchmaker.getSuccessCount() >= 20) {
                    specialties.add("成功案例丰富");
                }
            }

        } catch (Exception e) {
            System.err.println("生成红娘专长失败: " + e.getMessage());
        }

        if (specialties.isEmpty()) {
            return "专业婚恋服务";
        }

        return String.join("、", specialties);
    }

    /**
     * 获取推荐结果（供Controller使用）
     */
    public RecommendationResult getRecommendations(Long userId, Integer userGender, Integer userAge,
                                                  String userLocation, String recommendationType, Integer limit) {
        if ("user".equals(recommendationType)) {
            // 默认为普通用户角色，推荐异性
            return performUserRecommendation(userId, userGender, 1, userAge, userLocation, limit);
        } else if ("matchmaker".equals(recommendationType)) {
            return performMatchmakerRecommendation(userId, userLocation, null, limit);
        } else {
            throw new IllegalArgumentException("不支持的推荐类型: " + recommendationType);
        }
    }
}
