package com.zhentao.controller;

import com.zhentao.utils.JwtService;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 模拟文件上传控制器（用于测试，不依赖MinIO）
 * @author zhentao
 * @date 2025/7/21
 */
@Slf4j
@RestController
@RequestMapping("/api/social/mock-file")
public class MockFileController {


    // 模拟文件存储路径 - 使用绝对路径
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/moment/";
    private static final String BASE_URL = "http://localhost:9002/uploads/moment/";
    
    /**
     * 模拟上传动态图片
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
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            
            // 创建日期目录
            String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fullDir = UPLOAD_DIR + dateDir + "/";
            
            // 创建目录
            File dir = new File(fullDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            File destFile = new File(fullDir + fileName);
            file.transferTo(destFile);
            
            // 返回文件URL
            String fileUrl = BASE_URL + dateDir + "/" + fileName;
            
            log.info("用户{}模拟上传动态图片成功：{}", userId, fileUrl);
            return Result.OK(fileUrl);
            
        } catch (Exception e) {
            log.error("模拟上传动态图片失败：", e);
            return Result.ERROR("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 模拟批量上传动态图片
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
            
            // 创建日期目录
            String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fullDir = UPLOAD_DIR + dateDir + "/";
            File dir = new File(fullDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
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
                    
                    // 生成文件名
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String fileName = UUID.randomUUID().toString() + extension;
                    
                    // 保存文件
                    File destFile = new File(fullDir + fileName);
                    file.transferTo(destFile);
                    
                    // 添加到结果列表
                    String fileUrl = BASE_URL + dateDir + "/" + fileName;
                    imageUrls.add(fileUrl);
                }
            }
            
            log.info("用户{}模拟批量上传动态图片成功，数量：{}", userId, imageUrls.size());
            return Result.OK(imageUrls);
            
        } catch (Exception e) {
            log.error("模拟批量上传动态图片失败：", e);
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
