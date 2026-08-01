package com.btea.lexiflow.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits计费配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "lexiflow.pay.billing")
public class CreditBillingProperties {

    /**
     * 每人民币1元兑换的Credits
     */
    private Long creditsPerYuan = 10_000L;

    /**
     * 每百万输入Token对应的Credits
     */
    private Long inputRatePerMillion = 60_000L;

    /**
     * 每百万输出Token对应的Credits
     */
    private Long outputRatePerMillion = 360_000L;

    /**
     * Credits预占有效时间，单位为分钟
     */
    private Integer reservationTtlMinutes = 180;

    /**
     * 支付订单补偿任务执行间隔，单位为毫秒
     */
    private Long paymentReconcileDelayMillis = 30_000L;

    /**
     * Credits预占补偿任务执行间隔，单位为毫秒
     */
    private Long reservationReconcileDelayMillis = 60_000L;

    /**
     * 定时任务单批处理数量
     */
    private Integer reconcileBatchSize = 50;
}
