package com.zhentao.controller;

import com.zhentao.assistant.XiaozhiAgent;
import com.zhentao.bean.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 小红娘AI客服控制器
 * 提供专业的AI红娘服务，包括智能对话、推荐、语音合成、语音识别、文生图等多模态功能
 * 前端只需要调用一个 /xiaozhi/chat 接口即可实现所有功能
 */
@Tag(name = "小红娘AI客服", description = "专业的AI红娘客服服务，支持文字、语音、图片等多模态交互，智能推荐等功能")
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Operation(summary = "小红娘智能对话",
               description = "与专业AI红娘进行流式对话，支持多模态交互和智能推荐。" +
                           "AI会根据对话内容自动调用相应的工具（推荐、语音、图片等）")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(
            @Parameter(description = "聊天请求，包含用户信息、消息内容和多模态数据") @RequestBody ChatForm chatForm) {

        // 构建完整的消息内容，包含用户信息
        String enhancedMessage = buildEnhancedMessage(chatForm);

        return xiaozhiAgent.chat(chatForm.getMemoryId(), enhancedMessage);
    }

    /**
     * 构建增强的消息内容，包含用户信息用于AI智能判断
     */
    private String buildEnhancedMessage(ChatForm chatForm) {
        StringBuilder messageBuilder = new StringBuilder();

        // 添加用户基本信息（如果有的话）
        if (chatForm.getUserId() != null) {
            messageBuilder.append("[用户ID: ").append(chatForm.getUserId()).append("]");
        }

        if (chatForm.getUserGender() != null) {
            String genderText = chatForm.getUserGender() == 0 ? "女" : "男";
            messageBuilder.append("[用户性别: ").append(genderText).append("]");
        }

        if (chatForm.getUserRole() != null) {
            String roleText = chatForm.getUserRole() == 2 ? "红娘" : "普通用户";
            messageBuilder.append("[用户角色: ").append(roleText).append("]");
        }

        if (chatForm.getUserAge() != null) {
            messageBuilder.append("[用户年龄: ").append(chatForm.getUserAge()).append("岁]");
        }

        if (chatForm.getUserLocation() != null && !chatForm.getUserLocation().trim().isEmpty()) {
            messageBuilder.append("[用户地区: ").append(chatForm.getUserLocation()).append("]");
        }

        // 添加推荐类型提示（如果指定了的话）
        if (chatForm.getRecommendationType() != null && !chatForm.getRecommendationType().trim().isEmpty()) {
            String typeText = "user".equals(chatForm.getRecommendationType()) ? "异性用户推荐" : "红娘推荐";
            messageBuilder.append("[推荐需求: ").append(typeText).append("]");
        }

        // 添加语音相关信息
        if (chatForm.getAudioBase64() != null && !chatForm.getAudioBase64().trim().isEmpty()) {
            messageBuilder.append("[语音消息: ").append(chatForm.getAudioBase64()).append("]");
        }

        if (chatForm.getVoiceType() != null && !chatForm.getVoiceType().trim().isEmpty()) {
            messageBuilder.append("[语音类型: ").append(chatForm.getVoiceType()).append("]");
        }

        if (chatForm.getNeedVoiceResponse() != null && chatForm.getNeedVoiceResponse()) {
            messageBuilder.append("[需要语音回复]");
        }

        // 添加图片相关信息
        if (chatForm.getImageUrl() != null && !chatForm.getImageUrl().trim().isEmpty()) {
            messageBuilder.append("[图片URL: ").append(chatForm.getImageUrl()).append("]");
        }

        if (chatForm.getImageDescription() != null && !chatForm.getImageDescription().trim().isEmpty()) {
            messageBuilder.append("[图片描述需求: ").append(chatForm.getImageDescription()).append("]");
        }

        // 添加消息类型
        if (chatForm.getMessageType() != null && !chatForm.getMessageType().trim().isEmpty()) {
            messageBuilder.append("[消息类型: ").append(chatForm.getMessageType()).append("]");
        }

        // 最后添加用户的实际消息
        if (messageBuilder.length() > 0) {
            messageBuilder.append("\n\n");
        }
        messageBuilder.append(chatForm.getMessage());

        return messageBuilder.toString();
    }

    /**
     * 健康检查端点
     */
    @Operation(summary = "健康检查", description = "检查AI红娘服务是否正常运行")
    @GetMapping("/health")
    public String health() {
        return "AI红娘服务正常运行 - " + java.time.LocalDateTime.now();
    }

}
