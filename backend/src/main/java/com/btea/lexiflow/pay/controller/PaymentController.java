package com.btea.lexiflow.pay.controller;

import com.btea.lexiflow.common.convention.result.Result;
import com.btea.lexiflow.common.convention.result.Results;
import com.btea.lexiflow.pay.dto.req.PaymentOrderCreateReqDTO;
import com.btea.lexiflow.pay.dto.resp.CreditAccountRespDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderCreateRespDTO;
import com.btea.lexiflow.pay.service.CreditAccountService;
import com.btea.lexiflow.pay.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final PaymentService paymentService;
    private final CreditAccountService creditAccountService;

    /**
     * 创建Credits充值订单
     */
    @PostMapping("/orders")
    public Result<PaymentOrderCreateRespDTO> createOrder(
            @RequestBody @Valid PaymentOrderCreateReqDTO reqDTO) {
        return Results.success(paymentService.createOrder(reqDTO));
    }

    /**
     * 获取当前用户Credits账户
     */
    @GetMapping("/credits/account")
    public Result<CreditAccountRespDTO> getCreditAccount() {
        return Results.success(creditAccountService.getCurrentAccount());
    }
}
