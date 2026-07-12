package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:43
 * @Description: 文章命中词汇响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleVocabRespDTO {

    /**
     * 文章命中词汇汇总ID
     */
    private String articleVocabId;

    /**
     * 词库单词ID
     */
    private Long wordId;

    /**
     * 语言标识
     */
    private String languageCode;

    /**
     * 词条原型或标准写法
     */
    private String baseWord;

    /**
     * 文章中出现的词形 JSON
     */
    private String matchedForms;

    /**
     * 出现次数
     */
    private Integer occurrenceCount;

    /**
     * 首次出现的原文词形
     */
    private String firstMatchedText;

    /**
     * 首次出现的句子
     */
    private String firstSentence;

    /**
     * 翻译列表 JSON
     */
    private String translations;

    /**
     * 美式音标
     */
    private String us;

    /**
     * 英式音标
     */
    private String uk;

    /**
     * 日语假名读音
     */
    private String kana;
}
