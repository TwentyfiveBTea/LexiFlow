package com.btea.lexiflow.pay.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits常量类
 */
public final class CreditConstant {

    private CreditConstant() {
    }

    /**
     * Credits账户状态：冻结
     */
    public static final int ACCOUNT_STATUS_FROZEN = 0;

    /**
     * Credits账户状态：正常
     */
    public static final int ACCOUNT_STATUS_NORMAL = 1;

    /**
     * Credits流水类型：支付入账
     */
    public static final int TRANSACTION_TYPE_PAYMENT_CREDIT = 1;

    /**
     * Credits流水类型：系统赠送
     */
    public static final int TRANSACTION_TYPE_SYSTEM_GRANT = 2;

    /**
     * Credits流水类型：Credits预占
     */
    public static final int TRANSACTION_TYPE_RESERVE = 3;

    /**
     * Credits流水类型：预占结算
     */
    public static final int TRANSACTION_TYPE_RESERVATION_SETTLE = 4;

    /**
     * Credits流水类型：预占释放
     */
    public static final int TRANSACTION_TYPE_RESERVATION_RELEASE = 5;

    /**
     * Credits流水类型：人工调整
     */
    public static final int TRANSACTION_TYPE_MANUAL_ADJUSTMENT = 6;

    /**
     * Credits预占状态：预占中
     */
    public static final int RESERVATION_STATUS_PROCESSING = 0;

    /**
     * Credits预占状态：已结算
     */
    public static final int RESERVATION_STATUS_SETTLED = 1;

    /**
     * Credits预占状态：已释放
     */
    public static final int RESERVATION_STATUS_RELEASED = 2;

    /**
     * Credits预占状态：已超时释放
     */
    public static final int RESERVATION_STATUS_TIMEOUT_RELEASED = 3;
}
