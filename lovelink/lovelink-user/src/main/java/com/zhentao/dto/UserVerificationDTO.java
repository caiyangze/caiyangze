package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户实名认证DTO
 * 
 * @author zhentao
 */
@Data
public class UserVerificationDTO {
    
    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 20, message = "真实姓名长度不能超过20个字符")
    private String realName;
    
    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", 
             message = "身份证号格式不正确")
    private String idCardNo;
    
    /**
     * 身份证正面照片URL
     */
    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFront;
    
    /**
     * 身份证背面照片URL
     */
    @NotBlank(message = "身份证背面照片不能为空")
    private String idCardBack;
    
    /**
     * 人脸照片URL
     */
    @NotBlank(message = "人脸照片不能为空")
    private String facePhoto;
}
