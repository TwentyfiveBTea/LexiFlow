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
 * @Description: AI模型Token用量与Credits成本实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_ai_usage")
public class BizAiUsageDO {

    /**
     * AI用量记录ID（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 本次文章处理编号
     */
    private String processingNo;

    /**
     * 逻辑模型请求编号
     */
    private String requestNo;

    /**
     * 当前逻辑请求的第几次调用尝试
     */
    private Integer attemptNo;

    /**
     * 调用场景：1=PDF OCR，2=Global Profile，3=正文分块翻译
     */
    private Integer scene;

    /**
     * OCR页码或翻译分块序号，Global Profile时为空
     */
    private Integer unitIndex;

    /**
     * 模型供应商，例如GEMINI
     */
    private String provider;

    /**
     * 实际使用的模型名称
     */
    private String modelName;

    /**
     * 模型供应商返回的请求ID
     */
    private String providerRequestId;

    /**
     * 输入Token数
     */
    private Long inputTokens;

    /**
     * 输出Token数
     */
    private Long outputTokens;

    /**
     * 每百万输入Token对应的Credits费率快照
     */
    private Long inputRatePerMillion;

    /**
     * 每百万输出Token对应的Credits费率快照
     */
    private Long outputRatePerMillion;

    /**
     * 本次模型调用实际消耗的Credits
     */
    private Long creditsCost;

    /**
     * 请求状态：0=请求中，1=请求成功，2=请求失败，3=结果未知
     */
    private Integer requestStatus;

    /**
     * 计费状态：0=待结算，1=已计入结算，2=免单
     */
    private Integer billingStatus;

    /**
     * 请求失败原因摘要，不保存完整正文、OCR文本或图片数据
     */
    private String errorMessage;

    /**
     * 模型调用完成或失败时间
     */
    private Date completedAt;

    /**
     * 模型调用开始时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 记录更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
