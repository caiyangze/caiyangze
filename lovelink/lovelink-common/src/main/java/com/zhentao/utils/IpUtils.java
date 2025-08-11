package com.zhentao.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * 
 * @author zhentao
 */
@Slf4j
public class IpUtils {
    
    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String ip = null;
        
        // 1. 检查X-Forwarded-For头（代理服务器会添加此头）
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }
        
        // 2. 检查X-Real-IP头（Nginx代理会添加此头）
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 3. 检查Proxy-Client-IP头
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 4. 检查WL-Proxy-Client-IP头（WebLogic代理）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 5. 检查HTTP_CLIENT_IP头
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 6. 检查HTTP_X_FORWARDED_FOR头
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 7. 最后使用getRemoteAddr()方法获取
        ip = request.getRemoteAddr();
        
        log.debug("获取到的客户端IP: {}", ip);
        return ip;
    }
    
    /**
     * 验证IP地址是否有效
     * 
     * @param ip IP地址
     * @return true表示有效，false表示无效
     */
    private static boolean isValidIp(String ip) {
        return ip != null 
            && !ip.isEmpty() 
            && !"unknown".equalsIgnoreCase(ip)
            && !"0:0:0:0:0:0:0:1".equals(ip)  // IPv6本地地址
            && !"127.0.0.1".equals(ip);      // IPv4本地地址
    }
    
    /**
     * 判断是否为内网IP
     * 
     * @param ip IP地址
     * @return true表示内网IP，false表示外网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return true;
        }
        
        // IPv4本地地址
        if ("127.0.0.1".equals(ip) || "localhost".equals(ip)) {
            return true;
        }
        
        // 内网IP段
        return ip.startsWith("192.168.") 
            || ip.startsWith("10.") 
            || ip.startsWith("172.16.") 
            || ip.startsWith("172.17.")
            || ip.startsWith("172.18.")
            || ip.startsWith("172.19.")
            || ip.startsWith("172.20.")
            || ip.startsWith("172.21.")
            || ip.startsWith("172.22.")
            || ip.startsWith("172.23.")
            || ip.startsWith("172.24.")
            || ip.startsWith("172.25.")
            || ip.startsWith("172.26.")
            || ip.startsWith("172.27.")
            || ip.startsWith("172.28.")
            || ip.startsWith("172.29.")
            || ip.startsWith("172.30.")
            || ip.startsWith("172.31.");
    }
}
