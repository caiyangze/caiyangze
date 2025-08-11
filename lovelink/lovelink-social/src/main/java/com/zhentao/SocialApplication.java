package com.zhentao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 社交服务启动类
 *
 * @author zhentao
 */
@SpringBootApplication
@MapperScan("com.zhentao.mapper")
@EnableFeignClients
public class SocialApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialApplication.class, args);
    }
} 