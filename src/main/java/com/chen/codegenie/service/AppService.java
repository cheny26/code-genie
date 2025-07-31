package com.chen.codegenie.service;

import com.chen.codegenie.model.dto.app.AppQueryRequest;
import com.chen.codegenie.model.entity.App;
import com.chen.codegenie.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author chen
 */
public interface AppService extends IService<App> {

    /**
     * 校验应用
     *
     * @param app 应用信息
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取应用封装
     *
     * @param app 应用信息
     * @param request 请求对象
     * @return 应用封装
     */
    AppVO getAppVO(App app, HttpServletRequest request);

    /**
     * 分页获取应用封装
     *
     * @param appList 应用列表
     * @param request 请求对象
     * @return 应用封装列表
     */
    List<AppVO> getAppVOList(List<App> appList, HttpServletRequest request);

    /**
     * 获取精选应用查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest);
}
