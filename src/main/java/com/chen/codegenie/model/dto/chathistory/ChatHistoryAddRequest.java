package com.chen.codegenie.model.dto.chathistory;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 对话历史添加请求
 * @author chen
 */
@Data
public class ChatHistoryAddRequest implements Serializable {

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型：user/ai
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    @Serial
    private static final long serialVersionUID = 1L;
}