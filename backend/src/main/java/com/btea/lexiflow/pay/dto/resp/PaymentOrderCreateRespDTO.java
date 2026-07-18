package com.btea.lexiflow.pay.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单创建响应参数
 */
@Data
@Builder
public class PaymentOrderCreateRespDTO {

    /**
     * LexiFlow商户订单号
     */
    private String orderNo;

    /**
     * 支付金额，单位为分
     */
    private Long amountMinor;

    /**
     * 支付成功后应入账的Credits数量
     */
    private Long totalCredits;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单过期时间
     */
    private Date expiresAt;

    /**
     * 易支付页面提交地址
     */
    private String submitUrl;

    /**
     * 页面提交方式
     */
    private String method;

    /**
     * 已签名的页面表单参数
     */
    private Map<String, String> parameters;
}
