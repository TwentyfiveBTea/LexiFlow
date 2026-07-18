package com.btea.lexiflow.pay.integration.epay;

import com.btea.lexiflow.pay.config.EpayProperties;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpaySignerTest {

    @Test
    void shouldGenerateSignatureUsingEpayRules() {
        EpayProperties properties = new EpayProperties();
        properties.setMerchantKey("key");
        EpaySigner signer = new EpaySigner(properties);
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("name", "LexiFlow Credits充值");
        parameters.put("money", "10.00");
        parameters.put("pid", "1001");
        parameters.put("empty", "");
        parameters.put("sign", "ignored");
        parameters.put("sign_type", "MD5");

        String signature = signer.sign(parameters);

        assertEquals(32, signature.length());
        assertEquals(signature.toLowerCase(), signature);
        parameters.put("sign", signature);
        assertTrue(signer.verify(parameters));
        parameters.put("money", "20.00");
        assertFalse(signer.verify(parameters));
    }
}
