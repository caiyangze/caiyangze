package com.zhentao.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: Result 返回结果
 *  code 响应码 代表请求成功/失败
 *  message 响应信息
 *  data 响应数据
 * @Author: 振涛教育_李小超
 * @Date: 2024年2月26日 13:58
 */
@Data
public class Result {
    private Integer code;//状态码
    private String message;//提示信息
    private Object data;//数据

    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result() {
    }

    //成功响应
    public static Result OK() {
        return new Result(Constant.RESPONSE_CODE_SUCCESS, "操作成功", null);
    }

    public static Result OK(Object data) {
        return new Result(Constant.RESPONSE_CODE_SUCCESS, "操作成功", data);
    }

    //失败响应
    public static Result ERROR() {
        return new Result(Constant.RESPONSE_CODE_ERROR, "操作失败", null);
    }

    public static Result ERROR(Object data) {
        return new Result(Constant.RESPONSE_CODE_ERROR, "操作失败", data);
    }

    public static Result ERROR(String message) {
        return new Result(Constant.RESPONSE_CODE_ERROR, message, null);
    }

    //未登录响应
    public static Result NO_LOGIN(){
        return new Result(Constant.RESPONSE_CODE_NO_LOGIN, "未登录", null);
    }
    public static Result NO_LOGIN(String message){
        return new Result(Constant.RESPONSE_CODE_NO_LOGIN, message, null);
    }

    //权限不足响应
    public static Result FORBIDDEN(){
        return new Result(Constant.RESPONSE_CODE_FORBIDDEN, "权限不足，禁止访问", null);
    }

    // 添加success方法，与OK方法功能相同，提供更多选择
    public static Result success() {
        return OK();
    }

    public static Result success(Object data) {
        return OK(data);
    }

    public static Result success(String message) {
        return new Result(Constant.RESPONSE_CODE_SUCCESS, message, null);
    }

    public static Result success(String message, Object data) {
        return new Result(Constant.RESPONSE_CODE_SUCCESS, message, data);
    }

    // 添加error方法，与ERROR方法功能相同，提供更多选择
    public static Result error() {
        return ERROR();
    }

    public static Result error(String message) {
        return ERROR(message);
    }

    public static Result error(Object data) {
        return ERROR(data);
    }

    public static Result error(String message, Object data) {
        return new Result(Constant.RESPONSE_CODE_ERROR, message, data);
    }
}
