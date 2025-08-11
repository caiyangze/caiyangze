package com.zhentao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhentao.pojo.TbUser;
import com.zhentao.pojo.TbUserPhoto;
import com.zhentao.service.TbUserPhotoService;
import com.zhentao.service.UserService;
import com.zhentao.utils.JwtService;
import com.zhentao.utils.MinioUtil;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户相册控制器
 * 
 * @author zhentao
 */
@RestController
@RequestMapping("/user/photo")
@CrossOrigin("*")
@Slf4j
public class UserPhotoController {
    
    @Autowired
    private TbUserPhotoService userPhotoService;

    @Autowired
    private UserService userService;

    @Autowired
    private MinioUtil minioUtil;
    
    // 支持的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );
    
    // 最大文件大小：10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 上传相册照片
     */
    @PostMapping("/upload")
    public Result uploadPhoto(@RequestParam("file") MultipartFile file, 
                             @RequestParam(value = "photoDesc", required = false) String photoDesc,
                             @RequestParam(value = "isPublic", defaultValue = "1") Integer isPublic,
                             @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }
            
            Long userId = Long.valueOf((Integer) userIdObj);
            
            // 验证文件
            if (file.isEmpty()) {
                return Result.ERROR("上传文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                return Result.ERROR("只支持图片文件上传（jpg、png、gif、bmp、webp）");
            }
            
            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.ERROR("文件大小不能超过10MB");
            }
            
