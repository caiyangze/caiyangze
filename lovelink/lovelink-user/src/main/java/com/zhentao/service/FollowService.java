package com.zhentao.service;

import com.zhentao.utils.Result;

/**
 * 关注服务接口
 * 
 * @author zhentao
 */
public interface FollowService {
    
    /**
     * 获取互相关注的好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    Result getMutualFollowList(Long userId);
    
    /**
     * 关注用户
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    Result followUser(Long userId, Long targetUserId);
    
    /**
     * 取消关注
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 操作结果
     */
    Result unfollowUser(Long userId, Long targetUserId);
    
    /**
     * 检查是否关注某用户
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 是否关注
     */
    Result checkFollow(Long userId, Long targetUserId);
    
    /**
     * 获取关注列表
     * 
     * @param userId 用户ID
     * @return 关注列表
     */
    Result getFollowingList(Long userId);
    
    /**
     * 获取粉丝列表
     * 
     * @param userId 用户ID
     * @return 粉丝列表
     */
    Result getFollowersList(Long userId);
}
