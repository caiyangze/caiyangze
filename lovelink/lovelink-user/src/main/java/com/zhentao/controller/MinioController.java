package com.zhentao.controller;

import com.zhentao.service.UserService;
import com.zhentao.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: 阿巴阿巴
 * @CreateTime: 2025-07-09 14:55:30
 * @Description: minio上传控制层
 * @blessing: 佛祖保佑    永无BUG
 */
@RestController
@RequestMapping("upload")
public class MinioController {
    @Autowired
    UserService userService;
//    上传头像
    @RequestMapping("avatar")
    public String uploadAvatar(MultipartFile file) {
        return userService.uploadAvatar(file);
    }
}
