package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.pojo.TbTag;
import com.zhentao.pojo.TbUserTag;
import com.zhentao.service.TbTagService;
import com.zhentao.service.TbUserTagService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签控制器
 *
 * @author zhentao
 */
@RestController
@RequestMapping("/api/user/adminTag")
@CrossOrigin("*")
@Slf4j
public class TagController {

    @Autowired
    private TbTagService tagService;

    @Autowired
    private TbUserTagService userTagService;

    /**
     * 获取标签类型列表
     *
     * @return 标签类型列表
     */
    @GetMapping("/types")
    public Result getTagTypes() {
        // 从数据库中查询所有不同的标签类型
        QueryWrapper<TbTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT tag_type");
        queryWrapper.eq("tag_status", 1);  // 只查询启用状态的标签
        
        List<TbTag> tagTypesList = tagService.list(queryWrapper);
        
        // 转换为前端需要的格式
        List<Map<String, Object>> tagTypes = new ArrayList<>();
        
        // 标签类型对应的名称映射
        Map<Integer, String> typeNameMap = new HashMap<>();
        typeNameMap.put(1, "兴趣爱好");
        typeNameMap.put(2, "性格特点");
        typeNameMap.put(3, "生活方式");
        typeNameMap.put(4, "其他");
        
        // 将查询结果转换为前端需要的格式
        for (TbTag tag : tagTypesList) {
            Map<String, Object> typeMap = new HashMap<>();
            Integer tagType = tag.getTagType();
            typeMap.put("value", tagType);
            typeMap.put("label", typeNameMap.getOrDefault(tagType, "未知类型"));
            tagTypes.add(typeMap);
        }
        
        // 如果没有查询到结果，返回空列表
        if (tagTypes.isEmpty()) {
            log.warn("未查询到任何标签类型");
        }
        
        return Result.OK(tagTypes);
    }

