package com.btea.lexiflow.pay.controller;

import com.btea.lexiflow.pay.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 易支付异步通知控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pay")
public class PaymentNotifyController {

    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";

    private final PaymentService paymentService;

    /**
     * 通过GET接收易支付异步通知
     */
    @GetMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyByGet(HttpServletRequest request) {
        return handleNotify(request);
    }

    /**
     * 通过POST接收易支付异步通知
     */
    @PostMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notifyByPost(HttpServletRequest request) {
        return handleNotify(request);
    }

    private String handleNotify(HttpServletRequest request) {
        try {
            Map<String, String> parameters = extractSingleValueParameters(request);
            return paymentService.handleNotify(parameters) ? SUCCESS : FAIL;
        } catch (Exception e) {
            log.warn("支付通知处理失败: errorType={}", e.getClass().getSimpleName());
            return FAIL;
        }
    }

    private Map<String, String> extractSingleValueParameters(HttpServletRequest request) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] values = entry.getValue();
            if (values == null || values.length != 1) {
                throw new IllegalArgumentException("支付通知参数重复");
            }
            result.put(entry.getKey(), values[0]);
        }
        return result;
    }
}
