package com.btea.lexiflow.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.constant.CreditConstant;
import com.btea.lexiflow.pay.constant.PaymentConstant;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditLedgerDO;
import com.btea.lexiflow.pay.dao.entity.BizPaymentOrderDO;
import com.btea.lexiflow.pay.dao.mapper.BizCreditAccountMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditLedgerMapper;
import com.btea.lexiflow.pay.dao.mapper.BizPaymentOrderMapper;
import com.btea.lexiflow.pay.model.PaymentConfirmation;
import com.btea.lexiflow.pay.service.CreditAccountService;
import com.btea.lexiflow.pay.service.PaymentSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付入账事务服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaymentSettlementServiceImpl implements PaymentSettlementService {

    private static final String BUSINESS_TYPE_PAYMENT = "PAYMENT";

    private final BizPaymentOrderMapper paymentOrderMapper;
    private final BizCreditAccountMapper creditAccountMapper;
    private final BizCreditLedgerMapper creditLedgerMapper;
    private final CreditAccountService creditAccountService;

    /**
     * 确认支付结果并幂等入账Credits
     *
     * @param confirmation 支付确认参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAndCredit(PaymentConfirmation confirmation) {
        BizPaymentOrderDO order = paymentOrderMapper.selectByOrderNoForUpdate(confirmation.orderNo());
        if (order == null) {
            throw new ClientException(BaseErrorCode.PAYMENT_ORDER_NOT_FOUND);
        }
        validateConfirmation(order, confirmation);
        if (Integer.valueOf(PaymentConstant.ORDER_STATUS_PAID).equals(order.getOrderStatus())
                && Integer.valueOf(PaymentConstant.CREDIT_STATUS_CREDITED).equals(order.getCreditStatus())) {
            return;
        }
        if (Integer.valueOf(PaymentConstant.ORDER_STATUS_ABNORMAL).equals(order.getOrderStatus())) {
            throw new ClientException(BaseErrorCode.PAYMENT_ORDER_CONFLICT);
        }

        creditAccountService.initializeAccount(order.getUserId());
        BizCreditAccountDO account = creditAccountMapper.selectByUserIdForUpdate(order.getUserId());
        if (account == null) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }
        if (!Integer.valueOf(CreditConstant.ACCOUNT_STATUS_NORMAL).equals(account.getStatus())) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_FROZEN);
        }

        String idempotencyKey = "PAYMENT:" + order.getOrderNo();
        boolean ledgerExists = creditLedgerMapper.selectCount(new LambdaQueryWrapper<BizCreditLedgerDO>()
                .eq(BizCreditLedgerDO::getIdempotencyKey, idempotencyKey)) > 0;
        Date paidAt = confirmation.paidAt() == null ? new Date() : confirmation.paidAt();
        if (!ledgerExists) {
            long availableAfter = Math.addExact(valueOrZero(account.getAvailableCredits()), order.getTotalCredits());
            long frozenAfter = valueOrZero(account.getFrozenCredits());
            account.setAvailableCredits(availableAfter);
            creditAccountMapper.updateById(account);
            creditLedgerMapper.insert(BizCreditLedgerDO.builder()
                    .userId(order.getUserId())
                    .transactionType(CreditConstant.TRANSACTION_TYPE_PAYMENT_CREDIT)
                    .availableDelta(order.getTotalCredits())
                    .frozenDelta(0L)
                    .availableBalanceAfter(availableAfter)
                    .frozenBalanceAfter(frozenAfter)
                    .businessType(BUSINESS_TYPE_PAYMENT)
                    .businessId(order.getOrderNo())
                    .idempotencyKey(idempotencyKey)
                    .remark("Credits充值入账")
                    .build());
        }

        order.setOrderStatus(PaymentConstant.ORDER_STATUS_PAID);
        order.setCreditStatus(PaymentConstant.CREDIT_STATUS_CREDITED);
        order.setProviderTradeNo(confirmation.providerTradeNo());
        order.setUpstreamTradeNo(confirmation.upstreamTradeNo());
        order.setPaidAt(order.getPaidAt() == null ? paidAt : order.getPaidAt());
        order.setCreditedAt(order.getCreditedAt() == null ? new Date() : order.getCreditedAt());
        paymentOrderMapper.updateById(order);
    }

    private void validateConfirmation(BizPaymentOrderDO order, PaymentConfirmation confirmation) {
        if (!order.getAmountMinor().equals(confirmation.amountMinor())) {
            order.setOrderStatus(PaymentConstant.ORDER_STATUS_ABNORMAL);
            paymentOrderMapper.updateById(order);
            throw new ClientException(BaseErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
        if (order.getProviderTradeNo() != null
                && !order.getProviderTradeNo().equals(confirmation.providerTradeNo())) {
            order.setOrderStatus(PaymentConstant.ORDER_STATUS_ABNORMAL);
            paymentOrderMapper.updateById(order);
            throw new ClientException(BaseErrorCode.PAYMENT_ORDER_CONFLICT);
        }
        if (confirmation.providerTradeNo() == null || confirmation.providerTradeNo().isBlank()) {
            throw new ClientException(BaseErrorCode.PAYMENT_PROVIDER_RESPONSE_INVALID);
        }
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}
