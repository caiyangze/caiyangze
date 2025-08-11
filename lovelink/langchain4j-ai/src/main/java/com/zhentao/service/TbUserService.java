package com.zhentao.service;

import com.zhentao.pojo.TbUser;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author cyz
* @description 针对表【tb_user】的数据库操作Service
* @createDate 2025-08-05 10:43:11
*/
public interface TbUserService extends IService<TbUser> {

    /**
     * 根据条件推荐异性用户
     * @param currentUserId 当前用户ID（排除自己）
     * @param targetGender 目标性别（0-女，1-男）
     * @param userLocation 用户地区（可选，用于地区匹配）
     * @param limit 推荐数量限制
     * @return 推荐的用户列表
     */
    List<TbUser> findRecommendedUsers(Long currentUserId, Integer targetGender, String userLocation, Integer limit);

    /**
     * 根据用户ID获取用户详细信息（包含统计数据）
     * @param userId 用户ID
     * @return 用户详细信息
     */
    TbUser getUserDetailById(Long userId);

    /**
     * 批量获取用户基本信息
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    List<TbUser> getUsersByIds(List<Long> userIds);

}
