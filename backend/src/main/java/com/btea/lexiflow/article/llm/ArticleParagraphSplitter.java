package com.btea.lexiflow.article.llm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:07
 * @Description: 文章段落切分器
 */
@Component
public class ArticleParagraphSplitter {

    /**
     * 按自然段落切分文本
     *
     * @param text 原文文本
     * @return 自然段落列表
     */
    public List<String> splitParagraphs(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }

        String normalizedText = text.replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
        String[] paragraphs = normalizedText.split("\\n\\s*\\n");
        for (String paragraph : paragraphs) {
            String normalizedParagraph = paragraph.replaceAll("[ \\t]+", " ")
                    .replaceAll("\\n+", " ")
                    .trim();
            if (!normalizedParagraph.isBlank()) {
                result.add(normalizedParagraph);
            }
        }
        return result;
    }
}
