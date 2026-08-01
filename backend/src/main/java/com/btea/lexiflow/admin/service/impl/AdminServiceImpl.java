/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员服务实现类
 */
package com.btea.lexiflow.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.admin.dao.mapper.AdminMapper;
import com.btea.lexiflow.admin.dto.req.AdminGrantCreditsReqDTO;
import com.btea.lexiflow.admin.dto.req.AdminLoginReqDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditUsageRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditsSummaryRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminLoginRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminUserRespDTO;
import com.btea.lexiflow.admin.service.AdminService;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.common.convention.result.PageRespDTO;
import com.btea.lexiflow.infrastructure.security.util.JwtUtil;
import com.btea.lexiflow.pay.constant.AiUsageConstant;
import com.btea.lexiflow.pay.constant.CreditConstant;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditLedgerDO;
import com.btea.lexiflow.pay.dao.mapper.BizCreditAccountMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditLedgerMapper;
import com.btea.lexiflow.user.constant.UserConstant;
import com.btea.lexiflow.user.dao.entity.BizUsersDO;
import com.btea.lexiflow.user.dao.mapper.BizUsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * 管理员服务实现类。
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final BizUsersMapper usersMapper;
    private final BizCreditAccountMapper creditAccountMapper;
    private final BizCreditLedgerMapper creditLedgerMapper;
    private final JwtUtil jwtUtil;

    /**
     * 管理员登录。
     *
     * @param reqDTO 管理员登录请求参数
     * @return 管理员登录信息
     */
    @Override
    public AdminLoginRespDTO login(AdminLoginReqDTO reqDTO) {
        BizUsersDO admin = usersMapper.selectOne(new LambdaQueryWrapper<BizUsersDO>()
                .eq(BizUsersDO::getUsername, reqDTO.getAccount().trim())
                .eq(BizUsersDO::getRole, UserConstant.ROLE_ADMIN));
        if (admin == null || !BCrypt.checkpw(reqDTO.getPassword(), admin.getPasswordHash())
                || !Integer.valueOf(UserConstant.STATUS_NORMAL).equals(admin.getStatus())) {
            throw new ClientException(BaseErrorCode.ADMIN_LOGIN_FAILED);
        }
        String token = jwtUtil.generateUserToken(admin.getId());
        return AdminLoginRespDTO.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .token(token)
                .build();
    }

    /**
     * 分页查询普通用户注册信息。
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return 用户注册分页数据
     */
    @Override
    public PageRespDTO<AdminUserRespDTO> listUsers(Integer page, Integer pageSize) {
        requireAdmin();
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        long total = adminMapper.countUsers(UserConstant.ROLE_USER);
        return PageRespDTO.of(adminMapper.selectUsers(UserConstant.ROLE_USER,
                offset(currentPage, currentPageSize), currentPageSize), total, currentPage, currentPageSize);
    }

    /**
     * 查询Credits使用汇总。
     *
     * @return 各时间范围的Credits使用量
     */
    @Override
    public AdminCreditsSummaryRespDTO getCreditsSummary() {
        requireAdmin();
        Instant now = Instant.now();
        return AdminCreditsSummaryRespDTO.builder()
                .lastDayCredits(sumCreditsSince(now.minus(Duration.ofDays(1))))
                .lastThreeDaysCredits(sumCreditsSince(now.minus(Duration.ofDays(3))))
                .lastSevenDaysCredits(sumCreditsSince(now.minus(Duration.ofDays(7))))
                .lastThirtyDaysCredits(sumCreditsSince(now.minus(Duration.ofDays(30))))
                .build();
    }

    /**
     * 分页查询全部用户Credits使用记录。
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return Credits使用记录分页数据
     */
    @Override
    public PageRespDTO<AdminCreditUsageRespDTO> listCreditUsage(Integer page, Integer pageSize) {
        requireAdmin();
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        long total = adminMapper.countCreditUsage(CreditConstant.RESERVATION_STATUS_SETTLED);
        List<AdminCreditUsageRespDTO> records = adminMapper.selectCreditUsage(
                CreditConstant.RESERVATION_STATUS_SETTLED,
                AiUsageConstant.REQUEST_STATUS_SUCCESS,
                AiUsageConstant.BILLING_STATUS_SETTLED,
                offset(currentPage, currentPageSize),
                currentPageSize);
        return PageRespDTO.of(records, total, currentPage, currentPageSize);
    }

    /**
     * 向指定用户赠送Credits。
     *
     * @param reqDTO 赠送Credits请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantCredits(AdminGrantCreditsReqDTO reqDTO) {
        BizUsersDO admin = requireAdmin();
        long credits = reqDTO.getCredits() == null ? 0L : reqDTO.getCredits();
        if (credits < 1 || credits > 10_000_000L) {
            throw new ClientException(BaseErrorCode.ADMIN_GRANT_AMOUNT_INVALID);
        }
        BizUsersDO targetUser = usersMapper.selectById(reqDTO.getUserId().trim());
        if (targetUser == null || !Integer.valueOf(UserConstant.STATUS_NORMAL).equals(targetUser.getStatus())) {
            throw new ClientException(BaseErrorCode.USER_NOT_FOUND);
        }
        creditAccountMapper.insertIgnore(IdUtil.getSnowflakeNextIdStr(), targetUser.getId(), CreditConstant.ACCOUNT_STATUS_NORMAL);
        BizCreditAccountDO account = creditAccountMapper.selectByUserIdForUpdate(targetUser.getId());
        if (account == null) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_NOT_FOUND);
        }
        if (Integer.valueOf(CreditConstant.ACCOUNT_STATUS_FROZEN).equals(account.getStatus())) {
            throw new ClientException(BaseErrorCode.CREDIT_ACCOUNT_FROZEN);
        }
        long availableBalance = Math.addExact(account.getAvailableCredits() == null ? 0L : account.getAvailableCredits(), credits);
        account.setAvailableCredits(availableBalance);
        creditAccountMapper.updateById(account);
        String grantId = IdUtil.getSnowflakeNextIdStr();
        creditLedgerMapper.insert(BizCreditLedgerDO.builder()
                .userId(targetUser.getId())
                .transactionType(CreditConstant.TRANSACTION_TYPE_MANUAL_ADJUSTMENT)
                .availableDelta(credits)
                .frozenDelta(0L)
                .availableBalanceAfter(availableBalance)
                .frozenBalanceAfter(account.getFrozenCredits() == null ? 0L : account.getFrozenCredits())
                .businessType("ADMIN_GRANT")
                .businessId(admin.getId())
                .idempotencyKey("ADMIN_GRANT:" + grantId)
                .remark("管理员 " + admin.getUsername() + " 赠送Credits")
                .build());
    }

    /**
     * 校验当前登录用户具备管理员角色。
     *
     * @return 当前管理员用户
     */
    private BizUsersDO requireAdmin() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        BizUsersDO user = usersMapper.selectById(userId);
        if (user == null || !UserConstant.ROLE_ADMIN.equals(user.getRole())
                || !Integer.valueOf(UserConstant.STATUS_NORMAL).equals(user.getStatus())) {
            throw new ClientException(BaseErrorCode.ADMIN_ACCESS_DENIED);
        }
        return user;
    }

    /**
     * 统计指定时间开始之后的Credits使用量。
     *
     * @param from 开始时间
     * @return Credits使用量
     */
    private long sumCreditsSince(Instant from) {
        return adminMapper.sumConsumedCreditsSince(CreditConstant.RESERVATION_STATUS_SETTLED, Date.from(from));
    }

    /**
     * 规范化页码。
     *
     * @param page 页码
     * @return 合法页码
     */
    private int normalizePage(Integer page) {
        return page == null ? 1 : Math.max(page, 1);
    }

    /**
     * 规范化每页数量。
     *
     * @param pageSize 每页数量
     * @return 合法每页数量
     */
    private int normalizePageSize(Integer pageSize) {
        return pageSize == null ? 10 : Math.min(Math.max(pageSize, 1), 100);
    }

    /**
     * 计算分页偏移量。
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页偏移量
     */
    private long offset(int page, int pageSize) {
        return (long) (page - 1) * pageSize;
    }
}
