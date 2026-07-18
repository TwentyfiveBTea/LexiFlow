package com.btea.lexiflow.pay.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单响应参数
 */
@Data
@Builder
public class PaymentOrderRespDTO {

    /**
     * LexiFlow商户订单号
     */
    private String orderNo;

    /**
     * 商品名称
     */
    private String subject;

    /**
     * 支付金额，单位为分
     */
    private Long amountMinor;

    /**
     * 应入账的Credits数量
     */
    private Long totalCredits;

    /**
     * 支付设备类型
     */
    private String deviceType;

    /**
     * 订单状态：0=待支付，1=已支付，2=已过期，3=支付异常
     */
    private Integer orderStatus;

    /**
     * Credits入账状态：0=未入账，1=已入账
     */
    private Integer creditStatus;

    /**
     * 订单过期时间
     */
    private Date expiresAt;

    /**
     * 支付成功时间
     */
    private Date paidAt;

    /**
     * Credits入账时间
     */
    private Date creditedAt;

    /**
     * 订单创建时间
     */
    private Date createdAt;
}
