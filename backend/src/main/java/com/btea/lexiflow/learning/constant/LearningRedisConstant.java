/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 单词复习Redis常量
 */
package com.btea.lexiflow.learning.constant;

import java.time.Duration;

/**
 * 单词复习Redis常量。
 */
public final class LearningRedisConstant {

    private LearningRedisConstant() {
    }

    /**
     * 复习队列缓存键前缀
     */
    public static final String REVIEW_QUEUE_KEY_PREFIX = "lexiflow:learning:review-queue:";

    /**
     * 复习队列滑动过期时间
     */
    public static final Duration REVIEW_QUEUE_TTL = Duration.ofHours(24);
}
