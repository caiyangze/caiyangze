package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhentao.mapper.TbFollowMapper;
import com.zhentao.pojo.TbFollow;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.TbFollowService;
import com.zhentao.service.TbUserService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import com.zhentao.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关注控制器
 *
 * @author zhentao
 */
@RestController
@RequestMapping("/follow")
@Validated
public class FollowController {

    @Autowired
    private TbFollowService followService;

    @Autowired
    private TbUserService userService;

    @Autowired
    private TbFollowMapper followMapper;

    /**
     * 获取互相关注的好友列表
     */
    @GetMapping("/mutual")
    public Result getMutualFollowList() {
        Long currentUserId = ThreadLocalUtil.getCurrentUserId();
        if (currentUserId == null) {
            return Result.ERROR("用户未登录");
        }

        try {
            // 使用Mapper查询互相关注的用户ID列表
            List<Map<String, Object>> mutualFollowData = followMapper.selectMutualFollows(currentUserId);

            if (mutualFollowData.isEmpty()) {
                return Result.success("暂无互相关注的好友", new ArrayList<>());
            }

            // 提取用户ID列表
            List<Long> userIds = mutualFollowData.stream()
                    .map(data -> (Long) data.get("other_user_id"))
                    .collect(Collectors.toList());

            // 批量查询用户信息
            List<TbUser> mutualFriends = userService.listByIds(userIds);

            // 为了保护隐私，移除敏感信息
            List<Map<String, Object>> friendList = mutualFriends.stream()
                    .map(user -> {
                        Map<String, Object> friendInfo = new HashMap<>();
                        friendInfo.put("userId", user.getUserId());
                        friendInfo.put("nickname", user.getNickname());
                        friendInfo.put("avatarUrl", user.getAvatarUrl());
                        friendInfo.put("gender", user.getGender());
                        friendInfo.put("isVip", user.getIsVip());
                        friendInfo.put("userRole", user.getUserRole());

                        // 添加关注时间信息
                        mutualFollowData.stream()
                                .filter(data -> user.getUserId().equals(data.get("other_user_id")))
                                .findFirst()
                                .ifPresent(data -> friendInfo.put("followTime", data.get("follow_time")));

                        return friendInfo;
                    })
                    .collect(Collectors.toList());

            return Result.success("获取互相关注好友列表成功", friendList);

        } catch (Exception e) {
            return Result.ERROR("获取互相关注好友列表失败：" + e.getMessage());
        }
    }
    /**
     * 关注/取消关注用户
     */
    @RequestMapping("addAndCancel")
    public Result addFollow(@RequestParam("followedUserId") Integer followedUserId, @RequestHeader("token") String token){
        // 参数验证
        if (followedUserId == null) {
            return Result.ERROR("被关注用户ID不能为空");
        }

        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }
        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = (Integer) claimsMap.get("userId");

        // 防止自己关注自己
        if (userId.equals(followedUserId)) {
            return Result.ERROR("不能关注自己");
        }

        QueryWrapper<TbFollow> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("user_id", userId);
        followQueryWrapper.eq("followed_user_id", followedUserId);
        TbFollow tbFollow = followService.getOne(followQueryWrapper);

        if (tbFollow != null) {
            // 如果已存在关注记录，切换状态
            if(tbFollow.getFollowStatus() == 1) {
                // 当前是关注状态，改为取消关注
                tbFollow.setFollowStatus(0);
                tbFollow.setUpdatedAt(new Date());
                followService.updateById(tbFollow);
                //被关注用户粉丝数减一
                TbUser byId = userService.getById(followedUserId);
                byId.setFan(byId.getFan() - 1);
                userService.updateById(byId);
                //关注用户关注数减一
                TbUser user = userService.getById(userId);
                user.setCountFollow(user.getCountFollow() - 1);
                userService.updateById(user);
                return Result.OK("取消关注");
            } else {
                // 当前是取消关注状态，改为关注
                tbFollow.setFollowStatus(1);
                tbFollow.setUpdatedAt(new Date());
                followService.updateById(tbFollow);
                //被关注用户分数加一
                TbUser byId = userService.getById(followedUserId);
                byId.setFan(byId.getFan() + 1);
                userService.updateById(byId);
                //关注用户关注数加一
                TbUser user = userService.getById(userId);
                user.setCountFollow(user.getCountFollow() + 1);
                userService.updateById(user);
                return Result.OK("已关注");
            }
        } else {
            // 如果不存在关注记录，创建新的关注记录
            TbFollow follow = new TbFollow();
            follow.setUserId(Long.valueOf(userId));
            follow.setFollowedUserId(Long.valueOf(followedUserId));
            follow.setFollowStatus(1);
            follow.setCreatedAt(new Date());
            follow.setUpdatedAt(new Date());
            boolean save = followService.save(follow);
            if (save) {
                //被关注用户分数加一
                TbUser byId = userService.getById(followedUserId);
                byId.setFan(byId.getFan() + 1);
                userService.updateById(byId);
                //关注用户关注数加一
                TbUser user = userService.getById(userId);
                user.setCountFollow(user.getCountFollow() + 1);
                userService.updateById(user);
                return Result.OK("已关注");
            }
            return Result.ERROR("关注失败");
        }
    }

    /**
     * 检查是否关注某用户
     */
    @RequestMapping("isFollow")
    public Result isFollow(@RequestParam("followedUserId") Integer followedUserId, @RequestHeader("token") String token){
        // 参数验证
        if (followedUserId == null) {
            return Result.ERROR("被关注用户ID不能为空");
        }

        if (JwtService.verifyToken(token) != 1) {
            return Result.NO_LOGIN();
        }

        Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
        Integer userId = (Integer) claimsMap.get("userId");
        QueryWrapper<TbFollow> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("user_id", userId);
        followQueryWrapper.eq("followed_user_id", followedUserId);
        TbFollow tbFollow = followService.getOne(followQueryWrapper);
        if (tbFollow != null && tbFollow.getFollowStatus() == 1) {
            return Result.OK(true);
        }
        return Result.OK(false);
    }

}
