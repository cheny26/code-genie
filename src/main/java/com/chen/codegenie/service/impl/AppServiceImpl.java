package com.chen.codegenie.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.codegenie.exception.ErrorCode;
import com.chen.codegenie.exception.ThrowUtils;
import com.chen.codegenie.model.dto.app.AppQueryRequest;
import com.chen.codegenie.model.entity.App;
import com.chen.codegenie.model.entity.User;
import com.chen.codegenie.model.vo.AppVO;
import com.chen.codegenie.service.AppService;
import com.chen.codegenie.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.chen.codegenie.mapper.AppMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * 应用 服务层实现。
 *
 * @author chen
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage());
        }
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");
        }
        // 有参数则校验
        if (StrUtil.isNotBlank(appName) && appName.length() > 80) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage() + "，应用名称过长");
        }
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 8192) {
            throw new RuntimeException(ErrorCode.PARAMS_ERROR.getMessage() + "，初始化提示词过长");
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        queryWrapper.eq("id", id, id != null);
        queryWrapper.like("app_name", appName, StrUtil.isNotBlank(appName));
        queryWrapper.like("init_prompt", initPrompt, StrUtil.isNotBlank(initPrompt));
        queryWrapper.eq("code_gen_type", codeGenType, StrUtil.isNotBlank(codeGenType));
        queryWrapper.eq("deploy_key", deployKey, StrUtil.isNotBlank(deployKey));
        queryWrapper.eq("priority", priority, priority != null);
        queryWrapper.eq("user_id", userId, userId != null);
        
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            switch (sortField) {
                case "id":
                    queryWrapper.orderBy("id", isAsc);
                    break;
                case "appName":
                    queryWrapper.orderBy("app_name", isAsc);
                    break;
                case "priority":
                    queryWrapper.orderBy("priority", isAsc);
                    break;
                case "createTime":
                    queryWrapper.orderBy("create_time", isAsc);
                    break;
                case "updateTime":
                    queryWrapper.orderBy("update_time", isAsc);
                    break;
                default:
                    queryWrapper.orderBy("create_time", false);
                    break;
            }
        } else {
            queryWrapper.orderBy("create_time", false);
        }
        
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        
        // 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        appVO.setUser(userService.getUserVO(user));
        
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList, HttpServletRequest request) {
        if (CollUtil.isEmpty(appList)) {
            return List.of();
        }
        
        // 关联查询用户信息
        Set<Long> userIdSet = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        
        // 填充信息
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtil.copyProperties(app, appVO);
            Long userId = app.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        // 精选应用：优先级大于 0
        queryWrapper.gt("priority", 0);
        // 按优先级降序排列
        queryWrapper.orderBy("priority", false);
        queryWrapper.orderBy("create_time", false);
        return queryWrapper;
    }
}
