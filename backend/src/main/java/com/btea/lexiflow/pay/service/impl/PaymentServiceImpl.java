package com.btea.lexiflow.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.common.convention.result.PageRespDTO;
import com.btea.lexiflow.pay.config.CreditBillingProperties;
import com.btea.lexiflow.pay.config.EpayProperties;
import com.btea.lexiflow.pay.constant.PaymentConstant;
import com.btea.lexiflow.pay.dao.entity.BizPaymentOrderDO;
import com.btea.lexiflow.pay.dao.mapper.BizPaymentOrderMapper;
import com.btea.lexiflow.pay.dto.req.PaymentOrderCreateReqDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderCreateRespDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderRespDTO;
import com.btea.lexiflow.pay.dto.resp.RechargeRecordRespDTO;
import com.btea.lexiflow.pay.integration.epay.EpayClient;
import com.btea.lexiflow.pay.integration.epay.EpayOrderQueryResponse;
import com.btea.lexiflow.pay.integration.epay.EpaySigner;
import com.btea.lexiflow.pay.model.PaymentConfirmation;
import com.btea.lexiflow.pay.service.PaymentService;
import com.btea.lexiflow.pay.service.PaymentSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String FORM_METHOD_POST = "POST";
    private static final String SIGN_TYPE_MD5 = "MD5";
    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private final BizPaymentOrderMapper paymentOrderMapper;
    private final PaymentSettlementService settlementService;
    private final EpaySigner epaySigner;
    private final EpayClient epayClient;
    private final EpayProperties epayProperties;
    private final CreditBillingProperties billingProperties;

    /**
     * 创建支付订单
     *
     * @param reqDTO 支付订单创建请求
     * @return 支付订单创建结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderCreateRespDTO createOrder(PaymentOrderCreateReqDTO reqDTO) {
        validateCreateConfig();
        String userId = getCurrentUserId();
        String clientRequestNo = reqDTO.getClientRequestNo().trim();
        String deviceType = normalizeDevice(reqDTO.getDeviceType());
        long amountMinor = Math.multiplyExact(reqDTO.getAmountYuan().longValue(), 100L);
        long totalCredits = Math.multiplyExact(reqDTO.getAmountYuan().longValue(), getCreditsPerYuan());

        BizPaymentOrderDO existing = paymentOrderMapper.selectOne(new LambdaQueryWrapper<BizPaymentOrderDO>()
                .eq(BizPaymentOrderDO::getUserId, userId)
                .eq(BizPaymentOrderDO::getClientRequestNo, clientRequestNo));
        if (existing != null) {
            if (!existing.getAmountMinor().equals(amountMinor) || !existing.getDeviceType().equals(deviceType)) {
                throw new ClientException(BaseErrorCode.PAYMENT_ORDER_CONFLICT);
            }
            return toCreateResp(existing);
        }

        Date now = new Date();
        BizPaymentOrderDO order = BizPaymentOrderDO.builder()
                .orderNo(IdUtil.getSnowflakeNextIdStr())
                .userId(userId)
                .subject(epayProperties.getSubject().trim())
                .amountMinor(amountMinor)
                .totalCredits(totalCredits)
                .deviceType(deviceType)
                .clientRequestNo(clientRequestNo)
                .orderStatus(PaymentConstant.ORDER_STATUS_PENDING)
                .creditStatus(PaymentConstant.CREDIT_STATUS_PENDING)
                .expiresAt(new Date(now.getTime() + getOrderExpireMinutes() * 60_000L))
                .build();
        try {
            paymentOrderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            BizPaymentOrderDO concurrentOrder = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<BizPaymentOrderDO>()
                            .eq(BizPaymentOrderDO::getUserId, userId)
                            .eq(BizPaymentOrderDO::getClientRequestNo, clientRequestNo));
            if (concurrentOrder == null || !concurrentOrder.getAmountMinor().equals(amountMinor)
                    || !concurrentOrder.getDeviceType().equals(deviceType)) {
                throw new ClientException(BaseErrorCode.PAYMENT_ORDER_CONFLICT);
            }
            return toCreateResp(concurrentOrder);
        }
        log.info("支付订单创建成功: userId={}, orderNo={}, amountMinor={}, totalCredits={}, deviceType={}",
                userId, order.getOrderNo(), amountMinor, totalCredits, deviceType);
        return toCreateResp(order);
    }

    /**
     * 查询当前用户的支付订单
     *
     * @param orderNo 商户订单号
     * @return 支付订单信息
     */
    @Override
    public PaymentOrderRespDTO getOrder(String orderNo) {
        BizPaymentOrderDO order = getCurrentUserOrder(orderNo);
        expireIfNecessary(order);
        return toOrderResp(getCurrentUserOrder(orderNo));
    }

    /**
     * 获取当前用户的充值记录
     *
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 充值记录分页结果
     */
    @Override
    public PageRespDTO<RechargeRecordRespDTO> listRechargeRecords(Integer page, Integer pageSize) {
        int currentPage = page == null ? 1 : Math.max(page, 1);
        int currentPageSize = pageSize == null ? 10 : Math.min(Math.max(pageSize, 1), 100);
        String userId = getCurrentUserId();
        LambdaQueryWrapper<BizPaymentOrderDO> queryWrapper = new LambdaQueryWrapper<BizPaymentOrderDO>()
                .eq(BizPaymentOrderDO::getUserId, userId)
                .eq(BizPaymentOrderDO::getOrderStatus, PaymentConstant.ORDER_STATUS_PAID)
                .eq(BizPaymentOrderDO::getCreditStatus, PaymentConstant.CREDIT_STATUS_CREDITED);
        long total = paymentOrderMapper.selectCount(queryWrapper);
        long offset = (long) (currentPage - 1) * currentPageSize;
        List<RechargeRecordRespDTO> records = paymentOrderMapper.selectList(queryWrapper
                        .orderByDesc(BizPaymentOrderDO::getCreditedAt)
                        .last("LIMIT " + offset + ", " + currentPageSize))
                .stream()
                .map(this::toRechargeRecordResp)
                .toList();
        return PageRespDTO.of(records, total, currentPage, currentPageSize);
    }

    /**
     * 系统补偿查询支付订单
     *
     * @param orderNo 商户订单号
     */
    @Override
    public void reconcileOrder(String orderNo) {
        BizPaymentOrderDO order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<BizPaymentOrderDO>()
                .eq(BizPaymentOrderDO::getOrderNo, orderNo));
        if (order == null) {
            throw new ClientException(BaseErrorCode.PAYMENT_ORDER_NOT_FOUND);
        }
        if (Integer.valueOf(PaymentConstant.ORDER_STATUS_PAID).equals(order.getOrderStatus())
                && Integer.valueOf(PaymentConstant.CREDIT_STATUS_CREDITED).equals(order.getCreditStatus())) {
            return;
        }
        EpayOrderQueryResponse response = epayClient.queryOrder(orderNo);
        if (response == null || !Integer.valueOf(1).equals(response.getCode())) {
            throw new ClientException(BaseErrorCode.PAYMENT_PROVIDER_RESPONSE_INVALID);
        }
        if (Integer.valueOf(1).equals(response.getStatus())) {
            validateQueryResponse(order, response);
            settlementService.confirmAndCredit(new PaymentConfirmation(
                    orderNo,
                    response.getTradeNo(),
                    response.getApiTradeNo(),
                    parseAmountMinor(response.getMoney()),
                    parseProviderTime(response.getEndtime())));
        } else {
            expireIfNecessary(order);
        }
    }

    /**
     * 将到期的待支付订单标记为已过期
     *
     * @return 影响行数
     */
    @Override
    public int expirePendingOrders() {
        return paymentOrderMapper.expirePendingOrders(
                PaymentConstant.ORDER_STATUS_PENDING,
                PaymentConstant.ORDER_STATUS_EXPIRED);
    }

    /**
     * 获取待补偿处理的支付订单号
     *
     * @param limit 返回数量
     * @return 商户订单号列表
     */
    @Override
    public List<String> listReconcileOrderNos(int limit) {
        int queryLimit = Math.min(Math.max(limit, 1), 100);
        Date threshold = new Date(System.currentTimeMillis() - 60_000L);
        return paymentOrderMapper.selectList(new LambdaQueryWrapper<BizPaymentOrderDO>()
                        .and(wrapper -> wrapper
                                .eq(BizPaymentOrderDO::getOrderStatus, PaymentConstant.ORDER_STATUS_PENDING)
                                .le(BizPaymentOrderDO::getCreatedAt, threshold)
                                .or()
                                .eq(BizPaymentOrderDO::getOrderStatus, PaymentConstant.ORDER_STATUS_PAID)
                                .eq(BizPaymentOrderDO::getCreditStatus, PaymentConstant.CREDIT_STATUS_PENDING))
                        .orderByAsc(BizPaymentOrderDO::getCreatedAt)
                        .last("LIMIT " + queryLimit))
                .stream()
                .map(BizPaymentOrderDO::getOrderNo)
                .toList();
    }

    /**
     * 处理支付平台异步通知
     *
     * @param parameters 通知参数
     * @return 通知是否验证并处理成功
     */
    @Override
    public boolean handleNotify(Map<String, String> parameters) {
        if (!epaySigner.verify(parameters)) {
            return false;
        }
        if (!safeEquals(epayProperties.getMerchantId(), parameters.get("pid"))
                || !safeEquals(epayProperties.getPaymentType(), parameters.get("type"))
                || !TRADE_SUCCESS.equals(parameters.get("trade_status"))) {
            return false;
        }
        String orderNo = parameters.get("out_trade_no");
        String tradeNo = parameters.get("trade_no");
        if (orderNo == null || orderNo.isBlank() || tradeNo == null || tradeNo.isBlank()) {
            return false;
        }
        BizPaymentOrderDO order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<BizPaymentOrderDO>()
                .eq(BizPaymentOrderDO::getOrderNo, orderNo));
        if (order == null || !safeEquals(order.getSubject(), parameters.get("name"))) {
            return false;
        }
        long amountMinor = parseAmountMinor(parameters.get("money"));
        settlementService.confirmAndCredit(new PaymentConfirmation(orderNo, tradeNo, null, amountMinor, new Date()));
        log.info("支付通知处理成功: orderNo={}, providerTradeNo={}", orderNo, tradeNo);
        return true;
    }

    private PaymentOrderCreateRespDTO toCreateResp(BizPaymentOrderDO order) {
        Map<String, String> parameters = buildSubmitParameters(order);
        parameters.put("sign", epaySigner.sign(parameters));
        parameters.put("sign_type", SIGN_TYPE_MD5);
        return PaymentOrderCreateRespDTO.builder()
                .orderNo(order.getOrderNo())
                .amountMinor(order.getAmountMinor())
                .totalCredits(order.getTotalCredits())
                .orderStatus(order.getOrderStatus())
                .expiresAt(order.getExpiresAt())
                .submitUrl(buildSubmitUrl())
                .method(FORM_METHOD_POST)
                .parameters(parameters)
                .build();
    }

    private Map<String, String> buildSubmitParameters(BizPaymentOrderDO order) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("pid", epayProperties.getMerchantId());
        parameters.put("type", epayProperties.getPaymentType());
        parameters.put("out_trade_no", order.getOrderNo());
        parameters.put("notify_url", epayProperties.getNotifyUrl());
        parameters.put("return_url", epayProperties.getReturnUrl());
        parameters.put("name", order.getSubject());
        parameters.put("money", formatMoney(order.getAmountMinor()));
        parameters.put("sitename", epayProperties.getSiteName());
        parameters.put("device", order.getDeviceType());
        return parameters;
    }

    private void validateQueryResponse(BizPaymentOrderDO order, EpayOrderQueryResponse response) {
        if (!safeEquals(order.getOrderNo(), response.getOutTradeNo())
                || !safeEquals(epayProperties.getMerchantId(), response.getPid())
                || !safeEquals(epayProperties.getPaymentType(), response.getType())
                || !safeEquals(order.getSubject(), response.getName())
                || !order.getAmountMinor().equals(parseAmountMinor(response.getMoney()))) {
            throw new ClientException(BaseErrorCode.PAYMENT_PROVIDER_RESPONSE_INVALID);
        }
    }

    private BizPaymentOrderDO getCurrentUserOrder(String orderNo) {
        BizPaymentOrderDO order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<BizPaymentOrderDO>()
                .eq(BizPaymentOrderDO::getOrderNo, orderNo)
                .eq(BizPaymentOrderDO::getUserId, getCurrentUserId()));
        if (order == null) {
            throw new ClientException(BaseErrorCode.PAYMENT_ORDER_NOT_FOUND);
        }
        return order;
    }

    private void expireIfNecessary(BizPaymentOrderDO order) {
        if (Integer.valueOf(PaymentConstant.ORDER_STATUS_PENDING).equals(order.getOrderStatus())
                && order.getExpiresAt() != null && !order.getExpiresAt().after(new Date())) {
            paymentOrderMapper.expireOrderIfPending(
                    order.getId(),
                    PaymentConstant.ORDER_STATUS_PENDING,
                    PaymentConstant.ORDER_STATUS_EXPIRED);
        }
    }

    private PaymentOrderRespDTO toOrderResp(BizPaymentOrderDO order) {
        return PaymentOrderRespDTO.builder()
                .orderNo(order.getOrderNo())
                .subject(order.getSubject())
                .amountMinor(order.getAmountMinor())
                .totalCredits(order.getTotalCredits())
                .deviceType(order.getDeviceType())
                .orderStatus(order.getOrderStatus())
                .creditStatus(order.getCreditStatus())
                .expiresAt(order.getExpiresAt())
                .paidAt(order.getPaidAt())
                .creditedAt(order.getCreditedAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private RechargeRecordRespDTO toRechargeRecordResp(BizPaymentOrderDO order) {
        return RechargeRecordRespDTO.builder()
                .orderNo(order.getOrderNo())
                .amountYuan(BigDecimal.valueOf(order.getAmountMinor(), 2))
                .credits(order.getTotalCredits())
                .creditedAt(order.getCreditedAt())
                .build();
    }

    private String normalizeDevice(String deviceType) {
        String normalized = deviceType == null || deviceType.isBlank()
                ? epayProperties.getDefaultDevice()
                : deviceType.trim().toLowerCase(Locale.ROOT);
        if (normalized == null || epayProperties.getSupportedDevices() == null
                || !epayProperties.getSupportedDevices().contains(normalized)) {
            throw new ClientException(BaseErrorCode.PAYMENT_DEVICE_INVALID);
        }
        return normalized;
    }

    private long parseAmountMinor(String money) {
        try {
            return new BigDecimal(money)
                    .movePointRight(2)
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.PAYMENT_PROVIDER_RESPONSE_INVALID);
        }
    }

    private String formatMoney(long amountMinor) {
        return BigDecimal.valueOf(amountMinor, 2)
                .setScale(2, RoundingMode.UNNECESSARY)
                .toPlainString();
    }

    private Date parseProviderTime(String value) {
        if (value == null || value.isBlank()) {
            return new Date();
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).parse(value);
        } catch (Exception e) {
            return new Date();
        }
    }

    private String buildSubmitUrl() {
        String baseUrl = epayProperties.getBaseUrl();
        String path = epayProperties.getSubmitPath();
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }

    private void validateCreateConfig() {
        if (epayProperties.getBaseUrl() == null || epayProperties.getBaseUrl().isBlank()
                || epayProperties.getSubmitPath() == null || epayProperties.getSubmitPath().isBlank()
                || epayProperties.getMerchantId() == null || epayProperties.getMerchantId().isBlank()
                || epayProperties.getMerchantKey() == null || epayProperties.getMerchantKey().isBlank()
                || epayProperties.getNotifyUrl() == null || epayProperties.getNotifyUrl().isBlank()
                || epayProperties.getReturnUrl() == null || epayProperties.getReturnUrl().isBlank()
                || epayProperties.getSubject() == null || epayProperties.getSubject().isBlank()) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
    }

    private int getOrderExpireMinutes() {
        Integer value = epayProperties.getOrderExpireMinutes();
        return value == null || value <= 0 ? 5 : value;
    }

    private long getCreditsPerYuan() {
        Long value = billingProperties.getCreditsPerYuan();
        if (value == null || value <= 0) {
            throw new ClientException(BaseErrorCode.PAYMENT_CONFIG_INVALID);
        }
        return value;
    }

    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }

    private boolean safeEquals(String expected, String actual) {
        return expected != null && expected.equals(actual);
    }
}
