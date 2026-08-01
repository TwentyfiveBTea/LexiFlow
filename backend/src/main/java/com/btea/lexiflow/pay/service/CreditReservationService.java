package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.model.AiProcessingContext;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 文章处理Credits计费服务接口
 */
public interface CreditReservationService {

    /**
     * 创建文章处理Credits计费记录
     *
     * @param context AI处理上下文
     */
    void createInitialReservation(AiProcessingContext context);

    /**
     * 检查当前用户是否还有可用Credits
     *
     * @param context AI处理上下文
     */
    void ensureBalanceAvailable(AiProcessingContext context);

    /**
     * 按当前实际用量扣除Credits
     *
     * @param context AI处理上下文
     * @param usageKey 用量幂等键
     */
    void chargeActualUsage(AiProcessingContext context, String usageKey);

    /**
     * 结算文章处理Credits
     *
     * @param processingNo 文章处理编号
     */
    void settle(String processingNo);

    /**
     * 退回处理失败任务已扣除的Credits
     *
     * @param processingNo 文章处理编号
     */
    void release(String processingNo);

    /**
     * 退回超时任务已扣除的Credits
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
