package com.btea.lexiflow.pay.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_payment_order")
public class BizPaymentOrderDO {

    /**
     * 支付订单ID（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * LexiFlow商户订单号，对应易支付out_trade_no
     */
    private String orderNo;

    /**
     * 下单用户ID
     */
    private String userId;

    /**
     * 商品名称，对应易支付name
     */
    private String subject;

    /**
     * 支付金额，单位为分
     */
    private Long amountMinor;

    /**
     * 支付成功后应入账的Credits数量
     */
    private Long totalCredits;

    /**
     * 发起支付的设备类型，对应易支付device，例如pc、mobile、qq、wechat、alipay
     */
    private String deviceType;

    /**
     * 易支付平台订单号，对应trade_no
     */
    private String providerTradeNo;

    /**
     * 支付宝侧订单号，对应api_trade_no
     */
    private String upstreamTradeNo;

    /**
     * 客户端创建订单幂等请求号
     */
    private String clientRequestNo;

    /**
     * 订单状态：0=待支付，1=已支付，2=已过期，3=支付异常
     */
    private Integer orderStatus;

    /**
     * Credits入账状态：0=未入账，1=已入账
     */
    private Integer creditStatus;

    /**
     * 订单过期时间
     */
    private Date expiresAt;

    /**
     * 支付成功确认时间
     */
    private Date paidAt;

    /**
     * Credits成功入账时间
     */
    private Date creditedAt;

    /**
     * 订单创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 订单更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
