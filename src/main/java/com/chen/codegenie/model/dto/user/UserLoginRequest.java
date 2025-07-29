package com.chen.codegenie.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录
 * @author chen
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String userPassword;
}