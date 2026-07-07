package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:05
 * @Description: 文章翻译文本块
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslationChunkDTO {

    /**
     * 文本块序号
     */
    private Integer chunkIndex;

    /**
     * 当前块在全文中的起始段落序号
     */
    private Integer startParagraphIndex;

    /**
     * 当前块段落列表
     */
    private List<String> paragraphs;
}
