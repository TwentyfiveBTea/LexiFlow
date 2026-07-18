package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.dto.req.PaymentOrderCreateReqDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderCreateRespDTO;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付订单
     *
     * @param reqDTO 支付订单创建请求
     * @return 支付订单创建结果
     */
    PaymentOrderCreateRespDTO createOrder(PaymentOrderCreateReqDTO reqDTO);
}
