package com.btea.lexiflow.common.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 13:54
 * @Description: Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 缓存过期时间随机抖动比例
     */
    public static final double CACHE_TTL_JITTER_RATIO = 0.1D;

    /**
     * 用户 Token 缓存 key 前缀
     */
    public static final String USER_TOKEN_KEY_PREFIX = "lexiflow:user:token:";
}
