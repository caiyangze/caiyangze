package com.zhentao.utils;

import com.zhentao.config.MinioConfig;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类
 * 提供文件上传、下载、删除等功能
 *
 * @author zhentao
 */
@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param folder 文件夹名称，如：avatar、photo等
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 检查存储桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            
            // 如果不存在则创建
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
                log.info("创建存储桶：{}", minioConfig.getBucketName());
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            // 按日期分类存储
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String objectName = folder + "/" + sdf.format(new Date()) + "/" + uuid + suffix;

            PutObjectArgs build = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .contentType(file.getContentType())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build();

            // 上传文件
            minioClient.putObject(build);

            // 使用手动拼接方式获取永久URL
            String url = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;

            // 这行可以保留，但如果只是永久URL，通常不会有?参数
            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }
            
            log.info("文件上传成功，访问路径：{}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败：", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 头像访问路径
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatar");
    }

    /**
     * 上传实名认证相关文件
     *
     * @param file 认证文件（身份证正面、背面、人脸照片等）
     * @param fileType 文件类型：idcard-front、idcard-back、face-photo
     * @return 文件访问路径
     */
    public String uploadVerificationFile(MultipartFile file, String fileType) {
        return uploadFile(file, "verification/" + fileType);
    }

    /**
     * 上传身份证正面照片
     *
     * @param file 身份证正面照片
     * @return 文件访问路径
     */
    public String uploadIdCardFront(MultipartFile file) {
        return uploadVerificationFile(file, "idcard-front");
    }

    /**
     * 上传身份证背面照片
     *
     * @param file 身份证背面照片
     * @return 文件访问路径
     */
    public String uploadIdCardBack(MultipartFile file) {
        return uploadVerificationFile(file, "idcard-back");
    }

    /**
     * 上传人脸照片
     *
     * @param file 人脸照片
     * @return 文件访问路径
     */
    public String uploadFacePhoto(MultipartFile file) {
        return uploadVerificationFile(file, "face-photo");
    }

    /**
     * 上传用户相册照片
     *
     * @param file 相册照片
     * @return 文件访问路径
     */
    public String uploadUserPhoto(MultipartFile file) {
        return uploadFile(file, "userPhoto");
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败：", e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        }
    }
} 