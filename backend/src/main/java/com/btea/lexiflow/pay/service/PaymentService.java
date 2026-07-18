package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.dto.req.PaymentOrderCreateReqDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderCreateRespDTO;
import com.btea.lexiflow.pay.dto.resp.PaymentOrderRespDTO;
import com.btea.lexiflow.pay.dto.resp.RechargeRecordRespDTO;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取当前用户的充值记录
     *
     * @param limit 返回数量
     * @return 充值记录列表
     */
    List<RechargeRecordRespDTO> listRechargeRecords(Integer limit);

    /**
     * 处理支付平台异步通知
     *
     * @param parameters 通知参数
     * @return 通知是否验证并处理成功
     */
    boolean handleNotify(Map<String, String> parameters);
}
