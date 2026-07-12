package com.btea.lexiflow.vocab.constant;

import java.math.BigDecimal;
import java.util.Set;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库业务常量
 */
public final class VocabConstant {

    private VocabConstant() {
    }

    /**
     * 已删除状态
     */
    public static final int STATUS_DELETED = 0;

    /**
     * 正常状态
     */
    public static final int STATUS_NORMAL = 1;

    /**
     * 新词状态
     */
    public static final int WORD_STATUS_NEW = 0;

    /**
     * 学习中状态
     */
    public static final int WORD_STATUS_LEARNING = 1;

    /**
     * 已掌握状态
     */
    public static final int WORD_STATUS_MASTERED = 2;

    /**
     * 默认记忆难度系数
     */
    public static final BigDecimal DEFAULT_EASINESS_FACTOR = new BigDecimal("2.50");

    /**
     * 最小记忆难度系数
     */
    public static final BigDecimal MIN_EASINESS_FACTOR = new BigDecimal("1.30");

    /**
     * 掌握词汇所需复习次数
     */
    public static final int MASTERED_REVIEW_COUNT = 3;

    /**
     * 掌握词汇所需最低复习质量
     */
    public static final int MASTERED_MIN_QUALITY = 4;

    /**
     * 支持的语言标识集合
     */
    public static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "ja");
}
