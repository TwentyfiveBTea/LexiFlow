package com.btea.lexiflow.pay.model;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付成功确认参数
 */
public record PaymentConfirmation(
        String orderNo,
        String providerTradeNo,
        String upstreamTradeNo,
        Long amountMinor,
        Date paidAt) {
}
