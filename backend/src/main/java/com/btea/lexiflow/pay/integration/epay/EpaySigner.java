package com.btea.lexiflow.pay.integration.epay;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.config.EpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 易支付MD5签名组件
 */
@Component
@RequiredArgsConstructor
public class EpaySigner {

    private final EpayProperties epayProperties;

    /**
     * 生成易支付签名
     *
     * @param parameters 待签名参数
     * @return 小写MD5签名
     */
    public String sign(Map<String, String> parameters) {
        String merchantKey = epayProperties.getMerchantKey();
        if (merchantKey == null || merchantKey.isBlank()) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
        String content = parameters.entrySet().stream()
                .filter(entry -> !"sign".equals(entry.getKey()))
                .filter(entry -> !"sign_type".equals(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest((content + merchantKey).getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
    }

    /**
     * 验证易支付签名
     *
     * @param parameters 通知参数
     * @return 是否验证通过
     */
    public boolean verify(Map<String, String> parameters) {
        String receivedSign = parameters.get("sign");
        if (receivedSign == null || receivedSign.isBlank()) {
            return false;
        }
        byte[] expected = sign(parameters).getBytes(StandardCharsets.US_ASCII);
        byte[] actual = receivedSign.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, actual);
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte each : bytes) {
            builder.append(String.format("%02x", each));
        }
        return builder.toString();
    }
}
