package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.TbUserService;
import com.zhentao.mapper.TbUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author cyz
* @description 针对表【tb_user】的数据库操作Service实现
* @createDate 2025-08-05 10:43:11
*/
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser>
    implements TbUserService{

    @Override
    public List<TbUser> findRecommendedUsers(Long currentUserId, Integer targetGender, String userLocation, Integer limit) {
        QueryWrapper<TbUser> queryWrapper = buildUserQueryWrapper(currentUserId, targetGender);

        // 排序策略：优先推荐高质量活跃用户
        queryWrapper.orderByDesc("is_vip")
                   .orderByDesc("is_verified")
                   .orderByDesc("last_login_time")
                   .orderByDesc("count_like")
                   .orderByDesc("created_at");

        // 限制查询数量，获取候选用户用于算法筛选
        int candidateLimit = (limit != null && limit > 0) ? limit * 3 : 30;
        queryWrapper.last("LIMIT " + candidateLimit);

        return this.list(queryWrapper);
    }

    /**
     * 构建用户查询条件
     */
    private QueryWrapper<TbUser> buildUserQueryWrapper(Long currentUserId, Integer targetGender) {
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();

        // 基础条件
        queryWrapper.eq("account_status", 1); // 只查询正常状态用户

        if (currentUserId != null) {
            queryWrapper.ne("user_id", currentUserId); // 排除当前用户
        }

        if (targetGender != null) {
            queryWrapper.eq("gender", targetGender); // 筛选目标性别
        }

        return queryWrapper;
    }

    @Override
    public TbUser getUserDetailById(Long userId) {
        if (userId == null) {
            return null;
        }
        return this.getOne(buildUserQueryWrapper(null, null).eq("user_id", userId));
    }

    @Override
    public List<TbUser> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return this.list(buildUserQueryWrapper(null, null).in("user_id", userIds));
    }

}




