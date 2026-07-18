package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.model.AiProcessingContext;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits预占服务接口
 */
public interface CreditReservationService {

    /**
     * 创建文章处理初始Credits预占
     *
     * @param context AI处理上下文
     */
    void createInitialReservation(AiProcessingContext context);

    /**
     * 为逻辑模型请求追加Credits预占
     *
     * @param context AI处理上下文
     * @param stageKey 预占阶段幂等键
     * @param credits 追加Credits
     */
    void reserveAdditional(AiProcessingContext context, String stageKey, long credits);

    /**
     * 确保预占额度能够覆盖当前实际用量
     *
     * @param context AI处理上下文
     * @param usageKey 用量幂等键
     */
    void ensureActualUsageCovered(AiProcessingContext context, String usageKey);

    /**
     * 结算文章处理Credits
     *
     * @param processingNo 文章处理编号
     */
    void settle(String processingNo);

    /**
     * 释放文章处理Credits
     *
     * @param processingNo 文章处理编号
     */
    void release(String processingNo);

    /**
     * 超时释放文章处理Credits
     *
     * @param processingNo 文章处理编号
     */
    void timeoutRelease(String processingNo);

    /**
     * 获取已经超时的预占处理编号
     *
     * @param limit 返回数量
     * @return 文章处理编号列表
     */
    List<String> listExpiredProcessingNos(int limit);
}
