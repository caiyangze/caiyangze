package com.zhentao.config;

import com.zhentao.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 
 * @author zhentao
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/chat/**", "/follow/**") // 拦截聊天和关注相关接口
                .excludePathPatterns("/chat/stats", "/chat-test.html"); // 排除统计接口和测试页面
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 确保跨域配置生效
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false)
                .exposedHeaders("Authorization", "Content-Type", "X-Requested-With",
                               "Accept", "Origin", "Access-Control-Request-Method",
                               "Access-Control-Request-Headers")
                .maxAge(3600);
    }
}
