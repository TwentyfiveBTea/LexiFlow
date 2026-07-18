package com.btea.lexiflow.pay.integration.epay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 易支付订单查询响应参数
 */
@Data
public class EpayOrderQueryResponse {

    /**
     * 返回状态码，1表示查询成功
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String msg;

    /**
     * 易支付订单号
     */
    @JsonProperty("trade_no")
    private String tradeNo;

    /**
     * 商户订单号
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;

    /**
     * 支付宝侧订单号
     */
    @JsonProperty("api_trade_no")
    private String apiTradeNo;

    /**
     * 支付方式
     */
    private String type;

    /**
     * 商户ID
     */
    private String pid;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 支付金额，单位为元
     */
    private String money;

    /**
     * 支付状态：0=未支付，1=支付成功
     */
    private Integer status;

    /**
     * 支付平台订单创建时间
     */
    private String addtime;

    /**
     * 支付平台完成交易时间
     */
    private String endtime;
}
