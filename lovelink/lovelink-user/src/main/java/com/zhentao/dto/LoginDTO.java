package com.zhentao.dto;

import lombok.Data;

/**
 * 登录请求DTO
 * 
 * @author zhentao
 */
@Data
public class LoginDTO {
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 密码
     */
    private String password;
} 