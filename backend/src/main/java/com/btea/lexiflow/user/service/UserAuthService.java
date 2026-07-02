package com.btea.lexiflow.user.service;

import com.btea.lexiflow.user.dto.req.UserLoginReqDTO;
import com.btea.lexiflow.user.dto.req.UserRegisterReqDTO;
import com.btea.lexiflow.user.dto.resp.UserLoginRespDTO;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 22:16
 * @Description: 用户认证接口
 */
public interface UserAuthService {

    /**
     * 登录
     *
     * @param reqDTO 登录请求参数
     * @return 登录响应参数
     */
    UserLoginRespDTO login(UserLoginReqDTO reqDTO);

    /**
     * 注册
     *
     * @param reqDTO 注册请求参数
     */
    void register(UserRegisterReqDTO reqDTO);

    /**
     * 退出登录
     */
    void logout();
}
