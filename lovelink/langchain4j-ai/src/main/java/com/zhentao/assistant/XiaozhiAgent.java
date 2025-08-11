package com.zhentao.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
@AiService(wiringMode = EXPLICIT,
        streamingChatModel = "qwenStreamingChatModel", // 配置流式模型
        chatMemoryProvider = "chatMemoryProviderxiaozhi", // 配置聊天记忆
        tools = "appointmentTools", // 配置AI红娘工具
        contentRetriever = "contentRetrieverXiaozhiPincone") // 配置向量存储
public interface XiaozhiAgent {
    @SystemMessage(fromResource = "xiaozhi-prompt-template.txt") //系统提示词 文件加载的方式
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
