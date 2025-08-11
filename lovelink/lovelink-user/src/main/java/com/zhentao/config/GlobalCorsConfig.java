package com.zhentao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置
 * 作为 CorsFilter 的补充配置
 * 
 * @author zhentao
 */
@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 允许所有域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH") // 允许的方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(false) // 当使用 * 时，不能设置为 true
                .exposedHeaders("Authorization", "Content-Type", "X-Requested-With", 
                               "Accept", "Origin", "Access-Control-Request-Method", 
                               "Access-Control-Request-Headers") // 暴露的响应头
                .maxAge(3600); // 预检请求缓存时间
    }
}
