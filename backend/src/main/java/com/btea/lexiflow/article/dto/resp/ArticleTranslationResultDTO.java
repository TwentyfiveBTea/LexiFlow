package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:12
 * @Description: 文章翻译结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslationResultDTO {

    /**
     * 按自然段落拼接后的原文和中文译文
     */
    private String parsedContent;

    /**
     * 翻译状态
     */
    private Integer translationStatus;

    /**
     * 是否执行了翻译
     */
    private Boolean translated;
}
