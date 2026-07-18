package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.model.AiProcessingContext;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: AI模型用量服务接口
 */
public interface AiUsageService {

    /**
     * 创建AI模型调用尝试记录
     *
     * @return AI用量记录ID
     */
    String startAttempt(AiProcessingContext context,
                        String requestNo,
                        int attemptNo,
                        int scene,
                        Integer unitIndex,
                        String provider,
                        String modelName);

    /**
     * 标记AI模型调用成功
     */
    void completeSuccess(String usageId,
                         String providerRequestId,
                         long inputTokens,
                         long outputTokens);

    /**
     * 标记AI模型调用失败
     */
    void completeFailure(String usageId, String errorMessage);

    /**
     * 标记AI模型调用结果未知
     */
    void completeUnknown(String usageId, String errorMessage);
}
