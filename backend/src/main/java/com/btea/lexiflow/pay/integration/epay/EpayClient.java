package com.btea.lexiflow.pay.integration.epay;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.config.EpayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 易支付接口客户端
 */
@Slf4j
@Component
public class EpayClient {

    private final EpayProperties properties;
    private final RestClient restClient;

    public EpayClient(EpayProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(getPositive(properties.getConnectTimeoutMillis(), 5000)))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(getPositive(properties.getReadTimeoutMillis(), 10000)));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * 查询易支付订单
     *
     * @param orderNo 商户订单号
     * @return 易支付订单信息
     */
    public EpayOrderQueryResponse queryOrder(String orderNo) {
        validateQueryConfig();
        try {
            return restClient.get()
                    .uri(buildQueryBaseUrl(), builder -> builder
                            .queryParam("act", "order")
                            .queryParam("pid", properties.getMerchantId())
                            .queryParam("key", properties.getMerchantKey())
                            .queryParam("out_trade_no", orderNo)
                            .build())
                    .retrieve()
                    .body(EpayOrderQueryResponse.class);
        } catch (Exception e) {
            log.warn("易支付订单查询失败: orderNo={}, errorType={}", orderNo, e.getClass().getSimpleName());
            throw new ClientException(BaseErrorCode.PAYMENT_PROVIDER_ERROR);
        }
    }

    private String buildQueryBaseUrl() {
        String baseUrl = properties.getBaseUrl();
        String queryPath = properties.getQueryPath();
        if (baseUrl.endsWith("/") && queryPath.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + queryPath;
        }
        if (!baseUrl.endsWith("/") && !queryPath.startsWith("/")) {
            return baseUrl + "/" + queryPath;
        }
        return baseUrl + queryPath;
    }

    private void validateQueryConfig() {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()
                || properties.getQueryPath() == null || properties.getQueryPath().isBlank()
                || properties.getMerchantId() == null || properties.getMerchantId().isBlank()
                || properties.getMerchantKey() == null || properties.getMerchantKey().isBlank()) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
    }

    private int getPositive(Integer value, int fallback) {
        return value == null || value <= 0 ? fallback : value;
    }
}
