package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.mapper.TbFollowMapper;
import com.zhentao.mapper.TbUserMapper;
import com.zhentao.pojo.TbFollow;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.FollowService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注服务实现类
 * 
 * @author zhentao
 */
@Service
public class FollowServiceImpl implements FollowService {
    
    @Autowired
    private TbFollowMapper followMapper;
    
    @Autowired
    private TbUserMapper userMapper;
    
    @Override
    public Result getMutualFollowList(Long userId) {
        try {
            // 查询互相关注的用户
            List<Map<String, Object>> mutualFollows = followMapper.selectMutualFollows(userId);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> follow : mutualFollows) {
                Long otherUserId = (Long) follow.get("other_user_id");
                
                // 获取用户基本信息
                TbUser user = userMapper.selectById(otherUserId);
                if (user != null) {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getUserId());
                    userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "用户" + user.getUserId());
                    userInfo.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "/static/message/default-avatar.png");
                    userInfo.put("gender", user.getGender());
                    userInfo.put("followTime", follow.get("follow_time"));
                    result.add(userInfo);
                }
            }
            
            return Result.OK(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ERROR("获取好友列表失败");
        }
    }
    
    @Override
    @Transactional
    public Result followUser(Long userId, Long targetUserId) {
        try {
            if (userId.equals(targetUserId)) {
                return Result.ERROR("不能关注自己");
            }
            
            // 检查目标用户是否存在
            TbUser targetUser = userMapper.selectById(targetUserId);
            if (targetUser == null) {
                return Result.ERROR("用户不存在");
            }
            
            // 检查是否已经关注
            QueryWrapper<TbFollow> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("followed_user_id", targetUserId);
            TbFollow existingFollow = followMapper.selectOne(queryWrapper);
            
            if (existingFollow != null) {
                if (existingFollow.getFollowStatus() == 1) {
                    return Result.ERROR("已经关注过该用户");
                } else {
                    // 重新关注
                    existingFollow.setFollowStatus(1);
                    followMapper.updateById(existingFollow);
                }
            } else {
                // 新增关注记录
                TbFollow follow = new TbFollow();
                follow.setUserId(userId);
                follow.setFollowedUserId(targetUserId);
                follow.setFollowStatus(1);
                followMapper.insert(follow);
            }
            
            return Result.OK("关注成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ERROR("关注失败");
        }
    }
    
    @Override
    @Transactional
    public Result unfollowUser(Long userId, Long targetUserId) {
        try {
            QueryWrapper<TbFollow> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("followed_user_id", targetUserId)
                       .eq("follow_status", 1);
            
            TbFollow follow = followMapper.selectOne(queryWrapper);
            if (follow == null) {
                return Result.ERROR("未关注该用户");
            }
            
            // 取消关注
            follow.setFollowStatus(0);
            followMapper.updateById(follow);
            
            return Result.OK("取消关注成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ERROR("取消关注失败");
        }
    }
    
    @Override
    public Result checkFollow(Long userId, Long targetUserId) {
        try {
            QueryWrapper<TbFollow> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("followed_user_id", targetUserId)
                       .eq("follow_status", 1);
            
            TbFollow follow = followMapper.selectOne(queryWrapper);
            boolean isFollowing = follow != null;

            Map<String, Object> result = new HashMap<>();
            result.put("isFollowing", isFollowing);
            return Result.OK(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ERROR("检查关注状态失败");
        }
    }
    
    @Override
    public Result getFollowingList(Long userId) {
        try {
            QueryWrapper<TbFollow> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("follow_status", 1)
                       .orderByDesc("created_at");
            
            List<TbFollow> follows = followMapper.selectList(queryWrapper);

            List<Long> userIds = new ArrayList<>();
            for (TbFollow follow : follows) {
                userIds.add(follow.getFollowedUserId());
            }
            
            if (userIds.isEmpty()) {
                return Result.OK(new ArrayList<>());
            }
            
            List<TbUser> users = userMapper.selectBatchIds(userIds);
            List<Map<String, Object>> result = new ArrayList<>();
            for (TbUser user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "用户" + user.getUserId());
                userInfo.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "/static/message/default-avatar.png");
                userInfo.put("gender", user.getGender());
                result.add(userInfo);
            }
            
            return Result.OK(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ERROR("获取关注列表失败");
        }
    }
    
    @Override
    public Result getFollowersList(Long userId) {
        try {
            QueryWrapper<TbFollow> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("followed_user_id", userId)
                       .eq("follow_status", 1)
                       .orderByDesc("created_at");
            
            List<TbFollow> follows = followMapper.selectList(queryWrapper);

            List<Long> userIds = new ArrayList<>();
            for (TbFollow follow : follows) {
                userIds.add(follow.getUserId());
            }
            
            if (userIds.isEmpty()) {
                return Result.OK(new ArrayList<>());
            }
            
            List<TbUser> users = userMapper.selectBatchIds(userIds);
            List<Map<String, Object>> result = new ArrayList<>();
            for (TbUser user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "用户" + user.getUserId());
                userInfo.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "/static/message/default-avatar.png");
                userInfo.put("gender", user.getGender());
                result.add(userInfo);
            }
            
            return Result.OK(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.ERROR("获取粉丝列表失败");
        }
    }
}
