package com.btea.lexiflow.pay.task;

import com.btea.lexiflow.pay.config.CreditBillingProperties;
import com.btea.lexiflow.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单补偿任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOrderReconcileJob {

    private final PaymentService paymentService;
    private final CreditBillingProperties billingProperties;

    /**
     * 主动查询待处理支付订单并同步订单状态
     */
    @Scheduled(fixedDelayString = "${lexiflow.pay.billing.payment-reconcile-delay-millis:30000}")
    public void reconcileOrders() {
        int limit = getBatchSize();
        for (String orderNo : paymentService.listReconcileOrderNos(limit)) {
            try {
                paymentService.reconcileOrder(orderNo);
            } catch (Exception e) {
                log.warn("支付订单补偿失败: orderNo={}, errorType={}", orderNo, e.getClass().getSimpleName());
            }
        }
        paymentService.expirePendingOrders();
    }

    private int getBatchSize() {
        Integer value = billingProperties.getReconcileBatchSize();
        return value == null ? 50 : Math.min(Math.max(value, 1), 100);
    }
}
