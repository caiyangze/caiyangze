package com.zhentao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.cache.annotation.EnableCaching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI模块启动类
 * 集成语音识别、语音合成、红娘推荐等AI功能
 */
@SpringBootApplication
@MapperScan("com.zhentao.mapper")
@ComponentScan(basePackages = {
    "com.zhentao.service",
    "com.zhentao.controller",
    "com.zhentao.tools",
    "com.zhentao.config",
    "com.zhentao.assistant",
    "com.zhentao.store"
})
@EnableTransactionManagement
@EnableCaching
public class XiaozhiApp {

    private static final Logger log = LoggerFactory.getLogger(XiaozhiApp.class);

    public static void main(String[] args) {
        try {
            log.info("正在启动AI模块...");

            // 设置系统属性
            System.setProperty("spring.application.name", "langchain-ai");
            System.setProperty("file.encoding", "UTF-8");

            SpringApplication app = new SpringApplication(XiaozhiApp.class);

            // 设置启动参数
            app.setAdditionalProfiles("ai");

            app.run(args);

            log.info("AI模块启动成功！");

        } catch (Exception e) {
            log.error("AI模块启动失败", e);
            System.exit(1);
        }
    }
}
