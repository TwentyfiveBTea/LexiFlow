package com.btea.lexiflow.pay.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付常量类
 */
public final class PaymentConstant {

    private PaymentConstant() {
    }

    /**
     * 支付订单状态：待支付
     */
    public static final int ORDER_STATUS_PENDING = 0;

    /**
     * 支付订单状态：已支付
     */
    public static final int ORDER_STATUS_PAID = 1;

    /**
     * 支付订单状态：已过期
     */
    public static final int ORDER_STATUS_EXPIRED = 2;

    /**
     * 支付订单状态：支付异常
     */
    public static final int ORDER_STATUS_ABNORMAL = 3;

    /**
     * Credits入账状态：未入账
     */
    public static final int CREDIT_STATUS_PENDING = 0;

    /**
     * Credits入账状态：已入账
     */
    public static final int CREDIT_STATUS_CREDITED = 1;
}
