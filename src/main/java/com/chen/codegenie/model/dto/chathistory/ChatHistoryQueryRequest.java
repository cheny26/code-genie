package com.chen.codegenie.model.dto.chathistory;

import com.chen.codegenie.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求
 * @author chen
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 消息类型：user/ai
     */
    private String messageType;

    /**
     * 消息内容（模糊查询）
     */
    private String message;

    /**
     * 游标查询：最后一条记录的创建时间
     * 用于分页查询，获取比这个更早的消息
     */
    private LocalDateTime lastCreateTime;

    @Serial
    private static final long serialVersionUID = 1L;
}