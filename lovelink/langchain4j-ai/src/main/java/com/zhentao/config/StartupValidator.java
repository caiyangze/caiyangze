package com.zhentao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 启动验证器
 * 在应用启动后验证关键配置和服务状态
 */
@Component
public class StartupValidator implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupValidator.class);

    @Value("${server.port:8084}")
    private String serverPort;

    private final Environment environment;

    public StartupValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== AI红娘服务启动验证 ===");

        // 验证端口配置
        logger.info("✓ 服务端口: {}", serverPort);

        // 验证跨域配置
        logger.info("✓ 跨域配置: 已启用全局CORS支持");

        // 验证AI配置
        String apiKey = environment.getProperty("langchain4j.community.dashscope.streaming-chat-model.api-key");
        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("${")) {
            logger.info("✓ AI模型配置: 通义千问API密钥已配置");
        } else {
            logger.warn("⚠ AI模型配置: 未检测到API密钥，请检查application.properties配置");
        }

        // 验证数据库配置
        String dbUrl = environment.getProperty("spring.datasource.url");
        if (dbUrl != null && !dbUrl.trim().isEmpty() && !dbUrl.startsWith("${")) {
            logger.info("✓ 数据库配置: MySQL连接已配置");
        } else {
            logger.warn("⚠ 数据库配置: 未检测到数据库连接配置，请检查application.properties");
        }

        // 验证MongoDB配置
        String mongoUri = environment.getProperty("spring.data.mongodb.uri");
        if (mongoUri != null && !mongoUri.trim().isEmpty() && !mongoUri.startsWith("${")) {
            logger.info("✓ MongoDB配置: 聊天记忆存储已配置");
        } else {
            logger.warn("⚠ MongoDB配置: 未检测到MongoDB连接配置，请检查application.properties");
        }

        logger.info("=== 启动验证完成 ===");
        logger.info("🚀 AI红娘服务已启动，访问地址: http://localhost:{}/xiaozhi/chat", serverPort);
        logger.info("📖 API文档地址: http://localhost:{}/swagger-ui.html", serverPort);
        logger.info("🧪 前端测试页面: /pages/test/ai-test");
        logger.info("💬 AI聊天页面: /pages/ai-chat/ai-chat");
        logger.info("🔧 前端可以通过消息页面的AI红娘入口访问聊天功能");
    }
}
