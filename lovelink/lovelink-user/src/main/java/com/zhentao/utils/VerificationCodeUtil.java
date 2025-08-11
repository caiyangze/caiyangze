package com.zhentao.utils;

import java.util.Random;

/**
 * 验证码生成和校验工具类
 *
 * @author zhentao
 */
public class VerificationCodeUtil {

    /**
     * 生成指定长度的数字验证码
     *
     * @param length 验证码长度
     * @return 生成的验证码
     */
    public static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 生成6位数字验证码
     *
     * @return 6位数字验证码
     */
    public static String generateCode() {
        return generateCode(6);
    }
} 