/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员控制器
 */
package com.btea.lexiflow.admin.controller;

import com.btea.lexiflow.admin.dto.req.AdminGrantCreditsReqDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditUsageRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditsSummaryRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminUserRespDTO;
import com.btea.lexiflow.admin.service.AdminService;
import com.btea.lexiflow.common.convention.result.PageRespDTO;
import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 分页查询用户注册信息。
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return 用户注册分页数据
     */
    @GetMapping("/users")
    public Result<PageRespDTO<AdminUserRespDTO>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Results.success(adminService.listUsers(page, pageSize));
    }

    /**
     * 查询Credits使用汇总。
     *
     * @return Credits使用汇总
     */
    @GetMapping("/credits/summary")
    public Result<AdminCreditsSummaryRespDTO> getCreditsSummary() {
        return Results.success(adminService.getCreditsSummary());
    }

    /**
     * 分页查询全部用户Credits使用记录。
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return Credits使用记录分页数据
     */
    @GetMapping("/credits/usage")
    public Result<PageRespDTO<AdminCreditUsageRespDTO>> listCreditUsage(@RequestParam(defaultValue = "1") Integer page,
                                                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return Results.success(adminService.listCreditUsage(page, pageSize));
    }

    /**
     * 向指定用户赠送Credits。
     *
     * @param reqDTO 赠送Credits请求参数
     * @return 操作结果
     */
    @PostMapping("/credits/grant")
    public Result<Void> grantCredits(@RequestBody @Valid AdminGrantCreditsReqDTO reqDTO) {
        adminService.grantCredits(reqDTO);
        return Results.success();
    }
}
