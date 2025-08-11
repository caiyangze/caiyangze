package com.zhentao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 红娘服务启动类
 *
 * @author zhentao
 */
@SpringBootApplication
@MapperScan("com.zhentao.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class MatchmakerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchmakerApplication.class, args);
    }
}
