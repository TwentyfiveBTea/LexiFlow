package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:03
 * @Description: 文章翻译术语
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslationTermDTO {

    /**
     * 原文术语
     */
    private String source;

    /**
     * 中文译法
     */
    private String target;

    /**
     * 译法说明
     */
    private String note;

    public String key() {
        return source == null ? "" : source.trim().toLowerCase();
    }
}
