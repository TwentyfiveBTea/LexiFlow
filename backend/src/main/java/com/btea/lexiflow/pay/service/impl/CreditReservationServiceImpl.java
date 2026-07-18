package com.btea.lexiflow.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.config.CreditBillingProperties;
import com.btea.lexiflow.pay.constant.AiUsageConstant;
import com.btea.lexiflow.pay.constant.CreditConstant;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditLedgerDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditReservationDO;
import com.btea.lexiflow.pay.dao.mapper.BizAiUsageMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditAccountMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditLedgerMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditReservationMapper;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import com.btea.lexiflow.pay.service.CreditAccountService;
import com.btea.lexiflow.pay.service.CreditReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits预占服务实现类
 */
@Service
@RequiredArgsConstructor
public class CreditReservationServiceImpl implements CreditReservationService {

    private static final String BUSINESS_TYPE_ARTICLE_PROCESSING = "ARTICLE_PROCESSING";

    private final BizCreditReservationMapper reservationMapper;
    private final BizCreditAccountMapper accountMapper;
    private final BizCreditLedgerMapper ledgerMapper;
    private final BizAiUsageMapper aiUsageMapper;
    private final CreditAccountService creditAccountService;
    private final CreditBillingProperties billingProperties;

    /**
     * 创建文章处理初始Credits预占
     *
     * @param context AI处理上下文
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void createInitialReservation(AiProcessingContext context) {
        BizCreditReservationDO existing = reservationMapper.selectOne(
                new LambdaQueryWrapper<BizCreditReservationDO>()
                        .eq(BizCreditReservationDO::getProcessingNo, context.processingNo()));
        if (existing != null) {
            return;
        }
        long initialCredits = getPositive(billingProperties.getInitialReservationCredits(), 1L);
        creditAccountService.initializeAccount(context.userId());
        BizCreditAccountDO account = lockAccount(context.userId(), true);
        long availableCredits = valueOrZero(account.getAvailableCredits());
        if (availableCredits < initialCredits) {
            throw new ClientException(BaseErrorCode.CREDIT_BALANCE_INSUFFICIENT);
        }
        long availableAfter = availableCredits - initialCredits;
        long frozenAfter = Math.addExact(valueOrZero(account.getFrozenCredits()), initialCredits);
        account.setAvailableCredits(availableAfter);
        account.setFrozenCredits(frozenAfter);
        accountMapper.updateById(account);

        BizCreditReservationDO reservation = BizCreditReservationDO.builder()
                .id(IdUtil.getSnowflakeNextIdStr())
                .processingNo(context.processingNo())
                .userId(context.userId())
                .articleId(context.articleId())
                .reservedCredits(initialCredits)
                .consumedCredits(0L)
                .releasedCredits(0L)
                .status(CreditConstant.RESERVATION_STATUS_PROCESSING)
                .expiresAt(nextExpiration())
                .build();
        reservationMapper.insert(reservation);
        insertLedger(context.userId(), CreditConstant.TRANSACTION_TYPE_RESERVE,
                -initialCredits, initialCredits, availableAfter, frozenAfter, context.processingNo(),
                "RESERVE:" + context.processingNo() + ":INITIAL", "文章处理Credits初始预占");
    }

    /**
     * 为逻辑模型请求追加Credits预占
     *
     * @param context AI处理上下文
     * @param stageKey 预占阶段幂等键
     * @param credits 追加的Credits数量
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void reserveAdditional(AiProcessingContext context, String stageKey, long credits) {
        if (credits <= 0) {
            return;
        }
        BizCreditReservationDO reservation = reservationMapper.selectByProcessingNoForUpdate(context.processingNo());
        validateReservation(reservation, context);
        String idempotencyKey = "RESERVE:" + context.processingNo() + ":" + stageKey;
        if (ledgerExists(idempotencyKey)) {
            return;
        }
        reserveLocked(reservation, stageKey, credits);
    }

    /**
     * 确保预占额度能够覆盖当前实际用量
     *
     * @param context AI处理上下文
     * @param usageKey 用量幂等键
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void ensureActualUsageCovered(AiProcessingContext context, String usageKey) {
        BizCreditReservationDO reservation = reservationMapper.selectByProcessingNoForUpdate(context.processingNo());
        validateReservation(reservation, context);
        long actualCredits = getPendingCredits(context.processingNo());
        long reservedCredits = valueOrZero(reservation.getReservedCredits());
        long missingCredits = actualCredits - reservedCredits;
        if (missingCredits <= 0) {
            return;
        }
        String idempotencyKey = "RESERVE:" + context.processingNo() + ":ACTUAL:" + usageKey;
        if (ledgerExists(idempotencyKey)) {
            return;
        }
        reserveLocked(reservation, "ACTUAL:" + usageKey, missingCredits);
    }

    /**
     * 结算文章处理Credits并退回剩余预占额度
     *
     * @param processingNo 文章处理编号
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void settle(String processingNo) {
        BizCreditReservationDO reservation = reservationMapper.selectByProcessingNoForUpdate(processingNo);
        if (reservation == null) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_NOT_FOUND);
        }
        if (Integer.valueOf(CreditConstant.RESERVATION_STATUS_SETTLED).equals(reservation.getStatus())) {
            return;
        }
        ensureProcessing(reservation);
        long consumedCredits = getPendingCredits(processingNo);
        long reservedCredits = valueOrZero(reservation.getReservedCredits());
        if (consumedCredits > reservedCredits) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_CONFLICT);
        }
        BizCreditAccountDO account = lockAccount(reservation.getUserId(), false);
        long releasedCredits = reservedCredits - consumedCredits;
        long availableAfter = valueOrZero(account.getAvailableCredits()) + releasedCredits;
        long frozenAfter = valueOrZero(account.getFrozenCredits()) - reservedCredits;
        if (frozenAfter < 0) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_CONFLICT);
        }
        account.setAvailableCredits(availableAfter);
        account.setFrozenCredits(frozenAfter);
        accountMapper.updateById(account);

        reservation.setConsumedCredits(consumedCredits);
        reservation.setReleasedCredits(releasedCredits);
        reservation.setStatus(CreditConstant.RESERVATION_STATUS_SETTLED);
        reservation.setCompletedAt(new Date());
        reservationMapper.updateById(reservation);

        insertLedger(reservation.getUserId(), CreditConstant.TRANSACTION_TYPE_RESERVATION_SETTLE,
                releasedCredits, -reservedCredits, availableAfter, frozenAfter,
                processingNo, "SETTLE:" + processingNo, "文章处理Credits结算");
        aiUsageMapper.updateBillingStatus(processingNo,
                AiUsageConstant.BILLING_STATUS_PENDING,
                AiUsageConstant.BILLING_STATUS_SETTLED);
    }

    /**
     * 释放文章处理预占的Credits
     *
     * @param processingNo 文章处理编号
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void release(String processingNo) {
        releaseInternal(processingNo, CreditConstant.RESERVATION_STATUS_RELEASED, "RELEASE:" + processingNo,
                "文章处理失败，释放预占Credits");
    }

    /**
     * 超时释放文章处理预占的Credits
     *
     * @param processingNo 文章处理编号
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void timeoutRelease(String processingNo) {
        releaseInternal(processingNo, CreditConstant.RESERVATION_STATUS_TIMEOUT_RELEASED,
                "TIMEOUT_RELEASE:" + processingNo, "文章处理超时，释放预占Credits");
    }

    /**
     * 获取已经超时的预占处理编号
     *
     * @param limit 返回数量
     * @return 文章处理编号列表
     */
    @Override
    public List<String> listExpiredProcessingNos(int limit) {
        int queryLimit = Math.min(Math.max(limit, 1), 100);
        return reservationMapper.selectList(new LambdaQueryWrapper<BizCreditReservationDO>()
                        .eq(BizCreditReservationDO::getStatus, CreditConstant.RESERVATION_STATUS_PROCESSING)
                        .le(BizCreditReservationDO::getExpiresAt, new Date())
                        .orderByAsc(BizCreditReservationDO::getExpiresAt)
                        .last("LIMIT " + queryLimit))
                .stream()
                .map(BizCreditReservationDO::getProcessingNo)
                .toList();
    }

