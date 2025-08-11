package com.zhentao.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云身份证实名认证工具类
 * 
 * @author zhentao
 */
@Slf4j
@Component
public class AliIdCardVerifyUtil {
    
    private static final String HOST = "https://kzidcardv1.market.alicloudapi.com";
    private static final String PATH = "/api-mall/api/id_card/check";
    private static final String METHOD = "POST";
    private static final String APP_CODE = "c2dfb4566e14402b944dd6ecb65f685f";
    
    /**
     * 验证身份证信息
     * 
     * @param realName 真实姓名
     * @param idCardNo 身份证号
     * @return 验证结果
     */
    public VerifyResult verifyIdCard(String realName, String idCardNo) {
        try {
            log.info("开始验证身份证信息，姓名：{}，身份证号：{}", realName, maskIdCard(idCardNo));
            
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + APP_CODE);
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            // 构建请求参数
            Map<String, String> querys = new HashMap<>();
            Map<String, String> bodys = new HashMap<>();
            bodys.put("name", realName);
            bodys.put("idcard", idCardNo);
            
            // 发送请求
            HttpResponse response = HttpUtils.doPost(HOST, PATH, METHOD, headers, querys, bodys);
            
            if (response == null) {
                log.error("身份证验证请求失败，响应为空");
                return VerifyResult.error("身份证验证服务异常，请稍后重试");
            }
            
            // 解析响应
            String responseBody = EntityUtils.toString(response.getEntity());
            log.info("身份证验证响应：{}", responseBody);

            JSONObject jsonResponse = JSON.parseObject(responseBody);

            // 检查API调用是否成功
            if (jsonResponse == null) {
                log.error("身份证验证响应解析失败");
                return VerifyResult.error("身份证验证服务异常，请稍后重试");
            }

            // 根据阿里云API返回格式解析结果
            // 检查顶层返回码和成功标识
            Integer code = jsonResponse.getInteger("code");
            Boolean success = jsonResponse.getBoolean("success");
            String msg = jsonResponse.getString("msg");

            log.info("API返回码：{}，成功标识：{}，消息：{}", code, success, msg);

            // 检查API调用是否成功
            if (code != null && code == 200 && Boolean.TRUE.equals(success)) {
                // 检查是否有data字段
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data != null) {
                    log.info("data字段内容：{}", data.toJSONString());

                    // 检查desc字段，这是关键的验证结果
                    String desc = data.getString("desc");
                    Integer result = data.getInteger("result");

                    log.info("result：{}，desc：{}", result, desc);

                    // 根据实际API返回格式：
                    // desc = "一致" 表示身份证信息验证成功
                    // desc = "不一致" 表示身份证信息验证失败
                    // result 字段的含义可能与预期不同，以desc为准

                    if ("一致".equals(desc)) {
                        log.info("身份证验证成功，姓名：{}，身份证号：{}", realName, maskIdCard(idCardNo));
                        return VerifyResult.success("身份证验证成功");
                    } else if ("不一致".equals(desc)) {
                        log.warn("身份证验证失败，姓名：{}，身份证号：{}，desc：{}",
                                realName, maskIdCard(idCardNo), desc);
                        return VerifyResult.fail("身份证信息验证失败，请检查姓名和身份证号是否正确");
                    } else {
                        // 处理其他可能的desc值
                        log.warn("身份证验证返回未知结果，姓名：{}，身份证号：{}，result：{}，desc：{}",
                                realName, maskIdCard(idCardNo), result, desc);

                        // 如果desc包含"一致"关键词，认为是成功
                        if (desc != null && desc.contains("一致")) {
                            log.info("身份证验证成功（包含一致关键词），姓名：{}，身份证号：{}", realName, maskIdCard(idCardNo));
                            return VerifyResult.success("身份证验证成功");
                        } else {
                            return VerifyResult.fail("身份证信息验证失败，请检查姓名和身份证号是否正确");
                        }
                    }
                } else {
                    log.error("身份证验证响应data字段为空");
                    return VerifyResult.error("身份证验证服务异常，请稍后重试");
                }
            } else {
                // API调用失败
                log.error("身份证验证API调用失败，code：{}，success：{}，msg：{}", code, success, msg);
                return VerifyResult.error("身份证验证服务异常：" + msg);
            }
            
        } catch (Exception e) {
            log.error("身份证验证异常，姓名：{}，身份证号：{}", realName, maskIdCard(idCardNo), e);
            return VerifyResult.error("身份证验证服务异常，请稍后重试");
        }
    }
    
    /**
     * 脱敏身份证号
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "****" + idCard.substring(idCard.length() - 4);
    }
    
    /**
     * 验证结果类
     */
    public static class VerifyResult {
        private boolean success;
        private String message;
        private String code;
        
        private VerifyResult(boolean success, String message, String code) {
            this.success = success;
            this.message = message;
            this.code = code;
        }
        
        public static VerifyResult success(String message) {
            return new VerifyResult(true, message, "SUCCESS");
        }
        
        public static VerifyResult fail(String message) {
            return new VerifyResult(false, message, "VERIFY_FAIL");
        }
        
        public static VerifyResult error(String message) {
            return new VerifyResult(false, message, "SYSTEM_ERROR");
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getCode() {
            return code;
        }
    }
}
