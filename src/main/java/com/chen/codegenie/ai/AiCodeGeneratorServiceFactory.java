package com.chen.codegenie.ai;

import com.chen.codegenie.model.entity.ChatHistory;
import com.chen.codegenie.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;

/**
 * @author chen
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    @Lazy
    private ChatHistoryService chatHistoryService;

    private final RedisChatMemoryStore redisChatMemoryStore;

    public AiCodeGeneratorServiceFactory(RedisChatMemoryStore redisChatMemoryStore) {
        this.redisChatMemoryStore = redisChatMemoryStore;
    }

    private final Cache<Long,AiCodeGeneratorService> serviceCache= Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 默认提供一个 Bean
     */
    //@Bean
    //public AiCodeGeneratorService aiCodeGeneratorService() {
    //    return getAiCodeGeneratorService(0L);
    //}

    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId,this::createAiCodeGeneratorService);
    }


    //@Bean
    public AiCodeGeneratorService createAiCodeGeneratorService(long appId) {

        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        ChatMemory chatMemory=MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                // 根据 id 构建独立的对话记忆
                .chatMemory(chatMemory)
                .build();
    }
}
