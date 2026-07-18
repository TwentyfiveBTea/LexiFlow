package com.btea.lexiflow.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
     * 创建文章处理任务时的最小预占Credits
     */
    private Long initialReservationCredits = 1L;

    /**
     * 单页OCR图片输入Token估算值
     */
    private Long ocrImageInputTokenEstimate = 2_000L;

    /**
     * 文本输入字符数与Token数的保守换算比例
     */
    private BigDecimal charactersPerInputToken = BigDecimal.valueOf(2L);

    /**
     * 翻译输出Token相对于输入Token的估算倍率
     */
    private BigDecimal translationOutputTokenRatio = BigDecimal.valueOf(1.5D);

    /**
     * 预占安全倍率
     */
    private BigDecimal reservationSafetyMultiplier = BigDecimal.valueOf(1.2D);

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
