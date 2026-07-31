package com.btea.lexiflow.vocab.constant;

import java.time.Duration;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 词汇Redis缓存常量
 */
public final class VocabRedisConstant {

    private VocabRedisConstant() {
    }

    /**
     * 词汇缓存键前缀
     */
    public static final String CACHE_KEY_PREFIX = "lexiflow:cache:v2:vocab:";

    /**
     * 空结果缓存时间
     */
    public static final Duration EMPTY_CACHE_TTL = Duration.ofMinutes(1);

    /**
     * 词汇库缓存时间
     */
    public static final Duration LIBRARY_CACHE_TTL = Duration.ofMinutes(5);

    /**
     * 词汇库单词列表缓存时间
     */
    public static final Duration LIBRARY_WORD_CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 词汇库统计数据缓存时间
     */
    public static final Duration STATISTICS_CACHE_TTL = Duration.ofMinutes(1);

    /**
     * 词典词条缓存时间
     */
    public static final Duration WORD_CACHE_TTL = Duration.ofHours(24);

    /**
     * 用户词汇等级缓存时间
     */
    public static final Duration WORD_LEVEL_CACHE_TTL = Duration.ofHours(6);
}
