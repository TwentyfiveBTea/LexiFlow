package com.btea.lexiflow.pay.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.article.constant.ArticleConstant;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.pay.config.CreditBillingProperties;
import com.btea.lexiflow.pay.dao.entity.BizCreditReservationDO;
import com.btea.lexiflow.pay.dao.mapper.BizCreditReservationMapper;
import com.btea.lexiflow.pay.service.CreditReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits预占超时补偿任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditReservationReconcileJob {

    private final CreditReservationService reservationService;
    private final BizCreditReservationMapper reservationMapper;
    private final BizArticlesMapper articlesMapper;
    private final CreditBillingProperties billingProperties;

    /**
     * 结算或释放已经超时的Credits预占
     */
    @Scheduled(fixedDelayString = "${lexiflow.pay.billing.reservation-reconcile-delay-millis:60000}")
    public void reconcileReservations() {
        for (String processingNo : reservationService.listExpiredProcessingNos(getBatchSize())) {
            try {
                BizCreditReservationDO reservation = reservationMapper.selectOne(
                        new LambdaQueryWrapper<BizCreditReservationDO>()
                                .eq(BizCreditReservationDO::getProcessingNo, processingNo));
                if (reservation == null) {
                    continue;
                }
                BizArticlesDO article = articlesMapper.selectById(reservation.getArticleId());
                if (article != null
                        && Integer.valueOf(ArticleConstant.PARSE_STATUS_SUCCESS).equals(article.getParseStatus())
                        && Integer.valueOf(ArticleConstant.TRANSLATION_STATUS_SUCCESS)
                        .equals(article.getTranslationStatus())) {
                    reservationService.settle(processingNo);
                } else {
                    reservationService.timeoutRelease(processingNo);
                }
            } catch (Exception e) {
                log.warn("Credits预占补偿失败: processingNo={}, errorType={}",
                        processingNo, e.getClass().getSimpleName());
            }
        }
    }

    private int getBatchSize() {
        Integer value = billingProperties.getReconcileBatchSize();
        return value == null ? 50 : Math.min(Math.max(value, 1), 100);
    }
}
