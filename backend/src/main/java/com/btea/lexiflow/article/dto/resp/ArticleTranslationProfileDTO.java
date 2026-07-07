package com.btea.lexiflow.article.dto.resp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:32
 * @Description: 文章翻译全局上下文资料
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslationProfileDTO {

    /**
     * 文本类型：news/magazine/foreign_article/general_reading/fiction/essay/other
     */
    @JsonAlias("text_type")
    private String textType;

    /**
     * 文章主题
     */
    private String topic;

    /**
     * 文章语气
     */
    private String tone;

    /**
     * 目标读者
     */
    @JsonAlias("target_audience")
    private String targetAudience;

    /**
     * 核心论点
     */
    @JsonAlias("main_argument")
    private String mainArgument;

    /**
     * 全文摘要
     */
    private String summary;

    /**
     * 术语表
     */
    private List<ArticleTranslationTermDTO> terms;

    /**
     * 实体关系
     */
    private List<ArticleTranslationEntityDTO> entities;

    /**
     * 翻译风格规则
     */
    @JsonAlias("style_rules")
    private List<String> styleRules;
}
