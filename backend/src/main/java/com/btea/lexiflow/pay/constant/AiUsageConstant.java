package com.btea.lexiflow.pay.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: AI模型用量常量类
 */
public final class AiUsageConstant {

    private AiUsageConstant() {
    }

    /**
     * AI调用场景：PDF OCR
     */
    public static final int SCENE_PDF_OCR = 1;

    /**
     * AI调用场景：Global Profile
     */
    public static final int SCENE_GLOBAL_PROFILE = 2;

    /**
     * AI调用场景：正文分块翻译
     */
    public static final int SCENE_CONTENT_CHUNK_TRANSLATION = 3;

    /**
     * AI请求状态：请求中
     */
    public static final int REQUEST_STATUS_PROCESSING = 0;

    /**
     * AI请求状态：请求成功
     */
    public static final int REQUEST_STATUS_SUCCESS = 1;

    /**
     * AI请求状态：请求失败
     */
    public static final int REQUEST_STATUS_FAILED = 2;

    /**
     * AI请求状态：结果未知
     */
    public static final int REQUEST_STATUS_UNKNOWN = 3;

    /**
     * AI计费状态：待结算
     */
    public static final int BILLING_STATUS_PENDING = 0;

    /**
     * AI计费状态：已计入结算
     */
    public static final int BILLING_STATUS_SETTLED = 1;

    /**
     * AI计费状态：免单
     */
    public static final int BILLING_STATUS_FREE = 2;
}
