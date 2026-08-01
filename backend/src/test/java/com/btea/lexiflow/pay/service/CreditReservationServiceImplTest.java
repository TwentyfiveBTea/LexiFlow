package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.config.CreditBillingProperties;
import com.btea.lexiflow.pay.constant.CreditConstant;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditLedgerDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditReservationDO;
import com.btea.lexiflow.pay.dao.mapper.BizAiUsageMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditAccountMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditLedgerMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditReservationMapper;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import com.btea.lexiflow.pay.service.impl.CreditReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/8/1
 * @Description: Credits真实用量扣费服务测试
 */
@ExtendWith(MockitoExtension.class)
class CreditReservationServiceImplTest {

    private static final String USER_ID = "user-1";
    private static final String ARTICLE_ID = "article-1";
    private static final String PROCESSING_NO = "processing-1";

    @Mock
    private BizCreditReservationMapper reservationMapper;

    @Mock
    private BizCreditAccountMapper accountMapper;

    @Mock
    private BizCreditLedgerMapper ledgerMapper;

    @Mock
    private BizAiUsageMapper aiUsageMapper;

    @Mock
    private CreditAccountService creditAccountService;

    private CreditReservationServiceImpl service;
    private AiProcessingContext context;

    @BeforeEach
    void setUp() {
        service = new CreditReservationServiceImpl(
                reservationMapper,
                accountMapper,
                ledgerMapper,
                aiUsageMapper,
                creditAccountService,
                new CreditBillingProperties());
        context = new AiProcessingContext(USER_ID, ARTICLE_ID, PROCESSING_NO);
    }

    @Test
    void shouldCreateProcessingRecordWithoutFreezingCredits() {
        when(reservationMapper.selectOne(any())).thenReturn(null);
        when(accountMapper.selectByUserIdForUpdate(USER_ID)).thenReturn(account(60_000L, 0L));

        service.createInitialReservation(context);

        ArgumentCaptor<BizCreditReservationDO> reservationCaptor =
                ArgumentCaptor.forClass(BizCreditReservationDO.class);
        verify(reservationMapper).insert(reservationCaptor.capture());
        assertEquals(0L, reservationCaptor.getValue().getReservedCredits());
        assertEquals(0L, reservationCaptor.getValue().getConsumedCredits());
        verify(accountMapper, never()).updateById(any(BizCreditAccountDO.class));
        verify(ledgerMapper, never()).insert(any(BizCreditLedgerDO.class));
    }

    @Test
    void shouldCheckPositiveBalanceWithoutFreezingCredits() {
        BizCreditReservationDO reservation = processingReservation(0L, 0L);
        when(reservationMapper.selectByProcessingNoForUpdate(PROCESSING_NO)).thenReturn(reservation);
        when(accountMapper.selectByUserIdForUpdate(USER_ID)).thenReturn(account(60_000L, 0L));

        service.ensureBalanceAvailable(context);

        assertNotNull(reservation.getExpiresAt());
        verify(reservationMapper).updateById(reservation);
        verify(accountMapper, never()).updateById(any(BizCreditAccountDO.class));
        verify(ledgerMapper, never()).insert(any(BizCreditLedgerDO.class));
    }

    @Test
    void shouldRejectAiCallWhenBalanceIsZero() {
        when(reservationMapper.selectByProcessingNoForUpdate(PROCESSING_NO))
                .thenReturn(processingReservation(0L, 0L));
        when(accountMapper.selectByUserIdForUpdate(USER_ID)).thenReturn(account(0L, 0L));

        assertThrows(ClientException.class, () -> service.ensureBalanceAvailable(context));

        verify(accountMapper, never()).updateById(any(BizCreditAccountDO.class));
        verify(reservationMapper, never()).updateById(any(BizCreditReservationDO.class));
    }

    @Test
    void shouldChargeOnlyActualUsageWithoutChangingFrozenCredits() {
        BizCreditReservationDO reservation = processingReservation(0L, 0L);
        BizCreditAccountDO account = account(60_000L, 37L);
        when(reservationMapper.selectByProcessingNoForUpdate(PROCESSING_NO)).thenReturn(reservation);
        when(ledgerMapper.selectCount(any())).thenReturn(0L);
        when(aiUsageMapper.sumPendingCredits(any(), any(), any())).thenReturn(412L);
        when(accountMapper.selectByUserIdForUpdate(USER_ID)).thenReturn(account);

        service.chargeActualUsage(context, "usage-1");

        assertEquals(59_588L, account.getAvailableCredits());
        assertEquals(37L, account.getFrozenCredits());
        assertEquals(412L, reservation.getReservedCredits());
        assertEquals(412L, reservation.getConsumedCredits());
        ArgumentCaptor<BizCreditLedgerDO> ledgerCaptor = ArgumentCaptor.forClass(BizCreditLedgerDO.class);
        verify(ledgerMapper).insert(ledgerCaptor.capture());
        assertEquals(-412L, ledgerCaptor.getValue().getAvailableDelta());
        assertEquals(0L, ledgerCaptor.getValue().getFrozenDelta());
    }

    @Test
    void shouldRefundActualChargesWhenProcessingFails() {
        BizCreditReservationDO reservation = processingReservation(412L, 412L);
        BizCreditAccountDO account = account(59_588L, 0L);
        when(reservationMapper.selectByProcessingNoForUpdate(PROCESSING_NO)).thenReturn(reservation);
        when(accountMapper.selectByUserIdForUpdate(USER_ID)).thenReturn(account);

        service.release(PROCESSING_NO);

        assertEquals(60_000L, account.getAvailableCredits());
        assertEquals(0L, account.getFrozenCredits());
        assertEquals(0L, reservation.getConsumedCredits());
        assertEquals(412L, reservation.getReleasedCredits());
        assertEquals(CreditConstant.RESERVATION_STATUS_RELEASED, reservation.getStatus());
    }

    private BizCreditAccountDO account(long availableCredits, long frozenCredits) {
        return BizCreditAccountDO.builder()
                .userId(USER_ID)
                .availableCredits(availableCredits)
                .frozenCredits(frozenCredits)
                .status(CreditConstant.ACCOUNT_STATUS_NORMAL)
                .build();
    }

    private BizCreditReservationDO processingReservation(long reservedCredits, long consumedCredits) {
        return BizCreditReservationDO.builder()
                .processingNo(PROCESSING_NO)
                .userId(USER_ID)
                .articleId(ARTICLE_ID)
                .reservedCredits(reservedCredits)
                .consumedCredits(consumedCredits)
                .releasedCredits(0L)
                .status(CreditConstant.RESERVATION_STATUS_PROCESSING)
                .build();
    }
}
