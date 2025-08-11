package com.zhentao.service;

import com.zhentao.dto.NearbyUsersQueryDTO;
import com.zhentao.dto.UserLocationDTO;
import com.zhentao.vo.NearbyUserVO;

import java.util.List;

/**
 * 地理位置服务接口
 * @author zhentao
 * @date 2025/8/3
 */
public interface LocationService {
    
    /**
     * 更新用户位置信息
     * @param userId 用户ID
     * @param locationDTO 位置信息
     * @return 是否更新成功
     */
    Boolean updateUserLocation(Long userId, UserLocationDTO locationDTO);
    
    /**
     * 查询附近的用户
     * @param currentUserId 当前用户ID
     * @param queryDTO 查询条件
     * @return 附近用户列表
     */
    List<NearbyUserVO> getNearbyUsers(Long currentUserId, NearbyUsersQueryDTO queryDTO);
    
    /**
     * 获取用户当前位置信息
     * @param userId 用户ID
     * @return 用户位置信息
     */
    UserLocationDTO getUserLocation(Long userId);
    
    /**
     * 删除用户位置信息
     * @param userId 用户ID
     * @return 是否删除成功
     */
    Boolean removeUserLocation(Long userId);

    /**
     * 计算两点之间的距离
     * @param lon1 经度1
     * @param lat1 纬度1
     * @param lon2 经度2
     * @param lat2 纬度2
     * @return 距离（公里）
     */
    Double calculateDistance(Double lon1, Double lat1, Double lon2, Double lat2);

    /**
     * 打招呼
     * @param fromUserId 发起用户ID
     * @param toUserId 目标用户ID
     * @param message 招呼消息
     * @return 是否成功
     */
    Boolean sendGreeting(Long fromUserId, Long toUserId, String message);

    /**
     * 收藏用户
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    Boolean favoriteUser(Long userId, Long targetUserId);

    /**
     * 取消收藏用户
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否成功
     */
    Boolean unfavoriteUser(Long userId, Long targetUserId);

    /**
     * 获取收藏的用户列表
     * @param userId 用户ID
     * @return 收藏的用户列表
     */
    List<NearbyUserVO> getFavoriteUsers(Long userId);

    /**
     * 记录用户访问
     * @param visitorId 访问者ID
     * @param visitedId 被访问者ID
     * @return 是否成功
     */
    Boolean recordVisit(Long visitorId, Long visitedId);

    /**
     * 更新用户在线状态
     * @param userId 用户ID
     * @param isOnline 是否在线
     * @return 是否成功
     */
    Boolean updateUserOnlineStatus(Long userId, Boolean isOnline);

}
