package com.btea.lexiflow.article.nlp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/5 01:45
 * @Description: 文章词汇命中结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleVocabMatch {

    /**
     * 词库单词ID
     */
    private Long wordId;

    /**
     * 词条原型或标准写法
     */
    private String baseWord;

    /**
     * 文章中出现的词形集合
     */
    private Set<String> matchedForms;

    /**
     * 出现位置列表
     */
    private List<ArticleVocabOccurrence> occurrences;
}
