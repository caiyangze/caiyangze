package com.zhentao.service;

import com.zhentao.dto.ProfileDTO;
import com.zhentao.utils.Result;

import java.util.Map;

/**
 * 用户资料服务接口
 *
 * @author zhentao
 */
public interface ProfileService {
    
    /**
     * 创建用户资料
     *
     * @param profileDTO 用户资料DTO
     * @return 创建结果
     */
    Result createProfile(ProfileDTO profileDTO);
    
    /**
     * 更新用户资料
     *
     * @param profileDTO 用户资料DTO
     * @return 更新结果
     */
    Result updateProfile(ProfileDTO profileDTO);
    
    /**
     * 获取用户资料详情（包含用户基本信息）
     *
     * @param profileId 资料ID
     * @return 用户资料详情
     */
    Result getProfileDetailWithUser(Long profileId);
    
    /**
     * 根据用户ID获取资料
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    Result getProfileByUserId(Long userId);
    
    /**
     * 删除用户资料
     *
     * @param profileId 资料ID
     * @return 删除结果
     */
    Result deleteProfile(Long profileId);
    
    /**
     * 分页查询用户资料列表（包含用户基本信息）
     *
     * @param page 页码
     * @param size 每页数量
     * @return 用户资料列表
     */
    Result listProfilesWithUserInfo(Integer page, Integer size);
    
    /**
     * 条件查询用户资料
     *
     * @param condition 查询条件
     * @return 符合条件的用户资料
     */
    Result searchProfiles(Map<String, Object> condition);
    
    /**
     * 验证资料是否属于指定用户
     *
     * @param profileId 资料ID
     * @param userId 用户ID
     * @return 是否属于该用户
     */
    boolean isProfileOwner(Long profileId, Long userId);
} 