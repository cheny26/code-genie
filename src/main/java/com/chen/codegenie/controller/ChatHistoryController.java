package com.chen.codegenie.controller;

import com.chen.codegenie.annotation.AuthCheck;
import com.chen.codegenie.common.BaseResponse;
import com.chen.codegenie.common.DeleteRequest;
import com.chen.codegenie.common.ResultUtils;
import com.chen.codegenie.constant.UserConstant;
import com.chen.codegenie.exception.BusinessException;
import com.chen.codegenie.exception.ErrorCode;
import com.chen.codegenie.exception.ThrowUtils;
import com.chen.codegenie.model.dto.chathistory.ChatHistoryQueryRequest;
import com.chen.codegenie.model.entity.App;
import com.chen.codegenie.model.entity.ChatHistory;
import com.chen.codegenie.model.entity.User;
import com.chen.codegenie.service.AppService;
import com.chen.codegenie.service.ChatHistoryService;
import com.chen.codegenie.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author chen
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;


    /**
     * 删除对话历史
     *
     * @param deleteRequest 删除请求
     * @param request       请求对象
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChatHistory(@RequestBody DeleteRequest deleteRequest,
                                                   HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        
        // 判断是否存在
        ChatHistory oldChatHistory = chatHistoryService.getById(id);
        ThrowUtils.throwIf(oldChatHistory == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 仅本人或管理员可删除
        if (!oldChatHistory.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        boolean b = chatHistoryService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 分页获取对话历史列表（仅管理员）
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listChatHistoryVOByPage(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(pageNum, pageSize),
                chatHistoryService.getQueryWrapper(chatHistoryQueryRequest));
        return ResultUtils.success(chatHistoryPage);
    }


    /**
     * 根据应用ID获取最新的对话历史
     *
     * @param appId   应用ID
     * @param request 请求对象
     * @return 对话历史列表
     */
    @GetMapping("/latest/{appId}")
    public BaseResponse<List<ChatHistory>> getLatestChatHistoryByAppId(@PathVariable Long appId,
                                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        
        // 校验应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        User loginUser = userService.getLoginUser(request);
        // 仅应用创建者和管理员可查看
        if (!app.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        
        List<ChatHistory> chatHistoryList = chatHistoryService.getLatestChatHistoryByAppId(appId, 10);
        return ResultUtils.success(chatHistoryList);
    }


    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }



}
