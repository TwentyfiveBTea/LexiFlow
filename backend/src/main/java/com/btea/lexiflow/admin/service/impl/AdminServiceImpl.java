package com.btea.lexiflow.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btea.lexiflow.admin.dto.req.AdminGrantCreditsReqDTO;
import com.btea.lexiflow.admin.dto.req.AdminLoginReqDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditUsageRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditsSummaryRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminLoginRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminUserRespDTO;
import com.btea.lexiflow.admin.service.AdminService;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.common.convention.result.PageRespDTO;
import com.btea.lexiflow.infrastructure.security.util.JwtUtil;
import com.btea.lexiflow.pay.constant.AiUsageConstant;
import com.btea.lexiflow.pay.constant.CreditConstant;
import com.btea.lexiflow.pay.dao.entity.BizAiUsageDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditLedgerDO;
import com.btea.lexiflow.pay.dao.entity.BizCreditReservationDO;
import com.btea.lexiflow.pay.dao.mapper.BizAiUsageMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditAccountMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditLedgerMapper;
import com.btea.lexiflow.pay.dao.mapper.BizCreditReservationMapper;
import com.btea.lexiflow.user.constant.UserConstant;
import com.btea.lexiflow.user.dao.entity.BizUsersDO;
import com.btea.lexiflow.user.dao.mapper.BizUsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BizUsersMapper usersMapper;
    private final BizArticlesMapper articlesMapper;
    private final BizCreditReservationMapper creditReservationMapper;
    private final BizAiUsageMapper aiUsageMapper;
    private final BizCreditAccountMapper creditAccountMapper;
    private final BizCreditLedgerMapper creditLedgerMapper;
    private final JwtUtil jwtUtil;

    /**
     * 管理员登录
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
     * 分页查询普通用户注册信息
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
        LambdaQueryWrapper<BizUsersDO> queryWrapper = new LambdaQueryWrapper<BizUsersDO>()
                .eq(BizUsersDO::getRole, UserConstant.ROLE_USER);
        long total = usersMapper.selectCount(queryWrapper);
        List<AdminUserRespDTO> records = usersMapper.selectList(queryWrapper
                        .orderByDesc(BizUsersDO::getCreatedAt)
                        .last(limitClause(currentPage, currentPageSize)))
                .stream()
                .map(user -> AdminUserRespDTO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .registeredAt(user.getCreatedAt())
                        .build())
                .toList();
        return PageRespDTO.of(records, total, currentPage, currentPageSize);
    }

    /**
     * 查询Credits使用汇总
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
     * 分页查询全部用户Credits使用记录
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
        LambdaQueryWrapper<BizCreditReservationDO> queryWrapper = new LambdaQueryWrapper<BizCreditReservationDO>()
                .eq(BizCreditReservationDO::getStatus, CreditConstant.RESERVATION_STATUS_SETTLED);
        long total = creditReservationMapper.selectCount(queryWrapper);
        List<BizCreditReservationDO> reservations = creditReservationMapper.selectList(queryWrapper
                .orderByDesc(BizCreditReservationDO::getCompletedAt)
                .last(limitClause(currentPage, currentPageSize)));
        List<AdminCreditUsageRespDTO> records = buildCreditUsageRecords(reservations);
        return PageRespDTO.of(records, total, currentPage, currentPageSize);
    }

    /**
     * 向指定用户赠送Credits
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
     * 校验当前登录用户具备管理员角色
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
     * 统计指定时间开始之后的Credits使用量
     *
     * @param from 开始时间
     * @return Credits使用量
     */
    private long sumCreditsSince(Instant from) {
        QueryWrapper<BizCreditReservationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("COALESCE(SUM(consumed_credits), 0) AS consumed_credits")
                .eq("status", CreditConstant.RESERVATION_STATUS_SETTLED)
                .ge("completed_at", Date.from(from));
        BizCreditReservationDO aggregate = creditReservationMapper.selectOne(queryWrapper);
        return aggregate == null || aggregate.getConsumedCredits() == null ? 0L : aggregate.getConsumedCredits();
    }

    /**
     * 组装Credits使用记录
     *
     * @param reservations Credits预占记录
     * @return Credits使用记录
     */
    private List<AdminCreditUsageRespDTO> buildCreditUsageRecords(List<BizCreditReservationDO> reservations) {
        if (reservations.isEmpty()) {
            return List.of();
        }
        Map<String, BizUsersDO> users = usersMapper.selectBatchIds(reservations.stream()
                        .map(BizCreditReservationDO::getUserId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(BizUsersDO::getId, Function.identity()));
        Map<String, BizArticlesDO> articles = articlesMapper.selectBatchIds(reservations.stream()
                        .map(BizCreditReservationDO::getArticleId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(BizArticlesDO::getId, Function.identity()));
        Map<String, Long> ocrCredits = new HashMap<>();
        Map<String, Long> translationCredits = new HashMap<>();
        List<String> processingNumbers = reservations.stream()
                .map(BizCreditReservationDO::getProcessingNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!processingNumbers.isEmpty()) {
            aiUsageMapper.selectList(new LambdaQueryWrapper<BizAiUsageDO>()
                            .in(BizAiUsageDO::getProcessingNo, processingNumbers)
                            .eq(BizAiUsageDO::getRequestStatus, AiUsageConstant.REQUEST_STATUS_SUCCESS)
                            .eq(BizAiUsageDO::getBillingStatus, AiUsageConstant.BILLING_STATUS_SETTLED))
                    .forEach(usage -> mergeCredits(usage, ocrCredits, translationCredits));
        }
        return reservations.stream()
                .map(reservation -> toCreditUsageRecord(
                        reservation, users, articles, ocrCredits, translationCredits))
                .toList();
    }

    /**
     * 汇总AI调用的Credits使用量
     *
     * @param usage AI用量记录
     * @param ocrCredits OCR Credits汇总
     * @param translationCredits 翻译Credits汇总
     */
    private void mergeCredits(BizAiUsageDO usage,
                              Map<String, Long> ocrCredits,
                              Map<String, Long> translationCredits) {
        long credits = usage.getCreditsCost() == null ? 0L : usage.getCreditsCost();
        if (Integer.valueOf(AiUsageConstant.SCENE_PDF_OCR).equals(usage.getScene())) {
            ocrCredits.merge(usage.getProcessingNo(), credits, Long::sum);
        } else if (Integer.valueOf(AiUsageConstant.SCENE_GLOBAL_PROFILE).equals(usage.getScene())
                || Integer.valueOf(AiUsageConstant.SCENE_CONTENT_CHUNK_TRANSLATION).equals(usage.getScene())) {
            translationCredits.merge(usage.getProcessingNo(), credits, Long::sum);
        }
    }

    /**
     * 转换Credits使用记录
     *
     * @param reservation Credits预占记录
     * @param users 用户映射
     * @param articles 文章映射
     * @param ocrCredits OCR Credits汇总
     * @param translationCredits 翻译Credits汇总
     * @return Credits使用记录
     */
    private AdminCreditUsageRespDTO toCreditUsageRecord(BizCreditReservationDO reservation,
                                                         Map<String, BizUsersDO> users,
                                                         Map<String, BizArticlesDO> articles,
                                                         Map<String, Long> ocrCredits,
                                                         Map<String, Long> translationCredits) {
        BizUsersDO user = users.get(reservation.getUserId());
        BizArticlesDO article = articles.get(reservation.getArticleId());
        String articleTitle = reservation.getArticleId();
        if (article != null && article.getTitle() != null
                && Objects.equals(article.getUserId(), reservation.getUserId())) {
            articleTitle = article.getTitle();
        }
        return AdminCreditUsageRespDTO.builder()
                .userId(reservation.getUserId())
                .username(user == null ? null : user.getUsername())
                .articleTitle(articleTitle)
                .totalCredits(reservation.getConsumedCredits())
                .ocrCredits(ocrCredits.getOrDefault(reservation.getProcessingNo(), 0L))
                .translationCredits(translationCredits.getOrDefault(reservation.getProcessingNo(), 0L))
                .completedAt(reservation.getCompletedAt())
                .build();
    }

    /**
     * 规范化页码
     *
     * @param page 页码
     * @return 合法页码
     */
    private int normalizePage(Integer page) {
        return page == null ? 1 : Math.max(page, 1);
    }

    /**
     * 规范化每页数量
     *
     * @param pageSize 每页数量
     * @return 合法每页数量
     */
    private int normalizePageSize(Integer pageSize) {
        return pageSize == null ? 10 : Math.min(Math.max(pageSize, 1), 100);
    }

    /**
     * 生成分页限制语句
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return 分页限制语句
     */
    private String limitClause(int page, int pageSize) {
        return "LIMIT " + (long) (page - 1) * pageSize + ", " + pageSize;
    }
}
