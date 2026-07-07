package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 17:12
 * @Description: 文章词汇出现位置响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleVocabOccurrenceRespDTO {

    /**
     * 出现位置ID
     */
    private String occurrenceId;

    /**
     * 文章命中词汇汇总ID
     */
    private String articleVocabId;

    /**
     * 词库单词ID
     */
    private Long wordId;

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
     * 所在句子
     */
    private String sentence;

    /**
     * 词汇在纯文本中的开始位置
     */
    private Integer startOffset;

    /**
     * 词汇在纯文本中的结束位置
     */
    private Integer endOffset;
}
