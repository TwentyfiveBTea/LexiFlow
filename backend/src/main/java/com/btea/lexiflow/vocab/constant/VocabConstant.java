package com.btea.lexiflow.vocab.constant;

import java.math.BigDecimal;
import java.util.Map;
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

    /**
     * 英语词汇等级
     */
    public static final Set<String> ENGLISH_LEVELS = Set.of(
            "BEC", "CET4", "CET6", "GMAT", "GRE", "IELTS", "LEVEL4", "LEVEL8", "SAT", "TOEFL",
            "CHUZHONG", "GAOZHONG", "KAOYAN", "POSTGRADUATE");

    /**
     * 英语API等级与数据库等级的映射
     */
    public static final Map<String, String> ENGLISH_LEVEL_DATABASE_VALUES = Map.ofEntries(
            Map.entry("BEC", "BEC"),
            Map.entry("CET4", "CET-4"),
            Map.entry("CET6", "CET-6"),
            Map.entry("GMAT", "GMAT"),
            Map.entry("GRE", "GRE"),
            Map.entry("IELTS", "雅思"),
            Map.entry("LEVEL4", "专四"),
            Map.entry("LEVEL8", "专八"),
            Map.entry("SAT", "SAT"),
            Map.entry("TOEFL", "托福"),
            Map.entry("CHUZHONG", "初中"),
            Map.entry("GAOZHONG", "高中"),
            Map.entry("KAOYAN", "考研"),
            Map.entry("POSTGRADUATE", "考研"));

    /**
     * 英语数据库等级与API等级的映射
     */
    public static final Map<String, String> ENGLISH_LEVEL_API_VALUES = Map.ofEntries(
            Map.entry("BEC", "BEC"),
            Map.entry("CET-4", "CET4"),
            Map.entry("CET-6", "CET6"),
            Map.entry("GMAT", "GMAT"),
            Map.entry("GRE", "GRE"),
            Map.entry("雅思", "IELTS"),
            Map.entry("专四", "LEVEL4"),
            Map.entry("专八", "LEVEL8"),
            Map.entry("SAT", "SAT"),
            Map.entry("托福", "TOEFL"),
            Map.entry("初中", "CHUZHONG"),
            Map.entry("高中", "GAOZHONG"),
            Map.entry("考研", "KAOYAN"));

    /**
     * 日语词汇等级
     */
    public static final Set<String> JAPANESE_LEVELS = Set.of("N5", "N4", "N3", "N2", "N1");

}
