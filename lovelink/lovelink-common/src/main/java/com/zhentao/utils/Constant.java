package com.zhentao.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: Constant
 * @Author: 振涛教育_李小超
 * @Date: 2024年2月26日 14:05
 */
public class Constant {


    public static String TOKEN_NAME = "Authorization";

    /**
     * 请求响应码常量
     */
    public static final Integer RESPONSE_CODE_SUCCESS = 200;
    public static final Integer RESPONSE_CODE_ERROR = 400;
    public static final Integer RESPONSE_CODE_NO_LOGIN = 402;
    public static final Integer RESPONSE_CODE_FORBIDDEN = 403;

    public static final Map<Integer, String> RESPONSE_CODE_MAP = new HashMap<>();
    static {
        RESPONSE_CODE_MAP.put(RESPONSE_CODE_SUCCESS, "请求成功");
        RESPONSE_CODE_MAP.put(RESPONSE_CODE_ERROR, "请求失败");
        RESPONSE_CODE_MAP.put(RESPONSE_CODE_NO_LOGIN, "没有登录");
        RESPONSE_CODE_MAP.put(RESPONSE_CODE_FORBIDDEN, "权限不足");
    }

}
