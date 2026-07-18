package com.btea.lexiflow.pay.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.pay.dto.resp.CreditAccountRespDTO;
import com.btea.lexiflow.pay.service.CreditAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付与Credits控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pay")
public class PaymentController {

    private final CreditAccountService creditAccountService;

    /**
     * 获取当前用户Credits账户
     */
    @GetMapping("/credits/account")
    public Result<CreditAccountRespDTO> getCreditAccount() {
        return Results.success(creditAccountService.getCurrentAccount());
    }
}
