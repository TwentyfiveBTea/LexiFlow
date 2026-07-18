package com.btea.lexiflow.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.constant.AiUsageConstant;
import com.btea.lexiflow.pay.dao.entity.BizAiUsageDO;
import com.btea.lexiflow.pay.dao.mapper.BizAiUsageMapper;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import com.btea.lexiflow.pay.service.AiUsageService;
import com.btea.lexiflow.pay.service.CreditBillingCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: AI模型用量服务实现类
 */
@Service
@RequiredArgsConstructor
public class AiUsageServiceImpl implements AiUsageService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    private final BizAiUsageMapper aiUsageMapper;
    private final CreditBillingCalculator billingCalculator;

    /**
     * 创建AI模型调用尝试记录
     *
     * @param context AI处理上下文
     * @param requestNo 逻辑模型请求编号
     * @param attemptNo 当前调用尝试序号
     * @param scene AI调用场景
     * @param unitIndex OCR页码或翻译分块序号
     * @param provider 模型供应商
     * @param modelName 模型名称
     * @return AI用量记录ID
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String startAttempt(AiProcessingContext context,
                               String requestNo,
                               int attemptNo,
                               int scene,
                               Integer unitIndex,
                               String provider,
                               String modelName) {
        String usageId = IdUtil.getSnowflakeNextIdStr();
        BizAiUsageDO usage = BizAiUsageDO.builder()
                .id(usageId)
                .userId(context.userId())
                .articleId(context.articleId())
                .processingNo(context.processingNo())
                .requestNo(requestNo)
                .attemptNo(attemptNo)
                .scene(scene)
                .unitIndex(unitIndex)
                .provider(provider)
                .modelName(modelName)
                .inputTokens(0L)
                .outputTokens(0L)
                .inputRatePerMillion(billingCalculator.getInputRatePerMillion())
                .outputRatePerMillion(billingCalculator.getOutputRatePerMillion())
                .creditsCost(0L)
                .requestStatus(AiUsageConstant.REQUEST_STATUS_PROCESSING)
                .billingStatus(AiUsageConstant.BILLING_STATUS_PENDING)
                .build();
        aiUsageMapper.insert(usage);
        return usageId;
    }

    /**
     * 标记AI模型调用成功并记录Token用量及Credits成本
     *
     * @param usageId AI用量记录ID
     * @param providerRequestId 模型供应商请求ID
     * @param inputTokens 输入Token数量
     * @param outputTokens 输出Token数量
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void completeSuccess(String usageId,
                                String providerRequestId,
                                long inputTokens,
                                long outputTokens) {
        BizAiUsageDO usage = getUsage(usageId);
        usage.setProviderRequestId(providerRequestId);
        usage.setInputTokens(inputTokens);
        usage.setOutputTokens(outputTokens);
        usage.setCreditsCost(billingCalculator.calculate(inputTokens, outputTokens));
        usage.setRequestStatus(AiUsageConstant.REQUEST_STATUS_SUCCESS);
        usage.setCompletedAt(new Date());
        aiUsageMapper.updateById(usage);
    }

    /**
     * 标记AI模型调用失败并将本次调用设为免单
     *
     * @param usageId AI用量记录ID
     * @param errorMessage 失败原因摘要
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void completeFailure(String usageId, String errorMessage) {
        completeWithStatus(usageId, AiUsageConstant.REQUEST_STATUS_FAILED, errorMessage);
    }

    /**
     * 标记AI模型调用结果未知并将本次调用设为免单
     *
     * @param usageId AI用量记录ID
     * @param errorMessage 结果未知原因摘要
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void completeUnknown(String usageId, String errorMessage) {
        completeWithStatus(usageId, AiUsageConstant.REQUEST_STATUS_UNKNOWN, errorMessage);
    }

    private void completeWithStatus(String usageId, int status, String errorMessage) {
        BizAiUsageDO usage = getUsage(usageId);
        usage.setRequestStatus(status);
        usage.setBillingStatus(AiUsageConstant.BILLING_STATUS_FREE);
        usage.setErrorMessage(sanitizeError(errorMessage));
        usage.setCompletedAt(new Date());
        aiUsageMapper.updateById(usage);
    }

    private BizAiUsageDO getUsage(String usageId) {
        BizAiUsageDO usage = aiUsageMapper.selectById(usageId);
        if (usage == null) {
            throw new ClientException(BaseErrorCode.AI_USAGE_NOT_FOUND);
        }
        return usage;
    }

    private String sanitizeError(String errorMessage) {
        String value = errorMessage == null || errorMessage.isBlank() ? "unknown" : errorMessage.trim();
        return value.length() <= MAX_ERROR_MESSAGE_LENGTH ? value : value.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
