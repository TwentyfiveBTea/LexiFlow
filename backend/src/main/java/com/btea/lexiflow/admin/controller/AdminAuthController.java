/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员认证控制器
 */
package com.btea.lexiflow.admin.controller;

import com.btea.lexiflow.admin.dto.req.AdminLoginReqDTO;
import com.btea.lexiflow.admin.dto.resp.AdminLoginRespDTO;
import com.btea.lexiflow.admin.service.AdminService;
import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员认证控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;

    /**
     * 管理员登录。
     *
     * @param reqDTO 管理员登录请求参数
     * @return 管理员登录信息
     */
    @PostMapping("/login")
    public Result<AdminLoginRespDTO> login(@RequestBody @Valid AdminLoginReqDTO reqDTO) {
        return Results.success(adminService.login(reqDTO));
    }
}
