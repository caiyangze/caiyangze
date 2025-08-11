package com.zhentao.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 解决前端访问后端API的跨域问题
 *
 * 说明：
 * - 后端服务运行在 8084 端口
 * - 前端可能运行在不同端口（如 5173, 8080, 3000 等）
 * - 这个配置允许这些前端端口访问后端API
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 全局跨域配置
     * 允许前端应用访问后端API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端地址（常见的开发端口）
                .allowedOrigins(
                    "http://localhost:5173",    // Vite 默认端口
                    "http://127.0.0.1:5173",
                    "http://localhost:8080",    // uni-app H5 常用端口
                    "http://127.0.0.1:8080",
                    "http://localhost:3000",    // React/Next.js 默认端口
                    "http://127.0.0.1:3000",
                    "http://localhost:8081",    // 其他常用端口
                    "http://127.0.0.1:8081",
                    "http://localhost:9000",
                    "http://127.0.0.1:9000"
                )
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                // 允许的请求头
                .allowedHeaders("*")
                // 不允许携带凭证（解决跨域问题）
                .allowCredentials(false)
                // 预检请求的缓存时间（秒）
                .maxAge(3600);
    }
}
