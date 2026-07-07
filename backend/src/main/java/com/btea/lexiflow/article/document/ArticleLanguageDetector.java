package com.btea.lexiflow.article.document;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/5 01:58
 * @Description: 文章语言识别器
 */
@Component
public class ArticleLanguageDetector {

    private static final String UNKNOWN_LANGUAGE = "unknown";

    /**
     *   如果是 ja / zh / ko，就按字符或数字数量统计。
     *   如果是其他语言，就按空格分词统计。
     */
    private static final Set<String> CJK_LANGUAGES = Set.of("ja", "zh", "ko");

    private final LanguageDetector languageDetector = LanguageDetectorBuilder.fromAllLanguages().build();

    /**
     * 识别文章主语言
     *
     * @param text 文章纯文本
     * @return ISO 639-1 语言标识
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return UNKNOWN_LANGUAGE;
        }
        Language language = languageDetector.detectLanguageOf(text);
        if (language == null || Language.UNKNOWN.equals(language)) {
            return UNKNOWN_LANGUAGE;
        }
        return language.getIsoCode639_1().toString().toLowerCase(Locale.ROOT);
    }

    /**
     * 统计文章词数
     *
     * @param text 文章纯文本
     * @param languageCode 语言标识
     * @return 词数
     */
    public Integer countWords(String text, String languageCode) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        if (CJK_LANGUAGES.contains(languageCode)) {
            return (int) text.codePoints()
                    .filter(Character::isLetterOrDigit)
                    .count();
        }
        return text.trim().split("\\s+").length;
    }
}
