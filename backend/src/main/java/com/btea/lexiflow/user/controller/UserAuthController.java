package com.btea.lexiflow.user.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.user.dto.req.UserLoginReqDTO;
import com.btea.lexiflow.user.dto.req.UserRegisterReqDTO;
import com.btea.lexiflow.user.dto.resp.UserLoginRespDTO;
import com.btea.lexiflow.user.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 22:14
 * @Description: 用户认证控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;

    /**
     * 登录
     *
     * @param reqDTO 登录请求参数
     * @return 响应结果
     */
    @PostMapping("/login")
    public Result<UserLoginRespDTO> login(@RequestBody @Validated UserLoginReqDTO reqDTO) {
        return Results.success(userAuthService.login(reqDTO));
    }

    /**
     * 注册
     *
     * @param reqDTO 注册请求参数
     * @return 响应结果
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Validated UserRegisterReqDTO reqDTO) {
        userAuthService.register(reqDTO);
        return Results.success();
    }

    /**
     * 退出登陆
     *
     * @return 响应结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userAuthService.logout();
        return Results.success();
    }
}
