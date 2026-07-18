package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.config.CreditBillingProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreditBillingCalculatorTest {

    @Test
    void shouldCalculateCreditsUsingConfiguredRatesAndCeiling() {
        CreditBillingProperties properties = new CreditBillingProperties();
        properties.setInputRatePerMillion(60_000L);
        properties.setOutputRatePerMillion(360_000L);
        CreditBillingCalculator calculator = new CreditBillingCalculator(properties);

        assertEquals(60_000L, calculator.calculate(1_000_000L, 0L));
        assertEquals(360_000L, calculator.calculate(0L, 1_000_000L));
        assertEquals(13_200L, calculator.calculate(100_000L, 20_000L));
        assertEquals(1L, calculator.calculate(1L, 0L));
        assertEquals(0L, calculator.calculate(0L, 0L));
    }
}
