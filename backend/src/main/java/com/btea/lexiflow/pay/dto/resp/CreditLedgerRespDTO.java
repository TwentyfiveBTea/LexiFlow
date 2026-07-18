package com.btea.lexiflow.pay.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 文章Credits使用记录响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditLedgerRespDTO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 本篇文章实际使用的Credits总量
     */
    private Long totalCredits;

    /**
     * OCR实际使用的Credits
     */
    private Long ocrCredits;

    /**
     * 翻译实际使用的Credits，包含Global Profile和正文分块翻译
     */
    private Long translationCredits;

    /**
     * Credits结算完成时间
     */
    private Date completedAt;
}
