package com.chen.codegenie.service;

import com.chen.codegenie.model.dto.user.UserQueryRequest;
import com.chen.codegenie.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.chen.codegenie.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author chen
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param email         邮箱
     * @param userName      用户名
     * @param userPassword  密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String email,String userName, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userName     用户名
     * @param userPassword 用户密码
     * @param request      request
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userName, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request request
     * @return 用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);


    /**
     * 用户注销
     *
     * @return 退出登录是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据查询条件构造数据查询参数
     *
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 加密
     *
     * @param userPassword 用户密码
     * @return 加密后的用户密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 是否为管理员
     *
     * @param request 请求对象
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}
