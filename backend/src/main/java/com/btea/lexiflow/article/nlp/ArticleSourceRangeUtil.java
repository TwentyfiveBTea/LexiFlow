package com.btea.lexiflow.article.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/31
 * @Description: 文章原文范围提取工具类
 */
public final class ArticleSourceRangeUtil {

    private static final Pattern BLOCK_SEPARATOR = Pattern.compile("\\n[ \\t]*\\n");

    private ArticleSourceRangeUtil() {
    }

    /**
     * 提取文章中需要参与词汇分析的原文范围
     *
     * @param content 解析后的文章正文
     * @param translated 是否按原文和译文交替结构存储
     * @return 原文片段列表，偏移量基于完整正文
     */
    public static List<SourceSegment> extract(String content, boolean translated) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        List<SourceSegment> result = new ArrayList<>();
        Matcher matcher = BLOCK_SEPARATOR.matcher(content);
        int blockStart = 0;
        while (matcher.find()) {
            addBlock(result, content, blockStart, matcher.start(), translated);
            blockStart = matcher.end();
        }
        addBlock(result, content, blockStart, content.length(), translated);
        return result;
    }

    private static void addBlock(List<SourceSegment> result, String content,
                                 int blockStart, int blockEnd, boolean translated) {
        int sourceStart = blockStart;
        int sourceEnd = blockEnd;
        if (translated) {
            int lineBreak = content.indexOf('\n', blockStart);
            if (lineBreak >= blockStart && lineBreak < blockEnd) {
                sourceEnd = lineBreak;
            }
        }
        while (sourceStart < sourceEnd && Character.isWhitespace(content.charAt(sourceStart))) {
            sourceStart++;
        }
        while (sourceEnd > sourceStart && Character.isWhitespace(content.charAt(sourceEnd - 1))) {
            sourceEnd--;
        }
        if (sourceStart < sourceEnd) {
            result.add(new SourceSegment(content.substring(sourceStart, sourceEnd), sourceStart, sourceEnd));
        }
    }

    /**
     * 原文片段及其在完整正文中的绝对范围
     *
     * @param text 原文片段
     * @param startOffset 开始偏移量
     * @param endOffset 结束偏移量
     */
    public record SourceSegment(String text, int startOffset, int endOffset) {
    }
}
