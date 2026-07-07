package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 17:30
 * @Description: 文章列表响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleListRespDTO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章主语言标识
     */
    private String languageCode;

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
     * 创建时间
     */
    private Date createdAt;
}
