package com.zhentao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * @author zhentao
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
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
