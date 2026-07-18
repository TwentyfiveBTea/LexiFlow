package com.btea.lexiflow.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 易支付配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "lexiflow.pay.epay")
public class EpayProperties {

    /**
     * 易支付基础地址
     */
    private String baseUrl;

    /**
     * 页面跳转支付路径
     */
    private String submitPath = "/xpay/epay/submit.php";

    /**
     * 订单查询路径
     */
    private String queryPath = "/xpay/epay/api.php";

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 商户密钥
     */
    private String merchantKey;

    /**
     * 支付异步通知地址
     */
    private String notifyUrl;

    /**
     * 支付完成页面跳转地址
     */
    private String returnUrl;

    /**
     * 网站名称
     */
    private String siteName = "LexiFlow";

    /**
     * 商品名称
     */
    private String subject = "LexiFlow Credits充值";

    /**
     * 支付方式，当前为alipay
     */
    private String paymentType = "alipay";

    /**
     * 默认设备类型
     */
    private String defaultDevice = "pc";

    /**
     * 支持的设备类型
     */
    private Set<String> supportedDevices = new LinkedHashSet<>(Set.of("pc", "mobile", "qq", "wechat", "alipay"));

    /**
     * 订单有效时间，单位为分钟
     */
    private Integer orderExpireMinutes = 5;

    /**
     * 主动查单连接超时时间，单位为毫秒
     */
    private Integer connectTimeoutMillis = 5000;

    /**
     * 主动查单响应超时时间，单位为毫秒
     */
    private Integer readTimeoutMillis = 10000;
}
