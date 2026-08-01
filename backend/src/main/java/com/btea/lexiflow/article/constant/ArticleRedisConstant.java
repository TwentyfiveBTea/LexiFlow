package com.btea.lexiflow.article.constant;

import java.time.Duration;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 文章Redis缓存常量
 */
public final class ArticleRedisConstant {

    private ArticleRedisConstant() {
    }

    /**
     * 文章上传会话缓存键前缀
     */
    public static final String UPLOAD_SESSION_KEY_PREFIX = "lexiflow:article:upload-session:";

    /**
     * 文章上传会话有效时间
     */
    public static final Duration UPLOAD_SESSION_TTL = Duration.ofMinutes(30);

    /**
     * S3预签名上传地址有效时间
     */
    public static final Duration PRESIGNED_UPLOAD_TTL = Duration.ofMinutes(15);

    /**
     * 文章缓存键前缀
     */
    public static final String CACHE_KEY_PREFIX = "lexiflow:cache:v2:article:";

    /**
     * 空结果缓存时间
     */
    public static final Duration EMPTY_CACHE_TTL = Duration.ofMinutes(1);

    /**
     * 文章列表缓存时间
     */
    public static final Duration LIST_CACHE_TTL = Duration.ofMinutes(5);

    /**
     * 文章详情缓存时间
     */
    public static final Duration DETAIL_CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 文章处理状态缓存时间
     */
    public static final Duration PROCESSING_CACHE_TTL = Duration.ofSeconds(2);

    /**
     * 文章词汇数据缓存时间
     */
    public static final Duration VOCAB_CACHE_TTL = Duration.ofHours(2);
}