    /**
     * 分页查询标签列表
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @param tagType  标签类型
     * @param keyword  关键词
     * @return 标签列表分页结果
     */
    @GetMapping("/list")
    public Result getTagList(@RequestParam(required = false, defaultValue = "1") Integer page,
                             @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) Integer tagType,
                             @RequestParam(required = false) String keyword) {
        log.info("分页查询标签列表: page={}, pageSize={}, tagType={}, keyword={}", page, pageSize, tagType, keyword);

        // 构建查询条件
        LambdaQueryWrapper<TbTag> queryWrapper = new LambdaQueryWrapper<>();
        // 标签状态为启用
        queryWrapper.eq(TbTag::getTagStatus, 1);
        // 按标签类型过滤
        if (tagType != null) {
            queryWrapper.eq(TbTag::getTagType, tagType);
        }
        // 按关键词过滤
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(TbTag::getTagName, keyword);
        }
        // 按创建时间降序排序
        queryWrapper.orderByDesc(TbTag::getCreatedAt);

        // 执行分页查询
        Page<TbTag> tagPage = tagService.page(new Page<>(page, pageSize), queryWrapper);

        return Result.OK(tagPage);
    }

    /**
     * 根据类型获取标签列表
     *
     * @param tagType  标签类型
     * @param page     页码，默认1
     * @param pageSize 每页大小，默认10
     * @return 标签列表分页结果
     */
    @GetMapping("/byType")
    public Result getTagsByType(
        @RequestParam Integer tagType,
        @RequestParam(required = false, defaultValue = "1") Integer page,
        @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        log.info("根据类型获取标签列表: tagType={}, page={}, pageSize={}", tagType, page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<TbTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbTag::getTagType, tagType);
        queryWrapper.eq(TbTag::getTagStatus, 1);
        queryWrapper.orderByDesc(TbTag::getCreatedAt);

        // 执行分页查询
        Page<TbTag> tagPage = tagService.page(new Page<>(page, pageSize), queryWrapper);

        return Result.OK(tagPage);
    }

    /**
     * 创建标签
     *
     * @param tag 标签实体
     * @return 创建结果
     */
    @PostMapping
    public Result createTag(@RequestBody TbTag tag) {
        log.info("创建标签: {}", tag);

        // 参数校验
        if (!StringUtils.hasText(tag.getTagName())) {
            return Result.ERROR("标签名称不能为空");
        }
        if (tag.getTagType() == null) {
            return Result.ERROR("标签类型不能为空");
        }

        // 检查标签名称是否已存在
        LambdaQueryWrapper<TbTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbTag::getTagName, tag.getTagName());
        long countL = tagService.count(queryWrapper);
        if (countL > 0) {
            return Result.ERROR("标签名称已存在");
        }

        // 设置默认值
        tag.setTagId(null); // 确保ID为空，由数据库自动生成
        tag.setTagStatus(1); // 默认启用
        tag.setCreatedAt(new Date());
        tag.setUpdatedAt(new Date());
        
        // 保存标签
        boolean success = tagService.save(tag);
        if (success) {
            return Result.OK(tag);
        } else {
            return Result.ERROR("创建标签失败");
        }
    }
    
    /**
     * 更新标签
     *
     * @param tag 标签实体
     * @return 更新结果
     */
    @PutMapping
    public Result updateTag(@RequestBody TbTag tag) {
        log.info("更新标签: {}", tag);
        
        // 参数校验
        if (tag.getTagId() == null) {
            return Result.ERROR("标签ID不能为空");
        }
        if (!StringUtils.hasText(tag.getTagName())) {
            return Result.ERROR("标签名称不能为空");
        }
        if (tag.getTagType() == null) {
            return Result.ERROR("标签类型不能为空");
        }
        
        // 检查标签是否存在
        TbTag existingTag = tagService.getById(tag.getTagId());
        if (existingTag == null) {
            return Result.ERROR("标签不存在");
        }
        
        // 检查标签名称是否已被其他标签使用
        LambdaQueryWrapper<TbTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TbTag::getTagName, tag.getTagName());
        queryWrapper.ne(TbTag::getTagId, tag.getTagId());
        long countL = tagService.count(queryWrapper);
        if (countL > 0) {
            return Result.ERROR("标签名称已存在");
        }
        
        // 设置更新时间
        tag.setUpdatedAt(new Date());
        
        // 更新标签
        boolean success = tagService.updateById(tag);
        if (success) {
            return Result.OK(tag);
        } else {
            return Result.ERROR("更新标签失败");
        }
    }
    
    /**
     * 删除标签
     *
     * @param tagId 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/{tagId}")
    public Result deleteTag(@PathVariable Long tagId) {
        log.info("删除标签: tagId={}", tagId);
        
        // 检查标签是否存在
        TbTag tag = tagService.getById(tagId);
        if (tag == null) {
            return Result.ERROR("标签不存在");
        }
        
        // 检查标签是否被用户使用
        LambdaQueryWrapper<TbUserTag> userTagQueryWrapper = new LambdaQueryWrapper<>();
        userTagQueryWrapper.eq(TbUserTag::getTagId, tagId);
        long countL = userTagService.count(userTagQueryWrapper);
        if (countL > 0) {
            return Result.ERROR("标签已被用户使用，无法删除");
        }
        
        // 删除标签
        boolean success = tagService.removeById(tagId);
        if (success) {
            return Result.OK();
        } else {
            return Result.ERROR("删除标签失败");
        }
    }
    
    /**
     * 获取用户标签
     *
     * @param userId 用户ID
     * @return 用户标签列表
     */
    @GetMapping("/user/{userId}")
    public Result getUserTags(@PathVariable Long userId) {
        log.info("获取用户标签: userId={}", userId);
        
        // 查询用户标签关联
        LambdaQueryWrapper<TbUserTag> userTagQueryWrapper = new LambdaQueryWrapper<>();
        userTagQueryWrapper.eq(TbUserTag::getUserId, userId);
        List<TbUserTag> userTags = userTagService.list(userTagQueryWrapper);
        
        // 如果用户没有标签，返回空列表
        if (userTags.isEmpty()) {
            return Result.OK(Collections.emptyList());
        }
        
        // 获取标签ID列表
        List<Long> tagIds = userTags.stream()
                .map(TbUserTag::getTagId)
                .collect(Collectors.toList());
        
        // 查询标签详情
        List<TbTag> tags = tagService.listByIds(tagIds);
        
        return Result.OK(tags);
    }
    
    /**
     * 设置用户标签
     *
     * @param requestMap 请求参数，包含userId和tagIds
     * @return 设置结果
     */
    @PostMapping("/user")
    public Result setUserTags(@RequestBody Map<String, Object> requestMap) {
        Long userId = Long.valueOf(requestMap.get("userId").toString());
        List<Integer> tagIdsObj = (List<Integer>) requestMap.get("tagIds");
        List<Long> tagIds = tagIdsObj.stream().map(Long::valueOf).collect(Collectors.toList());
        
        log.info("设置用户标签: userId={}, tagIds={}", userId, tagIds);
        
        // 参数校验
        if (userId == null) {
            return Result.ERROR("用户ID不能为空");
        }
        if (tagIds == null || tagIds.isEmpty()) {
            return Result.ERROR("标签ID列表不能为空");
        }
        
        // 检查标签是否存在
        List<TbTag> tags = tagService.listByIds(tagIds);
        if (tags.size() != tagIds.size()) {
            return Result.ERROR("部分标签不存在");
        }
        
        try {
            // 开启事务
            // 1. 删除用户原有标签
            LambdaQueryWrapper<TbUserTag> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(TbUserTag::getUserId, userId);
            userTagService.remove(deleteWrapper);
            
            // 2. 添加新标签
            List<TbUserTag> userTags = new ArrayList<>();
            for (Long tagId : tagIds) {
                TbUserTag userTag = new TbUserTag();
                userTag.setUserId(userId);
                userTag.setTagId(tagId);
                userTag.setCreatedAt(new Date());
                userTags.add(userTag);
            }
            
            boolean success = userTagService.saveBatch(userTags);
            if (success) {
                return Result.OK();
            } else {
                return Result.ERROR("设置用户标签失败");
            }
        } catch (Exception e) {
            log.error("设置用户标签异常", e);
            return Result.ERROR("设置用户标签失败：" + e.getMessage());
        }
    }
} 