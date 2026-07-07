package com.btea.lexiflow.article.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:07
 * @Description: 文章翻译配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "lexiflow.article.translation")
public class ArticleTranslationProperties {

    /**
     * 是否启用上传后自动翻译
     */
    private Boolean enabled;

    /**
     * 翻译目标语言
     */
    private String targetLanguage;

    /**
     * 单个文本块最多包含的自然段落数
     */
    private Integer maxParagraphsPerChunk;

    /**
     * 单个文本块目标 token 上限，仅作为配置记录
     */
    private Integer maxTokensPerChunk;

    /**
     * 前一文本块结尾参考句数
     */
    private Integer overlapTailSentences;

    /**
     * 是否启用全局上下文资料
     */
    private Boolean globalProfileEnabled;

    /**
     * 是否启用动态术语表
     */
    private Boolean dynamicTermsEnabled;

    /**
     * 是否启用一致性审校
     */
    private Boolean consistencyReviewEnabled;

    /**
     * LLM 调用失败重试次数
     */
    private Integer maxRetryTimes;
}
