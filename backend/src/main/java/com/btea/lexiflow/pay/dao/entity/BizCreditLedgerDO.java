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
 * @Description: Credits变动流水实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_credit_ledger")
public class BizCreditLedgerDO {

    /**
     * Credits流水ID（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 流水类型：1=支付入账，2=系统赠送，3=Credits预占，4=预占结算，5=预占释放，6=人工调整
     */
    private Integer transactionType;

    /**
     * 可用Credits变化量，可正可负
     */
    private Long availableDelta;

    /**
     * 冻结Credits变化量，可正可负
     */
    private Long frozenDelta;

    /**
     * 变动后的可用Credits余额
     */
    private Long availableBalanceAfter;

    /**
     * 变动后的冻结Credits余额
     */
    private Long frozenBalanceAfter;

    /**
     * 业务类型：PAYMENT、SIGNUP_GRANT、ARTICLE_PROCESSING、ADMIN_ADJUSTMENT等
     */
    private String businessType;

    /**
     * 关联业务ID
     */
    private String businessId;

    /**
     * 幂等键，防止重复入账或重复扣费
     */
    private String idempotencyKey;

    /**
     * 流水说明
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
}