    private void reserveLocked(BizCreditReservationDO reservation, String stageKey, long credits) {
        creditAccountService.initializeAccount(reservation.getUserId());
        BizCreditAccountDO account = lockAccount(reservation.getUserId(), true);
        long availableCredits = valueOrZero(account.getAvailableCredits());
        if (availableCredits < credits) {
            throw new ClientException(BaseErrorCode.CREDIT_BALANCE_INSUFFICIENT);
        }
        long frozenCredits = valueOrZero(account.getFrozenCredits());
        long availableAfter = availableCredits - credits;
        long frozenAfter = Math.addExact(frozenCredits, credits);
        account.setAvailableCredits(availableAfter);
        account.setFrozenCredits(frozenAfter);
        accountMapper.updateById(account);

        reservation.setReservedCredits(Math.addExact(valueOrZero(reservation.getReservedCredits()), credits));
        reservation.setExpiresAt(nextExpiration());
        reservationMapper.updateById(reservation);

        insertLedger(reservation.getUserId(), CreditConstant.TRANSACTION_TYPE_RESERVE,
                -credits, credits, availableAfter, frozenAfter, reservation.getProcessingNo(),
                "RESERVE:" + reservation.getProcessingNo() + ":" + stageKey, "文章处理Credits预占");
    }

