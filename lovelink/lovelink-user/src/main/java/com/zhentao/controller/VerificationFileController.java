package com.zhentao.controller;

import com.zhentao.utils.JwtService;
import com.zhentao.utils.MinioUtil;
import com.zhentao.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 实名认证文件上传控制器
 * 
 * @author zhentao
 */
@RestController
@RequestMapping("/user/verification/upload")
@CrossOrigin("*")
@Slf4j
public class VerificationFileController {
    
    @Autowired
    private MinioUtil minioUtil;
    
    // 支持的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    );
    
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 上传身份证正面照片
     */
    @PostMapping("/idcard-front")
    public Result uploadIdCardFront(@RequestParam("file") MultipartFile file, @RequestHeader("token") String token) {
        return uploadVerificationFile(file, token, "身份证正面", "idcard-front");
    }
    
    /**
     * 上传身份证背面照片
     */
    @PostMapping("/idcard-back")
    public Result uploadIdCardBack(@RequestParam("file") MultipartFile file, @RequestHeader("token") String token) {
        return uploadVerificationFile(file, token, "身份证背面", "idcard-back");
    }
    
    /**
     * 上传人脸照片
     */
    @PostMapping("/face-photo")
    public Result uploadFacePhoto(@RequestParam("file") MultipartFile file, @RequestHeader("token") String token) {
        return uploadVerificationFile(file, token, "人脸照片", "face-photo");
    }
    
    /**
     * 通用的认证文件上传方法
     */
    private Result uploadVerificationFile(MultipartFile file, String token, String fileDesc, String fileType) {
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
            if (file == null || file.isEmpty()) {
                return Result.ERROR(fileDesc + "不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
                return Result.ERROR("只支持图片文件上传（JPG、PNG、GIF、BMP、WEBP）");
            }
            
            // 验证文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.ERROR("文件大小不能超过5MB");
            }
            
            // 验证文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return Result.ERROR("文件名不能为空");
            }
            
            log.info("用户{}开始上传{}，文件名：{}，大小：{}KB", 
                    userId, fileDesc, originalFilename, file.getSize() / 1024);
            
            // 根据文件类型选择上传方法
            String fileUrl;
            switch (fileType) {
                case "idcard-front":
                    fileUrl = minioUtil.uploadIdCardFront(file);
                    break;
                case "idcard-back":
                    fileUrl = minioUtil.uploadIdCardBack(file);
                    break;
                case "face-photo":
                    fileUrl = minioUtil.uploadFacePhoto(file);
                    break;
                default:
                    fileUrl = minioUtil.uploadVerificationFile(file, fileType);
                    break;
            }
            
            log.info("用户{}上传{}成功，访问路径：{}", userId, fileDesc, fileUrl);
            
            return Result.OK(fileUrl);
            
        } catch (Exception e) {
            log.error("上传{}失败", fileDesc, e);
            return Result.ERROR("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量上传认证文件（可选功能）
     */
    @PostMapping("/batch")
    public Result batchUploadVerificationFiles(
            @RequestParam(value = "idCardFront", required = false) MultipartFile idCardFront,
            @RequestParam(value = "idCardBack", required = false) MultipartFile idCardBack,
            @RequestParam(value = "facePhoto", required = false) MultipartFile facePhoto,
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
            
            log.info("用户{}开始批量上传认证文件", userId);
            
            Map<String, String> uploadResults = new java.util.HashMap<>();
            
            // 上传身份证正面
            if (idCardFront != null && !idCardFront.isEmpty()) {
                Result result = uploadVerificationFile(idCardFront, token, "身份证正面", "idcard-front");
                if (result.getCode() == 200) {
                    uploadResults.put("idCardFront", (String) result.getData());
                } else {
                    return result; // 如果有任何一个上传失败，直接返回错误
                }
            }
            
            // 上传身份证背面
            if (idCardBack != null && !idCardBack.isEmpty()) {
                Result result = uploadVerificationFile(idCardBack, token, "身份证背面", "idcard-back");
                if (result.getCode() == 200) {
                    uploadResults.put("idCardBack", (String) result.getData());
                } else {
                    return result;
                }
            }
            
            // 上传人脸照片
            if (facePhoto != null && !facePhoto.isEmpty()) {
                Result result = uploadVerificationFile(facePhoto, token, "人脸照片", "face-photo");
                if (result.getCode() == 200) {
                    uploadResults.put("facePhoto", (String) result.getData());
                } else {
                    return result;
                }
            }
            
            if (uploadResults.isEmpty()) {
                return Result.ERROR("请至少上传一个文件");
            }
            
            log.info("用户{}批量上传认证文件成功，上传数量：{}", userId, uploadResults.size());
            
            return Result.OK(uploadResults);
            
        } catch (Exception e) {
            log.error("批量上传认证文件失败", e);
            return Result.ERROR("批量上传失败：" + e.getMessage());
        }
    }
}
