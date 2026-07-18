package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.model.PaymentConfirmation;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付入账事务服务接口
 */
public interface PaymentSettlementService {

    /**
     * 确认支付并幂等入账Credits
     *
     * @param confirmation 支付确认参数
     */
    void confirmAndCredit(PaymentConfirmation confirmation);
}