    private void releaseInternal(String processingNo, int targetStatus, String idempotencyKey, String remark) {
        BizCreditReservationDO reservation = reservationMapper.selectByProcessingNoForUpdate(processingNo);
        if (reservation == null) {
            return;
        }
        if (!Integer.valueOf(CreditConstant.RESERVATION_STATUS_PROCESSING).equals(reservation.getStatus())) {
            return;
        }
        BizCreditAccountDO account = lockAccount(reservation.getUserId(), false);
        long reservedCredits = valueOrZero(reservation.getReservedCredits());
        long frozenAfter = valueOrZero(account.getFrozenCredits()) - reservedCredits;
        if (frozenAfter < 0) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_CONFLICT);
        }
        long availableAfter = Math.addExact(valueOrZero(account.getAvailableCredits()), reservedCredits);
        account.setAvailableCredits(availableAfter);
        account.setFrozenCredits(frozenAfter);
        accountMapper.updateById(account);

        reservation.setConsumedCredits(0L);
        reservation.setReleasedCredits(reservedCredits);
        reservation.setStatus(targetStatus);
        reservation.setCompletedAt(new Date());
        reservationMapper.updateById(reservation);

        insertLedger(reservation.getUserId(), CreditConstant.TRANSACTION_TYPE_RESERVATION_RELEASE,
                reservedCredits, -reservedCredits, availableAfter, frozenAfter,
                processingNo, idempotencyKey, remark);
        aiUsageMapper.updateBillingStatus(processingNo,
                AiUsageConstant.BILLING_STATUS_PENDING,
                AiUsageConstant.BILLING_STATUS_FREE);
    }

    private BizCreditAccountDO lockAccount(String userId, boolean requireNormal) {
        BizCreditAccountDO account = accountMapper.selectByUserIdForUpdate(userId);
        if (account == null) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }
        if (requireNormal && !Integer.valueOf(CreditConstant.ACCOUNT_STATUS_NORMAL).equals(account.getStatus())) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_FROZEN);
        }
        return account;
    }

    private void validateReservation(BizCreditReservationDO reservation, AiProcessingContext context) {
        if (reservation == null) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_NOT_FOUND);
        }
        if (!reservation.getUserId().equals(context.userId()) || !reservation.getArticleId().equals(context.articleId())) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_CONFLICT);
        }
        ensureProcessing(reservation);
    }

    private void ensureProcessing(BizCreditReservationDO reservation) {
        if (!Integer.valueOf(CreditConstant.RESERVATION_STATUS_PROCESSING).equals(reservation.getStatus())) {
            throw new ClientException(BaseErrorCode.CREDIT_RESERVATION_CONFLICT);
        }
    }

    private boolean ledgerExists(String idempotencyKey) {
        return ledgerMapper.selectCount(new LambdaQueryWrapper<BizCreditLedgerDO>()
                .eq(BizCreditLedgerDO::getIdempotencyKey, idempotencyKey)) > 0;
    }

    private void insertLedger(String userId,
                              int transactionType,
                              long availableDelta,
                              long frozenDelta,
                              long availableAfter,
                              long frozenAfter,
                              String businessId,
                              String idempotencyKey,
                              String remark) {
        if (availableDelta == 0 && frozenDelta == 0) {
            return;
        }
        ledgerMapper.insert(BizCreditLedgerDO.builder()
                .userId(userId)
                .transactionType(transactionType)
                .availableDelta(availableDelta)
                .frozenDelta(frozenDelta)
                .availableBalanceAfter(availableAfter)
                .frozenBalanceAfter(frozenAfter)
                .businessType(BUSINESS_TYPE_ARTICLE_PROCESSING)
                .businessId(businessId)
                .idempotencyKey(idempotencyKey)
                .remark(remark)
                .build());
    }

    private long getPendingCredits(String processingNo) {
        Long value = aiUsageMapper.sumPendingCredits(processingNo,
                AiUsageConstant.REQUEST_STATUS_SUCCESS,
                AiUsageConstant.BILLING_STATUS_PENDING);
        return valueOrZero(value);
    }

    private Date nextExpiration() {
        int minutes = billingProperties.getReservationTtlMinutes() == null
                ? 180
                : Math.max(billingProperties.getReservationTtlMinutes(), 1);
        return new Date(System.currentTimeMillis() + minutes * 60_000L);
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private long getPositive(Long value, long fallback) {
        return value == null || value <= 0 ? fallback : value;
    }
}
