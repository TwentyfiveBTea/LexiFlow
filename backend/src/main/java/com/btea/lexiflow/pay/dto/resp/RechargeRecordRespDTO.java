package com.btea.lexiflow.pay.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/18
 * @Description: 用户充值记录响应参数
 */
@Data
@Builder
public class RechargeRecordRespDTO {

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 充值金额，单位为元
     */
    private BigDecimal amountYuan;

    /**
     * 到账Credits数量
     */
    private Long credits;

    /**
     * Credits到账时间
     */
    private Date creditedAt;
}
