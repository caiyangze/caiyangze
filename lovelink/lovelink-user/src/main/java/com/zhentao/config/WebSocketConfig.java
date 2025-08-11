package com.zhentao.config;

import com.zhentao.handler.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 
 * @author zhentao
 */
@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("正在注册WebSocket处理器...");

        // 注册聊天WebSocket处理器 - 原生WebSocket
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*"); // 允许跨域，生产环境建议配置具体域名
        log.info("已注册WebSocket处理器: /ws/chat");

        // 注册聊天WebSocket处理器 - SockJS降级支持
        registry.addHandler(chatWebSocketHandler, "/ws/chat-sockjs")
                .setAllowedOrigins("*")
                .withSockJS(); // 启用SockJS支持
        log.info("已注册SockJS WebSocket处理器: /ws/chat-sockjs");
    }
}
