package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.config.CreditBillingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: AI模型请求Credits预占估算器
 */
@Component
@RequiredArgsConstructor
public class AiRequestReservationEstimator {

    private final CreditBillingProperties properties;
    private final CreditBillingCalculator billingCalculator;

    /**
     * 估算单页OCR请求Credits
     *
     * @param maxOutputTokens 最大输出Token数
     * @return 预占Credits
     */
    public long estimateOcr(Integer maxOutputTokens) {
        long inputTokens = getPositive(properties.getOcrImageInputTokenEstimate(), 2_000L);
        long outputTokens = maxOutputTokens == null ? 8_192L : Math.max(maxOutputTokens.longValue(), 1L);
        return applySafety(billingCalculator.calculate(inputTokens, outputTokens));
    }

    /**
     * 估算文本模型请求Credits
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return 预占Credits
     */
    public long estimateText(String systemPrompt, String userPrompt) {
        long characters = codePointCount(systemPrompt) + codePointCount(userPrompt);
        BigDecimal charactersPerToken = properties.getCharactersPerInputToken();
        if (charactersPerToken == null || charactersPerToken.signum() <= 0) {
            charactersPerToken = BigDecimal.valueOf(2L);
        }
        long inputTokens = BigDecimal.valueOf(characters)
                .divide(charactersPerToken, 0, RoundingMode.CEILING)
                .longValueExact();
        BigDecimal outputRatio = properties.getTranslationOutputTokenRatio();
        if (outputRatio == null || outputRatio.signum() <= 0) {
            outputRatio = BigDecimal.valueOf(1.5D);
        }
        long outputTokens = BigDecimal.valueOf(inputTokens)
                .multiply(outputRatio)
                .setScale(0, RoundingMode.CEILING)
                .longValueExact();
        return applySafety(billingCalculator.calculate(inputTokens, outputTokens));
    }

    private long applySafety(long credits) {
        BigDecimal multiplier = properties.getReservationSafetyMultiplier();
        if (multiplier == null || multiplier.compareTo(BigDecimal.ONE) < 0) {
            multiplier = BigDecimal.valueOf(1.2D);
        }
        return BigDecimal.valueOf(Math.max(credits, 1L))
                .multiply(multiplier)
                .setScale(0, RoundingMode.CEILING)
                .longValueExact();
    }

    private long codePointCount(String value) {
        return value == null ? 0L : value.codePoints().count();
    }

    private long getPositive(Long value, long fallback) {
        return value == null || value <= 0 ? fallback : value;
    }
}
