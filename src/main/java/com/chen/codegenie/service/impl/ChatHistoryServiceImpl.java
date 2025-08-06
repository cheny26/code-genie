package com.chen.codegenie.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.codegenie.constant.UserConstant;
import com.chen.codegenie.exception.BusinessException;
import com.chen.codegenie.exception.ErrorCode;
import com.chen.codegenie.exception.ThrowUtils;
import com.chen.codegenie.model.dto.chathistory.ChatHistoryQueryRequest;
import com.chen.codegenie.model.entity.App;
import com.chen.codegenie.model.entity.User;
import com.chen.codegenie.model.enums.MessageTypeEnum;
import com.chen.codegenie.model.vo.ChatHistoryVO;
import com.chen.codegenie.service.AppService;
import com.chen.codegenie.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.chen.codegenie.model.entity.ChatHistory;
import com.chen.codegenie.mapper.ChatHistoryMapper;
import com.chen.codegenie.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 *
 * @author chen
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    @Override
    public void validChatHistory(ChatHistory chatHistory, boolean add) {
        if (chatHistory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String message = chatHistory.getMessage();
        String messageType = chatHistory.getMessageType();
        Long appId = chatHistory.getAppId();
        Long userId = chatHistory.getUserId();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
            ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        // 有参数则校验
        if (StrUtil.isNotBlank(message) && message.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容过长");
        }
        if (StrUtil.isNotBlank(messageType)) {
            MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
            ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型不合法");
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        String messageType = chatHistoryQueryRequest.getMessageType();
        String message = chatHistoryQueryRequest.getMessage();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("message_type", messageType)
                .eq("app_id", appId)
                .eq("user_id", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("create_time", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("create_time", false);
        }
        return queryWrapper;
    }

    @Override
    public ChatHistoryVO getChatHistoryVO(ChatHistory chatHistory) {
        ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
        BeanUtil.copyProperties(chatHistory, chatHistoryVO);
        
        // 关联查询应用信息
        Long appId = chatHistory.getAppId();
        if (appId != null && appId > 0) {
            App app = appService.getById(appId);
            if (app != null) {
                chatHistoryVO.setAppName(app.getAppName());
            }
        }
        
        // 关联查询用户信息
        Long userId = chatHistory.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            if (user != null) {
                chatHistoryVO.setUserName(user.getUserName());
            }
        }
        
        return chatHistoryVO;
    }

    @Override
    public Page<ChatHistoryVO> getChatHistoryVOPage(Page<ChatHistory> chatHistoryPage) {
        List<ChatHistory> chatHistoryList = chatHistoryPage.getRecords();
        Page<ChatHistoryVO> chatHistoryVOPage = new Page<>(chatHistoryPage.getPageNumber(), 
                chatHistoryPage.getPageSize(), chatHistoryPage.getTotalRow());
        if (CollUtil.isEmpty(chatHistoryList)) {
            return chatHistoryVOPage;
        }
        
        // 关联查询用户和应用信息
        Set<Long> userIdSet = chatHistoryList.stream().map(ChatHistory::getUserId).collect(Collectors.toSet());
        Set<Long> appIdSet = chatHistoryList.stream().map(ChatHistory::getAppId).collect(Collectors.toSet());
        
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<App>> appIdAppListMap = appService.listByIds(appIdSet).stream()
                .collect(Collectors.groupingBy(App::getId));
        
        // 填充信息
        List<ChatHistoryVO> chatHistoryVOList = chatHistoryList.stream().map(chatHistory -> {
            ChatHistoryVO chatHistoryVO = new ChatHistoryVO();
            BeanUtil.copyProperties(chatHistory, chatHistoryVO);
            
            Long userId = chatHistory.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            if (user != null) {
                chatHistoryVO.setUserName(user.getUserName());
            }
            
            Long appId = chatHistory.getAppId();
            App app = null;
            if (appIdAppListMap.containsKey(appId)) {
                app = appIdAppListMap.get(appId).get(0);
            }
            if (app != null) {
                chatHistoryVO.setAppName(app.getAppName());
            }
            
            return chatHistoryVO;
        }).collect(Collectors.toList());
        
        chatHistoryVOPage.setRecords(chatHistoryVOList);
        return chatHistoryVOPage;
    }

    @Override
    public List<ChatHistoryVO> getLatestChatHistoryByAppId(Long appId, int pageSize) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("app_id = {0}", appId)
                .orderBy("create_time", false);
        
        Page<ChatHistory> chatHistoryPage = this.page(Page.of(1, pageSize), queryWrapper);
        Page<ChatHistoryVO> chatHistoryVOPage = getChatHistoryVOPage(chatHistoryPage);
        return chatHistoryVOPage.getRecords();
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        App app=appService.getById(appId);
        ThrowUtils.throwIf(app==null, ErrorCode.NOT_FOUND_ERROR);
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        ChatHistoryQueryRequest chatHistoryQueryRequest=new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public boolean removeByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("app_id = {0}", appId);
        
        return this.remove(queryWrapper);
    }

    @Override
    public Long saveUserMessage(Long appId, Long userId, String message) {
        return saveChatHistory(appId, userId, message, MessageTypeEnum.USER.getValue());
    }

    @Override
    public Long saveAiMessage(Long appId, Long userId, String message) {
        return saveChatHistory(appId, userId, message, MessageTypeEnum.AI.getValue());
    }

    //SELECT * FROM chat_history
    //WHERE app_id = ?
    //ORDER BY create_time
    //LIMIT 1, maxCount;
    @Override
    public int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount) {
        try{
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                }
                loadedCount++;
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        }catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

    /**
     * 保存对话历史的通用方法
     */
    private Long saveChatHistory(Long appId, Long userId, String message, String messageType) {
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        validChatHistory(chatHistory, true);
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存对话历史失败");
        
        return chatHistory.getId();
    }
}
