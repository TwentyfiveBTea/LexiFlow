package com.btea.lexiflow.vocab.util;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.vocab.constant.VocabConstant;

import java.util.Locale;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/31
 * @Description: 词汇等级转换工具类
 */
public final class VocabLevelUtil {

    private VocabLevelUtil() {
    }

    /**
     * 标准化API词汇等级
     *
     * @param languageCode 语言编码
     * @param level 词汇等级
     * @return 标准化后的API等级，未指定时返回null
     */
    public static String normalizeApiLevel(String languageCode, String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        String normalized = level.trim().toUpperCase(Locale.ROOT);
        if ("ja".equals(languageCode)) {
            if (!VocabConstant.JAPANESE_LEVELS.contains(normalized)) {
                throw new ClientException(BaseErrorCode.VOCAB_LEVEL_NOT_SUPPORTED);
            }
            return normalized;
        }
        if (!VocabConstant.ENGLISH_LEVELS.contains(normalized)) {
            throw new ClientException(BaseErrorCode.VOCAB_LEVEL_NOT_SUPPORTED);
        }
        return "POSTGRADUATE".equals(normalized) ? "KAOYAN" : normalized;
    }

    /**
     * 将API词汇等级转换为数据库等级
     *
     * @param languageCode 语言编码
     * @param level API词汇等级
     * @return 数据库词汇等级，未指定时返回null
     */
    public static String toDatabaseLevel(String languageCode, String level) {
        String normalized = normalizeApiLevel(languageCode, level);
        if (normalized == null || "ja".equals(languageCode)) {
            return normalized;
        }
        String databaseLevel = VocabConstant.ENGLISH_LEVEL_DATABASE_VALUES.get(normalized);
        if (databaseLevel == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LEVEL_NOT_SUPPORTED);
        }
        return databaseLevel;
    }

    /**
     * 将数据库词汇等级转换为API等级
     *
     * @param languageCode 语言编码
     * @param level 数据库词汇等级
     * @return API词汇等级，未指定时返回null
     */
    public static String toApiLevel(String languageCode, String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        String trimmed = level.trim();
        if ("ja".equals(languageCode)) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        String apiLevel = VocabConstant.ENGLISH_LEVEL_API_VALUES.get(trimmed);
        return apiLevel == null ? trimmed.toUpperCase(Locale.ROOT) : apiLevel;
    }
}
