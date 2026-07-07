package com.btea.lexiflow.article.nlp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/5 02:03
 * @Description: 文章词汇出现位置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleVocabOccurrence {

    /**
     * 文章中实际出现的文本
     */
    private String matchedText;

    /**
     * 标准化后的出现文本
     */
    private String normalizedText;

    /**
     * NLP 工具返回的原始词性标记
     */
    private String posTag;

    /**
     * 统一词性类型
     */
    private String posType;

    /**
     * 形态学特征
     */
    private String morphFeatures;

    /**
     * 所在句子
     */
    private String sentence;

    /**
     * 句子在纯文本中的开始位置
     */
    private Integer sentenceStartOffset;

    /**
     * 句子在纯文本中的结束位置
     */
    private Integer sentenceEndOffset;

    /**
     * 词汇在纯文本中的开始位置
     */
    private Integer startOffset;

    /**
     * 词汇在纯文本中的结束位置
     */
    private Integer endOffset;

    /**
     * 分析工具来源
     */
    private String analysisProvider;

    /**
     * 分析工具或模型版本
     */
    private String analysisVersion;
}
