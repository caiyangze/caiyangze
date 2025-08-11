package com.zhentao.service.impl;

import com.zhentao.dto.NearbyUsersQueryDTO;
import com.zhentao.dto.UserLocationDTO;
import com.zhentao.feign.UserServiceClient;
import com.zhentao.service.LocationService;
import com.zhentao.vo.NearbyUserVO;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.util.Random;

/**
 * Location service implementation
 * @author zhentao
 * @date 2025/8/3
 */
@Slf4j
@Service
public class LocationServiceImpl implements LocationService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserServiceClient userServiceClient;
    
    // Redis中存储用户位置的key前缀
    private static final String USER_LOCATION_KEY = "user:location";
    // Redis中存储用户详细位置信息的key前缀
    private static final String USER_LOCATION_DETAIL_KEY = "user:location:detail:";
    // 位置信息过期时间（24小时）
    private static final long LOCATION_EXPIRE_TIME = 24 * 60 * 60;
    
    @Override
    public Boolean updateUserLocation(Long userId, UserLocationDTO locationDTO) {
        try {
            // 1. 将用户位置添加到Redis Geo中
            Point point = new Point(locationDTO.getLongitude(), locationDTO.getLatitude());
            redisTemplate.opsForGeo().add(USER_LOCATION_KEY, point, userId.toString());
            
            // 2. 存储用户详细位置信息
            String detailKey = USER_LOCATION_DETAIL_KEY + userId;
            Map<String, Object> locationDetail = new HashMap<>();
            locationDetail.put("longitude", locationDTO.getLongitude());
            locationDetail.put("latitude", locationDTO.getLatitude());
            locationDetail.put("locationName", locationDTO.getLocationName());
            locationDetail.put("address", locationDTO.getAddress());
            locationDetail.put("updateTime", System.currentTimeMillis());
            
            redisTemplate.opsForHash().putAll(detailKey, locationDetail);
            redisTemplate.expire(detailKey, LOCATION_EXPIRE_TIME, TimeUnit.SECONDS);
            
            log.info("用户{}位置更新成功: 经度={}, 纬度={}", userId, locationDTO.getLongitude(), locationDTO.getLatitude());
            return true;
        } catch (Exception e) {
            log.error("更新用户{}位置失败", userId, e);
            return false;
        }
    }
    
    @Override
    public List<NearbyUserVO> getNearbyUsers(Long currentUserId, NearbyUsersQueryDTO queryDTO) {
        try {
            // 1. 构建查询条件
            Point center = new Point(queryDTO.getLongitude(), queryDTO.getLatitude());
            Distance radius = new Distance(queryDTO.getRadius(), Metrics.KILOMETERS);
            Circle circle = new Circle(center, radius);

            // 2. 查询附近的用户
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .includeCoordinates()
                    .sortAscending()
                    .limit(queryDTO.getLimit() * 2); // 扩大查询范围，后续筛选

            GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                    redisTemplate.opsForGeo().radius(USER_LOCATION_KEY, circle, args);

            log.info("Redis查询结果: {}", results != null ? results.getContent().size() : 0);

            if (results == null || results.getContent().isEmpty()) {
                log.info("附近没有找到其他用户");
                return new ArrayList<>();
            }

            // 3. 批量获取用户信息
            List<Long> userIds = new ArrayList<>();
            Map<Long, GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResultMap = new HashMap<>();

            for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
                try {
                    String userIdStr = result.getContent().getName().toString();
                    Long userId = Long.parseLong(userIdStr);

                    // 排除当前用户
                    if (!userId.equals(currentUserId)) {
                        userIds.add(userId);
                        geoResultMap.put(userId, result);
                    }
                } catch (Exception e) {
                    log.warn("解析用户ID失败: {}", result.getContent().getName(), e);
                }
            }

            if (userIds.isEmpty()) {
                log.info("排除当前用户后，附近没有其他用户");
                return new ArrayList<>();
            }

            // 4. 批量构建用户信息
            List<NearbyUserVO> nearbyUsers = buildNearbyUsersInBatch(userIds, geoResultMap, queryDTO);

            // 5. 按距离和活跃度排序
            nearbyUsers.sort((u1, u2) -> {
                // 优先显示在线用户
                if (u1.getIsOnline() != u2.getIsOnline()) {
                    return u2.getIsOnline().compareTo(u1.getIsOnline());
                }
                // VIP用户优先
                if (!u1.getIsVip().equals(u2.getIsVip())) {
                    return u2.getIsVip().compareTo(u1.getIsVip());
                }
                // 按距离排序
                return u1.getDistance().compareTo(u2.getDistance());
            });

            // 6. 限制返回数量
            if (nearbyUsers.size() > queryDTO.getLimit()) {
                nearbyUsers = nearbyUsers.subList(0, queryDTO.getLimit());
            }

            log.info("查询用户{}附近{}公里内的用户，找到{}个", currentUserId, queryDTO.getRadius(), nearbyUsers.size());
            return nearbyUsers;
        } catch (Exception e) {
            log.error("查询用户{}附近用户失败", currentUserId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public UserLocationDTO getUserLocation(Long userId) {
        try {
            String detailKey = USER_LOCATION_DETAIL_KEY + userId;
            Map<Object, Object> locationDetail = redisTemplate.opsForHash().entries(detailKey);
            
            if (locationDetail.isEmpty()) {
                return null;
            }
            
            UserLocationDTO locationDTO = new UserLocationDTO();
            locationDTO.setLongitude(Double.parseDouble(locationDetail.get("longitude").toString()));
            locationDTO.setLatitude(Double.parseDouble(locationDetail.get("latitude").toString()));
            locationDTO.setLocationName((String) locationDetail.get("locationName"));
            locationDTO.setAddress((String) locationDetail.get("address"));
            
            return locationDTO;
        } catch (Exception e) {
            log.error("获取用户{}位置信息失败", userId, e);
            return null;
        }
    }
    
    @Override
    public Boolean removeUserLocation(Long userId) {
        try {
            // 1. 从Redis Geo中移除用户位置
            redisTemplate.opsForGeo().remove(USER_LOCATION_KEY, userId.toString());
            
            // 2. 删除用户详细位置信息
            String detailKey = USER_LOCATION_DETAIL_KEY + userId;
            redisTemplate.delete(detailKey);
            
            log.info("用户{}位置信息删除成功", userId);
            return true;
        } catch (Exception e) {
            log.error("删除用户{}位置信息失败", userId, e);
            return false;
        }
    }
    

    
    /**
     * 批量构建附近用户VO对象
     */
    private List<NearbyUserVO> buildNearbyUsersInBatch(List<Long> userIds,
                                                       Map<Long, GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResultMap,
                                                       NearbyUsersQueryDTO queryDTO) {
        List<NearbyUserVO> nearbyUsers = new ArrayList<>();

        try {
            // 批量获取用户详细信息（这里可以优化为批量接口）
            Map<Long, Map<String, Object>> userDataMap = new HashMap<>();
            for (Long userId : userIds) {
                try {
                    Result userResult = userServiceClient.getUserDetail(userId);
                    if (userResult != null && userResult.getCode() == 200) {
                        Map<String, Object> userData = (Map<String, Object>) userResult.getData();
                        if (userData != null) {
                            userDataMap.put(userId, userData);
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取用户{}详细信息失败", userId, e);
                }
            }

            // 批量获取位置详细信息
            Map<Long, UserLocationDTO> locationMap = new HashMap<>();
            for (Long userId : userIds) {
                UserLocationDTO location = getUserLocation(userId);
                if (location != null) {
                    locationMap.put(userId, location);
                }
            }

            // 构建用户VO对象
            for (Long userId : userIds) {
                GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult = geoResultMap.get(userId);
                if (geoResult == null) continue;

                NearbyUserVO nearbyUser = new NearbyUserVO();
                nearbyUser.setUserId(userId);

                // 设置位置信息
                Point point = geoResult.getContent().getPoint();
                nearbyUser.setLongitude(point.getX());
                nearbyUser.setLatitude(point.getY());
                nearbyUser.setDistance(geoResult.getDistance().getValue());

                // 设置用户基本信息
                Map<String, Object> userData = userDataMap.get(userId);
                if (userData != null) {
                    fillUserBasicInfo(nearbyUser, userData);
                } else {
                    setDefaultUserInfo(nearbyUser, userId);
                }

                // 设置位置详细信息
                UserLocationDTO locationDetail = locationMap.get(userId);
                if (locationDetail != null) {
                    nearbyUser.setLocationName(locationDetail.getLocationName());
                }

                // 设置在线状态和活跃度
                setUserActivityInfo(nearbyUser, userId);

                // 检查是否符合筛选条件
                if (matchesFilter(nearbyUser, queryDTO)) {
                    nearbyUsers.add(nearbyUser);
                }
            }

        } catch (Exception e) {
            log.error("批量构建附近用户信息失败", e);
        }

        return nearbyUsers;
    }

    /**
     * 构建附近用户VO对象
     */
    private NearbyUserVO buildNearbyUserVO(Long userId, GeoResult<RedisGeoCommands.GeoLocation<Object>> result) {
        try {
            NearbyUserVO nearbyUser = new NearbyUserVO();
            nearbyUser.setUserId(userId);

            // 设置位置信息
            Point point = result.getContent().getPoint();
            nearbyUser.setLongitude(point.getX());
            nearbyUser.setLatitude(point.getY());

            // 设置距离
            Distance distance = result.getDistance();
            nearbyUser.setDistance(distance.getValue());

            // 从用户服务获取用户详细信息
            try {
                Result userResult = userServiceClient.getUserDetail(userId);
                log.info("用户{}详细信息查询结果: {}", userId, userResult != null ? userResult.getCode() : "null");

                if (userResult != null && userResult.getCode() == 200) {
                    Map<String, Object> userData = (Map<String, Object>) userResult.getData();
                    if (userData != null) {
                        // 填充用户基本信息
                        nearbyUser.setNickname((String) userData.get("nickname"));
                        nearbyUser.setAvatarUrl((String) userData.get("avatarUrl"));
                        nearbyUser.setGender((Integer) userData.get("gender"));
                        nearbyUser.setIsVip((Integer) userData.get("isVip"));
                        nearbyUser.setSelfIntroduction((String) userData.get("selfIntroduction"));

                        // 计算年龄
                        Object birthdateObj = userData.get("birthdate");
                        if (birthdateObj != null) {
                            try {
                                Date birthdate = (Date) birthdateObj;
                                nearbyUser.setAge(calculateAge(birthdate));
                            } catch (Exception e) {
                                log.warn("用户{}年龄计算失败", userId, e);
                            }
                        }

                        // 设置用户标签（模拟数据，实际应该从数据库获取）
                        nearbyUser.setTags(generateUserTags(userData));
                    } else {
                        log.warn("用户{}数据为空", userId);
                        setDefaultUserInfo(nearbyUser, userId);
                    }
                } else {
                    log.warn("用户{}服务调用失败: {}", userId, userResult != null ? userResult.getMessage() : "null");
                    setDefaultUserInfo(nearbyUser, userId);
                }
            } catch (Exception e) {
                log.error("调用用户服务失败: userId={}", userId, e);
                setDefaultUserInfo(nearbyUser, userId);
            }

            // 获取位置详细信息
            UserLocationDTO locationDetail = getUserLocation(userId);
            if (locationDetail != null) {
                nearbyUser.setLocationName(locationDetail.getLocationName());
            }

            // 设置在线状态（这里可以从Redis或其他地方获取）
            nearbyUser.setIsOnline(false); // 默认离线，可以后续完善
            nearbyUser.setLastActiveTime(new Date()); // 默认当前时间

            return nearbyUser;
        } catch (Exception e) {
            log.error("构建附近用户VO失败，用户ID: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 检查用户是否符合筛选条件
     */
    private boolean matchesFilter(NearbyUserVO user, NearbyUsersQueryDTO queryDTO) {
        // 性别筛选
        if (queryDTO.getGender() != null && queryDTO.getGender() != 0) {
            if (!queryDTO.getGender().equals(user.getGender())) {
                return false;
            }
        }
        
        // 年龄筛选
        if (user.getAge() != null) {
            if (queryDTO.getMinAge() != null && user.getAge() < queryDTO.getMinAge()) {
                return false;
            }
            if (queryDTO.getMaxAge() != null && user.getAge() > queryDTO.getMaxAge()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public Double calculateDistance(Double lon1, Double lat1, Double lon2, Double lat2) {
        if (lon1 == null || lat1 == null || lon2 == null || lat2 == null) {
            return null;
        }

        // 使用Haversine公式计算两点间距离
        final double R = 6371; // 地球半径（公里）

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 返回距离（公里）
    }

    /**
     * 计算年龄 - 支持Date类型
     */
    private Integer calculateAge(Date birthdate) {
        if (birthdate == null) {
            return null;
        }

        Calendar birth = Calendar.getInstance();
        birth.setTime(birthdate);

        Calendar now = Calendar.getInstance();

        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        // 如果今年的生日还没过，年龄减1
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    /**
     * 生成用户标签（模拟数据）
     */
    private List<String> generateUserTags(Map<String, Object> userData) {
        List<String> tags = new ArrayList<>();

        // 根据用户信息生成标签
        Integer gender = (Integer) userData.get("gender");
        if (gender != null) {
            if (gender == 1) {
                tags.add("阳光");
                tags.add("运动");
            } else if (gender == 2) {
                tags.add("温柔");
                tags.add("文艺");
            }
        }

        // 添加一些通用标签
        String[] commonTags = {"友善", "真诚", "有趣", "积极", "乐观", "幽默", "善良", "热情"};
        Random random = new Random();
        for (int i = 0; i < 2; i++) {
            String tag = commonTags[random.nextInt(commonTags.length)];
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }

        return tags;
    }

    /**
     * 填充用户基本信息
     */
    private void fillUserBasicInfo(NearbyUserVO nearbyUser, Map<String, Object> userData) {
        if (userData == null) {
            setDefaultUserInfo(nearbyUser, nearbyUser.getUserId());
            return;
        }

        nearbyUser.setNickname((String) userData.get("nickname"));
        nearbyUser.setAvatarUrl((String) userData.get("avatarUrl"));

        // 安全地获取Integer类型字段
        Object genderObj = userData.get("gender");
        nearbyUser.setGender(genderObj != null ? (Integer) genderObj : 0);

        Object vipObj = userData.get("isVip");
        nearbyUser.setIsVip(vipObj != null ? (Integer) vipObj : 0);

        nearbyUser.setSelfIntroduction((String) userData.get("selfIntroduction"));

        // 计算年龄
        Object birthdateObj = userData.get("birthdate");
        if (birthdateObj != null) {
            try {
                Date birthdate = (Date) birthdateObj;
                nearbyUser.setAge(calculateAge(birthdate));
            } catch (Exception e) {
                log.warn("用户{}年龄计算失败", nearbyUser.getUserId(), e);
            }
        }

        // 设置用户标签
        nearbyUser.setTags(generateUserTags(userData));
    }

    /**
     * 设置用户活跃度信息
     */
    private void setUserActivityInfo(NearbyUserVO nearbyUser, Long userId) {
        // 从Redis获取用户在线状态
        String onlineKey = "user:online:" + userId;
        Boolean isOnline = redisTemplate.hasKey(onlineKey);
        nearbyUser.setIsOnline(isOnline != null && isOnline);

        // 获取最后活跃时间
        String lastActiveKey = "user:last_active:" + userId;
        Object lastActiveObj = redisTemplate.opsForValue().get(lastActiveKey);
        if (lastActiveObj != null) {
            try {
                nearbyUser.setLastActiveTime(new Date(Long.parseLong(lastActiveObj.toString())));
            } catch (Exception e) {
                nearbyUser.setLastActiveTime(new Date());
            }
        } else {
            nearbyUser.setLastActiveTime(new Date());
        }

        // 设置匹配度（基于共同兴趣、年龄差等）
        nearbyUser.setMatchScore(calculateMatchScore(nearbyUser));
    }

    /**
     * 计算匹配度
     */
    private Double calculateMatchScore(NearbyUserVO user) {
        double score = 50.0; // 基础分数

        // VIP用户加分
        if (user.getIsVip() != null && user.getIsVip() == 1) {
            score += 10.0;
        }

        // 在线用户加分
        if (user.getIsOnline() != null && user.getIsOnline()) {
            score += 15.0;
        }

        // 距离越近分数越高
        if (user.getDistance() != null) {
            if (user.getDistance() < 1.0) {
                score += 20.0;
            } else if (user.getDistance() < 5.0) {
                score += 10.0;
            } else if (user.getDistance() < 10.0) {
                score += 5.0;
            }
        }

        // 有自我介绍加分
        if (user.getSelfIntroduction() != null && !user.getSelfIntroduction().isEmpty()
            && !user.getSelfIntroduction().contains("神秘")) {
            score += 5.0;
        }

        return Math.min(score, 100.0); // 最高100分
    }

    /**
     * 设置默认用户信息
     */
    private void setDefaultUserInfo(NearbyUserVO nearbyUser, Long userId) {
        nearbyUser.setNickname("用户" + userId);
        nearbyUser.setAvatarUrl("/static/default-avatar.png");
        nearbyUser.setGender(0);
        nearbyUser.setAge(25);
        nearbyUser.setIsVip(0);
        nearbyUser.setSelfIntroduction("这个人很神秘，没有留下介绍");
        nearbyUser.setTags(Arrays.asList("神秘", "低调"));
        nearbyUser.setMatchScore(30.0); // 默认较低匹配度
    }

    /**
     * 根据出生日期字符串计算年龄
     */
    private Integer calculateAgeFromString(String birthDateStr) {
        try {
            if (birthDateStr == null || birthDateStr.isEmpty()) {
                return null;
            }

            // 解析日期字符串（假设格式为 yyyy-MM-dd）
            String[] parts = birthDateStr.split("-");
            if (parts.length != 3) {
                return null;
            }

            int birthYear = Integer.parseInt(parts[0]);
            int birthMonth = Integer.parseInt(parts[1]);
            int birthDay = Integer.parseInt(parts[2]);

            Calendar birth = Calendar.getInstance();
            birth.set(birthYear, birthMonth - 1, birthDay);

            Calendar now = Calendar.getInstance();

            int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            // 如果还没到生日，年龄减1
            if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age > 0 ? age : null;
        } catch (Exception e) {
            log.warn("计算年龄失败，出生日期: {}", birthDateStr, e);
            return null;
        }
    }

    @Override
    public Boolean sendGreeting(Long fromUserId, Long toUserId, String message) {
        try {
            // 检查是否已经打过招呼（24小时内只能打招呼一次）
            String greetingKey = "greeting:" + fromUserId + ":" + toUserId;
            if (redisTemplate.hasKey(greetingKey)) {
                log.warn("用户{}已经向用户{}打过招呼", fromUserId, toUserId);
                return false;
            }

            // 记录打招呼
            Map<String, Object> greetingData = new HashMap<>();
            greetingData.put("fromUserId", fromUserId);
            greetingData.put("toUserId", toUserId);
            greetingData.put("message", message);
            greetingData.put("timestamp", System.currentTimeMillis());

            redisTemplate.opsForHash().putAll(greetingKey, greetingData);
            redisTemplate.expire(greetingKey, 24, TimeUnit.HOURS);

            // 给目标用户发送通知（这里可以集成消息推送）
            String notificationKey = "notification:greeting:" + toUserId;
            redisTemplate.opsForList().leftPush(notificationKey, greetingData);
            redisTemplate.expire(notificationKey, 7, TimeUnit.DAYS);

            log.info("用户{}向用户{}发送招呼成功", fromUserId, toUserId);
            return true;
        } catch (Exception e) {
            log.error("发送招呼失败", e);
            return false;
        }
    }

    @Override
    public Boolean favoriteUser(Long userId, Long targetUserId) {
        try {
            String favoriteKey = "user:favorites:" + userId;
            redisTemplate.opsForSet().add(favoriteKey, targetUserId.toString());
            redisTemplate.expire(favoriteKey, 30, TimeUnit.DAYS);

            log.info("用户{}收藏用户{}成功", userId, targetUserId);
            return true;
        } catch (Exception e) {
            log.error("收藏用户失败", e);
            return false;
        }
    }

    @Override
    public Boolean unfavoriteUser(Long userId, Long targetUserId) {
        try {
            String favoriteKey = "user:favorites:" + userId;
            redisTemplate.opsForSet().remove(favoriteKey, targetUserId.toString());

            log.info("用户{}取消收藏用户{}成功", userId, targetUserId);
            return true;
        } catch (Exception e) {
            log.error("取消收藏用户失败", e);
            return false;
        }
    }

    @Override
    public List<NearbyUserVO> getFavoriteUsers(Long userId) {
        try {
            String favoriteKey = "user:favorites:" + userId;
            Set<Object> favoriteUserIds = redisTemplate.opsForSet().members(favoriteKey);

            if (favoriteUserIds == null || favoriteUserIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> userIds = favoriteUserIds.stream()
                    .map(id -> Long.parseLong(id.toString()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            // 构建收藏用户信息（简化版，不包含地理位置）
            List<NearbyUserVO> favoriteUsers = new ArrayList<>();
            for (Long targetUserId : userIds) {
                try {
                    Result userResult = userServiceClient.getUserDetail(targetUserId);
                    if (userResult != null && userResult.getCode() == 200) {
                        Map<String, Object> userData = (Map<String, Object>) userResult.getData();
                        if (userData != null) {
                            NearbyUserVO user = new NearbyUserVO();
                            user.setUserId(targetUserId);
                            fillUserBasicInfo(user, userData);
                            user.setIsFollowed(true); // 收藏的用户标记为已关注
                            favoriteUsers.add(user);
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取收藏用户{}信息失败", targetUserId, e);
                }
            }

            return favoriteUsers;
        } catch (Exception e) {
            log.error("获取收藏用户列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Boolean recordVisit(Long visitorId, Long visitedId) {
        try {
            String visitKey = "user:visits:" + visitedId;
            Map<String, Object> visitData = new HashMap<>();
            visitData.put("visitorId", visitorId);
            visitData.put("timestamp", System.currentTimeMillis());

            redisTemplate.opsForList().leftPush(visitKey, visitData);
            // 只保留最近100次访问记录
            redisTemplate.opsForList().trim(visitKey, 0, 99);
            redisTemplate.expire(visitKey, 30, TimeUnit.DAYS);

            log.info("记录用户{}访问用户{}成功", visitorId, visitedId);
            return true;
        } catch (Exception e) {
            log.error("记录用户访问失败", e);
            return false;
        }
    }

    @Override
    public Boolean updateUserOnlineStatus(Long userId, Boolean isOnline) {
        try {
            String onlineKey = "user:online:" + userId;
            String lastActiveKey = "user:last_active:" + userId;

            if (isOnline) {
                redisTemplate.opsForValue().set(onlineKey, "1", 5, TimeUnit.MINUTES);
            } else {
                redisTemplate.delete(onlineKey);
            }

            // 更新最后活跃时间
            redisTemplate.opsForValue().set(lastActiveKey, System.currentTimeMillis(), 7, TimeUnit.DAYS);

            log.info("更新用户{}在线状态为{}成功", userId, isOnline);
            return true;
        } catch (Exception e) {
            log.error("更新用户在线状态失败", e);
            return false;
        }
    }

}
