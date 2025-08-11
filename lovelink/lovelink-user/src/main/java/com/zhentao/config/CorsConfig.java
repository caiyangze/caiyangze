package com.zhentao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * 跨域配置
 *
 * @author zhentao
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有域名进行跨域调用
        // Spring Boot 2.3.2 使用 addAllowedOrigin
        config.addAllowedOrigin("*");
        config.setAllowCredentials(false); // 当使用 * 时，不能设置为 true
        
        // 放行全部原始头信息
        config.addAllowedHeader("*");
        // 允许所有请求方法跨域调用
        config.addAllowedMethod("*");
        // 暴露头信息 - 不能使用通配符，需要指定具体的头信息
        config.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", 
            "Accept", "Origin", "Access-Control-Request-Method", 
            "Access-Control-Request-Headers", "Access-Control-Allow-Origin"
        ));
        // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        config.setMaxAge(3600L);
        
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 