package com.btea.lexiflow.article.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 18:45
 * @Description: 文章业务常量
 */
public final class ArticleConstant {

    private ArticleConstant() {
    }

    /**
     * 文章原文件 OSS 目录
     */
    public static final String ARTICLE_DIR = "article/";

    /**
     * 文章已删除状态
     */
    public static final int STATUS_DELETED = 0;

    /**
     * 文章正常状态
     */
    public static final int STATUS_NORMAL = 1;

    /**
     * 文章解析中
     */
    public static final int PARSE_STATUS_PROCESSING = 1;

    /**
     * 文章解析成功
     */
    public static final int PARSE_STATUS_SUCCESS = 2;

    /**
     * 文章解析失败
     */
    public static final int PARSE_STATUS_FAILED = 3;

    /**
     * 待翻译
     */
    public static final int TRANSLATION_STATUS_PENDING = 0;

    /**
     * 翻译中
     */
    public static final int TRANSLATION_STATUS_PROCESSING = 1;

    /**
     * 翻译成功
     */
    public static final int TRANSLATION_STATUS_SUCCESS = 2;

    /**
     * 翻译失败
     */
    public static final int TRANSLATION_STATUS_FAILED = 3;

    /**
     * 词汇待分析
     */
    public static final int ANALYSIS_STATUS_PENDING = 0;

    /**
     * 词汇分析中
     */
    public static final int ANALYSIS_STATUS_PROCESSING = 1;

    /**
     * 词汇分析成功
     */
    public static final int ANALYSIS_STATUS_SUCCESS = 2;

    /**
     * 词汇分析失败
     */
    public static final int ANALYSIS_STATUS_FAILED = 3;

    /**
     * 词库查询批处理大小
     */
    public static final int VOCAB_QUERY_BATCH_SIZE = 500;
}
