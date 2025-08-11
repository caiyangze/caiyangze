package com.zhentao.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author: 阿巴阿巴
 * @CreateTime: 2025-07-09 18:57:27
 * @Description: ddd
 * @blessing: 佛祖保佑    永无BUG
 */
@Data
public class LoginCodeDTO {
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String code;
}
