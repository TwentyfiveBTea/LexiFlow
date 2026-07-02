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
 * @Date: 2026/7/2 17:32
 * @Description: 文章命中词汇汇总实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("rel_article_vocab")
public class RelArticleVocabDO {

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
     * 词条原型或标准写法
     */
    private String baseWord;

    /**
     * 文章中出现的词形，例如 ["studying", "studied"]
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
     * 首次出现开始位置，基于解析后纯文本的 UTF-16 字符偏移量
     */
    private Integer firstStartOffset;

    /**
     * 首次出现结束位置，基于解析后纯文本的 UTF-16 字符偏移量
     */
    private Integer firstEndOffset;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
