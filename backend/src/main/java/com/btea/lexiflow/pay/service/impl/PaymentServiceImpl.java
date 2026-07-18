package com.btea.lexiflow.pay.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.config.CreditBillingProperties;
import com.btea.lexiflow.pay.config.EpayProperties;
import com.btea.lexiflow.pay.constant.PaymentConstant;
import com.btea.lexiflow.pay.dao.entity.BizPaymentOrderDO;
import com.btea.lexiflow.pay.dao.mapper.BizPaymentOrderMapper;
import com.btea.lexiflow.pay.dto.req.PaymentOrderCreateReqDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderCreateRespDTO;
import com.btea.lexiflow.pay.integration.epay.EpaySigner;
import com.btea.lexiflow.pay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.LinkedHashMap;
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
    private final BizPaymentOrderMapper paymentOrderMapper;
    private final EpaySigner epaySigner;
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
    private String formatMoney(long amountMinor) {
        return BigDecimal.valueOf(amountMinor, 2)
                .setScale(2, RoundingMode.UNNECESSARY)
                .toPlainString();
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
}
