package com.zhentao.controller;

import com.zhentao.utils.JwtService;
import com.zhentao.utils.MinioUtil;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 社交模块文件上传控制器
 * @author zhentao
 * @date 2025/7/21
 */
@Slf4j
@RestController
@RequestMapping("/api/social/file")
public class SocialFileController {
    
    @Autowired
    private MinioUtil minioUtil;
    
    /**
     * 上传动态图片
     */
    @PostMapping("/upload/moment")
    public Result uploadMomentImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        try {
            // 验证用户登录状态
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }
            
            // 验证文件
            if (file.isEmpty()) {
                return Result.ERROR("上传文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !isValidImageType(contentType)) {
                return Result.ERROR("只支持图片文件上传");
            }
            
            // 验证文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.ERROR("文件大小不能超过10MB");
            }
            
            // 上传文件到MinIO
            String imageUrl = minioUtil.uploadFile(file, "moment");
            
            log.info("用户{}上传动态图片成功：{}", userId, imageUrl);
            return Result.OK(imageUrl);
            
        } catch (Exception e) {
            log.error("上传动态图片失败：", e);
            return Result.ERROR("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量上传动态图片
     */
    @PostMapping("/upload/moment/batch")
    public Result uploadMomentImages(@RequestParam("files") MultipartFile[] files,
                                                  HttpServletRequest request) {
        try {
            // 验证用户登录状态
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }
            
            // 验证文件数量
            if (files == null || files.length == 0) {
                return Result.ERROR("请选择要上传的文件");
            }
            
            if (files.length > 9) {
                return Result.ERROR("最多只能上传9张图片");
            }
            
            List<String> imageUrls = new ArrayList<>();
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // 验证文件类型
                    String contentType = file.getContentType();
                    if (contentType == null || !isValidImageType(contentType)) {
                        return Result.ERROR("只支持图片文件上传：" + file.getOriginalFilename());
                    }
                    
                    // 验证文件大小（10MB）
                    if (file.getSize() > 10 * 1024 * 1024) {
                        return Result.ERROR("文件大小不能超过10MB：" + file.getOriginalFilename());
                    }
                    
                    // 上传文件到MinIO
                    String imageUrl = minioUtil.uploadFile(file, "moment");
                    imageUrls.add(imageUrl);
                }
            }
            
            log.info("用户{}批量上传动态图片成功，数量：{}", userId, imageUrls.size());
            return Result.OK(imageUrls);
            
        } catch (Exception e) {
            log.error("批量上传动态图片失败：", e);
            return Result.ERROR("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传动态视频
     */
    @PostMapping("/upload/video")
    public Result uploadMomentVideo(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        try {
            // 验证用户登录状态
            Long userId = getUserIdFromToken(request);
            if (userId == null) {
                return Result.NO_LOGIN();
            }
            
            // 验证文件
            if (file.isEmpty()) {
                return Result.ERROR("上传文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !isValidVideoType(contentType)) {
                return Result.ERROR("只支持视频文件上传");
            }
            
            // 验证文件大小（100MB）
            if (file.getSize() > 100 * 1024 * 1024) {
                return Result.ERROR("视频文件大小不能超过100MB");
            }
            
            // 上传文件到MinIO
            String videoUrl = minioUtil.uploadFile(file, "moment/video");
            
            log.info("用户{}上传动态视频成功：{}", userId, videoUrl);
            return Result.OK(videoUrl);
            
        } catch (Exception e) {
            log.error("上传动态视频失败：", e);
            return Result.ERROR("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证图片文件类型
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }
    
    /**
     * 验证视频文件类型
     */
    private boolean isValidVideoType(String contentType) {
        return contentType.equals("video/mp4") ||
               contentType.equals("video/avi") ||
               contentType.equals("video/mov") ||
               contentType.equals("video/wmv") ||
               contentType.equals("video/flv");
    }
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) {
                token = request.getParameter("token");
            }
            
            if (token != null && !token.isEmpty()) {
                // 验证token并获取用户ID
                return JwtService.getUserIdFromToken(token);
            }
            
            return null;
        } catch (Exception e) {
            log.warn("获取用户ID失败：", e);
            return null;
        }
    }
}