            // 验证文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return Result.ERROR("文件名不能为空");
            }
            
            log.info("用户{}开始上传相册照片，文件名：{}，大小：{}KB", 
                    userId, originalFilename, file.getSize() / 1024);
            
            // 上传文件到MinIO
            String photoUrl = minioUtil.uploadUserPhoto(file);
            
            // 获取当前用户相册数量，用于排序
            QueryWrapper<TbUserPhoto> countWrapper = new QueryWrapper<>();
            countWrapper.eq("user_id", userId);
            long photoCount = userPhotoService.count(countWrapper);
            
            // 保存到数据库
            TbUserPhoto userPhoto = new TbUserPhoto();
            userPhoto.setUserId(userId);
            userPhoto.setPhotoUrl(photoUrl);
            userPhoto.setPhotoDesc(photoDesc);
            userPhoto.setIsAvatar(0); // 默认不是头像
            userPhoto.setIsPublic(isPublic);
            userPhoto.setSortOrder((int) photoCount + 1); // 排序序号
            userPhoto.setCreatedAt(new Date());
            userPhoto.setUpdatedAt(new Date());
            
            boolean saved = userPhotoService.save(userPhoto);
            
            if (saved) {
                log.info("用户{}上传相册照片成功，访问路径：{}", userId, photoUrl);
                return Result.OK(userPhoto);
            } else {
                return Result.ERROR("保存照片信息失败");
            }
            
        } catch (Exception e) {
            log.error("上传相册照片失败", e);
            return Result.ERROR("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户相册列表
     */
    @GetMapping("/list")
    public Result getPhotoList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                              @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                              @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }
            
            Long userId = Long.valueOf((Integer) userIdObj);
            
            // 查询用户相册
            QueryWrapper<TbUserPhoto> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByAsc("sort_order");
            queryWrapper.orderByDesc("created_at");
            
            Page<TbUserPhoto> page = userPhotoService.page(new Page<>(pageNum, pageSize), queryWrapper);
            
            return Result.OK(page);
            
        } catch (Exception e) {
            log.error("获取用户相册列表失败", e);
            return Result.ERROR("获取相册列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除相册照片
     */
    @DeleteMapping("/delete/{photoId}")
    public Result deletePhoto(@PathVariable Long photoId, @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }
            
            Long userId = Long.valueOf((Integer) userIdObj);
            
            // 查询照片是否存在且属于当前用户
            TbUserPhoto userPhoto = userPhotoService.getById(photoId);
            if (userPhoto == null) {
                return Result.ERROR("照片不存在");
            }
            
            if (!userPhoto.getUserId().equals(userId)) {
                return Result.ERROR("无权删除此照片");
            }
            
            // 删除照片记录
            boolean deleted = userPhotoService.removeById(photoId);
            
            if (deleted) {
                log.info("用户{}删除相册照片成功，照片ID：{}", userId, photoId);
                return Result.OK("删除成功");
            } else {
                return Result.ERROR("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除相册照片失败", e);
            return Result.ERROR("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 设置照片为头像
     */
    @PostMapping("/setAvatar/{photoId}")
    public Result setAsAvatar(@PathVariable Long photoId, @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }

            Long userId = Long.valueOf((Integer) userIdObj);

            // 查询照片是否存在且属于当前用户
            TbUserPhoto userPhoto = userPhotoService.getById(photoId);
            if (userPhoto == null) {
                return Result.ERROR("照片不存在");
            }

            if (!userPhoto.getUserId().equals(userId)) {
                return Result.ERROR("无权操作此照片");
            }

            // 先将该用户的所有照片设置为非头像
            QueryWrapper<TbUserPhoto> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("user_id", userId);
            updateWrapper.eq("is_avatar", 1);

            TbUserPhoto updatePhoto = new TbUserPhoto();
            updatePhoto.setIsAvatar(0);
            updatePhoto.setUpdatedAt(new Date());
            userPhotoService.update(updatePhoto, updateWrapper);

            // 设置当前照片为头像
            userPhoto.setIsAvatar(1);
            userPhoto.setUpdatedAt(new Date());
            boolean updated = userPhotoService.updateById(userPhoto);

            if (updated) {
                // 同步更新用户表中的头像URL
                TbUser user = userService.getById(userId.intValue());
                if (user != null) {
                    user.setAvatarUrl(userPhoto.getPhotoUrl());
                    user.setUpdatedAt(new Date());
                    userService.updateById(user);
                }

                log.info("用户{}设置头像成功，照片ID：{}", userId, photoId);
                return Result.OK("设置头像成功");
            } else {
                return Result.ERROR("设置头像失败");
            }

        } catch (Exception e) {
            log.error("设置头像失败", e);
            return Result.ERROR("设置头像失败：" + e.getMessage());
        }
    }

    /**
     * 取消头像
     */
    @PostMapping("/cancelAvatar")
    public Result cancelAvatar(@RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }

            Long userId = Long.valueOf((Integer) userIdObj);

            // 将该用户的所有照片设置为非头像
            QueryWrapper<TbUserPhoto> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("user_id", userId);
            updateWrapper.eq("is_avatar", 1);

            TbUserPhoto updatePhoto = new TbUserPhoto();
            updatePhoto.setIsAvatar(0);
            updatePhoto.setUpdatedAt(new Date());
            boolean updated = userPhotoService.update(updatePhoto, updateWrapper);

            if (updated) {
                // 同步更新用户表中的头像URL为默认头像
                TbUser user = userService.getById(userId.intValue());
                if (user != null) {
                    user.setAvatarUrl("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
                    user.setUpdatedAt(new Date());
                    userService.updateById(user);
                }

                log.info("用户{}取消头像成功", userId);
                return Result.OK("取消头像成功");
            } else {
                return Result.ERROR("取消头像失败");
            }

        } catch (Exception e) {
            log.error("取消头像失败", e);
            return Result.ERROR("取消头像失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新照片信息
     */
    @PutMapping("/update/{photoId}")
    public Result updatePhoto(@PathVariable Long photoId,
                             @RequestBody Map<String, Object> updateData,
                             @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }
            
            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }
            
            Long userId = Long.valueOf((Integer) userIdObj);
            
            // 查询照片是否存在且属于当前用户
            TbUserPhoto userPhoto = userPhotoService.getById(photoId);
            if (userPhoto == null) {
                return Result.ERROR("照片不存在");
            }
            
            if (!userPhoto.getUserId().equals(userId)) {
                return Result.ERROR("无权操作此照片");
            }
            
            // 更新照片信息
            String photoDesc = (String) updateData.get("photoDesc");
            Integer isPublic = (Integer) updateData.get("isPublic");

            if (photoDesc != null) {
                userPhoto.setPhotoDesc(photoDesc);
            }
            if (isPublic != null) {
                userPhoto.setIsPublic(isPublic);
            }
            userPhoto.setUpdatedAt(new Date());
            
            boolean updated = userPhotoService.updateById(userPhoto);
            
            if (updated) {
                log.info("用户{}更新照片信息成功，照片ID：{}", userId, photoId);
                return Result.OK(userPhoto);
            } else {
                return Result.ERROR("更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新照片信息失败", e);
            return Result.ERROR("更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新照片排序
     */
    @PutMapping("/updateSort/{photoId}")
    public Result updatePhotoSort(@PathVariable Long photoId,
                                 @RequestBody Map<String, Object> sortData,
                                 @RequestHeader("token") String token) {
        try {
            // 验证token
            if (JwtService.verifyToken(token) != 1) {
                return Result.NO_LOGIN();
            }

            Map<String, Object> claimsMap = JwtService.getClaimsMap(token);
            Object userIdObj = claimsMap.get("userId");
            if (!(userIdObj instanceof Integer)) {
                return Result.NO_LOGIN();
            }

            Long userId = Long.valueOf((Integer) userIdObj);

            // 查询照片是否存在且属于当前用户
            TbUserPhoto userPhoto = userPhotoService.getById(photoId);
            if (userPhoto == null) {
                return Result.ERROR("照片不存在");
            }

            if (!userPhoto.getUserId().equals(userId)) {
                return Result.ERROR("无权操作此照片");
            }

            // 更新排序
            Integer sortOrder = (Integer) sortData.get("sortOrder");
            if (sortOrder != null) {
                userPhoto.setSortOrder(sortOrder);
                userPhoto.setUpdatedAt(new Date());

                boolean updated = userPhotoService.updateById(userPhoto);

                if (updated) {
                    log.info("用户{}更新照片排序成功，照片ID：{}，新排序：{}", userId, photoId, sortOrder);
                    return Result.OK("排序更新成功");
                } else {
                    return Result.ERROR("排序更新失败");
                }
            } else {
                return Result.ERROR("排序参数不能为空");
            }

        } catch (Exception e) {
            log.error("更新照片排序失败", e);
            return Result.ERROR("更新失败：" + e.getMessage());
        }
    }
}
