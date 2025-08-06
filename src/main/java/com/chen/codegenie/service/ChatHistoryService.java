package com.chen.codegenie.service;

import com.chen.codegenie.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.chen.codegenie.model.dto.chathistory.ChatHistoryQueryRequest;
import com.chen.codegenie.model.entity.ChatHistory;
import com.chen.codegenie.model.vo.ChatHistoryVO;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author chen
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 校验对话历史
     *
     * @param chatHistory 对话历史
     * @param add         是否为创建校验
     */
    void validChatHistory(ChatHistory chatHistory, boolean add);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 获取对话历史封装
     *
     * @param chatHistory 对话历史
     * @return 对话历史封装
     */
    ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory);

    /**
     * 分页获取对话历史封装
     *
     * @param chatHistoryPage 对话历史分页
     * @return 对话历史封装分页
     */
    Page<ChatHistoryVO> getChatHistoryVOPage(Page<ChatHistory> chatHistoryPage);

    /**
     * 根据应用ID获取最新的对话历史
     *
     * @param appId    应用ID
     * @param pageSize 页面大小
     * @return 对话历史列表
     */
    List<ChatHistoryVO> getLatestChatHistoryByAppId(Long appId, int pageSize);

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条消息的时间
     * @param loginUser      登录用户
     * @return 消息
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 根据应用ID删除所有对话历史（级联删除）
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean removeByAppId(Long appId);

    /**
     * 保存用户消息
     *
     * @param appId   应用ID
     * @param userId  用户ID
     * @param message 消息内容
     * @return 对话历史ID
     */
    Long saveUserMessage(Long appId, Long userId, String message);

    /**
     * 保存AI消息
     *
     * @param appId   应用ID
     * @param userId  用户ID
     * @param message 消息内容
     * @return 对话历史ID
     */
    Long saveAiMessage(Long appId, Long userId, String message);

    /**
     * 加载数据库的历史消息到AI记忆中
     * @param appId 应用ID
     * @param chatMemory AI记忆
     * @param maxCount 从数据库加载的最大消息数
     * @return 加载的消息数
     */
    int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount);
}