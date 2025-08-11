package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserProfile;
import com.zhentao.pojo.TbUserPhoto;
import com.zhentao.service.TbUserService;
import com.zhentao.service.TbUserProfileService;
import com.zhentao.service.TbUserPhotoService;
import com.zhentao.utils.PasswordEncoder;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理控制器
 * @author 王恒飞
 * @date 2025/7/14
 * @ClassName AdminUserController
 */
@RestController
@RequestMapping("/api/user/admin")
public class AdminUserController {
    
    @Autowired
    private TbUserService userService;
    
    @Autowired
    private TbUserProfileService userProfileService;
    
    @Autowired
    private TbUserPhotoService userPhotoService;
    
    /**
     * 分页查询用户列表
     * 
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键词(用户名/手机号)
     * @param userRole 用户角色
     * @param status 账号状态
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userRole,
            @RequestParam(required = false) Integer status
    ) {
        // 构建查询条件
        LambdaQueryWrapper<TbUser> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加关键词搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(TbUser::getNickname, keyword)
                    .or()
                    .like(TbUser::getPhone, keyword);
        }
        
        // 添加角色筛选条件
        if (userRole != null) {
            queryWrapper.eq(TbUser::getUserRole, userRole);
        }
        
        // 添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(TbUser::getAccountStatus, status);
        }
        
        // 按注册时间降序排序
        queryWrapper.orderByDesc(TbUser::getRegisterTime);
        
        // 执行分页查询
        Page<TbUser> userPage = userService.page(new Page<>(page, size), queryWrapper);
        
        return Result.OK(userPage);
    }
    
    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    public Result getUserDetail(@PathVariable Long userId) {
        TbUser user = userService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 清除敏感信息
        user.setPassword(null);
        
        return Result.OK(user);
    }
    
    /**
     * 获取用户个人资料
     * 
     * @param userId 用户ID
     * @return 用户个人资料
     */
    @GetMapping("/{userId}/profile")
    public Result getUserProfile(@PathVariable Long userId) {
        // 首先检查用户是否存在
        TbUser user = userService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 查询用户个人资料
        TbUserProfile userProfile = userProfileService.getOne(
            new LambdaQueryWrapper<TbUserProfile>().eq(TbUserProfile::getUserId, userId)
        );
        
        // 如果个人资料不存在，返回null而不是错误
        if (userProfile == null) {
            return Result.OK(null);
        }
        
        return Result.OK(userProfile);
    }
    
    /**
     * 获取用户相册
     * 
     * @param userId 用户ID
     * @return 用户相册列表
     */
    @GetMapping("/{userId}/photos")
    public Result getUserPhotos(@PathVariable Long userId) {
        // 首先检查用户是否存在
        TbUser user = userService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 查询用户相册
        List<TbUserPhoto> photos = userPhotoService.list(
            new LambdaQueryWrapper<TbUserPhoto>()
                .eq(TbUserPhoto::getUserId, userId)
                .orderByDesc(TbUserPhoto::getUpdatedAt)
        );
        
        return Result.OK(photos);
    }
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 新状态 (0-冻结，1-正常)
     * @return 操作结果
     */
    @PutMapping("/{userId}/status")
    public Result updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status
    ) {
        TbUser user = userService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 验证状态值是否合法
        if (status != 0 && status != 1) {
            return Result.ERROR("状态值无效");
        }
        
        user.setAccountStatus(status);
        user.setUpdatedAt(new Date());
        
        boolean success = userService.updateById(user);
        if (success) {
            return Result.OK("用户状态更新成功");
        } else {
            return Result.ERROR("用户状态更新失败");
        }
    }
    
    /**
     * 重置用户密码
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{userId}/reset-password")
    public Result resetPassword(@PathVariable Long userId) {
        TbUser user = userService.getById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 默认密码可以是手机号后6位或其他规则
        String defaultPassword = "123456";
        // 使用密码加密工具类对密码进行加密
        String encryptedPassword = PasswordEncoder.encode(defaultPassword);
        user.setPassword(encryptedPassword);
        user.setUpdatedAt(new Date());
        
        boolean success = userService.updateById(user);
        if (success) {
            return Result.OK("密码重置成功");
        } else {
            return Result.ERROR("密码重置失败");
        }
    }
    
    /**
     * 统计用户数据
     * 
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总用户数
        long totalUsers = userService.count();
        statistics.put("totalUsers", totalUsers);
        
        // VIP用户数
        long vipUsers = userService.count(new LambdaQueryWrapper<TbUser>()
                .eq(TbUser::getIsVip, 1));
        statistics.put("vipUsers", vipUsers);
        
        // 冻结用户数
        long frozenUsers = userService.count(new LambdaQueryWrapper<TbUser>()
                .eq(TbUser::getAccountStatus, 0));
        statistics.put("frozenUsers", frozenUsers);
        
        // 今日新增
        long todayNewUsers = userService.count(new LambdaQueryWrapper<TbUser>()
                .ge(TbUser::getRegisterTime, new Date())); // 这里实际应该使用日期工具计算今天凌晨
        statistics.put("todayNewUsers", todayNewUsers);
        
        return Result.OK(statistics);
    }
}
