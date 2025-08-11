package com.zhentao.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册数据传输对象
 * 
 * @author zhentao
 */
@Data
public class UserRegisterDTO {
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;
    
    /**
     * 验证码
     */
//    @NotBlank(message = "验证码不能为空")
//    @Size(min = 4, max = 6, message = "验证码长度不正确")
//    private String verifyCode;
    
    /**
     * 昵称
     */
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;
    
    /**
     * 头像路径
     */
    private String avatarUrl;


//    id值空则进行注册，有值则进行更改
    private Long Id;

//    性别
    private Integer gender;
} 