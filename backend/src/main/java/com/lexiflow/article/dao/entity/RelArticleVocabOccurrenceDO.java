package com.lexiflow.article.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:23
 * @Description: 文章词汇出现位置实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("rel_article_vocab_occurrence")
public class RelArticleVocabOccurrenceDO {

    /**
     * 唯一主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 文章命中词汇汇总ID
     */
    private String articleVocabId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 词库单词ID，结合 language_code 指向对应词表
     */
    private Long wordId;

    /**
     * 语言标识：en/ja/fr/de/es/zh
     */
    private String languageCode;

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
     * 统一词性类型：noun/proper_noun/verb/aux/adj/adv/pron/det/adp/num/conj/particle/interj/punct/symbol/other
     */
    private String posType;

    /**
     * 形态学特征，如时态、数、性、格等
     */
    private String morphFeatures;

    /**
     * 所在句子
     */
    private String sentence;

    /**
     * 句子在纯文本中的开始位置，UTF-16 字符偏移量
     */
    private Integer sentenceStartOffset;

    /**
     * 句子在纯文本中的结束位置，UTF-16 字符偏移量
     */
    private Integer sentenceEndOffset;

    /**
     * 词汇在纯文本中的开始位置，UTF-16 字符偏移量
     */
    private Integer startOffset;

    /**
     * 词汇在纯文本中的结束位置，UTF-16 字符偏移量
     */
    private Integer endOffset;

    /**
     * 分析工具来源，如 corenlp/kuromoji/spacy/stanza
     */
    private String analysisProvider;

    /**
     * 分析工具或模型版本
     */
    private String analysisVersion;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
}
