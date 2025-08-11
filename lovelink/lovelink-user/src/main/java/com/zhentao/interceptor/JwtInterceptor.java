package com.zhentao.interceptor;

import com.zhentao.utils.JwtService;
import com.zhentao.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * JWT拦截器
 * 用于从请求头中提取token并设置用户信息到ThreadLocal
 * 
 * @author zhentao
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        
        if (token != null && !token.isEmpty()) {
            try {
                // 验证token并获取用户ID
                Long userId = JwtService.getUserIdFromToken(token);
                if (userId != null) {
                    // 设置到ThreadLocal
                    ThreadLocalUtil.setCurrentUserId(userId);
                    log.debug("设置当前用户ID到ThreadLocal: {}", userId);
                }
            } catch (Exception e) {
                log.warn("解析token失败: {}", e.getMessage());
            }
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal，防止内存泄漏
        ThreadLocalUtil.remove();
    }
}
