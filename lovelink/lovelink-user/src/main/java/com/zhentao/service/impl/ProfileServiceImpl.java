package com.zhentao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.dto.ProfileDTO;
import com.zhentao.mapper.ProfileMapper;
import com.zhentao.mapper.UserMapper;
import com.zhentao.pojo.TbUserProfile;
import com.zhentao.pojo.TbUser;
import com.zhentao.service.ProfileService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户资料服务实现类
 *
 * @author zhentao
 */
@Service
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileMapper profileMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 创建用户资料
     *
     * @param profileDTO 用户资料DTO
     * @return 创建结果
     */
    @Override
    @Transactional
    public Result createProfile(ProfileDTO profileDTO) {
        // 检查用户是否已有资料
        LambdaQueryWrapper<TbUserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbUserProfile::getUserId, profileDTO.getUserId());
        TbUserProfile existProfile = profileMapper.selectOne(queryWrapper);
        
        if (existProfile != null) {
            return Result.ERROR("用户资料已存在，请使用更新接口");
        }
        
        // 创建新资料
        TbUserProfile profile = new TbUserProfile();
        BeanUtils.copyProperties(profileDTO, profile);
        
        // 设置创建和更新时间
        Date now = new Date();
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        
        // 保存到数据库
        profileMapper.insert(profile);
        
        return Result.OK(profile);
    }
    
    /**
     * 更新用户资料
     *
     * @param profileDTO 用户资料DTO
     * @return 更新结果
     */
    @Override
    @Transactional
    public Result updateProfile(ProfileDTO profileDTO) {
        // 查询资料是否存在
        TbUserProfile profile = profileMapper.selectById(profileDTO.getProfileId());
        if (profile == null) {
            return Result.ERROR("资料不存在");
        }
        
        // 更新资料
        BeanUtils.copyProperties(profileDTO, profile);
        profile.setUpdatedAt(new Date());
        
        // 保存到数据库
        profileMapper.updateById(profile);
        
        return Result.OK(profile);
    }
    
    /**
     * 获取用户资料详情（包含用户基本信息）
     *
     * @param profileId 资料ID
     * @return 用户资料详情
     */
    @Override
    public Result getProfileDetailWithUser(Long profileId) {
        QueryWrapper<TbUserProfile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", profileId);
        TbUserProfile tbUserProfile = profileMapper.selectOne(queryWrapper);
        Long pid = tbUserProfile.getProfileId();
        // 查询资料
        TbUserProfile profile = profileMapper.selectById(pid);
        if (profile == null) {
            return Result.ERROR("资料不存在");
        }
        
        // 查询用户基本信息
        TbUser user = userMapper.selectById(profile.getUserId());
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 清除敏感信息
        user.setPassword(null);
        
        // 组合用户资料和基本信息
        Map<String, Object> result = new HashMap<>();
        result.put("profile", profile);
        result.put("user", user);
        
        return Result.OK(result);
    }
    
    /**
     * 根据用户ID获取资料
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    @Override
    public Result getProfileByUserId(Long userId) {
        // 查询资料
        LambdaQueryWrapper<TbUserProfile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbUserProfile::getUserId, userId);
        TbUserProfile profile = profileMapper.selectOne(queryWrapper);
        
        if (profile == null) {
            return Result.ERROR("用户资料不存在");
        }
        
        // 查询用户基本信息
        TbUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.ERROR("用户不存在");
        }
        
        // 清除敏感信息
        user.setPassword(null);
        
        // 组合用户资料和基本信息
        Map<String, Object> result = new HashMap<>();
        result.put("profile", profile);
        result.put("user", user);
        
        return Result.OK(result);
    }
    
    /**
     * 删除用户资料
     *
     * @param profileId 资料ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public Result deleteProfile(Long profileId) {
        // 查询资料是否存在
        TbUserProfile profile = profileMapper.selectById(profileId);
        if (profile == null) {
            return Result.ERROR("资料不存在");
        }
        
        // 删除资料
        profileMapper.deleteById(profileId);
        
        return Result.OK("删除成功");
    }
    
    /**
     * 分页查询用户资料列表（包含用户基本信息）
     *
     * @param page 页码
     * @param size 每页数量
     * @return 用户资料列表
     */
    @Override
    public Result listProfilesWithUserInfo(Integer page, Integer size) {
        // 创建分页对象
        IPage<TbUserProfile> pageInfo = new Page<>(page, size);
        
        // 查询资料列表
        profileMapper.selectPage(pageInfo, null);
        
        // 查询对应的用户信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TbUserProfile profile : pageInfo.getRecords()) {
            TbUser user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                
                // 组合用户资料和基本信息
                Map<String, Object> result = new HashMap<>();
                result.put("profile", profile);
                result.put("user", user);
                
                resultList.add(result);
            }
        }
        
        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", pageInfo.getTotal());
        resultMap.put("pages", pageInfo.getPages());
        resultMap.put("current", pageInfo.getCurrent());
        resultMap.put("size", pageInfo.getSize());
        resultMap.put("records", resultList);
        
        return Result.OK(resultMap);
    }
    
    /**
     * 条件查询用户资料
     *
     * @param condition 查询条件
     * @return 符合条件的用户资料
     */
    @Override
    public Result searchProfiles(Map<String, Object> condition) {
        // 构建查询条件
        LambdaQueryWrapper<TbUserProfile> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据条件添加查询参数
        if (condition.containsKey("education")) {
            queryWrapper.eq(TbUserProfile::getEducation, condition.get("education"));
        }
        
        if (condition.containsKey("maritalStatus")) {
            queryWrapper.eq(TbUserProfile::getMaritalStatus, condition.get("maritalStatus"));
        }
        
        if (condition.containsKey("hasChildren")) {
            queryWrapper.eq(TbUserProfile::getHasChildren, condition.get("hasChildren"));
        }
        
        if (condition.containsKey("workCity")) {
            queryWrapper.like(TbUserProfile::getWorkCity, condition.get("workCity"));
        }
        
        if (condition.containsKey("hometown")) {
            queryWrapper.like(TbUserProfile::getHometown, condition.get("hometown"));
        }
        
        if (condition.containsKey("incomeLevel")) {
            queryWrapper.ge(TbUserProfile::getIncomeLevel, condition.get("incomeLevel"));
        }
        
        if (condition.containsKey("houseStatus")) {
            queryWrapper.eq(TbUserProfile::getHouseStatus, condition.get("houseStatus"));
        }
        
        if (condition.containsKey("carStatus")) {
            queryWrapper.eq(TbUserProfile::getCarStatus, condition.get("carStatus"));
        }
        
        // 分页参数
        Integer page = condition.containsKey("page") ? (Integer) condition.get("page") : 1;
        Integer size = condition.containsKey("size") ? (Integer) condition.get("size") : 10;
        
        // 创建分页对象
        IPage<TbUserProfile> pageInfo = new Page<>(page, size);
        
        // 查询资料列表
        profileMapper.selectPage(pageInfo, queryWrapper);
        
        // 查询对应的用户信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (TbUserProfile profile : pageInfo.getRecords()) {
            TbUser user = userMapper.selectById(profile.getUserId());
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                
                // 组合用户资料和基本信息
                Map<String, Object> result = new HashMap<>();
                result.put("profile", profile);
                result.put("user", user);
                
                resultList.add(result);
            }
        }
        
        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", pageInfo.getTotal());
        resultMap.put("pages", pageInfo.getPages());
        resultMap.put("current", pageInfo.getCurrent());
        resultMap.put("size", pageInfo.getSize());
        resultMap.put("records", resultList);
        
        return Result.OK(resultMap);
    }
    
    /**
     * 验证资料是否属于指定用户
     *
     * @param profileId 资料ID
     * @param userId 用户ID
     * @return 是否属于该用户
     */
    @Override
    public boolean isProfileOwner(Long profileId, Long userId) {
        // 查询资料
        TbUserProfile profile = profileMapper.selectById(profileId);
        if (profile == null) {
            return false;
        }
        
        // 验证所有权
        return profile.getUserId().equals(Long.valueOf(userId));
    }
} 