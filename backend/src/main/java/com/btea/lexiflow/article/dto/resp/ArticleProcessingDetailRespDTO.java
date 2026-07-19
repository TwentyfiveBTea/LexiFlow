package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/19
 * @Description: 文章处理详情响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleProcessingDetailRespDTO {

    /**
     * 词数统计
     */
    private Integer wordCount;

    /**
     * 解析状态
     */
    private Integer parseStatus;

    /**
     * 翻译状态
     */
    private Integer translationStatus;

    /**
     * 词汇分析状态
     */
    private Integer analysisStatus;

    /**
     * 解析完成时间
     */
    private Date parsedAt;

    /**
     * 翻译完成时间
     */
    private Date translatedAt;

    /**
     * 最近一次词汇分析完成时间
     */
    private Date analyzedAt;
}
