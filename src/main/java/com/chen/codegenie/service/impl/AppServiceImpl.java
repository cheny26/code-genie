package com.chen.codegenie.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.codegenie.constant.AppConstant;
import com.chen.codegenie.core.AiCodeGeneratorFacade;
import com.chen.codegenie.exception.BusinessException;
import com.chen.codegenie.exception.ErrorCode;
import com.chen.codegenie.exception.ThrowUtils;
import com.chen.codegenie.model.dto.app.AppQueryRequest;
import com.chen.codegenie.model.entity.App;
import com.chen.codegenie.model.entity.User;
import com.chen.codegenie.model.enums.CodeGenTypeEnum;
import com.chen.codegenie.model.vo.AppVO;
import com.chen.codegenie.service.AppService;
import com.chen.codegenie.service.ChatHistoryService;
import com.chen.codegenie.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.chen.codegenie.mapper.AppMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.io.File;
import java.time.LocalDateTime;
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

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    //todo 优化架构，存在循环依赖问题
    @Resource
    @Lazy
    private ChatHistoryService    chatHistoryService;

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
        if (appQueryRequest == null) {
            return new QueryWrapper();
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
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id",id)
                .eq("code_gen_type", codeGenType)
                .eq("deploy_key", deployKey)
                .eq("priority", priority)
                .eq("user_id", userId)
                .like("app_name", appName)
                .like("init_prompt", initPrompt);
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            switch (sortField) {
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
    public Flux<String> chatToGen(Long appId,String userPrompt, User loginUser) {
        //参数校验
        App app = this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR);
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum=CodeGenTypeEnum.getEnumByValue(codeGenType);
        chatHistoryService.saveUserMessage(appId,loginUser.getId(),userPrompt);
        Flux<String> ans= aiCodeGeneratorFacade.generateAndSaveCodeStream(userPrompt,codeGenTypeEnum,app.getId());
        StringBuilder aiMessage=new StringBuilder();
        return ans.map(chunk->{
            aiMessage.append(chunk);
            return chunk;
        }).doOnComplete(()->{
            chatHistoryService.saveAiMessage(appId,loginUser.getId(),aiMessage.toString());
        }).doOnError(e->{
            String errorMessage="A回复失败"+e.getMessage();
            chatHistoryService.saveAiMessage(appId,loginUser.getId(),errorMessage);
        });
    }

    @Override
    public String deploy(Long appId, User loginUser) {
        //参数验证
        ThrowUtils.throwIf(appId==null || appId<0,ErrorCode.PARAMS_ERROR );
        App app=this.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()),ErrorCode.NO_AUTH_ERROR);
        String deployKey=app.getDeployKey();
        //TODO 添加去重
        if(StrUtil.isBlank(deployKey)){
            deployKey= RandomUtil.randomString(6);
        }
        String codeGenType = app.getCodeGenType();
        String outputFilePath= AppConstant.CODE_OUTPUT_ROOT_DIR+ File.separator+codeGenType+"_"+appId;
        //判断应用代码是否存在
        File sourceFile=new File(outputFilePath);
        if(!sourceFile.exists() || !sourceFile.isDirectory()){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"应用代码不存在");
        }
        //判断deployKey是否已经存在
        String deployPath=AppConstant.CODE_DEPLOY_ROOT_DIR+File.separator+deployKey;
        // 复制文件到部署目录
        try {
            FileUtil.copyContent(sourceFile, new File(deployPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        //更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

}
