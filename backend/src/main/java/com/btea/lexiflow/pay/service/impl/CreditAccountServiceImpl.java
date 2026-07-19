package com.btea.lexiflow.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.common.convention.result.PageRespDTO;
import com.btea.lexiflow.pay.constant.AiUsageConstant;
import com.btea.lexiflow.pay.constant.CreditConstant;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import com.btea.lexiflow.pay.dao.mapper.BizCreditAccountMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditReservationMapper;
import com.btea.lexiflow.pay.dto.resp.CreditAccountRespDTO;
import com.btea.lexiflow.pay.dto.resp.CreditLedgerRespDTO;
import com.btea.lexiflow.pay.service.CreditAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits账户服务实现类
 */
@Service
@RequiredArgsConstructor
public class CreditAccountServiceImpl implements CreditAccountService {

    private final BizCreditAccountMapper creditAccountMapper;
    private final BizCreditReservationMapper creditReservationMapper;

    /**
     * 幂等初始化用户Credits账户
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeAccount(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ClientException(BaseErrorCode.USER_NOT_FOUND);
        }
        creditAccountMapper.insertIgnore(IdUtil.getSnowflakeNextIdStr(), userId, CreditConstant.ACCOUNT_STATUS_NORMAL);
    }

    /**
     * 获取当前用户Credits账户
     *
     * @return Credits账户信息
     */
    @Override
    public CreditAccountRespDTO getCurrentAccount() {
        String userId = getCurrentUserId();
        initializeAccount(userId);
        BizCreditAccountDO account = creditAccountMapper.selectOne(new LambdaQueryWrapper<BizCreditAccountDO>()
                .eq(BizCreditAccountDO::getUserId, userId));
        if (account == null) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }
        return CreditAccountRespDTO.builder()
                .availableCredits(account.getAvailableCredits())
                .frozenCredits(account.getFrozenCredits())
                .status(account.getStatus())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    /**
     * 获取当前用户的文章Credits使用记录
     *
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 文章Credits使用记录分页结果
     */
    @Override
    public PageRespDTO<CreditLedgerRespDTO> listCurrentLedger(Integer page, Integer pageSize) {
        String userId = getCurrentUserId();
        int currentPage = page == null ? 1 : Math.max(page, 1);
        int currentPageSize = pageSize == null ? 10 : Math.min(Math.max(pageSize, 1), 100);
        long offset = (long) (currentPage - 1) * currentPageSize;
        long total = creditReservationMapper.countSettledUsageByUser(
                userId,
                CreditConstant.RESERVATION_STATUS_SETTLED);
        List<CreditLedgerRespDTO> records = creditReservationMapper.selectSettledUsageByUser(
                userId,
                CreditConstant.RESERVATION_STATUS_SETTLED,
                AiUsageConstant.REQUEST_STATUS_SUCCESS,
                AiUsageConstant.BILLING_STATUS_SETTLED,
                offset,
                currentPageSize);
        return PageRespDTO.of(records, total, currentPage, currentPageSize);
    }

    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }
}
