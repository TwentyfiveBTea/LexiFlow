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
 * @Description: Credits预占记录实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_credit_reservation")
public class BizCreditReservationDO {

    /**
     * Credits预占记录ID（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 本次文章处理编号
     */
    private String processingNo;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 本次文章处理累计预占的Credits
     */
    private Long reservedCredits;

    /**
     * 最终实际消费的Credits
     */
    private Long consumedCredits;

    /**
     * 最终退回用户的Credits
     */
    private Long releasedCredits;

    /**
     * 预占状态：0=预占中，1=已结算，2=已释放，3=已超时释放
     */
    private Integer status;

    /**
     * 预占超时时间，用于异常任务补偿释放
     */
    private Date expiresAt;

    /**
     * 结算或释放完成时间
     */
    private Date completedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
