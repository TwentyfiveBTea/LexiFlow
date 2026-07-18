package com.btea.lexiflow.user.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.user.dto.req.UserChangePasswordReqDTO;
import com.btea.lexiflow.user.dto.req.UserChangeUsernameReqDTO;
import com.btea.lexiflow.user.dto.resp.UserProfileRespDTO;
import com.btea.lexiflow.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/3 14:53
 * @Description: 用户个人资料控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 更改头像
     *
     * @param file 头像文件
     * @return 响应结果
 */
    @PostMapping("/change-avatar")
    public Result<String> changeAvatar(@RequestParam("avatar") MultipartFile file) {
        return Results.success(userProfileService.changeAvatar(file));
    }

    /**
     * 更改用户名
     *
     * @param reqDTO 更改用户名请求参数
     * @return 响应结果
     */
    @PostMapping("/change-username")
    public Result<Void> changeUsername(@RequestBody @Validated UserChangeUsernameReqDTO reqDTO) {
        userProfileService.changeUsername(reqDTO);
        return Results.success();
    }

    /**
     * 更改密码
     *
     * @param reqDTO 更改密码请求参数
     * @return 响应结果
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody @Validated UserChangePasswordReqDTO reqDTO) {
        userProfileService.changePassword(reqDTO);
        return Results.success();
    }

    /**
     * 获取头像弹窗所需的用户资料
     *
     * @return 用户头像、邮箱和注册天数
     */
    @GetMapping
    public Result<UserProfileRespDTO> getProfile() {
        return Results.success(userProfileService.getProfile());
    }
}
