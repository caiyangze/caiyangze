package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhentao.pojo.TbMatchmaker;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.TbMatchmakerService;
import com.zhentao.service.TbUserService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 红娘信息查询控制器
 * @author zhentao
 */
@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class MatchmakerInfoController {
    
    @Autowired
    private TbMatchmakerService matchmakerService;

    @Autowired
    private TbUserService userService;
    
    /**
     * 根据红娘ID获取红娘信息
     * @param matchmakerId 红娘ID
     * @return 红娘信息
     */
    @GetMapping("/matchmaker/{matchmakerId}")
    public Result getMatchmakerInfo(@PathVariable Long matchmakerId) {
        try {
            TbMatchmaker matchmaker = matchmakerService.getById(matchmakerId);
            if (matchmaker == null) {
                return Result.ERROR("红娘信息不存在");
            }
            
            return Result.OK(matchmaker);
        } catch (Exception e) {
            return Result.ERROR("获取红娘信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/info/{userId}")
    public Result getUserInfo(@PathVariable Long userId) {
        try {
            TbUser user = userService.getById(userId);
            if (user == null) {
                return Result.ERROR("用户信息不存在");
            }

            // 清除敏感信息
            user.setPassword(null);

            return Result.OK(user);
        } catch (Exception e) {
            return Result.ERROR("获取用户信息失败：" + e.getMessage());
        }
    }
}
