package com.btea.lexiflow.article.dto.resp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:05
 * @Description: 文章段落翻译结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslatedParagraphDTO {

    /**
     * 段落序号
     */
    private Integer index;

    /**
     * 原文段落
     */
    private String source;

    /**
     * 中文译文
     */
    @JsonAlias("translation")
    private String translation;
}
