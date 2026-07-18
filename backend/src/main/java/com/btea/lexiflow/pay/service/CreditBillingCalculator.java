package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.config.CreditBillingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits计费计算器
 */
@Component
@RequiredArgsConstructor
public class CreditBillingCalculator {

    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000L);

    private final CreditBillingProperties properties;

    /**
     * 根据输入和输出Token计算Credits
     *
     * @param inputTokens 输入Token数
     * @param outputTokens 输出Token数
     * @return 应消耗的Credits
     */
    public long calculate(long inputTokens, long outputTokens) {
        if (inputTokens < 0 || outputTokens < 0) {
            throw new ClientException(BaseErrorCode.AI_USAGE_INVALID);
        }
        BigDecimal inputCost = BigDecimal.valueOf(inputTokens)
                .multiply(BigDecimal.valueOf(getInputRatePerMillion()));
        BigDecimal outputCost = BigDecimal.valueOf(outputTokens)
                .multiply(BigDecimal.valueOf(getOutputRatePerMillion()));
        return inputCost.add(outputCost)
                .divide(ONE_MILLION, 0, RoundingMode.CEILING)
                .longValueExact();
    }

    public long getInputRatePerMillion() {
        Long value = properties.getInputRatePerMillion();
        if (value == null || value < 0) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
        return value;
    }

    public long getOutputRatePerMillion() {
        Long value = properties.getOutputRatePerMillion();
        if (value == null || value < 0) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
        return value;
    }
}
