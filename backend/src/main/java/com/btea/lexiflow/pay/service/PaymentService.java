package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.dto.req.PaymentOrderCreateReqDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderCreateRespDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderRespDTO;

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

    /**
     * 查询当前用户的支付订单
     *
     * @param orderNo 商户订单号
     * @return 支付订单信息
     */
    PaymentOrderRespDTO getOrder(String orderNo);
}
