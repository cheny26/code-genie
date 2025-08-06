package com.chen.codegenie.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史视图对象（脱敏）
 * @author chen
 */
@Data
public class ChatHistoryVO implements Serializable {

    /**
     * id
     */
    private Long id;

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

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建用户名
     */
    private String userName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;
}